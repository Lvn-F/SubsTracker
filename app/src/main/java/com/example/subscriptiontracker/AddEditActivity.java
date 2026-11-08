package com.example.subscriptiontracker;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.subscriptiontracker.db.DatabaseHelper;
import com.example.subscriptiontracker.model.Subscription;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddEditActivity extends AppCompatActivity {
    public static final String EXTRA_ID = "extra_id";

    private DatabaseHelper db;
    private Subscription subscription;
    private boolean isEdit;

    private EditText etName, etPrice;
    private Spinner spCycle, spCategory;
    private Button btnDate;
    private MaterialSwitch swActive;

    private final Calendar dueCal = Calendar.getInstance();
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);

        db = new DatabaseHelper(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        etName = findViewById(R.id.etName);
        etPrice = findViewById(R.id.etPrice);
        spCycle = findViewById(R.id.spCycle);
        spCategory = findViewById(R.id.spCategory);
        btnDate = findViewById(R.id.btnDate);
        swActive = findViewById(R.id.swActive);
        Button btnSave = findViewById(R.id.btnSave);

        spCycle.setAdapter(ArrayAdapter.createFromResource(this,
                R.array.cycle_labels, android.R.layout.simple_spinner_dropdown_item));
        spCategory.setAdapter(ArrayAdapter.createFromResource(this,
                R.array.category_labels, android.R.layout.simple_spinner_dropdown_item));

        long id = getIntent().getLongExtra(EXTRA_ID, -1);
        isEdit = id != -1;
        if (isEdit) {
            subscription = db.getById(id);
            if (subscription == null) {
                finish();
                return;
            }
            getSupportActionBar().setTitle(R.string.title_edit);
            fillForm();
        } else {
            subscription = new Subscription();
            getSupportActionBar().setTitle(R.string.title_add);
        }

        dueCal.setTimeInMillis(subscription.getNextDueDate());
        updateDateButton();

        btnDate.setOnClickListener(v -> new DatePickerDialog(this,
                (view, year, month, day) -> {
                    dueCal.set(year, month, day);
                    updateDateButton();
                },
                dueCal.get(Calendar.YEAR),
                dueCal.get(Calendar.MONTH),
                dueCal.get(Calendar.DAY_OF_MONTH)).show());

        btnSave.setOnClickListener(v -> save());
    }

    private void fillForm() {
        etName.setText(subscription.getName());
        etPrice.setText(String.valueOf((long) subscription.getPrice()));
        spCycle.setSelection(subscription.isYearly() ? 1 : 0);
        swActive.setChecked(subscription.isActive());

        String[] cats = getResources().getStringArray(R.array.category_labels);
        for (int i = 0; i < cats.length; i++) {
            if (cats[i].equals(subscription.getCategory())) {
                spCategory.setSelection(i);
                break;
            }
        }
    }

    private void updateDateButton() {
        btnDate.setText(dateFormat.format(new Date(dueCal.getTimeInMillis())));
    }

    private void save() {
        String name = etName.getText().toString().trim();
        String priceText = etPrice.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Nama layanan wajib diisi");
            return;
        }
        double price;
        try {
            price = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            etPrice.setError("Masukkan harga yang valid");
            return;
        }
        if (price <= 0) {
            etPrice.setError("Harga harus lebih dari 0");
            return;
        }

        subscription.setName(name);
        subscription.setPrice(price);
        subscription.setBillingCycle(spCycle.getSelectedItemPosition() == 1
                ? Subscription.YEARLY : Subscription.MONTHLY);
        subscription.setNextDueDate(dueCal.getTimeInMillis());
        subscription.setCategory(spCategory.getSelectedItem().toString());
        subscription.setActive(swActive.isChecked());

        if (isEdit) db.update(subscription);
        else db.insert(subscription);

        Toast.makeText(this, "Tersimpan", Toast.LENGTH_SHORT).show();
        finish();
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
