package com.home.honor.home;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by honor on 2017/3/17.
 */
public class PlaylistAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private static ArrayList<JSONObject> listData = new ArrayList<>();

    public PlaylistAdapter(Context context) {
        this.context = context;
    }

    public void setData(ArrayList<JSONObject> src){
        listData = src;
        notifyDataSetChanged();
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.albums_item_view, parent, false);
        MyHolder myHolder = new MyHolder(view);
        return myHolder;

    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        MyHolder myHolder = (MyHolder)holder;
        try{
            myHolder.playlistName.setText(listData.get(position).getString("playlists_name"));
            Glide.with(context).load(listData.get(position).getString("coverImgUrl")).into(myHolder.coverImg);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView coverImg;
        private TextView playlistName;

        public MyHolder(View itemView) {
            super(itemView);
            coverImg = (ImageView)itemView.findViewById(R.id.albums_item_picture);
            playlistName = (TextView)itemView.findViewById(R.id.albums_item_filename);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getPosition();
            try{
                int playlist_id = listData.get(position).getInt("playlist_id");
                StaticClass.httpGet(StaticClass.serverName + "playlist?playlist_id=" + playlist_id).enqueue(new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {

                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        PlayControl.musicFragment.getPlaylist(false);
                        ((PlaylistActivity)context).finish();
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}