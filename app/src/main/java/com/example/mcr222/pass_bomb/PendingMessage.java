package com.example.mcr222.pass_bomb;

/**
 * Created by mcr222 on 19/07/16.
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
