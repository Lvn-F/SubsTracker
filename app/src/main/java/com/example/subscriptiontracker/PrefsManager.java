package com.example.subscriptiontracker;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Pembungkus SharedPreferences. Hanya menyimpan pengaturan kecil yang
 * berlaku global. Data langganan disimpan di database (DatabaseHelper).
 */
public class PrefsManager {
    private static final String PREF_NAME = "subscription_tracker_prefs";

    private static final String KEY_CURRENCY = "pref_currency";
    private static final String KEY_REMIND_DAYS = "pref_remind_days";
    private static final String KEY_DARK_MODE = "pref_dark_mode";
    private static final String KEY_NOTIF_ENABLED = "pref_notif_enabled";
    private static final String KEY_SORT_ORDER = "pref_sort_order";

    public static final String CURRENCY_IDR = "IDR";
    public static final String CURRENCY_USD = "USD";

    public static final String SORT_NAME = "name";
    public static final String SORT_PRICE = "price";
    public static final String SORT_DATE = "date";

    /** Kurs tetap untuk tampilan USD (perkiraan, bukan kurs real-time). */
    private static final double USD_RATE = 16000.0;

    private final SharedPreferences prefs;

    public PrefsManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public String getCurrency() {
        return prefs.getString(KEY_CURRENCY, CURRENCY_IDR);
    }

    public void setCurrency(String currency) {
        prefs.edit().putString(KEY_CURRENCY, currency).apply();
    }

    public int getRemindDays() {
        return prefs.getInt(KEY_REMIND_DAYS, 3);
    }

    public void setRemindDays(int days) {
        prefs.edit().putInt(KEY_REMIND_DAYS, days).apply();
    }

    public boolean isDarkMode() {
        return prefs.getBoolean(KEY_DARK_MODE, false);
    }

    public void setDarkMode(boolean enabled) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply();
    }

    public boolean isNotifEnabled() {
        return prefs.getBoolean(KEY_NOTIF_ENABLED, true);
    }

    public void setNotifEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_NOTIF_ENABLED, enabled).apply();
    }

    public String getSortOrder() {
        return prefs.getString(KEY_SORT_ORDER, SORT_DATE);
    }

    public void setSortOrder(String order) {
        prefs.edit().putString(KEY_SORT_ORDER, order).apply();
    }

    /** Terapkan tema terang/gelap sesuai pengaturan tersimpan. */
    public void applyTheme() {
        AppCompatDelegate.setDefaultNightMode(isDarkMode()
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
    }

    /** Format harga (disimpan dalam Rupiah) sesuai mata uang pilihan. */
    public String formatMoney(double priceIdr) {
        if (CURRENCY_USD.equals(getCurrency())) {
            NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
            nf.setMinimumFractionDigits(2);
            nf.setMaximumFractionDigits(2);
            return "$ " + nf.format(priceIdr / USD_RATE);
        }
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("id", "ID"));
        nf.setMaximumFractionDigits(0);
        return "Rp " + nf.format(priceIdr);
    }
}
