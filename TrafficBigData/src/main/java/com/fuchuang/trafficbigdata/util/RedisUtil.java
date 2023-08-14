//package com.fuchuang.trafficbigdata.util;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Component;
//
//@Component
//public class RedisUtil {
//
//    @Autowired
//    private RedisTemplate<String,Object> redisTemplate ;
//
//
//    /**
//     * 是否存在key值
//     * @param key
//     * @return
//     */
//
//    public boolean HasKey(String key){
//
//        if (redisTemplate.hasKey(key)){
//
//            return true ;
//        }else {
//
//            return false ;
//        }
//    }
//
//    /**
//     *
//     * 添加缓存
//     * @param key
//     * @param value
//     * @return
//     */
//    public boolean AddItem(String key ,Object value){
//
//        if (value == null){
//            return false ;
//        }else {
//            redisTemplate.opsForValue().set(key,value);
//            return true ;
//        }
//    }
//
//    /**
//     *
//     * 删除缓存
//     * @param key
//     * @return
//     */
//    public boolean DelItem(String key){
//
//        if (redisTemplate.hasKey(key)){
//
//            redisTemplate.delete(key) ;
//            return true ;
//        }else {
//
//            return  false ;
//        }
//    }
//
//    /**
//     *
//     * 更新缓存
//     * @param key
//     * @param value
//     * @return
//     */
//    public boolean UpdateItem(String key,Object value){
//
//        if (this.DelItem(key) && this.AddItem(key,value) ){
//
//            return true ;
//        }else
//        {
//            return false ;
//        }
//
//    }
//
//    /**
//     *
//     * 获取缓存
//     * @param key
//     * @return
//     */
//    public Object GetItem (String key){
//
//        if (this.HasKey(key)){
//
//            return redisTemplate.opsForValue().get(key) ;
//        }else {
//
//            return null ;
//        }
//
//    }
//}