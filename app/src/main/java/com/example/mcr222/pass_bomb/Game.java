package com.example.mcr222.pass_bomb;

import android.widget.ImageView;

import com.example.mcr222.pass_bomb.BluetoothServices;
import com.example.mcr222.pass_bomb.Bomb;
import com.example.mcr222.pass_bomb.MessageProcessor;
import com.example.mcr222.pass_bomb.PhoneService;
import com.example.mcr222.pass_bomb.Player;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by mcr222 on 27/06/16.
 */
public class Game implements Serializable {
    //Includes mePlayer
    private ArrayList<Player> players;
    private Integer startPoints;
    private Boolean startsWithBomb;
    private String uniqueString;

    public Game(Integer startPoints, ArrayList<Player> players, String uniqueString){
        this.startPoints = startPoints;
        this.players = players;
        this.uniqueString = uniqueString;
        if(this.uniqueString == null) {
            this.uniqueString = UUID.randomUUID().toString().substring(0, 6);
        }
    }

    public Game getCopy() {
        ArrayList<Player> copyPlayers = new ArrayList<Player>();
        for(int i =0;i<players.size();++i) {
            copyPlayers.add(players.get(i).getCopy());
        }
        Game game = new Game(getStartPoints(),copyPlayers,getUniqueString());
        game.setStartsWithBomb(startsWithBomb());
        return game;
    }

    public Integer getStartPoints() {
        return startPoints;
    }

    public String getUniqueString() {
        return uniqueString;
    }

    public Boolean startsWithBomb() {
        return startsWithBomb;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public void setStartsWithBomb(Boolean startsWithBomb) {
        this.startsWithBomb = startsWithBomb;
    }

    @Override
    public String toString() {
        String out="startsWithBomb: " + startsWithBomb + ", startPoints: " + startPoints +", numberPlayers: " + players.size() + ", players: ";
        for(int i=0;i<players.size();++i) {
            out += players.get(i).getId()+", ";
        }
        return out;
    }
}
