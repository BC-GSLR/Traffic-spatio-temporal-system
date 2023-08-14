package com.fuchuang.trafficbigdata.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.fuchuang.trafficbigdata.pojo.ItationItude;
import com.fuchuang.trafficbigdata.service.DataRead;
import com.fuchuang.trafficbigdata.service.DataToPojo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class BaseDataController {

    @Autowired
    private RedisTemplate<String,String> redisTemplate ;

    @GetMapping("/itationtude")
    public Object ItationItudeController(@RequestParam(value = "timestamp",required = true) String timdestamp){

        Map<Object, Object> initialData = redisTemplate.opsForHash().entries("initial:data") ;
        Set<String> axis = redisTemplate.opsForSet().members("timeaxis:data");
        TreeSet<String> axisdata = new TreeSet<>() ;
        axisdata.addAll(axis) ;
        for (String t : axisdata){
            Long time = Long.valueOf(t) % 1000000 ;
            long ts = Long.valueOf(timdestamp) ;
            if (ts>=time){
                timdestamp = t ;
            }
        }
        Map<Object,Object> updateData = redisTemplate.opsForHash().entries("timestamp:"+timdestamp);
        for (Object s : updateData.keySet()){
            initialData.put(s,updateData.get(s)) ;
        }

        Map<String,List<Double>> item = new HashMap<>() ;
        for (Object s : initialData.keySet()){
            String key = String.valueOf(s) ;
            List<Double> ll = new ArrayList<>(2) ;
            String[] lls = String.valueOf(initialData.get(s)).split("-") ;
            ll.add(Double.valueOf(lls[0])) ;
            ll.add(Double.valueOf(lls[1])) ;
            item.put(key,ll) ;
        }

        return item ;

    }

    @GetMapping("axis")
    public Object TimeAxis(){
        return redisTemplate.opsForSet().members("timeaxis:data") ;
    }

    @GetMapping("/mode")
    public Object TripRecommendController(@RequestParam(value = "imsi",required = true) String imsi,
                                          @RequestParam(value = "timestamp",required = true) String timestamp){

        String key = "person:" + imsi+"-"+timestamp ;
        Map<Object, Object> stations = new HashMap<>() ;
        stations = redisTemplate.opsForHash().entries(key) ;

        return stations;
    }
}
