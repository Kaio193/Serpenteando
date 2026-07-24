package com.kaio.cobra3d.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kaio.cobra3d.R;
import com.kaio.cobra3d.model.entity.Content;

import java.util.List;

public class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Content content);
    }

    private List<Content> contents;
    private OnItemClickListener listener;

    public ContentAdapter(List<Content> contents, OnItemClickListener listener) {
        this.contents = contents;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_content_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Content content = contents.get(position);
        holder.titleText.setText(content.getTitle());
        holder.previewText.setText(content.getBody());

        String category = content.getCategory();
        holder.categoryBadge.setText(getCategoryLabel(category));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(content);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contents != null ? contents.size() : 0;
    }

    private String getCategoryLabel(String category) {
        switch (category) {
            case "cobras": return "Cobras";
            case "anfibios": return "Anfíbios";
            case "peconhentos": return "Peçonhentos";
            case "nao_peconhentos": return "Não Peçonhentos";
            default: return category;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView previewText;
        TextView categoryBadge;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.titleText);
            previewText = itemView.findViewById(R.id.previewText);
            categoryBadge = itemView.findViewById(R.id.categoryBadge);
        }
    }
}
