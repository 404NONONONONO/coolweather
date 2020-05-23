package com.coolweather.android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        // 从SharedPreferences 文件中读取缓存数据，如果不为空，说明之前已经请求过天气数据，不再需要用户再次选择城市了，直接跳转WeatherActivity即可
        if(prefs.getString("weather",null) != null){
            startActivity(new Intent(this,WeatherActivity.class));
            finish();
        }
    }
}
