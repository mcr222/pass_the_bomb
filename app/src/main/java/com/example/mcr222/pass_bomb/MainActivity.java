/*
 * ©Copyright, 2016 Marc Cayuela Rafols
 * All Rights Reserved.
 */

package com.example.mcr222.pass_bomb;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.content.Intent;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Main activity
 * Defines the main page of the app, where the game develops
 *
 * Created by Marc Cayuela Rafols on 27/06/16.
 */
public class MainActivity extends AppCompatActivity {
    private UnloadBombButton unloadBombButton;
    //button to change to NewGameActivity and start a new game
    private Button newGameButton;
    //shows the number of points left
    private static TextView pointsLeftView;
    //shows the unique identifier of the game
    private static TextView gameUniqueID;
    public static final int BLUETOOTH_ON = 0;
    private static boolean isGameOn = false;

    //TODO:make this 1 minute, now it is 5 seconds to faster test the code
    //this delays the start of the game so that bomb cannot be passed immediately after beginning the game
    private long startGameTime = 5000;
    private Timer startGameTimer;


    private static ArrayList<BroadcastReceiver> receivers = new ArrayList<BroadcastReceiver>();

    public static final String SERVICE_NAME = "le8tY9a1sc3TTq21Wbg";
    public static final UUID uuid = new UUID(5566644, 3322555);

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        InstructionsActivity.showInstructions(this);

        //TODO: add onFinishInflate() (wait until content view is ready)

        ImageView bombImage = (ImageView) findViewById(R.id.bombImage);

        MessageProcessor.setBomb(new Bomb(bombImage, this));

        BluetoothServices.setMainActivity(this);
        BluetoothServices.startBluetoothServices();

        PhoneService.startPhoneService(this);

        //TODO: unload bomb at the beggining duplicates the bomb it is possible that it is because when it unloads the bomb
        //TODO: it does not remove the pending send bomb messages
        unloadBombButton = new UnloadBombButton((Button) findViewById(R.id.unloadBomb));
        newGameButton = (Button) findViewById(R.id.newGame);
        pointsLeftView = (TextView) findViewById(R.id.pointsLeft);
        gameUniqueID = (TextView) findViewById(R.id.gameUniqueID);
        setNewGameButton();


        setGame((Game) getIntent().getSerializableExtra("Game"));

    }


    @Override
    /**
     * this is necessary to avoid problems when phone is rotated
     */
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //Do nothing
    }

    /*@Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceivers();
    }*/

    /**
     * Kill the current game
     */
    public void stopGame() {
        Bomb bomb = MessageProcessor.getBomb();
        bomb.initializeCounter(0, pointsLeftView);
        bomb.hideBomb();
        setUniqueID("");
        setNewGameButton();
        isGameOn = false;
        startGameTimer.cancel();

    }

    /**
     * Registers a broadcast receiver to listen to different phone actions done outside the app (like
     * locking the phone or shutting down bluetooth).
     *
     * @param broadcastReceiver
     * @param filter
     */
    public void mainRegisterReceiver(BroadcastReceiver broadcastReceiver, IntentFilter filter) {
        registerReceiver(broadcastReceiver, filter);
        receivers.add(broadcastReceiver);
    }

    //TODO: this should be used to kill the receivers when game is closed
    /**
     * Unregisters all the broadcast receivers registered to the phone by the app
     */
    private void unregisterReceivers() {
        Iterator<BroadcastReceiver> it = receivers.iterator();
        while (it.hasNext()) {
            unregisterReceiver(it.next());
        }
        receivers.clear();
    }

    /**
     * Sets the button that goes to the new game creation screen
     */
    private void setNewGameButton() {
        newGameButton.setText("+");
        newGameButton.setBackgroundColor(Color.GRAY);
        newGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), NewGameActivity.class);
                startActivity(i);
                System.out.println("New game screen starting!");
            }
        });
    }

    /**
     * Changes the new game button by a stop button in order to stop the game
     */
    private void setNewGameButtonAsStop() {
        newGameButton.setText(" ");
        newGameButton.setBackgroundColor(Color.RED);
        newGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopGame();
            }
        });
    }

    /**
     * Sets a new game
     *
     * @param game contains all information regarding the game (players, bomb, game UID...)
     */
    public void setGame(Game game) {
        if (game != null) {
            Bomb bomb = MessageProcessor.getBomb();
            bomb.initializeCounter(game.getStartPoints(), pointsLeftView);
            BluetoothServices.setAllPlayers(game.getPlayers());
            //set inactive bomb if player starts with bomb
            if (game.startsWithBomb()) {
                try {
                    bomb.showBomb();
                    //bomb.activateBomb();
                } catch (HasBombException e) {
                    //should never happen
                }
            }

            setUniqueID(game.getUniqueString());

            setNewGameButtonAsStop();

            //start of the game is delayed to avoid sending the bomb before everybody is initially set
            startGameTimer = new Timer();
            startGameTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    isGameOn = true;
                    BluetoothServices.killInitialConnection();
                }
            }, startGameTime);

            makeToast("New Game Starts!!");

        }
    }

    /**
     * Whether the game is already started or not
     *
     * @return true if game is on
     */
    public static boolean isGameOn() {
        return isGameOn;
    }

    /**
     * Set if game is on or off
     *
     * @param isGameOn whether game is on
     */
    public static void setGameOn(boolean isGameOn) {
        MainActivity.isGameOn = isGameOn;
    }

    /**
     * Toast are small messages that appear over the app screen
     *
     * @param text message to show
     */
    private void makeToast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();

    }

    /**
     * Sets the unique ID of the game
     *
     * @param uniqueID
     */
    private void setUniqueID(final String uniqueID) {
        //When setting values of the UI, it must be run in the UI thread or it won't work
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                gameUniqueID.setText(uniqueID);
            }
        });
    }

    @Override
    /**
     * Gets the results returned when an activity is started for a result (here it is only used
     * to know when bluetooth is turned on)
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MainActivity.BLUETOOTH_ON) {
            if (resultCode == RESULT_OK) {
                System.out.println("Bluetooth is on!");
                BluetoothServices.setStarted(true);
            }

        }

    }

}