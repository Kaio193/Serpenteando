package com.kaio.cobra3d.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import androidx.appcompat.widget.SwitchCompat;
import com.kaio.cobra3d.R;
import com.kaio.cobra3d.util.PreferenceManager;
import com.kaio.cobra3d.util.TTSHelper;

public class SettingsFragment extends Fragment {

    private PreferenceManager preferenceManager;
    private TTSHelper ttsHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        preferenceManager = new PreferenceManager(requireContext());
        ttsHelper = new TTSHelper(requireContext());

        // Dark Mode switch
        View darkModeRow = view.findViewById(R.id.rowDarkMode);
        TextView darkModeTitle = darkModeRow.findViewById(R.id.rowTitle);
        SwitchCompat darkModeSwitch = darkModeRow.findViewById(R.id.rowSwitch);
        darkModeTitle.setText("Modo Escuro");
        darkModeSwitch.setChecked(preferenceManager.isDarkModeEnabled());
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferenceManager.setDarkModeEnabled(isChecked);
            AppCompatDelegate.setDefaultNightMode(
                isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        // Voice Mode switch
        View voiceRow = view.findViewById(R.id.rowVoiceMode);
        TextView voiceTitle = voiceRow.findViewById(R.id.rowTitle);
        SwitchCompat voiceSwitch = voiceRow.findViewById(R.id.rowSwitch);
        voiceTitle.setText("Modo Voz");
        voiceSwitch.setChecked(preferenceManager.isVoiceEnabled());
        voiceSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferenceManager.setVoiceEnabled(isChecked);
            String message = isChecked ? "Modo voz ativado" : "Modo voz desativado";
            if (isAdded() && getContext() != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
            if (isChecked) {
                ttsHelper.speak("Modo voz ativado. As informações serão lidas em voz alta.");
            } else {
                ttsHelper.stop();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (ttsHelper != null) {
            ttsHelper.shutdown();
            ttsHelper = null;
        }
        preferenceManager = null;
    }
}
