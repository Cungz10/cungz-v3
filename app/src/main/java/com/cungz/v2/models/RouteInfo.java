package com.cungz.v2.models;

public class RouteInfo {
    private String distance;
    private String duration;

    public RouteInfo(String distance, String duration) {
        this.distance = distance;
        this.duration = duration;
    }

    public String getDistance() {
        return distance;
    }

    public String getDuration() {
        return duration;
    }
}
