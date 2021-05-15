package com.ling.video;

import android.content.Context;
import android.util.AttributeSet;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class VideoGlSurfaceView extends PhotoView {
    protected LinkedBlockingQueue<VideoFrame> mAVFrameQueue = new LinkedBlockingQueue(30);
    protected WeakReference<HardDecodeExceptionCallback> mHardDecodeExceptionCallback;
    Filter mFilter;
    Photo mMiddlePhoto;
    final int MAX_COUNT_TIME = 20;
    final int MAX_TIME_SIZE = 5;
    int frameCount = 0;
    List<Long> decodeTimes = new ArrayList();

    public VideoGlSurfaceView(Context context, AttributeSet attrs, HardDecodeExceptionCallback callback) {
        super(context, attrs);
        if (callback != null) {
            this.mHardDecodeExceptionCallback = new WeakReference(callback);
        }

    }

    public void drawVideoFrame(VideoFrame frame) {
        try {
            this.mAVFrameQueue.put(frame);
            this.requestRender();
        } catch (InterruptedException var3) {
        }

    }

    public void setFilter(final Filter filter) {
        this.queue(new Runnable() {
            public void run() {
                if (VideoGlSurfaceView.this.mFilter != null) {
                    VideoGlSurfaceView.this.mFilter.release();
                }

                VideoGlSurfaceView.this.mFilter = filter;
                VideoGlSurfaceView.this.mFilter.initial();
            }
        });
    }

    protected Photo appFilter(Photo src) {
        if (this.mFilter != null) {
            if (this.mMiddlePhoto == null && src != null) {
                this.mMiddlePhoto = Photo.create(src.width(), src.height());
            }

            this.mFilter.process(src, this.mMiddlePhoto);
            return this.mMiddlePhoto;
        } else {
            return src;
        }
    }

    protected void initial() {
        super.initial();
        if (this.mFilter != null) {
            this.mFilter.initial();
        }

        this.mAVFrameQueue.clear();
    }

    protected void release() {
        super.release();
        if (this.mFilter != null) {
            this.mFilter.release();
        }

        if (this.mMiddlePhoto != null) {
            this.mMiddlePhoto.clear();
            this.mMiddlePhoto = null;
        }

        this.mAVFrameQueue.clear();
    }

    public void onHardDecodeException(Exception e) {
        if (this.mHardDecodeExceptionCallback != null && this.mHardDecodeExceptionCallback.get() != null) {
            ((HardDecodeExceptionCallback)this.mHardDecodeExceptionCallback.get()).onHardDecodeException(e);
        }

    }

    public void onOtherException(Throwable e) {
        if (this.mHardDecodeExceptionCallback != null && this.mHardDecodeExceptionCallback.get() != null) {
            ((HardDecodeExceptionCallback)this.mHardDecodeExceptionCallback.get()).onOtherException(e);
        }

    }

    public void onDecodeTime(long decodeOneFrameMilliseconds) {
        if (this.decodeTimes.size() >= 5) {
            this.decodeTimes.remove(0);
        }

        this.decodeTimes.add(decodeOneFrameMilliseconds);
        if (this.decodeTimes.size() > 0 && this.frameCount >= 20) {
            long averageDecodeTime = 0L;

            long mTime;
            for(Iterator var5 = this.decodeTimes.iterator(); var5.hasNext(); averageDecodeTime += mTime) {
                mTime = (Long)var5.next();
            }

            averageDecodeTime /= (long)this.decodeTimes.size();
            if (this.mHardDecodeExceptionCallback != null && this.mHardDecodeExceptionCallback.get() != null) {
                ((HardDecodeExceptionCallback)this.mHardDecodeExceptionCallback.get()).onDecodePerformance(averageDecodeTime);
            }

            this.frameCount = 0;
        } else {
            ++this.frameCount;
        }

    }
}
