package com.wubeibei.door;

import android.media.MediaPlayer;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private AllVideoFragment AllFragment;
    private FragmentManager fragmentManager;
    private VideoView DoorView;


    private List<String> LeftDoorlist = new ArrayList<>(Arrays.asList(
            Environment.getExternalStorageDirectory() + "/MiniBus/LeftDoor/DoorClose.mp4",
            Environment.getExternalStorageDirectory() + "/MiniBus/LeftDoor/DoorOpen.mp4",
            Environment.getExternalStorageDirectory() + "/MiniBus/LeftDoor/End.mp4",
            Environment.getExternalStorageDirectory() + "/MiniBus/LeftDoor/Fache.mp4",
            Environment.getExternalStorageDirectory() + "/MiniBus/LeftDoor/Start.mp4",
            Environment.getExternalStorageDirectory() + "/MiniBus/LeftDoor/Tingkao.mp4",
            Environment.getExternalStorageDirectory() + "/MiniBus/LeftDoor/Welcome.mp4"
    ));
    private List<String> RightDoorlist = new ArrayList<>(Arrays.asList(
            Environment.getExternalStorageDirectory() + "/MiniBus/RightDoor/DoorClose.mp4",
            Environment.getExternalStorageDirectory() + "/MiniBus/RightDoor/DoorOpen.mp4",
            Environment.getExternalStorageDirectory() + "/MiniBus/RightDoor/End.mp4",
            Environment.getExternalStorageDirectory() + "/MiniBus/RightDoor/Fache.mp4",
            Environment.getExternalStorageDirectory() + "/MiniBus/RightDoor/Start.mp4",
            Environment.getExternalStorageDirectory() + "/MiniBus/RightDoor/Tingkao.mp4",
            Environment.getExternalStorageDirectory() + "/MiniBus/RightDoor/Welcome.mp4"
    ));
    private List<String> LeftDoorPlayList = new ArrayList<>(Arrays.asList(
            Environment.getExternalStorageDirectory() + "/MiniBus/LeftDoor/Welcome.mp4",
            Environment.getExternalStorageDirectory() + "/MiniBus/LeftDoor/Start.mp4",
            Environment.getExternalStorageDirectory() + "/MiniBus/LeftDoor/Start.mp4"
    ));

    private final static int closing = 0;
    private final static int opening = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hideBottomUIMenu();
        new Thread(new Runnable() {
            @Override
            public void run() {
                UDP_receive();
            }
        }).start();
        DoorView = findViewById(R.id.DoorVideo);
        DoorView.setVideoPath(RightDoorlist.get(6));
        DoorView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.setLooping(true);
                mp.start();
            }
        });
        DoorView.start();
        AllFragment = AllVideoFragment.newInstance((ArrayList<String>) LeftDoorPlayList);
        // 初始化fragment管理器
        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().add(R.id.fragment_container, AllFragment).hide(AllFragment).show(AllFragment).commit();
    }

    // 接收CAN总线
    private void UDP_receive() {
        byte[] receMsgs = new byte[14];
        DatagramSocket datagramSocket;
        DatagramPacket datagramPacket;
        try {
            datagramSocket = new DatagramSocket(5556);
            while (true) {
                datagramPacket = new DatagramPacket(receMsgs, receMsgs.length);
                datagramSocket.receive(datagramPacket);
                dispose(datagramPacket.getData());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            UDP_receive();
        }
    }

    // 处理收到的byte数组
    private void dispose(byte[] receMsgs) {
        try {
            JSONObject jsonObject = JSONObject.parseObject(new String(receMsgs));
            LogUtil.d(TAG, jsonObject.toJSONString());
            int id = jsonObject.getIntValue("id");
            int data;
            switch (id) {
                case LeftDoorCommand.Left_Work_Sts:
                    data = jsonObject.getIntValue("data");
                    showDoorState(LeftDoorlist, data);
                    break;
                case RightDoorCommand.Right_Work_Sts:
                    data = jsonObject.getIntValue("data");
                    showDoorState(RightDoorlist, data);
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 显示门的状态
    private void showDoorState(final List<String> list, final int DoorState) {
        new Thread() {
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //更新UI
                        switch (DoorState) {
                            // opening
                            case 1:
                                DoorView.setVideoPath(list.get(opening));
                                fragmentManager.beginTransaction().hide(AllFragment).commit();
                                break;
                            // opened
                            case 3:
                                if (DoorView.isPlaying())
                                    DoorView.pause();
                                fragmentManager.beginTransaction().show(AllFragment).commit();
                                break;
                            // closing
                            case 4:
                                DoorView.setVideoPath(list.get(closing));
                                fragmentManager.beginTransaction().hide(AllFragment).commit();
                                break;
                            // closed
                            case 0:
                                if (DoorView.isPlaying())
                                    DoorView.pause();
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


    public void play(final VideoView videoView, final String path) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                videoView.setVideoPath(Environment.getExternalStorageDirectory() + path);
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
                        if (videoView.isPlaying())
                            videoView.pause();
                    }
                }
        );
    }

    public void resume(final VideoView videoView) {
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
