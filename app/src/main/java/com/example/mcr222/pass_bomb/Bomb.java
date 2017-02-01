/*
 * ©Copyright, 2016 Marc Cayuela Rafols
 * All Rights Reserved.
 */

package com.example.mcr222.pass_bomb;

import android.app.Activity;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Bomb represents the bomb in the game. All players have this object from the beginning, but it does
 * not mean that they all start with the bomb. This object only encloses the bomb logic, and takes care
 * of showing the bomb, and starting the point counter.
 *
 * Created by Marc Cayuela Rafols on 27/06/16.
 */
public class Bomb {
    //boolean that is true if player has bomb
    private AtomicBoolean hasBomb = new AtomicBoolean(false);
    //UI object that represents the area where the image of the bomb appears in the phone
    private ImageView bombImage;
    //counter of points
    private PointsCounter pointsCounter;
    //the main screen activity, used to modify what's shown on the main screen
    private Activity main;

    public Bomb(ImageView bombImage, Activity main) {
        this.main = main;
        this.bombImage = bombImage;
    }

    /**
     * Initializes point counter
     * @param startPoints points with which the counter starts
     * @param pointsLeftView object that represents the visualization of the counter in the phone's screen
     */
    public void initializeCounter(int startPoints, TextView pointsLeftView) {
        System.out.println("points " + startPoints);
        System.out.println("pointcounter " + pointsLeftView);
        pointsCounter = new PointsCounter(pointsLeftView, main);
        pointsCounter.setCounter(startPoints);
        //TODO:change time to 60000 (one minute) after testing app
        pointsCounter.setTimerInterval(5000);
    }

    /**
     * Shows the image of a bomb in the phone
     * @throws HasBombException
     */
    public void showBomb() throws HasBombException {
        if(!hasBomb()) {
            main.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    bombImage.setImageResource(R.drawable.bomb);
                    hasBomb.set(true);
                    PhoneService.sendNotification(null);
                }
            });

        }
        else {
            throw new HasBombException();
        }
    }

    /**
     * Activates bomb, if user has bomb then counter is started so it counts down
     */
    public void activateBomb(){
        if(hasBomb()) {
            pointsCounter.startCounter();
        }
    }

    public void deactivateBomb() {
        pointsCounter.stopCounter();
    }

    /**
     * Hides the image of the bomb in the phone
     */
    public void hideBomb(){
        main.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bombImage.setImageDrawable(null);
                deactivateBomb();
                hasBomb.set(false);
                PhoneService.removeNotification();
            }
        });
    }

    /**
     * Whether this user has bomb or not
     * @return true if has bomb
     */
    public boolean hasBomb() {
        return hasBomb.get();
    }
}
