package com.cast.tv.screen.mirroring.iptv.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;

import androidx.core.content.ContextCompat;

public class ColorUtils {
    public static GradientDrawable getShapeFromColor(String color) {
        GradientDrawable shape =  new GradientDrawable();
        shape.setCornerRadius( 50 );
        shape.setColor(Color.parseColor(color));
        return shape;
    }

    public static int getColorFromCode(String color) {
        return Color.parseColor(color);
    }

    public static int getColorFromResource(Context context, int colorId) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return context.getResources().getColor(colorId, context.getTheme());
            } else {
                return ContextCompat.getColor(context, colorId);
            }
        } catch (Exception e) {
            return Color.BLACK;
        }

    }
}
