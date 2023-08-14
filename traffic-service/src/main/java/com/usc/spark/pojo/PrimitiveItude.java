package com.usc.spark.pojo;

import java.io.Serializable;

public class PrimitiveItude implements Serializable {
    //imsi,time,arr._1,longitude,latitude
    private String imsi;
    private String time;
    private String lac;
    private String longitude;
    private String latitude;

    public PrimitiveItude() {
        super();
    }

    public PrimitiveItude(String imsi, String time, String lac, String longitude, String latitude) {
        this.imsi = imsi;
        this.time = time;
        this.lac = lac;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getImsi() {
        return imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLac() {
        return lac;
    }

    public void setLac(String lac) {
        this.lac = lac;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }
}
