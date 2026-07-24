package com.kaio.cobra3d.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kaio.cobra3d.R;
import com.kaio.cobra3d.model.entity.AnatomyHotspot;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter simples para a lista de hotspots exibida no rodapé do Fragment 3D.
 * Cada item mostra nome + descrição e dispara callback de clique.
 */
public class HotspotAdapter extends RecyclerView.Adapter<HotspotAdapter.HotspotVH> {

    public interface OnHotspotClick {
        void onClick(int index);
    }

    private final List<AnatomyHotspot> hotspots = new ArrayList<>();
    private final OnHotspotClick listener;

    public HotspotAdapter(OnHotspotClick listener) {
        this.listener = listener;
    }

    public void setHotspots(List<AnatomyHotspot> items) {
        hotspots.clear();
        if (items != null) {
            hotspots.addAll(items);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HotspotVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hotspot, parent, false);
        return new HotspotVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull HotspotVH holder, int position) {
        AnatomyHotspot h = hotspots.get(position);
        holder.name.setText(h.getHotspotName() != null ? h.getHotspotName() : "—");
        holder.description.setText(h.getDescription() != null ? h.getDescription() : "");
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(position);
        });
    }

    @Override
    public int getItemCount() {
        return hotspots.size();
    }

    static class HotspotVH extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView description;

        HotspotVH(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.hotspotName);
            description = itemView.findViewById(R.id.hotspotDescription);
        }
    }
}