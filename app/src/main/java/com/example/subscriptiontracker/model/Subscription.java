package com.example.subscriptiontracker.model;

import java.util.Calendar;

public class Subscription {
    public static final String MONTHLY = "MONTHLY";
    public static final String YEARLY = "YEARLY";

    private long id;
    private String name;
    private double price;          // disimpan dalam Rupiah (IDR) per siklus
    private String billingCycle;   // MONTHLY / YEARLY
    private long nextDueDate;      // millis
    private String category;
    private boolean active;

    public Subscription() {
        this.billingCycle = MONTHLY;
        this.active = true;
        this.nextDueDate = System.currentTimeMillis();
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getBillingCycle() { return billingCycle; }
    public void setBillingCycle(String billingCycle) { this.billingCycle = billingCycle; }

    public long getNextDueDate() { return nextDueDate; }
    public void setNextDueDate(long nextDueDate) { this.nextDueDate = nextDueDate; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public boolean isYearly() { return YEARLY.equals(billingCycle); }

    /** Biaya yang dihitung per bulan (tahunan dibagi 12). */
    public double getMonthlyCost() {
        return isYearly() ? price / 12.0 : price;
    }

    /** Biaya yang dihitung per tahun (bulanan dikali 12). */
    public double getYearlyCost() {
        return isYearly() ? price : price * 12.0;
    }

    /** Selisih hari dari hari ini ke tanggal tagih (negatif = sudah lewat). */
    public int daysUntilDue() {
        Calendar today = Calendar.getInstance();
        clearTime(today);
        Calendar due = Calendar.getInstance();
        due.setTimeInMillis(nextDueDate);
        clearTime(due);
        long diff = due.getTimeInMillis() - today.getTimeInMillis();
        return (int) Math.round(diff / (24.0 * 60 * 60 * 1000));
    }

    private static void clearTime(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
    }
}
