package com.home.honor.home;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ws.WebSocket;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by honor on 2017/3/15.
 */
public class MusicFragment extends Fragment {
    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    public static Music_Adapter mMyAdapter;
    public static ImageView mHeaderBg;
    private Button btnSelList;
    public float alpha;
    public Button music_next;
    public Button music_prev;
    public Button btn_playing;

    public void setPlayingStatus(){
        try {
            btn_playing.setBackgroundResource(PlayControl.playing_status.equals("playing") ? R.drawable.pause : R.drawable.playing);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.music, container, false);

        btnSelList = (Button)view.findViewById(R.id.music_list);
        btnSelList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),PlaylistActivity.class);
                startActivity(intent);
            }
        });
        music_prev = (Button)view.findViewById(R.id.music_prev);
        music_prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("cmd","prev");
                    RequestBody requestBody = RequestBody.create(WebSocket.TEXT,jsonObject.toString());
                    PlayControl.ws.sendMessage(requestBody);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        music_next = (Button)view.findViewById(R.id.music_next);
        music_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("cmd","next");
                    RequestBody requestBody = RequestBody.create(WebSocket.TEXT,jsonObject.toString());
                    PlayControl.ws.sendMessage(requestBody);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });
        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        mToolbar.setBackgroundColor(Color.TRANSPARENT);
        //mToolbar.inflateMenu(R.menu.toolbar_right_menu);
        mToolbar.setTitleTextColor(Color.WHITE);
        mHeaderBg = (ImageView) view.findViewById(R.id.header_image);
        PlayControl.musicFragment = this;
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mMyAdapter = new Music_Adapter();
        mMyAdapter.frg = this;
        mMyAdapter.context = getContext();

        btn_playing = (Button)view.findViewById(R.id.btn_playing);
        btn_playing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("cmd","pause");
                    RequestBody requestBody = RequestBody.create(WebSocket.TEXT,jsonObject.toString());
                    PlayControl.ws.sendMessage(requestBody);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mMyAdapter);
        int toolbarHeight = mToolbar.getLayoutParams().height;
        //Log.i(TAG,"toolbar height:"+toolbarHeight);
        final int headerBgHeight = toolbarHeight + getStatusBarHeight(getActivity());
        //Log.i(TAG,"headerBgHeight:"+headerBgHeight);
        ViewGroup.LayoutParams params =  mHeaderBg.getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = headerBgHeight;

        mHeaderBg.setImageAlpha(0);


        StatusBarUtils.setTranslucentImageHeader(getActivity(),0,mToolbar);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                View headerView = null;

                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int firstVisibleItem = manager.findFirstVisibleItemPosition();
                if (firstVisibleItem == 0) {
                    headerView = recyclerView.getChildAt(0);
                }
                if (headerView == null) {
                    return;
                }

                alpha = Math.abs(headerView.getTop()) * 1.0f / headerView.getHeight();

                //Log.i(TAG, "alpha:" + alpha + "top :" + headerView.getTop() + " height: " + headerView.getHeight());
                Drawable drawable = mHeaderBg.getDrawable();
                if (drawable != null) {
                    drawable.mutate().setAlpha((int) (alpha * 255));
                    mHeaderBg.setImageDrawable(drawable);
                }
            }
        });
        getPlaylist(true);
        setPlayingStatus();
        return view;
    }
    private static int getStatusBarHeight(Context context) {
        // 获得状态栏高度
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        return context.getResources().getDimensionPixelSize(resourceId);
    }

    public void getPlaylist(boolean Playing){
        Log.d("Playlist","Getting");
        String strPlaying = "?playing="+(Playing?"true":"false");
        StaticClass.httpGet(StaticClass.serverName + "music" + strPlaying).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(),"服务器连接失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Response response) throws IOException {
                final String str = response.body().string();
                Log.d("Music",str);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObject = new JSONObject(str);
                            mMyAdapter.setData(jsonObject);
                            mMyAdapter.notifyDataSetChanged();
                        }catch (Exception e){
                            Log.e("MusicFragment",e.getMessage());
                        }
                    }
                });
            }
        });

    }

    public void getStatus(){

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {}
    }
}
