package com.ling.video;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.renderscript.Matrix4f;
import android.util.Log;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Map;

public class RendererUtils {
    private static int[] frame = new int[1];
    private static final float[] TEX_VERTICES = new float[]{0.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F};
    private static final float[] POS_VERTICES = new float[]{-1.0F, -1.0F, 1.0F, -1.0F, -1.0F, 1.0F, 1.0F, 1.0F};
    private static final String VERTEX_SHADER = "attribute vec4 a_position;\nattribute vec2 a_texcoord;\nuniform mat4 u_model_view; \nvarying vec2 v_texcoord;\nvoid main() {\n  gl_Position = u_model_view*a_position;\n  v_texcoord = a_texcoord;\n}\n";
    private static final String FRAGMENT_SHADER = "precision mediump float;\nuniform sampler2D tex_sampler;\nuniform float alpha;\nvarying vec2 v_texcoord;\nvoid main() {\nvec4 color = texture2D(tex_sampler, v_texcoord);\ngl_FragColor = color;\n}\n";
    private static final int FLOAT_SIZE_BYTES = 4;
    private static final float DEGREE_TO_RADIAN = 0.017453292F;

    public RendererUtils() {
    }

    public static int createTexture() {
        int[] textures = new int[1];
        GLES20.glGenTextures(textures.length, textures, 0);
        checkGlError("glGenTextures");
        return textures[0];
    }

    public static int createTexture(Bitmap bitmap) {
        int texture = createTexture();
        GLES20.glBindTexture(3553, texture);
        int internalFormat = GLUtils.getInternalFormat(bitmap);
        int type = GLUtils.getType(bitmap);
        GLUtils.texImage2D(3553, 0, internalFormat, bitmap, type, 0);
        GLES20.glTexParameteri(3553, 10240, 9729);
        GLES20.glTexParameteri(3553, 10241, 9729);
        GLES20.glTexParameteri(3553, 10242, 33071);
        GLES20.glTexParameteri(3553, 10243, 33071);
        checkGlError("texImage2D");
        return texture;
    }

    public static int createTexture(int texture, Bitmap bitmap) {
        if (texture < 0) {
            texture = createTexture();
        }

        GLES20.glBindTexture(3553, texture);
        int internalFormat = GLUtils.getInternalFormat(bitmap);
        int type = GLUtils.getType(bitmap);
        GLUtils.texImage2D(3553, 0, internalFormat, bitmap, type, 0);
        GLES20.glTexParameteri(3553, 10240, 9729);
        GLES20.glTexParameteri(3553, 10241, 9729);
        GLES20.glTexParameteri(3553, 10242, 33071);
        GLES20.glTexParameteri(3553, 10243, 33071);
        checkGlError("texImage2D");
        return texture;
    }

    public static Bitmap saveTexture(int texture, int width, int height) {
        int[] frame = new int[1];
        GLES20.glGenFramebuffers(1, frame, 0);
        checkGlError("glGenFramebuffers");
        GLES20.glBindFramebuffer(36160, frame[0]);
        checkGlError("glBindFramebuffer");
        GLES20.glFramebufferTexture2D(36160, 36064, 3553, texture, 0);
        checkGlError("glFramebufferTexture2D");
        ByteBuffer buffer = ByteBuffer.allocate(width * height * 4);
        GLES20.glReadPixels(0, 0, width, height, 6408, 5121, buffer);
        checkGlError("glReadPixels");
        Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        GLES20.glBindFramebuffer(36160, 0);
        checkGlError("glBindFramebuffer");
        GLES20.glDeleteFramebuffers(1, frame, 0);
        checkGlError("glDeleteFramebuffer");
        return bitmap;
    }

    public static void clearTexture(int texture) {
        int[] textures = new int[]{texture};
        GLES20.glDeleteTextures(textures.length, textures, 0);
        checkGlError("glDeleteTextures");
    }

    private static float[] getFitVertices(int srcWidth, int srcHeight, int dstWidth, int dstHeight) {
        float srcAspectRatio = (float)srcWidth / (float)srcHeight;
        float dstAspectRatio = (float)dstWidth / (float)dstHeight;
        float relativeAspectRatio = dstAspectRatio / srcAspectRatio;
        float[] vertices = new float[8];
        System.arraycopy(POS_VERTICES, 0, vertices, 0, vertices.length);
        if (relativeAspectRatio > 1.0F) {
            vertices[0] /= relativeAspectRatio;
            vertices[2] /= relativeAspectRatio;
            vertices[4] /= relativeAspectRatio;
            vertices[6] /= relativeAspectRatio;
        } else {
            vertices[1] *= relativeAspectRatio;
            vertices[3] *= relativeAspectRatio;
            vertices[5] *= relativeAspectRatio;
            vertices[7] *= relativeAspectRatio;
        }

        return vertices;
    }

