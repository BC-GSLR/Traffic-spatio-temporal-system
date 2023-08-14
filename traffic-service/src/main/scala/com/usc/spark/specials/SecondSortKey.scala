package com.usc.spark.specials

case class SecondSortKey(val first:Long,val second:Long) extends  Ordered[SecondSortKey]  {
  def compare(that: SecondSortKey): Int = {
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