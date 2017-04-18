package com.home.honor.home;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.ws.WebSocket;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by honor on 2017/3/17.
 */
public class Movie_Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public Context context;
    public static ArrayList<String> filenames = new ArrayList<>();
    public void setData(ArrayList<String>data){
        filenames = data;
        notifyDataSetChanged();
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if(viewType==0){
            return new Movie_HeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_header_item_view,null));
        }else {
            return new Movie_item_ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_item_view,null));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(position != 0){
            int posindata = position - 1;
            Movie_item_ViewHolder myholder = (Movie_item_ViewHolder)holder;
            String filename =filenames.get(posindata);
            myholder.filename.setText(filename);
            myholder.order.setText(""+position);
            String img_url = StaticClass.thumbServer + filename + ".jpg";
            Glide.with(context).load(img_url).into(myholder.thumb);
            Log.d("movie_thumb",img_url);
            try {
                if ( filename.equals(PlayControl.playing_moviename)) {
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
        return position == 0 ? 0 : 1;
    }

    @Override
    public int getItemCount() {
        return filenames.size() + 1;
    }
    public static class Movie_item_ViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener{
        public TextView order;
        public TextView filename;
        public TextView playing;
        public ImageView thumb;
        public Movie_item_ViewHolder(View itemView) {
            super(itemView);
            order = (TextView)itemView.findViewById(R.id.movie_order);
            filename = (TextView)itemView.findViewById(R.id.movie_filename);
            playing = (TextView)itemView.findViewById(R.id.movie_playing);
            thumb = (ImageView)itemView.findViewById(R.id.movie_thumb);
            itemView.setOnClickListener(this);
        }
        @Override
        public void onClick(View view){
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("cmd","play");
                JSONObject data = new JSONObject();
                data.put("media","movie");
                int position = getPosition()-1;
                data.put("filename",filenames.get(position));
                jsonObject.put("data",data);
                RequestBody requestBody = RequestBody.create(WebSocket.TEXT,jsonObject.toString());
                PlayControl.ws.sendMessage(requestBody);
                Log.e("OnClick",jsonObject.toString());
            }catch (Exception e){
                Log.e("Movie_Adapter",e.getMessage());
            }
        }
    }

    public static class Movie_HeaderViewHolder extends RecyclerView.ViewHolder{

        public Movie_HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }
}
