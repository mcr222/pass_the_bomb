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


public class NewGameActivity extends AppCompatActivity {
    private Button startGameButton;
    private EditText numberPoints;
    private EditText numberBombs;
    private PlayerListWidget playerListWidget;
    private Button searchPlayers;

    public class PlayersAdapter extends ArrayAdapter<Player> {

        public PlayersAdapter(Context context) {
            super(context, 0, new ArrayList<Player>());
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            Player player = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.listitem, parent, false);
            }
            // Lookup view for data population
            TextView playerName = (TextView) convertView.findViewById(R.id.playerName);
            // Populate the data into the template view using the data object
            playerName.setText(player.getId());
            // Return the completed view to render on screen
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

        numberPoints = (EditText) findViewById(R.id.points);
        numberBombs = (EditText) findViewById(R.id.numberBombs);

        startGameButton = (Button) findViewById(R.id.startGame);

        ListView listView = (ListView) findViewById(R.id.playerList);

        playerListWidget = new PlayerListWidget(listView, new PlayersAdapter(this), this);

        searchPlayers = (Button) findViewById(R.id.searchPlayers);

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
        BluetoothServices.waitForInitialConnection();

        //TODO: hide discoverability when done with this Activity
        //TODO: does  not make device discoverable
        BluetoothServices.makeBluetoothDiscoverable();

        MessageProcessor.setNewGameActivity(this);

        //TODO: in this all should be waiting for incoming bluetooth conenctions to receive the game

        startGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*ArrayList<Player> selectedPlayers = playerListWidget.getSelectedPlayers();
                BluetoothServices.setInitialConnection(selectedPlayers.get(0));*/

                ArrayList<Player> selectedPlayers = playerListWidget.getSelectedPlayers();
                System.out.println("Selected players");
                System.out.println(selectedPlayers);
                Player myselfPlayer = BluetoothServices.getMyselfPlayer();
                selectedPlayers.add(0,myselfPlayer);
                System.out.println("Selected players");
                System.out.println(selectedPlayers);


                if(selectedPlayers.size() > 1) {
                    int points = 100;
                    if (numberPoints.getText() != null && !numberPoints.getText().toString().equals("")) {
                        points = Integer.parseInt(numberPoints.getText().toString());
                    }

                    int bombs = 1;
                    if (numberBombs.getText() != null && !numberBombs.getText().toString().equals("")) {
                        bombs = Integer.parseInt(numberBombs.getText().toString());
                    }

                    Game game = new Game(points, selectedPlayers, null);

                    //TODO: check if this returns before a connection is stablished between players (if it does must use callback to continue)
                    BluetoothServices.setAllPlayers(game.getPlayers());

                    HashSet<Integer> playersWithStartingBomb
                            = getRandomSelectedPlayersForBombs(bombs, selectedPlayers.size());

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);

                    for (int i = 0; i < selectedPlayers.size(); ++i) {
                        Game gameCopy = game.getCopy();
                        if (playersWithStartingBomb.contains(new Integer(i))) {
                            gameCopy.setStartsWithBomb(true);
                        } else {
                            gameCopy.setStartsWithBomb(false);
                        }

                        if(selectedPlayers.get(i).equals(myselfPlayer)) {
                            System.out.println("my game");
                            intent.putExtra("Game", gameCopy);
                        }
                        else {
                            System.out.println("sendinggame");
                            System.out.println(gameCopy);
                            MessageProcessor.sendNewGame(selectedPlayers.get(i), gameCopy);
                        }
                    }

                    startActivity(intent);
                }
            }
        });

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //Do nothing
    }

    //TODO: when receives a Game message via bluetooth should do sth like this:
    public void setGame(Game game) {
        System.out.println("Receives game!!");
        System.out.println(game);
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("Game",game);

        startActivity(intent);
    }

    private HashSet<Integer> getRandomSelectedPlayersForBombs(int numberOfBombs, int numberOfPlayers) {
        if(numberOfBombs>=numberOfPlayers){
            numberOfBombs = numberOfPlayers-1;
        }
        System.out.println("Number of bombs: " + Integer.toString(numberOfBombs));
        System.out.println("Number of players: " + Integer.toString(numberOfPlayers));

        HashSet<Integer> playersWithBombs = new HashSet<Integer>();
        Random rdmGenerator = new Random();
        while(playersWithBombs.size() < numberOfBombs) {
            playersWithBombs.add(rdmGenerator.nextInt(numberOfPlayers));
        }
        System.out.println("Players with bomb: " + playersWithBombs.toString());
        return playersWithBombs;
    }


    public void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

}