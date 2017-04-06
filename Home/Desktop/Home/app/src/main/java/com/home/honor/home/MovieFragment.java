package com.home.honor.home;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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
 * Created by honor on 2017/3/17.
 */
public class MovieFragment extends Fragment {
    private RecyclerView movie_rv;
    public static Movie_Adapter movie_adapter;
    private Button btn_playing;
    private Button movie_prev;
    private Button movie_next;
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
        final View view = inflater.inflate(R.layout.movie, container, false);

        movie_rv = (RecyclerView)view.findViewById(R.id.rv_movie);
        movie_adapter = new Movie_Adapter();
        movie_rv.setAdapter(movie_adapter);
        movie_adapter.context = getContext();
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
        movie_prev = (Button)view.findViewById(R.id.movie_prev);
        movie_next = (Button)view.findViewById(R.id.movie_next);
        movie_prev.setOnClickListener(new View.OnClickListener() {
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
        movie_next.setOnClickListener(new View.OnClickListener() {
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
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        movie_rv.setLayoutManager(manager);
        PlayControl.movieFragment = this;
        setPlayingStatus();
        return view;
    }
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            StaticClass.httpGet(StaticClass.serverName + "movie").enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {

                }

                @Override
                public void onResponse(Response response) throws IOException {
                    final String str = response.body().string();
                    Log.d("Movies",str);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                JSONArray ja = new JSONArray(str);
                                ArrayList<String> filenames = new ArrayList<String>();
                                for (int i=0;i<ja.length();i++) {
                                    filenames.add(ja.getString(i));
                                }
                                movie_adapter.setData(filenames);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    });


                }
            });
        }
    }
}
