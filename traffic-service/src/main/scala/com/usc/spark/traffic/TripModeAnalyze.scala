package com.usc.spark.traffic

import java.lang
import java.text.{DecimalFormat, SimpleDateFormat}

import com.usc.spark.Dao.Impl.TaskDaoImpl
import com.usc.spark.conf.ConfigurationManager
import org.apache.spark.sql.{Dataset, Row, SparkSession}
import com.usc.spark.constant.Constants
import com.usc.spark.data.{HiveData, LocalData}
import com.usc.spark.pojo.Point
import com.usc.spark.specials.{CalUtil, SecondSortKey, StationTImeSecond}
import com.usc.spark.util.{ParamUtils, RedisClient, SparkUtils}
import net.minidev.json.JSONObject
import net.minidev.json.parser.JSONParser
import org.apache.spark.rdd.RDD
import redis.clients.jedis.Jedis

import scala.collection.mutable.ListBuffer
import scala.util.control.Breaks.{break, breakable}
import scala.collection.mutable.Map
import scala.io.{BufferedSource, Source}
object TripModeAnalyze {

  def main(args: Array[String]): Unit = {
      val conf=SparkSession.builder().appName(Constants.SPARK_LOCAL)
      SparkUtils.setMaster(conf)
      conf.enableHiveSupport()
      val spark: SparkSession = conf.getOrCreate()
      import spark.implicits._

      //获取数据
     if(ConfigurationManager.getBoolean(Constants.SPARK_LOCAL)){
       //获取本地数据
          val localData=new LocalData
          localData.getData(spark)
     }else{
       //获取hive数据
          spark.sql("use traffic")
           val hiveData=new HiveData
            hiveData.getData(spark)
     }

      val taskId: lang.Long = ParamUtils.getTaskIdFromArgs(args,Constants.SPARK_LOCAL_TASKID_TripModeAnalyze)
      if(taskId==0L){
          println("arg is null")
        return
      }

    val taskDao=new TaskDaoImpl()
    val task=taskDao.findTaskById(String.valueOf(taskId))

    if(task==null){
      return;
    }
    println(task.getTaskId)

    val jsonParser=new JSONParser()
    val taskParamsJsonObject : JSONObject = jsonParser.parse(task.getTaskParams).asInstanceOf[JSONObject]
    var primitiveDataSet: Dataset[Row] = SparkUtils.getPrimitiveItudeByDateRange(spark,taskParamsJsonObject)
    println(primitiveDataSet.count())

    primitiveDataSet=primitiveDataSet.cache()
  //将数据按照imsi,time,进行二次排序
    var primitiveRdd: RDD[(String, (String, String, String))] =primitiveDataSet.rdd.map(arr=>{
      val imsi=arr.getString(0)
      val time=arr.getString(1)
      (SecondSortKey(imsi.toLong,time.toLong),arr)
    }).sortByKey().map(arr=>{
      val imsi=arr._2.getString(0)
      val time=arr._2.getString(1)
      val station=arr._2.getString(2)
      val longitude=arr._2.getString(3)
      val latitude=arr._2.getString(4)
      (imsi,(time,station,longitude+"-"+latitude))
    })
  primitiveRdd=primitiveRdd.cache()
  //计算每个人在每个时间点的时间差与距离差，以及速度
  var timeAndDistanceRdd: RDD[(String, String, String, String, String, String, String)] = calTimeDistance(primitiveRdd)
  timeAndDistanceRdd=timeAndDistanceRdd.cache()


  //计算距离各个车站得距离
  var nearStationRdd: RDD[((String, String, String, String, String, String, String), String)] = getNearStation(spark,timeAndDistanceRdd)
  nearStationRdd=nearStationRdd.cache()

  val nearStationSortRdd: RDD[((String, String, String, String, String, String, String), String)] =nearStationRdd.map(arr=>{
   (SecondSortKey(arr._1._1.toLong,arr._1._2.toLong),arr)
 }).sortByKey().map(_._2)

  var tripModeRdd:  RDD[Point]  = judgetripMode(spark,nearStationRdd)
    tripModeRdd=tripModeRdd.cache()
    tripModeRdd.saveAsTextFile("/dsads")

    /*
    //计算各站交通拥挤程度 并且进行redis存储
    val crowdRdd: RDD[(String, String, String, Double, Int, String, String)] = getStationCrowd(spark,tripModeRdd)

    //计算驻留地，工作地，居住地，并进行redis存储
    val lingeringRdd: RDD[(String, String)] = getlingringAnalyze(spark,tripModeRdd)

    //个人一天出行方式标签化，及夜行判断，并且存储在redis当中
    val tripRdd: RDD[(String, String, String, String, String)] = getTripLabel(spark,tripModeRdd)
*/






}
//计算每个人在每个时间点的时间差与距离差，以及速度
def calTimeDistance(primitiveItude: RDD[(String, (String, String, String))]):RDD[(String,String,String,String,String,String,String)] ={

  val groupRdd: RDD[(String, Iterable[(String, String, String)])] = primitiveItude.groupByKey()
  val cacheRdd: RDD[(String, Iterable[(String, String, String)])] = groupRdd.cache()
  val format: SimpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss")
  var listRdd= cacheRdd.map(one=>{
    val imsi=one._1
    val iterable=one._2
    val iterator: Iterator[(String, String, String)] = iterable.toIterator
    var uptp=("","","")
    val list=new ListBuffer[(String,String,String,String,String,String,String)]

    while(iterator.hasNext){
      val tuple: (String, String, String) = iterator.next()
      breakable{
        if(uptp._1.equals("")){
          uptp=tuple
          val l=(imsi,tuple._1,"0.0","0.0","0.0",tuple._2,tuple._3)
          list.append(l)
          break()
        }

        val time=((format.parse(tuple._1).getTime-format.parse(uptp._1).getTime)/1000).toInt
        val distance=SparkUtils.getDistance(tuple._3.split("-")(0).toDouble,tuple._3.split("-")(1).toDouble,uptp._3.split("-")(0).toDouble,uptp._3.split("-")(1).toDouble)
        val speed=CalUtil.calSpeed(time.toString,distance.toString)
        val l=(imsi,tuple._1,distance.toString,time.toString,speed.toString,tuple._2,tuple._3)
        list.append(l)
        uptp=tuple
      }
    }
    list
  })
  listRdd=listRdd.cache()
  listRdd.count()

  val storeRdd=listRdd.flatMap(list=>{
    list
  })
  return  storeRdd

}
//距离较近得几个车站
def getNearStation(spark: SparkSession,timeAndDistanceRdd: RDD[(String, String, String, String, String, String, String)]):RDD[((String, String, String, String, String, String, String), String)]  ={
  import spark.implicits._
  val tripDataSet: Dataset[Row] = SparkUtils.geTripMode(spark)

  var tripDataRdd=tripDataSet.map(arr=>{
    arr.toString()
  })
  tripDataRdd=tripDataRdd.cache()
  tripDataRdd.count()
  val list: List[String] =tripDataRdd.collect().toList

  //设置广播变量
  val broadList=spark.sparkContext.broadcast(list)

  var distanceRdd=timeAndDistanceRdd.map(one=>{

    val tripList=new ListBuffer[(String,Double)]()

    //计算距离所有车站距离
    broadList.value.foreach(arr=>{
      val trip=arr.replace("]","").replace("[","").split(",")
      val itude: Array[String] = one._7.split("-")
      println(trip(0)+":"+trip(1)+":"+itude(0)+":"+itude(1))
      val d: Double = SparkUtils.getDistance(itude(0).toDouble,itude(1).toDouble,trip(0).toDouble,trip(1).toDouble)
      tripList.append((trip(3),d))

    })
    //选出最近得几个
    val sortList=tripList.sortWith((x1,x2)=>{
      x2._2>x1._2
    })
    (one,sortList)
  })
  distanceRdd=distanceRdd.cache()

  val lastRdd: RDD[((String, String, String, String, String, String, String), String)] =distanceRdd.map(arr=> {
    val value: ListBuffer[(String, Double)] = arr._2
    var tripDistance=""

    for (i <- 0 to ConfigurationManager.getInteger(Constants.SPARK_NEAR_STATION_COUNT)-1) {
      if(i==0){
        tripDistance=tripDistance+value(i)._1+":"+value(i)._2
      }else{
        tripDistance=tripDistance+"|"+value(i)._1+":"+value(i)._2
      }
      println("这是算完一个")
    }

    (arr._1,tripDistance)
  })
  lastRdd
}
//判断出行方式
def judgetripMode(spark:SparkSession,nearStationSortRdd:RDD[((String, String, String, String, String, String, String), String)]):  RDD[Point]  ={

  val gRdd=nearStationSortRdd.map(x=>{
    val point= new Point()
    point.setImsi(x._1._1)
    point.setStamp(x._1._2)
    point.setDistance(x._1._3)
    point.setTime(x._1._4)
    point.setSpeed(x._1._5)
    point.setLac(x._1._6)
    point.setItude(x._1._7)
    point.setStation(x._2)
    (x._1._1,point)
  }).groupByKey()
  val listRdd: RDD[List[Point]] = gRdd.map(x=>{
    val imsi=x._1
    val iterator: Iterator[Point] = x._2.toIterator
    val list: List[Point] = iterator.toList
    //      for(i<- 0 to list.length-1){
    //        val point: Point = list(i)
    //        println(point.getImsi+":"+point.getStamp+":"+util.calSpeed(point.getTime,point.getDistane))
    //      }
    for(i<- 0 to list.length-1){

      val point: Point = list(i)
      if(point.getMode()==null) {
        if ( i != 0 && CalUtil.isOperationTime(point.getStamp) == 1 && CalUtil.isCloseStation(point.getStation)==1) {
          val last = list(i - 1)
          //判断与上一个点的站是否相同,而距离产生变化

          if (CalUtil.isMove(point.getStation, last.getStation) == 0 && point.getDistance.toDouble != 0) {
            val speed=point.getSpeed
            if(!last.getMode.equals("驻留")){
              point.setMode(last.getMode)
              point.setSpeed(speed)
            }
            else{
              point.setSpeed(speed)
              if(speed.toDouble>11){
                point.setMode("地铁")
              }
              else if(speed.toDouble>1){
                point.setMode("公交车")
              }else{
                point.setMode("步行")
              }

            }
          }
          //判断与上一个点的站是否相同,而距离未发生变化
          else if (CalUtil.isMove(point.getStation, last.getStation) == 0 && point.getDistance.toDouble == 0) {
            var schedule = point.getTime.toDouble
            //判断后续点是否也是驻留
            breakable(
              for (j <- i + 1 to list.length - 1) {
                if (CalUtil.isMove(point.getStation, list(j).getStation) == 0 && list(j).getDistance.toDouble == 0) {
                  schedule = schedule + list(j).getTime.toDouble
                } else {
                  break()
                }
              })
            if (schedule > 600) {
              point.setSpeed("0.0")
              point.setMode("驻留")
              breakable(
                for (j <- i + 1 to list.length - 1) {
                  if (CalUtil.isMove(point.getStation, list(j).getStation) == 0 && list(j).getDistance.toDouble == 0) {
                    list(j).setSpeed("0.0")
                    list(j).setMode("驻留")
                  } else {
                    break()
                  }
                })
            } else {
              if(last.getMode=="步行"){
                point.setMode("驻留")

              }
              else if(last.getMode=="地铁"){
                point.setMode("驻留")
              }
              else {
                point.setMode(last.getMode)
              }
            }
            //如果位置发生变化
          }else{
            val speed=CalUtil.calSpeed(point.getTime, point.getDistance)
            point.setSpeed(speed)
            if(speed.toDouble>11){
              point.setMode("地铁")
            }else{
              point.setMode("公交车")
            }

          }

        } else {
          if(i==0){
            point.setMode("驻留")
          }else {

            val last = list(i - 1)
            //判断与上一个点的站是否相同,而距离产生变化
            if (CalUtil.isMove(point.getStation, last.getStation) == 0 && point.getDistance.toDouble != 0) {
              val speed=CalUtil.calSpeed(point.getTime, point.getDistance)
              if(!last.getMode.equals("驻留")){
                point.setMode(last.getMode)
                point.setSpeed(speed)
              }else{
                point.setSpeed(speed)
                if(speed.toDouble>1){
                  point.setMode("轿车")
                }else{
                  point.setMode("步行")
                }
              }

            }
            //判断与上一个点的站是否相同,而距离未发生变化
            else if (CalUtil.isMove(point.getStation, last.getStation) == 0 && point.getDistance.toDouble == 0) {
              var schedule = point.getTime.toDouble
              //判断后续点是否也是驻留
              breakable(
                for (j <- i + 1 to list.length - 1) {
                  if (CalUtil.isMove(point.getStation, list(j).getStation) == 0 && list(j).getDistance.toDouble == 0) {
                    schedule = schedule + list(j).getTime.toDouble
                  } else {

                    break()
                  }
                })
              if (schedule > 600) {
                point.setMode("驻留")
                breakable(
                  for (j <- i + 1 to list.length - 1) {
                    if (CalUtil.isMove(point.getStation, list(j).getStation) == 0 && list(j).getDistance.toDouble == 0) {
                      list(j).setMode("驻留")
                    } else {
                      break()
                    }
                  })
              } else {
                if (last.getMode == "步行") {
                  point.setMode("驻留")
                }
                else if (last.getMode == "地铁") {
                  point.setMode("驻留")
                }
                else {
                  point.setMode(last.getMode)
                }
              }
              //如果位置发生变化
            } else {
              val speed = CalUtil.calSpeed(point.getTime, point.getDistance)
              point.setSpeed(speed)
              if(speed.toDouble>1){
                point.setMode("轿车")
              }else{
                point.setMode("步行")
              }
            }
          }
        }
      }

    }
    var isCar=0
    for(i<- 0 to list.length-1){
      val point: Point = list(i)
      if(point.getMode.equals("轿车")){
          isCar=1
      }
      if(isCar==1 && point.getMode.equals("公交车")){
        point.setMode("轿车")
      }
      if(point.getMode.equals("地铁")){
          val isSubway= CalUtil.judgeIsSubway(point.getStation)
        if(isSubway==0){
          if(isCar==0)
          point.setMode("公交车")
        }else{
          point.setMode("轿车")
        }
      }
    }
    val format: DecimalFormat = new DecimalFormat("0.0000")
    for(i<- 0 to list.length-1) {
      val point: Point = list(i)
      if((point.getMode.equals("轿车") ||point.getMode.equals("公交车"))&& point.getSpeed.toDouble>11){
          val speed=point.getSpeed.toDouble
          val x=11-11/speed
          val str= format.format(x)
          point.setSpeed(str)
      }
      if(point.getMode.equals("地铁")  && point.getSpeed.toDouble>22){
        val speed=point.getSpeed.toDouble
        val x=22-22/speed
        val str= format.format(x)
        point.setSpeed(str)
      }
    }
    list

  })
  val pointRdd: RDD[Point] =listRdd.flatMap(list=>{
    list
  })
  println(pointRdd.count())
    return pointRdd

  }

