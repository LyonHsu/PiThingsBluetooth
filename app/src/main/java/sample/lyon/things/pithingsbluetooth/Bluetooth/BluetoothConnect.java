package sample.lyon.things.pithingsbluetooth.Bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothConnect  {
    BluetoothSocket socket;
    InputStream socketIn = null;
    OutputStream socketOut=null;
    public  void connect(BluetoothDevice device) throws IOException {
        // 固定的UUID
        final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
        UUID uuid = UUID.fromString(SPP_UUID);
        BluetoothSocket socket = device.createRfcommSocketToServiceRecord(uuid);
        socket.connect();
        socketIn = socket.getInputStream();
        socketOut = socket.getOutputStream();
        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();
    }

    public void cancel(){
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
