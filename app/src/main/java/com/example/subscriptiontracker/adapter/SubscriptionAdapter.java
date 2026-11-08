package com.example.subscriptiontracker.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.subscriptiontracker.PrefsManager;
import com.example.subscriptiontracker.R;
import com.example.subscriptiontracker.model.Subscription;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SubscriptionAdapter extends RecyclerView.Adapter<SubscriptionAdapter.VH> {

    public interface Listener {
        void onItemClick(Subscription s);
        void onItemLongClick(Subscription s);
    }

    private final List<Subscription> items = new ArrayList<>();
    private final Listener listener;
    private PrefsManager prefs;
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd MMM yyyy", new Locale("id", "ID"));

    public SubscriptionAdapter(PrefsManager prefs, Listener listener) {
        this.prefs = prefs;
        this.listener = listener;
    }

    public void setItems(List<Subscription> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subscription, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Subscription s = items.get(position);
        String cycle = s.isYearly() ? "per tahun" : "per bulan";
        String category = (s.getCategory() == null || s.getCategory().isEmpty())
                ? "-" : s.getCategory();

        h.tvName.setText(s.getName());
        h.tvDetail.setText(category + " · " + cycle);
        h.tvPrice.setText(prefs.formatMoney(s.getPrice()));

        String due = "Jatuh tempo " + dateFormat.format(new Date(s.getNextDueDate()));
        int days = s.daysUntilDue();
        if (!s.isActive()) {
            due = "Nonaktif";
        } else if (days < 0) {
            due += " (terlambat " + (-days) + " hari)";
        } else if (days == 0) {
            due += " (hari ini)";
        } else {
            due += " (" + days + " hari lagi)";
        }
        h.tvDue.setText(due);
        h.itemView.setAlpha(s.isActive() ? 1f : 0.5f);

        h.itemView.setOnClickListener(v -> listener.onItemClick(s));
        h.itemView.setOnLongClickListener(v -> {
            listener.onItemLongClick(s);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvName, tvDetail, tvPrice, tvDue;

        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvDetail = itemView.findViewById(R.id.tvDetail);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvDue = itemView.findViewById(R.id.tvDue);
        }
    }
}
