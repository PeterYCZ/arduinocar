package com.rancidbacon.Handbag;

import android.util.Log;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class CarRemoteControlThread extends Thread{
    final int controlPort = 8000;
    private BaseActivity baseActivity;

    public CarRemoteControlThread(BaseActivity baseActivity){
        this.baseActivity = baseActivity;
    }

    @Override
    public void run() {
        try {
            ServerSocket server = new ServerSocket(controlPort);

            while(true){
                Socket remotePerson = server.accept();
                CarRemoteControlHandler handler = new CarRemoteControlHandler(baseActivity, remotePerson);
                handler.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
