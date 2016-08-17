package com.example.mcr222.pass_bomb;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
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


public class MainActivity extends AppCompatActivity {
    private UnloadBombButton unloadBombButton;
    private Button newGameButton;
    private static TextView pointsLeftView;
    private static TextView gameUniqueID;
    public static final int BLUETOOTH_ON = 0;
    private static boolean isGameOn = false;
    //TODO:make this 1 minute
    private long startGameTime = 5000;
    private Timer startGameTimer;

    private static ArrayList<BroadcastReceiver> receivers = new ArrayList<BroadcastReceiver>();

    public static final String SERVICE_NAME = "le8tY9a1sc3TTq21Wbg";
    public static final UUID uuid = new UUID(5566644,3322555);

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //TODO: in some phones when screen is locked then the app reestarts!!

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
        //TODO: important!1 when bomb is effectively unloaded remove pending messages!!!
        unloadBombButton = new UnloadBombButton((Button) findViewById(R.id.unloadBomb));
        newGameButton = (Button) findViewById(R.id.newGame);
        pointsLeftView = (TextView) findViewById(R.id.pointsLeft);
        gameUniqueID = (TextView) findViewById(R.id.gameUniqueID);
        setNewGameButton();


        setGame((Game) getIntent().getSerializableExtra("Game"));

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

    public void stopGame() {
        Bomb bomb = MessageProcessor.getBomb();
        bomb.initializeCounter(0,pointsLeftView);
        bomb.hideBomb();
        setUniqueID("");
        setNewGameButton();
        isGameOn = false;
        startGameTimer.cancel();

    }

    public void mainRegisterReceiver(BroadcastReceiver broadcastReceiver, IntentFilter filter) {
        registerReceiver(broadcastReceiver,filter);
        receivers.add(broadcastReceiver);
    }

    private void unregisterReceivers(){
        Iterator<BroadcastReceiver> it = receivers.iterator();
        while(it.hasNext()) {
            unregisterReceiver(it.next());
        }
        receivers.clear();
    }

    private void setNewGameButton(){
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

    private void setNewGameButtonAsStop(){
        newGameButton.setText(" ");
        newGameButton.setBackgroundColor(Color.RED);
        newGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopGame();
            }
        });
    }

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

            startGameTimer = new Timer();
            startGameTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    isGameOn = true;
                    BluetoothServices.killInitialConnection();
                }
            },startGameTime);

            makeToast("New Game Starts!!");

        }
    }

    public static boolean isGameOn() {
        return isGameOn;
    }

    public static void setGameOn(boolean isGameOn) {
        MainActivity.isGameOn = isGameOn;
    }

    private void makeToast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();

    }

    private void setUniqueID(final String uniqueID) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                gameUniqueID.setText(uniqueID);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == MainActivity.BLUETOOTH_ON){
            if(resultCode == RESULT_OK) {
                System.out.println("Bluetooth is on!");
                BluetoothServices.setStarted(true);
            }

        }

    }

}