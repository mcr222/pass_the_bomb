/*
 * ©Copyright, 2016 Marc Cayuela Rafols
 * All Rights Reserved.
 */

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
 * This is the widget that shows the list of possible players when creating a new game (it contains
 * the potential players and displays them in the phone new game screen).
 *
 * Created by Marc Cayuela Rafols on 1/07/16.
 */
public class PlayerListWidget {
    //android object that supports the view of the list
    private ListView playerListWidget;
    //adapter that connects the list of objects to visualize with the visualization itself. In other
    //words, it takes the objects to be displayed and transforms them in a displayable View item that
    //will go inside the ListView.
    private NewGameActivity.PlayersAdapter listAdapter;
    //list of objects to display
    private ArrayList<Player> selectedPlayers;
    private final NewGameActivity activity;


    /**
     *
     * @param playerListWidget
     * @param listAdapter
     * @param activity
     */
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

    /**
     * Get the list of currently selected players from the list
     *
     * @return
     */
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
