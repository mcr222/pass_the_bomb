/*
 * ©Copyright, 2016 Marc Cayuela Rafols
 * All Rights Reserved.
 */

package com.example.mcr222.pass_bomb;

import android.app.Activity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Logic that creates and sends messages (Message.java). This basically creates the logic that interprets
 * each kind of message.
 *
 * Very tightly related to the BluetoothServices.java class.
 *
 * Created by Marc Cayuela Rafols on 27/06/16.
 */
public class MessageProcessor {
    //object that represents the bomb of this game (it is in this class because it is where it is
    // most used
    private static Bomb bomb;
    private static NewGameActivity newGameActivity;

    /*private static Timer timerBlockBomb = new Timer();;
    private static long timerBlockInterval = 10000;*/

    //whether the bomb was unloaded or not
    private static boolean bombUnloaded = false;

    public static void setNewGameActivity(NewGameActivity newGameActivity){
        MessageProcessor.newGameActivity = newGameActivity;
    }

    public static void setBomb(Bomb bomb) {
        MessageProcessor.bomb = bomb;
    }

    public static Bomb getBomb() {
        return bomb;
    }

    /**
     * Tries to send to all players the bomb to unload it to any player. BluetoothServices makes sure
     * that the unloadBomb is sent only to one user at a time (but that all are tried).
     */
    public static void sendUnloadBomb(){
        if(bomb.hasBomb()) {
            //gets a randomized list of players to send randomly
            ArrayList<Player> allPlayers = BluetoothServices.getRandomPlayersList();
            System.out.println("All players:");
            System.out.println(allPlayers);
            Iterator<Player> it = allPlayers.iterator();
            while(it.hasNext()) {
                bombUnloaded = false;
                //creates messages of type unloadBomb to be sent to all players (BluetoothServices
                //will take into account not resending bomb multiple times)
                //TODO: check that unloadBomb messages are sent one by one!!!
                BluetoothServices.sendMessage(it.next(), new Message(Message.MessageType.unloadBomb, ""));
            }
        }
    }


    /**
     * Receives whether the bomb was successfully unloaded or not
     *
     * @param message unloadBomb message
     */
    public synchronized static void receiveIfBombUnloaded(Message message){
        System.out.println("Received bomb if unloaded!!");
        System.out.println(message);
        System.out.println(message.getClass());
        System.out.println(message.getPayload());
        System.out.println(message.getPayload().getClass());
        //hide bomb if it was successfully unloaded
        bombUnloaded = (Boolean) message.getPayload();
        if (bombUnloaded) {
            bomb.hideBomb();
        }
    }

  /*  private static class UnloadTimerTask extends TimerTask {
        private Player sender;

        UnloadTimerTask(Player sender) {
            this.sender = sender;
        }

        @Override
        public void run() {
            if(PhoneService.isPhoneLocked()) {
                getBomb(sender);
            }
        }
    }*/

    /**
     * Gets the bomb from a remote player. That is, some other player has unloaded the bomb to this
     * player.
     *
     * @param sender who sent the bomb to this player
     */
    private static void getBomb(Player sender) {
        try {
            //this player now has the bomb
            bomb.showBomb();
            //sends to the sender player that the bomb was succesfully unloaded
            sendIfBombUnloaded(sender, true);
        } catch (HasBombException exception) {
            //Should never be an exception as if it has bomb is checked
            //previously (in receiveUnloadBomb function)
            sendIfBombUnloaded(sender, false);
        }
    }

    /**
     * Receives an unload bomb message. That is, another remote player is trying to unload bomb to
     * this player.
     *
     * @param sender player that's trying to unload the bomb.
     */
    public synchronized static void receiveUnloadBomb(Player sender) {
        System.out.println("Receiving bomb");
        //only can receive the bomb if it doesn't have a bomb already and the player's phone is
        //unlocked, otherwise the bomb is not unloaded
        if(!bomb.hasBomb()) {
            if (PhoneService.isPhoneLocked()) {
                System.out.println("Phone locked, can't unload");
                sendIfBombUnloaded(sender, false);
                //TODO: this is not synchronized as it is not blocking
                //timerBlockBomb.schedule(new UnloadTimerTask(sender), timerBlockInterval);

            } else {
                System.out.println("Phone unlocked, receiving bomb");
                getBomb(sender);
            }
        }
        else {
            sendIfBombUnloaded(sender, false);
        }
    }

    /**
     * Sends a message of type ifBombUnloaded, that specifies whether the bomb was unloaded to this
     * player or not
     *
     * @param sender who sent the bomb
     * @param unloaded whether the bomb was unloaded to this player (player receives the bomb)
     */
    public static void sendIfBombUnloaded(Player sender, boolean unloaded){
        System.out.println("Sending if bomb was unloaded!!");
        BluetoothServices.sendMessage(sender,new Message(Message.MessageType.ifBombUnloaded, new Boolean(unloaded)));
    }

    /**
     * Handles the unlock event by activating the bomb
     */
    public static void handleUnlockEvent(){
        System.out.println("unlocks!");
        bomb.activateBomb();
    }

    /**
     * Sends a message containing the new game to be started (that's when this player has done
     * the set up of the game)
     *
     * @param player player to be sent the game
     * @param game game to send to the player
     */
    public static void sendNewGame(Player player, Game game) {
        BluetoothServices.sendMessage(player,new Message(Message.MessageType.newGame,game));
    }

    /**
     * Receives a new game message (that's when another player has done the set up of the game)
     *
     * @param message message containing the game
     */
    public synchronized static void receiveNewGame(Message message) {
        //set the new game
        Game game = (Game)message.getPayload();
        newGameActivity.setGame(game);

        //sends to all players the message confirming the reception of the new game.
        //It is sent to all players in order to create connections with all the players in the game
        //TODO: doesn't this create duplicate connections??
        Iterator<Player> it = game.getPlayers().iterator();
        while(it.hasNext()) {
            Player player = it.next();
            if(!player.equals(BluetoothServices.getMyselfPlayer())) {
                BluetoothServices.sendMessage(player, new Message(Message.MessageType.receivedNewGame,""));
            }
        }
    }


}
