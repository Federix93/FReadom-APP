package com.example.android.lab1.utils;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.example.android.lab1.R;
import com.example.android.lab1.ui.chat.ChatActivity;

import java.util.HashSet;

import static com.example.android.lab1.utils.FirebaseNotificationService.FREADOM_GROUP;

public abstract class NotificationUtilities {

    public final static String BOOK_REQUEST_CHANNEL_ID = "requestChannel";
    public final static String NEW_MESSAGE_CHANNEL_ID = "messageChannel";
    public final static String LOAN_CHANNEL_ID = "loanChannel";
    public final static String NEW_LOAN_KEY = "newLoan";
    public final static String FINISHED_LOAN_KEY = "finishedLoan";
    public static final String LOAN_REQUEST_KEY = "newLoanRequest";
    public static final String LOAN_END_KEY = "newLoanEnd";

    private static int notificationCount = 0;
    private static HashSet<String> activeNotifications = new HashSet<>();

    public static void addNotification(String notificationID)
    {
        activeNotifications.add(notificationID);
        notificationCount++;
    }

    public static void removeNotification(String notificationID, Context context, boolean deleteFromStack)
    {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        activeNotifications.remove(notificationID);
        notificationCount--;
        if(notificationCount < 0)
            notificationCount = 0;
        if(deleteFromStack)
        {
            notificationManager.cancel(notificationID.hashCode());
        }
        if(!notificationsPending())
        {
            notificationManager.cancel(FirebaseNotificationService.SUMMARY_KEY);
        }
    }

    public static boolean notificationExist(String notificationID)
    {
        return activeNotifications.contains(notificationID);
    }

    public static boolean notificationsPending()
    {
        return (notificationCount > 0);
    }

    public static void deleteAll() {
        notificationCount = 0;
        activeNotifications.clear();
    }

    public static void scheduleNotification(Context context, long endDate, String bookID, String chatID, String senderUID, String otherUserName, String otherUserPic) {
        PendingIntent cancelIntent = PendingIntent.getBroadcast(
                context,
                (chatID+bookID+senderUID).hashCode(),
                new Intent(context, DirectReplyReceiver.class).putExtra("notificationID", chatID+bookID+senderUID),
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        String contentText = context.getResources().getString(R.string.reminder_notification_content);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, LOAN_CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon)
                .setColor(context.getResources().getColor(R.color.colorSecondaryAccent))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setDeleteIntent(cancelIntent)
                .setGroup(FREADOM_GROUP)
                .setContentTitle(context.getResources().getString(R.string.reminder_notification_title))
                .setContentText(contentText)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(contentText));

        int notificationId = (chatID+bookID+senderUID).hashCode();

        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra("ChatID", chatID);
        intent.putExtra("Username", otherUserName);
        intent.putExtra("ImageURL", otherUserPic);
        intent.putExtra("BookID", bookID);
        intent.putExtra("SenderUID", senderUID);
        intent.putExtra("ReminderNotification", true);

        PendingIntent reminderAlarm = PendingIntent.getActivity(context, notificationId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(reminderAlarm);

        Notification notification = builder.build();

        Intent notificationIntent = new Intent(context, DirectReplyReceiver.class);
        notificationIntent.putExtra("alarmID", notificationId);
        notificationIntent.putExtra("alarmNotification", notification);
        notificationIntent.putExtra("ChatID", chatID);
        notificationIntent.putExtra("BookID", bookID);
        notificationIntent.putExtra("SenderUID", senderUID);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notificationId, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, endDate, pendingIntent);
    }
}
