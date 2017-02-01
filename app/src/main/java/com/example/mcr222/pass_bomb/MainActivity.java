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

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Main activity
 * Defines the main page of the app, where the game develops
 * <p>
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
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

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

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }


    @Override
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
     * @param broadcastReceiver
     * @param filter
     */
    public void mainRegisterReceiver(BroadcastReceiver broadcastReceiver, IntentFilter filter) {
        registerReceiver(broadcastReceiver, filter);
        receivers.add(broadcastReceiver);
    }

    private void unregisterReceivers() {
        Iterator<BroadcastReceiver> it = receivers.iterator();
        while (it.hasNext()) {
            unregisterReceiver(it.next());
        }
        receivers.clear();
    }

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

            //start of the game is delayed to avoid sending the bomb before everybody is initialy set
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
     * Toasts are the messages to show on the phone when screen is locked.
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MainActivity.BLUETOOTH_ON) {
            if (resultCode == RESULT_OK) {
                System.out.println("Bluetooth is on!");
                BluetoothServices.setStarted(true);
            }

        }

    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.mcr222.pass_bomb/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.mcr222.pass_bomb/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}