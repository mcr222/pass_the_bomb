/*
 * ©Copyright, 2016 Marc Cayuela Rafols
 * All Rights Reserved.
 */

package com.example.mcr222.pass_bomb;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;

/**
 * This thread is responsible for creating connections via bluetooth. It basically connects with a
 * player. In order to establish the connections, the other player must have a listening thread that
 * will accept the connections (this is done with the ReceptionConnectionsThread.java).
 *
 * Created by Marc Cayuela Rafols on 8/07/16.
 */
public class ConnectionThread extends Thread {
    private final BluetoothSocket mmSocket;

    /**
     * Creates the thread
     * @param playerToConnect player to connect to
     */
    public ConnectionThread(Player playerToConnect) {
        //gets the device to connect to (the player's device)
        BluetoothDevice deviceToConnect = BluetoothServices.getmBluetoothAdapter().getRemoteDevice(playerToConnect.getMAC());
        System.out.println("device to connect to");
        System.out.println(deviceToConnect.getAddress());
        System.out.println(deviceToConnect.getName());
        System.out.println(deviceToConnect.toString());
        System.out.println("device to connect to");
        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final
        BluetoothSocket tmp = null;

        // Get a BluetoothSocket to connect with the given BluetoothDevice (that is a player)
        try {
            // MY_UUID is the app's UUID string, also used by the reception part of the code
            // creates and insecure RF socket
            tmp = deviceToConnect.createInsecureRfcommSocketToServiceRecord(MainActivity.uuid);
            System.out.println("Socket found!");

        } catch (IOException e) { }
        mmSocket = tmp;
    }

    /**
     * Run the thread to try to connect to the player
     */
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

        //if connection was successful then create a thread that will manage the messaging in the
        // already open connection
        new MessagingThread(mmSocket).start();
    }

    /** Will cancel an in-progress connection, and close the socket */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }
}