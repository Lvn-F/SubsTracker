package com.example.subscriptiontracker.worker;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.app.PendingIntent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.subscriptiontracker.MainActivity;
import com.example.subscriptiontracker.PrefsManager;
import com.example.subscriptiontracker.db.DatabaseHelper;
import com.example.subscriptiontracker.model.Subscription;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ReminderWorker extends Worker {
    private static final String WORK_NAME = "subscription_reminder";
    private static final String CHANNEL_ID = "subscription_reminder_channel";

    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    public static void schedule(Context context) {
        PeriodicWorkRequest request =
                new PeriodicWorkRequest.Builder(ReminderWorker.class, 1, TimeUnit.DAYS).build();
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request);
    }

    public static void cancel(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        PrefsManager prefs = new PrefsManager(context);
        if (!prefs.isNotifEnabled()) return Result.success();

        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return Result.success();
        }

        createChannel(context);

        DatabaseHelper db = new DatabaseHelper(context);
        List<Subscription> all = db.getAll(PrefsManager.SORT_DATE);
        int remind = prefs.getRemindDays();
        int notifId = 1000;

        for (Subscription s : all) {
            if (!s.isActive()) continue;
            int days = s.daysUntilDue();
            if (days < 0 || days > remind) continue;

            String when = days == 0 ? "hari ini" : days + " hari lagi";
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pi = PendingIntent.getActivity(context, 0, intent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder b = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("Tagihan " + s.getName() + " jatuh tempo " + when)
                    .setContentText("Siapkan " + prefs.formatMoney(s.getPrice()))
                    .setContentIntent(pi)
                    .setAutoCancel(true);

            NotificationManagerCompat.from(context).notify(notifId + (int) s.getId(), b.build());
        }
        return Result.success();
    }

    private void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Pengingat Langganan", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager nm = context.getSystemService(NotificationManager.class);
            nm.createNotificationChannel(channel);
        }
    }
}
