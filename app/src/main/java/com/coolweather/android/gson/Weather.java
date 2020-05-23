package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Weather {
    // 对Basic，AQI，Now，Suggestion和Forecast 类进行引用

    public String status;// 成功返回ok，失败返回具体原因
    public Basic basic;
    public AQI aqi;
    public Now now;
    public Suggestion suggestion;

    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;
}
