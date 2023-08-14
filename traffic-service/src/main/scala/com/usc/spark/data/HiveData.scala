package com.usc.spark.data

import java.text.SimpleDateFormat
import java.util.Date

import org.apache.spark.api.java.JavaRDD
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.sql.{Dataset, Row, SparkSession}

class HiveData {
    def getData(spark:SparkSession): Unit ={
     /* import spark.implicits._
      val format: SimpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss")
       val primitiveData=spark.sql("select p.imsi,p.timestamp,s.laci,s.longitude,s.latitude,from primitive_data p,station_itude s where p.lac_id+'-'+p.cell_id =s.laci")
       var primitiveRdd=primitiveData.map(arr=>{
          (arr.getString(0),arr.getString(0),arr.getString(0),arr.getString(0),arr.getString(0),arr.getString(0))
        }).filter(arr=>{
         !arr._1.contains("*")&& !arr._1.contains("^")&& !arr._1.contains("#")&&
           !arr._1.equals("") && !arr._2.equals("")&& !arr._3.equals("")&& !arr._4.equals("")&& !arr._5.equals("")
       }).map(arr=>{
         var date=new Date(arr._2.toLong)
         var str=format.format(date)
         Row(arr._1,str,arr._3,arr._4,arr._5)
       })
      primitiveRdd.count()
      val primiType=StructType(List[StructField](
        StructField("imsi", StringType, nullable = true),
        StructField("time", StringType, nullable = true),
        StructField("lac", StringType, nullable = true),
        StructField("longitude", StringType, nullable = true),
        StructField("latitude", StringType, nullable = true)
      ))
      val primitiveFrame=spark.createDataFrame(primitiveRdd.toJavaRDD,primiType)
      primitiveFrame.createOrReplaceTempView("primitive_itude")
*/
    }
}
