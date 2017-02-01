/*
 * ©Copyright, 2016 Marc Cayuela Rafols
 * All Rights Reserved.
 */

package com.example.mcr222.pass_bomb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Represents a message send through bluetooth. It mainly contains a type and a payload
 *
 * Created by mcr222 on 27/06/16.
 */
public class Message implements Serializable {

    //types of messages that are sent between users currently
    public enum MessageType {
        unloadBomb, ifBombUnloaded, newGame, receivedNewGame
    }

    //type of this message
    private MessageType type;
    //payload of the message (can be any kind of object, arrays, integers... depending on the type of message)
    private Object payload;
    //how many times it was retried so far (to avoid retrying infinite times to send a message)
    private int retries;

    /**
     * Constructor
     * @param type type of message
     * @param payload payload of the message
     */
    public Message(MessageType type, Object payload) {
        this.type = type;
        this.payload = payload;
        this.retries = 0;
    }

    /**
     * Increment by one the number of times this message has been tried to send
     */
    public void incrementRetries() {
        ++ retries;
    }

    /**
     * Get number of retries
     * @return
     */
    public int getRetries() {
        return retries;
    }

    /**
     * Parses the message object from an input stream (that is received via bluetooth)
     * @param bytes number of bytes
     * @param messageBytes bytes of the message
     * @return message deserialized
     */
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

    /**
     * Serializes this message
     * @return an array of bytes representing this message serialized
     */
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
