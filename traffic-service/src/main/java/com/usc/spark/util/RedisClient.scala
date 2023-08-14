package com.usc.spark.util

import com.usc.spark.conf.ConfigurationManager
import com.usc.spark.constant.Constants
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import redis.clients.jedis.JedisPool

object RedisClient extends Serializable {
 /* val redisHost =ConfigurationManager.getInteger(Constants.RedisHost).toString
  val redisPort = ConfigurationManager.getInteger(Constants.RedisPort)
  val redisTimeout = ConfigurationManager.getInteger(Constants.ReidsTimeOut)*/
 val redisHost="121.199.31.199"
  val redisPort =6379
  val redisTimeout =30000
  /**
   * JedisPool是一个连接池，既可以保证线程安全，又可以保证了较高的效率。 
   */
  lazy val pool = new JedisPool(new GenericObjectPoolConfig(), redisHost, redisPort, redisTimeout)

//  lazy val hook = new Thread {
//    override def run = {
//      println("Execute hook thread: " + this)
//      pool.destroy()
//    }
//  }
//  sys.addShutdownHook(hook.run)
}