package com.simplexity.basictts;

import java.util.Locale;

public class VoiceInfo {
    private final String humanReadableName;
    private final String voiceId;
    private final Locale locale;
    private final boolean isNetworkRequired;
    private boolean isDefault;


    public VoiceInfo(String humanReadableName, String voiceId, Locale locale, boolean isNetworkRequired) {
        this.humanReadableName = humanReadableName;
        this.voiceId = voiceId;
        this.locale = locale;
        this.isNetworkRequired = isNetworkRequired;

    }

    @Override
    public String toString() {
        return "name=" + humanReadableName + ", id=" + voiceId + ", locale=" + locale;
    }

    public String getHumanReadableName() {
        return humanReadableName;
    }

    public String getVoiceId() {
        return voiceId;
    }

    public Locale getLocale() {
        return locale;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public boolean isNetworkRequired() {
        return isNetworkRequired;
    }
}
