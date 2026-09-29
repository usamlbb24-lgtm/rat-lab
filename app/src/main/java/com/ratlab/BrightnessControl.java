package com.ratlab;
import android.content.Context;
import android.provider.Settings;
public class BrightnessControl {
    public static String set(Context ctx, String args) {
        try {
            int lvl = Integer.parseInt(args);
            if (lvl < 0) lvl = 0; if (lvl > 255) lvl = 255;
            Settings.System.putInt(ctx.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, lvl);
            return "Brightness: " + lvl;
        } catch (Exception e) { return "Error: " + e.getMessage(); }
    }
}