    public static void setRenderMatrix(RendererUtils.RenderContext context, float[] matrix) {
        context.mModelViewMat = matrix;
    }

    public static void setRenderToFit(RendererUtils.RenderContext context, int srcWidth, int srcHeight, int dstWidth, int dstHeight) {
        context.posVertices = createVerticesBuffer(getFitVertices(srcWidth, srcHeight, dstWidth, dstHeight));
    }

    public static void setRenderToFit(RendererUtils.RenderContext context, int srcWidth, int srcHeight, int dstWidth, int dstHeight, float offsetx, float offsetY, float scale) {
        Matrix4f matrix4f = new Matrix4f();
        float srcAspectRatio = (float)srcWidth / (float)srcHeight;
        float dstAspectRatio = (float)dstWidth / (float)dstHeight;
        float relativeAspectRatio = dstAspectRatio / srcAspectRatio;
        float ratioscale = 1.0F;
        float x;
        float y;
        if (relativeAspectRatio > 1.0F) {
            ratioscale = srcAspectRatio / dstAspectRatio;
            matrix4f.scale(ratioscale * scale, scale, 0.0F);
            x = -offsetx / ((float)srcHeight * scale);
            y = offsetY / ((float)srcHeight * scale);
            matrix4f.translate(x, y, 0.0F);
        } else {
            matrix4f.scale(scale, relativeAspectRatio * scale, 0.0F);
            x = -offsetx / ((float)srcWidth * scale);
            y = offsetY / ((float)srcWidth * scale);
            matrix4f.translate(x, y, 0.0F);
        }

        context.mModelViewMat = matrix4f.getArray();
    }

    public static void setRenderToAlpha(RendererUtils.RenderContext context, int alpha) {
        context.alpha = (float)alpha / 255.0F;
    }

    public static void setRenderToRotate(RendererUtils.RenderContext context, int srcWidth, int srcHeight, int dstWidth, int dstHeight, float degrees) {
        float radian = -degrees * 0.017453292F;
        float cosTheta = (float)Math.cos((double)radian);
        float sinTheta = (float)Math.sin((double)radian);
        float cosWidth = cosTheta * (float)srcWidth;
        float sinWidth = sinTheta * (float)srcWidth;
        float cosHeight = cosTheta * (float)srcHeight;
        float sinHeight = sinTheta * (float)srcHeight;
        float[] vertices = new float[8];
        vertices[0] = -cosWidth + sinHeight;
        vertices[1] = -sinWidth - cosHeight;
        vertices[2] = cosWidth + sinHeight;
        vertices[3] = sinWidth - cosHeight;
        vertices[4] = -vertices[2];
        vertices[5] = -vertices[3];
        vertices[6] = -vertices[0];
        vertices[7] = -vertices[1];
        float maxWidth = Math.max(Math.abs(vertices[0]), Math.abs(vertices[2]));
        float maxHeight = Math.max(Math.abs(vertices[1]), Math.abs(vertices[3]));
        float scale = Math.min((float)dstWidth / maxWidth, (float)dstHeight / maxHeight);

        for(int i = 0; i < 8; i += 2) {
            vertices[i] *= scale / (float)dstWidth;
            vertices[i + 1] *= scale / (float)dstHeight;
        }

        context.posVertices = createVerticesBuffer(vertices);
    }

