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
            //TODO:when player has 0 points do not allow bomb passing
            //TODO: put timer before this button becomes available at the start of the game
            if (!BluetoothServices.hasPendingUnloadBomb()) {
                MessageProcessor.sendUnloadBomb();
            } else {
                BluetoothServices.sendPendingMessages();
            }
        }
    }


}
