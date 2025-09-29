package com.simplexity.basictts;

import android.content.Context;
import android.content.SharedPreferences;

public class ConfigManager {
    private final SharedPreferences prefs;

    public ConfigManager(Context context) {
        prefs = context.getSharedPreferences("tts_prefs", Context.MODE_PRIVATE);
    }

    public void setPitch(float pitch) {
        prefs.edit().putFloat("pitch", pitch).apply();
    }

    public void setSpeed(float speed) {
        prefs.edit().putFloat("speed", speed).apply();
    }

    public float getPitch() {
        return prefs.getFloat("pitch", 1.0f);
    }

    public float getSpeed() {
        return prefs.getFloat("speed", 1.0f);
    }

    public void setVoice(String voiceName) {
        prefs.edit().putString("voice", voiceName).apply();
    }

    public String getVoice() {
        return prefs.getString("voice", null);
    }

    public void setDarkMode(boolean enabled) {
        prefs.edit().putBoolean("dark_mode", enabled).apply();
    }

    public boolean isDarkMode() {
        return prefs.getBoolean("dark_mode", true);
    }
}
