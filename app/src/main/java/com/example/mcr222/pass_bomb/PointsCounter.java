package com.example.mcr222.pass_bomb;

import android.app.Activity;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by mcr222 on 27/06/16.
 */
public class PointsCounter {

    private int totalPoints;
    private int counter;
    private Timer timer;
    private boolean counterStarted=false;
    private long intervalTime;
    private TextView pointsLeftView;
    private Activity main;
    private int lastPointsUnlocked = 0;

    public PointsCounter(TextView pointsLeftView, Activity main){
        this.pointsLeftView = pointsLeftView;
        this.main = main;
        timer = new Timer();
    }

    public void setTimerInterval(long intervalTime){
        this.intervalTime = intervalTime;
    }

    public void startCounter(){
        lastPointsUnlocked = counter;
        if(!counterStarted) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    //TODO: limit the number of points that can be lost at once (if the phone was not unlocked in the process)
                    --counter;
                    updateCounterView(null);
                    if (counter <= 0) {
                        updateCounterView("0");
                        MainActivity.setGameOn(false);
                        timer.cancel();
                        //TODO: timer stops discounting points if phone is locked just before it looses 1/5 of the points
                    } else if((lastPointsUnlocked - counter)>totalPoints/5 && !PhoneService.isPhoneLocked()) {
                        System.out.println("Lost more than 1/5, bomb is deactivated");
                        //TODO: add notification when bomb is active and when not (to see if player is loosing points)
                        timer.cancel();
                    }
                }
            }, intervalTime, intervalTime);
            counterStarted = true;
        }
    }

    public void stopCounter() {
        timer.cancel();
        counterStarted = false;
    }

    public void setCounter(int counter) {
        this.counter = counter;
        this.totalPoints = counter;
        updateCounterView(null);
    }

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
