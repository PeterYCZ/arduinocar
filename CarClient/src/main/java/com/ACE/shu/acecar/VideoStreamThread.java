package com.ACE.shu.acecar;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class VideoStreamThread extends Thread {
    final String hotspotIP = "192.168.43.1";
    final int vedioPort = 9999;
    private Activity activity;
    private ImageView imView;

    public VideoStreamThread(Activity activity) {
        super();
        this.activity = activity;
        this.imView = (ImageView) activity.findViewById(R.id.video_layout);
    }

    @Override
    public void run() {
        final byte[] magicCode = "ShowMeThePicture".getBytes();
        byte[] inBuffer = new byte[1024 * 768 * 2]; //Todo: PictureSizeProblem

        try{
            DatagramPacket magicPacket = new DatagramPacket(magicCode, magicCode.length);
            DatagramPacket picPacket = new DatagramPacket(inBuffer, inBuffer.length);

            DatagramSocket vedioGetSocket = new DatagramSocket();
            vedioGetSocket.connect(InetAddress.getByName(hotspotIP),vedioPort);
            vedioGetSocket.send(magicPacket);

            while (true) {
                Log.d("Person", "Try to recieve");
                vedioGetSocket.receive(picPacket);
                Bitmap bitmap = BitmapFactory.decodeByteArray(picPacket.getData(), 0, picPacket.getLength());

                imView.setImageBitmap(bitmap);
                imView.postInvalidate();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
