/*
 * ©Copyright, 2016 Marc Cayuela Rafols
 * All Rights Reserved.
 */

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
 * sending through bluetooth. THIS IS A CRITICAL CLASS.
 *
 * Created by Marc Cayuela Rafols on 27/06/16.
 */
public class BluetoothServices {

    //map containing one messaging thread per player in order to communicate with all of them
    private static HashMap<Player,MessagingThread> messagingThreadMap = new HashMap<Player,MessagingThread>();

    //represents this device bluetooth adapter
    private static BluetoothAdapter mBluetoothAdapter;
    //list of all players
    private static ArrayList<Player> allPlayers = null;
    private static boolean started=false;
    private static MainActivity mainActivity;
    //queue with all pending messages
    private static LinkedList<PendingMessage> sendingQueue = new LinkedList<>();
    private static InitialReceptionConnectionsThread initialConnectionThread;
    private static Timer initialTimer;

    //this receives all the actions that want to be tracked regarding bluetooth (see startBluetoothServices())
    private static BluetoothReceiver mReceiver;
    private static int receivedConfirmations = 0;


    /**
     * Puts the messaging thread for the player in the map. If the player already had a thread, then cancel
     * previous thread and update with this new one (this is done to reestablish connections between players)
     *
     * @param player player to which the thread communicates to
     * @param messagingThread
     */
    public synchronized static void putInMessagingThreadMap(Player player, MessagingThread messagingThread) {
        System.out.println("Add messaging thread to map");
        System.out.println(player);
        System.out.println(messagingThread);
        //stops the previous thread for the player
        if(messagingThreadMap.containsKey(player)) {
            System.out.println("Already contained a thread for this player, cancelling");
            messagingThreadMap.get(player).cancel();
            messagingThreadMap.get(player).interrupt();
        }

        messagingThreadMap.put(player, messagingThread);
        //tries to send pending messages since a connection has been reestablished
        sendPendingMessages();
    }

    /**
     * Sets whether bluetooth is on or not
     *
     * @param started
     */
    public static void setStarted(boolean started){
        BluetoothServices.started = started;
    }

    public static void setMainActivity(MainActivity mainActivity) {
        BluetoothServices.mainActivity = mainActivity;
    }

    public static BluetoothAdapter getmBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    /**
     * Starts bluetooth and listens to pairing requests and bluetooth state changes
     */
    public static void startBluetoothServices() {
        mReceiver = new BluetoothReceiver();
        //Register the BroadcastReceiver for multiple bluetooth actions

        //when another bluetooth device is discovered in range
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);

        //when discovering action finishes (discovering means that bluetooth searches for nearby devices)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        //when bluetooth changes from on to off
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

