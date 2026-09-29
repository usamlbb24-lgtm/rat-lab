package com.ratlab;
import android.content.Context;
import android.hardware.camera2.CameraManager;
public class TorchControl {
    private static boolean on = false;
    public static String toggle(Context ctx) {
        try {
            CameraManager cm = (CameraManager) ctx.getSystemService(Context.CAMERA_SERVICE);
            String id = cm.getCameraIdList()[0];
            on = !on;
            cm.setTorchMode(id, on);
            return on ? "Torch ON" : "Torch OFF";
        } catch (Exception e) { return "Error: " + e.getMessage(); }
    }
}
