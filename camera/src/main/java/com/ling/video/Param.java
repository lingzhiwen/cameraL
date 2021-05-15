package com.ling.video;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import java.io.IOException;
import java.io.InputStream;

public class Param {
    public int handle;
    public String name;

    public Param(String name) {
        this.name = name;
    }

    public void setParams(int program) {
        this.handle = GLES20.glGetUniformLocation(program, this.name);
    }

    public void clear() {
    }

    public String toString() {
        return this.name;
    }

    public static class TextureFileParam extends Param.TextureParam {
        Context mContext;
        String mTextureFile;

        public TextureFileParam(String name, String textureFile, Context context, int textureId) {
            super(name, (Bitmap)null, textureId);
            this.mContext = context;
            this.mTextureFile = textureFile;
        }

        public void setParams(int program) {
            if (this.textureBitmap == null) {
                try {
                    InputStream is = this.mContext.getAssets().open(this.mTextureFile);
                    this.textureBitmap = BitmapFactory.decodeStream(is);
                    is.close();
                } catch (IOException var4) {
                    var4.printStackTrace();
                }
            }

            super.setParams(program);
        }
    }

    public static class TextureValueParam extends Param {
        int textureValue;
        int textureId;

        public TextureValueParam(String name, int textureValue, int textureId) {
            super(name);
            this.textureValue = textureValue;
            this.textureId = textureId;
        }

        public void clear() {
            super.clear();
            GLES20.glActiveTexture(this.textureId);
        }

        public void setParams(int program) {
            super.setParams(program);
            if (this.handle != 0 && this.textureValue != 0) {
                GLES20.glActiveTexture(this.textureId);
                GLES20.glBindTexture(3553, this.textureValue);
                GLES20.glTexParameterf(3553, 10240, 9728.0F);
                GLES20.glTexParameterf(3553, 10241, 9728.0F);
                GLES20.glTexParameterf(3553, 10242, 33071.0F);
                GLES20.glTexParameterf(3553, 10243, 33071.0F);
                GlslFilter.checkGlError("texImage2D");
                int textureIndex = 0;
                switch(this.textureId) {
                    case 33984:
                        textureIndex = 0;
                        break;
                    case 33985:
                        textureIndex = 1;
                        break;
                    case 33986:
                        textureIndex = 2;
                        break;
                    case 33987:
                        textureIndex = 3;
                        break;
                    case 33988:
                        textureIndex = 4;
                        break;
                    case 33989:
                        textureIndex = 5;
                        break;
                    case 33990:
                        textureIndex = 6;
                        break;
                    case 33991:
                        textureIndex = 7;
                }

                GLES20.glUniform1i(this.handle, textureIndex);
                GlslFilter.checkGlError("set texture:" + textureIndex);
            }
        }
    }

    public static class TextureParam extends Param {
        Bitmap textureBitmap;
        int textureId;
        int[] texture = new int[]{0};

        public TextureParam(String name, Bitmap textureBitmap, int textureId) {
            super(name);
            this.textureBitmap = textureBitmap;
            this.textureId = textureId;
        }

        public void clear() {
            super.clear();
            GLES20.glActiveTexture(this.textureId);
            GLES20.glDeleteTextures(1, this.texture, 0);
            this.texture[0] = 0;
        }

        public void setParams(int program) {
            super.setParams(program);
            if (this.handle != 0 && this.textureBitmap != null) {
                GLES20.glActiveTexture(this.textureId);
                GLES20.glGenTextures(1, this.texture, 0);
                GLES20.glBindTexture(3553, this.texture[0]);
                GLES20.glTexParameterf(3553, 10240, 9728.0F);
                GLES20.glTexParameterf(3553, 10241, 9728.0F);
                GLES20.glTexParameterf(3553, 10242, 33071.0F);
                GLES20.glTexParameterf(3553, 10243, 33071.0F);
                GLUtils.texImage2D(3553, 0, this.textureBitmap, 0);
                GlslFilter.checkGlError("texImage2D");
                int textureIndex = 0;
                switch(this.textureId) {
                    case 33984:
                        textureIndex = 0;
                        break;
                    case 33985:
                        textureIndex = 1;
                        break;
                    case 33986:
                        textureIndex = 2;
                        break;
                    case 33987:
                        textureIndex = 3;
                        break;
                    case 33988:
                        textureIndex = 4;
                        break;
                    case 33989:
                        textureIndex = 5;
                        break;
                    case 33990:
                        textureIndex = 6;
                        break;
                    case 33991:
                        textureIndex = 7;
                }

                GLES20.glUniform1i(this.handle, textureIndex);
                GlslFilter.checkGlError("set texture:" + textureIndex);
            }
        }
    }

