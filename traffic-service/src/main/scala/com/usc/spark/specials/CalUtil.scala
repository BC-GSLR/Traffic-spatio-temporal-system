package com.usc.spark.specials

import java.text.DecimalFormat

import com.usc.spark.conf.ConfigurationManager
import com.usc.spark.constant.Constants

object CalUtil extends Serializable {
  //计算速度
  def calSpeed(time:String,distance:String): String ={
    val format: DecimalFormat = new DecimalFormat("0.0000")
    if(time.toDouble==0){
      return "0.0";
    }
    val d: Double = distance.toDouble/time.toDouble
    val str: String = format.format(d)
    return str
  }
  //是否在6:00到22:00之间
  def  isOperationTime(stamp:String): Int = {
    val double: Double = stamp.substring(8, 14).toDouble
    if (double < 220000 && double > 60000) {
      return 1
    } else {
      return 0
    }
  }
  //是否远离车站
  def  isCloseStation(distance:String): Int ={
    val str: String = distance.split("\\|")(0).split(":")(1)
    if(str.toDouble>3000){
      return 0
    }else{
      return 1
    }
  }

  //最近的几个车站是否发生变化
  def isMove(x1:String,x2:String): Int ={

    val stations1: Array[String] = x1.split("\\|")
    val stations2: Array[String] = x2.split("\\|")
    var count=0
    for(i<-0 to ConfigurationManager.getInteger(Constants.SPARK_NEAR_STATION_COUNT)-1 ){
      if(stations1(i).split(":")(0)==stations2(i).split(":")(0)){
        count=count+1
      }
    }
    if(count!=ConfigurationManager.getInteger(Constants.SPARK_NEAR_STATION_COUNT)){
      return 1
    }else{
      return 0
    }
  }
  //计算时间段
  def judgeTime(time:String):String ={
    val format = new DecimalFormat("00")
    val str: String = time.substring(8,10)
    if(str.equals("23")){
      return "23-00"
    }else{
      return str+"-"+format.format(str.toInt+1).toString
    }
  }
  //判断拥挤程度
  def judgeCrow(aveSpeed:String): String ={
    if(aveSpeed.toDouble>5){
        return "畅通"
    }else if(aveSpeed.toDouble>2){
      return "拥挤"
    }else{
      return "堵塞"
    }
  }
  //判断最近车站中是否含地铁
  def judgeIsSubway(stations:String): Int ={
     val strings: Array[String] = stations.split("\\|")
     for(i <- 0 to ConfigurationManager.getInteger(Constants.SPARK_NEAR_STATION_COUNT)-1){
         val station: Array[String] = strings(i).split(":")
        if(station(0).contains("地铁") && 1500>station(1).toDouble){
          return 1
        }
    }
    return 0
  }
  //判断白天区与半夜区
  def judgeDaytimeOrNight(stamp:String): Int ={
    val str= stamp.substring(8,14).toInt
    if(str<210000 && str>60000){
    return 0
    }else{
      return 1
    }
  }
  //判断深夜
  def judgeDeepNight(stamp:String): Int ={
    val str= stamp.substring(8,14).toInt
    if(str<40000 && str>10000){
      return 1
    }else{
      return 0
    }
  }

}
