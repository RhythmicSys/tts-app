package com.simplexity.basictts;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;

import java.util.Set;

public class TtsManager {

    private TextToSpeech textToSpeech;
    private String defaultVoice;
    private float defaultPitch;
    private float defaultSpeed;
    private boolean isInitialized;


    public TtsManager(String defaultVoice, float defaultPitch, float defaultSpeed,
                      Context context) {
        this.defaultVoice = defaultVoice;
        this.defaultPitch = defaultPitch;
        this.defaultSpeed = defaultSpeed;
        textToSpeech = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true;
                Voice voice = getVoiceFromString(defaultVoice);
                textToSpeech.setPitch(defaultPitch);
                textToSpeech.setSpeechRate(defaultSpeed);
                if (voice != null) textToSpeech.setVoice(voice);
            }
        });
    }

    public TtsManager(Context context) {
        textToSpeech = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true;
            }
        });

    }


    public String getDefaultVoice() {
        return defaultVoice;
    }

    public void setDefaultVoice(String defaultVoice) {
        this.defaultVoice = defaultVoice;
    }

    public float getDefaultPitch() {
        return defaultPitch;
    }

    public void setDefaultPitch(float defaultPitch) {
        this.defaultPitch = defaultPitch;
    }

    public float getDefaultSpeed() {
        return defaultSpeed;
    }

    public void setDefaultSpeed(float defaultSpeed) {
        this.defaultSpeed = defaultSpeed;
    }

    public void sendMessage(String message){
        if (isInitialized) {
            textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    public void sendMessage(String message, Voice voice, float pitch, float speed) {
        if (isInitialized) {
            textToSpeech.setVoice(voice);
            textToSpeech.setPitch(pitch);
            textToSpeech.setSpeechRate(speed);
            textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    public TextToSpeech getTextToSpeech() {
        return textToSpeech;
    }

    private Voice getVoiceFromString(String string) {
        if (textToSpeech == null) {
            Log.d("TtsManager", "TextToSpeech is null");
            return null;
        }
        if (string == null) {
            Log.d("TtsManager", "String is null");
            return null;
        }
        Set<Voice> voices = textToSpeech.getVoices();
        if (voices == null) {
            Log.d("TtsManager", "Voices are null");
            return null;
        }
        for (Voice voice : textToSpeech.getVoices()) {
            if (voice.getName().equals(string)) {
                Log.d("TtsManager", "Voice found: " + voice.getName());
                return voice;
            }
        }
        return null;
    }

    public void shutdown() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}
