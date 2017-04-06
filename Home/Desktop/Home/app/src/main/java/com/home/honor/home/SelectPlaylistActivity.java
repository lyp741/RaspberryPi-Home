package com.home.honor.home;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by honor on 2017/3/17.
 */
public class SelectPlaylistActivity extends Activity {
    private RecyclerView rv_selectPlaylist;
    private SelPlaylistAdapter selplAdapter;
    private Button nav_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selectplaylist);
        StatusBarUtils.setFullScreen(this);

        rv_selectPlaylist = (RecyclerView) findViewById(R.id.rv_selectplaylist);
        selplAdapter = new SelPlaylistAdapter(this);
        rv_selectPlaylist.setAdapter(selplAdapter);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        rv_selectPlaylist.setLayoutManager(manager);
        StaticClass.httpGet(StaticClass.serverName + "selplaylist").enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {
                final String str = response.body().string();
                Log.d("SelPlaylist",str);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            JSONArray ja = new JSONArray(str);
                            ArrayList<JSONObject> arr = new ArrayList<JSONObject>();
                            for (int i=0;i<ja.length();i++){
                                arr.add(ja.getJSONObject(i));
                            }
                            selplAdapter.setData(arr);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        nav_back = (Button) findViewById(R.id.selectplaylist_nav_back);
        nav_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }


}