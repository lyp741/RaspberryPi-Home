package com.home.honor.home;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;


import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import com.squareup.okhttp.ws.WebSocket;
import com.squareup.okhttp.ws.WebSocketCall;
import com.squareup.okhttp.ws.WebSocketListener;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okio.Buffer;


/**
 * Created by honor on 2017/2/19.
 */
public class CameraFragment extends Fragment {
    private Button openAlbums;
    private Button capture;
    private ImageView camera_iv;
    static OkHttpClient mOkHttpClient;
    static WebSocket ws;
    static boolean closed = true;
    private void startWS(){
        if (closed == false){
            return;
        }
        Request request = new Request.Builder()
                .url(StaticClass.websocketServer+"camera")
                .build();
        WebSocketCall webSocketCall=WebSocketCall.create(mOkHttpClient,request);
        closed = false;
        webSocketCall.enqueue(new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.e("CameraFrag","OnOpen");
                CameraFragment.ws = webSocket;
                closed = false;
            }

            @Override
            public void onFailure(IOException e, Response response) {
                try {
                    ws.close(1000,"fail");
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                closed = true;
            }

            @Override
            public void onMessage(ResponseBody message) throws IOException {
                Log.d("CameraFrag","onMessage");
                byte[] bytes = message.bytes();
                final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        camera_iv.setImageBitmap(bitmap);
                    }
                });
            }

            @Override
            public void onPong(Buffer payload) {

            }

            @Override
            public void onClose(int code, String reason) {
                closed = true;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.camera, container, false);
        camera_iv = (ImageView)view.findViewById(R.id.camera_iv);
        openAlbums = (Button)view.findViewById(R.id.camera_albums);
        openAlbums.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),AlbumsActivity.class);
                startActivity(intent);
            }
        });
        mOkHttpClient = new OkHttpClient();
        capture = (Button)view.findViewById(R.id.camera_capture);
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    RequestBody requestBody = RequestBody.create(WebSocket.TEXT,"capture");
                    ws.sendMessage(requestBody);
                }catch (Exception e){
                    Log.e("CameraFrag capture",e.getMessage());
                }
            }
        });

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            Log.d("setUVH","true");
            try {
                startWS();
            }catch (Exception e){
                Log.e("CameraFrag start",e.getMessage());
            }
        }else {
            Log.d("setUVH","false");
            try {
                ws.close(1000,"");
            }catch (Exception e){
                Log.e("CameraFrag capture",e.getMessage());
            }
        }
    }

    @Override
    public void onPause(){
        Log.d("Camera Pause","onPause");
        super.onPause();
        setUserVisibleHint(false);
    }

    @Override
    public void onResume(){
        super.onResume();
        int curitem = MainActivity.viewpager.getCurrentItem();
        if(curitem == 1){
            Log.e("onResume","onResume");
            setUserVisibleHint(true);
        }
    }
}
