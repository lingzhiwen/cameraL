package com.ling.video;

import android.content.Context;
import android.os.Build.VERSION;
import android.util.AttributeSet;

public class VideoGlSurfaceViewFactory {
    public VideoGlSurfaceViewFactory() {
    }

    public static VideoGlSurfaceView createVideoGlSurfaceView(Context context, HardDecodeExceptionCallback callback, boolean useHard) {
        if (useHard) {
            AndroidH264DecoderUtil.DecoderProperties decoderProperty = AndroidH264DecoderUtil.findAVCDecoder();
            if (decoderProperty != null) {
                if (VERSION.SDK_INT < 21) {
                    return new VideoGlSurfaceViewGPU(context, (AttributeSet)null, decoderProperty, callback);
                }

                return new VideoGlSurfaceViewGPULollipop2(context, (AttributeSet)null, decoderProperty, callback);
            }
        }

        return new VideoGlSurfaceViewFFMPEG(context, (AttributeSet)null, callback);
    }
}
