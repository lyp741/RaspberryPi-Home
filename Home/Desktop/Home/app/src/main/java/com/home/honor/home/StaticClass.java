package com.home.honor.home;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

/**
 * Created by honor on 2017/3/27.
 */
public class StaticClass {
    static String serverIP = "192.168.199.177";
    static String serverName = "http://"+serverIP+":8000/";
    static String websocketServer = "ws://" + serverIP + ":8000/";
    static String imageServer = "http://" + serverIP + ":8089/";
    static String thumbServer = "http://" + serverIP + ":8090/";
    public static Call httpGet(String url){
        //创建okHttpClient对象
        OkHttpClient mOkHttpClient = new OkHttpClient();
//创建一个Request
        final Request request = new Request.Builder()
                .url(url)
                .build();
//new call
        Call call = mOkHttpClient.newCall(request);
        return call;
    }
}
