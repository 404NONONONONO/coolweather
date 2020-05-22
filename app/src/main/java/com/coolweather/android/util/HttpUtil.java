package com.coolweather.android.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpUtil {

    /**
     * @param address 请求地址
     * @param callback 回调（处理服务器响应）
     */
    public static void sendOkHttpRequest(String address,okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();//创建一个OkHttp实例
        Request request = new Request.Builder().url(address).build();//想要发送一条Http请求，需要创建一个Request对象（此时为空，没有实际作用）
        client.newCall(request).enqueue(callback);
    }

}
