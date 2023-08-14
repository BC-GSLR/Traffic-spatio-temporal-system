package com.fuchuang.trafficbigdata.service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class DataRead {


    private static  String LongitudeLatitudeFilePath = "src/main/resources/原始数据_经纬度数据primitive_itude.txt";
    private static  String TripModeFilePath ="src/main/resources/出行方式trip_mode.txt";
    private static  String ModeFilePath = "src/main/resources/mode.txt";

    /**
     * 经纬度数据读入
     * @return
     */
    public static List<String> LongitudeLatitudeRead(){

        Path path = Paths.get(LongitudeLatitudeFilePath);
        try {
            File file = new File(LongitudeLatitudeFilePath) ;
            if (!file.exists()){
                System.out.println("文件不存在");
                return null ;
            }
            List<String> lines = Files.readAllLines(path,StandardCharsets.UTF_8);
            return lines;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null ;
    }

    /**
     * 出行方式数据读入
     * @return
     */
    public static List<String> TripModeRead(){
        Path path = Paths.get(TripModeFilePath);
        try {
            File file = new File(TripModeFilePath) ;
            if (!file.exists()){
                System.out.println("文件不存在");
                return null ;
            }
            List<String> lines = Files.readAllLines(path,StandardCharsets.UTF_8);
            return lines;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null ;
    }

    /**
     * 各点出行方式数据读入
     * @return
     */
    public static List<String> ModeRead(){
        Path path = Paths.get(ModeFilePath);
        try {
            File file = new File(ModeFilePath) ;
            if (!file.exists()){
                System.out.println("文件不存在");
                return null ;
            }
            List<String> lines = Files.readAllLines(path,StandardCharsets.UTF_8);
            return lines;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null ;
    }
}
