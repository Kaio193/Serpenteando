package com.serpenteando.cobra3d.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.serpenteando.cobra3d.R;
import com.serpenteando.cobra3d.model.entity.MissionItem;

import java.util.List;

public class MissionItemAdapter extends RecyclerView.Adapter<MissionItemAdapter.ViewHolder> {

    private List<MissionItem> items;
    private boolean enabled = true;

    // Descrições padrão são carregadas de strings.xml (string-array mission_default_descriptions)

    public MissionItemAdapter(List<MissionItem> items) {
        this.items = items;
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

        boolean isFulfilled = item.isCorrect();

        // Círculo de status: ✓ branco em círculo verde (concluído) ou contorno (pendente)
        if (isFulfilled) {
            holder.statusIcon.setBackgroundResource(R.drawable.circle_check_done);
            holder.statusIcon.setText("\u2713");
            holder.statusIcon.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white));
        } else {
            holder.statusIcon.setBackgroundResource(R.drawable.circle_check_pending);
            holder.statusIcon.setText("");
        }

        // Título sempre branco — o estado aparece só no círculo (como no mockup)
        holder.itemText.setTextColor(
                ContextCompat.getColor(holder.itemView.getContext(), R.color.cascavel_on_bg));

        // Descrição do item
        String trigger = item.getTrigger();
        if (trigger != null && !trigger.isEmpty()) {
            holder.description.setText(getDescriptionForTrigger(trigger, holder.itemView.getContext()));
            holder.description.setVisibility(View.VISIBLE);
        } else {
            String[] descs = holder.itemView.getContext().getResources().getStringArray(R.array.mission_default_descriptions);
            int descIndex = position % descs.length;
            holder.description.setText(descs[descIndex]);
            holder.description.setVisibility(View.VISIBLE);
        }

        // Ícone dourado à direita só aparece quando o item está concluído (como no mockup)
        if (isFulfilled) {
            int iconRes = getConceptIconRes(trigger, position);
            if (iconRes != 0) {
                holder.conceptIcon.setImageResource(iconRes);
                holder.conceptIcon.setVisibility(View.VISIBLE);
            } else {
                holder.conceptIcon.setVisibility(View.GONE);
            }
        } else {
            holder.conceptIcon.setVisibility(View.GONE);
        }

        // Desabilita interação quando desativado
        holder.itemView.setAlpha(enabled ? 1.0f : 0.6f);
        holder.itemView.setOnClickListener(null);
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public int getFulfilledCount() {
        int count = 0;
        if (items != null) {
            for (MissionItem item : items) {
                if (item.isCorrect()) count++;
            }
        }
        return count;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        notifyDataSetChanged();
    }

    /**
     * Retorna a descrição traduzida do trigger da missão.
     * Usa string resources para suportar PT/EN/ES.
     */
    private String getDescriptionForTrigger(String trigger, android.content.Context ctx) {
        switch (trigger) {
            case "QUIZ_FACIL": return ctx.getString(R.string.mission_trigger_quiz_facil);
            case "QUIZ_MEDIO": return ctx.getString(R.string.mission_trigger_quiz_medio);
            case "QUIZ_DIFICIL": return ctx.getString(R.string.mission_trigger_quiz_dificil);
            case "ANATOMY_HOTSPOT": return ctx.getString(R.string.mission_trigger_anatomy_hotspot);
            case "ANATOMY_LAYER": return ctx.getString(R.string.mission_trigger_anatomy_layer);
            case "CONTENT_DETAIL": return ctx.getString(R.string.mission_trigger_content_detail);
            default: return ctx.getString(R.string.mission_trigger_default);
        }
    }

    /**
     * Ícone dourado por tipo de tarefa (vetor + tint gold no layout).
     */
    private int getConceptIconRes(String trigger, int position) {
        if (trigger == null) {
            int[] icons = {
                    R.drawable.ic_quiz,
                    R.drawable.ic_content,
                    R.drawable.ic_3d,
                    R.drawable.ic_medal
            };
            return icons[position % icons.length];
        }
        switch (trigger) {
            case "QUIZ_FACIL":
            case "QUIZ_MEDIO":
            case "QUIZ_DIFICIL": return R.drawable.ic_quiz;
            case "ANATOMY_HOTSPOT":
            case "ANATOMY_LAYER": return R.drawable.ic_3d;
            case "CONTENT_DETAIL": return R.drawable.ic_content;
            default: return R.drawable.ic_medal;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView statusIcon;
        TextView itemText;
        TextView description;
        ImageView conceptIcon;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            statusIcon = itemView.findViewById(R.id.itemStatusIcon);
            itemText = itemView.findViewById(R.id.itemText);
            description = itemView.findViewById(R.id.itemDescription);
            conceptIcon = itemView.findViewById(R.id.itemConceptIcon);
        }
    }
}
