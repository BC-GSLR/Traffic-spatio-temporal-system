package com.usc.spark.pojo;

import java.io.Serializable;

public class Point implements Serializable {
    private String imsi;
    private String stamp;
    private String distance;
    private String time;
    private String speed;
    private String lac;
    private String itude;
    private String station;
    private String mode;

    public String getImsi() {
        return imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    public String getStamp() {
        return stamp;
    }

    public void setStamp(String stamp) {
        this.stamp = stamp;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getLac() {
        return lac;
    }

    public void setLac(String lac) {
        this.lac = lac;
    }

    public String getItude() {
        return itude;
    }

    public void setItude(String itude) {
        this.itude = itude;
    }

    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    @Override
    public String toString() {
        return imsi + ',' +
                 stamp + ',' +
                distance + ',' +
                time + ',' +
                speed + ',' +
                lac + ',' +
                itude + ',' +
                station + ',' +
                mode + ',' +
                '}';
    }
}
