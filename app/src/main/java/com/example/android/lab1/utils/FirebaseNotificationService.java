package com.example.android.lab1.utils;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.example.android.lab1.R;
import com.example.android.lab1.ui.chat.ChatActivity;
import com.example.android.lab1.utils.Utilities;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

public class FirebaseNotificationService extends com.google.firebase.messaging.FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(TAG, "onMessageReceived: Parto");
        switch (remoteMessage.getData().get("type")) {
            case Utilities.NEW_MESSAGE_CHANNEL_ID:
                sendNewMessageNotification(remoteMessage.getData());
                break;

            case Utilities.BOOK_REQUEST_CHANNEL_ID:
                //sendNewBookRequestNotification(remoteMessage);
                break;

            default:
                Log.d(TAG, "onMessageReceived: Fottuto default");
                break;
        }

    }

    private void sendNewMessageNotification(Map<String, String> data) {
        Log.d(TAG, "sendNewMessageNotification: ci entro?");
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("ChatID", data.get("chat"));
        intent.putExtra("Username", data.get("sender"));
        intent.putExtra("ImageURL", data.get("senderPic"));
        intent.putExtra("BookID", data.get("book"));
        intent.putExtra("SenderUID", data.get("senderUID"));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

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
                .setContentIntent(pendingIntent);

        if(isText)
        {
            notificationBuilder
                    .setContentTitle(String.format(getResources().getString(R.string.new_message_notification_title), data.get("sender")))
                    .setContentText(data.get("body"))
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(data.get("body")));
        }
        else
        {
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

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }


}