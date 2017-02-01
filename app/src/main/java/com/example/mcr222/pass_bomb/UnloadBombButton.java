/*
 * ©Copyright, 2016 Marc Cayuela Rafols
 * All Rights Reserved.
 */

package com.example.mcr222.pass_bomb;

import android.view.View;
import android.widget.Button;


public class UnloadBombButton {

    public UnloadBombButton(Button buttonUnloadBomb) {
        buttonUnloadBomb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickAction();
            }
        });
    }

    private void onClickAction(){
        if(MainActivity.isGameOn()) {
            if (!BluetoothServices.hasPendingUnloadBomb()) {
                MessageProcessor.sendUnloadBomb();
            } else {
                BluetoothServices.sendPendingMessages();
            }
        }
    }


}
