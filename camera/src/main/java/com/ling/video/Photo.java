package com.ling.video;

import android.graphics.Bitmap;

public class Photo {
    private int texture = -1;
    private int width;
    private int height;

    public static Photo create(Bitmap bitmap) {
        return bitmap != null ? new Photo(RendererUtils.createTexture(bitmap), bitmap.getWidth(), bitmap.getHeight()) : null;
    }

    public static Photo create(int width, int height) {
        return new Photo(RendererUtils.createTexture(), width, height);
    }

    public Photo(int texture, int width, int height) {
        this.texture = texture;
        this.width = width;
        this.height = height;
    }

    public void update(Bitmap bitmap) {
        this.texture = RendererUtils.createTexture(this.texture, bitmap);
        this.width = bitmap.getWidth();
        this.height = bitmap.getHeight();
    }

    public int texture() {
        return this.texture;
    }

    public void setTexture(int texture) {
        RendererUtils.clearTexture(this.texture);
        this.texture = texture;
    }

    public boolean matchDimension(Photo photo) {
        return photo.width == this.width && photo.height == this.height;
    }

    public void changeDimension(int width, int height) {
        this.width = width;
        this.height = height;
        RendererUtils.clearTexture(this.texture);
        this.texture = RendererUtils.createTexture();
    }

    public int width() {
        return this.width;
    }

    public int height() {
        return this.height;
    }

    public Bitmap save() {
        return RendererUtils.saveTexture(this.texture, this.width, this.height);
    }

    public void clear() {
        RendererUtils.clearTexture(this.texture);
        this.texture = -1;
    }

    public void updateSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void swap(Photo photo) {
        int tmp = this.texture;
        this.texture = photo.texture;
        photo.texture = tmp;
    }
}
