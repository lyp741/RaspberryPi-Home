package com.home.honor.home;

import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import com.squareup.okhttp.ws.WebSocket;
import com.squareup.okhttp.ws.WebSocketCall;
import com.squareup.okhttp.ws.WebSocketListener;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okio.Buffer;

/**
 * Created by honor on 2017/3/30.
 */
public class PlayControl {
    public static OkHttpClient mOkHttpClient ;
    public static MusicFragment musicFragment;
    public static String playing_media;
    public static int playing_songid;
    public static String playing_moviename;
    public static String playing_status;
    public static PlaylistActivity playlistActivity;
    public static WebSocket ws;
    public static MovieFragment movieFragment;

    public void startWebSocket(){
        Request request = new Request.Builder()
                .url(StaticClass.websocketServer+"playcontrol")
                .build();
        mOkHttpClient.setConnectTimeout(10, TimeUnit.SECONDS); // connect timeout
        mOkHttpClient.setReadTimeout(600, TimeUnit.SECONDS);
        WebSocketCall webSocketCall=WebSocketCall.create(mOkHttpClient,request);
        webSocketCall.enqueue(new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                ws = webSocket;
                Log.e("PlayControl","OnOpen");
            }

            @Override
            public void onFailure(IOException e, Response response) {
                Log.e("PlayControl","OnFailue");
                e.printStackTrace();
                startWebSocket();
            }

            @Override
            public void onMessage(ResponseBody message) throws IOException {
                final String str = message.string();
                Log.d("PlayControl",str);
                try {
                    JSONObject job = new JSONObject(str);
                    String cmd = job.getString("cmd");
                    if(cmd.equals("status")){
                        JSONObject stats = job.getJSONObject("data");
                        playing_status = stats.getString("playing_status");
                        playing_media = stats.getString("playing_media");
                        if(playing_media.equals("music")){
                            playing_moviename = "";
                            playing_songid = stats.getInt("playing_songid");
                        }else if(playing_media.equals("movie")){
                            playing_songid = 0;
                            playing_moviename = stats.getString("playing_moviename");
                        }
                        musicFragment.getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                musicFragment.mMyAdapter.notifyDataSetChanged();
                                musicFragment.setPlayingStatus();
                                movieFragment.setPlayingStatus();
                                movieFragment.movie_adapter.notifyDataSetChanged();
                            }
                        });
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onPong(Buffer payload) {

            }

            @Override
            public void onClose(int code, String reason) {
                startWebSocket();
            }
        });
    }
    public PlayControl(){
        mOkHttpClient = new OkHttpClient();
        startWebSocket();
    }
}
