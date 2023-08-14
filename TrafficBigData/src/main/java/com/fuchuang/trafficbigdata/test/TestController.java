package com.fuchuang.trafficbigdata.test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class TestController {

    @GetMapping("/line")
    public String LineController(){
        String str = "{\n" +
                "    \"line_char_data\": [\n" +
                "        {\n" +
                "            \"pass_count\": 116,\n" +
                "            \"left_pass_count\": 1,\n" +
                "            \"right_pass_count\": 1,\n" +
                "            \"straight_pass_count\": 114,\n" +
                "            \"time_now\": \"2020-05-23 17:21:21\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"pass_count\": 116,\n" +
                "            \"left_pass_count\": 1,\n" +
                "            \"right_pass_count\": 1,\n" +
                "            \"straight_pass_count\": 114,\n" +
                "            \"time_now\": \"2020-05-23 17:21:25\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"pass_count\": 116,\n" +
                "            \"left_pass_count\": 1,\n" +
                "            \"right_pass_count\": 1,\n" +
                "            \"straight_pass_count\": 114,\n" +
                "            \"time_now\": \"2020-05-23 17:21:29\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"pass_count\": 116,\n" +
                "            \"left_pass_count\": 1,\n" +
                "            \"right_pass_count\": 1,\n" +
                "            \"straight_pass_count\": 114,\n" +
                "            \"time_now\": \"2020-05-23 17:21:34\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"pass_count\": 116,\n" +
                "            \"left_pass_count\": 1,\n" +
                "            \"right_pass_count\": 1,\n" +
                "            \"straight_pass_count\": 114,\n" +
                "            \"time_now\": \"2020-05-23 17:21:37\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"pass_count\": 116,\n" +
                "            \"left_pass_count\": 1,\n" +
                "            \"right_pass_count\": 1,\n" +
                "            \"straight_pass_count\": 114,\n" +
                "            \"time_now\": \"2020-05-23 17:21:40\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"pass_count\": 116,\n" +
                "            \"left_pass_count\": 1,\n" +
                "            \"right_pass_count\": 1,\n" +
                "            \"straight_pass_count\": 114,\n" +
                "            \"time_now\": \"2020-05-23 17:21:42\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"pass_count\": 116,\n" +
                "            \"left_pass_count\": 1,\n" +
                "            \"right_pass_count\": 1,\n" +
                "            \"straight_pass_count\": 114,\n" +
                "            \"time_now\": \"2020-05-23 17:21:44\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"pass_count\": 116,\n" +
                "            \"left_pass_count\": 1,\n" +
                "            \"right_pass_count\": 1,\n" +
                "            \"straight_pass_count\": 114,\n" +
                "            \"time_now\": \"2020-05-23 17:21:46\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        return str;
    }

    @GetMapping("mengban")
    public Object mengban(){
        Map<String, Object> res = new HashMap<>() ;
        List<List<String>> list = new ArrayList<>() ;
        List demo01 = new ArrayList(2) ;
        demo01.add("30.25") ;
        demo01.add("71.44") ;
        List demo02 = new ArrayList(2) ;
        demo02.add("80.52");
        demo02.add("101.93") ;
        List demo03 = new ArrayList(2) ;
        demo03.add("180.77") ;
        demo03.add("100.42") ;
        List demo04 = new ArrayList(2) ;
        demo04.add("260.47") ;
        demo04.add("97.28") ;
        List demo05 = new ArrayList(2) ;
        demo05.add("60.43") ;
        demo05.add("72.58") ;
        list.add(demo01) ;
        list.add(demo02) ;
        list.add(demo03) ;
        list.add(demo04) ;
        list.add(demo05) ;


        List<String> Forbid = new ArrayList<>() ;
        Forbid.add("小轿车");
        Forbid.add("卡车");
        res.put("points",list) ;
        res.put("forbid",Forbid) ;

        List<Object> weiguitingche = new ArrayList<>() ;
        weiguitingche.add(list) ;

        Map<String,Object> result = new HashMap<>() ;
        result.put("违规车道",res);

        result.put("非法停车",list) ;

        Map<String,Object> Fin = new HashMap<>() ;
        Fin.put("1",result) ;
        Fin.put("pic","文件名");


        return Fin ;
    }

}
