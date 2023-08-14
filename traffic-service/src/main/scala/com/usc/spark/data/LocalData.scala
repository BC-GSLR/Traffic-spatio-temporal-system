package com.usc.spark.data

import java.text.SimpleDateFormat
import java.util.Date

import com.usc.spark.pojo.PrimitiveItude
import com.usc.spark.specials.SecondSortKey
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}

import scala.io.{BufferedSource, Source}
class LocalData {
    def getData(spark:SparkSession): Unit ={
        val primitiveData=spark.read.textFile("primitive_data.txt")
        import spark.implicits._
        val firstData=firstClean(spark,primitiveData)
        val itudeStation=spark.read.textFile("itude_station.txt")
        val primiItude= secondClean(spark,firstData,itudeStation)

        val primiType=StructType(List[StructField](
          StructField("imsi", StringType, nullable = true),
          StructField("time", StringType, nullable = true),
          StructField("lac", StringType, nullable = true),
          StructField("longitude", StringType, nullable = true),
          StructField("latitude", StringType, nullable = true)
        ))
      val primiFrame=spark.createDataFrame(primiItude,primiType)
      //创建primitive_itude视图
      primiFrame.createOrReplaceTempView("primitive_itude")

      val source: BufferedSource = Source.fromFile("./trip_mode.txt","gbk")
      val array: Array[String] = source.getLines().toArray
      val tripMode= spark.sparkContext.parallelize(array).map(arr=>{

        val str=arr.split(",")

        Row(str(0),str(1),str(2),str(3))
      })
      println(tripMode.count())
      val tripType=StructType(List[StructField](
        StructField("longitude", StringType, nullable = true),
        StructField("latitude", StringType, nullable = true),
        StructField("mode", StringType, nullable = true),
        StructField("modeName", StringType, nullable = true)
       // StructField("modeNum", StringType, nullable = true)
      ))
      val tripFrame=spark.createDataFrame(tripMode,tripType)

      //创建trip_mode视图
      tripFrame.createOrReplaceTempView("trip_mode")
    }

    /**
      * 第一次数据清洗
      * @param spark
      * @param dirtyData
      */
    def firstClean(spark: SparkSession,dirtyData:Dataset[String]):Dataset[(String, String, String, String)] ={
        import spark.implicits._
      val format: SimpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss")
        var primitiveData=dirtyData.map(one=>{
          val arr=one.split(",")
          if(arr(0).equals("timestamp")){
            (arr(0),arr(1),arr(2),arr(3))
          }else{
          var date=new Date(arr(0).toLong)
          var str=format.format(date)
            (str,arr(1),arr(2),arr(3))
          }
        }).filter(arr=>{
             arr._1.startsWith("20181003")&&
            !arr._1.equals("timestamp")&& !arr._2.contains("*")&&
              !arr._2.contains("^")&& !arr._2.contains("#")&&
              !arr._1.equals("") && !arr._2.equals("")&& !arr._3.equals("")&& !arr._4.equals("")&&
            !arr._1.equals(" ") && !arr._2.equals(" ")&& !arr._3.equals(" ")&& !arr._4.equals(" ")
        })
        println("过滤各种杂值后"+primitiveData.count())


        primitiveData=primitiveData.cache()
        primitiveData.collect()
        return primitiveData;
    }

    /**
      * 第二次数据清洗
      * @param spark
      * @param firstData
      * @param unitData
      */
    def secondClean(spark: SparkSession,firstData: Dataset[(String, String, String, String)],unitData:Dataset[String]):RDD[Row]={
        import spark.implicits._
       val primitiveData=firstData.map(arr=>{
           (arr._3+"-"+arr._4,arr._1+":"+arr._2)
       })
        primitiveData.count()
        val stationData=unitData.map(one=>{
            val strings: Array[String] = one.split(",")
            (strings(2),strings(0)+":"+strings(1))
        })
        stationData.count()

        val primitiveRdd: RDD[(String, String)] = primitiveData.rdd
        val stationRdd: RDD[(String, String)] = stationData.rdd

        var psRdd=primitiveRdd.leftOuterJoin(stationRdd)
        psRdd.cache()
        psRdd.count()
        psRdd=psRdd.filter(arr=>{
            arr._2._2.getOrElse("5")!="5"
        })
        println(psRdd.count())
        var dataRdd =psRdd.map(arr=>{
            val time: String = arr._2._1.split(":")(0)
            val imsi: String = arr._2._1.split(":")(1)
            val longitude: String = arr._2._2.getOrElse("12345").split(":")(0)
            val latitude: String = arr._2._2.getOrElse("12345").split(":")(1)
            (imsi+"-"+time,imsi+","+time+","+arr._1+","+longitude+","+latitude)
        })
          val keys= dataRdd.groupByKey()
          var rdds=keys.map(arr=>{
            val iterator= arr._2.toIterator
            val x=iterator.next()
            x
          }).map(x=>{
            (SecondSortKey(x.split(",")(0).toLong,x.split(",")(1).toLong),x)
          }).sortByKey().map(x=>{
            val arr=x._2.split(",")
            Row(arr(0),arr(1),arr(2),arr(3),arr(4))
          })
      rdds=rdds.cache()
          println(rdds.count())

        return rdds
    }



}
