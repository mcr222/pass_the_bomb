package com.example.mcr222.pass_bomb;

import android.bluetooth.BluetoothDevice;

import java.io.Serializable;

/**
 * Created by mcr222 on 29/06/16.
 */
public class Player implements Serializable{
    private String id;
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
