//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.ling.video;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.media.MediaCodec;
import android.media.MediaCrypto;
import android.media.MediaFormat;
import android.media.MediaCodec.BufferInfo;
import android.opengl.GLES20;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

@TargetApi(16)
public class VideoGlSurfaceViewGPU extends VideoGlSurfaceView implements OnFrameAvailableListener {
    private static final String TAG = "VideoGPU";
    Photo mPhoto;
    Photo mTexturePhoto;
    int mSurfaceTextureId;
    volatile boolean updateSurface = false;
    GlslFilter mTextureFilter;
    float[] mTextureMatrix = new float[16];
    volatile VideoGlSurfaceViewGPU.VideoDecodeThread mVideoDecodeThread;
    AndroidH264DecoderUtil.DecoderProperties mDecoderProperties;

    public VideoGlSurfaceViewGPU(Context context, AttributeSet attrs, AndroidH264DecoderUtil.DecoderProperties decoderProperties, HardDecodeExceptionCallback callback) {
        super(context, attrs, callback);
        this.mDecoderProperties = decoderProperties;
    }

    protected void initial() {
        super.initial();
        this.mTextureFilter = new GlslFilter(this.getContext());
        this.mTextureFilter.setType(36197);
        this.mTextureFilter.initial();
        this.mSurfaceTextureId = RendererUtils.createTexture();
        GLES20.glBindTexture(36197, this.mSurfaceTextureId);
        RendererUtils.checkGlError("glBindTexture mTextureID");
        GLES20.glTexParameterf(36197, 10241, 9728.0F);
        GLES20.glTexParameterf(36197, 10240, 9729.0F);
        GLES20.glTexParameteri(36197, 10242, 33071);
        GLES20.glTexParameteri(36197, 10243, 33071);
        this.updateSurface = false;
        RendererUtils.checkGlError("surfaceCreated");
        this.mVideoDecodeThread = new VideoGlSurfaceViewGPU.VideoDecodeThread(this, this.mDecoderProperties);
        this.mVideoDecodeThread.start();
    }

    protected void release() {
        super.release();
        if (this.mVideoDecodeThread != null) {
            this.mVideoDecodeThread.stopThreadAsyn();
            this.mVideoDecodeThread = null;
        }

        RendererUtils.clearTexture(this.mSurfaceTextureId);
        this.mTextureFilter.release();
        if (this.mPhoto != null) {
            this.mPhoto.clear();
            this.mPhoto = null;
        }

    }

    public void drawFrame() {
        super.drawFrame();
        if (this.mVideoDecodeThread != null) {
            int videoWith = this.mVideoDecodeThread.getVideoWidth();
            int videoHeight = this.mVideoDecodeThread.getVideoHeight();
            if (videoWith != 0 && videoHeight != 0) {
                long lastTime = System.currentTimeMillis();
                if (this.mPhoto == null) {
                    this.mPhoto = Photo.create(videoWith, videoHeight);
                } else {
                    this.mPhoto.updateSize(videoWith, videoHeight);
                }

                if (this.mTexturePhoto == null) {
                    this.mTexturePhoto = new Photo(this.mSurfaceTextureId, videoWith, videoHeight);
                }

                synchronized(this) {
                    if (this.updateSurface) {
                        this.mVideoDecodeThread.updateSurfaceTexture(this.mTextureMatrix);
                        this.mTextureFilter.updateTextureMatrix(this.mTextureMatrix);
                        this.updateSurface = false;
                    }
                }

                RendererUtils.checkGlError("drawFrame");
                this.mTextureFilter.process(this.mTexturePhoto, this.mPhoto);
                Photo dst = this.appFilter(this.mPhoto);
                this.setPhoto(dst);
                Log.d("P2PTime", "render frame:(" + videoWith + "," + videoHeight + "), render time:" + (System.currentTimeMillis() - lastTime));
            }
        }
    }

    public synchronized void onFrameAvailable(SurfaceTexture surfaceTexture) {
        this.updateSurface = true;
        this.requestRender();
    }

