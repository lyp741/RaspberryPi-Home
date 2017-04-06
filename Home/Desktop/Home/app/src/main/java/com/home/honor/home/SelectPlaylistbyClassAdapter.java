package com.home.honor.home;

import android.content.Context;
import android.support.annotation.StringDef;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
public class SelectPlaylistbyClassAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    public String title = "";
    public ArrayList<String> subs = new ArrayList<>();
    public SelectPlaylistbyClassAdapter(Context context){
        this.context = context;
    }

    public void setData(JSONObject data){
        try{
            subs.clear();
            title = data.getString("title");
            //Log.e("SelPlaylistClassAdapter",data.toString());
            JSONArray ja = data.getJSONArray("sub");
            for(int i=0;i<ja.length();i++){
                subs.add(ja.getString(i));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        notifyDataSetChanged();
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        RecyclerView.ViewHolder myHolder;
        if(viewType==1) {
            view = LayoutInflater.from(context).inflate(R.layout.selplaylist_item_view, parent, false);
            myHolder = new MyHolder(view);
        }else {
            view = LayoutInflater.from(context).inflate(R.layout.playlist_class_item_view, parent, false);
            myHolder = new FirstHolder(view);
        }
        return myHolder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        if(position!=0) {
            try{
                ((MyHolder)holder).sub.setText(subs.get(position - 1));
            }catch (Exception e){
                //e.printStackTrace();
            }
        }else {
            try{
                ((FirstHolder)holder).title.setText(title);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getItemViewType(int position){
        if(position == 0){
            return 0;
        }else{
            return 1;
        }
    }

    @Override
    public int getItemCount() {
        return subs.size() + 1;
    }

    class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView sub;
        public MyHolder(View itemView) {
            super(itemView);
            sub = (TextView) itemView.findViewById(R.id.selplaylist_class_sub);
            itemView.setOnClickListener(this);
        }
        @Override
        public void onClick(View view){
            StaticClass.httpGet(StaticClass.serverName + "selplaylist?playlist_class=" + subs.get(getPosition()-1)).enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {

                }

                @Override
                public void onResponse(Response response) throws IOException {
                    PlayControl.playlistActivity.getPlaylists();
                    ((SelectPlaylistActivity)context).finish();
                }
            });

        }
    }
    class FirstHolder extends RecyclerView.ViewHolder{
        public TextView title;
        public FirstHolder(View itemView){
            super(itemView);
            title = (TextView)itemView.findViewById(R.id.selplaylist_class_title);
        }
    }
}
