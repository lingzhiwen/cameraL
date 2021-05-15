package com.ling.video;

import android.content.Context;
import android.opengl.GLES20;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class GlslFilter extends Filter {
	private static final int FLOAT_SIZE_BYTES = 4;
	private static final float[] TEX_VERTICES = new float[]{0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F};
	private static final float[] TEX_VERTICES_SURFACE_TEXTURE = new float[]{0.0F, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F};
	private static final float[] POS_VERTICES = new float[]{-1.0F, -1.0F, 0.0F, -1.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F, -1.0F, 0.0F};
	private static final float[] IDENTIFY_MATRIX = new float[]{1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F};
	private static final String VERTEX_SHADER = "attribute vec4 a_position;\nattribute vec2 a_texcoord;\nuniform mat4 u_texture_mat; \nuniform mat4 u_model_view; \nvarying vec2 textureCoordinate;\nvoid main() {\n  gl_Position = u_model_view*a_position;\n  vec4 tmp = u_texture_mat*vec4(a_texcoord.x,a_texcoord.y,0.0,1.0);\n  textureCoordinate = tmp.xy;\n}\n";
	private static final String FRAGMENT_SHADER = "precision mediump float;\nuniform sampler2D inputImageTexture;\nvarying vec2 textureCoordinate;\nvoid main() {\n  gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n}\n";
	private final String SURFACE_TEXTURE_FRAGMENT_SHADER = "#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nuniform samplerExternalOES inputImageTexture;\nvarying vec2 textureCoordinate;\nvoid main() {\n  gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n}\n";
	public static final int GL_TEXTURE_EXTERNAL_OES = 36197;
	public static final int GL_TEXTURE_2D = 3553;
	int mInputTextureType = 3553;
	protected int shaderProgram;
	private int texSamplerHandle;
	private int texCoordHandle;
	private int posCoordHandle;
	private FloatBuffer texVertices;
	private FloatBuffer posVertices;
	private int texCoordMatHandle;
	private int modelViewMatHandle;
	private int[] frameBufferObjectId = new int[]{0};
	final float[] mTextureMat = new float[]{1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F};
	final float[] mModelViewMat = new float[]{1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F};
	boolean isInitialed = false;
	GlslFilter mNextGlslFilter;
	Photo mMiddlePhoto;
	protected Context mContext;

	public GlslFilter(Context context) {
		this.mContext = context;
	}

	public String vertexShader() {
		return "attribute vec4 a_position;\nattribute vec2 a_texcoord;\nuniform mat4 u_texture_mat; \nuniform mat4 u_model_view; \nvarying vec2 textureCoordinate;\nvoid main() {\n  gl_Position = u_model_view*a_position;\n  vec4 tmp = u_texture_mat*vec4(a_texcoord.x,a_texcoord.y,0.0,1.0);\n  textureCoordinate = tmp.xy;\n}\n";
	}

	public String fragmentShader() {
		return this.mInputTextureType == 3553 ? "precision mediump float;\nuniform sampler2D inputImageTexture;\nvarying vec2 textureCoordinate;\nvoid main() {\n  gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n}\n" : "#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nuniform samplerExternalOES inputImageTexture;\nvarying vec2 textureCoordinate;\nvoid main() {\n  gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n}\n";
	}

	public void setNextFilter(GlslFilter filter) {
		this.mNextGlslFilter = filter;
	}

	public final void setType(int type) {
		this.mInputTextureType = type;
	}

	public final void initial() {
		if (!this.isInitialed) {
			this.isInitialed = true;
			int vertexShader = loadShader(35633, this.vertexShader());
			if (vertexShader == 0) {
				throw new RuntimeException("Could not load vertex shader: " + this.vertexShader());
			} else {
				int pixelShader = loadShader(35632, this.fragmentShader());
				if (pixelShader == 0) {
					throw new RuntimeException("Could not load fragment shader: " + this.fragmentShader());
				} else {
					this.shaderProgram = GLES20.glCreateProgram();
					if (this.shaderProgram != 0) {
						GLES20.glAttachShader(this.shaderProgram, vertexShader);
						checkGlError("glAttachShader");
						GLES20.glAttachShader(this.shaderProgram, pixelShader);
						checkGlError("glAttachShader");
						GLES20.glLinkProgram(this.shaderProgram);
						int[] linkStatus = new int[1];
						GLES20.glGetProgramiv(this.shaderProgram, 35714, linkStatus, 0);
						if (linkStatus[0] != 1) {
							String info = GLES20.glGetProgramInfoLog(this.shaderProgram);
							GLES20.glDeleteProgram(this.shaderProgram);
							this.shaderProgram = 0;
							throw new RuntimeException("Could not link program: " + info);
						} else {
							this.texSamplerHandle = GLES20.glGetUniformLocation(this.shaderProgram, "inputImageTexture");
							this.texCoordHandle = GLES20.glGetAttribLocation(this.shaderProgram, "a_texcoord");
							this.posCoordHandle = GLES20.glGetAttribLocation(this.shaderProgram, "a_position");
							this.texCoordMatHandle = GLES20.glGetUniformLocation(this.shaderProgram, "u_texture_mat");
							this.modelViewMatHandle = GLES20.glGetUniformLocation(this.shaderProgram, "u_model_view");
							if (this.mInputTextureType == 3553) {
								this.texVertices = createVerticesBuffer(TEX_VERTICES);
							} else {
								this.texVertices = createVerticesBuffer(TEX_VERTICES_SURFACE_TEXTURE);
							}

							this.posVertices = createVerticesBuffer(POS_VERTICES);
							this.prepareParams();
							if (this.mNextGlslFilter != null) {
								this.mNextGlslFilter.initial();
							}

						}
					} else {
						throw new RuntimeException("Could not create program");
					}
				}
			}
		}
	}

	protected void prepareParams() {
	}

	protected void updateParams() {
	}

	protected void doRelease() {
	}

	public final void release() {
		if (this.isInitialed) {
			this.isInitialed = false;
			if (this.mMiddlePhoto != null) {
				this.mMiddlePhoto.clear();
				this.mMiddlePhoto = null;
			}

			if (this.mNextGlslFilter != null) {
				this.mNextGlslFilter.release();
			}

			this.doRelease();
			if (this.shaderProgram > 0) {
				GLES20.glDeleteProgram(this.shaderProgram);
				this.shaderProgram = 0;
			}

			if (this.frameBufferObjectId[0] > 0) {
				GLES20.glDeleteFramebuffers(1, this.frameBufferObjectId, 0);
				this.frameBufferObjectId[0] = 0;
			}

		}
	}

	public final void updateModelViewMatrix(float[] matrix) {
		for(int i = 0; i < matrix.length; ++i) {
			this.mModelViewMat[i] = matrix[i];
		}

	}

	public final void flipXModelView() {
		this.mModelViewMat[0] = -this.mModelViewMat[0];
		this.mModelViewMat[1] = -this.mModelViewMat[1];
		this.mModelViewMat[2] = -this.mModelViewMat[2];
		this.mModelViewMat[3] = -this.mModelViewMat[3];
	}

	public final void flipYModelView() {
		this.mModelViewMat[4] = -this.mModelViewMat[4];
		this.mModelViewMat[5] = -this.mModelViewMat[5];
		this.mModelViewMat[6] = -this.mModelViewMat[6];
		this.mModelViewMat[7] = -this.mModelViewMat[7];
	}

	public final void rotationModelView(int rotation) {
		this.updateModelViewMatrix(IDENTIFY_MATRIX);
		float c = (float)Math.cos((double)rotation * 3.1415926D / 180.0D);
		float s = (float)Math.sin((double)rotation * 3.1415926D / 180.0D);
		this.mModelViewMat[0] = c;
		this.mModelViewMat[1] = -s;
		this.mModelViewMat[4] = s;
		this.mModelViewMat[5] = c;
	}

	public final void updateTextureMatrix(float[] matrix) {
		for(int i = 0; i < matrix.length; ++i) {
			this.mTextureMat[i] = matrix[i];
		}

	}

	public void process(Photo in, Photo out) {
		if (this.mNextGlslFilter == null) {
			this.processInner(in, out);
		} else {
			if (this.mMiddlePhoto == null) {
				Photo tmp = in;
				if (in == null) {
					tmp = out;
				}

				if (tmp != null) {
					this.mMiddlePhoto = Photo.create(tmp.width(), tmp.height());
				}
			}

			this.processInner(in, this.mMiddlePhoto);
			this.mNextGlslFilter.processInner(this.mMiddlePhoto, out);
		}

	}

	private void processInner(Photo in, Photo out) {
		if (this.shaderProgram != 0) {
			if (out == null) {
				GLES20.glBindFramebuffer(36160, 0);
			} else {
				if (this.frameBufferObjectId[0] == 0) {
					GLES20.glGenFramebuffers(1, this.frameBufferObjectId, 0);
				}

				GLES20.glActiveTexture(33984);
				GLES20.glBindTexture(3553, out.texture());
				GLES20.glTexParameteri(3553, 10240, 9729);
				GLES20.glTexParameteri(3553, 10241, 9729);
				GLES20.glTexParameteri(3553, 10242, 33071);
				GLES20.glTexParameteri(3553, 10243, 33071);
				GLES20.glTexImage2D(3553, 0, 6408, out.width(), out.height(), 0, 6408, 5121, (Buffer)null);
				GLES20.glBindFramebuffer(36160, this.frameBufferObjectId[0]);
				GLES20.glFramebufferTexture2D(36160, 36064, 3553, out.texture(), 0);
				checkGlError("glBindFramebuffer");
			}

			GLES20.glUseProgram(this.shaderProgram);
			checkGlError("glUseProgram");
			GLES20.glViewport(0, 0, out.width(), out.height());
			checkGlError("glViewport");
			GLES20.glDisable(3042);
			GLES20.glVertexAttribPointer(this.texCoordHandle, 2, 5126, false, 0, this.texVertices);
			GLES20.glEnableVertexAttribArray(this.texCoordHandle);
			GLES20.glVertexAttribPointer(this.posCoordHandle, 3, 5126, false, 0, this.posVertices);
			GLES20.glEnableVertexAttribArray(this.posCoordHandle);
			checkGlError("vertex attribute setup");
			if (in != null && this.texSamplerHandle >= 0) {
				GLES20.glActiveTexture(33984);
				checkGlError("glActiveTexture");
				GLES20.glBindTexture(this.mInputTextureType, in.texture());
				checkGlError("glBindTexture");
				GLES20.glTexParameteri(this.mInputTextureType, 10240, 9729);
				GLES20.glTexParameteri(this.mInputTextureType, 10241, 9729);
				GLES20.glTexParameteri(this.mInputTextureType, 10242, 33071);
				GLES20.glTexParameteri(this.mInputTextureType, 10243, 33071);
				GLES20.glUniform1i(this.texSamplerHandle, 0);
				checkGlError("texSamplerHandle");
			}

			GLES20.glUniformMatrix4fv(this.texCoordMatHandle, 1, false, this.mTextureMat, 0);
			checkGlError("texCoordMatHandle");
			GLES20.glUniformMatrix4fv(this.modelViewMatHandle, 1, false, this.mModelViewMat, 0);
			checkGlError("modelViewMatHandle");
			this.updateParams();
			GLES20.glDrawArrays(6, 0, 4);
			checkGlError("glDrawArrays");
			GLES20.glFinish();
			if (out != null) {
				GLES20.glFramebufferTexture2D(36160, 36064, 3553, 0, 0);
				GLES20.glBindFramebuffer(36160, 0);
			}

			checkGlError("after process");
		}
	}

	private static int loadShader(int shaderType, String source) {
		int shader = GLES20.glCreateShader(shaderType);
		if (shader != 0) {
			GLES20.glShaderSource(shader, source);
			GLES20.glCompileShader(shader);
			int[] compiled = new int[1];
			GLES20.glGetShaderiv(shader, 35713, compiled, 0);
			if (compiled[0] == 0) {
				String info = GLES20.glGetShaderInfoLog(shader);
				GLES20.glDeleteShader(shader);
				// int shader = false;
				throw new RuntimeException("Could not compile shader " + shaderType + ":" + info);
			}
		}

		return shader;
	}

	private static FloatBuffer createVerticesBuffer(float[] vertices) {
		FloatBuffer buffer = ByteBuffer.allocateDirect(vertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		buffer.put(vertices).position(0);
		return buffer;
	}

	public static void checkGlError(String op) {
		int error;
		if ((error = GLES20.glGetError()) != 0) {
			throw new RuntimeException(getError(op, error));
		}
	}

	public static String getError(String message, int status) {
		StringBuffer sb = new StringBuffer();
		sb.append(message).append(" - ");
		switch(status) {
			case 0:
				sb.append("No errors.");
				break;
			case 1280:
				sb.append("Invalid enum");
				break;
			case 1281:
				sb.append("Invalid value");
				break;
			case 1282:
				sb.append("Invalid operation");
				break;
			case 1286:
				sb.append("OpenGL invalid framebuffer operation.");
				break;
			case 36053:
				sb.append("Framebuffer complete.");
				break;
			case 36054:
				sb.append("OpenGL framebuffer attached images must have same format.");
				break;
			case 36055:
				sb.append("OpenGL framebuffer missing attachment.");
				break;
			case 36057:
				sb.append("OpenGL framebuffer attached images must have same dimensions.");
				break;
			case 36061:
				sb.append("OpenGL framebuffer format not supported. ");
				break;
			default:
				sb.append("OpenGL error: " + status);
		}

		return sb.toString();
	}
}
