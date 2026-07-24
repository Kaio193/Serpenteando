package com.kaio.cobra3d.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.viewpager.widget.ViewPager;

import com.kaio.cobra3d.R;
import com.kaio.cobra3d.adapter.OnboardingPagerAdapter;

public class OnboardingActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "serpenteando_prefs";
    private static final String ONBOARDING_KEY = "onboarding_completed";

    private ViewPager viewPager;
    private LinearLayout dotsLayout;
    private TextView skipButton;
    private TextView nextButton;
    private OnboardingPagerAdapter adapter;

    private final int[] images = {
            R.drawable.ic_cobra,
            R.drawable.ic_book,
            R.drawable.ic_missao
    };

    private final String[] titles = {
            "Conheça as Serpentes",
            "Teste seu Conhecimento",
            "Complete Missões"
    };

    private final String[] descriptions = {
            "Explore informações detalhadas sobre cobras brasileiras, aprenda a identificar espécies e entenda seus habitats.",
            "Desafie-se com quizzes interativos sobre os mais diversos temas do mundo das serpentes.",
            "Ganhe pontos e desbloqueie conquistas completando missões e atividades educativas."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(
                getWindow(), getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(false);
        controller.setAppearanceLightNavigationBars(false);

        viewPager = findViewById(R.id.viewPager);
        dotsLayout = findViewById(R.id.dotsLayout);
        skipButton = findViewById(R.id.skipButton);
        nextButton = findViewById(R.id.nextButton);

        adapter = new OnboardingPagerAdapter(this, images, titles, descriptions);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(pageChangeListener);

        setupDots(0);

        skipButton.setOnClickListener(v -> goToMain());

        nextButton.setOnClickListener(v -> {
            int current = viewPager.getCurrentItem();
            if (current < images.length - 1) {
                viewPager.setCurrentItem(current + 1);
            } else {
                goToMain();
            }
        });
    }

    private void setupDots(int current) {
        dotsLayout.removeAllViews();
        for (int i = 0; i < images.length; i++) {
            ImageView dot = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(10, 10);
            params.setMargins(6, 0, 6, 0);
            dot.setLayoutParams(params);

            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.OVAL);
            if (i == current) {
                shape.setColor(ContextCompat.getColor(this, R.color.cascavel_jade));
            } else {
                shape.setColor(ContextCompat.getColor(this, R.color.cascavel_on_surface_variant));
            }
            dot.setBackground(shape);
            dotsLayout.addView(dot);
        }
    }

    private final ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

        @Override
        public void onPageSelected(int position) {
            setupDots(position);
            if (position == images.length - 1) {
                skipButton.setVisibility(View.GONE);
                nextButton.setText("Começar");
            } else {
                skipButton.setVisibility(View.VISIBLE);
                nextButton.setText("Próximo");
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {}
    };

    private void goToMain() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(ONBOARDING_KEY, true).apply();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
