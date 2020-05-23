package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

public class Basic {
    //由于 JSON中的一些字段不太适合直接作为 Java字段来命名，因此使用注解的方式让 JSON字段和 Java字段之间建立映射关系
    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update{
        @SerializedName("loc")
        public String updateTime;
    }
}
