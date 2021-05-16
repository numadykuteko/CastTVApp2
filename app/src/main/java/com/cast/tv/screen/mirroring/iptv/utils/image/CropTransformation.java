package com.cast.tv.screen.mirroring.iptv.utils.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;

import java.security.MessageDigest;

public class CropTransformation extends BitmapTransformation {

    private static final int VERSION = 1;
    private static final String ID = "CropTransformation." + VERSION;

    private int height, width;

    public CropTransformation(int height) {
        this.height = height;
    }

    @Override
    protected Bitmap transform(@NonNull Context context, @NonNull BitmapPool pool,
                               @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        width = toTransform.getWidth();
        height = toTransform.getHeight() - height;

        Bitmap.Config config =
                toTransform.getConfig() != null ? toTransform.getConfig() : Bitmap.Config.ARGB_8888;
        Bitmap bitmap = pool.get(width, height, config);

        bitmap.setHasAlpha(true);

        float scaleX = (float) width / toTransform.getWidth();
        float scaleY = (float) height / toTransform.getHeight();
        float scale = Math.max(scaleX, scaleY);

        float scaledWidth = scale * toTransform.getWidth();
        float scaledHeight = scale * toTransform.getHeight();
        float left = (width - scaledWidth) / 2;
        float top = getTop(scaledHeight);

        RectF targetRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);

        setCanvasBitmapDensity(toTransform, bitmap);

        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(toTransform, null, targetRect, null);

        return bitmap;
    }

    private float getTop(float scaledHeight) {
        return (height - scaledHeight) / 2;
    }

    @Override
    public String toString() {
        return "CropTransformation(width=" + width + ", height=" + height + ")";
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof CropTransformation &&
                ((CropTransformation) o).width == width &&
                ((CropTransformation) o).height == height;
    }

    @Override
    public int hashCode() {
        return ID.hashCode() + width * 100000 + height * 1000;
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update((ID + width + height).getBytes(CHARSET));
    }
}

