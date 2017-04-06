package com.home.honor.home;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
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
public class Music_Adapter  extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public Context context;
    public Fragment frg;
    static Bitmap bitmap;
    static JSONObject jsonData;
    static String img_url;
    static ImageView headerImageView;
    static ArrayList<song_info> songs = new ArrayList<>();

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == 1){
            return  new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.music_item_layout,null));
        }else{
            return new HeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.header_layout,null));
        }
    }
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(position==0){
            if(bitmap!=null)
                ((HeaderViewHolder)holder).headerImage.setImageBitmap(bitmap);
        }else {
            int real_post = position -1;
            song_info si = songs.get(real_post);
            MyViewHolder myholder = (MyViewHolder)holder;
            myholder.order.setText(""+si.order);
            myholder.song_name.setText(si.song_name);
            myholder.artist_album.setText(si.artist+"-"+si.album_name);
            try {
                    if (si.song_id == PlayControl.playing_songid) {
                        if (PlayControl.playing_status.equals("playing")) {
                            myholder.playing.setBackgroundResource(R.drawable.play_nocircle);
                        } else if (PlayControl.playing_status.equals("paused")) {
                            myholder.playing.setBackgroundResource(R.drawable.pause_nocircle);
                        }
                        myholder.playing.setVisibility(View.VISIBLE);
                    } else {
                        myholder.playing.setVisibility(View.INVISIBLE);
                    }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    @Override
    public int getItemViewType(int position) {
        return position==0?0:1;
    }

    @Override
    public int getItemCount() {
        return songs.size()+1;
    }

    public void setData(JSONObject data){
        jsonData = data;
        try{
            img_url = jsonData.getString("img_url");
            JSONArray jsArray = jsonData.getJSONArray("musics");
            songs.clear();
            for(int i=0;i<jsArray.length();i++){
                JSONObject tmp_jsobject = jsArray.getJSONObject(i);
                int song_id = tmp_jsobject.getInt("song_id");
                String song_name = tmp_jsobject.getString("song_name");
                String album_name = tmp_jsobject.getString("album_name");
                int album_id = tmp_jsobject.getInt("album_id");
                String mp3_url = tmp_jsobject.getString("mp3_url");
                String quality = tmp_jsobject.getString("quality");
                int order = tmp_jsobject.getInt("order");
                String artist = tmp_jsobject.getString("artist");
                songs.add(new song_info(song_id, song_name, album_name, album_id, mp3_url, quality, order,artist));
            }
            //

            SimpleTarget target = new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap bitmap, GlideAnimation glideAnimation) {
// do something with the bitmap
// for demonstration purposes, let's just set it to an ImageView
                    MusicFragment.mHeaderBg.setImageBitmap( bitmap );
                    headerImageView.setImageBitmap(bitmap);
                    Drawable drawable = MusicFragment.mHeaderBg.getDrawable();
                    if (drawable != null) {
                        drawable.mutate().setAlpha((int) (((MusicFragment)frg).alpha * 255));
                        MusicFragment.mHeaderBg.setImageDrawable(drawable);
                    }
                }
            };
            Glide.with(context).load(img_url).asBitmap().into(target);
            //Glide.with(context).load(img_url).into(headerImageView);
            notifyDataSetChanged();
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView order;
        public TextView song_name;
        public TextView artist_album;
        public TextView playing;

        public MyViewHolder(View itemView) {
            super(itemView);
            order = (TextView) itemView.findViewById(R.id.music_order);
            song_name = (TextView)itemView.findViewById(R.id.music_songname);
            artist_album = (TextView)itemView.findViewById(R.id.music_artist_album);
            playing = (TextView)itemView.findViewById(R.id.music_playing);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("cmd","play");
                JSONObject data = new JSONObject();
                data.put("media","music");
                int position = getPosition()-1;
                data.put("songid",songs.get(position).song_id);
                jsonObject.put("data",data);
                RequestBody requestBody = RequestBody.create(WebSocket.TEXT,jsonObject.toString());
                PlayControl.ws.sendMessage(requestBody);
                Log.e("OnClick",jsonObject.toString());
            }catch (Exception e){
                Log.e("Music_Adapter",e.getMessage());
            }
        }
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder{
        public ImageView headerImage;
        public HeaderViewHolder(View itemView) {
            super(itemView);
            headerImage = (ImageView) itemView.findViewById(R.id.header_image2);
            headerImageView = headerImage;
        }
    }
    public static class song_info{
        public int song_id;
        public String song_name;
        public String album_name;
        public int album_id;
        public String mp3_url;
        public String quality;
        public int order;
        public String artist;
        song_info(int sid, String sname, String alname, int aid, String url, String qlt, int od, String art){
            this.song_id = sid;
            this.song_name = sname;
            this.album_name = alname;
            this.album_id = aid;
            this.mp3_url = url;
            this.quality = qlt;
            this.order = od;
            this.artist = art;
        }
    }
}
