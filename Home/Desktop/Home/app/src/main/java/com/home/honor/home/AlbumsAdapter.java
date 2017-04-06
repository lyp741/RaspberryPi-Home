package com.home.honor.home;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

/**
 * Created by honor on 2017/3/15.
 */
public class AlbumsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private static boolean haveSelete = false;
    private static ArrayList<String> listData = new ArrayList<>();

    public AlbumsAdapter(Context context) {
        this.context = context;
    }

    public void setData(ArrayList<String>src){
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
        MyHolder myholder = (MyHolder)holder;
        myholder.filename.setText(listData.get(position));
        Glide.with(context).load(StaticClass.imageServer+listData.get(position)+".jpg").into(myholder.picture);
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {
        private ImageView picture;
        private TextView filename;

        public MyHolder(View itemView) {
            super(itemView);
            picture = (ImageView)itemView.findViewById(R.id.albums_item_picture);
            filename = (TextView)itemView.findViewById(R.id.albums_item_filename);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener mOnItemLongClickListener) {
        this.mOnItemLongClickListener = mOnItemLongClickListener;
    }
}
