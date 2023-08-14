package com.fuchuang.trafficbigdata.service;

import com.fuchuang.trafficbigdata.pojo.ItationItude;
import com.fuchuang.trafficbigdata.pojo.Mode;
import com.fuchuang.trafficbigdata.pojo.TripMode;

import java.util.ArrayList;
import java.util.List;

public class DataToPojo {

    public static List<ItationItude> LongitudeLatitudeToPojo(){

        List<String> strs = DataRead.LongitudeLatitudeRead() ;
        List<ItationItude> ItationItudes = new ArrayList<>() ;

        for(String s : strs){
            String[] s_list =  s.split(",") ;
            ItationItude itationItude = new ItationItude() ;
            itationItude.setImsi(s_list[0]);
            itationItude.setTimestamp(s_list[1]);
            itationItude.setLac_id(s_list[2]);
            itationItude.setLongitude(s_list[3]);
            itationItude.setLatitude(s_list[4]);
            ItationItudes.add(itationItude) ;
        }

        return ItationItudes;
    }

    public static List<TripMode> TripModeToPojo(){

        List<String> strs = DataRead.TripModeRead() ;
        List<TripMode> tripModes = new ArrayList<>() ;
        for (String s : strs){
            String[] s_list = s.split(",") ;
            TripMode tripMode = new TripMode() ;
            tripMode.setLongitude(s_list[0]);
            tripMode.setLatitude(s_list[1]);
            tripMode.setMode(s_list[2]);
            tripMode.setMode_name(s_list[3]);
            tripModes.add(tripMode) ;
        }
        return tripModes ;
    }

    public static List<Mode> ModeToPojo(){

        List<String> strs = DataRead.ModeRead() ;
        List<Mode> modes = new ArrayList<>() ;
        for (String s : strs){
            String[] s_list =  s.split(",");
            Mode mode = new Mode() ;
            mode.setImsi(s_list[0]);
            mode.setTimeStamp(s_list[1]);
            mode.setDistance(s_list[2]);
            mode.setTime(s_list[3]);
            mode.setSpeed(s_list[4]);
            mode.setLac_id(s_list[5]);
            mode.setItude(s_list[6]);
            mode.setStations(s_list[7]);
            mode.setMode(s_list[8]);
            modes.add(mode) ;
        }
        return modes ;
    }
}
