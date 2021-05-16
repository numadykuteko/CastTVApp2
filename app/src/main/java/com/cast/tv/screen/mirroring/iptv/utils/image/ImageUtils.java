package com.cast.tv.screen.mirroring.iptv.utils.image;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.request.RequestOptions;

import com.cast.tv.screen.mirroring.iptv.R;
import com.cast.tv.screen.mirroring.iptv.constants.DataConstants;
import com.cast.tv.screen.mirroring.iptv.utils.glide.GlideApp;

import java.io.File;
import java.io.FileOutputStream;

public class ImageUtils {
    public static void loadImageToView(Context context, String url, ImageView imageView) {
        if (!url.equals("")) {
            GlideApp.with(context)
                    .load(url)
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_no_image)
                            .error(R.drawable.ic_no_image)
                            .centerCrop()
                            .override(DataConstants.MAX_SIZE_IMAGE_LOADER_WIDTH, DataConstants.MAX_SIZE_IMAGE_LOADER_HEIGHT)
                    )
                    .into(imageView);
        } else {
            GlideApp.with(context)
                    .load(R.drawable.ic_no_image)
                    .into(imageView);
        }
    }

    public static void loadImageBigSizeToView(Context context, String url, ImageView imageView) {
        if (!url.equals("")) {
            GlideApp.with(context)
                    .load(url)
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_no_image)
                            .error(R.drawable.ic_no_image)
                            .centerCrop()
                    )
                    .into(imageView);
        } else {
            GlideApp.with(context)
                    .load(R.drawable.ic_no_image)
                    .into(imageView);
        }
    }

    public static void loadImageHorizontalToView(Context context, String url, ImageView imageView) {
        if (!url.equals("")) {
            GlideApp.with(context)
                    .load(url)
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_background_splash)
                            .error(R.drawable.ic_background_splash)
                            .centerCrop()
                            .override(DataConstants.HORIZONTAL_IMAGE_LOADER_WIDTH, DataConstants.HORIZONTAL_IMAGE_LOADER_HEIGHT)
                    )
                    .into(imageView);
        } else {
            GlideApp.with(context)
                    .load(R.drawable.ic_no_image)
                    .into(imageView);
        }
    }

    public static void loadImageFromUriToView(Context context, File imageFile, ImageView imageView) {

        if (imageFile != null) {
            GlideApp.with(context)
                    .load(imageFile)
                    .apply(new RequestOptions()
                            .centerCrop()
                            .error(R.drawable.ic_no_image)
                    )
                    .into(imageView);
        }

    }

    public static void loadImageFromDrawableToView(Context context, int resourceId, ImageView imageView) {
        GlideApp.with(context)
                .load(resourceId)
                .apply(new RequestOptions()
                        .centerCrop()
                )
                .into(imageView);
    }


    public static void loadImageFromUrlToView(Context context, String url, ImageView imageView) {
        GlideApp.with(context)
                .load(url)
                .apply(new RequestOptions()
                        .centerCrop()
                        .placeholder(R.drawable.ic_background_splash)
                        .error(R.drawable.ic_background_splash)
                )
                .into(imageView);
    }

    public static int[] getScreenSize(Context context) {
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            height += resources.getDimensionPixelSize(resourceId);
        }
        resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            height += resources.getDimensionPixelSize(resourceId);
        }
        return new int[]{width, height};
    }

    public static int getNavigationBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        return resources.getDimensionPixelSize(resourceId);
    }

    private static boolean checkIfBitmapIsWhite(Bitmap bitmap) {
        if (bitmap == null)
            return true;
        Bitmap whiteBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
        Canvas canvas = new Canvas(whiteBitmap);
        canvas.drawColor(Color.WHITE);
        return bitmap.sameAs(whiteBitmap);
    }

    public static String saveImage(String folderPath, String filename, Bitmap finalBitmap) {
        if (finalBitmap == null || checkIfBitmapIsWhite(finalBitmap))
            return null;

        File myDir = new File(folderPath);
        String fileName = filename + ".png";

        File file = new File(myDir, fileName);
        if (file.exists())
            file.delete();

        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return file.getAbsolutePath();
    }


    public static ContentValues getContentValueForImage(File image) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, image.getName());
        values.put(MediaStore.Images.Media.DISPLAY_NAME, image.getName());
        values.put(MediaStore.Images.Media.DESCRIPTION, DataConstants.IMAGE_DESCRIPTION);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATA, image.getAbsolutePath());

        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
        return values;
    }

    public static void saveImageToGallery(Context context, File image) {
        if (image != null && image.exists()) {
            ContentValues values = ImageUtils.getContentValueForImage(image);
            context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        }
    }

    public static Bitmap transformBitmap(@NonNull Context context, int height, @NonNull Bitmap toTransform) {
        int width = toTransform.getWidth();
        height = toTransform.getHeight() - height;

        BitmapPool bitmapPool = Glide.get(context).getBitmapPool();

        Bitmap.Config config =
                toTransform.getConfig() != null ? toTransform.getConfig() : Bitmap.Config.ARGB_8888;
        Bitmap bitmap = bitmapPool.get(width, height, config);

        bitmap.setHasAlpha(true);

        float scaleX = (float) width / toTransform.getWidth();
        float scaleY = (float) height / toTransform.getHeight();
        float scale = Math.max(scaleX, scaleY);

        float scaledWidth = scale * toTransform.getWidth();
        float scaledHeight = scale * toTransform.getHeight();
        float left = (width - scaledWidth) / 2;
        float top = (height - scaledHeight) / 2;

        RectF targetRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);

        setCanvasBitmapDensity(toTransform, bitmap);

        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(toTransform, null, targetRect, null);

        return bitmap;
    }

    static void setCanvasBitmapDensity(@NonNull Bitmap toTransform, @NonNull Bitmap canvasBitmap) {
        canvasBitmap.setDensity(toTransform.getDensity());
    }


}
