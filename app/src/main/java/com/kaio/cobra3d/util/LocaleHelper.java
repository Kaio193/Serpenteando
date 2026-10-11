package com.kaio.cobra3d.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

/**
 * Gerencia a troca de idioma do app.
 * Salva a preferência do usuário e aplica o Locale em toda a Activity.
 */
public class LocaleHelper {

    private static final String PREFS_NAME = "serpenteando_prefs";
    private static final String PREF_LANGUAGE = "app_language";

    // Códigos de idioma suportados
    public static final String LANG_PT = "pt";
    public static final String LANG_EN = "en";
    public static final String LANG_ES = "es";

    /**
     * Aplica o idioma salvo ao contexto da aplicação.
     * Deve ser chamado no onCreate() de cada Activity, ANTES de setContentView().
     */
    public static Context onAttach(Context context) {
        String lang = getPersistedLanguage(context);
        return setLocale(context, lang);
    }

    /**
     * Troca o idioma e salva a preferência.
     * Após chamar isso, a Activity precisa ser recreada pra aplicar.
     */
    public static Context setLocale(Context context, String languageCode) {
        persistLanguage(context, languageCode);

        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        config.setLocale(locale);

        // updateConfiguration está deprecated mas é o mais compatível
        resources.updateConfiguration(config, resources.getDisplayMetrics());

        return context.createConfigurationContext(config);
    }

    /**
     * Retorna o código do idioma atual ("pt", "en" ou "es").
     */
    public static String getLanguage(Context context) {
        return getPersistedLanguage(context);
    }

    /**
     * Retorna o nome legível do idioma atual.
     */
    public static String getLanguageName(Context context) {
        switch (getLanguage(context)) {
            case LANG_EN: return "English";
            case LANG_ES: return "Español";
            default: return "Português";
        }
    }

    /**
     * Retorna a sigla do idioma (sem emojis).
     */
    public static String getLanguageFlag(Context context) {
        switch (getLanguage(context)) {
            case LANG_EN: return "EN";
            case LANG_ES: return "ES";
            default: return "PT";
        }
    }

    private static String getPersistedLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(PREF_LANGUAGE, LANG_PT);
    }

    private static void persistLanguage(Context context, String languageCode) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(PREF_LANGUAGE, languageCode).apply();
    }
}
