package com.home.honor.home;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by honor on 2017/3/15.
 */
public class AlbumsActivity extends Activity {
    RecyclerView myRecyclerView;
    Toolbar mToolbar;
    AlbumsAdapter albumsAdapter;
    Button nav_back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.albums);
        StatusBarUtils.setFullScreen(this);
        mToolbar = (Toolbar) findViewById(R.id.albums_toolbar);
        //mToolbar.setBackgroundColor(Color.TRANSPARENT);
        //mToolbar.inflateMenu(R.menu.toolbar_right_menu);
        mToolbar.setTitleTextColor(Color.WHITE);
        myRecyclerView = (RecyclerView) findViewById(R.id.rv_albums);
        albumsAdapter = new AlbumsAdapter(this);
        myRecyclerView.setAdapter(albumsAdapter);
        myRecyclerView.setLayoutManager(new GridLayoutManager(this,2));
        //SpacesItemDecoration decoration = new SpacesItemDecoration(30);
        //myRecyclerView.addItemDecoration(decoration);

        nav_back = (Button)findViewById(R.id.albums_nav_back);
        nav_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        StaticClass.httpGet(StaticClass.serverName + "albums").enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(AlbumsActivity.this,"连接服务器失败!",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Response response) throws IOException {
                final String str = response.body().string();
                Log.d("Albums",str);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            JSONArray ja = new JSONArray(str);
                            ArrayList<String> arrayList = new ArrayList<String>();
                            for (int i=0;i<ja.length();i++){
                                arrayList.add(ja.getString(i));
                            }
                            albumsAdapter.setData(arrayList);
                        }catch (Exception e){
                            Log.e("Albums",e.getMessage());
                        }
                    }
                });
            }
        });
    }

    public class SpacesItemDecoration extends RecyclerView.ItemDecoration {//设置每个Item的间距
        private int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.left = space;
            outRect.right = space;
            outRect.bottom = space;
            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.top = space;
            }
        }
    }
}