    public static void setRenderToFlip(RendererUtils.RenderContext context, int srcWidth, int srcHeight, int dstWidth, int dstHeight, float horizontalDegrees, float verticalDegrees) {
        float[] base = getFitVertices(srcWidth, srcHeight, dstWidth, dstHeight);
        int horizontalRounds = (int)horizontalDegrees / 180;
        if (horizontalRounds % 2 != 0) {
            base[0] = -base[0];
            base[4] = base[0];
            base[2] = -base[2];
            base[6] = base[2];
        }

        int verticalRounds = (int)verticalDegrees / 180;
        if (verticalRounds % 2 != 0) {
            base[1] = -base[1];
            base[3] = base[1];
            base[5] = -base[5];
            base[7] = base[5];
        }

        float length = 5.0F;
        float[] vertices = new float[8];
        System.arraycopy(base, 0, vertices, 0, vertices.length);
        float radian;
        float cosTheta;
        float sinTheta;
        float scale;
        if (horizontalDegrees % 180.0F != 0.0F) {
            radian = (horizontalDegrees - (float)(horizontalRounds * 180)) * 0.017453292F;
            cosTheta = (float)Math.cos((double)radian);
            sinTheta = (float)Math.sin((double)radian);
            scale = length / (length + sinTheta * base[0]);
            vertices[0] = cosTheta * base[0] * scale;
            vertices[1] = base[1] * scale;
            vertices[4] = vertices[0];
            vertices[5] = base[5] * scale;
            scale = length / (length + sinTheta * base[2]);
            vertices[2] = cosTheta * base[2] * scale;
            vertices[3] = base[3] * scale;
            vertices[6] = vertices[2];
            vertices[7] = base[7] * scale;
        }

        if (verticalDegrees % 180.0F != 0.0F) {
            radian = (verticalDegrees - (float)(verticalRounds * 180)) * 0.017453292F;
            cosTheta = (float)Math.cos((double)radian);
            sinTheta = (float)Math.sin((double)radian);
            scale = length / (length + sinTheta * base[1]);
            vertices[0] = base[0] * scale;
            vertices[1] = cosTheta * base[1] * scale;
            vertices[2] = base[2] * scale;
            vertices[3] = vertices[1];
            scale = length / (length + sinTheta * base[5]);
            vertices[4] = base[4] * scale;
            vertices[5] = cosTheta * base[5] * scale;
            vertices[6] = base[6] * scale;
            vertices[7] = vertices[5];
        }

        context.posVertices = createVerticesBuffer(vertices);
    }

    public static void renderBackground() {
        GLES20.glClearColor(0.10588F, 0.109804F, 0.12157F, 1.0F);
        GLES20.glClear(16384);
    }

    public static void renderTexture(RendererUtils.RenderContext context, int texture, int viewWidth, int viewHeight) {
        GLES20.glUseProgram(context.shaderProgram);
        if (GLES20.glGetError() != 0) {
            createProgram();
            checkGlError("createProgram");
        }

        GLES20.glViewport(0, 0, viewWidth, viewHeight);
        checkGlError("glViewport");
        GLES20.glDisable(3042);
        GLES20.glVertexAttribPointer(context.texCoordHandle, 2, 5126, false, 0, context.texVertices);
        GLES20.glEnableVertexAttribArray(context.texCoordHandle);
        GLES20.glVertexAttribPointer(context.posCoordHandle, 2, 5126, false, 0, context.posVertices);
        GLES20.glEnableVertexAttribArray(context.posCoordHandle);
        checkGlError("vertex attribute setup");
        GLES20.glActiveTexture(33984);
        checkGlError("glActiveTexture");
        GLES20.glBindTexture(3553, texture);
        GLES20.glTexParameteri(3553, 10240, 9729);
        GLES20.glTexParameteri(3553, 10241, 9729);
        GLES20.glTexParameteri(3553, 10242, 33071);
        GLES20.glTexParameteri(3553, 10243, 33071);
        checkGlError("glBindTexture");
        GLES20.glUniform1i(context.texSamplerHandle, 0);
        GLES20.glUniform1f(context.alphaHandle, context.alpha);
        GLES20.glUniformMatrix4fv(context.modelViewMatHandle, 1, false, context.mModelViewMat, 0);
        checkGlError("modelViewMatHandle");
        GLES20.glDrawArrays(5, 0, 4);
        GLES20.glFinish();
    }

