package com.example.mcr222.pass_bomb;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * This class includes all the functionalities related with bluetooth connection and message
 * sending through bluetooth.
 *
 * Created by mcr222 on 27/06/16.
 */
public class BluetoothServices {

    private static HashMap<Player,MessagingThread> messagingThreadMap = new HashMap<Player,MessagingThread>();
    private static BluetoothAdapter mBluetoothAdapter;
    private static ArrayList<Player> allPlayers = null;
    private static boolean started=false;
    private static MainActivity mainActivity;
    private static LinkedList<PendingMessage> sendingQueue = new LinkedList<>();
    private static InitialReceptionConnectionsThread initialConnectionThread;
    private static Timer initialTimer;
    private static BluetoothReceiver mReceiver;
    private static int receivedConfirmations = 0;


    public synchronized static void putInMessagingThreadMap(Player player, MessagingThread messagingThread) {
        System.out.println("Add messaging thread to map");
        System.out.println(player);
        System.out.println(messagingThread);
        if(messagingThreadMap.containsKey(player)) {
            System.out.println("Already contained a thread for this player, cancelling");
            messagingThreadMap.get(player).cancel();
            messagingThreadMap.get(player).interrupt();
        }
        messagingThreadMap.put(player, messagingThread);
        sendPendingMessages();
    }

    public static void setStarted(boolean started){
        //TODO: this doesn't check if bluetooth is already started!!!

        BluetoothServices.started = started;
    }

    public static void setMainActivity(MainActivity mainActivity) {
        BluetoothServices.mainActivity = mainActivity;
    }

