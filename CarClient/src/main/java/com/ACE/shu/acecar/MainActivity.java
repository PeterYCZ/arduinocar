package com.ACE.shu.acecar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

public class MainActivity extends Activity {
    final String hotspotIP = "192.168.43.1";
    final int controlPort = 8000;
    final int vedioPort = 9999;

    AsyncTask<byte[], Void, Void> controlTask;
    Socket controlSocket = null;
    DataOutputStream controlsOutputStream = null;

    Thread vedioGetTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkWifiStatus(); /*检测wifi状态*/

        /* Vedio Streaming. */
//        vedioGetTask = new VideoStreamThread(this);
//        vedioGetTask.start();

    }

    @Override
    protected void onResume() {
        super.onResume();
        checkWifiStatus();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (controlSocket != null && controlSocket.isConnected()) {
            try {
                controlSocket.close();
                controlSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (controlSocket != null && controlSocket.isConnected()) {
            try {
                controlSocket.close();
                controlSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkWifiStatus() {
        try {
            WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            String ssid = wifiManager.getConnectionInfo().getSSID();
            if (ssid == null || (!ssid.equals("ForACE") && (!ssid.equals("\"" + "ForACE" +"\"")))) {
                Toast.makeText(this, "请连接到ForACE", Toast.LENGTH_LONG).show();
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendControlByte(View view) {

        byte[] controlDirective = null;

        switch (view.getId()) {
            case R.id.button_up:
                controlDirective = new byte[]{0, 0, 1};
                break;
            case R.id.button_down:
                controlDirective = new byte[]{1, 0, 0};
                break;
            case R.id.button_left:
                controlDirective = new byte[]{0, 1, 0};
                break;
            case R.id.button_right:
                controlDirective = new byte[]{0, 1, 1};
                break;
            case R.id.button_start_avoid:
                controlDirective = new byte[]{1, 0, 1};
                break;
            case R.id.button_stop_avoid:
                controlDirective = new byte[]{1, 1, 0};
                break;
            case R.id.button_stop:
                controlDirective = new byte[]{1, 1, 0};
                break;
            case R.id.button_voice:
                controlDirective = new byte[]{0, 0, 0};
                break;
        }


        controlTask = new AsyncTask<byte[], Void, Void>() {
            @Override
            protected Void doInBackground(byte[]... buffers) {
                if (controlSocket == null && controlsOutputStream == null) {
                    try {
                        controlSocket = new Socket(hotspotIP, controlPort);
                        controlsOutputStream = new DataOutputStream(controlSocket.getOutputStream());
                    } catch (IOException e) {
                        Log.d("controlTask","SocketError");
                        e.printStackTrace();
                    }
                }

                try {
                    controlsOutputStream.write(buffers[0], 0, buffers[0].length);
                    controlsOutputStream.flush();
                    Log.d("Send", buffers[0][0] + " " +  buffers[0][1] + " " + buffers[0][2]);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        controlTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, controlDirective);
    }
}
