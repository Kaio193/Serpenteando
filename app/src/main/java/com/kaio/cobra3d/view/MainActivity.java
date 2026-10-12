package com.kaio.cobra3d.view;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.kaio.cobra3d.R;
import com.kaio.cobra3d.util.LocaleHelper;
import com.kaio.cobra3d.util.SwipeFrameLayout;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private NavController.OnDestinationChangedListener destinationListener;

    // Custom bottom nav views
    private LinearLayout navHome, navQuiz, nav3d, navMissions, navSettings;
    private TextView navHomeLabel, navQuizLabel, nav3dLabel, navMissionsLabel, navSettingsLabel;
    private ImageView navHomeIcon, navQuizIcon, nav3dIcon, navMissionsIcon, navSettingsIcon;

    private int currentNavId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Aplica o idioma salvo ANTES de inflar o layout
        LocaleHelper.onAttach(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        enableEdgeToEdge();

        if (android.os.Build.VERSION.SDK_INT >= 31) {
            getWindow().setBackgroundBlurRadius(25);
        }

        // Setup NavHostFragment
        NavHostFragment navHost = null;
        try {
            navHost = (NavHostFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.navHostFragment);
        } catch (ClassCastException e) {
            navHost = null;
        }
        if (navHost != null) {
            navController = navHost.getNavController();

            // Setup custom bottom nav
            setupCustomBottomNav();

            // Listen for destination changes to highlight active tab
            destinationListener = (controller, destination, arguments) -> {
                int destId = destination.getId();
                highlightTab(destId);

                // Show/hide bottom nav for sub-screens
                boolean isTab = destId == R.id.nav_home
                        || destId == R.id.nav_quiz
                        || destId == R.id.nav_anatomy
                        || destId == R.id.nav_missions
                        || destId == R.id.nav_settings;
                LinearLayout bottomNav = findViewById(R.id.customBottomNav);
                if (bottomNav != null) {
                    bottomNav.setVisibility(isTab ? View.VISIBLE : View.GONE);
                }
                // Realinha o espaço reservado embaixo: com a navbar escondida o
                // conteúdo desce até a barra nativa (senão sobra faixa vazia).
                applyBottomSpacing();
            };
            navController.addOnDestinationChangedListener(destinationListener);

            // Start on home
            if (savedInstanceState == null) {
                highlightTab(R.id.nav_home);
            }

            // Navegação por SWIPE horizontal REMOVIDA (Kaio, 11/10 — item 17).
            // Antes daqui saía o setupSwipeGesture(), que ligava o arrasto
            // Home <-> Quiz <-> 3D <-> Missões <-> Config. Agora quem navega é
            // só a navbar e os botões. O SwipeFrameLayout do layout continua
            // no XML (ele cuida do espaçamento/insets), mas fica sem callback
            // — ou seja, sem nenhum gesto de navegação.

            // Ajusta o bottom nav pra não ficar escondido atrás da barra de
            // navegação nativa (celular com botões, ex: A36)
            applyNavBarInsets();
        }
    }

    /**
     * Ajusta a barra de navegação inferior do app pra não sobrepor a barra
     * nativa do celular. Em celulares com gestos funciona sozinho; em
     * celulares com botões (A36), a barra nativa fica em cima da nossa.
     * Aqui a gente adiciona paddingBottom = altura da barra nativa.
     */
    private void applyNavBarInsets() {
        View root = findViewById(R.id.mainRoot);
        if (root == null) return;

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            int bottomInset = bars.bottom;

            LinearLayout bottomNav = findViewById(R.id.customBottomNav);
            if (bottomNav != null) {
                // Navbar flutuante (pill): em vez de crescer pra trás da barra
                // nativa, ela flutua ACIMA dela — margem = inset + folga.
                ViewGroup.MarginLayoutParams navLp =
                        (ViewGroup.MarginLayoutParams) bottomNav.getLayoutParams();
                navLp.bottomMargin = bottomInset + dpToPx(NAV_PILL_MARGIN_DP);
                bottomNav.setLayoutParams(navLp);
            }

            lastBottomInset = bottomInset;
            applyBottomSpacing();

            return windowInsets;
        });
        // O conteúdo dos fragments NÃO pode somar o inset de baixo de novo.
        // O swipeContainer já termina exatamente no topo da pill da navbar, e
        // vários layouts de fragment usam fitsSystemWindows="true" — sem isso
        // eles aplicam o inset nativo outra vez no padding inferior e sobra uma
        // FAIXA de ~48dp com a cor de fundo entre o conteúdo e a navbar
        // (era a "barrinha" que escondia a explicação do quiz).
        // O topo (status bar) continua sendo tratado normalmente.
        SwipeFrameLayout swipeContent = findViewById(R.id.swipeContainer);
        if (swipeContent != null) {
            ViewCompat.setOnApplyWindowInsetsListener(swipeContent, (v, insets) -> {
                Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                return insets.inset(0, 0, 0, bars.bottom);
            });
        }

        root.requestApplyInsets();
    }

    /** Último inset da barra nativa recebido (px), guardado pra realinhar o
     *  conteúdo quando a navbar do app é escondida ou mostrada de novo. */
    private int lastBottomInset = 0;

    /**
     * Reserva embaixo do conteúdo só o que existe de verdade:
     * - navbar do app VISÍVEL   -> 64dp da pill + 10dp de folga + inset nativo;
     * - navbar do app ESCONDIDA -> só o inset nativo.
     * Sem isso, nas telas que escondem a navbar (Sobre, Conteúdo, Mitos) sobrava
     * uma FAIXA VAZIA de 75dp em cima da barra nativa - a "barrinha" invisível.
     */
    private void applyBottomSpacing() {
        SwipeFrameLayout swipe = findViewById(R.id.swipeContainer);
        if (swipe == null) return;
        LinearLayout bottomNav = findViewById(R.id.customBottomNav);
        boolean navVisible = bottomNav != null && bottomNav.getVisibility() == View.VISIBLE;
        int reserved = navVisible ? dpToPx(NAV_PILL_HEIGHT_DP + NAV_PILL_MARGIN_DP) : 0;
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) swipe.getLayoutParams();
        lp.bottomMargin = reserved + lastBottomInset;
        swipe.setLayoutParams(lp);
    }

    /** Altura da pill da navbar — precisa bater com layout_bottom_nav_custom.xml */
    private static final int NAV_PILL_HEIGHT_DP = 64;
    /** Folga entre a pill e a barra nativa do celular (usada em applyNavBarInsets) */
    private static final int NAV_PILL_MARGIN_DP = 10;

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void setupCustomBottomNav() {
        navHome = findViewById(R.id.nav_home);
        navQuiz = findViewById(R.id.nav_quiz);
        nav3d = findViewById(R.id.nav_3d);
        navMissions = findViewById(R.id.nav_missions);
        navSettings = findViewById(R.id.nav_settings);

        navHomeLabel = findViewById(R.id.nav_home_label);
        navQuizLabel = findViewById(R.id.nav_quiz_label);
        nav3dLabel = findViewById(R.id.nav_3d_label);
        navMissionsLabel = findViewById(R.id.nav_missions_label);
        navSettingsLabel = findViewById(R.id.nav_settings_label);

        navHomeIcon = findViewById(R.id.nav_home_icon);
        navQuizIcon = findViewById(R.id.nav_quiz_icon);
        nav3dIcon = findViewById(R.id.nav_3d_icon);
        navMissionsIcon = findViewById(R.id.nav_missions_icon);
        navSettingsIcon = findViewById(R.id.nav_settings_icon);



        navHome.setOnClickListener(v -> navigateTo(R.id.nav_home));
        navQuiz.setOnClickListener(v -> navigateTo(R.id.nav_quiz));
        nav3d.setOnClickListener(v -> navigateTo(R.id.nav_anatomy));
        navMissions.setOnClickListener(v -> navigateTo(R.id.nav_missions));
        navSettings.setOnClickListener(v -> navigateTo(R.id.nav_settings));
    }

    private void navigateTo(int destinationId) {
        if (navController == null) return;
        if (navController.getCurrentDestination() != null
                && navController.getCurrentDestination().getId() == destinationId) return;

        // Pop back to start destination before navigating to a new tab
        navController.popBackStack(R.id.nav_home, false);

        // Navigate to the selected tab
        navController.navigate(destinationId);
    }

    private void highlightTab(int activeId) {
        // Reset all to inactive
        resetTab(navHomeLabel, navHomeIcon, R.drawable.ic_home);
        resetTab(navQuizLabel, navQuizIcon, R.drawable.ic_dice);
        resetTab(nav3dLabel, nav3dIcon, R.drawable.ic_3d);
        resetTab(navMissionsLabel, navMissionsIcon, R.drawable.ic_rocket);
        resetTab(navSettingsLabel, navSettingsIcon, R.drawable.ic_settings);

        // Highlight active tab
        if (activeId == R.id.nav_home) {
            setActiveTab(navHomeLabel, navHomeIcon, R.drawable.ic_home);
        } else if (activeId == R.id.nav_quiz) {
            setActiveTab(navQuizLabel, navQuizIcon, R.drawable.ic_dice);
        } else if (activeId == R.id.nav_anatomy) {
            setActiveTab(nav3dLabel, nav3dIcon, R.drawable.ic_3d);
        } else if (activeId == R.id.nav_missions) {
            setActiveTab(navMissionsLabel, navMissionsIcon, R.drawable.ic_rocket);
        } else if (activeId == R.id.nav_settings) {
            setActiveTab(navSettingsLabel, navSettingsIcon, R.drawable.ic_settings);
        }
    }

    private void resetTab(TextView label, ImageView icon, int iconRes) {
        label.setTextColor(ContextCompat.getColor(this, R.color.cascavel_on_surface_variant));
        icon.setColorFilter(ContextCompat.getColor(this, R.color.cascavel_on_surface_variant));
    }

    private void setActiveTab(TextView label, ImageView icon, int iconRes) {
        label.setTextColor(ContextCompat.getColor(this, R.color.cascavel_jade_bright));
        icon.setColorFilter(ContextCompat.getColor(this, R.color.cascavel_jade_bright));
    }

    @Override
    protected void onDestroy() {
        if (navController != null && destinationListener != null) {
            navController.removeOnDestinationChangedListener(destinationListener);
        }
        super.onDestroy();
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
