/*
 * ©Copyright, 2016 Marc Cayuela Rafols
 * All Rights Reserved.
 */

package com.example.mcr222.pass_bomb;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;

/**
 * Screen that overlays over the main screen and the new game screen in order to explain the game
 *
 * Created by Marc Cayuela Rafols on 1/08/16.
 */
public class InstructionsActivity extends Activity {

    private static final String INSTRUCTION = "instruction";
    private static final String INSTRUCTION_MAIN = "instruction_main";
    private static final String INSTRUCTION_NEW = "instruction_new";

    //TODO:remove this (this is to avoid showing the instructions at the beginning every time when testing)
    private static boolean mainAlready = false;
    private static boolean newAlready = false;

    //This list contains all the objects to show for the instructions of the main page.
    //Every element of the list is a list, where each list is a page of the instructions. The elements of
    //the list are the view objects of the instructions.
    private int[][] instructionsMain = new int[][]{
            {R.id.newGameInstructionCircle, R.id.newGameInstructionText1, R.id.newGameInstructionText2},
            {R.id.stopGameInstructionImage, R.id.stopGameInstructionText},
            {R.id.bombInstructionImage, R.id.bombInstructionText},
            {R.id.gameUniqueIDInstructionImage, R.id.gameUniqueIDInstructionCircle, R.id.gameUniqueIDInstructionText},
            {R.id.counterInstructionCircle, R.id.counterInstructionText1, R.id.counterInstructionText2},
            {R.id.unloadBombCircle, R.id.unloadBombText}
    };

    //Same as the other list of lists but this are instructions for the new game page.
    private int[][] instructionsNew = new int[][]{
            {R.id.initInstructionText},
            {R.id.configurePointsInstructionCircle, R.id.configurePointsInstructionText},
            {R.id.configureBombsInstructionCircle, R.id.configureBombsInstructionText},
            {R.id.searchPlayersInstructionCircle, R.id.searchPlayersInstructionText},
            {R.id.selectPlayersInstructionText},
            {R.id.startGameInstructionCircle, R.id.startGameInstructionText1, R.id.startGameInstructionText2, R.id.startGameInstructionText3}
    };

    //what instructions to use depending if on main screen or new game screen
    private int[][] instructionsToUse;

    //current page of the instructions shown (i.e. index of the list of lists).
    private int currentPage = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.instructions_activity);

        //gets which screen is being showed now in order to use the appropriate instructions
        final String mode = (String) getIntent().getSerializableExtra(INSTRUCTION);

        if (mode.equals(INSTRUCTION_MAIN)) {
            instructionsToUse = instructionsMain;
        } else {
            instructionsToUse = instructionsNew;
        }

        //sets to the initial instructions page
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.instructions);
        changePage(currentPage - 1, currentPage, mode);
        currentPage += 1;

        //when the layout is clicked on, then change of page of instructions
        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePage(currentPage - 1, currentPage, mode);
                currentPage += 1;
            }
        });

    }

    /**
     * Changes between the pages of the instructions shown
     *
     * @param previousPage previous page that was shown
     * @param pageToShow page to show
     * @param mode string specifying the screen seen (main or new game).
     */
    private void changePage(int previousPage, int pageToShow, String mode) {
        if (previousPage > -1) {
            //sets all elements of previous page to invisible (hides page)
            showHidePage(previousPage, View.INVISIBLE);
        }

        if (pageToShow < instructionsToUse.length) {
            //shows next page if there is next page
            showHidePage(pageToShow, View.VISIBLE);
        } else {
            //if no next page, then go to the main or new game activity (hides instructions)
            Intent intent;
            if (mode.equals(INSTRUCTION_MAIN)) {
                intent = new Intent(getApplicationContext(), MainActivity.class);
            } else {
                intent = new Intent(getApplicationContext(), NewGameActivity.class);
            }

            startActivity(intent);
        }
    }

    /**
     * Shows or hides all elements of an instruction page
     * @param page index of the page to show/hide
     * @param mode whether to show or hide the page
     */
    private void showHidePage(int page, int mode) {
        int[] instr = instructionsToUse[page];
        for (int i = 0; i < instr.length; ++i) {
            findViewById(instr[i]).setVisibility(mode);
        }
    }

    /**
     * Shows the instructions overlay over the activity specified if they have not been showed before
     * @param activity activity onto which to overlay instructions
     */
    public static void showInstructions(Activity activity) {
        //this is a global variable that is stored with the app memory in the phone forever in order to know
        //whether instructions have been shown before or not, as we do not want to show instructions
        //all the time the app is started
        String preferenceKeyword = "RanBefore_" + activity.getLocalClassName();
        System.out.println(preferenceKeyword);
        SharedPreferences preferences = activity.getPreferences(MODE_PRIVATE);
        boolean ranBefore = preferences.getBoolean(preferenceKeyword, false);

        //TODO: remove this
//        String mode;
//        if(activity.getClass().equals(MainActivity.class)) {
//            mode = INSTRUCTION_MAIN;
//        } else {
//            mode = INSTRUCTION_NEW;
//        }
        System.out.println(mainAlready);
        System.out.println(newAlready);
        //TODO: change de if with the commented if below when ready to deploy app
        if (!ranBefore) {
            //if((mode.equals(INSTRUCTION_MAIN) && !mainAlready) || (mode.equals(INSTRUCTION_NEW) && !newAlready) ) {

            //sets the value of the long term variable to true (meaning that instructions have been
            //shown already for the specific activity).
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(preferenceKeyword, true);
            editor.commit();

            Intent intent = new Intent(activity.getApplicationContext(), InstructionsActivity.class);
            if (activity.getClass().equals(MainActivity.class)) {
                intent.putExtra(INSTRUCTION, INSTRUCTION_MAIN);
                mainAlready = true;
            } else {
                intent.putExtra(INSTRUCTION, INSTRUCTION_NEW);
                newAlready = true;
            }
            //start instructions activity
            activity.startActivity(intent);

        }
    }

}
