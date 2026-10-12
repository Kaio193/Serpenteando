package com.serpenteando.cobra3d.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.serpenteando.cobra3d.R;
import com.serpenteando.cobra3d.model.entity.Content;
import com.serpenteando.cobra3d.util.SnakeImageLoader;

import java.util.List;

public class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Content content);
    }

    private final List<Content> contents;
    private final OnItemClickListener listener;

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

        String body = content.getBody();
        holder.previewText.setText(body != null ? body : "");

        String sci = content.getScientificName();
        if (sci != null && !sci.isEmpty()) {
            holder.scientificText.setVisibility(View.VISIBLE);
            holder.scientificText.setText(sci);
        } else {
            holder.scientificText.setVisibility(View.GONE);
        }

        bindBadge(holder.itemView.getContext(), content, holder.categoryBadge);

        Bitmap bmp = SnakeImageLoader.load(holder.itemView.getContext(), content.getImageResName());
        if (bmp != null) {
            holder.contentImage.setImageBitmap(bmp);
        } else {
            holder.contentImage.setImageResource(R.drawable.ic_cobra);
        }

        holder.itemView.setOnClickListener(vista -> {
            if (listener != null) {
                listener.onItemClick(content);
            }
        });
    }

    public static void bindBadge(Context context, Content content, TextView badge) {
        if (content.isVenomous()) {
            badge.setText(R.string.badge_peconhenta);
            badge.setBackgroundResource(R.drawable.badge_venomous);
            badge.setTextColor(ContextCompat.getColor(context, R.color.cascavel_wrong));
        } else {
            badge.setText(R.string.badge_inofensiva);
            badge.setBackgroundResource(R.drawable.badge_safe);
            badge.setTextColor(ContextCompat.getColor(context, R.color.cascavel_jade_bright));
        }
    }

    @Override
    public int getItemCount() {
        return contents != null ? contents.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView titleText;
        final TextView previewText;
        final TextView scientificText;
        final TextView categoryBadge;
        final ImageView contentImage;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.titleText);
            previewText = itemView.findViewById(R.id.previewText);
            scientificText = itemView.findViewById(R.id.scientificText);
            categoryBadge = itemView.findViewById(R.id.categoryBadge);
            contentImage = itemView.findViewById(R.id.contentImage);
        }
    }
}