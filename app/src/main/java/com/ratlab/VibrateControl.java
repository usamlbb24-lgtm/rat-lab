package com.ratlab;
import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
public class VibrateControl {
    public static String vibrate(Context ctx, String args) {
        try {
            long ms = args.isEmpty() ? 1000 : Long.parseLong(args);
            Vibrator v = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                v.vibrate(ms);
            }
            return "Vibrate " + ms + "ms";
        } catch (Exception e) { return "Error: " + e.getMessage(); }
    }
}
