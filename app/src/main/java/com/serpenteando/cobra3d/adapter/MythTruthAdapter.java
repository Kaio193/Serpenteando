package com.serpenteando.cobra3d.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.serpenteando.cobra3d.R;
import com.serpenteando.cobra3d.model.entity.MythTruth;

import java.util.List;

public class MythTruthAdapter extends RecyclerView.Adapter<MythTruthAdapter.ViewHolder> {

    private List<MythTruth> items;

    public MythTruthAdapter(List<MythTruth> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_myth_truth, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MythTruth item = items.get(position);
        holder.statementText.setText(item.getStatement());
        holder.explanationText.setText(item.getExplanation());

        if (item.isMyth()) {
            holder.badgeText.setText(R.string.label_mito);
            holder.badgeText.setBackgroundResource(R.drawable.badge_myth);
        } else {
            holder.badgeText.setText(R.string.label_verdade);
            holder.badgeText.setBackgroundResource(R.drawable.badge_truth);
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView statementText;
        TextView explanationText;
        TextView badgeText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            statementText = itemView.findViewById(R.id.statementText);
            explanationText = itemView.findViewById(R.id.explanationText);
            badgeText = itemView.findViewById(R.id.badgeText);
        }
    }
}
