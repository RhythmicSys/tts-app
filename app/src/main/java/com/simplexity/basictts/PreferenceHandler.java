package com.simplexity.basictts;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreferenceCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class PreferenceHandler extends PreferenceFragmentCompat {

    private TextToSpeech textToSpeech;
    private ListPreference languagePreference, voicePreference, networkPreference;
    private SeekBarPreference pitchPreference, speedPreference;
    private float defaultPitch, defaultSpeed;
    private Voice defaultVoice;
    private Button testButton;
    private SwitchPreferenceCompat darkMode;
    private final HashMap<String, Set<VoiceInfo>> languageVoices = new HashMap<>();
    private final HashMap<String, Locale> localeOptions = new HashMap<>();

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        textToSpeech = new TextToSpeech(getContext(), onTtsInitialized());

        languagePreference = findPreference("selected_language_locale");
        voicePreference = findPreference("selected_voice_id");
        networkPreference = findPreference("network_options");
        darkMode = findPreference("dark_mode_enabled");
        pitchPreference = findPreference("speech_pitch");
        speedPreference = findPreference("speech_speed");

        if (voicePreference != null) {
            voicePreference.setEnabled(true);
        }
        if (languagePreference != null) {
            languagePreference.setEnabled(true);
        }
        if (networkPreference != null) {
            networkPreference.setEnabled(false);
        }
        setupListeners();


    }

    private void setupListeners() {
        if (languagePreference != null) {
            languagePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                Log.d("PreferenceHandler", "Language changed to: " + newValue);
                if (newValue != null) {
                    Locale selected = Locale.forLanguageTag(newValue.toString());
                    populateLanguageVoices(selected);
                    voicePreference.setValue(voicePreference.getEntries()[0].toString());
                }
                return true;
            });
        }
        if (voicePreference != null) {
            voicePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                Log.d("PreferenceHandler", "Voice changed to: " + newValue);
                return true;
            });
        }
        if (networkPreference != null) {
            networkPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                Log.d("PreferenceHandler", "Network changed to: " + newValue);
                return true;
            });
        }
        if (darkMode != null) {
            darkMode.setOnPreferenceChangeListener((preference, newValue) -> {
                if (darkMode.isChecked()) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                Log.d("PreferenceHandler", "Dark mode changed to: " + newValue);
                return true;
            });
        }
        if (pitchPreference != null) {
            pitchPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                Log.d("PreferenceHandler", "Pitch changed to: " + newValue);
                return true;
            });
        }
        if (speedPreference != null) {
            speedPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                Log.d("PreferenceHandler", "Speed changed to: " + newValue);
                return true;
            });
        }

    }

    private void loadAllVoices() {
        if (textToSpeech == null) {
            Log.d("PreferenceHandler", "TextToSpeech is null");
            return;
        }
        Log.d("PreferenceHandler", "Loading all voices");

        for (Voice voice : textToSpeech.getVoices()) {
            if (voice == null) {
                Log.d("PreferenceHandler", "Voice is null");
                continue;
            }
            Locale locale = voice.getLocale();
            if (locale == null) {
                Log.d("PreferenceHandler", "Locale is null");
                continue;
            }
            Log.d("PreferenceHandler", "Locale: " + locale);
            VoiceInfo voiceInfo = new VoiceInfo(voice.getName(), voice.getName(), locale,
                    voice.isNetworkConnectionRequired());
            Log.d("PreferenceHandler", "Voice: " + voiceInfo);
            Set<VoiceInfo> currentVoices;
            if (languageVoices.containsKey(locale.toLanguageTag())) {
                currentVoices = languageVoices.get(locale.toLanguageTag());
            } else {
                currentVoices = new HashSet<>();
            }
            currentVoices.add(voiceInfo);
            languageVoices.put(locale.toLanguageTag(), currentVoices);
        }
        Log.d("PreferenceHandler", "All voices loaded");
        Log.d("PreferenceHandler", "Language voices: " + languageVoices);
    }

    private void populateLocaleList() {
        if (textToSpeech == null) {
            Log.d("PreferenceHandler", "TextToSpeech is null");
            return;
        }
        Log.d("PreferenceHandler", "Populating locale list");
        Set<Locale> locales = textToSpeech.getAvailableLanguages();
        if (locales == null) {
            Log.d("PreferenceHandler", "No locales found");
            return;
        }
        Log.d("PreferenceHandler", "Locales found: " + locales);
        List<Locale> sortedLocales = new ArrayList<>(locales);
        sortedLocales.sort(Comparator.comparing(Locale::getDisplayName));

        String[] entries = sortedLocales.stream()
                .map(Locale::getDisplayName)
                .toArray(String[]::new);

        String[] entryValues = sortedLocales.stream()
                .map(Locale::toLanguageTag)
                .toArray(String[]::new);

        languagePreference.setEntries(entries);
        languagePreference.setEntryValues(entryValues);
        languagePreference.setEnabled(true);
        String currentValue = languagePreference.getValue();
        if (currentValue == null || !Arrays.asList(entryValues).contains(currentValue)) {
            String fallback = Locale.getDefault().toLanguageTag();
            languagePreference.setValue(fallback);
            currentValue = fallback;
        }
        Log.d("PreferenceHandler", "Current value: " + currentValue);
        populateLanguageVoices(Locale.forLanguageTag(currentValue));
        Log.d("PreferenceHandler", "Locale list populated");
    }


    private void populateLanguageVoices(Locale locale) {
        if (locale == null) {
            Log.d("PreferenceHandler", "Locale is null");
            return;
        }
        Log.d("PreferenceHandler", "Locale: " + locale);
        Set<VoiceInfo> voices = languageVoices.get(locale.toLanguageTag());
        Log.d("PreferenceHandler", "Voices: " + voices);
        Log.d("PreferenceHandler", "Voice preference: " + voicePreference);
        Log.d("PreferenceHandler", "Language Voices: " + languageVoices);
        if (voices == null) {
            Log.d("PreferenceHandler", "No voices found for locale: " + locale);
            return;
        }
        Log.d("PreferenceHandler", "Voices found: " + voices);
        List<VoiceInfo> sortedVoices = new ArrayList<>(voices);
        sortedVoices.sort(Comparator.comparing(VoiceInfo::getHumanReadableName, String.CASE_INSENSITIVE_ORDER));

        String[] entries = sortedVoices.stream()
                .map(VoiceInfo::getHumanReadableName)
                .toArray(String[]::new);

        String[] entryValues = sortedVoices.stream()
                .map(VoiceInfo::getVoiceId)
                .toArray(String[]::new);

        voicePreference.setEntryValues(entryValues);
        voicePreference.setEntries(entries);
        String currentValue = voicePreference.getValue();
        if (currentValue == null || !Arrays.asList(entryValues).contains(currentValue)) {
            String fallback = entryValues[0];
            voicePreference.setValue(fallback);
        }
        voicePreference.setEnabled(true);

    }


    private TextToSpeech.OnInitListener onTtsInitialized() {
        return (status -> {
            if (status == TextToSpeech.SUCCESS) {
                loadAllVoices();
                populateLocaleList();
                Log.d("PreferenceHandler", "TextToSpeech initialization successful");
            } else {
                Log.d("PreferenceHandler", "TextToSpeech initialization failed");
            }
        });

    }

}