    private static RendererUtils.RenderContext createProgram(float[] vertex, float[] tex) {
        int vertexShader = loadShader(35633, "attribute vec4 a_position;\nattribute vec2 a_texcoord;\nuniform mat4 u_model_view; \nvarying vec2 v_texcoord;\nvoid main() {\n  gl_Position = u_model_view*a_position;\n  v_texcoord = a_texcoord;\n}\n");
        if (vertexShader == 0) {
            return null;
        } else {
            int pixelShader = loadShader(35632, "precision mediump float;\nuniform sampler2D tex_sampler;\nuniform float alpha;\nvarying vec2 v_texcoord;\nvoid main() {\nvec4 color = texture2D(tex_sampler, v_texcoord);\ngl_FragColor = color;\n}\n");
            if (pixelShader == 0) {
                return null;
            } else {
                int program = GLES20.glCreateProgram();
                if (program != 0) {
                    GLES20.glAttachShader(program, vertexShader);
                    checkGlError("glAttachShader");
                    GLES20.glAttachShader(program, pixelShader);
                    checkGlError("glAttachShader");
                    GLES20.glLinkProgram(program);
                    int[] linkStatus = new int[1];
                    GLES20.glGetProgramiv(program, 35714, linkStatus, 0);
                    if (linkStatus[0] != 1) {
                        String info = GLES20.glGetProgramInfoLog(program);
                        GLES20.glDeleteProgram(program);
                        // int program = false;
                        throw new RuntimeException("Could not link program: " + info);
                    }
                }

                RendererUtils.RenderContext context = new RendererUtils.RenderContext();
                context.texSamplerHandle = GLES20.glGetUniformLocation(program, "tex_sampler");
                context.alphaHandle = GLES20.glGetUniformLocation(program, "alpha");
                context.texCoordHandle = GLES20.glGetAttribLocation(program, "a_texcoord");
                context.posCoordHandle = GLES20.glGetAttribLocation(program, "a_position");
                context.modelViewMatHandle = GLES20.glGetUniformLocation(program, "u_model_view");
                context.texVertices = createVerticesBuffer(tex);
                context.posVertices = createVerticesBuffer(vertex);
                context.shaderProgram = program;
                return context;
            }
        }
    }

    public static RendererUtils.RenderContext createProgram() {
        return createProgram(POS_VERTICES, TEX_VERTICES);
    }

