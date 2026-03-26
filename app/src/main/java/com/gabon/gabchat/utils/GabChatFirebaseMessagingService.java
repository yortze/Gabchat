package com.gabon.gabchat.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import com.gabon.gabchat.GabChatApp;
import com.gabon.gabchat.R;
import com.gabon.gabchat.activities.ChatActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class GabChatFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getData().isEmpty()) return;

        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        String senderId = remoteMessage.getData().get("senderId");
        String type = remoteMessage.getData().getOrDefault("type", "message");

        if ("call".equals(type)) {
            showCallNotification(title, body, senderId);
        } else {
            showMessageNotification(title, body, senderId);
        }
    }

    private void showMessageNotification(String title, String body, String senderId) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("receiverId", senderId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
                GabChatApp.CHANNEL_ID_MESSAGES)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void showCallNotification(String title, String body, String callerId) {
        Intent intent = new Intent(this, com.gabon.gabchat.activities.CallActivity.class);
        intent.putExtra("receiverId", callerId);
        intent.putExtra("isOutgoing", false);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
                GabChatApp.CHANNEL_ID_CALLS)
                .setSmallIcon(R.drawable.ic_call)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setFullScreenIntent(pendingIntent, true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_CALL);

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.notify(1001, builder.build());
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (uid != null) {
            FirebaseDatabase.getInstance().getReference("users")
                    .child(uid).child("fcmToken").setValue(token);
        }
    }
}
