package com.example.subscriptiontracker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.subscriptiontracker.adapter.SubscriptionAdapter;
import com.example.subscriptiontracker.db.DatabaseHelper;
import com.example.subscriptiontracker.model.Subscription;
import com.example.subscriptiontracker.worker.ReminderWorker;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity implements SubscriptionAdapter.Listener {

    private PrefsManager prefs;
    private DatabaseHelper db;
    private SubscriptionAdapter adapter;

    private TextView tvTotalMonth, tvTotalYear, tvDueSoon, tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = new PrefsManager(this);
        prefs.applyTheme(); // tema dibaca dari SharedPreferences sebelum layar dibuat
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvTotalMonth = findViewById(R.id.tvTotalMonth);
        tvTotalYear = findViewById(R.id.tvTotalYear);
        tvDueSoon = findViewById(R.id.tvDueSoon);
        tvEmpty = findViewById(R.id.tvEmpty);

        RecyclerView rv = findViewById(R.id.recyclerView);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SubscriptionAdapter(prefs, this);
        rv.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> startActivity(new Intent(this, AddEditActivity.class)));

        requestNotificationPermission();
    }

    /** Pengaturan dibaca ulang di sini agar perubahan langsung terlihat tanpa restart. */
    @Override
    protected void onResume() {
        super.onResume();
        prefs.applyTheme();
        if (prefs.isNotifEnabled()) {
            ReminderWorker.schedule(this);
        } else {
            ReminderWorker.cancel(this);
        }
        loadData();
    }

    private void loadData() {
        List<Subscription> list = db.getAll(prefs.getSortOrder());
        adapter.setItems(list);
        tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);

        double month = 0, year = 0;
        int dueSoon = 0;
        int remind = prefs.getRemindDays();
        for (Subscription s : list) {
            if (!s.isActive()) continue;
            month += s.getMonthlyCost();
            year += s.getYearlyCost();
            int d = s.daysUntilDue();
            if (d >= 0 && d <= remind) dueSoon++;
        }
        tvTotalMonth.setText(prefs.formatMoney(month));
        tvTotalYear.setText(prefs.formatMoney(year));
        tvDueSoon.setText(dueSoon + " langganan jatuh tempo dalam " + remind + " hari");
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }
    }

    @Override
    public void onItemClick(Subscription s) {
        Intent i = new Intent(this, AddEditActivity.class);
        i.putExtra(AddEditActivity.EXTRA_ID, s.getId());
        startActivity(i);
    }

    @Override
    public void onItemLongClick(Subscription s) {
        String[] options = {"Tandai sudah dibayar", "Hapus"};
        new AlertDialog.Builder(this)
                .setTitle(s.getName())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        db.markPaid(s.getId());
                        loadData();
                    } else {
                        new AlertDialog.Builder(this)
                                .setMessage("Hapus langganan " + s.getName() + "?")
                                .setPositiveButton("Hapus", (d, w) -> {
                                    db.delete(s.getId());
                                    loadData();
                                })
                                .setNegativeButton("Batal", null)
                                .show();
                    }
                })
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
