package com.example.camera;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.decoder.ling.H264Decoder;
import com.ling.camera.ReadH264FileThread;
import com.ling.camera.VideoViewGl;
import com.ling.utils.H264FileUtils;
import com.ling.video.VideoGlSurfaceView;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private VideoGlSurfaceView videoGlSurfaceView;
    private static int FRAME_MAX_LEN = 300 * 1024;
    private VideoViewGl mVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btn = findViewById(R.id.btn);
        // videoGlSurfaceView = findViewById(R.id.videoGlSurfaceView);
        FrameLayout videoFrameView = (FrameLayout) findViewById(R.id.video_frame);
        mVideoView = new VideoViewGl(this);
        videoFrameView.addView(mVideoView, FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //H264Decoder.init();
                new ReadH264FileThread(MainActivity.this, mVideoView).start();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVideoView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVideoView.onPause();
    }

}