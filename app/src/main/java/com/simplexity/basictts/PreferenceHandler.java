package com.simplexity.basictts;

import android.os.Bundle;
import android.speech.tts.Voice;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreferenceCompat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class PreferenceHandler extends PreferenceFragmentCompat {

    private TtsManager ttsManager;
    private ListPreference languageOptions, voiceOptions, networkOptions;
    private SeekBarPreference speechPitch, speechSpeed;
    private float defaultPitch, defaultSpeed;
    private Voice defaultVoice;
    private Button testButton;
    private SwitchPreferenceCompat darkMode;
    private final HashMap<String, Set<VoiceInfo>> languageVoices = new HashMap<>();

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        ttsManager = new TtsManager(getContext());

        languageOptions = findPreference("selected_language_locale");
        voiceOptions = findPreference("selected_voice_id");
        networkOptions = findPreference("network_options");
        darkMode = findPreference("dark_mode_enabled");
        speechPitch = findPreference("speech_pitch");
        speechSpeed = findPreference("speech_speed");

        if (voiceOptions != null) {
            voiceOptions.setEnabled(false);
        }
        if (languageOptions != null) {
            languageOptions.setEnabled(false);
        }
        if (networkOptions != null) {
            networkOptions.setEnabled(false);
        }
        loadAllVoices();
        setupListeners();


    }

    private void setupListeners() {
        if (languageOptions != null) {
            languageOptions.setOnPreferenceChangeListener((preference, newValue) -> {
                Log.d("PreferenceHandler", "Language changed to: " + newValue);
                if (newValue != null) {
                    populateLanguageVoices(new Locale(newValue.toString()));
                }
                return true;
            });
        }
    }

    private void loadAllVoices() {
        Set<Voice> systemVoices = ttsManager.getTextToSpeech().getVoices();
        for (Voice voice : systemVoices) {
            Locale locale = voice.getLocale();
            VoiceInfo voiceInfo = new VoiceInfo(voice.getName(), voice.getName(), locale,
                    voice.isNetworkConnectionRequired());
            languageVoices.getOrDefault(locale.toString(), new HashSet<>()).add(voiceInfo);
        }
    }


    private void populateLanguageVoices(Locale locale) {
        if (locale == null) {
            Log.d("PreferenceHandler", "Locale is null");
            return;
        }
        Log.d("PreferenceHandler", "Locale: " + locale.toString());
        Set<VoiceInfo> voices = languageVoices.get(locale.toString());
        if (voices == null) {
            Log.d("PreferenceHandler", "No voices found for locale: " + locale.toString());
            return;
        }
        Log.d("PreferenceHandler", "Voices found: " + voices.toString());
        voiceOptions.setEntries(voices.stream().map(VoiceInfo::getHumanReadableName).toArray(String[]::new));
        voiceOptions.setEntryValues(voices.stream().map(VoiceInfo::getVoiceId).toArray(String[]::new));
        voiceOptions.setEnabled(true);
    }


}
