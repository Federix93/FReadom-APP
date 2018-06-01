package com.example.android.lab1.utils;

import android.content.Context;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.util.HashSet;

public abstract class NotificationUtilities {

    private static String TAG = "GNIPPO";

    private static int notificationCount = 0;
    private static HashSet<String> activeNotifications = new HashSet<>();

    public static void addNotification(String notificationID)
    {
        activeNotifications.add(notificationID);
        notificationCount++;
        Log.d(TAG, "addNotification");
        Log.d(TAG, "COUNT= "+notificationCount);
    }

    public static void removeNotification(String notificationID, Context context)
    {
        activeNotifications.remove(notificationID);
        notificationCount--;
        Log.d(TAG, "COUNT= "+notificationCount);
        if(!notificationsPending())
        {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.cancel(FirebaseNotificationService.SUMMARY_KEY);
        }
    }

    public static boolean notificationExist(String notificationID)
    {
        return activeNotifications.contains(notificationID);
    }

    public static boolean notificationsPending()
    {
        return (notificationCount > 1);
    }

}
