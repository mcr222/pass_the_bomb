/*
 * ©Copyright, 2016 Marc Cayuela Rafols
 * All Rights Reserved.
 */

package com.example.mcr222.pass_bomb;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by mcr222 on 8/07/16.
 */
public class InitialReceptionConnectionsThread extends Thread {
    private final BluetoothServerSocket mmServerSocket;

    public InitialReceptionConnectionsThread() {
        // Use a temporary object that is later assigned to mmServerSocket,
        // because mmServerSocket is final
        BluetoothServerSocket tmp = null;
        try {
            // MY_UUID is the app's UUID string, also used by the client code
            //TODO: review name
            tmp = BluetoothServices.getmBluetoothAdapter().listenUsingRfcommWithServiceRecord(MainActivity.SERVICE_NAME, MainActivity.uuid);
        } catch (IOException e) { }
        mmServerSocket = tmp;
    }

    public void run() {
        BluetoothSocket socket = null;
        // Keep listening until exception occurs or a socket is returned
        while (true) {
            try {
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                break;
            }
            // If a connection was accepted
            if (socket != null) {
                // Do work to manage the connection (in a separate thread)
                System.out.println("Socket Accepted!!");
                new MessagingThread(socket).start();
                break;
            }
        }
        cancel();
    }

    /** Will cancel the listening socket, and cause the thread to finish */
    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) { }
    }
}
