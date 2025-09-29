package com.simplexity.basictts;

import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;

public class Util {

    public static Voice getVoiceFromString(String string, TextToSpeech textToSpeech) {
        if (string == null) return null;
        for (Voice voice : textToSpeech.getVoices()) {
            if (voice.getName().equals(string)) {
                return voice;
            }
        }
        return null;
    }
}
