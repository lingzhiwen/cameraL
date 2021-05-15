//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.ling.video;

import android.content.Context;
import android.opengl.GLES20;
import android.util.AttributeSet;
import android.util.Log;

import com.decoder.ling.H264Decoder;

public class VideoGlSurfaceViewFFMPEG extends VideoGlSurfaceView {
    private static final String TAG = "VideoFFMPEG";
    YUVFilter mYUVFilter;
    Photo mPhoto;
    int mWidth;
    int mHeight;
    int[] mYUVTextures;
    volatile boolean mInitialed = false;
    private H264Decoder h264Decoder;

    public VideoGlSurfaceViewFFMPEG(Context context, AttributeSet attrs, HardDecodeExceptionCallback callback) {
        super(context, attrs, callback);
        h264Decoder = new H264Decoder();
    }

    protected void initial() {
        super.initial();
        this.mYUVFilter = new YUVFilter(this.getContext());
        this.mYUVFilter.initial();
        this.mYUVTextures = new int[3];
        GLES20.glGenTextures(this.mYUVTextures.length, this.mYUVTextures, 0);
        this.mYUVFilter.setYuvTextures(this.mYUVTextures);
        this.mInitialed = true;
    }

    protected void release() {
        super.release();
        this.mInitialed = false;
        h264Decoder.release();
        if (this.mYUVFilter != null) {
            this.mYUVFilter.release();
            this.mYUVFilter = null;
            GLES20.glDeleteTextures(this.mYUVTextures.length, this.mYUVTextures, 0);
        }

        if (this.mPhoto != null) {
            this.mPhoto.clear();
            this.mPhoto = null;
        }

    }

    public void drawFrame() {
        super.drawFrame();
        Log.e("TAG","h264Decoder decode111...");
        if (this.mInitialed) {
            VideoFrame frame = (VideoFrame)this.mAVFrameQueue.poll();
            if (frame != null && frame.data != null) {
                Log.e("TAG","h264Decoder decode222...");
                long lastTime = System.currentTimeMillis();
                if (frame.width != this.mWidth || frame.height != this.mHeight) {
                    this.mWidth = frame.width;
                    this.mHeight = frame.height;
                    h264Decoder.release();
                    h264Decoder.init();
                }
                Log.e("TAG","h264Decoder decode333...");
                if (h264Decoder.decode(frame.data, frame.data.length, frame.timeStamp)) {
                    int ret = h264Decoder.toTexture(this.mYUVTextures[0], this.mYUVTextures[1], this.mYUVTextures[2]);
                    if (ret < 0) {
                        return;
                    }
                    Log.e("TAG",h264Decoder.getWidth()+"<< >>"+h264Decoder.getHeight());
                    if (this.mPhoto == null) {
                        this.mPhoto = Photo.create(h264Decoder.getWidth(), h264Decoder.getHeight());
                    } else {
                        this.mPhoto.updateSize(h264Decoder.getWidth(), h264Decoder.getHeight());
                    }

                    this.mYUVFilter.process((Photo)null, this.mPhoto);
                    Photo dst = this.appFilter(this.mPhoto);
                    RendererUtils.checkGlError("process");
                    this.setPhoto(dst);
                    RendererUtils.checkGlError("setPhoto");
                }

                if (this.mAVFrameQueue.size() > 0) {
                    this.requestRender();
                }

                long decodeOneFrameMilliseconds = System.currentTimeMillis() - lastTime;
                this.onDecodeTime(decodeOneFrameMilliseconds);
            }
        }
    }
}
