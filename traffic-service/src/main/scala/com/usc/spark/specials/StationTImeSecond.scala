package com.usc.spark.specials

case class StationTImeSecond (val first:String,val second:Long) extends  Ordered[StationTImeSecond]  {
  def compare(that: StationTImeSecond): Int = {
    if(this.first<that.first)
      return -1
    else if(this.first>that.first)
      return 1
    else{
      if (this.second==that.second) {
        return 0
      }
      else if(this.second<that.second)
        return -1
      else
        return 1
    }
  }
}