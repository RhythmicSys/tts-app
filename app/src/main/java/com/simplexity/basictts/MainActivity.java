package com.simplexity.basictts;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private Log logger;

    private RecyclerView recyclerView;
    private EditText textBox;
    private Button sendButton;
    private ImageButton settingsButton;
    private MessageAdapter messageAdapter;
    private TtsManager ttsManager;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener;

    private List<String> messages = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tts_chat);
        setupScreenStuff();
        loadTtsManagerFromPreferences();
        setupPreferenceListener();
    }


    private void setupScreenStuff() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        recyclerView = findViewById(R.id.chat_recycler_view);
        textBox = findViewById(R.id.edit_tts_message);
        sendButton = findViewById(R.id.tts_send_button);
        settingsButton = findViewById(R.id.settings_button);
        messageAdapter = new MessageAdapter(messages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);
        sendButton.setOnClickListener(v -> sendMessage());

        textBox.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });

        settingsButton.setOnClickListener(v -> {
            Log.d("MainActivity", "Settings button clicked");
            startActivity(new Intent(this, SettingsActivity.class));
        });

    }

    private void setupPreferenceListener(){
        sharedPreferenceChangeListener = (sharedPreferences, key) -> {
            if (key == null) return;
            if (key.equals("selected_voice_id") ||
                    key.equals("speech_pitch") ||
                    key.equals("speech_speed")) {
                loadTtsManagerFromPreferences();
            }
        };
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }

    private void loadTtsManagerFromPreferences() {
        if (ttsManager != null) {
            ttsManager.shutdown();
        }
        String voiceId = sharedPreferences.getString("selected_voice_id", null);
        float pitch = sharedPreferences.getInt("speech_pitch", 100) / 100f;
        float speed = sharedPreferences.getInt("speech_speed", 100) / 100f;
        Log.d("MainActivity", "Loading TTS manager from preferences");
        Log.d("MainActivity", "Voice ID: " + voiceId);
        Log.d("MainActivity", "Pitch: " + pitch);
        Log.d("MainActivity", "Speed: " + speed);
        if (voiceId != null) {
            ttsManager = new TtsManager(voiceId, pitch, speed, this);
        } else {
            ttsManager = new TtsManager(this);
        }

    }


    private void sendMessage() {
        ttsManager.sendMessage(textBox.getText().toString().trim());
        messages.add(textBox.getText().toString().trim());
        messageAdapter.notifyItemInserted(messages.size() - 1);
        recyclerView.scrollToPosition(messages.size() - 1);
        textBox.setText("");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ttsManager != null) {
            ttsManager.shutdown();
        }
    }

}
