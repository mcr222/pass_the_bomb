package com.example.mcr222.pass_bomb;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by mcr222 on 8/07/16.
 */
public class MessagingThread extends Thread{

    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private final BluetoothDevice remoteDevice;

    public MessagingThread(BluetoothSocket socket) {
        System.out.println("Starting messaging thread " + socket.getRemoteDevice().getName());
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;

        remoteDevice = mmSocket.getRemoteDevice();
        BluetoothServices.putInMessagingThreadMap(getRemotePlayer(),this);
    }

    private Player getRemotePlayer() {
        return new Player(remoteDevice.getName(),remoteDevice.getAddress());
    }

    public void run() {
        byte[] buffer = new byte[1024];  // buffer store for the stream
        int bytes; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs
        while (!this.isInterrupted()) {
            try {
                // Read from the InputStream
                System.out.println("Reading bytes");
                bytes = mmInStream.read(buffer);
                System.out.println("Bytes read:" + Integer.toString(bytes));
                // Send the obtained bytes to the UI activity
                BluetoothServices.receiveMessage(getRemotePlayer(),Message.parseMessage(bytes,buffer));
            } catch (IOException e) {
                break;
            }
        }
    }


    /* Call this from the main activity to send data to the remote device */
    public void write(Message message) {
        try {
            System.out.println("Writing bytes");
            message.incrementRetries();
            mmOutStream.write(message.serializeMessage());
        } catch (IOException e) {
            if(!message.getType().equals(Message.MessageType.unloadBomb) || message.getRetries()<2) {
                System.out.println(e);
                System.out.println("Putting in pending messages");
                BluetoothServices.addToSendingQueue(new PendingMessage(getRemotePlayer(), message));
                System.out.println("Restarting connection");
                ArrayList<Player> players = new ArrayList<Player>();
                players.add(getRemotePlayer());
                BluetoothServices.setConnections(players);
            }
        }
    }

    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            mmInStream.close();
            mmOutStream.close();
            mmSocket.close();
        } catch (IOException e) { }
    }

}
