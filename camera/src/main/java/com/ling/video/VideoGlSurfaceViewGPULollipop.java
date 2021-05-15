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
import android.media.MediaCodec.Callback;
import android.media.MediaCodec.CodecException;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import com.ling.video.AndroidH264DecoderUtil.DecoderProperties;
import java.nio.ByteBuffer;

@TargetApi(21)
public class VideoGlSurfaceViewGPULollipop extends VideoGlSurfaceView implements OnFrameAvailableListener {
	private static final String TAG = "VideoGPULollipop";
	static final int MSG_INITIAL = 1;
	static final int MSG_UNINITIAL = 2;
	static final int MSG_REINITIAL = 3;
	static final int MSG_LAST = 4;
	Photo mPhoto;
	Photo mTexturePhoto;
	int mSurfaceTextureId;
	volatile boolean updateSurface = false;
	GlslFilter mTextureFilter;
	float[] mTextureMatrix = new float[16];
	int mVideoWidth;
	int mVideoHeight;
	int mWidth;
	int mHeight;
	DecoderProperties mDecoderProperties;
	private volatile MediaCodec decoder;
	HandlerThread mDecoderThread;
	Handler mDecoderThreadHandler;
	Surface mSurface;
	SurfaceTexture mSurfaceTexture;
	VideoFrame mRemainVideoFrame;
	volatile boolean mStarted = false;
	volatile boolean mInitialError = false;

	public VideoGlSurfaceViewGPULollipop(Context context, AttributeSet attrs, DecoderProperties decoderProperties, HardDecodeExceptionCallback callback) {
		super(context, attrs, callback);
		this.mDecoderProperties = decoderProperties;
	}

	protected void initial() {
		super.initial();
		if (!this.mStarted) {
			this.mInitialError = false;
			this.mDecoderThread = new HandlerThread("video_decoder");
			this.mDecoderThread.start();
			this.mDecoderThreadHandler = new Handler(this.mDecoderThread.getLooper()) {
				public void handleMessage(Message msg) {
					switch(msg.what) {
						case 1:
							VideoGlSurfaceViewGPULollipop.this.configureMediaDecode(msg.arg1, msg.arg2);
							break;
						case 2:
							VideoGlSurfaceViewGPULollipop.this.releseMediaDecode();
							break;
						case 3:
							VideoGlSurfaceViewGPULollipop.this.releseMediaDecode();
							VideoGlSurfaceViewGPULollipop.this.configureMediaDecode(msg.arg1, msg.arg2);
					}

				}
			};
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
			this.mWidth = 0;
			this.mHeight = 0;
			this.mSurfaceTexture = new SurfaceTexture(this.getSurfaceTextureId());
			this.mSurfaceTexture.setOnFrameAvailableListener(new OnFrameAvailableListener() {
				public void onFrameAvailable(SurfaceTexture surfaceTexture) {
					VideoGlSurfaceViewGPULollipop.this.onFrameAvailable(surfaceTexture);
				}
			});
			this.mSurface = new Surface(this.mSurfaceTexture);
			this.mStarted = true;
		}
	}

	void releseMediaDecode() {
		if (this.decoder != null) {
			try {
				this.decoder.stop();
				this.decoder.release();
				this.decoder = null;
				Log.d("VideoGPULollipop", "Release decoder success");
			} catch (Exception var2) {
				Log.d("VideoGPULollipop", "Release decoder error:" + var2.getMessage());
			}
		}

	}

