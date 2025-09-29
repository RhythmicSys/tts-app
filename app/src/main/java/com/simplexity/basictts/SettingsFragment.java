package com.simplexity.basictts;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreferenceCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class SettingsFragment extends PreferenceFragmentCompat implements TextToSpeech.OnInitListener {

    private static final String TAG = "SettingsFragment";

    private TextToSpeech tts;
    private ListPreference languagePreference;
    private ListPreference voicePreference;
    private SeekBarPreference pitchPreference;
    private SeekBarPreference speedPreference;
    private SwitchPreferenceCompat darkModePreference;

    private SharedPreferences sharedPreferences;

    // Stores VoiceInfo objects grouped by their language display name
    private final Map<String, List<VoiceInfo>> voicesByLanguage = new HashMap<>();

    static class VoiceInfo {
        String humanReadableName;
        String voiceId; // The actual ID for tts.setVoice()
        Locale locale;
        boolean isNetworkRequired;

        VoiceInfo(String humanReadableName, String voiceId, Locale locale, boolean isNetworkRequired) {
            this.humanReadableName = humanReadableName;
            this.voiceId = voiceId;
            this.locale = locale;
            this.isNetworkRequired = isNetworkRequired;
        }

        // Optional: Override toString for easier debugging if VoiceInfo objects are logged
        @Override
        public String toString() {
            return humanReadableName + " (" + voiceId + ")";
        }
    }


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());

        languagePreference = findPreference("selected_language_locale");
        voicePreference = findPreference("selected_voice_id");
        pitchPreference = findPreference("speech_pitch");
        speedPreference = findPreference("speech_speed");
        darkModePreference = findPreference("dark_mode_enabled");

        // Disable voice preference initially, it depends on language selection
        if (voicePreference != null) {
            voicePreference.setEnabled(false);
        }
        if (languagePreference != null) {
            languagePreference.setEnabled(false); // Disable until voices are loaded
        }


        // Initialize TTS to get voice list
        // Consider moving TTS initialization to Application class or a Singleton
        // if TTS is used extensively and needs to persist across activities.
        // For settings, initializing here is okay but ensure proper shutdown.
        tts = new TextToSpeech(requireContext(), this);

        setupListeners();
    }

    private void setupListeners() {
        if (languagePreference != null) {
            languagePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                Log.d(TAG, "Language selection changed to: " + newValue);
                if (newValue == null) {
                    disableVoicePreference();
                } else {
                    populateVoicePreference((String) newValue);
                }
                return true;
            });
        }

        if (voicePreference != null) {
            voicePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                Log.d(TAG, "Voice selection changed to: " + newValue);
                // Just need to save, no further UI update needed here based on this change
                return true;
            });
        }

        if (pitchPreference != null) {
            pitchPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                // Integer value from SeekBar (e.g., 50 to 200)
                // No immediate TTS action here, just save. Applied when TTS speaks.
                Log.d(TAG, "Pitch changed to: " + newValue);
                return true;
            });
        }

        if (speedPreference != null) {
            speedPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                // Integer value from SeekBar (e.g., 50 to 200)
                Log.d(TAG, "Speed changed to: " + newValue);
                return true;
            });
        }

        if (darkModePreference != null) {
            // Update summary based on initial value
            updateDarkModeSummary(sharedPreferences.getBoolean("dark_mode_enabled", false));

            darkModePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean isEnabled = (Boolean) newValue;
                Log.d(TAG, "Dark mode toggled to: " + isEnabled);
                if (isEnabled) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                updateDarkModeSummary(isEnabled);
                // The activity might need to be recreated for the theme change to fully apply
                // This typically happens automatically or you can force it if needed.
                return true;
            });
        }
    }

    private void updateDarkModeSummary(boolean isEnabled) {
        if (darkModePreference != null) {
            // This is automatically handled by summaryOn/summaryOff in XML for SwitchPreferenceCompat
            // If you needed more custom summary:
            // darkModePreference.setSummary(isEnabled ? "Dark mode is active" : "Light mode is active");
        }
    }


    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            Log.d(TAG, "TTS Initialized successfully.");
            loadAndProcessVoices();
        } else {
            Log.e(TAG, "TTS Initialization failed with status: " + status);
            // Handle TTS initialization failure (e.g., show a toast)
            if (languagePreference != null) languagePreference.setEnabled(false);
            disableVoicePreference();
        }
    }

    private void loadAndProcessVoices() {
        if (tts == null) {
            Log.e(TAG, "TTS engine is null in loadAndProcessVoices.");
            return;
        }

        voicesByLanguage.clear();
        Set<Voice> systemVoices = tts.getVoices();

        if (systemVoices == null || systemVoices.isEmpty()) {
            Log.w(TAG, "No TTS voices found on the system.");
            if (languagePreference != null) languagePreference.setEnabled(false);
            disableVoicePreference();
            return;
        }

        Log.d(TAG, "Found " + systemVoices.size() + " system voices.");

        Map<Locale, Integer> voiceCounterPerLocale = new HashMap<>();

        for (Voice voice : systemVoices) {
            Locale locale = voice.getLocale();
            if (locale == null || locale.getDisplayName().isEmpty()) {
                Log.w(TAG, "Voice found with null or empty locale: " + voice.getName());
                continue; // Skip voices with no usable locale
            }

            String langDisplayName = locale.getDisplayName(); // e.g., "English (United States)"

            voiceCounterPerLocale.putIfAbsent(locale, 1);
            int count = voiceCounterPerLocale.get(locale);

            String systemName = voice.getName();
            String gender = "";
            String variantName = "";
            boolean isNetwork = voice.isNetworkConnectionRequired();
            String type = isNetwork ? "Network" : "Local";

            if (systemName.toLowerCase().contains("male")) gender = "Male";
            else if (systemName.toLowerCase().contains("female")) gender = "Female";

            String[] parts = systemName.split("[#x-]");
            if (parts.length > 2) {
                for (int i = 2; i < parts.length; i++) {
                    String p = parts[i].toLowerCase();
                    if (!p.equals(locale.getLanguage().toLowerCase()) &&
                            !p.equals(locale.getCountry().toLowerCase()) &&
                            !p.equals("male") && !p.equals("female") &&
                            !p.equals("local") && !p.equals("network") &&
                            !p.matches("\\d+")) {
                        variantName = capitalizeFirstLetter(p);
                        break;
                    }
                }
            }
            StringBuilder humanNameBuilder = new StringBuilder();
            if (!variantName.isEmpty()) humanNameBuilder.append(variantName);
            else humanNameBuilder.append("Voice ").append(count);

            if (!gender.isEmpty()) humanNameBuilder.append(" (").append(gender);
            else humanNameBuilder.append(" ("); // Start parenthesis if no gender but type exists

            if (!gender.isEmpty() && !type.isEmpty()) humanNameBuilder.append(" - ");
            else if (gender.isEmpty() && type.isEmpty()) { // Neither gender nor type
                // Remove trailing parenthesis if it was added
                if(humanNameBuilder.toString().endsWith(" (")) {
                    humanNameBuilder.setLength(humanNameBuilder.length() - 2);
                }
            }


            if (!type.isEmpty()) humanNameBuilder.append(type);

            // Close parenthesis if we opened it
            if (!gender.isEmpty() || !type.isEmpty()){
                humanNameBuilder.append(")");
            }


            VoiceInfo voiceInfo = new VoiceInfo(humanNameBuilder.toString(), voice.getName(), locale, isNetwork);
            voicesByLanguage.computeIfAbsent(langDisplayName, k -> new ArrayList<>()).add(voiceInfo);
            voiceCounterPerLocale.put(locale, count + 1);
        }

        populateLanguagePreference();

        // Pre-select and populate voice preference if a language is already selected
        String currentSelectedLanguageLocaleString = sharedPreferences.getString("selected_language_locale", null);
        if (currentSelectedLanguageLocaleString != null && languagePreference.getEntries() != null && languagePreference.getEntries().length > 0) {
            // Ensure the language preference is actually set before trying to populate voices
            languagePreference.setValue(currentSelectedLanguageLocaleString); // This triggers its change listener if value is valid
            populateVoicePreference(currentSelectedLanguageLocaleString);
        } else if (languagePreference.getEntries() == null || languagePreference.getEntries().length == 0) {
            disableVoicePreference();
        }
    }


    private void populateLanguagePreference() {
        if (languagePreference == null) {
            Log.e(TAG, "LanguagePreference is null in populateLanguagePreference.");
            return;
        }

        if (voicesByLanguage.isEmpty()) {
            Log.w(TAG, "No voices grouped by language. Disabling language preference.");
            languagePreference.setEnabled(false);
            languagePreference.setEntries(new CharSequence[0]);
            languagePreference.setEntryValues(new CharSequence[0]);
            languagePreference.setValue(null);
            disableVoicePreference(); // Also disable voice preference
            return;
        }

        List<String> languageDisplayNames = new ArrayList<>(voicesByLanguage.keySet());
        Collections.sort(languageDisplayNames, String::compareToIgnoreCase);

        List<String> languageLocaleStrings = new ArrayList<>();
        for (String displayName : languageDisplayNames) {
            List<VoiceInfo> voicesInLang = voicesByLanguage.get(displayName);
            if (voicesInLang != null && !voicesInLang.isEmpty()) {
                languageLocaleStrings.add(voicesInLang.get(0).locale.toString()); // e.g., "en_US"
            } else {
                // This case should ideally not happen if voicesByLanguage is populated correctly
                Log.w(TAG, "Language display name '" + displayName + "' has no associated VoiceInfo list or it's empty.");
                languageLocaleStrings.add(""); // Placeholder, though ideally filter out such display names
            }
        }
        // Filter out any display names that didn't yield a valid locale string
        List<String> validDisplayNames = new ArrayList<>();
        List<String> validLocaleStrings = new ArrayList<>();
        for(int i=0; i<languageDisplayNames.size(); i++){
            if(!languageLocaleStrings.get(i).isEmpty()){
                validDisplayNames.add(languageDisplayNames.get(i));
                validLocaleStrings.add(languageLocaleStrings.get(i));
            }
        }


        if(validDisplayNames.isEmpty()){
            Log.w(TAG, "No valid languages to populate. Disabling language preference.");
            languagePreference.setEnabled(false);
            languagePreference.setEntries(new CharSequence[0]);
            languagePreference.setEntryValues(new CharSequence[0]);
            languagePreference.setValue(null);
            disableVoicePreference();
            return;
        }


        languagePreference.setEntries(validDisplayNames.toArray(new CharSequence[0]));
        languagePreference.setEntryValues(validLocaleStrings.toArray(new CharSequence[0]));
        languagePreference.setEnabled(true);

        String currentSelectedLanguage = sharedPreferences.getString("selected_language_locale", null);
        if (currentSelectedLanguage != null && validLocaleStrings.contains(currentSelectedLanguage)) {
            languagePreference.setValue(currentSelectedLanguage);
        } else {
            languagePreference.setValue(null); // Clear if previous selection no longer valid
            disableVoicePreference();
        }
        Log.d(TAG, "Language preference populated with " + validDisplayNames.size() + " languages.");
    }

    private void populateVoicePreference(String selectedLanguageLocaleString) {
        if (voicePreference == null) {
            Log.e(TAG, "VoicePreference is null in populateVoicePreference.");
            return;
        }
        if (selectedLanguageLocaleString == null || selectedLanguageLocaleString.isEmpty()) {
            Log.w(TAG, "Selected language locale string is null or empty. Disabling voice preference.");
            disableVoicePreference();
            return;
        }

        // Find the matching language display name for the given locale string
        String targetLanguageDisplayName = null;
        for (Map.Entry<String, List<VoiceInfo>> entry : voicesByLanguage.entrySet()) {
            if (!entry.getValue().isEmpty() && entry.getValue().get(0).locale.toString().equals(selectedLanguageLocaleString)) {
                targetLanguageDisplayName = entry.getKey();
                break;
            }
        }

        if (targetLanguageDisplayName == null || !voicesByLanguage.containsKey(targetLanguageDisplayName)) {
            Log.w(TAG, "No voices found for language locale: " + selectedLanguageLocaleString + ". Disabling voice preference.");
            disableVoicePreference();
            return;
        }

        List<VoiceInfo> voicesInSelectedLanguage = voicesByLanguage.get(targetLanguageDisplayName);
        if (voicesInSelectedLanguage == null || voicesInSelectedLanguage.isEmpty()) {
            Log.w(TAG, "Voice list for " + targetLanguageDisplayName + " is null or empty. Disabling voice preference.");
            disableVoicePreference();
            return;
        }

        List<String> voiceHumanNames = new ArrayList<>();
        List<String> voiceIds = new ArrayList<>();
        for (VoiceInfo vi : voicesInSelectedLanguage) {
            voiceHumanNames.add(vi.humanReadableName);
            voiceIds.add(vi.voiceId);
        }

        if (voiceHumanNames.isEmpty()) {
            Log.w(TAG, "No human-readable voice names found for " + targetLanguageDisplayName + ". Disabling voice preference.");
            disableVoicePreference();
            return;
        }

        voicePreference.setEntries(voiceHumanNames.toArray(new CharSequence[0]));
        voicePreference.setEntryValues(voiceIds.toArray(new CharSequence[0]));
        voicePreference.setEnabled(true);

        String currentVoiceId = sharedPreferences.getString("selected_voice_id", null);
        if (currentVoiceId != null && voiceIds.contains(currentVoiceId)) {
            voicePreference.setValue(currentVoiceId);
        } else {
            voicePreference.setValue(null); // Clear if previous selection invalid or no selection
        }
        Log.d(TAG, "Voice preference populated with " + voiceHumanNames.size() + " voices for " + targetLanguageDisplayName);
    }

    private void disableVoicePreference() {
        if (voicePreference != null) {
            voicePreference.setEnabled(false);
            voicePreference.setEntries(new CharSequence[0]);
            voicePreference.setEntryValues(new CharSequence[0]);
            voicePreference.setValue(null);
            Log.d(TAG, "Voice preference disabled.");
        }
    }


    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase(Locale.getDefault()) + str.substring(1).toLowerCase(Locale.getDefault());
    }

    @Override
    public void onDestroy() {
        if (tts != null) {
            Log.d(TAG, "Shutting down TTS engine.");
            tts.stop();
            tts.shutdown();
            tts = null; // Help GC
        }
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Re-check dark mode preference in case it was changed by system outside the app
        if (darkModePreference != null) {
            boolean currentDarkModeState = (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES);
            if(sharedPreferences.getBoolean("dark_mode_enabled", false) != currentDarkModeState) {
                // If shared pref is out of sync with actual mode (e.g. system change), update switch
                darkModePreference.setChecked(currentDarkModeState);
                sharedPreferences.edit().putBoolean("dark_mode_enabled", currentDarkModeState).apply();
            } else {
                darkModePreference.setChecked(sharedPreferences.getBoolean("dark_mode_enabled", false));
            }
            updateDarkModeSummary(darkModePreference.isChecked());

        }
    }
}
