package com.example.soundfriends.services;

import android.app.Notification;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.soundfriends.R;

// NotificationManager.java
public class NotificationManager {
    private final Context context;
    private final NotificationManagerCompat notificationManager;

    public NotificationManager(Context context) {
        this.context = context;
        this.notificationManager = NotificationManagerCompat.from(context);
    }

    public Notification buildNotification(MediaSessionCompat mediaSession) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "channelId")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Song Title")
                .setContentText("Artist Name")
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_large_icon))
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1, 2)) // Indices of actions in compact view
                .addAction(new NotificationCompat.Action(
                        R.drawable.ic_previous, "Previous", null))
                .addAction(new NotificationCompat.Action(
                        R.drawable.ic_play, "Play", null))
                .addAction(new NotificationCompat.Action(
                        R.drawable.ic_next, "Next", null));

        return builder.build();
    }
}
