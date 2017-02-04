/*
 * ©Copyright, 2016 Marc Cayuela Rafols
 * All Rights Reserved.
 */

package com.example.mcr222.pass_bomb;

/**
 * Pending message to be send. It is a message that failed to be sent. It contains the message that
 * had to be sent and the player to whom it must be sent.
 *
 * Created by Marc Cayuela Rafols on 19/07/16.
 */
public class PendingMessage {
        private Player playerToSend;
        private Message messageToSend;

        public PendingMessage(Player playerToSend, Message messageToSend) {
            this.playerToSend = playerToSend;
            this.messageToSend = messageToSend;
            System.out.println("New pending message");
            System.out.println(messageToSend);
        }

        public Message getMessageToSend() {
            System.out.println(messageToSend);
            return messageToSend;
        }

        public Player getPlayerToSend() {
            return playerToSend;
        }

}
