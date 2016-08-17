package com.example.mcr222.pass_bomb;

import android.app.Activity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by mcr222 on 27/06/16.
 */
public class MessageProcessor {
    private static Bomb bomb;
    private static NewGameActivity newGameActivity;

    private static Timer timerBlockBomb = new Timer();;
    private static long timerBlockInterval = 10000;

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

    public static void sendUnloadBomb(){
        if(bomb.hasBomb()) {
            ArrayList<Player> allPlayers = BluetoothServices.getRandomPlayersList();
            System.out.println("All players:");
            System.out.println(allPlayers);
            Iterator<Player> it = allPlayers.iterator();
            while(it.hasNext()) {
                bombUnloaded = false;
                BluetoothServices.sendMessage(it.next(), new Message(Message.MessageType.unloadBomb, ""));
            }
        }
    }


    public synchronized static void receiveIfBombUnloaded(Message message){
        System.out.println("Received bomb if unloaded!!");
        System.out.println(message);
        System.out.println(message.getClass());
        System.out.println(message.getPayload());
        System.out.println(message.getPayload().getClass());
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

    private static void getBomb(Player sender) {
        try {
            bomb.showBomb();
            sendIfBombUnloaded(sender, true);
        } catch (HasBombException exception) {
            //Should never be an exception as if it has bomb is checked
            //previously
            sendIfBombUnloaded(sender, false);
        }
    }

    public synchronized static void receiveUnloadBomb(Player sender) {
        System.out.println("Receiving bomb");
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

    public static void sendIfBombUnloaded(Player sender, boolean unloaded){
        System.out.println("Sending if bomb was unloaded!!");
        BluetoothServices.sendMessage(sender,new Message(Message.MessageType.ifBombUnloaded, new Boolean(unloaded)));
    }

    public static void handleUnlockEvent(){
        System.out.println("unlocks!");
        bomb.activateBomb();
    }

    public static void sendNewGame(Player player, Game game) {
        BluetoothServices.sendMessage(player,new Message(Message.MessageType.newGame,game));
    }

    public synchronized static void receiveNewGame(Message message) {
        //TODO: Should set game from NewActivity (that changes to MainActivity
        Game game = (Game)message.getPayload();
        newGameActivity.setGame(game);
        Iterator<Player> it = game.getPlayers().iterator();
        while(it.hasNext()) {
            Player player = it.next();
            if(!player.equals(BluetoothServices.getMyselfPlayer())) {
                BluetoothServices.sendMessage(player, new Message(Message.MessageType.receivedNewGame,""));
            }
        }
    }


}
