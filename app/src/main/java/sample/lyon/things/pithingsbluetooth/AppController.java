package sample.lyon.things.pithingsbluetooth;

import android.app.Application;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

public class AppController extends Application {

    String TAG = AppController.class.getName();
    private static AppController appController;
    private TextToSpeech mTtsEngine;
    private static final String UTTERANCE_ID =
            "com.example.androidthings.bluetooth.audio.UTTERANCE_ID";
    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();
        appController = this;
        initTts();
    }

    public static synchronized AppController getInstance() {
        return appController;
    }

    private void initTts() {
        mTtsEngine = new TextToSpeech(AppController.this,
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if (status == TextToSpeech.SUCCESS) {
                            mTtsEngine.setLanguage(Locale.US);
                        } else {
                            Log.w(TAG, "Could not open TTS Engine (onInit status=" + status
                                    + "). Ignoring text to speech");
                            mTtsEngine = null;
                        }
                    }
                });
    }
    public void speak(String utterance) {
        Log.i(TAG, utterance);
        if (mTtsEngine != null) {
            mTtsEngine.speak(utterance, TextToSpeech.QUEUE_ADD, null, UTTERANCE_ID);
        }
    }
}
