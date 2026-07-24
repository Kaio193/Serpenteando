package com.kaio.cobra3d.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.kaio.cobra3d.R;

public class OnboardingPagerAdapter extends PagerAdapter {

    private final Context context;
    private final int[] images;
    private final String[] titles;
    private final String[] descriptions;

    public OnboardingPagerAdapter(Context context, int[] images, String[] titles, String[] descriptions) {
        this.context = context;
        this.images = images;
        this.titles = titles;
        this.descriptions = descriptions;
    }

    @Override
    public int getCount() {
        return images.length;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.onboarding_page, container, false);

        ImageView image = view.findViewById(R.id.pageImage);
        TextView title = view.findViewById(R.id.pageTitle);
        TextView description = view.findViewById(R.id.pageDescription);

        image.setImageResource(images[position]);
        title.setText(titles[position]);
        description.setText(descriptions[position]);

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
}
