package com.ling.video;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.renderscript.Matrix4f;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import com.ling.video.CustomChooseConfig.ComponentSizeChooser;
import com.ling.video.RendererUtils.RenderContext;
import java.nio.IntBuffer;
import java.util.Vector;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class PhotoView extends GLSurfaceView {
    public static final String TAG = "PhotoView";
    private final PhotoView.PhotoRenderer renderer;
    public int mMaxTextureSize;
    int mWidth;
    int mHeight;
    float mScale = 1.0F;
    boolean mIsFinger = false;
    float mTargeScaleOffset = 0.0F;
    float mStartScale;
    float mOffsetX = 0.0F;
    float mOffsetY = 0.0F;
    int mMaxOffsetX;
    int mMaxOffsetY;
    float mMiniScale;
    long mAnimaStartTime;
    long mAnimaTime = 400L;
    volatile boolean mIsResume = false;
    volatile boolean isInitial = false;
    Photo firstPhoto;
    Interpolator mInterpolator = new AccelerateDecelerateInterpolator();
    private PhotoView.OnScreenWindowChangedListener onScreenWindowChangedListener = null;

    public void snap(PhotoView.PhotoSnapCallback callback) {
        this.renderer.snap(callback);
    }

    public PhotoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!this.supportsOpenGLES2(context)) {
            throw new RuntimeException("not support gles 2.0");
        } else {
            this.renderer = new PhotoView.PhotoRenderer();
            this.setEGLContextClientVersion(2);
            this.setEGLConfigChooser(new ComponentSizeChooser(8, 8, 8, 8, 0, 0));
            this.getHolder().setFormat(1);
            this.setRenderer(this.renderer);
            this.setRenderMode(0);
        }
    }

    private boolean supportsOpenGLES2(Context context) {
        ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        return configurationInfo.reqGlEsVersion >= 131072;
    }

    public void setOnScreenWindowChangedListener(PhotoView.OnScreenWindowChangedListener listener) {
        this.onScreenWindowChangedListener = listener;
    }

    public void reset() {
        this.mScale = 1.0F;
        this.mIsFinger = false;
        this.mStartScale = this.mScale;
        this.mOffsetX = 0.0F;
        this.mOffsetY = 0.0F;
        this.mTargeScaleOffset = 0.0F;
    }

    public float getScale() {
        return this.mScale;
    }

    public void setScale(float scale, boolean animal) {
        if (animal) {
            this.mTargeScaleOffset = scale - this.mScale;
            this.mStartScale = this.mScale;
            this.mAnimaStartTime = System.currentTimeMillis();
        } else {
            this.mScale = scale;
            this.mStartScale = this.mScale;
            this.mTargeScaleOffset = 0.0F;
        }

        if ((double)scale > 1.0D) {
            this.mIsFinger = true;
        } else {
            this.mIsFinger = false;
        }

    }

    public float getMiniScale() {
        return this.mMiniScale;
    }

    public void move(float x, float y, boolean isFinger) {
        this.mOffsetX += x;
        this.mOffsetY += y;
        if ((double)this.mScale > 1.0D) {
            this.mIsFinger = isFinger;
        } else {
            this.mIsFinger = false;
        }

    }

    public int getPhotoWith() {
        return this.mWidth;
    }

    public int getPhotoHeight() {
        return this.mHeight;
    }

    public void queue(Runnable r) {
        this.renderer.queue.add(r);
        this.requestRender();
    }

    public void remove(Runnable runnable) {
        this.renderer.queue.remove(runnable);
    }

    public void flush() {
        this.renderer.queue.clear();
    }

    public void setPhoto(Photo photo) {
        this.renderer.setPhoto(photo);
        this.mWidth = photo.width();
        this.mHeight = photo.height();
    }

    public void setRenderMatrix(float[] matrix) {
        this.renderer.setRenderMatrix(matrix);
    }

    public void onResume() {
        super.onResume();
        Log.d("PhotoView", "onResume");
        this.mIsResume = true;
        this.flush();
        this.queue(new Runnable() {
            public void run() {
                if (!PhotoView.this.isInitial) {
                    PhotoView.this.isInitial = true;
                    PhotoView.this.initial();
                }

            }
        });
    }

    public void onPause() {
        super.onPause();
        Log.d("PhotoView", "onPause");
        this.mIsResume = false;
        this.queueEvent(new Runnable() {
            public void run() {
                PhotoView.this.flush();
                if (PhotoView.this.isInitial) {
                    PhotoView.this.release();
                    PhotoView.this.isInitial = false;
                }

            }
        });
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        super.surfaceChanged(holder, format, w, h);
        Log.d("PhotoView", "surfaceChanged");
    }

    public void surfaceCreated(SurfaceHolder holder) {
        super.surfaceCreated(holder);
        Log.d("PhotoView", "surfaceCreated");
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
        Log.d("PhotoView", "surfaceDestroyed");
    }

    protected void initial() {
        Log.d("PhotoView", "initial");
    }

    protected void release() {
        Log.d("PhotoView", "release");
        this.renderer.release();
        if (this.firstPhoto != null) {
            this.firstPhoto.clear();
        }

    }

    public void drawFrame() {
    }

    public void setFirstBitmap(final Bitmap bitmap) {
        this.queue(new Runnable() {
            public void run() {
                PhotoView.this.firstPhoto = Photo.create(bitmap);
                PhotoView.this.setPhoto(PhotoView.this.firstPhoto);
                bitmap.recycle();
            }
        });
    }

    class PhotoRenderer implements Renderer {
        final Vector<Runnable> queue = new Vector();
        RenderContext renderContext;
        Photo photo;
        int viewWidth;
        int viewHeight;
        int lastWidth;
        int lastHeight;
        int lastX1;
        int lastY1;
        int lastX2;
        int lastY2;

        PhotoRenderer() {
        }

        void setPhoto(Photo photo) {
            this.photo = photo;
        }

        void setRenderMatrix(float[] matrix) {
            RendererUtils.setRenderMatrix(this.renderContext, matrix);
        }

        public void onDrawFrame(GL10 gl) {
            Runnable r = null;
            synchronized(this.queue) {
                if (!this.queue.isEmpty()) {
                    r = (Runnable)this.queue.remove(0);
                }
            }

            if (r != null) {
                r.run();
            }

            if (!this.queue.isEmpty()) {
                PhotoView.this.requestRender();
            }

            if (PhotoView.this.mIsResume) {
                RendererUtils.renderBackground();
                PhotoView.this.drawFrame();
                if (this.photo != null) {
                    this.buildAnimal();
                    this.setRenderMatrix(this.photo.width(), this.photo.height());
                    RendererUtils.renderTexture(this.renderContext, this.photo.texture(), this.viewWidth, this.viewHeight);
                }
            }

        }

        void buildAnimal() {
            long time = System.currentTimeMillis() - PhotoView.this.mAnimaStartTime;
            if (time > PhotoView.this.mAnimaTime) {
                PhotoView.this.mScale = PhotoView.this.mStartScale + PhotoView.this.mTargeScaleOffset;
            } else {
                float ratio = PhotoView.this.mInterpolator.getInterpolation((float)((double)time * 1.0D / (double)PhotoView.this.mAnimaTime));
                PhotoView.this.mScale = PhotoView.this.mStartScale + ratio * PhotoView.this.mTargeScaleOffset;
                PhotoView.this.requestRender();
            }
        }

        void setRenderMatrix(int srcWidth, int srcHeight) {
            Matrix4f matrix4f = new Matrix4f();
            float srcAspectRatio = (float)srcWidth / (float)srcHeight;
            float dstAspectRatio = (float)this.viewWidth / (float)this.viewHeight;
            float relativeAspectRatio = dstAspectRatio / srcAspectRatio;
            float ratioscale = 1.0F;
            float x;
            float y;
            float xScale;
            float yScale;
            if (relativeAspectRatio < 1.0F) {
                ratioscale = srcAspectRatio / dstAspectRatio;
                PhotoView.this.mMiniScale = relativeAspectRatio;
                PhotoView.this.mMaxOffsetX = (int)((float)this.viewWidth * ratioscale * PhotoView.this.mScale - (float)this.viewWidth);
                PhotoView.this.mMaxOffsetY = (int)((float)this.viewHeight * PhotoView.this.mScale - (float)this.viewHeight);
                if (PhotoView.this.mOffsetX < (float)(-PhotoView.this.mMaxOffsetX)) {
                    PhotoView.this.mOffsetX = (float)(-PhotoView.this.mMaxOffsetX);
                }

                if (PhotoView.this.mOffsetX > (float)PhotoView.this.mMaxOffsetX) {
                    PhotoView.this.mOffsetX = (float)PhotoView.this.mMaxOffsetX;
                }

                if (PhotoView.this.mOffsetY < (float)(-PhotoView.this.mMaxOffsetY)) {
                    PhotoView.this.mOffsetY = (float)(-PhotoView.this.mMaxOffsetY);
                }

                if (PhotoView.this.mOffsetY > (float)PhotoView.this.mMaxOffsetY) {
                    PhotoView.this.mOffsetY = (float)PhotoView.this.mMaxOffsetY;
                }

                xScale = ratioscale * PhotoView.this.mScale;
                yScale = PhotoView.this.mScale;
                matrix4f.scale(xScale, yScale, 0.0F);
                x = PhotoView.this.mOffsetX / ((float)this.viewWidth * xScale);
                y = PhotoView.this.mOffsetY / ((float)this.viewHeight * yScale);
                if ((double)PhotoView.this.mScale < 1.0D) {
                    y = 0.0F;
                }

                matrix4f.translate(x, y, 0.0F);
            } else {
                PhotoView.this.mMiniScale = 1.0F;
                PhotoView.this.mMaxOffsetX = (int)((float)this.viewWidth * PhotoView.this.mScale - (float)this.viewWidth);
                PhotoView.this.mMaxOffsetY = (int)((float)this.viewHeight * relativeAspectRatio * PhotoView.this.mScale - (float)this.viewHeight);
                if (PhotoView.this.mOffsetX < (float)(-PhotoView.this.mMaxOffsetX)) {
                    PhotoView.this.mOffsetX = (float)(-PhotoView.this.mMaxOffsetX);
                }

                if (PhotoView.this.mOffsetX > (float)PhotoView.this.mMaxOffsetX) {
                    PhotoView.this.mOffsetX = (float)PhotoView.this.mMaxOffsetX;
                }

                if (PhotoView.this.mOffsetY < (float)(-PhotoView.this.mMaxOffsetY)) {
                    PhotoView.this.mOffsetY = (float)(-PhotoView.this.mMaxOffsetY);
                }

                if (PhotoView.this.mOffsetY > (float)PhotoView.this.mMaxOffsetY) {
                    PhotoView.this.mOffsetY = (float)PhotoView.this.mMaxOffsetY;
                }

                xScale = PhotoView.this.mScale;
                yScale = relativeAspectRatio * PhotoView.this.mScale;
                matrix4f.scale(xScale, yScale, 0.0F);
                x = PhotoView.this.mOffsetX / ((float)this.viewWidth * xScale);
                y = PhotoView.this.mOffsetY / ((float)this.viewHeight * yScale);
                matrix4f.translate(x, y, 0.0F);
            }

            this.renderContext.mModelViewMat = matrix4f.getArray();
            int x1 = (int)((1.0F - 1.0F / xScale - x) * (float)srcWidth / 2.0F);
            int x2 = (int)((float)x1 + 1.0F / xScale * (float)srcWidth);
            int y1 = (int)((1.0F / yScale - 1.0F - y) * (float)srcHeight / 2.0F);
            int y2 = (int)((float)y1 - 1.0F / yScale * (float)srcHeight);
            if (x1 < 0) {
                x1 = 0;
            }

            if (x2 > srcWidth) {
                x2 = srcWidth;
            }

            if (y1 > 0) {
                y1 = 0;
            }

            if (y2 < 0 - srcHeight) {
                y2 = 0 - srcHeight;
            }

            if (this.lastWidth != srcWidth || this.lastHeight != srcHeight || this.lastX1 != x1 || this.lastY1 != y1 || this.lastX2 != x2 || this.lastY2 != y2) {
                if (PhotoView.this.onScreenWindowChangedListener != null) {
                    PhotoView.this.onScreenWindowChangedListener.onScreenWindowChanged(PhotoView.this.mIsFinger, srcWidth, srcHeight, x1, y1, x2, y2);
                }

                this.lastWidth = srcWidth;
                this.lastHeight = srcHeight;
                this.lastX1 = x1;
                this.lastY1 = y1;
                this.lastX2 = x2;
                this.lastY2 = y2;
            }

        }

        public void onSurfaceChanged(GL10 gl, int width, int height) {
            Log.d("PhotoView", "onSurfaceChanged");
            this.viewWidth = width;
            this.viewHeight = height;
            PhotoView.this.reset();
        }

        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            Log.d("PhotoView", "onSurfaceCreated");
            GLES20.glEnable(3553);
            IntBuffer buffer = IntBuffer.allocate(1);
            GLES20.glGetIntegerv(3379, buffer);
            PhotoView.this.mMaxTextureSize = buffer.get(0);
            GLES20.glGetError();
            this.renderContext = RendererUtils.createProgram();
        }

        public void release() {
            RendererUtils.releaseRenderContext(this.renderContext);
        }

        public void snap(final PhotoView.PhotoSnapCallback callback) {
            if (callback != null) {
                if (this.photo == null) {
                    if (callback != null) {
                        callback.onSnap((Bitmap)null);
                    }

                } else {
                    PhotoView.this.queue(new Runnable() {
                        public void run() {
                            Bitmap bitmap = RendererUtils.saveTexture(PhotoRenderer.this.photo.texture(), PhotoRenderer.this.photo.width(), PhotoRenderer.this.photo.height());
                            if (callback != null) {
                                callback.onSnap(bitmap);
                            }

                        }
                    });
                    PhotoView.this.requestRender();
                }
            }
        }
    }

    public interface OnScreenWindowChangedListener {
        void onScreenWindowChanged(boolean var1, int var2, int var3, int var4, int var5, int var6, int var7);
    }

    public interface PhotoSnapCallback {
        void onSnap(Bitmap var1);
    }
}