  //计算各站得拥挤程度
  def getStationCrowd(spark:SparkSession,tripModeRdd:  RDD[Point]): RDD[(String, String, String, Double, Int, String, String)] ={
    var primitiveRdd=tripModeRdd.map(point=>{
      (point.getImsi,point.getStamp,point.getDistance,point.getTime,point.getSpeed,point.getLac,point.getItude,point.getStation,point.getMode)
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

    /*val source: BufferedSource = Source.fromFile("./trip_mode.txt","gbk")
    val array: Array[String] = source.getLines().toArray

    val tripModeData= spark.sparkContext.parallelize(array).map(arr=>{

      val str=arr.split(",")
      (str(3),str(0)+"-"+str(1))
    })*/
    import spark.implicits._
    val tripDataSet= SparkUtils.geTripMode(spark).rdd
    var tripModeData=tripDataSet.map(arr=>{
      val str= arr.toString().replace("]","").replace("[","").split(",")
      (str(3).toString,str(0)+"-"+str(1))
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
        jedis.set("crowd"+":"+tuple._2+":"+tuple._1+":"+tuple._3,tuple._7+"-"+tuple._6)

      }
      RedisClient.pool.returnResource(jedis)
    })
    crowdRdd






  }
  //驻留分析
  def getlingringAnalyze(spark:SparkSession,tripModeRdd:  RDD[Point]): RDD[(String, String)]  ={
      val groupRdd=tripModeRdd.map(arr=>{
        (arr.getImsi,(arr.getStamp,arr.getTime,arr.getItude,arr.getMode))
      }).groupByKey().map(point=>{
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
        jedis.set("lingering:"+tuple._1+":"+tuple._2,tuple._2)
        //jedis.del("lingering:"+tuple._1+":"+tuple._2)
      }
      RedisClient.pool.returnResource(jedis)
    })
     keyRdd
  }

  //个人一天出行方式标签化
  def getTripLabel(spark:SparkSession,tripModeRdd:  RDD[Point]): RDD[(String, String, String, String, String)] ={
    var primitiveRdd=tripModeRdd.map(point=>{
      (point.getImsi,(point.getStamp,point.getDistance,point.getTime,point.getSpeed,point.getLac,point.getItude,point.getStation,point.getMode))
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
        }}
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
      // jedis.del("trip:"+arr._1)
      RedisClient.pool.returnResource(jedis)
      lists

    }).flatMap(arr=>{
      arr
    })
    lastRdd.count
   lastRdd
  }
}
