package com.wubeibei.door.fragment;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;

import com.wubeibei.door.R;

import java.util.ArrayList;


public class AllVideoFragment extends Fragment {
    private static final String TAG = "AllVideoFragment";
    private VideoView videoView;
    private int AllVideo_index = 0;
    private ArrayList<Uri> list;

    public AllVideoFragment() {
    }

    public static AllVideoFragment newInstance(ArrayList<Uri> stringList) {
        AllVideoFragment fragment = new AllVideoFragment();
        Bundle args = new Bundle();
        args.putSerializable("list", stringList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_video, container, false);
        videoView = view.findViewById(R.id.AllVideo);
        if (getArguments() != null) {
            list = (ArrayList<Uri>) getArguments().getSerializable("list");
            videoView.setVideoURI(list.get(AllVideo_index));
            this.setOnCompletionListener();
        }
        return view;
    }

    public void setOnCompletionListener(){
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                AllVideo_index++;
                videoView.setVideoURI(list.get(AllVideo_index % list.size()));
                videoView.start();
            }
        });
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            videoView.start();
            Log.d(TAG, "onHiddenChanged: " + hidden);
        } else {
            videoView.pause();
        }
    }

    public void cancel(){
        AllVideo_index = 0;
        videoView.setVideoURI(list.get(AllVideo_index));
    }

    public void pause() {
        videoView.pause();
    }

    public void start(){
        videoView.start();
    }
}
