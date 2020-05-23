package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

public class Now {
    //由于 JSON中的一些字段不太适合直接作为 Java字段来命名，因此使用注解的方式让 JSON字段和 Java字段之间建立映射关系
    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;

    public class More{
        @SerializedName("txt")
        public String info;
    }
}
