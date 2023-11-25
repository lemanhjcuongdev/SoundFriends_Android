package com.example.soundfriends.services;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.Nullable;

// MusicPlayerService.java
public class MusicPlayerService extends Service {
    private MediaPlayer mediaPlayer;
    private MediaSessionCompat mediaSession;
    private NotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        initMediaSession();
        notificationManager = new NotificationManager(this);
    }

    private void initMediaSession() {
        mediaSession = new MediaSessionCompat(this, "MusicPlayerService");
        mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setCallback(new MediaSessionCallback());
    }

    private class MediaSessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            // Implement play logic
            mediaPlayer.start();
            showNotification();
        }

        @Override
        public void onPause() {
            // Implement pause logic
            mediaPlayer.pause();
            showNotification();
        }

        @Override
        public void onStop() {
            // Implement stop logic
            mediaPlayer.stop();
            showNotification();
        }

        @Override
        public void onSkipToNext() {
            // Implement skip to next logic
            showNotification();
        }

        @Override
        public void onSkipToPrevious() {
            // Implement skip to previous logic
            showNotification();
        }
    }

    private void showNotification() {
        Notification notification = notificationManager.buildNotification(mediaSession);
        startForeground(1, notification);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mediaSession.setActive(true);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
        mediaSession.release();
    }
}
