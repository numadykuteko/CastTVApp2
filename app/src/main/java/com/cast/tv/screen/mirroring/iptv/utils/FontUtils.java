package com.cast.tv.screen.mirroring.iptv.utils;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.widget.TextView;

public class FontUtils {

    public static Typeface getFontBold(AssetManager assetManager) {
        return Typeface.createFromAsset(assetManager, "fonts/Roboto-Bold.ttf");
    }

    private static Typeface getFont(AssetManager assetManager) {
        return Typeface.createFromAsset(assetManager, "fonts/Roboto-Medium.ttf");
    }

    private static Typeface getFontThin(AssetManager assetManager) {
        return Typeface.createFromAsset(assetManager, "fonts/Roboto-Light.ttf");
    }

    private static Typeface getFontThinItalic(AssetManager assetManager) {
        return Typeface.createFromAsset(assetManager, "fonts/Roboto-LightItalic.ttf");
    }

    private static Typeface getFontItalic(AssetManager assetManager) {
        return Typeface.createFromAsset(assetManager, "fonts/Roboto-MediumItalic.ttf");
    }

    private static Typeface getFontBoldItalic(AssetManager assetManager) {
        return Typeface.createFromAsset(assetManager, "fonts/Roboto-BoldItalic.ttf");
    }


    private static Typeface getFontGreatVibes(AssetManager assetManager) {
        return Typeface.createFromAsset(assetManager, "fonts/GreatVibes-Regular.ttf");
    }

    private static Typeface getFontCrimson(AssetManager assetManager) {
        return Typeface.createFromAsset(assetManager, "fonts/Crimson.ttf");
    }

    public static void setFontBold(TextView textView, AssetManager assetManager) {
        textView.setTypeface(getFontBold(assetManager));
    }

    public static void setFont(TextView textView, AssetManager assetManager) {
        textView.setTypeface(getFont(assetManager));
    }
    public static void setFontThin(TextView textView, AssetManager assetManager) {
        textView.setTypeface(getFontThin(assetManager));
    }
    public static void setFontThinItalic(TextView textView, AssetManager assetManager) {
        textView.setTypeface(getFontThinItalic(assetManager));
    }
    public static void setFontItalic(TextView textView, AssetManager assetManager) {
        textView.setTypeface(getFontItalic(assetManager));
    }
    public static void setFontBoldItalic(TextView textView, AssetManager assetManager) {
        textView.setTypeface(getFontBoldItalic(assetManager));
    }
    public static void setFontGreatVibes(TextView textView, AssetManager assetManager) {
        textView.setTypeface(getFontGreatVibes(assetManager));
    }
    public static void setFontCrimson(TextView textView, AssetManager assetManager) {
        textView.setTypeface(getFontCrimson(assetManager));
    }
}
