package com.example.subscriptiontracker.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.subscriptiontracker.PrefsManager;
import com.example.subscriptiontracker.model.Subscription;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "subscription_tracker.db";
    private static final int DB_VERSION = 1;

    private static final String T_SUB = "subscriptions";
    private static final String T_HIST = "payment_history";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + T_SUB + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "name TEXT NOT NULL,"
                + "price REAL NOT NULL,"
                + "billing_cycle TEXT NOT NULL,"
                + "next_due_date INTEGER NOT NULL,"
                + "category TEXT,"
                + "is_active INTEGER NOT NULL DEFAULT 1)");
        db.execSQL("CREATE TABLE " + T_HIST + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "subscription_id INTEGER NOT NULL,"
                + "paid_date INTEGER NOT NULL,"
                + "amount REAL NOT NULL,"
                + "FOREIGN KEY(subscription_id) REFERENCES " + T_SUB + "(id) ON DELETE CASCADE)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + T_HIST);
        db.execSQL("DROP TABLE IF EXISTS " + T_SUB);
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    private ContentValues toValues(Subscription s) {
        ContentValues v = new ContentValues();
        v.put("name", s.getName());
        v.put("price", s.getPrice());
        v.put("billing_cycle", s.getBillingCycle());
        v.put("next_due_date", s.getNextDueDate());
        v.put("category", s.getCategory());
        v.put("is_active", s.isActive() ? 1 : 0);
        return v;
    }

    private Subscription fromCursor(Cursor c) {
        Subscription s = new Subscription();
        s.setId(c.getLong(c.getColumnIndexOrThrow("id")));
        s.setName(c.getString(c.getColumnIndexOrThrow("name")));
        s.setPrice(c.getDouble(c.getColumnIndexOrThrow("price")));
        s.setBillingCycle(c.getString(c.getColumnIndexOrThrow("billing_cycle")));
        s.setNextDueDate(c.getLong(c.getColumnIndexOrThrow("next_due_date")));
        s.setCategory(c.getString(c.getColumnIndexOrThrow("category")));
        s.setActive(c.getInt(c.getColumnIndexOrThrow("is_active")) == 1);
        return s;
    }

    public long insert(Subscription s) {
        return getWritableDatabase().insert(T_SUB, null, toValues(s));
    }

    public int update(Subscription s) {
        return getWritableDatabase().update(T_SUB, toValues(s), "id = ?",
                new String[]{String.valueOf(s.getId())});
    }

    public void delete(long id) {
        getWritableDatabase().delete(T_SUB, "id = ?", new String[]{String.valueOf(id)});
    }

    public Subscription getById(long id) {
        try (Cursor c = getReadableDatabase().query(T_SUB, null, "id = ?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            if (c.moveToFirst()) return fromCursor(c);
        }
        return null;
    }

    /** Ambil semua langganan, diurutkan sesuai pengaturan (name / price / date). */
    public List<Subscription> getAll(String sortOrder) {
        String orderBy;
        if (PrefsManager.SORT_NAME.equals(sortOrder)) {
            orderBy = "name COLLATE NOCASE ASC";
        } else if (PrefsManager.SORT_PRICE.equals(sortOrder)) {
            orderBy = "price DESC";
        } else {
            orderBy = "next_due_date ASC";
        }
        List<Subscription> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query(T_SUB, null, null, null, null, null, orderBy)) {
            while (c.moveToNext()) list.add(fromCursor(c));
        }
        return list;
    }

    /** Catat pembayaran lalu majukan tanggal tagih berikutnya satu siklus. */
    public void markPaid(long id) {
        Subscription s = getById(id);
        if (s == null) return;

        long now = System.currentTimeMillis();
        ContentValues h = new ContentValues();
        h.put("subscription_id", id);
        h.put("paid_date", now);
        h.put("amount", s.getPrice());
        getWritableDatabase().insert(T_HIST, null, h);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(s.getNextDueDate());
        do {
            if (s.isYearly()) cal.add(Calendar.YEAR, 1);
            else cal.add(Calendar.MONTH, 1);
        } while (cal.getTimeInMillis() < now);

        s.setNextDueDate(cal.getTimeInMillis());
        update(s);
    }
}
