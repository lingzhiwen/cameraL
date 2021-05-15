
package com.ling.camera;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.FrameLayout;

import com.ling.video.AndroidH264DecoderUtil.DecoderProperties;
import com.ling.video.HardDecodeExceptionCallback;
import com.ling.video.PhotoView.PhotoSnapCallback;
import com.ling.video.VideoFrame;
import com.ling.video.VideoGlSurfaceView;
import com.ling.video.VideoGlSurfaceViewFactory;

public class VideoViewGl extends FrameLayout {
    final static String TAG = "VideoView";
    public static boolean ENABLE_MEDIACODEC = false;
    static final int MAX_SCALE = 4;
    DecoderProperties mDecoderProperties;
    // static final int SHOW_PROGRESS = 5;
    VideoGlSurfaceView mSurfaceView;
    float mLastX = 0;
    float mLastY = 0;

    // size of the video
    int mVideoHeight;
    int mVideoWidth;

    // Drawable mFrameDrawable;
    boolean mIsFull;

    private GestureDetector mDetector;
    private ScaleGestureDetector mScaleDetector;

    // Current scale and scale at start of zoom
    private float mScale = 1.0f;
    private float mScaleLast;

    private float mCenterX;
    private float mCenterY;

    private boolean mZooming = false;

    boolean mIsMinScale = false;
    boolean mIsResume;

    public VideoViewGl(Context context) {
        super(context);
        initial();
    }

    public VideoViewGl(Context context, AttributeSet attrs) {
        super(context, attrs);
        initial();
    }
    
    

    public void initial() {

        mSurfaceView = VideoGlSurfaceViewFactory.createVideoGlSurfaceView(getContext(),new HardDecodeExceptionCallback(){

            @Override
            public void onHardDecodeException(Exception e) {

            }

            @Override
            public void onOtherException(Throwable e) {

            }

            @Override
            public void onDecodePerformance(long decodeOneFrameMilliseconds) {

            }
        },false);
        addView(mSurfaceView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        mScaleDetector = new ScaleGestureDetector(getContext(),
                new ScaleGestureDetector.OnScaleGestureListener() {

                    @Override
                    public void onScaleEnd(ScaleGestureDetector detector) {
                        // TODO Auto-generated method stub
                        // if (mScale < 1.0f) {
                        // mBuilder.setScale((float) (1.0 - mScale), 200);
                        // mBuilder.start();
                        // }
                    }

                    @Override
                    public boolean onScaleBegin(ScaleGestureDetector detector) {
                        // TODO Auto-generated method stub
                        mZooming = true;
                        mScaleLast = mScale;
                        return true;
                    }

                    @Override
                    public boolean onScale(ScaleGestureDetector detector) {
                        // TODO Auto-generated method stub
                        float scale = mSurfaceView.getScale() * detector.getScaleFactor();
                        scale = Math.max(mSurfaceView.getMiniScale(), Math.min(scale, MAX_SCALE));
                        mSurfaceView.setScale(scale, false);
                        mSurfaceView.requestRender();
                        mScaleLast = mScale;
                        return true;
                    }
                });

        mDetector = new GestureDetector(getContext(),
                new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2,
                            float velocityX, float velocityY) {
                        if ((Math.abs(e1.getX() - e2.getX()) > 50 || Math
                                .abs(e1.getY() - e2.getY()) > 50)
                                && (Math.abs(velocityX) > 500 || Math
                                        .abs(velocityY) > 500)) {
                            // PointF vTranslateEnd = new PointF(mSurfaceOffsetX
                            // + (velocityX * 0.25f), mSurfaceOffsetY +
                            // (velocityY * 0.25f));
                            // if (mBuilder != null) {
                            // mBuilder.setTranslation(
                            // (int) (velocityX * 0.1f),
                            // (int) (velocityY * 0.1f), 200, false,
                            // null);
                            // mBuilder.start();
                            // }
                            return true;
                        }
                        return super.onFling(e1, e2, velocityX, velocityY);
                    }

                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e) {
                        return true;
                    }

                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        if (!mIsFull) {
                            if (mSurfaceView.getScale() > 1.0) {
                                mSurfaceView.setScale(1.0f, true);
                                mSurfaceView.requestRender();
                            } else if (mSurfaceView.getScale() > 0.9) {
                                mSurfaceView.setScale(mSurfaceView.getMiniScale(), true);
                                mSurfaceView.requestRender();
                            }
                            else {
                                mSurfaceView.setScale(1.0f, true);
                                mSurfaceView.requestRender();
                            }
                        } else {
                            if (mSurfaceView.getScale() > 1.0) {
                                mSurfaceView.setScale(1.0f, true);
                                mSurfaceView.requestRender();
                            } else {
                                mSurfaceView.setScale(2.0f, true);
                                mSurfaceView.requestRender();
                            }
                        }
                        return true;
                    }
                });

    }

    public void setVideoFrameSize(int width, int height, boolean isFull) {

        mSurfaceView.reset();
        mIsFull = isFull;

        LayoutParams frameLp = (LayoutParams) getLayoutParams();
        frameLp.width = width;
        frameLp.height = height;
        frameLp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        setLayoutParams(frameLp);

    }

    public VideoGlSurfaceView getSurfaceView() {
        return mSurfaceView;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDetector.onTouchEvent(event);
        mScaleDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mLastX = event.getX();
            mLastY = event.getY();
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (!mZooming) {
                mSurfaceView.move((int) (event.getX() - mLastX),
                        (int) -(event.getY() - mLastY),false);
                mSurfaceView.requestRender();
                mLastX = event.getX();
                mLastY = event.getY();
            }
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_UP
                || event.getAction() == MotionEvent.ACTION_CANCEL) {
            // if (!mZooming)
            // moveBack();
            mZooming = false;
            return true;
        }
        return true;
    }

    public void drawVideoFrame(VideoFrame frame) {
        mSurfaceView.drawVideoFrame(frame);
    }
    public void snap(PhotoSnapCallback callback) {
        mSurfaceView.snap(callback);
    }
    public void onResume() {
        mIsResume = true;
        if (mSurfaceView != null)
            mSurfaceView.onResume();
    }

    public void onPause() {
        mIsResume = false;
        if (mSurfaceView != null)
            mSurfaceView.onPause();
    }
}
