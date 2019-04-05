package com.wubeibei.door;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.VideoView;

import com.alibaba.fastjson.JSONObject;
import com.wubeibei.door.command.LeftDoorCommand;
import com.wubeibei.door.command.RightDoorCommand;
import com.wubeibei.door.util.ByteUtil;
import com.wubeibei.door.util.LogUtil;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private List<Uri> DoorPlaylist;
    private List<Uri> Doorlist;
    private Map<Integer, byte[]> SendMap;
    private final static boolean flag = true; // true 为左门
    private final static int closing = 0;
    private final static int opening = 1;
    private final static int welcome = 6;
    private final static int tingkao = 5;
    private final static int arrow = 4;
    private final static int start = 3;
    private final static int end = 2;
    private final static int dooropen = 1;
    private final static int doorclose = 0;

    private boolean AutoState = false;

    private int msec = 0;
    private int videoindex = 0;
    private VideoView videoView;
    private circulPlay circulPlay = new circulPlay();
    private sequencePlay sequencePlay;

    private final static String HostIp = "192.168.43.1";
    private final static int HostPort = 4001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        hideBottomUIMenu();
        videoView = findViewById(R.id.videoview);
        sequencePlay = new sequencePlay();
        videoView.setVideoURI(Doorlist.get(welcome));
        videoView.start();
        videoView.setOnCompletionListener(new circulPlay());
        new Thread(new Runnable() {
            @Override
            public void run() {
                UDP_receive();
            }
        }).start();
    }

    private void init(){
        SendMap = new HashMap<Integer, byte[]>(){
            {
                put(0, new byte[]{(byte) 0xAA, (byte) 0xBB, 0x20, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0x37});
                put(5, new byte[]{(byte) 0xAA, (byte) 0xBB, (byte) 0xC0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0x37});
                put(10, new byte[]{(byte) 0xAA, (byte) 0xBB, 0x00, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0x36});
                put(11, new byte[]{(byte) 0xAA, (byte) 0xBB, 0x40, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0x37});
                put(12, new byte[]{(byte) 0xAA, (byte) 0xBB, (byte) 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0x37});
                put(13, new byte[]{(byte) 0xAA, (byte) 0xBB, 0x00, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0x36});
                put(14, new byte[]{(byte) 0xAA, (byte) 0xBB, (byte) 0xC0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0x37});
                put(20, new byte[]{(byte) 0xAA, (byte) 0xBB, 0x40, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0x37});
                put(21, new byte[]{(byte) 0xAA, (byte) 0xBB, (byte) 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0x37});
            }
        };
        List<Uri> leftDoorlist = new ArrayList<>(Arrays.asList(
                Uri.parse("android.resource://" + getPackageName() + "/raw/" + R.raw.left_doorclose),
                Uri.parse("android.resource://" + getPackageName() + "/raw/" + R.raw.left_dooropen),
                Uri.parse("android.resource://" + getPackageName() + "/raw/" + R.raw.left_end),
                Uri.parse("android.resource://" + getPackageName() + "/raw/" + R.raw.left_start),
                Uri.parse("android.resource://" + getPackageName() + "/raw/" + R.raw.left_arrow),
                Uri.parse("android.resource://" + getPackageName() + "/raw/" + R.raw.left_tingkao),
                Uri.parse("android.resource://" + getPackageName() + "/raw/" + R.raw.left_welcome)
        ));
        List<Uri> rightDoorlist = new ArrayList<>(Arrays.asList(
                Uri.parse("android.resource://" + getPackageName() + "/raw/" + R.raw.right_doorclose),
                Uri.parse("android.resource://" + getPackageName() + "/raw/" + R.raw.right_dooropen),
                Uri.parse("android.resource://" + getPackageName() + "/raw/" + R.raw.right_end),
                Uri.parse("android.resource://" + getPackageName() + "/raw/" + R.raw.right_start),
                Uri.parse("android.resource://" + getPackageName() + "/raw/" + R.raw.right_arrow),
                Uri.parse("android.resource://" + getPackageName() + "/raw/" + R.raw.right_tingkao),
                Uri.parse("android.resource://" + getPackageName() + "/raw/" + R.raw.right_welcome)
        ));
        List<Uri> rightPlayDoorlist = new ArrayList<>(Arrays.asList(
                rightDoorlist.get(welcome),
                rightDoorlist.get(arrow),
                rightDoorlist.get(start),
                rightDoorlist.get(dooropen),
                rightDoorlist.get(arrow),
                rightDoorlist.get(doorclose),
                rightDoorlist.get(arrow),
                rightDoorlist.get(tingkao),
                rightDoorlist.get(arrow),
                rightDoorlist.get(tingkao),
                rightDoorlist.get(arrow),
                rightDoorlist.get(tingkao),
                rightDoorlist.get(dooropen),
                rightDoorlist.get(arrow),
                rightDoorlist.get(doorclose),
                rightDoorlist.get(end),
                rightDoorlist.get(arrow),
                rightDoorlist.get(end),
                rightDoorlist.get(arrow),
                rightDoorlist.get(end),
                rightDoorlist.get(arrow),
                rightDoorlist.get(dooropen),
                rightDoorlist.get(arrow),
                rightDoorlist.get(arrow),
                rightDoorlist.get(doorclose)
        ));
        List<Uri> leftPlayDoorlist = new ArrayList<>(Arrays.asList(
                leftDoorlist.get(welcome),
                leftDoorlist.get(arrow),
                leftDoorlist.get(start),
                leftDoorlist.get(dooropen),
                leftDoorlist.get(arrow),
                leftDoorlist.get(doorclose),
                leftDoorlist.get(arrow),
                leftDoorlist.get(tingkao),
                leftDoorlist.get(arrow),
                leftDoorlist.get(tingkao),
                leftDoorlist.get(arrow),
                leftDoorlist.get(tingkao),
                leftDoorlist.get(dooropen),
                leftDoorlist.get(arrow),
                leftDoorlist.get(doorclose),
                leftDoorlist.get(end),
                leftDoorlist.get(arrow),
                leftDoorlist.get(end),
                leftDoorlist.get(arrow),
                leftDoorlist.get(end),
                leftDoorlist.get(arrow),
                leftDoorlist.get(dooropen),
                leftDoorlist.get(arrow),
                leftDoorlist.get(arrow),
                leftDoorlist.get(doorclose)
        ));
        if(flag) {
            Doorlist = leftDoorlist;
            DoorPlaylist = leftPlayDoorlist;
        }
        else {
            Doorlist = rightDoorlist;
            DoorPlaylist = rightPlayDoorlist;
        }
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
                                    setAuto();
                                    break;
                                case LeftDoorCommand.Remote:
                                    setRemote();
                                    break;
                            }
                            break;
                        case LeftDoorCommand.Left_Work_Sts:
                            if (flag)
                                showDoorState(jsonObject.getIntValue("data"));
                            break;
                        case RightDoorCommand.Right_Work_Sts:
                            if (!flag)
                                showDoorState(jsonObject.getIntValue("data"));
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

    private void setAuto() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                videoView.setVideoURI(DoorPlaylist.get(videoindex % DoorPlaylist.size()));
                videoView.setOnCompletionListener(sequencePlay);
                videoView.seekTo(msec);
                videoView.start();
                Log.d(TAG, "setAuto: 开始于 ： " + videoindex + "/" + msec);
                AutoState = true;
                if(flag) {
                    if (SendMap.containsKey(videoindex % DoorPlaylist.size()))
                        send(SendMap.get(videoindex % DoorPlaylist.size()));
                }
            }
        });
    }

    private void setRemote(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                videoView.setVideoURI(Doorlist.get(welcome));
                videoView.setOnCompletionListener(circulPlay);
                msec = 0;
                videoindex = 0;
                videoView.start();
                AutoState = false;
            }
        });
    }

    // 显示门的状态
    private void showDoorState(final int DoorState) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //更新UI
                switch (DoorState) {
                    // opening
                    case 1:
                        videoView.pause();
                        if (AutoState) {
                            if (videoView.getCurrentPosition() == videoView.getDuration())
                                msec = 0;
                            else
                                msec = videoView.getCurrentPosition();
                        }
                        Log.d(TAG, "run: 暂停于 ： " + videoindex + "/" + msec);
                        videoView.setVideoURI(Doorlist.get(opening));
                        videoView.setOnCompletionListener(circulPlay);
                        videoView.start();
                        break;
                    // opened
                    case 3:
                        if (AutoState)
                            setAuto();
                        else
                            setRemote();
                        break;
                    // closing
                    case 4:
                        videoView.pause();
                        if (AutoState) {
                            if (videoView.getCurrentPosition() == videoView.getDuration())
                                msec = 0;
                            else
                                msec = videoView.getCurrentPosition();
                        }
                        Log.d(TAG, "run: 暂停于 ： " + videoindex + "/" + msec);
                        videoView.setVideoURI(Doorlist.get(closing));
                        videoView.setOnCompletionListener(circulPlay);
                        videoView.start();
                        break;
                    // closed
                    case 0:
                        if (AutoState)
                            setAuto();
                        else
                            setRemote();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    class circulPlay implements MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mp) {
            Log.d(TAG, "onCompletion: " + "循环播放");
            mp.start();
        }
    }

    class sequencePlay implements MediaPlayer.OnCompletionListener{
        @Override
        public void onCompletion(MediaPlayer mp) {
            videoindex++;
            Log.d(TAG, "onCompletion: " + "切换到 " + videoindex);
            videoView.setVideoURI(DoorPlaylist.get(videoindex % DoorPlaylist.size()));
            videoView.start();
            if(flag) {
                if (SendMap.containsKey(videoindex % DoorPlaylist.size()))
                    send(SendMap.get(videoindex % DoorPlaylist.size()));
            }
        }
    }

    private void send(final byte[] bytes){
        new Thread(new Runnable() {
            @Override
            public void run() {
                DatagramSocket datagramSocket = null;
                DatagramPacket datagramPacket;
                try {
                    datagramSocket = new DatagramSocket();
                    datagramPacket = new DatagramPacket(bytes, bytes.length, InetAddress.getByName(HostIp), HostPort);
                    Log.d(TAG, "run: " + ByteUtil.bytesToHex(bytes));
                    datagramSocket.send(datagramPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (datagramSocket != null) {
                        datagramSocket.close();
                    }
                }
            }
        }).start();
    }

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