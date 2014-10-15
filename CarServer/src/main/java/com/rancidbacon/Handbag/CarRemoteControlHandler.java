package com.rancidbacon.Handbag;

import android.util.Log;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class CarRemoteControlHandler extends Thread {
    private BaseActivity baseActivity;
    private Socket socket;

    public CarRemoteControlHandler(BaseActivity baseActivity, Socket socket) {
        super();
        this.baseActivity = baseActivity;
        this.socket = socket;
        Log.d("CarRemoteControlHandler", "CreateThread");
    }

    @Override
    public void run() {

        DataInputStream dataInputStream = null;
        try {
            dataInputStream = new DataInputStream(socket.getInputStream());
            byte[] controlDirective = new byte[3];

            while(true) {
                if(dataInputStream.read(controlDirective, 0, 3) == -1){
                    return;
                }

                int controlOp = controlDirective[0] * 4 + controlDirective[1] * 2 + controlDirective[2];

                Log.d("Directive", controlDirective[0] + " " + controlDirective[1] + " " + controlDirective[2]);
                Log.d("Directive", "" + controlOp);

                if(baseActivity.mOutputStream == null) {
                    baseActivity.finish();
                }

                baseActivity.sendCommand(BaseActivity.COMMAND_GOT_EVENT, BaseActivity.EVENT_BUTTON_CLICK, controlOp);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
