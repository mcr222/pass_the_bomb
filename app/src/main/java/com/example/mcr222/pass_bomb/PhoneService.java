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
 * Created by mcr222 on 27/06/16.
 */
public class PhoneService {

    private static boolean isLocked;
    private static MainActivity mainActivity;
    private static final int NOTIFICATION_ID = 222;

    private static NotificationManager mNotificationManager;
    // Create a BroadcastReceiver for ACTION_FOUND
    public static class UnlockReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When phone is unlocked
            if (Intent.ACTION_USER_PRESENT.equals(action)) {
                isLocked = false;
                phoneUnlocksEvent();
            }
            else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                isLocked = true;
            }
            System.out.println("isLocked: " + Boolean.toString(isLocked));
        }
    };

    public static void startPhoneService(MainActivity activity) {
        isLocked = false;
        IntentFilter filter = new IntentFilter(Intent.ACTION_USER_PRESENT);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        activity.mainRegisterReceiver(new UnlockReceiver(), filter); // TODO: Don't forget to unregister during onDestroy
        PhoneService.mainActivity = activity;

        mNotificationManager =
                (NotificationManager) mainActivity.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static boolean isPhoneLocked() {
        return isLocked;
    }

    public static void phoneUnlocksEvent() {
        MessageProcessor.handleUnlockEvent();
    }

    public static void removeNotification() {
        mNotificationManager.cancel(PhoneService.NOTIFICATION_ID);
    }

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
        //TODO: consider a head up notification (just add vibrate to make it head's up notification)

        // mId allows you to update the notification later on.
        mNotificationManager.notify(PhoneService.NOTIFICATION_ID, mBuilder.build());
    }
}
