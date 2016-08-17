package com.example.mcr222.pass_bomb;

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by mcr222 on 1/07/16.
 */
public class PlayerListWidget {
    private ListView playerListWidget;
    private NewGameActivity.PlayersAdapter listAdapter;
    private ArrayList<Player> selectedPlayers;
    private final NewGameActivity activity;



    public PlayerListWidget(ListView playerListWidget, NewGameActivity.PlayersAdapter listAdapter, final NewGameActivity activity) {
        this.activity = activity;
        this.listAdapter = listAdapter;
        this.playerListWidget = playerListWidget;
        playerListWidget.setAdapter(listAdapter);
        playerListWidget.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        selectedPlayers = new ArrayList<Player>();

        this.playerListWidget.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                activity.hideKeyboard();
                Player player = (Player) adapterView.getItemAtPosition(i);
                if(selectedPlayers.contains(player)) {
                    selectedPlayers.remove(player);
                    view.setBackgroundColor(Color.WHITE);
                }
                else {
                    selectedPlayers.add(player);
                    view.setBackgroundColor(Color.BLUE);
                }

            }
        });
    }

    public ArrayList<Player> getSelectedPlayers() {
        return selectedPlayers;
    }

    public void setAllPlayers(ArrayList<Player> allPlayers) {
        selectedPlayers.clear();
        listAdapter.clear();
        listAdapter.addAll(allPlayers);
        listAdapter.notifyDataSetChanged();

        System.out.println(playerListWidget.getChildCount());

        for (int i=0;i<playerListWidget.getChildCount();++i) {
            System.out.println(playerListWidget.getChildAt(i));
            playerListWidget.getChildAt(i).setBackgroundColor(Color.WHITE);
        }

        playerListWidget.requestFocus();
        activity.hideKeyboard();

    }

}
