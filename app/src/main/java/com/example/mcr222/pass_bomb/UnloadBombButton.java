/*
 * ©Copyright, 2016 Marc Cayuela Rafols
 * All Rights Reserved.
 */

package com.example.mcr222.pass_bomb;

import android.view.View;
import android.widget.Button;

/**
 * Button that performs the action of unloading bomb
 *
 *  Created by Marc Cayuela Rafols on 8/07/16.
 */
public class UnloadBombButton {

    public UnloadBombButton(Button buttonUnloadBomb) {
        buttonUnloadBomb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickAction();
            }
        });
    }

    /**
     * Action performed when button is pressed
     */
    private void onClickAction(){
        //first check if game has started already
        if(MainActivity.isGameOn()) {
            //if there are pending messages of unload bomb then send pending messages.
            //this is used in order to avoid infinite clicks on the button that would saturate the
            //sending messages thread
            if (!BluetoothServices.hasPendingUnloadBomb()) {
                MessageProcessor.sendUnloadBomb();
            } else {
                BluetoothServices.sendPendingMessages();
            }
        }
    }


}
