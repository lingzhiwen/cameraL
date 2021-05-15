package com.ling.video;

import android.content.Context;
import android.opengl.GLES20;

public class YUVFilter extends GlslFilter {
    private final String YUV_FRAGMENT_SHADER_STRING = "precision mediump float;\nvarying vec2 textureCoordinate;\nuniform sampler2D y_tex;\nuniform sampler2D u_tex;\nuniform sampler2D v_tex;\nvoid main() {\n  float y = texture2D(y_tex, textureCoordinate).r;\n  float u = texture2D(u_tex, textureCoordinate).r - 0.5;\n  float v = texture2D(v_tex, textureCoordinate).r - 0.5;\n  gl_FragColor = vec4(y + 1.403 * v,                       y - 0.344 * u - 0.714 * v,                       y + 1.77 * u, 1.0);\n}\n";
    int texYHandle;
    int texUHandle;
    int texVHandle;
    int[] textures;

    public YUVFilter(Context context) {
        super(context);
    }

    public String fragmentShader() {
        return "precision mediump float;\nvarying vec2 textureCoordinate;\nuniform sampler2D y_tex;\nuniform sampler2D u_tex;\nuniform sampler2D v_tex;\nvoid main() {\n  float y = texture2D(y_tex, textureCoordinate).r;\n  float u = texture2D(u_tex, textureCoordinate).r - 0.5;\n  float v = texture2D(v_tex, textureCoordinate).r - 0.5;\n  gl_FragColor = vec4(y + 1.403 * v,                       y - 0.344 * u - 0.714 * v,                       y + 1.77 * u, 1.0);\n}\n";
    }

    protected void prepareParams() {
        super.prepareParams();
        this.texYHandle = GLES20.glGetUniformLocation(this.shaderProgram, "y_tex");
        this.texUHandle = GLES20.glGetUniformLocation(this.shaderProgram, "u_tex");
        this.texVHandle = GLES20.glGetUniformLocation(this.shaderProgram, "v_tex");
    }

    protected void updateParams() {
        super.updateParams();
        checkGlError("setYuvTextures");
        GLES20.glActiveTexture(33984);
        GLES20.glBindTexture(3553, this.textures[0]);
        GLES20.glTexParameterf(3553, 10241, 9729.0F);
        GLES20.glTexParameterf(3553, 10240, 9729.0F);
        GLES20.glTexParameterf(3553, 10242, 33071.0F);
        GLES20.glTexParameterf(3553, 10243, 33071.0F);
        GLES20.glUniform1i(this.texYHandle, 0);
        checkGlError("glBindTexture y");
        GLES20.glActiveTexture(33985);
        GLES20.glBindTexture(3553, this.textures[1]);
        GLES20.glTexParameterf(3553, 10240, 9729.0F);
        GLES20.glTexParameterf(3553, 10242, 33071.0F);
        GLES20.glTexParameterf(3553, 10243, 33071.0F);
        GLES20.glUniform1i(this.texUHandle, 1);
        checkGlError("glBindTexture u");
        GLES20.glActiveTexture(33986);
        GLES20.glBindTexture(3553, this.textures[2]);
        GLES20.glTexParameterf(3553, 10240, 9729.0F);
        GLES20.glTexParameterf(3553, 10242, 33071.0F);
        GLES20.glTexParameterf(3553, 10243, 33071.0F);
        GLES20.glUniform1i(this.texVHandle, 2);
        checkGlError("glBindTexture v");
    }

    public void setYuvTextures(int[] mYUVTextures) {
        this.textures = mYUVTextures;
    }
}
