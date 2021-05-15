package com.sona.test;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.camera.R;
import com.sona.readFileDecode.AACFileDecoderActivity;
import com.sona.readFileDecode.H264FileDecodeActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_sona);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play_video:
                Intent i = new Intent(this, H264FileDecodeActivity.class);
                startActivity(i);
                break;
            case R.id.play_audio:
                Intent i1 = new Intent(this, AACFileDecoderActivity.class);
                startActivity(i1);
                break;
        }
    }
}
