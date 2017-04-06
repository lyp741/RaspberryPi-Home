package com.home.honor.home;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
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
public class SelPlaylistAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    public SelPlaylistAdapter(Context context){this.context = context;}
    public ArrayList<JSONObject> arrData = new ArrayList<>();

    public void setData(ArrayList<JSONObject> data){
        arrData = data;
        notifyDataSetChanged();
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        RecyclerView recyclerView;
        SelectPlaylistbyClassAdapter adapter;
        RecyclerView.ViewHolder myHolder;
        if(viewType==0) {
            view = LayoutInflater.from(context).inflate(R.layout.selplaylist_allorpersonal_item_view, parent, false);
            myHolder = new FirstHolder(view);
        }else {
            view = LayoutInflater.from(context).inflate(R.layout.selplaylistbyclass_item_view, parent, false);
            recyclerView = (RecyclerView)view.findViewById(R.id.rv_selectplaylistbyclass);
            adapter = new SelectPlaylistbyClassAdapter(context);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL));
            SimpleDividerDecoration decoration = new SimpleDividerDecoration(context);
            recyclerView.addItemDecoration(decoration);
            myHolder = new OtherHolder(view);
            ((OtherHolder)myHolder).adapter = adapter;
        }
        return myHolder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (position != 0){
            ((OtherHolder)holder).adapter.setData(arrData.get(position - 1));
        }
    }

    @Override
    public int getItemViewType(int position){
        return position;
    }

    @Override
    public int getItemCount() {
        return arrData.size() + 1;
    }
    class FirstHolder extends RecyclerView.ViewHolder{
        public RelativeLayout all_playlist;
        public RelativeLayout my_playlist;
        public String selected = "全部";
        public FirstHolder(View itemView) {
            super(itemView);
            all_playlist = (RelativeLayout)itemView.findViewById(R.id.selplaylist_all);
            all_playlist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selected = "全部";
                    get();
                }
            });
            my_playlist = (RelativeLayout)itemView.findViewById(R.id.selplaylist_mine);
            my_playlist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selected = "我的";
                    get();
                }
            });
        }

        public void get(){
            StaticClass.httpGet(StaticClass.serverName + "selplaylist?playlist_class=" + selected).enqueue(new Callback() {
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
    class OtherHolder extends RecyclerView.ViewHolder {
        public SelectPlaylistbyClassAdapter adapter;
        public OtherHolder(View itemView) {
            super(itemView);
        }
    }

    public class SimpleDividerDecoration extends RecyclerView.ItemDecoration {

        private int dividerHeight;
        private Paint dividerPaint;

        public SimpleDividerDecoration(Context context) {
            dividerPaint = new Paint();
            dividerPaint.setColor(context.getResources().getColor(R.color.colorAccent));
            dividerHeight = 1;
        }


        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.bottom = dividerHeight;
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            int childCount = parent.getChildCount();
            //int left = parent.getPaddingLeft();
            int right = parent.getWidth() - parent.getPaddingRight();

            for (int i = 0; i < childCount; i++) {
                View view = parent.getChildAt(i);
                float top = view.getBottom();
                float bottom = view.getBottom() + dividerHeight;
                int left = view.getLeft();
                c.drawRect(left, top, view.getRight(), bottom, dividerPaint);//bottom
                c.drawRect(view.getLeft(),view.getTop(),view.getLeft()+dividerHeight,view.getBottom(),dividerPaint);//left
                c.drawRect(view.getLeft(),view.getTop(),view.getRight(),view.getTop()+dividerHeight,dividerPaint);//top
                c.drawRect(view.getRight(),view.getTop(),view.getRight()+dividerHeight,view.getBottom(),dividerPaint);//right
                //Log.e("11","left:"+left+"  top:"+top+"  right"+right+"  bottom"+bottom);
            }
        }
    }
}
