package com.ratlab;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

public class DeviceInfo {
    public static String get(Context ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append("Model: ").append(Build.MANUFACTURER).append(" ").append(Build.MODEL).append("\n");
        sb.append("Android: ").append(Build.VERSION.RELEASE).append(" (SDK ").append(Build.VERSION.SDK_INT).append(")\n");
        sb.append("Brand: ").append(Build.BRAND).append("\n");
        sb.append("Device: ").append(Build.DEVICE).append("\n");
        sb.append("Hardware: ").append(Build.HARDWARE).append("\n");
        sb.append("CPU: ").append(Build.SUPPORTED_ABIS[0]).append("\n");

        try {
            StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
            long free = stat.getAvailableBytes();
            long total = stat.getTotalBytes();
            sb.append("Storage: ").append(human(free)).append(" / ").append(human(total)).append("\n");
        } catch (Exception e) {}

        try {
            ActivityManager am = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
            am.getMemoryInfo(mi);
            sb.append("RAM: ").append(human(mi.availMem)).append(" free / ").append(human(mi.totalMem)).append("\n");
        } catch (Exception e) {}

        return sb.toString();
    }

    private static String human(long bytes) {
        long kb = bytes / 1024;
        long mb = kb / 1024;
        long gb = mb / 1024;
        if (gb > 0) return gb + " GB";
        if (mb > 0) return mb + " MB";
        return kb + " KB";
    }
}