	void configureMediaDecode(int width, int height) {
		if (this.decoder == null) {
			Log.d("VideoGPULollipop", "configureMediaDecode width:" + width + " height:" + height);

			try {
				MediaFormat format = MediaFormat.createVideoFormat("video/avc", width, height);
				format.setInteger("color-format", this.mDecoderProperties.colorFormat);
				Log.d("VideoGPULollipop", "Codec Name--------" + this.mDecoderProperties.codecName + "Codec Format--------" + this.mDecoderProperties.colorFormat);
				this.decoder = MediaCodec.createByCodecName(this.mDecoderProperties.codecName);
				this.mWidth = width;
				this.mHeight = height;
				this.decoder.setCallback(new Callback() {
					public void onInputBufferAvailable(MediaCodec codec, int index) {
						if (VideoGlSurfaceViewGPULollipop.this.decoder != null && VideoGlSurfaceViewGPULollipop.this.mStarted) {
							Log.d("VideoGPULollipop", "onInputBufferAvailable");
							ByteBuffer inputBuffer = VideoGlSurfaceViewGPULollipop.this.decoder.getInputBuffer(index);
							VideoFrame frame = null;
							if (VideoGlSurfaceViewGPULollipop.this.mRemainVideoFrame != null) {
								frame = VideoGlSurfaceViewGPULollipop.this.mRemainVideoFrame;
								VideoGlSurfaceViewGPULollipop.this.mRemainVideoFrame = null;
							} else {
								try {
									frame = (VideoFrame)VideoGlSurfaceViewGPULollipop.this.mAVFrameQueue.take();
								} catch (InterruptedException var6) {
									var6.printStackTrace();
								}
							}

							if (frame != null && frame.data != null) {
								if (VideoGlSurfaceViewGPULollipop.this.decoder != null && frame.width == VideoGlSurfaceViewGPULollipop.this.mWidth && frame.height == VideoGlSurfaceViewGPULollipop.this.mHeight) {
									inputBuffer.rewind();
									inputBuffer.put(frame.data);
									VideoGlSurfaceViewGPULollipop.this.decoder.queueInputBuffer(index, 0, frame.data.length, frame.timeStamp * 1000L, 0);
								} else {
									Log.d("VideoGPULollipop", "release media decoder, isIFrame:" + frame.isIFrame + " (" + VideoGlSurfaceViewGPULollipop.this.mWidth + "," + VideoGlSurfaceViewGPULollipop.this.mHeight + ")-->(" + frame.width + "," + frame.height + ")");
									VideoGlSurfaceViewGPULollipop.this.mRemainVideoFrame = frame;
									VideoGlSurfaceViewGPULollipop.this.clearMsg();
									VideoGlSurfaceViewGPULollipop.this.releseMediaDecode();
									VideoGlSurfaceViewGPULollipop.this.configureMediaDecode(frame.width, frame.height);
								}
							}
						}
					}

					public void onOutputBufferAvailable(MediaCodec codec, int index, BufferInfo info) {
						if (VideoGlSurfaceViewGPULollipop.this.decoder != null && VideoGlSurfaceViewGPULollipop.this.mStarted) {
							Log.d("VideoGPULollipop", "onOutputBufferAvailable");
							MediaFormat outformat = VideoGlSurfaceViewGPULollipop.this.decoder.getOutputFormat();
							VideoGlSurfaceViewGPULollipop.this.mVideoWidth = outformat.getInteger("width");
							VideoGlSurfaceViewGPULollipop.this.mVideoHeight = outformat.getInteger("height");
							VideoGlSurfaceViewGPULollipop.this.decoder.releaseOutputBuffer(index, true);
						}
					}

					public void onError(MediaCodec codec, CodecException e) {
						Log.e("VideoGPULollipop", "onError:" + e.getMessage());
						VideoGlSurfaceViewGPULollipop.this.onHardDecodeException(e);
					}

					public void onOutputFormatChanged(MediaCodec codec, MediaFormat format) {
						Log.d("VideoGPULollipop", "onOutputFormatChanged");
					}
				});
				this.decoder.configure(format, this.mSurface, (MediaCrypto)null, 0);
				this.decoder.start();
			} catch (Exception var4) {
				this.mInitialError = true;
				this.releseMediaDecode();
				this.onHardDecodeException(var4);
			}

		}
	}

	protected void release() {
		super.release();
		this.mStarted = false;
		RendererUtils.clearTexture(this.mSurfaceTextureId);
		this.mTextureFilter.release();
		if (this.mPhoto != null) {
			this.mPhoto.clear();
			this.mPhoto = null;
		}

		this.mSurfaceTexture.setOnFrameAvailableListener((OnFrameAvailableListener)null);
		this.mSurfaceTexture.release();
		this.mSurface.release();
		this.clearMsg();
		this.mDecoderThreadHandler.sendEmptyMessage(2);
		this.mDecoderThread.quitSafely();
		this.mDecoderThread = null;
		this.mDecoderThreadHandler = null;
	}

	void clearMsg() {
		if (this.mDecoderThreadHandler != null) {
			for(int i = 0; i < 4; ++i) {
				this.mDecoderThreadHandler.removeMessages(i);
			}

		}
	}

	public void drawVideoFrame(VideoFrame frame) {
		if (this.mStarted) {
			if (this.decoder == null && !this.mInitialError) {
				this.clearMsg();
				this.mDecoderThreadHandler.obtainMessage(1, frame.width, frame.height).sendToTarget();
			}

			super.drawVideoFrame(frame);
		}
	}

	public void drawFrame() {
		super.drawFrame();
		int videoWith = this.mVideoWidth;
		int videoHeight = this.mVideoHeight;
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

			if (this.updateSurface) {
				this.mSurfaceTexture.updateTexImage();
				this.mSurfaceTexture.getTransformMatrix(this.mTextureMatrix);
				this.mTextureFilter.updateTextureMatrix(this.mTextureMatrix);
				this.updateSurface = false;
			}

			RendererUtils.checkGlError("drawFrame");
			this.mTextureFilter.process(this.mTexturePhoto, this.mPhoto);
			Photo dst = this.appFilter(this.mPhoto);
			this.setPhoto(dst);
			Log.d("P2PTime", "render frame:(" + videoWith + "," + videoHeight + "), render time:" + (System.currentTimeMillis() - lastTime));
		}
	}

	public synchronized void onFrameAvailable(SurfaceTexture surfaceTexture) {
		this.updateSurface = true;
		this.requestRender();
	}

	int getSurfaceTextureId() {
		return this.mSurfaceTextureId;
	}
}
