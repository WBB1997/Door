package com.wubeibei.door;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.VideoView;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wubeibei.door.command.LeftDoorCommand;
import com.wubeibei.door.command.RightDoorCommand;
import com.wubeibei.door.fragment.AllVideoFragment;
import com.wubeibei.door.util.LogUtil;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private AllVideoFragment AllFragment;
    private FragmentManager fragmentManager;
    private VideoView DoorView;
    private List<Uri> LeftDoorlist;
    private List<Uri> RightDoorlist;
    private List<Uri> RightDoorPlaylist;
    private List<Uri> LeftDoorPlaylist;
    private final static int closing = 0;
    private final static int opening = 1;
    private final static int welcome = 6;
    private final static int tingkao = 5;
    private final static int arrow = 4;
    private final static int start = 3;
    private final static int end = 2;
    private final static int dooropen = 1;
    private final static int doorclose = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        hideBottomUIMenu();
        DoorView = findViewById(R.id.DoorVideo);
        DoorView.setVideoURI(RightDoorlist.get(6));
        DoorView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.setLooping(true);
                mp.start();
            }
        });
        AllFragment = AllVideoFragment.newInstance((ArrayList<Uri>) LeftDoorPlaylist);
        // 初始化fragment管理器
        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().add(R.id.fragment_container, AllFragment).commit();
        new Thread(new Runnable() {
            @Override
            public void run() {
                UDP_receive();
            }
        }).start();
        DoorView.start();
    }

    private void init(){
        LeftDoorlist = new ArrayList<>(Arrays.asList(
                Uri.parse("android.resource://" + getPackageName() + "/raw/" + R.raw.left_doorclose),
                Uri.parse("android.resource://" + getPackageName() + "/raw/" + R.raw.left_dooropen),
                Uri.parse("android.resource://" + getPackageName() + "/raw/" + R.raw.left_end),
                Uri.parse("android.resource://" + getPackageName() + "/raw/" + R.raw.left_start),
                Uri.parse("android.resource://" + getPackageName() + "/raw/" + R.raw.left_arrow),
                Uri.parse("android.resource://" + getPackageName() + "/raw/" + R.raw.left_tingkao),
                Uri.parse("android.resource://" + getPackageName() + "/raw/" + R.raw.left_welcome)
        ));
        RightDoorlist = new ArrayList<>(Arrays.asList(
                Uri.parse("android.resource://" + getPackageName() + "/raw/" + R.raw.right_doorclose),
                Uri.parse("android.resource://" + getPackageName() + "/raw/" + R.raw.right_dooropen),
                Uri.parse("android.resource://" + getPackageName() + "/raw/" + R.raw.right_end),
                Uri.parse("android.resource://" + getPackageName() + "/raw/" + R.raw.right_start),
                Uri.parse("android.resource://" + getPackageName() + "/raw/" + R.raw.right_arrow),
                Uri.parse("android.resource://" + getPackageName() + "/raw/" + R.raw.right_tingkao),
                Uri.parse("android.resource://" + getPackageName() + "/raw/" + R.raw.right_welcome)
        ));
        RightDoorPlaylist = new ArrayList<>(Arrays.asList(
                RightDoorlist.get(welcome),
                RightDoorlist.get(arrow),
                RightDoorlist.get(start),
                RightDoorlist.get(dooropen),
                RightDoorlist.get(arrow),
                RightDoorlist.get(doorclose),
                RightDoorlist.get(arrow),
                RightDoorlist.get(tingkao),
                RightDoorlist.get(arrow),
                RightDoorlist.get(tingkao),
                RightDoorlist.get(arrow),
                RightDoorlist.get(tingkao),
                RightDoorlist.get(dooropen),
                RightDoorlist.get(arrow),
                RightDoorlist.get(doorclose),
                RightDoorlist.get(end),
                RightDoorlist.get(arrow),
                RightDoorlist.get(end),
                RightDoorlist.get(arrow),
                RightDoorlist.get(end),
                RightDoorlist.get(arrow),
                RightDoorlist.get(dooropen),
                RightDoorlist.get(arrow),
                RightDoorlist.get(arrow),
                RightDoorlist.get(doorclose)
        ));
        LeftDoorPlaylist = new ArrayList<>(Arrays.asList(
                LeftDoorlist.get(welcome),
                LeftDoorlist.get(arrow),
                LeftDoorlist.get(start),
                LeftDoorlist.get(dooropen),
                LeftDoorlist.get(arrow),
                LeftDoorlist.get(doorclose),
                LeftDoorlist.get(arrow),
                LeftDoorlist.get(tingkao),
                LeftDoorlist.get(arrow),
                LeftDoorlist.get(tingkao),
                LeftDoorlist.get(arrow),
                LeftDoorlist.get(tingkao),
                LeftDoorlist.get(dooropen),
                LeftDoorlist.get(arrow),
                LeftDoorlist.get(doorclose),
                LeftDoorlist.get(end),
                LeftDoorlist.get(arrow),
                LeftDoorlist.get(end),
                LeftDoorlist.get(arrow),
                LeftDoorlist.get(end),
                LeftDoorlist.get(arrow),
                LeftDoorlist.get(dooropen),
                LeftDoorlist.get(arrow),
                LeftDoorlist.get(arrow),
                LeftDoorlist.get(doorclose)
        ));
    }

    // 接收CAN总线
    private void UDP_receive() {
        try {
            DatagramSocket datagramSocket = new DatagramSocket(null);
            datagramSocket.setReuseAddress(true);
            datagramSocket.bind(new InetSocketAddress(5556));

            DatagramPacket datagramPacket;
            while (true) {
                byte[] receMsgs = new byte[1024];
                datagramPacket = new DatagramPacket(receMsgs, receMsgs.length);
                // 读取到命令
                try {
                    datagramSocket.receive(datagramPacket);
                    JSONObject jsonObject = JSONObject.parseObject(new String(receMsgs));
                    LogUtil.d(TAG, jsonObject.toJSONString());
                    int id = jsonObject.getIntValue("id");
                    int data;
                    switch (id) {
                        case LeftDoorCommand.Driver_model:
                            data = jsonObject.getIntValue("data");
                            switch (data) {
                                case LeftDoorCommand.Auto:
                                    fragmentManager.beginTransaction().show(AllFragment).commit();
                                    DoorView.pause();
                                    break;
                                case LeftDoorCommand.Remote:
                                    DoorView.setVideoURI(RightDoorlist.get(6));
                                    DoorView.start();
                                    fragmentManager.beginTransaction().hide(AllFragment).commit();
                                    AllFragment.cancel();
                                    break;
                            }
                        case LeftDoorCommand.Left_Work_Sts:
                            data = jsonObject.getIntValue("data");
                            showDoorState(LeftDoorlist, data);
                            break;
                        case RightDoorCommand.Right_Work_Sts:
                            data = jsonObject.getIntValue("data");
                            showDoorState(RightDoorlist, data);
                            break;
                    }
                } catch (IOException e) {
                    // 命令解释错误则重新读取命令
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            UDP_receive();
        }
    }

    // 显示门的状态
    private void showDoorState(final List<Uri> list, final int DoorState) {
        new Thread() {
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //更新UI
                        switch (DoorState) {
                            // opening
                            case 1:
                                play(DoorView, list.get(opening));
                                AllFragment.pause();
                                fragmentManager.beginTransaction().hide(AllFragment).commit();
                                break;
                            // opened
                            case 3:
                                pause(DoorView);
                                AllFragment.start();
                                fragmentManager.beginTransaction().show(AllFragment).commit();
                                break;
                            // closing
                            case 4:
                                play(DoorView, list.get(closing));
                                AllFragment.pause();
                                fragmentManager.beginTransaction().hide(AllFragment).commit();
                                break;
                            // closed
                            case 0:
                                pause(DoorView);
                                AllFragment.start();
                                fragmentManager.beginTransaction().show(AllFragment).commit();
                                break;
                            default:
                                break;
                        }
                    }
                });
            }
        }.start();
    }

//    //替换fragment
//    public void replaceFragment(final Fragment fragment) {
//        this.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                // 获得一个 FragmentTransaction 的实例
//                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//
//                // 先隐藏所有fragment
//                for (Fragment fragment1 : fragmentManager.getFragments())
//                    fragmentTransaction.hide(fragment1);
//
//                // 再显示fragment
//                if (fragment.isAdded())
//                    fragmentTransaction.show(fragment);
//                else
//                    fragmentTransaction.add(R.id.fragment_container, fragment);
//                fragmentTransaction.commitAllowingStateLoss();
//            }
//        });
//    }


    public void play(final VideoView videoView, final Uri uri) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                videoView.setVideoURI(uri);
                videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mp.setLooping(true);
                        mp.start();
                    }
                });
                videoView.start();
            }
        });
    }

    public void pause(final VideoView videoView) {
        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        videoView.pause();
                    }
                }
        );
    }

    public void start(final VideoView videoView) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                videoView.start();
            }
        });
    }

    /**
     * 隐藏虚拟按键，并且全屏
     */
    protected void hideBottomUIMenu() {
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }
}
