package com.simplexity.basictts;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TtsChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MessageAdapter adapter;
    private EditText inputText;
    private Button sendButton;
    private ImageButton settingsButton;

    private List<String> messages = new ArrayList<>();
    private AndroidTtsEngine tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tts_chat);
        // Init TTS
        tts = new AndroidTtsEngine(this);
        tts.setLanguage(Locale.US);

        // RecyclerView setup
        recyclerView = findViewById(R.id.chatRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MessageAdapter(messages);
        recyclerView.setAdapter(adapter);


        // Input + buttons
        inputText = findViewById(R.id.edit_tts_message);
        sendButton = findViewById(R.id.tts_send_button);
        settingsButton = findViewById(R.id.settings_button);

        // Send button logic
        sendButton.setOnClickListener(v -> sendMessage());
        settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(TtsChatActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // Enter key "send"
        inputText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });

        // Auto-scroll when keyboard shows
        recyclerView.addOnLayoutChangeListener((v, left, top, right, bottom,
                                                oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom) {
                recyclerView.postDelayed(() ->
                        recyclerView.scrollToPosition(adapter.getItemCount() - 1), 100);
            }
        });
    }

    private void sendMessage() {
        String text = inputText.getText().toString().trim();
        if (text.isEmpty()) return;

        // Add message to list
        messages.add(text);
        adapter.notifyItemInserted(messages.size() - 1);
        recyclerView.scrollToPosition(messages.size() - 1);

        // Speak it
        tts.speak(text);

        // Clear input
        inputText.setText("");
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

}
