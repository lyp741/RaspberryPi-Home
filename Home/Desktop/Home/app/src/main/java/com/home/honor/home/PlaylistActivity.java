package com.home.honor.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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
public class PlaylistActivity extends Activity{
    private Button navBack;
    private RecyclerView rv_playlist;
    private PlaylistAdapter plAdapter;
    private Button selPlaylist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlist);
        StatusBarUtils.setFullScreen(this);
        PlayControl.playlistActivity = this;
        navBack = (Button)findViewById(R.id.playlist_nav_back);
        navBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        rv_playlist = (RecyclerView)findViewById(R.id.rv_playlist);
        rv_playlist.setLayoutManager(new GridLayoutManager(this,2));
        plAdapter = new PlaylistAdapter(this);
        rv_playlist.setAdapter(plAdapter);
        selPlaylist = (Button)findViewById(R.id.select_playlist);
        selPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PlaylistActivity.this,SelectPlaylistActivity.class);
                startActivity(intent);
            }
        });
        getPlaylists();
    }

    public void getPlaylists(){
        StaticClass.httpGet(StaticClass.serverName + "playlist").enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(PlaylistActivity.this,"连接服务器失败!",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Response response) throws IOException {
                final String str = response.body().string();
                Log.d("Playlist",str);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            JSONArray ja = new JSONArray(str);
                            ArrayList<JSONObject> arrayList = new ArrayList<>();
                            for (int i=0;i<ja.length();i++){
                                arrayList.add(ja.getJSONObject(i));
                            }
                            plAdapter.setData(arrayList);
                        }catch (Exception e){
                            Log.e("Albums",e.getMessage());
                        }
                    }
                });
            }
        });
    }
}
