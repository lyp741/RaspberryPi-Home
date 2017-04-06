package com.home.honor.home;

import android.app.DownloadManager;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by honor on 2017/2/17.
 */

public class WeatherFragment extends Fragment {
    weather_adapter wa;
    RecyclerView rv;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.weather, container, false);
        rv = (RecyclerView)view.findViewById(R.id.weather_rv);
        wa = new weather_adapter(getContext());
        rv.setAdapter(wa);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            //相当于Fragment的onResume
            StaticClass.httpGet(StaticClass.serverName+"weather").enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    //Log.e("Weather",e.getMessage());
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(),"连接服务器失败!",Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(final Response response) throws IOException {
                    final String str = response.body().string();
                    Log.d("Weather",str);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject jsonObject = new JSONObject(str);
                                wa.setData(jsonObject);
                                wa.notifyDataSetChanged();
                            }catch (Exception e){
                                Log.e("WeatherFragment",e.getMessage());
                            }
                        }
                    });


                }
            });
        } else {
            //相当于Fragment的onPause
        }
    }
}