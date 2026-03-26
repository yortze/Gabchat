package com.gabon.gabchat;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import com.google.firebase.FirebaseApp;

public class GabChatApp extends Application {

    public static final String CHANNEL_ID_MESSAGES = "gabchat_messages";
    public static final String CHANNEL_ID_CALLS = "gabchat_calls";

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);

            NotificationChannel messagesChannel = new NotificationChannel(
                    CHANNEL_ID_MESSAGES,
                    "Messages GabChat",
                    NotificationManager.IMPORTANCE_HIGH
            );
            messagesChannel.setDescription("Notifications de nouveaux messages");
            messagesChannel.enableVibration(true);
            manager.createNotificationChannel(messagesChannel);

            NotificationChannel callsChannel = new NotificationChannel(
                    CHANNEL_ID_CALLS,
                    "Appels GabChat",
                    NotificationManager.IMPORTANCE_MAX
            );
            callsChannel.setDescription("Notifications d'appels entrants");
            manager.createNotificationChannel(callsChannel);
        }
    }
}
