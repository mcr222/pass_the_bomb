package com.example.mcr222.pass_bomb;

import android.app.Activity;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicBoolean;


public class Bomb {
    private AtomicBoolean hasBomb = new AtomicBoolean(false);
    private ImageView bombImage;
    private PointsCounter pointsCounter;
    private Activity main;

    public Bomb(ImageView bombImage, Activity main) {
        this.main = main;
        this.bombImage = bombImage;
    }

    public void initializeCounter(int startPoints, TextView pointsLeftView) {
        System.out.println("points " + startPoints);
        System.out.println("pointcounter " + pointsLeftView);
        pointsCounter = new PointsCounter(pointsLeftView, main);
        pointsCounter.setCounter(startPoints);
        //TODO:change tiime to 60000
        pointsCounter.setTimerInterval(5000);
    }

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

    public void activateBomb(){
        if(hasBomb()) {
            pointsCounter.startCounter();
        }
    }

    public void deactivateBomb() {
        pointsCounter.stopCounter();
    }

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

    public boolean hasBomb() {
        return hasBomb.get();
    }
}
