package com.example.android.lab1.utils;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.example.android.lab1.R;
import com.example.android.lab1.ui.chat.ChatActivity;
import com.example.android.lab1.ui.chat.CurrentOpenChat;
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

            default:
                break;
        }

    }

    private void sendNewMessageNotification(Map<String, String> data) {

        if(!NotificationUtilities.notificationExist(data.get("chat")))
            NotificationUtilities.addNotification(data.get("chat"));

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("ChatID", data.get("chat"));
        intent.putExtra("Username", data.get("sender"));
        intent.putExtra("ImageURL", data.get("senderPic"));
        intent.putExtra("BookID", data.get("book"));
        intent.putExtra("SenderUID", data.get("senderUID"));
        intent.putExtra("FromNotification", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent cancelIntent = PendingIntent.getBroadcast(
                getApplicationContext(),
                1,
                new Intent(getApplicationContext(), DirectReplyReceiver.class).putExtra("ChatID", data.get("chat")),
                PendingIntent.FLAG_UPDATE_CURRENT
        );


        PendingIntent pendingIntent;
        if (!LifecycleHandler.isApplicationInForeground()) {
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addNextIntentWithParentStack(intent);
            pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_ONE_SHOT);
        } else {
            pendingIntent = PendingIntent.getActivity(this, 0, intent,
                    PendingIntent.FLAG_ONE_SHOT);
        }

        PendingIntent replyPendingIntent = PendingIntent.getBroadcast(
                getApplicationContext(),
                0,
                new Intent(getApplicationContext(), DirectReplyReceiver.class)
                        .putExtra("ChatID", data.get("chat")),
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        RemoteInput remoteInput = new RemoteInput.Builder(MESSAGE_REPLY_KEY)
                .setLabel(String.format(getResources().getString(R.string.answer_to), data.get("sender")))
                .build();

        NotificationCompat.Action action =
                new NotificationCompat.Action.Builder(R.drawable.ic_world_24dp,
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


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, Utilities.NEW_MESSAGE_CHANNEL_ID)
                .setSmallIcon(R.drawable.share_icon)
                .setLargeIcon(userImage)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
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

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("ChatID", data.get("chat"));
        intent.putExtra("Username", data.get("sender"));
        intent.putExtra("ImageURL", data.get("senderPic"));
        intent.putExtra("BookID", data.get("book"));
        intent.putExtra("SenderUID", data.get("senderUID"));
        intent.putExtra("FromNotification", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent cancelIntent = PendingIntent.getBroadcast(
                getApplicationContext(),
                1,
                new Intent(getApplicationContext(), DirectReplyReceiver.class).putExtra("ChatID", data.get("chat")),
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        PendingIntent pendingIntent;

        if (!LifecycleHandler.isApplicationInForeground()) {
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addNextIntentWithParentStack(intent);
            pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_ONE_SHOT);
        } else {
            pendingIntent = PendingIntent.getActivity(this, 0, intent,
                    PendingIntent.FLAG_ONE_SHOT);
        }

        Bitmap bookThumbnail = null;

        try {
            bookThumbnail = BitmapFactory.decodeStream(new URL(data.get("bookThumbnail")).openConnection().getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, Utilities.NEW_MESSAGE_CHANNEL_ID)
                .setSmallIcon(R.drawable.share_icon)
                .setLargeIcon(bookThumbnail)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
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

    private void notificationSummary()
    {
        NotificationCompat.Builder summary = new NotificationCompat.Builder(this, Utilities.NEW_MESSAGE_CHANNEL_ID);

        summary.setContentTitle("SUMMARY")
                //content text to support devices running API level < 24 (DA TESTARE)
                .setContentText("Two new messages")
                .setSmallIcon(R.drawable.ic_world_24dp)
//                .setStyle(new NotificationCompat.InboxStyle()
//                        .addLine(data.get("sender")+" "+data.get("body"))
//                        .setBigContentTitle("Nuovi messaggi")
//                        .setSummaryText("Nuove notifiche"))
                .setGroup(FREADOM_GROUP)
                .setGroupSummary(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(SUMMARY_KEY, summary.build());

    }

}