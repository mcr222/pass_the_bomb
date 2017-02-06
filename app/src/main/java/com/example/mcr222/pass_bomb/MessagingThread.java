/*
 * ©Copyright, 2016 Marc Cayuela Rafols
 * All Rights Reserved.
 */

package com.example.mcr222.pass_bomb;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * This thread works on an existing bluetooth and is responsible of sending and receiving bluetooth
 * messages. There is one messaging thread per socket (and each socket is create per player), so
 * each MessagingThread instance will receive messages from a particular player.
 *
 * Created by Marc Cayuela Rafols on 8/07/16.
 */
public class MessagingThread extends Thread{

    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private final BluetoothDevice remoteDevice;

    /**
     * Creates a messaging thread with the socket (and player behind socket).
     *
     * @param socket socket that represents the bluetooth connection
     */
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

        //the streams are used in order to receive and send data
        mmInStream = tmpIn;
        mmOutStream = tmpOut;

        //store this messaging thread in the map with all the messaging threads per player
        remoteDevice = mmSocket.getRemoteDevice();
        BluetoothServices.putInMessagingThreadMap(getRemotePlayer(),this);
    }

    /**
     * Get the player at the other side of this messaging connection
     * @return the remove player to whom this thread is connected
     */
    private Player getRemotePlayer() {
        return new Player(remoteDevice.getName(),remoteDevice.getAddress());
    }

    /**
     * Run the messaging thread by listening to input data streams
     */
    public void run() {
        byte[] buffer = new byte[1024];  // buffer store for the stream
        int bytes; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs
        while (!this.isInterrupted()) {
            try {
                // Read from the InputStream a stream of bytes
                System.out.println("Reading bytes");
                bytes = mmInStream.read(buffer);
                System.out.println("Bytes read:" + Integer.toString(bytes));
                //Parse the stream of bytes and pass message to the BluetoothServices to process message
                BluetoothServices.receiveMessage(getRemotePlayer(),Message.parseMessage(bytes,buffer));
            } catch (IOException e) {
                break;
            }
        }
    }


    /**
     * Sends data (a message) to the remote device of this messaging thread
     *
     * @param message message to send
     */
    public void write(Message message) {
        try {
            System.out.println("Writing bytes");
            message.incrementRetries();
            mmOutStream.write(message.serializeMessage());
        } catch (IOException e) {
            //if message could not be sent then try to resend. Do not try to resend message if it is
            //an unload bomb message, since this messages should not be resend automatically
            //additionally only allow for 2 retries to send the message
            if(!message.getType().equals(Message.MessageType.unloadBomb) || message.getRetries()<2) {
                System.out.println(e);
                System.out.println("Putting in pending messages");
                //add message to pending queue
                BluetoothServices.addToSendingQueue(new PendingMessage(getRemotePlayer(), message));
                System.out.println("Restarting connection");
                //since the message could not be sent, it is a potential problem witht he connection,
                //so connection is tried to reestablish.
                //TODO:careful with reestablish connection, maybe tpo many connections are created and not closed
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