    public static class HueFloatParam extends Param.VarFloatParam {
        ColorSpaceMatrix mColorSpaceMatrix = new ColorSpaceMatrix();
        float[] mMatrix;

        public HueFloatParam(String name, float value, float min, float max) {
            super(name, value, min, max);
            this.mMatrix = this.mColorSpaceMatrix.getMatrix();
        }

        public void setParams(int program) {
            this.handle = GLES20.glGetUniformLocation(program, this.name);
            this.mColorSpaceMatrix.identity();
            this.mColorSpaceMatrix.setHue(this.value);
            this.mMatrix = this.mColorSpaceMatrix.getMatrix();
            GLES20.glUniformMatrix4fv(this.handle, 1, false, this.mMatrix, 0);
        }

        public String toString() {
            return this.name + "=" + this.mMatrix.toString();
        }
    }

    public static class VarFloatParam extends Param.FloatParam {
        float min;
        float max;

        public VarFloatParam(String name, float value, float min, float max) {
            super(name, value);
            this.min = min;
            this.max = max;
        }

        public void setValue(float value) {
            this.value = value;
        }

        public float getValue() {
            return this.value;
        }

        public String getName() {
            return this.name;
        }

        public void setProgress(int progress) {
            this.value = this.min + (float)progress * (this.max - this.min) / 100.0F;
        }

        public int getProgress() {
            return (int)((this.value - this.min) * 100.0F / (this.max - this.min));
        }
    }

    public static class RectParam extends Param {
        float[] value;

        public RectParam(String name, float[] value) {
            super(name);
            this.value = value;
        }

        public String toString() {
            return this.name + "=" + this.value.toString();
        }

        public void setParams(int program) {
            super.setParams(program);
            if (this.handle >= 0) {
                GLES20.glUniform4fv(this.handle, this.value.length / 4, this.value, 0);
            }
        }
    }

    public static class FloatsParam extends Param {
        float[] value;

        public FloatsParam(String name, float[] value) {
            super(name);
            this.value = value;
        }

        public String toString() {
            return this.name + "=" + this.value.toString();
        }

        public void setParams(int program) {
            super.setParams(program);
            if (this.handle >= 0) {
                switch(this.value.length) {
                    case 1:
                        GLES20.glUniform1f(this.handle, this.value[0]);
                        break;
                    case 2:
                        GLES20.glUniform2fv(this.handle, 1, this.value, 0);
                        break;
                    case 3:
                        GLES20.glUniform3fv(this.handle, 1, this.value, 0);
                        break;
                    case 4:
                        GLES20.glUniform4fv(this.handle, 1, this.value, 0);
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                    case 10:
                    case 11:
                    case 12:
                    case 13:
                    case 14:
                    case 15:
                    default:
                        break;
                    case 9:
                        GLES20.glUniformMatrix3fv(this.handle, 1, false, this.value, 0);
                        break;
                    case 16:
                        GLES20.glUniformMatrix4fv(this.handle, 1, false, this.value, 0);
                }

            }
        }
    }

    public static class IntParam extends Param {
        int value;

        public String toString() {
            return this.name + "=" + this.value;
        }

        public IntParam(String name, int value) {
            super(name);
            this.value = value;
        }

        public void setParams(int program) {
            super.setParams(program);
            if (this.handle >= 0) {
                GLES20.glUniform1i(this.handle, this.value);
            }
        }
    }

    public static class FloatParam extends Param {
        float value;

        public FloatParam(String name, float value) {
            super(name);
            this.value = value;
        }

        public float value() {
            return this.value;
        }

        public void setParams(int program) {
            super.setParams(program);
            if (this.handle >= 0) {
                GLES20.glUniform1f(this.handle, this.value);
            }
        }

        public String toString() {
            return this.name + "=" + this.value;
        }
    }
}
