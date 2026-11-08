package com.example.subscriptiontracker;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.subscriptiontracker.worker.ReminderWorker;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.materialswitch.MaterialSwitch;

public class SettingsActivity extends AppCompatActivity {

    private static final String[] CURRENCY_VALUES = {
            PrefsManager.CURRENCY_IDR, PrefsManager.CURRENCY_USD};
    private static final int[] REMIND_VALUES = {1, 3, 7};
    private static final String[] SORT_VALUES = {
            PrefsManager.SORT_DATE, PrefsManager.SORT_NAME, PrefsManager.SORT_PRICE};

    private PrefsManager prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = new PrefsManager(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_settings);
        }

        Spinner spCurrency = findViewById(R.id.spCurrency);
        Spinner spRemind = findViewById(R.id.spRemind);
        Spinner spSort = findViewById(R.id.spSort);
        MaterialSwitch swDark = findViewById(R.id.swDark);
        MaterialSwitch swNotif = findViewById(R.id.swNotif);

        spCurrency.setAdapter(ArrayAdapter.createFromResource(this,
                R.array.currency_labels, android.R.layout.simple_spinner_dropdown_item));
        spRemind.setAdapter(ArrayAdapter.createFromResource(this,
                R.array.remind_labels, android.R.layout.simple_spinner_dropdown_item));
        spSort.setAdapter(ArrayAdapter.createFromResource(this,
                R.array.sort_labels, android.R.layout.simple_spinner_dropdown_item));

        // Tampilkan nilai yang tersimpan
        spCurrency.setSelection(indexOf(CURRENCY_VALUES, prefs.getCurrency()));
        spRemind.setSelection(indexOf(REMIND_VALUES, prefs.getRemindDays()));
        spSort.setSelection(indexOf(SORT_VALUES, prefs.getSortOrder()));
        swDark.setChecked(prefs.isDarkMode());
        swNotif.setChecked(prefs.isNotifEnabled());

        // Setiap perubahan langsung disimpan lewat PrefsManager (apply())
        spCurrency.setOnItemSelectedListener(new SimpleSelect(pos ->
                prefs.setCurrency(CURRENCY_VALUES[pos])));
        spRemind.setOnItemSelectedListener(new SimpleSelect(pos ->
                prefs.setRemindDays(REMIND_VALUES[pos])));
        spSort.setOnItemSelectedListener(new SimpleSelect(pos ->
                prefs.setSortOrder(SORT_VALUES[pos])));

        swNotif.setOnCheckedChangeListener((b, checked) -> {
            prefs.setNotifEnabled(checked);
            if (checked) ReminderWorker.schedule(this);
            else ReminderWorker.cancel(this);
        });

        swDark.setOnCheckedChangeListener((b, checked) -> {
            prefs.setDarkMode(checked);
            prefs.applyTheme();
        });
    }

    private static int indexOf(String[] arr, String value) {
        for (int i = 0; i < arr.length; i++) if (arr[i].equals(value)) return i;
        return 0;
    }

    private static int indexOf(int[] arr, int value) {
        for (int i = 0; i < arr.length; i++) if (arr[i] == value) return i;
        return 0;
    }

    private interface OnPick { void pick(int position); }

    private static class SimpleSelect implements AdapterView.OnItemSelectedListener {
        private final OnPick callback;
        SimpleSelect(OnPick callback) { this.callback = callback; }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            callback.pick(position);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) { }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
