package com.sona.readFileDecode;

import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.camera.R;

public class H264FileDecodeActivity extends AppCompatActivity {

    //SurfaceView
    private SurfaceView playSurface;
    private SurfaceHolder holder;
    //解码器
    private MediaCodecUtil codecUtil;
    //读取文件解码线程
    private ReadH264FileThread thread;
    //文件路径
    private String path = Environment.getExternalStorageDirectory().toString() + "/Hao/test.h264";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_h264_file_decodec);
        initSurface();
    }

    //初始化播放相关
    private void initSurface() {
        playSurface = (SurfaceView) findViewById(R.id.play_surface);
        holder = playSurface.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (codecUtil == null) {
                    codecUtil = new MediaCodecUtil(holder);
                    codecUtil.startCodec();
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (codecUtil != null) {
                    codecUtil.stopCodec();
                    codecUtil = null;
                }
            }
        });
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.play:
                if (thread == null) {
                    thread = new ReadH264FileThread(codecUtil, path,H264FileDecodeActivity.this);
                    thread.start();
                }
                break;
        }
    }
}