        //when another device requests pairing with this device
        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);

        mainActivity.mainRegisterReceiver(mReceiver, filter);

        if(!started) {
            //tries to start bluetooth if it is not on
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

    /**
     * Asks to the user to make the bluetooth discoverable in order to be able to find the other players.
     */
    public static void makeBluetoothDiscoverable() {
        System.out.println("Started");
        System.out.println(started);
        if(started) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 60);
            mainActivity.startActivity(discoverableIntent);
        }
    }


    /**
     * This class processes all the actions registered regarding bluetooth changes
     */
    private static class BluetoothReceiver extends BroadcastReceiver{

        //set of players that have been discovered
        private HashSet<Player> playerArraySet = new HashSet<Player>();
        private Callback callback;

        public BluetoothReceiver() {
            super();
        }

        public void setCallback(Callback callback) {
            this.callback = callback;
        }


        /**
         * Receives a registered action and processed it depending on the type of action it is
         *
         * @param context
         * @param intent
         */
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
            //when discovering finishes use callback to return results
            else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if(callback!=null) {
                    callback.callback(new ArrayList<Player>(playerArraySet));
                }
            }
            //check whether bluetooth was turned off to avoid cheating
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
                    //tries to automatically respond to the pairing request
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

    /**
     * Starts discovering activity, trying to find bluetooth devices nearby
     *
     * @param bluetoothCallback function to call when discovering nearby devices finishes
     */
    public static void detectNearbyDevices(Callback bluetoothCallback) {

        mReceiver.setCallback(bluetoothCallback);

        mBluetoothAdapter.startDiscovery();

    }

    /**
     * Start connections for all players in list
     *
     * @param playersToConnect players to connect to
     */
    public static void setConnections(ArrayList<Player> playersToConnect){
        //TODO: review
        for(int i=0;i<playersToConnect.size();++i) {
            new ConnectionThread(playersToConnect.get(i)).start();
        }

    }

    /**
     * Cancel the initial connection threads
     */
    public static void cancelWaitingForInitialConnection() {
        initialConnectionThread.cancel();
        initialTimer.cancel();
    }

    /**
     * Start initial connection thread, that waits and receives connections from players
     */
    public static void waitForInitialConnection() {
        initialConnectionThread = new InitialReceptionConnectionsThread();
        initialConnectionThread.start();
        //this insists until all initial messages have been sent
        initialTimer = new Timer();
        /*initialTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                BluetoothServices.sendPendingMessages();
            }
        },10000,10000);*/
    }

    /**
     * This creates a thread for this player in order to wait for incoming connections
     */
    private static void waitForConnections() {
        //TODO: review
        new ReceptionConnectionsThread().start();
    }

    /**
     * Sets all game players and initiates connections with all of them with higher index.
     * This is done in order to have the connections only once per pair of players (each player will
     * connect with all players with higher index, and then all players will be connected between them)
     *
     * @param allPlayers
     */
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
            //The first position is the game creator and already has established all connections with the rest
            //Therefore, all previous players in the list have connected to all the players after them. Thus,
            //this player only need to connect with the ones after himself in the list, as the connections
            //with the previous players have been established by the previous players
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

    /**
     * Get the list of players but in a random order
     *
     * @return shuffled players list
     */
    public static ArrayList<Player> getRandomPlayersList() {
        System.out.println("Detecting nearby players");
        ArrayList<Player> arrayKeys = new ArrayList<Player>(messagingThreadMap.keySet());
        Collections.shuffle(arrayKeys);
        return arrayKeys;
    }

    /**
     * Send a message to receiver
     *
     * @param receiver
     * @param message
     */
    //TODO: BluetoothServices must make sure that the unloadBomb is sent only to one user at a time.
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

    /**
     *  Get messaging thread from the map for the specific player
     *
     * @param receiver player from who to receive messaging thread
     * @return MessagingThread between this player and receiver
     */
    private synchronized static MessagingThread getFromThreadMap(Player receiver) {
        return messagingThreadMap.get(receiver);
    }

    /**
     * Add pending message to queue of pending messages
     * @param pendingMessage
     */
    public synchronized static void addToSendingQueue(PendingMessage pendingMessage){
        //TODO: consider syncrhonizing with different objects messaginThread map and sendingQueue
        System.out.println(pendingMessage);
        sendingQueue.add(pendingMessage);
    }

    /**
     * Whether the pending message queue has an unload bomb pending message or not
     *
     * TODO: IMPORTANT!! unload bomb are never put in the pending message queue!!!! (see messagethread class)
     * @return
     */
    public static boolean hasPendingUnloadBomb() {
        Iterator<PendingMessage> it = sendingQueue.iterator();
        while(it.hasNext()) {
            if(it.next().getMessageToSend().getType().equals(Message.MessageType.unloadBomb)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tries to resend all pending messages
     */
    public synchronized static void sendPendingMessages() {
        //TODO: mooolt important!! el bluetooth torna a demanar la vinculació dels dispositius quan surten de l'abast del bluetooth!!!
        System.out.println("Sending pending messages");
        System.out.println(sendingQueue.size());
        PendingMessage pendingMessage;
        LinkedList<PendingMessage> pendingMessages = new LinkedList<>(sendingQueue);
        //TODO: important, no resending messages of unload bomb when phone is locked, since then it bomb could be
        //TODO: send by the phone when resending messages (this case is protected by the fact that messages are only
        //TODO: resend when user presses send button
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

    /**
     * Receives a message and sends it to the according function depending on the type of message
     * @param sender
     * @param message
     */
    public synchronized static void receiveMessage(Player sender, Message message){
        System.out.println("Received new message");
        System.out.println(sender);
        System.out.println(message);
        if(message==null) {
            System.out.println("Received NULL message!");
            return;
        }
        System.out.println(message.getType());
        //process depending on the message type
        Message.MessageType messageType = message.getType();
        if(messageType.equals(Message.MessageType.unloadBomb)) {
            System.out.println("unloadBomb");
            MessageProcessor.receiveUnloadBomb(sender);
        }
        else if(messageType.equals(Message.MessageType.ifBombUnloaded)) {
            System.out.println("ifBombUnloaded");
            MessageProcessor.receiveIfBombUnloaded(message);
        }
        else if(messageType.equals(Message.MessageType.newGame)) {
            System.out.println("newGame");
            MessageProcessor.receiveNewGame(message);
        } else if (messageType.equals(Message.MessageType.receivedNewGame)) {
            System.out.println("Other's received new game");
            System.out.println(sender);
            //increase receive new game confirmations
            ++receivedConfirmations;
            //try to kill initial connection (only kills it if received confirmations are received
            //from all players
            killInitialConnection();
        }
        else {
            System.out.println("Message type not found!!!");
        }
    }

    /**
     * Cancel initial reception connection thread once all confirmations from all players are received (meaning
     * that all players have received the game), and start a "normal" reception connection thread
     */
    public static void killInitialConnection() {
        if(receivedConfirmations>=allPlayers.size()-1 && MainActivity.isGameOn()){
            cancelWaitingForInitialConnection();
            waitForConnections();
        }
    }

    /**
     * Get player that represents myself
     *
     * @return player with my credentials
     */
    public static Player getMyselfPlayer() {
        return new Player(mBluetoothAdapter.getName(),mBluetoothAdapter.getAddress());
    }

}
