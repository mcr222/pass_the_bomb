/*
 * ©Copyright, 2016 Marc Cayuela Rafols
 * All Rights Reserved.
 */

package com.example.mcr222.pass_bomb;

import android.app.Activity;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Counter of the point that the user has at every time. Also manages the displaying of the points
 *
 * Created by Marc Cayuela Rafols on 27/06/16.
 */
public class PointsCounter {
    //points at the start
    private int totalPoints;
    //current number of points
    private int counter;
    //timer that decreases points (only started if bomb is active)
    private Timer timer;
    //whether this counter was started already (to avoid starting twice)
    private boolean counterStarted=false;
    //time it takes to discount one point;
    private long intervalTime;
    //object from the phone's view that displays the points
    private TextView pointsLeftView;

    private Activity main;

    //number of points the user had the last time he unlocked the phone. This counter is used
    //to stop the counter if user leaves phone locked for a certain period of time (it actually limits
    // the number of points the user can loose if leaves the phone unlocked).
    private int lastPointsUnlocked = 0;

    public PointsCounter(TextView pointsLeftView, Activity main){
        this.pointsLeftView = pointsLeftView;
        this.main = main;
        timer = new Timer();
    }

    /**
     * Sets the time that it takes to discount points
     * @param intervalTime time in ms to discount one point
     */
    public void setTimerInterval(long intervalTime){
        this.intervalTime = intervalTime;
    }

    /**
     * Starts the counter countdown if countdown isn't already on. This is called everytime the phone
     * is unlocked, so the lastPointsUnlocked variable is updated.
     */
    public void startCounter(){
        //keeps the counter value from the last time phone was unlocked
        lastPointsUnlocked = counter;
        if(!counterStarted) {
            timer = new Timer();
            //this timer will execute the code every intervalTime time
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    --counter;
                    updateCounterView(null);
                    //if counter reaches 0 stop counter
                    if (counter <= 0) {
                        updateCounterView("0");
                        MainActivity.setGameOn(false);
                        timer.cancel();
                    //if phone was unlocked for a while stop timer too
                    } else if((lastPointsUnlocked - counter)>totalPoints/5 && !PhoneService.isPhoneLocked()) {
                        System.out.println("Lost more than 1/5, bomb is deactivated");
                        timer.cancel();
                    }
                }
            }, intervalTime, intervalTime);
            counterStarted = true;
        }
    }

    /**
     * Stops the countdown
     */
    public void stopCounter() {
        timer.cancel();
        counterStarted = false;
    }

    /**
     * Sets counter initial value
     * @param counter initial point number
     */
    public void setCounter(int counter) {
        this.counter = counter;
        this.totalPoints = counter;
        updateCounterView(null);
    }

    /**
     * Updates the top counter viewe of the user in the main screen with the text or the number of
     * points user has left
     * @param text if text is null then the counter view is updated with the current number of points
     */
    private void updateCounterView(String text) {
        final String txt = text;
        main.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(txt!=null) {
                    pointsLeftView.setText(txt);
                } else {
                    pointsLeftView.setText(Integer.toString(counter));
                }
            }
        });
    }
}
