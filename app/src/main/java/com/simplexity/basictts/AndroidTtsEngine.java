package com.simplexity.basictts;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;

import java.util.Locale;

public class AndroidTtsEngine {
    private TextToSpeech tts;
    private boolean initialized = false;
    private final Context context;
    private Locale language;

    public AndroidTtsEngine(Context context) {
        this.context = context;
        tts = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
                Voice voice = Util.getVoiceFromString(new ConfigManager(context).getVoice(), tts);
                if (voice != null) tts.setVoice(voice);
                initialized = true;
            }
        });
    }

    public boolean speak(String text, float rate, float pitch) {
        if (!initialized) return false;
        tts.setSpeechRate(rate);
        tts.setPitch(pitch);
        Voice voice = Util.getVoiceFromString(new ConfigManager(getContext()).getVoice(), tts);
        if (voice != null) tts.setVoice(voice);

        int result = tts.speak(text, TextToSpeech.QUEUE_ADD, null, Long.toString(System.currentTimeMillis()));
        return result == TextToSpeech.SUCCESS;
    }

    public boolean speak(String text) {
        if (!initialized) return false;
        ConfigManager configManager = new ConfigManager(getContext());
        float rate = configManager.getSpeed();
        float pitch = configManager.getPitch();
        tts.setSpeechRate(rate);
        tts.setPitch(pitch);
        Voice voice = Util.getVoiceFromString(new ConfigManager(getContext()).getVoice(), tts);
        if (voice != null) tts.setVoice(voice);

        int result = tts.speak(text, TextToSpeech.QUEUE_ADD, null, Long.toString(System.currentTimeMillis()));
        return result == TextToSpeech.SUCCESS;
    }

    public void shutdown() {
        if (tts != null) {
            tts.shutdown();
        }
    }

    public Context getContext() {
        return context;
    }

    public void setLanguage(Locale locale) {
        tts.setLanguage(locale);
    }

    public Locale getLanguage(){
        return language;
    }

    public void stop() {
        tts.stop();
    }

    public TextToSpeech getTts() {
        return tts;
    }
}
