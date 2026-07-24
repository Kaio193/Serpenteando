package com.kaio.cobra3d.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kaio.cobra3d.R;
import com.kaio.cobra3d.model.entity.MissionItem;

import java.util.ArrayList;
import java.util.List;

public class MissionItemAdapter extends RecyclerView.Adapter<MissionItemAdapter.ViewHolder> {

    private List<MissionItem> items;
    private List<Boolean> selections;
    private boolean enabled = true;

    public MissionItemAdapter(List<MissionItem> items) {
        this.items = items;
        this.selections = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            selections.add(false);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mission_checkbox, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MissionItem item = items.get(position);
        holder.itemText.setText(item.getItemText());

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(selections.get(position));

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!enabled) {
                buttonView.setChecked(!isChecked);
                return;
            }
            selections.set(position, isChecked);
        });

        holder.itemView.setAlpha(enabled ? 1.0f : 0.5f);
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public List<Boolean> getSelections() {
        return selections;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView itemText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.itemCheckBox);
            itemText = itemView.findViewById(R.id.itemText);
        }
    }
}
