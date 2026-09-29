package com.ratlab;
import android.content.Context;
import android.speech.tts.TextToSpeech;
import java.util.Locale;
public class TtsControl {
    public static String speak(Context ctx, String text) {
        try {
            TextToSpeech tts = new TextToSpeech(ctx, status -> {
                if (status == TextToSpeech.SUCCESS) {}
            });
            tts.setLanguage(new Locale("id", "ID"));
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "rat");
            return "TTS: " + text;
        } catch (Exception e) { return "Error: " + e.getMessage(); }
    }
}
