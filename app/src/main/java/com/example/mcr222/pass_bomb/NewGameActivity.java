/*
 * ©Copyright, 2016 Marc Cayuela Rafols
 * All Rights Reserved.
 */

package com.example.mcr222.pass_bomb;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

/**
 * New game activity
 * Defines the page where a new game is created
 *
 * Created by Marc Cayuela Rafols on 27/06/16.
 */
public class NewGameActivity extends AppCompatActivity {
    //button to start a new game
    private Button startGameButton;
    //text boxes to input game variables
    private EditText numberPoints;
    private EditText numberBombs;

    //widget that contains all potential players
    private PlayerListWidget playerListWidget;
    //button to search players via bluetooth
    private Button searchPlayers;

    //this adapter converts a list of objects into a list of elements to be viewed in the phone screen
    //(see PlayerListWidget.java)
    public class PlayersAdapter extends ArrayAdapter<Player> {

        public PlayersAdapter(Context context) {
            super(context, 0, new ArrayList<Player>());
        }

        /**
         * This function is called by the list view for every object in the list of objects that want
         * to be showed. For each of this items the function returns a View item that the list can
         * render inside itself.
         *
         * @param position position in the list of objects to display
         * @param convertView list view that contains items to be viewed
         * @param parent view that contains the list view
         * @return
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the object for this position to be displayed
            Player player = getItem(position);
            // Check if an existing view (for the list) is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.listitem, parent, false);
            }
            // Lookup view for data population
            TextView playerName = (TextView) convertView.findViewById(R.id.playerName);
            // Populate the data into the template view using the data object
            playerName.setText(player.getId());
            // Return the completed view for the object that wants to be rendered
            return convertView;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newgame);

        InstructionsActivity.showInstructions(this);

        findViewById(R.id.newGameScreen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
            }
        });

        //get all view objects
        numberPoints = (EditText) findViewById(R.id.points);
        numberBombs = (EditText) findViewById(R.id.numberBombs);

        startGameButton = (Button) findViewById(R.id.startGame);

        ListView listView = (ListView) findViewById(R.id.playerList);

        playerListWidget = new PlayerListWidget(listView, new PlayersAdapter(this), this);

        searchPlayers = (Button) findViewById(R.id.searchPlayers);

        //set action search players
        searchPlayers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothServices.detectNearbyDevices(new Callback() {
                    @Override
                    public void callback(Object object) {
                        playerListWidget.setAllPlayers((ArrayList<Player>) object);
                    }
                });

            }

        });


        //TODO: kill waiting for initial connection after done
        //all devices that go into the new game activity will be listening for incoming connections
        //so that they can receive the game from the creator
        BluetoothServices.waitForInitialConnection();

        //TODO: hide discoverability when done with this Activity
        //TODO: does  not make device discoverable
        BluetoothServices.makeBluetoothDiscoverable();

        MessageProcessor.setNewGameActivity(this);

        //TODO: in this point all should be waiting for incoming bluetooth conenctions to receive the game

        //what happens when new game is created (only one players creates the game and then it sends
        //it to the rest of players)
        startGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*ArrayList<Player> selectedPlayers = playerListWidget.getSelectedPlayers();
                BluetoothServices.setInitialConnection(selectedPlayers.get(0));*/

                //get selected players for the game
                ArrayList<Player> selectedPlayers = playerListWidget.getSelectedPlayers();
                System.out.println("Selected players");
                System.out.println(selectedPlayers);
                Player myselfPlayer = BluetoothServices.getMyselfPlayer();
                selectedPlayers.add(0,myselfPlayer);
                System.out.println("Selected players");
                System.out.println(selectedPlayers);


                if(selectedPlayers.size() > 1) {
                    //get number of points (defaults to 100)
                    int points = 100;
                    if (numberPoints.getText() != null && !numberPoints.getText().toString().equals("")) {
                        points = Integer.parseInt(numberPoints.getText().toString());
                    }
                    //get number of bombs (defaults to 1)
                    int bombs = 1;
                    if (numberBombs.getText() != null && !numberBombs.getText().toString().equals("")) {
                        bombs = Integer.parseInt(numberBombs.getText().toString());
                    }

                    //create a new game object
                    Game game = new Game(points, selectedPlayers, null);

                    //TODO: check if this returns before a connection is established between players (if it does must use callback to continue)
                    BluetoothServices.setAllPlayers(game.getPlayers());

                    //get the indexes of the players that will start with a bomb
                    HashSet<Integer> playersWithStartingBomb
                            = getRandomSelectedPlayersForBombs(bombs, selectedPlayers.size());

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);

                    //send game to all players
                    for (int i = 0; i < selectedPlayers.size(); ++i) {
                        //for each player create a copy of the game and set whether it starts with a bomb
                        Game gameCopy = game.getCopy();
                        if (playersWithStartingBomb.contains(new Integer(i))) {
                            gameCopy.setStartsWithBomb(true);
                        } else {
                            gameCopy.setStartsWithBomb(false);
                        }

                        if(selectedPlayers.get(i).equals(myselfPlayer)) {
                            System.out.println("my game");
                            //saves the information of the game so the main screen can retrieve it
                            intent.putExtra("Game", gameCopy);
                        }
                        else {
                            System.out.println("sendinggame");
                            System.out.println(gameCopy);
                            //sends game to the other players
                            MessageProcessor.sendNewGame(selectedPlayers.get(i), gameCopy);
                        }
                    }

                    //this makes the app return to the main screen
                    startActivity(intent);
                }
            }
        });

    }

    @Override
    /**
     * this is necessary to avoid problems when phone is rotated
     */
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //Do nothing
    }

    /**
     * Sets a new game when the player is not the creator of the game and it received the game setup
     * via bluetooth.
     *
     * @param game game to start
     */
    public void setGame(Game game) {
        System.out.println("Receives game!!");
        System.out.println(game);
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        //save the game so main screen can retrieve information
        intent.putExtra("Game",game);
        //go to main screen
        startActivity(intent);
    }

    /**
     * Randomly determines which players will start with bombs
     *
     * @param numberOfBombs number of bombs to assign to players
     * @param numberOfPlayers number of players in the game
     * @return a set with random indexes of players that will start with a bomb
     */
    private HashSet<Integer> getRandomSelectedPlayersForBombs(int numberOfBombs, int numberOfPlayers) {
        //number of bombs is adjusted if there are more bombs than players
        if(numberOfBombs>=numberOfPlayers){
            numberOfBombs = numberOfPlayers-1;
        }
        System.out.println("Number of bombs: " + Integer.toString(numberOfBombs));
        System.out.println("Number of players: " + Integer.toString(numberOfPlayers));

        HashSet<Integer> playersWithBombs = new HashSet<Integer>();
        Random rdmGenerator = new Random();
        //keep generating indexes until all bombs are assigned
        while(playersWithBombs.size() < numberOfBombs) {
            playersWithBombs.add(rdmGenerator.nextInt(numberOfPlayers));
        }
        System.out.println("Players with bomb: " + playersWithBombs.toString());
        return playersWithBombs;
    }


    /**
     * Hides the phone's keyboard
     */
    public void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

}