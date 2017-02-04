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
 * Created by mcr222 on 1/08/16.
 */
public class InstructionsActivity extends Activity {

    private static final String INSTRUCTION = "instruction";
    private static final String INSTRUCTION_MAIN = "instruction_main";
    private static final String INSTRUCTION_NEW = "instruction_new";

    //TODO:remove this (this is to avoid showing the instructions at the beggining every time when testing)
    private static boolean mainAlready = false;
    private static boolean newAlready = false;

    private int[][] instructionsMain = new int[][]{
            {R.id.newGameInstructionCircle, R.id.newGameInstructionText1, R.id.newGameInstructionText2},
            {R.id.stopGameInstructionImage, R.id.stopGameInstructionText},
            {R.id.bombInstructionImage, R.id.bombInstructionText},
            {R.id.gameUniqueIDInstructionImage, R.id.gameUniqueIDInstructionCircle, R.id.gameUniqueIDInstructionText},
            {R.id.counterInstructionCircle, R.id.counterInstructionText1, R.id.counterInstructionText2},
            {R.id.unloadBombCircle, R.id.unloadBombText}
    };

    private int[][] instructionsNew = new int[][]{
            {R.id.initInstructionText},
            {R.id.configurePointsInstructionCircle, R.id.configurePointsInstructionText},
            {R.id.configureBombsInstructionCircle, R.id.configureBombsInstructionText},
            {R.id.searchPlayersInstructionCircle, R.id.searchPlayersInstructionText},
            {R.id.selectPlayersInstructionText},
            {R.id.startGameInstructionCircle, R.id.startGameInstructionText1, R.id.startGameInstructionText2, R.id.startGameInstructionText3}
    };

    private int[][] instructionsToUse;

    private int currentPage = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.instructions_activity);

        final String mode = (String) getIntent().getSerializableExtra(INSTRUCTION);

        if (mode.equals(INSTRUCTION_MAIN)) {
            instructionsToUse = instructionsMain;
        } else {
            instructionsToUse = instructionsNew;
        }

        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.instructions);
        changePage(currentPage - 1, currentPage, mode);
        currentPage += 1;

        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePage(currentPage - 1, currentPage, mode);
                currentPage += 1;
            }
        });

    }

    private void changePage(int previousPage, int pageToShow, String mode) {
        if (previousPage > -1) {
            showHidePage(previousPage, View.INVISIBLE);
        }

        if (pageToShow < instructionsToUse.length) {
            showHidePage(pageToShow, View.VISIBLE);
        } else {
            Intent intent;
            if (mode.equals(INSTRUCTION_MAIN)) {
                intent = new Intent(getApplicationContext(), MainActivity.class);
            } else {
                intent = new Intent(getApplicationContext(), NewGameActivity.class);
            }

            startActivity(intent);
        }
    }

    private void showHidePage(int page, int mode) {
        int[] instr = instructionsToUse[page];
        for (int i = 0; i < instr.length; ++i) {
            findViewById(instr[i]).setVisibility(mode);
        }
    }

    public static void showInstructions(Activity activity) {
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
        //TODO: change de if
        if (!ranBefore) {
            //if((mode.equals(INSTRUCTION_MAIN) && !mainAlready) || (mode.equals(INSTRUCTION_NEW) && !newAlready) ) {
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
            activity.startActivity(intent);

        }
    }

}
