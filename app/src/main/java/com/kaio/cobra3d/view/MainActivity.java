package com.kaio.cobra3d.view;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.kaio.cobra3d.R;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        enableEdgeToEdge();

        if (android.os.Build.VERSION.SDK_INT >= 31) {
            getWindow().setBackgroundBlurRadius(25);
        }

        bottomNav = findViewById(R.id.bottomNavigation);
        NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.navHostFragment);
        if (navHost != null) {
            navController = navHost.getNavController();
            NavigationUI.setupWithNavController(bottomNav, navController);

            navController.addOnDestinationChangedListener(
                    (controller, destination, arguments) -> {
                        int destId = destination.getId();
                        boolean isTab = destId == R.id.nav_home
                                || destId == R.id.nav_quiz
                                || destId == R.id.nav_missions
                                || destId == R.id.nav_settings;
                        bottomNav.setVisibility(isTab ? android.view.View.VISIBLE : android.view.View.GONE);
                    });
        }
    }

    private void enableEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(
                getWindow(), getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(
                getResources().getBoolean(R.bool.window_light_status_bar));
        controller.setAppearanceLightNavigationBars(false);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController != null && navController.navigateUp()
                || super.onSupportNavigateUp();
    }
}
