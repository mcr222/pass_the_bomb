package com.example.mcr222.pass_bomb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by mcr222 on 27/06/16.
 */
public class Message implements Serializable {

    public enum MessageType {
        unloadBomb, ifBombUnloaded, newGame, receivedNewGame
    }

    private MessageType type;
    private Object payload;
    private int retries;

    public Message(MessageType type, Object payload) {
        this.type = type;
        this.payload = payload;
        this.retries = 0;
    }

    public void incrementRetries() {
        ++ retries;
    }

    public int getRetries() {
        return retries;
    }

    public static Message parseMessage(int bytes, byte[] messageBytes) {
        ByteArrayInputStream b = new ByteArrayInputStream(messageBytes,0,bytes);
        try {
            ObjectInputStream o = new ObjectInputStream(b);
            Message mess = (Message) o.readObject();
            return mess;
        } catch(IOException exc){
            return null;
        } catch (ClassNotFoundException exc) {
            return null;
        }

    }

    public byte[] serializeMessage(){
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        try {
            ObjectOutputStream o = new ObjectOutputStream(b);
            o.writeObject(this);
        } catch(IOException exc) {

        }
        return b.toByteArray();
    }

    public MessageType getType() {
        return type;
    }

    public Object getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return type.toString() +" "+ payload.toString();
    }

}
