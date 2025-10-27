package com.example.mutual_fund_comparison.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mutual_fund_comparison.R;
import com.example.mutual_fund_comparison.model.Fund;

import java.util.ArrayList;
import java.util.List;

public class FundAdapter extends RecyclerView.Adapter<FundAdapter.VH> {
    private final ArrayList<Fund> items;
    private final OnRemoveListener removeListener;

    public interface OnRemoveListener {
        void onRemove(int position);
    }

    public FundAdapter(ArrayList<Fund> items, OnRemoveListener l) {
        // defend against null being passed
        this.items = (items != null) ? items : new ArrayList<>();
        this.removeListener = l;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fund, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Fund f = items.get(position);
        holder.title.setText(f.getName());

        holder.remove.setOnClickListener(view -> {
            // use binding adapter position and check valid
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            // if the host wants to handle removal (e.g. sync state), call listener
            if (removeListener != null) {
                removeListener.onRemove(pos);
            } else {
                // otherwise remove locally and notify
                removeAt(pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** Helper to remove from adapter list and notify RecyclerView */
    public void removeAt(int position) {
        if (position < 0 || position >= items.size()) return;
        items.remove(position);
        notifyItemRemoved(position);
    }

    /** Replace data set */
    public void updateData(List<Fund> newList) {
        items.clear();
        if (newList != null) items.addAll(newList);
        notifyDataSetChanged();
    }

    /** Add single item */
    public void addItem(Fund f) {
        items.add(f);
        notifyItemInserted(items.size() - 1);
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView title;
        final ImageButton remove;

        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.fundTitle);
            remove = itemView.findViewById(R.id.btnRemove);
        }
    }
}
