package com.serpenteando.cobra3d.view;

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
import com.serpenteando.cobra3d.R;
import com.serpenteando.cobra3d.model.db.DatabaseHelper;
import com.serpenteando.cobra3d.util.AlarmScheduler;
import com.serpenteando.cobra3d.util.LocaleHelper;
import com.serpenteando.cobra3d.util.NotificationHelper;
import com.serpenteando.cobra3d.util.PreferenceManager;

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

    // Email central do app (Kaio) — usado nos botões Fale Conosco e Reportar Bug
    private static final String EMAIL_CENTRAL = "suporteserpenteando@gmail.com";

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
        langRow.setOnClickListener(vista -> {
            showLanguageDialog();
        });

        // === SEÇÃO: NOTIFICAÇÕES ===
        // Abre dialog de configuração de notificações
        View notifRow = view.findViewById(R.id.rowNotification);
        ((ImageView) notifRow.findViewById(R.id.rowIcon)).setImageResource(R.drawable.ic_notification);
        ((TextView) notifRow.findViewById(R.id.rowTitle)).setText(R.string.settings_notifications);
        ((TextView) notifRow.findViewById(R.id.rowValue)).setText(R.string.settings_notifications_on);
        notifRow.findViewById(R.id.rowValue).setVisibility(View.VISIBLE);
        notifRow.setOnClickListener(vista -> {
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
        // Tocar na linha inteira também liga/desliga o switch
        darkModeRow.setOnClickListener(vista -> darkModeSwitch.toggle());

        // Linha Versão: só mostra o número, sem seta
        View versionRow = view.findViewById(R.id.rowVersion);
        ((ImageView) versionRow.findViewById(R.id.rowIcon)).setImageResource(R.drawable.ic_info);
        ((TextView) versionRow.findViewById(R.id.rowTitle)).setText(R.string.settings_version);
        ((TextView) versionRow.findViewById(R.id.rowValue)).setText(R.string.settings_version_name);
        versionRow.findViewById(R.id.rowValue).setVisibility(View.VISIBLE);
        versionRow.findViewById(R.id.rowChevron).setVisibility(View.GONE);

        // Linha Sobre: abre a tela Sobre
        View aboutRow = view.findViewById(R.id.rowAbout);
        ((ImageView) aboutRow.findViewById(R.id.rowIcon)).setImageResource(R.drawable.ic_person_silhouette);
        ((TextView) aboutRow.findViewById(R.id.rowTitle)).setText(R.string.settings_about);
        aboutRow.setOnClickListener(vista -> {
            Navigation.findNavController(vista).navigate(R.id.nav_about);
        });

        // Linha Créditos: abre o dialog de créditos
        View creditsRow = view.findViewById(R.id.rowCredits);
        ((ImageView) creditsRow.findViewById(R.id.rowIcon)).setImageResource(R.drawable.ic_trophy);
        ((TextView) creditsRow.findViewById(R.id.rowTitle)).setText(R.string.credits_title);
        creditsRow.setOnClickListener(vista -> {
            showCreditsDialog();
        });

        // [TEI Intents] Fale Conosco: abre o Gmail já com assunto e mensagem prontos
        View contactRow = view.findViewById(R.id.rowContact);
        ((ImageView) contactRow.findViewById(R.id.rowIcon)).setImageResource(R.drawable.ic_help);
        ((TextView) contactRow.findViewById(R.id.rowTitle)).setText(R.string.settings_contact);
        contactRow.setOnClickListener(vista -> abrirEmail(
                getString(R.string.contact_email_subject),
                getString(R.string.contact_email_body)));

        // Linha Reportar Bug: mesma ideia, com o roteiro do bug já escrito
        View bugRow = view.findViewById(R.id.rowBugReport);
        ((ImageView) bugRow.findViewById(R.id.rowIcon)).setImageResource(R.drawable.ic_bug_report);
        ((TextView) bugRow.findViewById(R.id.rowTitle)).setText(R.string.settings_report_bug);
        bugRow.setOnClickListener(vista -> abrirEmail(
                getString(R.string.bug_email_subject),
                getString(R.string.bug_email_body)));
    }

    // Abre o app de e-mail com destinatário, assunto e mensagem já preenchidos
    // Vai tudo na própria URI do mailto: porque o Gmail ignora o EXTRA_SUBJECT
    // no ACTION_SENDTO — era isso que deixava o campo da mensagem vazio (13/10)
    private void abrirEmail(String assunto, String corpo) {
        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:" + EMAIL_CENTRAL
                    + "?subject=" + Uri.encode(assunto)
                    + "&body=" + Uri.encode(corpo)));
            intent.putExtra(Intent.EXTRA_SUBJECT, assunto);
            intent.putExtra(Intent.EXTRA_TEXT, corpo);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), R.string.settings_no_email, Toast.LENGTH_SHORT).show();
        }
    }

    // [TEI Dialogs] Dialog de créditos com layout próprio

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
            if (closeBtn != null) closeBtn.setOnClickListener(vista -> dialog.dismiss());

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

            // Marca o idioma que está selecionado
            updateLangRow(langPt, currentLang.equals("pt"));
            updateLangRow(langEn, currentLang.equals("en"));
            updateLangRow(langEs, currentLang.equals("es"));

            langPt.setOnClickListener(vista -> {
                changeLanguage(LocaleHelper.LANG_PT);
                dialog.dismiss();
            });
            langEn.setOnClickListener(vista -> {
                changeLanguage(LocaleHelper.LANG_EN);
                dialog.dismiss();
            });
            langEs.setOnClickListener(vista -> {
                changeLanguage(LocaleHelper.LANG_ES);
                dialog.dismiss();
            });

            if (closeBtn != null) closeBtn.setOnClickListener(vista -> dialog.dismiss());
            dialog.show();

        } catch (Exception e) {
            Log.e("SettingsFragment", "Error showing language dialog", e);
            Toast.makeText(getContext(), getString(R.string.error_prefix) + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void updateLangRow(LinearLayout row, boolean selected) {
        if (row == null) return;
        row.setBackgroundResource(selected ? R.drawable.chip_active : R.drawable.chip_inactive);
        // Primeiro filho da linha = o texto do idioma
        // Texto branco nos dois estados (Kaio, 07/10): a linha do idioma agora é verde
        // como os botões, então o texto verde-claro não teria contraste. O selecionado
        // continua marcado pelo negrito e pelo ✓.
        if (row.getChildCount() > 0 && row.getChildAt(0) instanceof TextView) {
            TextView label = (TextView) row.getChildAt(0);
            label.setTextColor(ContextCompat.getColor(requireContext(), R.color.cascavel_on_button));
            label.setTypeface(null, selected ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        }
        // Segundo filho da linha = o ✓ de selecionado
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
                // Liga/desliga o alarme do lembrete de missões pendentes
                if (isChecked) {
                    AlarmScheduler.scheduleMissionsReminder(requireContext());
                } else {
                    AlarmScheduler.cancelMissionsReminder(requireContext());
                }
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
            if (saveBtn != null) saveBtn.setOnClickListener(vista -> dialog.dismiss());

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