    public static void releaseRenderContext(RendererUtils.RenderContext context) {
        if (context != null && context.shaderProgram > 0) {
            GLES20.glDeleteProgram(context.shaderProgram);
            context.shaderProgram = 0;
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

    public static FloatBuffer createVerticesBuffer(float[] vertices) {
        if (vertices.length != 8) {
            throw new RuntimeException("Number of vertices should be four.");
        } else {
            FloatBuffer buffer = ByteBuffer.allocateDirect(vertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            buffer.put(vertices).position(0);
            return buffer;
        }
    }

    public static String getEGLErrorString(int error) {
        switch(error) {
            case 12288:
                return "EGL_SUCCESS";
            case 12289:
                return "EGL_NOT_INITIALIZED";
            case 12290:
                return "EGL_BAD_ACCESS";
            case 12291:
                return "EGL_BAD_ALLOC";
            case 12292:
                return "EGL_BAD_ATTRIBUTE";
            case 12293:
                return "EGL_BAD_CONFIG";
            case 12294:
                return "EGL_BAD_CONTEXT";
            case 12295:
                return "EGL_BAD_CURRENT_SURFACE";
            case 12296:
                return "EGL_BAD_DISPLAY";
            case 12297:
                return "EGL_BAD_MATCH";
            case 12298:
                return "EGL_BAD_NATIVE_PIXMAP";
            case 12299:
                return "EGL_BAD_NATIVE_WINDOW";
            case 12300:
                return "EGL_BAD_PARAMETER";
            case 12301:
                return "EGL_BAD_SURFACE";
            case 12302:
                return "EGL_CONTEXT_LOST";
            default:
                return " " + error;
        }
    }

    public static void checkGlError(String op) {
        int error;
        if ((error = GLES20.glGetError()) != 0) {
            Log.e("RendererUtils", op + ": glError " + getEGLErrorString(error));
            Map<Thread, StackTraceElement[]> ts = Thread.getAllStackTraces();
            StackTraceElement[] ste = (StackTraceElement[])ts.get(Thread.currentThread());
            StackTraceElement[] var4 = ste;
            int var5 = ste.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                StackTraceElement s = var4[var6];
                Log.e("SS     ", s.toString());
            }
        }

    }

    public static void createFrame() {
        GLES20.glGenFramebuffers(1, frame, 0);
        checkGlError("glGenFramebuffers");
    }

    public static void deleteFrame() {
        GLES20.glDeleteFramebuffers(1, frame, 0);
        checkGlError("glDeleteFramebuffer");
    }

    public static int createFilterProgram(String vertexSource, String fragSource) {
        int vertexShader = loadShader(35633, vertexSource == null ? "attribute vec4 a_position;\nattribute vec2 a_texcoord;\nuniform mat4 u_model_view; \nvarying vec2 v_texcoord;\nvoid main() {\n  gl_Position = u_model_view*a_position;\n  v_texcoord = a_texcoord;\n}\n" : vertexSource);
        if (vertexShader == 0) {
            return 0;
        } else {
            int pixelShader = loadShader(35632, fragSource);
            if (pixelShader == 0) {
                return 0;
            } else {
                int program = GLES20.glCreateProgram();
                if (program != 0) {
                    GLES20.glAttachShader(program, vertexShader);
                    checkGlError("glAttachShader");
                    GLES20.glAttachShader(program, pixelShader);
                    checkGlError("glAttachShader");
                    GLES20.glLinkProgram(program);
                    int[] linkStatus = new int[1];
                    GLES20.glGetProgramiv(program, 35714, linkStatus, 0);
                    if (linkStatus[0] != 1) {
                        String info = GLES20.glGetProgramInfoLog(program);
                        GLES20.glDeleteProgram(program);
                        // int program = false;
                        throw new RuntimeException("Could not link program: " + info);
                    }
                }

                return program;
            }
        }
    }

    public static void renderTexture2FBO(RendererUtils.FilterContext context, int texture, int dstTexture, int viewWidth, int viewHeight) {
        GLES20.glActiveTexture(33984);
        checkGlError("glActiveTexture");
        GLES20.glBindTexture(3553, dstTexture);
        checkGlError("glBindTexture");
        GLES20.glTexImage2D(3553, 0, 6408, viewWidth, viewHeight, 0, 6408, 5121, (Buffer)null);
        checkGlError("glTexImage2D");
        GLES20.glBindFramebuffer(36160, frame[0]);
        checkGlError("glBindFramebuffer");
        GLES20.glFramebufferTexture2D(36160, 36064, 3553, dstTexture, 0);
        checkGlError("glFramebufferTexture2D");
        GLES20.glViewport(0, 0, viewWidth, viewHeight);
        checkGlError("glViewport");
        GLES20.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
        GLES20.glClear(16384);
        GLES20.glUseProgram(context.shaderProgram);
        if (GLES20.glGetError() != 0) {
            checkGlError("createProgram");
        }

        GLES20.glVertexAttribPointer(context.texCoordHandle, 2, 5126, false, 0, context.texVertices);
        GLES20.glEnableVertexAttribArray(context.texCoordHandle);
        GLES20.glVertexAttribPointer(context.posCoordHandle, 2, 5126, false, 0, context.posVertices);
        GLES20.glEnableVertexAttribArray(context.posCoordHandle);
        checkGlError("vertex attribute setup");
        GLES20.glUniform1i(context.texSamplerHandle, 0);
        checkGlError("glUniform1i");
        GLES20.glActiveTexture(33984);
        checkGlError("glActiveTexture");
        GLES20.glBindTexture(3553, texture);
        checkGlError("glBindTexture");
        GLES20.glTexParameteri(3553, 10240, 9729);
        GLES20.glTexParameteri(3553, 10241, 9729);
        GLES20.glTexParameteri(3553, 10242, 33071);
        GLES20.glTexParameteri(3553, 10243, 33071);
        GLES20.glDrawArrays(5, 0, 4);
        GLES20.glFinish();
        GLES20.glBindFramebuffer(36160, 0);
        checkGlError("glBindFramebuffer");
        deleteProgram(context.shaderProgram);
    }

    public static void deleteProgram(int id) {
        GLES20.glDeleteProgram(id);
    }

    public static class FilterContext {
        public int shaderProgram;
        public int texSamplerHandle;
        public int texCoordHandle;
        public int posCoordHandle;
        public FloatBuffer texVertices;
        public FloatBuffer posVertices;

        public FilterContext() {
        }
    }

    public static class RenderContext {
        private int shaderProgram;
        private int texSamplerHandle;
        private int alphaHandle;
        private int texCoordHandle;
        private int posCoordHandle;
        private FloatBuffer texVertices;
        private FloatBuffer posVertices;
        private float alpha = 1.0F;
        private int modelViewMatHandle;
        float[] mModelViewMat = new float[]{1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F};

        public RenderContext() {
        }
    }
}
