package com.kaio.cobra3d.view;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.util.Log;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import androidx.appcompat.widget.SwitchCompat;
import com.kaio.cobra3d.R;
import com.kaio.cobra3d.model.db.DatabaseHelper;
import com.kaio.cobra3d.util.AlarmScheduler;
import com.kaio.cobra3d.util.LocaleHelper;
import com.kaio.cobra3d.util.NotificationHelper;
import com.kaio.cobra3d.util.PreferenceManager;

/**
 * Tela de Configurações — gerencia preferências do usuário.
 *
 * Funcionalidades:
 * - Troca de idioma (PT/EN/ES) com recriação da Activity
 * - Dark Mode (AppCompatDelegate)
 * - Notificações (AlarmManager)
 * - Contato/Suporte (Intent implícita)
 *
 * [TEI Persistência] Usa SharedPreferences (slide 06) via PreferenceManager
 * — mesmo conceito do prof. Alexandre, só que encapsulado.
 *
 * [TEI Intents] Intent implícita pro email (slide 05)
 * — ACTION_SENDTO com mailto: é literalmente o exemplo da aula.
 */
public class SettingsFragment extends Fragment {

    // PreferenceManager: wrapper de SharedPreferences
    private PreferenceManager preferenceManager;
    private boolean ignoreDarkModeListener = false; // Flag pra ignorar listener do dark mode durante setup

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // [TEI Persistência] PreferenceManager = SharedPreferences do slide 06
        // Salva: dark mode, idioma, notificações, última missão
        preferenceManager = new PreferenceManager(requireContext());

        // === SEÇÃO: IDIOMA ===
        // Abre dialog de seleção de idioma (PT/EN/ES)
        View langRow = view.findViewById(R.id.rowLanguage);
        ((ImageView) langRow.findViewById(R.id.rowIcon)).setImageResource(R.drawable.ic_language);
        ((TextView) langRow.findViewById(R.id.rowTitle)).setText(R.string.settings_language);
        ((TextView) langRow.findViewById(R.id.rowValue)).setText(LocaleHelper.getLanguageName(requireContext()));
        langRow.findViewById(R.id.rowValue).setVisibility(View.VISIBLE);
        langRow.setOnClickListener(v -> {
            showLanguageDialog();
        });

        // === SEÇÃO: NOTIFICAÇÕES ===
        // Abre dialog de configuração de notificações
        View notifRow = view.findViewById(R.id.rowNotification);
        ((ImageView) notifRow.findViewById(R.id.rowIcon)).setImageResource(R.drawable.ic_notification);
        ((TextView) notifRow.findViewById(R.id.rowTitle)).setText(R.string.settings_notifications);
        ((TextView) notifRow.findViewById(R.id.rowValue)).setText(R.string.settings_notifications_on);
        notifRow.findViewById(R.id.rowValue).setVisibility(View.VISIBLE);
        notifRow.setOnClickListener(v -> {
            showNotificationsDialog();
        });

