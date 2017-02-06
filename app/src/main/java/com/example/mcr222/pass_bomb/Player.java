/*
 * ©Copyright, 2016 Marc Cayuela Rafols
 * All Rights Reserved.
 */

package com.example.mcr222.pass_bomb;

import android.bluetooth.BluetoothDevice;

import java.io.Serializable;

/**
 * Represents a player in the game
 *
 * Created by Marc Cayuela Rafols on 29/06/16.
 */
public class Player implements Serializable{
    //id is the name of the player
    private String id;
    //MAC is the physical address of the players' phone device
    private String MAC;

    public Player(String id, String MAC) {
        this.MAC= MAC;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getMAC() {
        return MAC;
    }

    public Player getCopy() {
        return new Player(getId(),getMAC());
    }

    @Override
    /**
     * Compares to players to check if they are equal
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Player player = (Player) o;

        if (!id.equals(player.id)) return false;
        return MAC.equals(player.MAC);

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + MAC.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "id: " + id + ", MAC: " + MAC;
    }
}