    VideoFrame takeVideoFrame() throws InterruptedException {
        return (VideoFrame)this.mAVFrameQueue.take();
    }

    int getSurfaceTextureId() {
        return this.mSurfaceTextureId;
    }

    static class VideoDecodeThread extends WorkThread {
        int mVideoWidth;
        int mVideoHeight;
        int mWidth;
        int mHeight;
        VideoFrame mRemainFrame;
        private static final int DEQUEUE_INPUT_TIMEOUT = 2000;
        private static final int DEQUEUE_OUTPUT_TIMEOUT = 2000;
        BufferInfo info = new BufferInfo();
        AndroidH264DecoderUtil.DecoderProperties mDecoderProperties;
        private MediaCodec decoder;
        private ByteBuffer[] inputBuffers;
        private ByteBuffer[] outputBuffers;
        volatile boolean mInitialError = false;
        Surface mSurface;
        SurfaceTexture mSurfaceTexture;
        WeakReference<VideoGlSurfaceViewGPU> mVideoGlSurfaceViewGPURef;

        public VideoDecodeThread(VideoGlSurfaceViewGPU videoGlSurfaceViewGPU, AndroidH264DecoderUtil.DecoderProperties decoderProperties) {
            super("VideoDecodeThread");
            this.mVideoGlSurfaceViewGPURef = new WeakReference(videoGlSurfaceViewGPU);
            this.mDecoderProperties = decoderProperties;
            Log.d("VideoGPU", "VideoDecodeThread start");
        }

        public int getVideoWidth() {
            return this.mVideoWidth;
        }

        public int getVideoHeight() {
            return this.mVideoHeight;
        }

        protected int doRepeatWork() throws InterruptedException {
            if (!this.mIsRunning) {
                return 0;
            } else {
                VideoFrame frame = null;
                if (this.mRemainFrame != null) {
                    frame = this.mRemainFrame;
                    this.mRemainFrame = null;
                } else if (this.mVideoGlSurfaceViewGPURef != null && this.mVideoGlSurfaceViewGPURef.get() != null) {
                    frame = ((VideoGlSurfaceViewGPU)this.mVideoGlSurfaceViewGPURef.get()).takeVideoFrame();
                }

                if (!this.mIsRunning) {
                    return 0;
                } else {
                    long lastTime = System.currentTimeMillis();
                    if (frame != null && frame.data != null) {
                        if (this.mInitialError) {
                            return 0;
                        } else {
                            if (this.decoder == null || frame.width != this.mWidth || frame.height != this.mHeight) {
                                Log.d("VideoGPU", "release media decoder, isIFrame:" + frame.isIFrame + " (" + this.mWidth + "," + this.mHeight + ")-->(" + frame.width + "," + frame.height + ")");
                                this.mWidth = frame.width;
                                this.mHeight = frame.height;
                                this.releseMediaDecode();
                                this.configureMediaDecode(this.mWidth, this.mHeight);
                            }

                            if (this.decoder == null) {
                                return 0;
                            } else {
                                int inputBufIndex = this.decoder.dequeueInputBuffer(2000L);
                                if (inputBufIndex >= 0) {
                                    ByteBuffer dstBuf = this.inputBuffers[inputBufIndex];
                                    dstBuf.rewind();
                                    dstBuf.put(frame.data);
                                    this.decoder.queueInputBuffer(inputBufIndex, 0, frame.data.length, frame.timeStamp * 1000L, 0);
                                } else {
                                    this.mRemainFrame = frame;
                                }

                                while(this.mIsRunning) {
                                    int res = this.decoder.dequeueOutputBuffer(this.info, 2000L);
                                    if (res >= 0) {
                                        MediaFormat outformat = this.decoder.getOutputFormat();
                                        this.mVideoWidth = outformat.getInteger("width");
                                        this.mVideoHeight = outformat.getInteger("height");
                                        this.decoder.releaseOutputBuffer(res, true);
                                    } else if (res == -3) {
                                        this.outputBuffers = this.decoder.getOutputBuffers();
                                    } else if (res != -2) {
                                        long decodeOneFrameMilliseconds = System.currentTimeMillis() - lastTime;
                                        if (this.mVideoGlSurfaceViewGPURef != null && this.mVideoGlSurfaceViewGPURef.get() != null) {
                                            ((VideoGlSurfaceViewGPU)this.mVideoGlSurfaceViewGPURef.get()).onDecodeTime(decodeOneFrameMilliseconds);
                                        }

                                        return 0;
                                    }
                                }

                                return 0;
                            }
                        }
                    } else {
                        return 0;
                    }
                }
            }
        }

