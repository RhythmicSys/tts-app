# References
[Custom speech/pronunciation sequences](https://developer.android.com/reference/android/speech/tts/TextToSpeech#addSpeech(java.lang.CharSequence,%20java.io.File))
[Save to file](https://developer.android.com/reference/android/speech/tts/TextToSpeech#synthesizeToFile(java.lang.CharSequence,%20android.os.Bundle,%20android.os.ParcelFileDescriptor,%20java.lang.String))
    Possibly useful for expansion into more conventional AAC-type situations where buttons are saved to specific words/phrases
[Localizations info, limitations](https://developer.android.com/reference/android/speech/tts/TextToSpeechService)
[Voice info, getters](https://developer.android.com/reference/android/speech/tts/Voice#getFeatures())
[Speak method, literally how to make the tts say the thing](https://developer.android.com/reference/android/speech/tts/TextToSpeech#speak(java.lang.CharSequence,%20int,%20android.os.Bundle,%20java.lang.String))
    At some point, ideally, there would be a way to pause the playback, as a main issue with TTS is that it does not have the ability to pause when speaking like a normal conversation, it's an insane struggle to figure out a good point in the convo, so even having it load and have a play button would be useful. Would also probably need 'save to file', not sure how to handle that info on an app like this

[Keyboard accessibility](https://developer.android.com/reference/android/accessibilityservice/AccessibilityService.SoftKeyboardController.OnShowModeChangedListener)
    This is likely when the keyboard is changed to floating type or something similar, need to find a good solution for input display on that

Bluetooth: Not sure if anything with that is necessary, since I am just playing with the normal audio handler

# Config Stuff

[Preferences](https://developer.android.com/reference/androidx/preference/package-summary)
[Preference Fragment Compat](https://developer.android.com/reference/androidx/preference/PreferenceFragmentCompat#summary)
[Preference Screen](https://developer.android.com/reference/androidx/preference/PreferenceScreen)

