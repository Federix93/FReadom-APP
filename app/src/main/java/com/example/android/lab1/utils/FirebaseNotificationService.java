package com.example.android.lab1.utils;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.support.v4.app.TaskStackBuilder;

import com.example.android.lab1.R;
import com.example.android.lab1.ui.ReviewActivity;
import com.example.android.lab1.ui.chat.ChatActivity;
import com.example.android.lab1.ui.chat.CurrentOpenChat;
import com.example.android.lab1.ui.homepage.HomePageActivity;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

public class FirebaseNotificationService extends com.google.firebase.messaging.FirebaseMessagingService {

    private static final String TAG = "GNIPPO";
    public static final String MESSAGE_REPLY_KEY = "message_reply_key";
    public static final String FREADOM_GROUP = "freadom_app_group";
    public static final int SUMMARY_KEY = 478514;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        switch (remoteMessage.getData().get("type")) {
            case Utilities.NEW_MESSAGE_CHANNEL_ID:
                String currentOpenChat = CurrentOpenChat.getOpenChatID();
                if (!(currentOpenChat != null && currentOpenChat.equals(remoteMessage.getData().get("chat")))) {
                    sendNewMessageNotification(remoteMessage.getData());
                }
                break;

            case Utilities.BOOK_REQUEST_CHANNEL_ID:
                sendNewBookRequestNotification(remoteMessage.getData());
                break;

            case Utilities.NEW_LOAN_KEY:
                sendNewLoanNotification(remoteMessage.getData());
                break;

            case Utilities.FINISHED_LOAN_KEY:
                sendFinishedLoanNotification(remoteMessage.getData());
                break;

            default:
                break;
        }

    }

    private void sendNewMessageNotification(Map<String, String> data) {

        if(!NotificationUtilities.notificationExist(data.get("chat")))
            NotificationUtilities.addNotification(data.get("chat"));

        Intent clickIntent = new Intent(this, ChatActivity.class);
        clickIntent.putExtra("ChatID", data.get("chat"));
        clickIntent.putExtra("Username", data.get("sender"));
        clickIntent.putExtra("ImageURL", data.get("senderPic"));
        clickIntent.putExtra("BookID", data.get("book"));
        clickIntent.putExtra("SenderUID", data.get("senderUID"));
        clickIntent.putExtra("FromNotification", true);

        PendingIntent clickPendingIntent;
        if (!LifecycleHandler.isApplicationInForeground()) {
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addNextIntentWithParentStack(clickIntent);
            clickPendingIntent = stackBuilder.getPendingIntent(data.get("chat").hashCode(), PendingIntent.FLAG_ONE_SHOT);
        } else {
            clickPendingIntent = PendingIntent.getActivity(this, data.get("chat").hashCode(), clickIntent,
                    PendingIntent.FLAG_ONE_SHOT);
        }

        PendingIntent replyPendingIntent = PendingIntent.getBroadcast(
                getApplicationContext(),
                (data.get("chat")+data.get("body")).hashCode(),
                new Intent(getApplicationContext(), DirectReplyReceiver.class)
                        .putExtra("notificationID", data.get("chat")),
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        RemoteInput remoteInput = new RemoteInput.Builder(MESSAGE_REPLY_KEY)
                .setLabel(String.format(getResources().getString(R.string.answer_to), data.get("sender")))
                .build();

        NotificationCompat.Action action =
                new NotificationCompat.Action.Builder(R.drawable.notification_icon,
                        getResources().getString(R.string.reply), replyPendingIntent)
                        .addRemoteInput(remoteInput)
                        .build();

        Bitmap userImage = null;

        try {
            userImage = BitmapFactory.decodeStream(new URL(data.get("senderPic")).openConnection().getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        boolean isText = data.get("isText").equals("true");

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        PendingIntent cancelIntent = PendingIntent.getBroadcast(
                getApplicationContext(),
                (data.get("chat")+data.get("sender")).hashCode(),
                new Intent(getApplicationContext(), DirectReplyReceiver.class).putExtra("notificationID", data.get("chat")),
                PendingIntent.FLAG_UPDATE_CURRENT
        );


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, Utilities.NEW_MESSAGE_CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon)
                .setColor(getResources().getColor(R.color.colorSecondaryAccent))
                .setLargeIcon(userImage)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(clickPendingIntent)
                .addAction(action)
                .setDeleteIntent(cancelIntent)
                .setGroup(FREADOM_GROUP);


        if (isText) {
            notificationBuilder
                    .setContentTitle(String.format(getResources().getString(R.string.new_message_notification_title), data.get("sender")))
                    .setContentText(data.get("body"))
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(data.get("body")));
        } else {
            Bitmap sentPhoto = null;

            try {
                sentPhoto = BitmapFactory.decodeStream(new URL(data.get("body")).openConnection().getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            notificationBuilder
                    .setContentTitle(String.format(getResources().getString(R.string.new_message_notification_title_photo), data.get("sender")))
                    .setStyle(new NotificationCompat.BigPictureStyle()
                            .bigPicture(sentPhoto));
        }


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(data.get("chat").hashCode(), notificationBuilder.build());
        if(NotificationUtilities.notificationsPending())
            notificationSummary();

    }

    private void sendNewBookRequestNotification(Map<String, String> data) {

        if(!NotificationUtilities.notificationExist(data.get("chat")))
            NotificationUtilities.addNotification(data.get("chat"));

        Intent clickIntent = new Intent(this, ChatActivity.class);
        clickIntent.putExtra("ChatID", data.get("chat"));
        clickIntent.putExtra("Username", data.get("sender"));
        clickIntent.putExtra("ImageURL", data.get("senderPic"));
        clickIntent.putExtra("BookID", data.get("book"));
        clickIntent.putExtra("SenderUID", data.get("senderUID"));
        clickIntent.putExtra("FromNotification", true);

        PendingIntent clickPendingIntent;

        if (!LifecycleHandler.isApplicationInForeground()) {
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addNextIntentWithParentStack(clickIntent);
            clickPendingIntent = stackBuilder.getPendingIntent(data.get("chat").hashCode(), PendingIntent.FLAG_ONE_SHOT);
        } else {
            clickPendingIntent = PendingIntent.getActivity(this, data.get("chat").hashCode(), clickIntent,
                    PendingIntent.FLAG_ONE_SHOT);
        }

        Bitmap bookThumbnail = null;

        try {
            bookThumbnail = BitmapFactory.decodeStream(new URL(data.get("bookThumbnail")).openConnection().getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        PendingIntent cancelIntent = PendingIntent.getBroadcast(
                getApplicationContext(),
                (data.get("chat")+data.get("sender")).hashCode(),
                new Intent(getApplicationContext(), DirectReplyReceiver.class).putExtra("notificationID", data.get("chat")),
                PendingIntent.FLAG_UPDATE_CURRENT
        );


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, Utilities.NEW_MESSAGE_CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon)
                .setColor(getResources().getColor(R.color.colorSecondaryAccent))
                .setLargeIcon(bookThumbnail)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(clickPendingIntent)
                .setDeleteIntent(cancelIntent)
                .setGroup(FREADOM_GROUP);

        String contentText = String.format(getResources().getString(R.string.new_request_notification_content), data.get("sender"), data.get("bookTitle"));

        notificationBuilder
                .setContentTitle(getResources().getString(R.string.new_request_notification_title))
                .setContentText(contentText)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(contentText));


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(data.get("chat").hashCode(), notificationBuilder.build());
        if(NotificationUtilities.notificationsPending())
            notificationSummary();
    }

    private void sendNewLoanNotification(Map<String, String> data) {

        if(!NotificationUtilities.notificationExist(data.get("book")))
            NotificationUtilities.addNotification(data.get("book"));

        Intent clickIntent = new Intent(this, HomePageActivity.class);
        clickIntent.putExtra("LoanStart", true);
        clickIntent.putExtra("bookLent", data.get("book"));

        PendingIntent clickPendingIntent;

        if (!LifecycleHandler.isApplicationInForeground()) {
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addNextIntentWithParentStack(clickIntent);
            clickPendingIntent = stackBuilder.getPendingIntent(data.get("book").hashCode(), PendingIntent.FLAG_ONE_SHOT);
        } else {
            clickPendingIntent = PendingIntent.getActivity(this, data.get("book").hashCode(), clickIntent,
                    PendingIntent.FLAG_ONE_SHOT);
        }

        Bitmap bookThumbnail = null;

        try {
            bookThumbnail = BitmapFactory.decodeStream(new URL(data.get("bookThumbnail")).openConnection().getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        PendingIntent cancelIntent = PendingIntent.getBroadcast(
                getApplicationContext(),
                (data.get("book")+data.get("sender")).hashCode(),
                new Intent(getApplicationContext(), DirectReplyReceiver.class).putExtra("notificationID", data.get("book")),
                PendingIntent.FLAG_UPDATE_CURRENT
        );


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, Utilities.LOAN_CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon)
                .setColor(getResources().getColor(R.color.colorSecondaryAccent))
                .setLargeIcon(bookThumbnail)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(clickPendingIntent)
                .setDeleteIntent(cancelIntent)
                .setGroup(FREADOM_GROUP);

        String contentText = String.format(getResources().getString(R.string.new_loan_notification_content), data.get("sender"), data.get("bookTitle"));

        notificationBuilder
                .setContentTitle(getResources().getString(R.string.new_loan_notification_title))
                .setContentText(contentText)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(contentText));


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(data.get("book").hashCode(), notificationBuilder.build());
        if(NotificationUtilities.notificationsPending())
            notificationSummary();
    }

    private void sendFinishedLoanNotification(Map<String, String> data) {

        if(!NotificationUtilities.notificationExist(data.get("book")))
            NotificationUtilities.addNotification(data.get("book"));

        Intent clickIntent = new Intent(this, ReviewActivity.class);
        clickIntent.putExtra(ReviewActivity.Keys.BOOK_ID, data.get("book"));
        clickIntent.putExtra(ReviewActivity.Keys.REVIEWER_ID, data.get("borrowerUID"));
        clickIntent.putExtra(ReviewActivity.Keys.REVIEWED_ID, data.get("lenderUID"));
        clickIntent.putExtra(ReviewActivity.Keys.NOTIFICATION_CMD, true);

        PendingIntent clickPendingIntent;

        if (!LifecycleHandler.isApplicationInForeground()) {
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addNextIntentWithParentStack(clickIntent);
            clickPendingIntent = stackBuilder.getPendingIntent(data.get("book").hashCode(), PendingIntent.FLAG_ONE_SHOT);
        } else {
            clickPendingIntent = PendingIntent.getActivity(this, data.get("book").hashCode(), clickIntent,
                    PendingIntent.FLAG_ONE_SHOT);
        }

        Bitmap bookThumbnail = null;

        try {
            bookThumbnail = BitmapFactory.decodeStream(new URL(data.get("bookThumbnail")).openConnection().getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        PendingIntent cancelIntent = PendingIntent.getBroadcast(
                getApplicationContext(),
                (data.get("book")+data.get("sender")).hashCode(),
                new Intent(getApplicationContext(), DirectReplyReceiver.class).putExtra("notificationID", data.get("book")),
                PendingIntent.FLAG_UPDATE_CURRENT
        );


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, Utilities.LOAN_CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon)
                .setColor(getResources().getColor(R.color.colorSecondaryAccent))
                .setLargeIcon(bookThumbnail)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(clickPendingIntent)
                .setDeleteIntent(cancelIntent)
                .setGroup(FREADOM_GROUP);

        String contentText = String.format(getResources().getString(R.string.finished_loan_notification_content), data.get("sender"), data.get("bookTitle"));

        notificationBuilder
                .setContentTitle(getResources().getString(R.string.finished_loan_notification_title))
                .setContentText(contentText)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(contentText));


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(data.get("book").hashCode(), notificationBuilder.build());
        if(NotificationUtilities.notificationsPending())
            notificationSummary();
    }

    private void notificationSummary()
    {
        NotificationCompat.Builder summary = new NotificationCompat.Builder(this, Utilities.NEW_MESSAGE_CHANNEL_ID);

        Intent clickIntent = new Intent(this, HomePageActivity.class);
        clickIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent clickPendingIntent;
        if (!LifecycleHandler.isApplicationInForeground()) {
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addNextIntentWithParentStack(clickIntent);
            clickPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_ONE_SHOT);
        } else {
            clickPendingIntent = PendingIntent.getActivity(this, 0, clickIntent,
                    PendingIntent.FLAG_ONE_SHOT);
        }

        PendingIntent cancelIntent = PendingIntent.getBroadcast(
                getApplicationContext(),
                (SUMMARY_KEY+"x").hashCode(),
                new Intent(getApplicationContext(), DirectReplyReceiver.class).putExtra("summaryID", SUMMARY_KEY),
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        summary.setContentText(getString(R.string.may_have_new_messages))
                .setSmallIcon(R.drawable.notification_icon)
                .setContentIntent(clickPendingIntent)
                .setGroup(FREADOM_GROUP)
                .setContentIntent(cancelIntent)
                .setGroupSummary(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(SUMMARY_KEY, summary.build());

    }

    public void scheduleNotification(String bookID, long endDate)
    {
/*
        PendingIntent cancelIntent = PendingIntent.getBroadcast(
                getApplicationContext(),
                (bookID).hashCode()+(int)endDate,
                new Intent(getApplicationContext(), DirectReplyReceiver.class).putExtra("notificationID", bookID),
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, Utilities.LOAN_CHANNEL_ID)
                .setSmallIcon(R.drawable.share_icon)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSound(defaultSoundUri)
                .setDeleteIntent(cancelIntent)
                .setGroup(FREADOM_GROUP)
                .setContentTitle(getResources().getString(R.string.new_request_notification_title))
                .setContentText(contentText)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(contentText));

        Notification reminderNotification = notificationBuilder.build();

        Intent reminderIntent = new Intent(this, DirectReplyReceiver.class);
        reminderIntent.putExtra("reminderID", bookID.hashCode());
        reminderIntent.putExtra("reminderNotification", reminderNotification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, bookID.hashCode(), reminderIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + 10000;
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);






        notificationBuilder
                .setContentTitle(getResources().getString(R.string.new_request_notification_title))
                .setContentText(contentText)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(contentText));


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(data.get("chat").hashCode(), notificationBuilder.build());
        if(NotificationUtilities.notificationsPending())
            notificationSummary();
*/
    }

}