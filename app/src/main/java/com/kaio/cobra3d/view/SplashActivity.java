package com.kaio.cobra3d.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.kaio.cobra3d.R;
import com.kaio.cobra3d.util.LocaleHelper;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 1500;
    private static final String PREFS_NAME = "serpenteando_prefs";
    private static final String ONBOARDING_KEY = "onboarding_completed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Aplica idioma salvo ANTES de setContentView
        LocaleHelper.onAttach(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(
                getWindow(), getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(false);
        controller.setAppearanceLightNavigationBars(false);

        new Handler(Looper.getMainLooper()).postDelayed(this::navigate, SPLASH_DELAY_MS);
    }

    private void navigate() {
        if (isFinishing()) return;

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean onboardingDone = prefs.getBoolean(ONBOARDING_KEY, false);

        Intent intent = onboardingDone
                ? new Intent(this, MainActivity.class)
                : new Intent(this, OnboardingActivity.class);

        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
