package com.fuchuang.trafficbigdata;

import com.fuchuang.trafficbigdata.pojo.ItationItude;
import com.fuchuang.trafficbigdata.pojo.Mode;
import com.fuchuang.trafficbigdata.pojo.TripMode;
import com.fuchuang.trafficbigdata.service.DataRead;
import com.fuchuang.trafficbigdata.service.DataToPojo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest(classes = TrafficbigdataApplication.class)
public class ReadTest {

    @Autowired
    private RedisTemplate<String,String> redisTemplate ;

    /**
     * 个人位置数据写入
     */
    @Test
    public void PersonalDataToRedis(){
        List<ItationItude> itationItudes = DataToPojo.LongitudeLatitudeToPojo() ;
        String Timsi = "" ;


        for (ItationItude i : itationItudes){
            // 个人所有位置数据
            //redisTemplate.opsForHash().put("position:"+i.getImsi(),i.getTimestamp(),i.getLongitude()+"-"+i.getLatitude());
            // 时间戳数据
            //redisTemplate.opsForHash().put("timestamp:"+i.getTimestamp(),i.getImsi(),i.getLongitude()+"-"+i.getLatitude());

            // 时间轴数据
            //redisTemplate.opsForSet().add("timeaxis:data",i.getTimestamp());

            // 存取初始化数据
            if (!Timsi.equals(i.getImsi())){
                redisTemplate.opsForHash().put("initial:data",i.getImsi(),i.getLongitude()+"-"+i.getLatitude()) ;
                Timsi = i.getImsi() ;
            }

        }
    }

    @Test
    public void ModeTest(){
        //DataToPojo.ModeToPojo() ;
//        List<TripMode> tripModes = DataToPojo.TripModeToPojo() ;
//        for (TripMode t : tripModes){
//            redisTemplate.opsForHash().put("station:"+t.getMode_name(),"position",t.getLongitude()+"-"+t.getLatitude());
//        }

//        List<Mode> modes = DataToPojo.ModeToPojo() ;
//
//        for (Mode m : modes){
//            String str = m.getStations() ;
//            String[] stations = str.split("\\|") ;
//            for (String s : stations){
//                String[] stationDistance = s.split(":") ;
//                redisTemplate.opsForHash().put("person:"+m.getImsi()+"-"+m.getTimeStamp(),stationDistance[0],stationDistance[1]);
//            }
//        }
    }




}
