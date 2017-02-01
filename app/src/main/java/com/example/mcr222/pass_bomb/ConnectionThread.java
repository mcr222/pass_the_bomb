/*
 * ©Copyright, 2016 Marc Cayuela Rafols
 * All Rights Reserved.
 */

package com.example.mcr222.pass_bomb;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;

/**
 * Created by mcr222 on 8/07/16.
 */
public class ConnectionThread extends Thread {
    private final BluetoothSocket mmSocket;

    public ConnectionThread(Player playerToConnect) {
        BluetoothDevice deviceToConnect = BluetoothServices.getmBluetoothAdapter().getRemoteDevice(playerToConnect.getMAC());
        System.out.println("device to connect to");
        System.out.println(deviceToConnect.getAddress());
        System.out.println(deviceToConnect.getName());
        System.out.println(deviceToConnect.toString());
        System.out.println("device to connect to");
        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final
        BluetoothSocket tmp = null;

        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            // MY_UUID is the app's UUID string, also used by the server code
            tmp = deviceToConnect.createInsecureRfcommSocketToServiceRecord(MainActivity.uuid);
            System.out.println("Socket found!");

        } catch (IOException e) { }
        mmSocket = tmp;
    }

    public void run() {
        // Cancel discovery because it will slow down the connection
        BluetoothServices.getmBluetoothAdapter().cancelDiscovery();

        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            mmSocket.connect();
            System.out.println("Socket connect");
        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            try {
                mmSocket.close();
            } catch (IOException closeException) { }
            return;
        }

        // Do work to manage the connection (in a separate thread)
        new MessagingThread(mmSocket).start();
    }

    /** Will cancel an in-progress connection, and close the socket */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }
}