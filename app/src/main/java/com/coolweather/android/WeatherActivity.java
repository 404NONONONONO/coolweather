package com.coolweather.android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coolweather.android.gson.Forecast;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    private ScrollView sv_weather_layout;
    private TextView tv_title_city, tv_title_update_time;// title.xml
    private TextView tv_degree_text, tv_weather_info_text;// now.xml
    private LinearLayout ll_forecast_layout;// forecast.xml
    private TextView tv_aqi_text, tv_pm25_text;// aqi.xml
    private TextView tv_comfort_text, tv_car_wash_text, tv_sport_text;// suggestion.xml
    private ImageView iv_bing_pic_img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 由于此功能是 5.0以上才支持，在此对版本号进行判断
        if(Build.VERSION.SDK_INT >= 21){
            // 获取当前当前活动的 DecorView
            View decorView = getWindow().getDecorView();
            // 调用setSystemUiVisibility() 方法，来改变系统 UI显示，表示活动的布局会显示在状态栏上面
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            // 最后调用setStatusBarColor() 方法，将状态栏设置为透明色
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);

        initUI();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        if (weatherString != null) {
            // 有缓存时直接解析天气数据(第一次肯定没有缓存）
            Weather weather = Utility.handleWeatherResponse(weatherString);
            showWeatherInfo(weather);
        } else {
            // 无缓存时去服务器查询天气
            String weatherId = getIntent().getStringExtra("weather_id");// 从Intent 中读取天气 id
            sv_weather_layout.setVisibility(View.INVISIBLE);// 请求数据时，隐藏ScrollView
            requestWeather(weatherId);// 从服务器请求天气数据
        }

        //在 SharedPreferences 中读取缓存的背景图片
        String bingPic = prefs.getString("bing_pic", null);
        if (bingPic != null) {
            //如果有缓存，直接使用Glide 加载图片
            Glide.with(this).load(bingPic).into(iv_bing_pic_img);
        } else {
            //没有缓存，调用loadBingPic() 方法，请求必应背景图。
            loadBingPic();
        }

    }

    /**
     * 加载必应每日一图
     */
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        // 获取到必应背景图链接
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                // 将链接缓存到 SharedPreferences 中
                editor.putString("bing_pic", bingPic);
                editor.apply();
                // 把当前线程切换到主线程
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 使用 Glide加载此图片
                        Glide.with(WeatherActivity.this).load(bingPic).into(iv_bing_pic_img);
                    }
                });
            }
        });
    }

    /**
     * 根据天气id 请求城市天气信息
     *
     * @param weatherId 天气 id
     */
    private void requestWeather(final String weatherId) {
        // 1.拼接接口地址
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=bc0418b57b2d4918819d3974ac1285d9";
        //Log.i(TAG, "weatherId: " + weatherUrl);
        // 2.向以上地址发出请求（服务器会将相应城市的天气信息以JSON 格式返回）
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseText = response.body().string();
                // 3.将服务器返回的json数据转换成 Weather对象
                final Weather weather = Utility.handleWeatherResponse(responseText);
                // 4.将当前线程切换到主线程
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 5.服务器返回的status 状态是 ok，说明请求成功
                        if (weather != null && "ok".equals(weather.status)) {
                            // 6.返回的数据，缓存到 SharedPreferences中
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            // 7.进行内容展示
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        loadBingPic();
    }

    /**
     * 处理并展示 Weather 实体类中的数据
     *
     * @param weather
     */
    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        tv_title_city.setText(cityName);
        tv_title_update_time.setText(updateTime);
        tv_degree_text.setText(degree);
        tv_weather_info_text.setText(weatherInfo);
        ll_forecast_layout.removeAllViews();

        for (Forecast forecast : weather.forecastList) {
            // 动态加载 forecast_item.xml布局，设置相应数据，并添加到父布局中
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, ll_forecast_layout, false);
            TextView tv_date_text = view.findViewById(R.id.tv_date_text);
            TextView tv_info_text = view.findViewById(R.id.tv_info_text);
            TextView tv_max_text = view.findViewById(R.id.tv_max_text);
            TextView tv_min_text = view.findViewById(R.id.tv_min_text);

            tv_date_text.setText(forecast.date);
            tv_info_text.setText(forecast.more.info);
            tv_max_text.setText(forecast.temperature.max);
            tv_min_text.setText(forecast.temperature.min);
            ll_forecast_layout.addView(view);
        }
        if (weather.aqi != null) {
            tv_aqi_text.setText(weather.aqi.city.aqi);
            tv_pm25_text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
        String sport = "运动建议：" + weather.suggestion.sport.info;
        tv_comfort_text.setText(comfort);
        tv_car_wash_text.setText(carWash);
        tv_sport_text.setText(sport);
        // 请求成功，添加至缓存后，将ScrollView重新展示
        sv_weather_layout.setVisibility(View.VISIBLE);

    }


    /**
     * 初始化控件
     */
    private void initUI() {
        sv_weather_layout = findViewById(R.id.sv_weather_layout);
        tv_title_city = findViewById(R.id.tv_title_city);
        tv_title_update_time = findViewById(R.id.tv_title_update_time);
        tv_degree_text = findViewById(R.id.tv_degree_text);
        tv_weather_info_text = findViewById(R.id.tv_weather_info_text);
        ll_forecast_layout = findViewById(R.id.ll_forecast_layout);
        tv_aqi_text = findViewById(R.id.tv_aqi_text);
        tv_pm25_text = findViewById(R.id.tv_pm25_text);
        tv_comfort_text = findViewById(R.id.tv_comfort_text);
        tv_car_wash_text = findViewById(R.id.tv_car_wash_text);
        tv_sport_text = findViewById(R.id.tv_sport_text);
        iv_bing_pic_img = findViewById(R.id.iv_bing_pic_img);
    }
}
