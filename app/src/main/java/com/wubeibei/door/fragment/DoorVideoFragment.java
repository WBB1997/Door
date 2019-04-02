package com.wubeibei.door.fragment;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

import com.wubeibei.door.R;

public class DoorVideoFragment extends Fragment {
    private static final String TAG = "DoorVideoFragment";
    private VideoView DoorVideo;

    public DoorVideoFragment() {
    }

    public static DoorVideoFragment newInstance() {
        return new DoorVideoFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_door_video, container, false);;
        DoorVideo = view.findViewById(R.id.DoorVideo);
        DoorVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.d(TAG, "onCompletion: " + "循环播放");
                mp.start();
            }
        });
        DoorVideo.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                Log.d(TAG, "onInfo: " + what + "  " + extra);
                return false;
            }
        });
        DoorVideo.setMediaController(null);
        return view;
    }



    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.d(TAG, "onHiddenChanged: " + hidden);
        if(!hidden){
            Log.d(TAG, "开始播放");
            DoorVideo.start();
        }else {
            Log.d(TAG, "暂停播放");
            DoorVideo.pause();
        }
    }

    public void setVideoURI(Uri uri) {
        DoorVideo.setVideoURI(uri);
    }

    public void start(){
        DoorVideo.start();
    }
}
