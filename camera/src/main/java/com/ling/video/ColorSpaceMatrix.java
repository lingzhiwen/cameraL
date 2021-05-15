package com.ling.video;

import java.util.Arrays;

public class ColorSpaceMatrix {
    private final float[] mMatrix = new float[16];
    private static final float RLUM = 0.3086F;
    private static final float GLUM = 0.6094F;
    private static final float BLUM = 0.082F;

    public ColorSpaceMatrix() {
        this.identity();
    }

    public ColorSpaceMatrix(ColorSpaceMatrix matrix) {
        System.arraycopy(matrix.mMatrix, 0, this.mMatrix, 0, matrix.mMatrix.length);
    }

    public float[] getMatrix() {
        return this.mMatrix;
    }

    public void identity() {
        Arrays.fill(this.mMatrix, 0.0F);
        this.mMatrix[0] = this.mMatrix[5] = this.mMatrix[10] = this.mMatrix[15] = 1.0F;
    }

    public void convertToLuminance() {
        this.mMatrix[0] = this.mMatrix[1] = this.mMatrix[2] = 0.3086F;
        this.mMatrix[4] = this.mMatrix[5] = this.mMatrix[6] = 0.6094F;
        this.mMatrix[8] = this.mMatrix[9] = this.mMatrix[10] = 0.082F;
    }

    private void multiply(float[] a) {
        float[] temp = new float[16];

        int y4;
        for(int y = 0; y < 4; ++y) {
            y4 = y * 4;

            for(int x = 0; x < 4; ++x) {
                temp[y4 + x] = this.mMatrix[y4 + 0] * a[x] + this.mMatrix[y4 + 1] * a[4 + x] + this.mMatrix[y4 + 2] * a[8 + x] + this.mMatrix[y4 + 3] * a[12 + x];
            }
        }

        for(y4 = 0; y4 < 16; ++y4) {
            this.mMatrix[y4] = temp[y4];
        }

    }

    private void xRotateMatrix(float rs, float rc) {
        ColorSpaceMatrix c = new ColorSpaceMatrix();
        float[] tmp = c.mMatrix;
        tmp[5] = rc;
        tmp[6] = rs;
        tmp[9] = -rs;
        tmp[10] = rc;
        this.multiply(tmp);
    }

    private void yRotateMatrix(float rs, float rc) {
        ColorSpaceMatrix c = new ColorSpaceMatrix();
        float[] tmp = c.mMatrix;
        tmp[0] = rc;
        tmp[2] = -rs;
        tmp[8] = rs;
        tmp[10] = rc;
        this.multiply(tmp);
    }

    private void zRotateMatrix(float rs, float rc) {
        ColorSpaceMatrix c = new ColorSpaceMatrix();
        float[] tmp = c.mMatrix;
        tmp[0] = rc;
        tmp[1] = rs;
        tmp[4] = -rs;
        tmp[5] = rc;
        this.multiply(tmp);
    }

    private void zShearMatrix(float dx, float dy) {
        ColorSpaceMatrix c = new ColorSpaceMatrix();
        float[] tmp = c.mMatrix;
        tmp[2] = dx;
        tmp[6] = dy;
        this.multiply(tmp);
    }

    public void setHue(float rot) {
        float mag = (float)Math.sqrt(2.0D);
        float xrs = 1.0F / mag;
        float xrc = 1.0F / mag;
        this.xRotateMatrix(xrs, xrc);
        mag = (float)Math.sqrt(3.0D);
        float yrs = -1.0F / mag;
        float yrc = (float)Math.sqrt(2.0D) / mag;
        this.yRotateMatrix(yrs, yrc);
        float lx = this.getRedf(0.3086F, 0.6094F, 0.082F);
        float ly = this.getGreenf(0.3086F, 0.6094F, 0.082F);
        float lz = this.getBluef(0.3086F, 0.6094F, 0.082F);
        float zsx = lx / lz;
        float zsy = ly / lz;
        this.zShearMatrix(zsx, zsy);
        float zrs = (float)Math.sin((double)rot * 3.141592653589793D / 180.0D);
        float zrc = (float)Math.cos((double)rot * 3.141592653589793D / 180.0D);
        this.zRotateMatrix(zrs, zrc);
        this.zShearMatrix(-zsx, -zsy);
        this.yRotateMatrix(-yrs, yrc);
        this.xRotateMatrix(-xrs, xrc);
    }

    public void changeSaturation(float s) {
        this.mMatrix[0] = (1.0F - s) * 0.3086F + s;
        this.mMatrix[1] = (1.0F - s) * 0.3086F;
        this.mMatrix[2] = (1.0F - s) * 0.3086F;
        this.mMatrix[4] = (1.0F - s) * 0.6094F;
        this.mMatrix[5] = (1.0F - s) * 0.6094F + s;
        this.mMatrix[6] = (1.0F - s) * 0.6094F;
        this.mMatrix[8] = (1.0F - s) * 0.082F;
        this.mMatrix[9] = (1.0F - s) * 0.082F;
        this.mMatrix[10] = (1.0F - s) * 0.082F + s;
    }

    public float getRed(int r, int g, int b) {
        return (float)r * this.mMatrix[0] + (float)g * this.mMatrix[4] + (float)b * this.mMatrix[8] + this.mMatrix[12];
    }

    public float getGreen(int r, int g, int b) {
        return (float)r * this.mMatrix[1] + (float)g * this.mMatrix[5] + (float)b * this.mMatrix[9] + this.mMatrix[13];
    }

    public float getBlue(int r, int g, int b) {
        return (float)r * this.mMatrix[2] + (float)g * this.mMatrix[6] + (float)b * this.mMatrix[10] + this.mMatrix[14];
    }

    private float getRedf(float r, float g, float b) {
        return r * this.mMatrix[0] + g * this.mMatrix[4] + b * this.mMatrix[8] + this.mMatrix[12];
    }

    private float getGreenf(float r, float g, float b) {
        return r * this.mMatrix[1] + g * this.mMatrix[5] + b * this.mMatrix[9] + this.mMatrix[13];
    }

    private float getBluef(float r, float g, float b) {
        return r * this.mMatrix[2] + g * this.mMatrix[6] + b * this.mMatrix[10] + this.mMatrix[14];
    }
}
