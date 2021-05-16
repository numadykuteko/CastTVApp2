package com.cast.tv.screen.mirroring.iptv.utils;

import android.content.Context;

import com.cast.tv.screen.mirroring.iptv.R;
import com.cast.tv.screen.mirroring.iptv.data.DataManager;

public class ThemeUtils {
    public static int getBaseThemeColor(Context context) {
        DataManager dataManager = DataManager.getInstance(context);
        int themeIndex = dataManager.getTheme();

        switch (themeIndex) {
            case 1:
                return ColorUtils.getColorFromResource(context, R.color.jade_theme_color);
            case 2:
                return ColorUtils.getColorFromResource(context, R.color.blue_theme_color);
            case 3:
                return ColorUtils.getColorFromResource(context, R.color.orange_theme_color);
            case 4:
                return ColorUtils.getColorFromResource(context, R.color.violet_theme_color);
        }

        return ColorUtils.getColorFromResource(context, R.color.jade_theme_color);
    }
}