        protected void doInitial() {
            Log.d("VideoGPU", "doInitial");
            this.mWidth = 0;
            this.mHeight = 0;
            int surfaceId = 0;
            if (this.mVideoGlSurfaceViewGPURef != null && this.mVideoGlSurfaceViewGPURef.get() != null) {
                surfaceId = ((VideoGlSurfaceViewGPU)this.mVideoGlSurfaceViewGPURef.get()).getSurfaceTextureId();
            }

            this.mSurfaceTexture = new SurfaceTexture(surfaceId);
            this.mSurfaceTexture.setOnFrameAvailableListener(new OnFrameAvailableListener() {
                public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                    if (VideoDecodeThread.this.mVideoGlSurfaceViewGPURef != null && VideoDecodeThread.this.mVideoGlSurfaceViewGPURef.get() != null) {
                        ((VideoGlSurfaceViewGPU)VideoDecodeThread.this.mVideoGlSurfaceViewGPURef.get()).onFrameAvailable(surfaceTexture);
                    }

                }
            });
            this.mSurface = new Surface(this.mSurfaceTexture);
            this.mInitialError = false;
        }

        protected void doRelease() {
            Log.d("VideoGPU", "doRelease");
            this.mSurfaceTexture.setOnFrameAvailableListener((OnFrameAvailableListener)null);
            this.mSurfaceTexture.release();
            this.mSurface.release();
            this.mVideoGlSurfaceViewGPURef = null;
            this.releseMediaDecode();
            Log.d("VideoGPU", "VideoDecodeThread stop");
        }

        void configureMediaDecode(int width, int height) {
            Log.d("VideoGPU", "configureMediaDecode width:" + width + " height:" + height);

            try {
                MediaFormat format = MediaFormat.createVideoFormat("video/avc", width, height);
                format.setInteger("color-format", this.mDecoderProperties.colorFormat);
                Log.d("VideoGPU", "Codec Name--------" + this.mDecoderProperties.codecName + "Codec Format--------" + this.mDecoderProperties.colorFormat);

                try {
                    this.decoder = MediaCodec.createByCodecName(this.mDecoderProperties.codecName);
                } catch (Exception var5) {
                    var5.printStackTrace();
                }

                this.decoder.configure(format, this.mSurface, (MediaCrypto)null, 0);
                this.decoder.start();
                this.inputBuffers = this.decoder.getInputBuffers();
                this.outputBuffers = this.decoder.getOutputBuffers();
            } catch (Exception var6) {
                this.mInitialError = true;
                this.releseMediaDecode();
                if (this.mVideoGlSurfaceViewGPURef != null && this.mVideoGlSurfaceViewGPURef.get() != null) {
                    ((VideoGlSurfaceViewGPU)this.mVideoGlSurfaceViewGPURef.get()).onHardDecodeException(var6);
                }
            }

        }

        void releseMediaDecode() {
            Log.d("VideoGPU", "releseMediaDecode");
            if (this.decoder != null) {
                try {
                    this.decoder.stop();
                    this.decoder.release();
                    this.decoder = null;
                    Log.d("VideoGPU", "Release decoder success");
                } catch (Exception var2) {
                    Log.d("VideoGPU", "Release decoder error" + var2.getMessage());
                }
            }

        }

        public void updateSurfaceTexture(float[] mtx) {
            this.mSurfaceTexture.updateTexImage();
            this.mSurfaceTexture.getTransformMatrix(mtx);
        }
    }
}
