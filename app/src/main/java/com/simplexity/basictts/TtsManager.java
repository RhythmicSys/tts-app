package com.simplexity.basictts;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;

public class TtsManager {

    private final TextToSpeech textToSpeech;
    private Voice defaultVoice;
    private float defaultPitch;
    private float defaultSpeed;
    private boolean isInitialized;


    public TtsManager(Voice defaultVoice, float defaultPitch, float defaultSpeed,
                      Context context) {
        this.defaultVoice = defaultVoice;
        this.defaultPitch = defaultPitch;
        this.defaultSpeed = defaultSpeed;
        textToSpeech = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true;
            }
        });
        textToSpeech.setPitch(defaultPitch);
        textToSpeech.setSpeechRate(defaultSpeed);
        textToSpeech.setVoice(defaultVoice);
    }

    public TtsManager(Context context) {
        textToSpeech = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true;
            }
        });

    }


    public Voice getDefaultVoice() {
        return defaultVoice;
    }

    public void setDefaultVoice(Voice defaultVoice) {
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

    public void shutdown() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}