        // === SEÇÃO: MODO ESCURO ===
        // [TEI Manipulando Views] Switch + setOnCheckedChangeListener
        // — mesma técnica dos exercícios 02
        View darkModeRow = view.findViewById(R.id.rowDarkMode);
        ((ImageView) darkModeRow.findViewById(R.id.rowIcon)).setImageResource(R.drawable.ic_dark_mode);
        TextView darkModeTitle = darkModeRow.findViewById(R.id.rowTitle);
        SwitchCompat darkModeSwitch = darkModeRow.findViewById(R.id.rowSwitch);
        darkModeTitle.setText(R.string.settings_dark_mode);
        ignoreDarkModeListener = true;
        darkModeSwitch.setChecked(preferenceManager.isDarkModeEnabled());
        ignoreDarkModeListener = false;
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (ignoreDarkModeListener) return;
            preferenceManager.setDarkModeEnabled(isChecked);
            // Delay pra garantir que a preferência foi salva antes de recriar a Activity
            if (getView() != null) {
                getView().post(() -> AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO));
            }
        });
        // Click no row inteiro também alterna o switch
        // NOTA: toggle() dispara o listener, que chama setDefaultNightMode,
        // que recria a Activity. Isso é intencional.
        darkModeRow.setOnClickListener(v -> darkModeSwitch.toggle());

        // Version click row
        View versionRow = view.findViewById(R.id.rowVersion);
        ((ImageView) versionRow.findViewById(R.id.rowIcon)).setImageResource(R.drawable.ic_info);
        ((TextView) versionRow.findViewById(R.id.rowTitle)).setText(R.string.settings_version);
        ((TextView) versionRow.findViewById(R.id.rowValue)).setText(R.string.settings_version_name);
        versionRow.findViewById(R.id.rowValue).setVisibility(View.VISIBLE);
        versionRow.findViewById(R.id.rowChevron).setVisibility(View.GONE);

        // About click row — navega para a tela Sobre
        View aboutRow = view.findViewById(R.id.rowAbout);
        ((ImageView) aboutRow.findViewById(R.id.rowIcon)).setImageResource(R.drawable.ic_person_silhouette);
        ((TextView) aboutRow.findViewById(R.id.rowTitle)).setText(R.string.settings_about);
        aboutRow.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.nav_about);
        });

        // Credits click row — abre dialog com os créditos do app
        View creditsRow = view.findViewById(R.id.rowCredits);
        ((ImageView) creditsRow.findViewById(R.id.rowIcon)).setImageResource(R.drawable.ic_trophy);
        ((TextView) creditsRow.findViewById(R.id.rowTitle)).setText(R.string.credits_title);
        creditsRow.setOnClickListener(v -> {
            showCreditsDialog();
        });

        // === SEÇÃO: CONTATO ===
        // [TEI Intents] Intent implícita — abre app de email do sistema
        // Mesmo padrão do slide 05: ACTION_SENDTO + mailto:
        View contactRow = view.findViewById(R.id.rowContact);
        ((ImageView) contactRow.findViewById(R.id.rowIcon)).setImageResource(R.drawable.ic_help);
        ((TextView) contactRow.findViewById(R.id.rowTitle)).setText(R.string.settings_contact);
        contactRow.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                // Email central criado pelo Kaio (Falar Conosco)
                intent.setData(Uri.parse("mailto:centralserpenteando@gmail.com"));
                // A mensagem já vai preenchida (item 25 do doc, 30/09) e o
                // usuário pode editar tudo antes de enviar.
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.contact_email_subject));
                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.contact_email_body));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(getContext(), R.string.settings_no_email, Toast.LENGTH_SHORT).show();
            }
        });

        // Bug report click row
        View bugRow = view.findViewById(R.id.rowBugReport);
        ((ImageView) bugRow.findViewById(R.id.rowIcon)).setImageResource(R.drawable.ic_bug_report);
        ((TextView) bugRow.findViewById(R.id.rowTitle)).setText(R.string.settings_report_bug);
        bugRow.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                // Email central criado pelo Kaio (Reportar bug)
                intent.setData(Uri.parse("mailto:centralserpenteando@gmail.com"));
                // Modelo de mensagem (item 26 do doc, 30/09): já vem com o
                // roteiro do que descrever, editável pelo usuário.
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.bug_email_subject));
                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.bug_email_body));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(getContext(), R.string.settings_no_email, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Dialog de créditos — lista devs, orientador, avaliador, cliente e
     * tecnologias usadas no app.
     * [TEI Dialogs] Dialog customizado (slide 05.1).
     */
    private void showCreditsDialog() {
        try {
            if (!isAdded() || getContext() == null) return;

            Dialog dialog = new Dialog(requireContext());
            dialog.setContentView(R.layout.dialog_credits);
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().setLayout(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
            }
            dialog.setCanceledOnTouchOutside(true);

            TextView closeBtn = dialog.findViewById(R.id.dialogCloseButton);
            if (closeBtn != null) closeBtn.setOnClickListener(v -> dialog.dismiss());

            dialog.show();
        } catch (Exception e) {
            Log.e("SettingsFragment", "Error showing credits dialog", e);
            Toast.makeText(getContext(), getString(R.string.error_prefix) + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Dialog de seleção de idioma.
     * [TEI Dialogs] Dialog customizado (slide 05.1)
     * Salva o idioma e recria a Activity pra aplicar imediatamente.
     * O banco é resetado (só conteúdo) pra carregar perguntas traduzidas.
     */
    private void showLanguageDialog() {
        try {
            if (!isAdded() || getContext() == null) return;

            Dialog dialog = new Dialog(requireContext());
            dialog.setContentView(R.layout.dialog_language);
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().setLayout(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
            }
            dialog.setCanceledOnTouchOutside(true);

            String currentLang = LocaleHelper.getLanguage(requireContext());

            LinearLayout langPt = dialog.findViewById(R.id.langPortugues);
            LinearLayout langEn = dialog.findViewById(R.id.langEnglish);
            LinearLayout langEs = dialog.findViewById(R.id.langEspanol);
            TextView closeBtn = dialog.findViewById(R.id.dialogCloseButton);

            // Highlight o idioma atual
            updateLangRow(langPt, currentLang.equals("pt"));
            updateLangRow(langEn, currentLang.equals("en"));
            updateLangRow(langEs, currentLang.equals("es"));

            langPt.setOnClickListener(v -> {
                changeLanguage(LocaleHelper.LANG_PT);
                dialog.dismiss();
            });
            langEn.setOnClickListener(v -> {
                changeLanguage(LocaleHelper.LANG_EN);
                dialog.dismiss();
            });
            langEs.setOnClickListener(v -> {
                changeLanguage(LocaleHelper.LANG_ES);
                dialog.dismiss();
            });

            if (closeBtn != null) closeBtn.setOnClickListener(v -> dialog.dismiss());
            dialog.show();

        } catch (Exception e) {
            Log.e("SettingsFragment", "Error showing language dialog", e);
            Toast.makeText(getContext(), getString(R.string.error_prefix) + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void updateLangRow(LinearLayout row, boolean selected) {
        if (row == null) return;
        // Item 2/7 do Kaio (11/10/2026): as linhas de idioma usam o MESMO verde
        // da classificacao "Inofensiva" (chip_jade/cascavel_jade_bright).
        row.setBackgroundResource(selected ? R.drawable.chip_jade_selected : R.drawable.chip_jade);
        // First child = label TextView
        // Texto branco nos dois estados (Kaio, 07/10): a linha do idioma agora é
        // verde como os botões, então o texto verde-claro não teria contraste.
        // O selecionado continua marcado pelo negrito e pelo ✓.
        if (row.getChildCount() > 0 && row.getChildAt(0) instanceof TextView) {
            TextView label = (TextView) row.getChildAt(0);
            label.setTextColor(ContextCompat.getColor(requireContext(), R.color.cascavel_on_button));
            label.setTypeface(null, selected ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        }
        // Second child = checkmark
        if (row.getChildCount() > 1 && row.getChildAt(1) instanceof TextView) {
            TextView check = (TextView) row.getChildAt(1);
            check.setVisibility(selected ? android.view.View.VISIBLE : android.view.View.GONE);
            if (selected) check.setTextColor(ContextCompat.getColor(requireContext(), R.color.cascavel_on_button));
        }
    }

    /**
     * Troca o idioma e recria a Activity.
     * 1. Salva preferência (SharedPreferences)
     * 2. Reseta conteúdo do banco (mantém progresso do usuário)
     * 3. Recria Activity pra aplicar novo idioma em todas as telas
     */
    private void changeLanguage(String langCode) {
        LocaleHelper.setLocale(requireContext(), langCode);

        // Reseta o banco pra recriar com perguntas traduzidas
        DatabaseHelper.resetForLanguageChange(requireContext());

        // Recria a Activity inteira pra aplicar o novo idioma em todas as telas
        if (getActivity() != null) {
            getActivity().recreate();
        }
    }

    private void showNotificationsDialog() {
        try {
            if (!isAdded() || getContext() == null) return;

            Dialog dialog = new Dialog(requireContext());
            dialog.setContentView(R.layout.dialog_notifications);
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
            }
            dialog.setCanceledOnTouchOutside(true);

            // Pega os switches do dialog
            SwitchCompat switchDaily = dialog.findViewById(R.id.switchDailyReminder);
            SwitchCompat switchMissions = dialog.findViewById(R.id.switchNewMissions);
            SwitchCompat switchAchievements = dialog.findViewById(R.id.switchAchievements);

            // Carrega estado salvo
            if (switchDaily != null) switchDaily.setChecked(preferenceManager.isDailyReminderEnabled());
            if (switchMissions != null) switchMissions.setChecked(preferenceManager.isMissionNotifEnabled());
            if (switchAchievements != null) switchAchievements.setChecked(preferenceManager.isAchievementNotifEnabled());

            // Salva ao alterar
            if (switchDaily != null) switchDaily.setOnCheckedChangeListener((btn, isChecked) -> {
                preferenceManager.setDailyReminderEnabled(isChecked);
                if (isChecked) {
                    AlarmScheduler.scheduleDailyReminder(requireContext());
                } else {
                    AlarmScheduler.cancelDailyReminder(requireContext());
                }
            });
            if (switchMissions != null) switchMissions.setOnCheckedChangeListener((btn, isChecked) -> {
                preferenceManager.setMissionNotifEnabled(isChecked);
            });
            if (switchAchievements != null) switchAchievements.setOnCheckedChangeListener((btn, isChecked) -> {
                preferenceManager.setAchievementNotifEnabled(isChecked);
            });

            // Solicita permissão no Android 13+ se ainda não tiver
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                if (!NotificationHelper.hasPermission(requireContext())) {
                    requestPermissions(
                            new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                            1001);
                }
            }

            dialog.show();

            TextView saveBtn = dialog.findViewById(R.id.dialogSaveButton);
            if (saveBtn != null) saveBtn.setOnClickListener(v -> dialog.dismiss());

        } catch (Exception e) {
            Log.e("SettingsFragment", "Error showing notifications dialog", e);
            Toast.makeText(getContext(), R.string.settings_error_dialog, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        preferenceManager = null;
    }
}
