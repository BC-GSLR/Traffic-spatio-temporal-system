package com.usc.spark.specials

case class SecondSortKey(val first:Long,val second:Long) extends  Ordered[SecondSortKey]  {
  def compare(that: SecondSortKey): Boolean = {
        if(this.first==that.first){
          return this.second>that.second
        }else{
          return this.first>that.first
        }

  }
}