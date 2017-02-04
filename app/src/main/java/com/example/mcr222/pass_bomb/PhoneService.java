/*
 * ©Copyright, 2016 Marc Cayuela Rafols
 * All Rights Reserved.
 */

package com.example.mcr222.pass_bomb;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.NotificationCompat;

/**
 * This class manages all the communication between the phone's actions (locking, notifications...)
 * and the app.
 *
 * Created by Marc Cayuela Rafols on 27/06/16.
 */
public class PhoneService {

    //whether phone is locked
    private static boolean isLocked;
    private static MainActivity mainActivity;
    private static final int NOTIFICATION_ID = 222;

    //
    private static NotificationManager mNotificationManager;
    // Create a BroadcastReceiver for ACTION_FOUND
    // We can register actions to the phone and when the action happens the phone will
    // call the broadcast receiver registered
    public static class UnlockReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When phone is unlocked
            if (Intent.ACTION_USER_PRESENT.equals(action)) {
                isLocked = false;
                phoneUnlocksEvent();
            }
            //when phone is locked
            else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                isLocked = true;
            }
            System.out.println("isLocked: " + Boolean.toString(isLocked));
        }
    };

    /**
     * Starts the service that controls the user actions performed on the phone
     * @param activity main game activity
     */
    public static void startPhoneService(MainActivity activity) {
        isLocked = false;
        //the filters are the actions from the phone that we want to keep informed of
        IntentFilter filter = new IntentFilter(Intent.ACTION_USER_PRESENT);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        //must register a receiver in order to do things when filter actions are registered
        activity.mainRegisterReceiver(new UnlockReceiver(), filter); // TODO: Don't forget to unregister during onDestroy
        PhoneService.mainActivity = activity;

        mNotificationManager =
                (NotificationManager) mainActivity.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /**
     * Wheter phone is currently locked
     * @return
     */
    public static boolean isPhoneLocked() {
        return isLocked;
    }

    /**
     * This processes the unlock event.
     */
    public static void phoneUnlocksEvent() {
        MessageProcessor.handleUnlockEvent();
    }

    /**
     * Removes the current notification.
     */
    public static void removeNotification() {
        mNotificationManager.cancel(PhoneService.NOTIFICATION_ID);
    }

    /**
     * Sends a notification to the user. Notifications are the messages to show on the phone (they
     * appear in the notification center when phone is locked).
     * @param text text of the notification. If null "You have received a bomb!" is the default text.
     */
    public static void sendNotification(String text){
        if(text==null) {
            text = "You have received a bomb!";
        }

        removeNotification();

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(mainActivity)
                        .setSmallIcon(R.drawable.bomb)
                        .setContentTitle("Pass the bomb")
                        .setContentText(text)
                        .setPriority(Notification.PRIORITY_MAX);

        // mId allows you to update the notification later on.
        mNotificationManager.notify(PhoneService.NOTIFICATION_ID, mBuilder.build());
    }
}
