package com.usc.spark.traffic

import java.text.DecimalFormat
import java.util

import com.usc.spark.conf.ConfigurationManager
import com.usc.spark.constant.Constants
import com.usc.spark.data.LocalData
import com.usc.spark.pojo.Point
import com.usc.spark.specials.{CalUtil, SecondSortKey, StationTImeSecond}
import com.usc.spark.util.{RedisClient, SparkUtils}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Dataset, Row, SparkSession}
import redis.clients.jedis.Jedis

import scala.collection.mutable.ListBuffer
import scala.io.{BufferedSource, Source}
import scala.util.control.Breaks.{break, breakable}

object TestData {
  def main(args: Array[String]): Unit = {
/*

    val spark=SparkSession.builder().master("local").appName("traffic").getOrCreate()
    import spark.implicits._
    val source: BufferedSource = Source.fromFile("./trip_mode.txt","gbk")
    val array: Array[String] = source.getLines().toArray
    val tripMode= spark.sparkContext.parallelize(array).map(arr=>{
      val str=arr.split(",")
      (str(0),str(1),str(2),str(3))
    }).map(arr=>{
      val jedis = RedisClient.pool.getResource
        for(i<- 0 to 23){
          var x=""
          var j=i+1
          var y=""

          if(i<10){
            x="0"+i.toString
          }else{
            x=i.toString
          }
          if(j==24){
            j=0
          }
          if(j<10){
            y="0"+j.toString
          }else{
            y=j.toString
          }
          jedis.set("crowd"+":"+x+"-"+y+":"+arr._4+":"+arr._1+"-"+arr._2,"畅通-"+"8.4")

        }
      RedisClient.pool.returnResource(jedis)
      arr
    })
  tripMode.count()
  */


/*

     //驻留分析
    val spark=SparkSession.builder().master("local").appName("traffic").getOrCreate()
    import spark.implicits._
    val modeRdd: Dataset[String] = spark.read.textFile("./mode.txt")
    val groupRdd=modeRdd.map(arr=>{
      val str=arr.split(",")
      (str(0),(str(1),str(3),str(6),str(8)))
    }).rdd.groupByKey().map(point=>{
      val iterator: Iterator[(String, String, String, String)] = point._2.toIterator
      val list: Seq[(String, String, String, String)] = iterator.toList
      val lists=new ListBuffer[(String,String)]
      for(i<- 0 to list.length-1){

          if((i==0)){
            var schedule =list(i)._2.toDouble
          breakable(
          for (j <- i + 1 to list.length - 1) {
             if(list(j).equals("驻留")){
               schedule=schedule+list(j)._2.toDouble
             }else {

               break()
             }
          })
          if(schedule>600){
            var row=(list(i)._3+"-"+point._1,"")

            if(CalUtil.judgeDaytimeOrNight(list(i)._1)==0){
              row=(list(i)._3+"-"+point._1+"-工作地","工作地")
            }else{
               row=(list(i)._3+"-"+point._1+"-居住地","居住地")
            }

            lists.append(row)
          }
        }
        if((i!=0 && !list(i-1)._4.equals("驻留") && list(i)._4.equals("驻留"))){
          var schedule =list(i)._2.toDouble
          breakable(
            for (j <- i + 1 to list.length - 1) {
              if(list(j).equals("驻留")){
                schedule=schedule+list(j)._2.toDouble
              }else {

                break()
              }
            })
          if(schedule>600){
            var row=(list(i)._3+"-"+point._1,"")

            if(CalUtil.judgeDaytimeOrNight(list(i)._1)==0){
              row=(list(i)._3+"-工作地","工作地")
            }
            else{
               row=(list(i)._3+"-居住地","居住地")
            }
            lists.append(row)
          }
        }
      }
      lists
    })
    val listRdd=groupRdd.flatMap(list=>{
      list
    })

    val keyRdd=listRdd.groupByKey().keys.map(arr=>{
      val str=arr.split("-")

      (str(0)+"-"+str(1),str(2))
    })
   keyRdd.foreachPartition(arr=>{
     val jedis = RedisClient.pool.getResource
     jedis.select(1)
     while(arr.hasNext){
       val tuple: (String, String) = arr.next()
       //jedis.del("lingering:"+tuple._1+":"+tuple._2)
       jedis.set("lingering:"+tuple._1+":"+tuple._2,tuple._2)

     }
     RedisClient.pool.returnResource(jedis)
   })

*/


    //交通拥挤分析
    val spark=SparkSession.builder().master("local").appName("traffic").getOrCreate()
    import spark.implicits._
    val modeRdd: Dataset[String] = spark.read.textFile("./mode.txt")
    var primitiveRdd=modeRdd.rdd.map(arr=>{
        val str=arr.split(",")

      (SecondSortKey(str(0).toLong,str(1).toLong),(str(0),str(1),str(2),str(3),str(4),str(5),str(6),str(7),str(8)))
    }).sortByKey().map(arr=>{
       arr._2
    })
    primitiveRdd=primitiveRdd.cache()
    val stationRdd=primitiveRdd.flatMap(arr=>{
      val stations=arr._8.split("\\|")
      val list=new ListBuffer[(String,(String,String,String,String,String,String))]
      for(i <- 0 to ConfigurationManager.getInteger(Constants.SPARK_NEAR_STATION_COUNT)-1){
        val str: Array[String] = stations(i).split(":")
        if(arr._9.equals("公交车") || arr._9.equals("地铁"))
        if(600 > str(1).toDouble){
          val station=str(0)
          val s=(station,(arr._2,arr._1,arr._3,arr._4,arr._5,arr._9))
          list.append(s)
        }
      }
      list
    })

    val source: BufferedSource = Source.fromFile("./trip_mode.txt","gbk")
    val array: Array[String] = source.getLines().toArray

    val tripModeData= spark.sparkContext.parallelize(array).map(arr=>{

      val str=arr.split(",")
      (str(3),str(0)+"-"+str(1))
    })

    val taskRdd: RDD[(String, ((String, String, String, String, String, String), Option[String]))] =stationRdd.leftOuterJoin(tripModeData)
    val timeRdd=taskRdd.map(arr=>{
      (arr._1+":"+CalUtil.judgeTime(arr._2._1._1),(arr._2._2.getOrElse("12345").toString,arr._2._1._1,arr._2._1._2,arr._2._1._3,arr._2._1._4,arr._2._1._5,arr._2._1._6))
    })
    timeRdd.cache()
    //计算个时间段平均速度，并划分对应得拥挤程度等级
    val crowdRdd: RDD[(String, String, String, Double, Int, String, String)] =timeRdd.groupByKey().map(point=>{
      val format: DecimalFormat = new DecimalFormat("0.0000")
      val iterator: Iterator[(String, String, String, String, String, String, String)] = point._2.toIterator
      var sumSpeed=0.toDouble
      var carNum=0
      var itude=""
        while (iterator.hasNext){

          val tuple: (String, String, String, String, String, String, String) = iterator.next()
          if(carNum==0){
            itude=tuple._1
          }
          sumSpeed=sumSpeed+tuple._6.toDouble
          carNum=carNum+1
        }

      val aveSpeed=format.format(sumSpeed/carNum)
      val station=point._1.split(":")(0)
      val time=point._1.split(":")(1)
      (StationTImeSecond(station,time.replace("-","").toLong),(station,time,itude,sumSpeed,carNum,aveSpeed,CalUtil.judgeCrow(aveSpeed)))
    }).sortByKey().map(arr=>{
      arr._2
    })
    crowdRdd.foreachPartition(arr=>{
      val jedis = RedisClient.pool.getResource

      while(arr.hasNext){
        val tuple: (String, String, String, Double, Int, String, String) = arr.next()

        //jedis.del("crowd+"+":"+tuple._2+":"+tuple._1+":"+tuple._3)
      // jedis.set("crowd"+":"+tuple._2+":"+tuple._1+":"+tuple._3,tuple._7+"-"+tuple._6)

      }
      RedisClient.pool.returnResource(jedis)
    })


    //jedis.set("crowd"+":"+x+"-"+y+":"+arr._4+":"+arr._1+"-"+arr._2,"拥挤")

    /*
      //夜行分析
    val spark=SparkSession.builder().master("local").appName("traffic").getOrCreate()
    import spark.implicits._
    val modeRdd: Dataset[String] = spark.read.textFile("./mode.txt")
    var primitiveRdd=modeRdd.rdd.map(arr=>{
      val str=arr.split(",")

      (SecondSortKey(str(0).toLong,str(1).toLong),(str(0),str(1),str(2),str(6),str(8)))
    }).sortByKey().map(arr=>{
      arr._2
    }).map(arr=>{
      (arr._1,(arr._2,arr._3,arr._4,arr._5))
    }).groupByKey()
    val nightRdd=primitiveRdd.map(arr=>{
      var count=0
      var list: List[(String, String, String, String)] = arr._2.iterator.toList

      for(i<-0 to list.length-1){
          if(CalUtil.judgeDeepNight(list(i)._1)==1&& list(i)._2.toDouble!=0){
            count=count+1
          }
      }
      if(count==0){
        list=list.filter(x=>{
          x._1=="5"
        })

      }
      list
    })
    val lastRdd=nightRdd.flatMap(arr=>{
      arr
    })
    lastRdd.foreach(println)
*/


/*
  //出行方式标签化，及夜行分析
  val spark=SparkSession.builder().master("local").appName("traffic").getOrCreate()
  import spark.implicits._
  val modeRdd: Dataset[String] = spark.read.textFile("./mode.txt")
  var primitiveRdd=modeRdd.rdd.map(arr=>{
      val str=arr.split(",")
    (SecondSortKey(str(0).toLong,str(1).toLong),(str(0),str(1),str(2),str(3),str(4),str(5),str(6),str(7),str(8)))
  }).sortByKey().map(arr=>{
    (arr._2._1,(arr._2._2,arr._2._3,arr._2._4,arr._2._5,arr._2._6,arr._2._7,arr._2._8,arr._2._9))
  })
    val lastRdd=primitiveRdd.groupByKey().map(arr=>{
       val list: List[(String, String, String, String, String, String, String, String)] = arr._2.toIterator.toList
      val lists=new ListBuffer[(String,String,String,String,String)]
      var count=0
      var night=""
      //判断是否在半夜出行位置移动
      for(i<-0 to list.length-1){
        if(CalUtil.judgeDeepNight(list(i)._1)==1&& list(i)._2.toDouble!=0){
          count=count+1
        }
      }
     if(count>3){
       night="夜行"
     }

      var j=0
      for(i<-0 to list.length-1 if i>(j-1)){
        val row=(arr._1,list(i)._1,list(i)._6,list(i)._8,list(i)._4)
        lists.append(row)
        if(list(i)._8.equals("驻留")){
          breakable(
            for (k <- i + 1 to list.length - 1) {
              if(list(k)._8.equals("驻留") && k+1<list.length){
              }else {
                if(k==list.length-1){
                  val row=(arr._1,list(k)._1,list(k)._6,list(k)._8,list(k)._4)
                  lists.append(row)
                  j=k+1
                }else{
                  if((k-1)!=i){
                    val row=(arr._1,list(k-1)._1,list(k-1)._6,list(k-1)._8,list(k-1)._4)
                    lists.append(row)
                  }
                  j=k
                }
                break()
              }
            })
        }else{
          j=j+1
        }

      }
      val jedis: Jedis = RedisClient.pool.getResource
      jedis.select(2)
      var value=""
      for(i<-0 to lists.length-1){
        if(i==lists.length-1){
          value=value+lists(i)._2+"-"+lists(i)._3+"-"+lists(i)._4+"-"+lists(i)._5
        }else{
          value=value+lists(i)._2+"-"+lists(i)._3+"-"+lists(i)._4+"-"+lists(i)._5+":"
        }
      }
      jedis.set("trip:"+arr._1+"-"+night,value)
      //jedis.del("trip:"+arr._1+"-"+night)
      RedisClient.pool.returnResource(jedis)
      lists
    }).flatMap(arr=>{
      arr
    })
    println(lastRdd.count)
    //lastRdd.foreach(println)
*/





}
}