    public static BluetoothAdapter getmBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    public static void startBluetoothServices() {
        mReceiver = new BluetoothReceiver();
        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);

        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);

        mainActivity.mainRegisterReceiver(mReceiver, filter);

        if(!started) {
           mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if(mBluetoothAdapter!=null){
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    mainActivity.startActivityForResult(enableBtIntent, MainActivity.BLUETOOTH_ON);
                } else {
                    started = true;
                }
            }

        }
    }

    public static void makeBluetoothDiscoverable() {
        System.out.println("Started");
        System.out.println(started);
        if(started) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 60);
            mainActivity.startActivity(discoverableIntent);
        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND
     private static class BluetoothReceiver extends BroadcastReceiver{

        private HashSet<Player> playerArraySet = new HashSet<Player>();
        private Callback callback;

        public BluetoothReceiver() {
            super();
        }

        public void setCallback(Callback callback) {
            this.callback = callback;
        }


        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                System.out.println(device.getName());
                // Add the name and address to an array adapter to show in a ListView
                playerArraySet.add(new Player(device.getName(),device.getAddress()));
            }
            else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if(callback!=null) {
                    callback.callback(new ArrayList<Player>(playerArraySet));
                }
            }
            else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                System.out.println("Bluetooth state changed, state:");
                System.out.println(state);
                if(state ==  BluetoothAdapter.STATE_TURNING_OFF) {
                    System.out.println("Game is stopped!!");
                    mainActivity.stopGame();
                }
            }
            else if(BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)) {
                System.out.println("Pairing request!!!");
                try {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    int pin = intent.getIntExtra("android.bluetooth.device.extra.PAIRING_KEY", 1234);
                    System.out.println(pin);
                    //the pin in case you need to accept for an specific pin
                    byte[] pinBytes;
                    pinBytes = ("" + pin).getBytes("UTF-8");
                    if(Build.VERSION.SDK_INT >18) {
                        System.out.println("Automatically confirming");
                        device.setPin(pinBytes);
                        //setPairing confirmation if needed
                        device.setPairingConfirmation(true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void detectNearbyDevices(Callback bluetoothCallback) {

        mReceiver.setCallback(bluetoothCallback);

        mBluetoothAdapter.startDiscovery();

    }

    public static void setConnections(ArrayList<Player> playersToConnect){
        //TODO: review
        for(int i=0;i<playersToConnect.size();++i) {
            new ConnectionThread(playersToConnect.get(i)).start();
        }

    }


    public static void cancelWaitingForInitialConnection() {
        initialConnectionThread.cancel();
        initialTimer.cancel();
    }

    public static void waitForInitialConnection() {
        initialConnectionThread = new InitialReceptionConnectionsThread();
        initialConnectionThread.start();
        //this insists until all initial messages have been sent
        initialTimer = new Timer();
        initialTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                BluetoothServices.sendPendingMessages();
            }
        },10000,10000);
    }

    private static void waitForConnections() {
        //TODO: review
        new ReceptionConnectionsThread().start();
    }

    public static void setAllPlayers(ArrayList<Player> allPlayers) {
        System.out.println("current allPlayers");
        System.out.println(BluetoothServices.allPlayers);
        System.out.println("setting allPlayers");
        System.out.println(allPlayers);
        if(BluetoothServices.allPlayers == null) {
            BluetoothServices.allPlayers = allPlayers;
            System.out.println("Setting players");
            System.out.println(allPlayers.size());
            System.out.println(allPlayers.get(0));
            System.out.println(allPlayers.get(1));
            int myPositionInList = allPlayers.indexOf(getMyselfPlayer());
            System.out.println(myPositionInList);
            //The first position is the game creator and already has established all connections
            if(myPositionInList<allPlayers.size()-1) {
                System.out.println("Setting connections");
                setConnections(new ArrayList<Player>(allPlayers.subList(myPositionInList + 1, allPlayers.size())));
            }
            /*if(myPositionInList>1) {
                //TODO: do not return unless all connections are established!!!
                //Start from the 1st one because connection was established already with the game creator
                waitForConnections(new ArrayList<Player>(allPlayers.subList(1, myPositionInList-1)));
            }*/
            //waitForConnections();
        }
    }

    public static ArrayList<Player> getRandomPlayersList() {
        System.out.println("Detecting nearby players");
        ArrayList<Player> arrayKeys = new ArrayList<Player>(messagingThreadMap.keySet());
        Collections.shuffle(arrayKeys);
        return arrayKeys;
    }

    public synchronized static void sendMessage(Player receiver, Message message){
        System.out.println("Sending message");
        System.out.println(message);
        System.out.println(receiver.getId());
        MessagingThread messagingThread = getFromThreadMap(receiver);

        if (messagingThread != null) {
            //If message is a pending unloadBomb but player does not have bomb then do not send message
            //and discard it (do not add to pending queue)
            if(!message.getType().equals(Message.MessageType.unloadBomb) || MessageProcessor.getBomb().hasBomb()) {
                System.out.println("Message to be send now");
                messagingThread.write(message);
            }
        }
        else {
            System.out.println("Message to queue");
            addToSendingQueue(new PendingMessage(receiver,message));
        }
    }

    private synchronized static MessagingThread getFromThreadMap(Player receiver) {
        return messagingThreadMap.get(receiver);
    }

    public synchronized static void addToSendingQueue(PendingMessage pendingMessage){
        //TODO: consider syncrhonizing with different objects messaginThread map and sendingQueue
        System.out.println(pendingMessage);
        sendingQueue.add(pendingMessage);
    }

    public static boolean hasPendingUnloadBomb() {
        Iterator<PendingMessage> it = sendingQueue.iterator();
        while(it.hasNext()) {
            if(it.next().getMessageToSend().getType().equals(Message.MessageType.unloadBomb)) {
                return true;
            }
        }
        return false;
    }

    public synchronized static void sendPendingMessages() {
        //TODO: mooolt important!! el bluetooth torna a demanar la vinculació dels dispositius quan surten de l'abast del bluetooth!!!
        System.out.println("Sending pending messages");
        System.out.println(sendingQueue.size());
        PendingMessage pendingMessage;
        LinkedList<PendingMessage> pendingMessages = new LinkedList<>(sendingQueue);
        while(!pendingMessages.isEmpty()) {
            sendingQueue.poll();
            pendingMessage = pendingMessages.poll();
            //Have to be careful, if message is a pending unloadBomb it must still have the bomb
            if(!pendingMessage.getMessageToSend().getType().equals(Message.MessageType.unloadBomb)
                    || MessageProcessor.getBomb().hasBomb() ) {
                System.out.println("Pending message");
                System.out.println(pendingMessage.getPlayerToSend());
                System.out.println(pendingMessage.getMessageToSend());
                sendMessage(pendingMessage.getPlayerToSend(), pendingMessage.getMessageToSend());
            }
        }
    }

    public synchronized static void receiveMessage(Player sender, Message message){
        System.out.println("Received new message");
        System.out.println(sender);
        System.out.println(message);
        System.out.println(message.getType());
        Message.MessageType messageType = message.getType();
        if(messageType.equals(Message.MessageType.unloadBomb)) {
            System.out.println("Switch tooooo");
            System.out.println("unloadBomb");
            MessageProcessor.receiveUnloadBomb(sender);
        }
        else if(messageType.equals(Message.MessageType.ifBombUnloaded)) {
            System.out.println("Switch tooooo");
            System.out.println("ifBombUnloaded");
            MessageProcessor.receiveIfBombUnloaded(message);
        }
        else if(messageType.equals(Message.MessageType.newGame)) {
            System.out.println("Switch tooooo");
            System.out.println("newGame");
            MessageProcessor.receiveNewGame(message);
        } else if (messageType.equals(Message.MessageType.receivedNewGame)) {
            System.out.println("Other's received new game");
            System.out.println(sender);
            ++receivedConfirmations;
            killInitialConnection();
        }
        else {
            System.out.println("Message type not found!!!");
        }
    }

    public static void killInitialConnection() {
        if(receivedConfirmations>=allPlayers.size()-1 && MainActivity.isGameOn()){
            cancelWaitingForInitialConnection();
            waitForConnections();
        }
    }

    public static Player getMyselfPlayer() {
        return new Player(mBluetoothAdapter.getName(),mBluetoothAdapter.getAddress());
    }

}
