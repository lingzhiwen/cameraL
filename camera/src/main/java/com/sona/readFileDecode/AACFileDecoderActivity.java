package com.sona.readFileDecode;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.camera.R;


/**
 * 1、DVR音频测试Activity
 */
public class AACFileDecoderActivity extends AppCompatActivity {


    private ReadAACFileThread audioThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aactest);

    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.play_audio_test:
                if (audioThread == null) {
                    audioThread = new ReadAACFileThread();
                    audioThread.start();
                }
                break;
        }
    }
}