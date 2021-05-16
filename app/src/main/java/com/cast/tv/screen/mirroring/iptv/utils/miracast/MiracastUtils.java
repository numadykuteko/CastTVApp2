package com.cast.tv.screen.mirroring.iptv.utils.miracast;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.hardware.display.DisplayManager;
import android.view.Display;

import com.cast.tv.screen.mirroring.iptv.constants.AppConstants;
import com.cast.tv.screen.mirroring.iptv.ui.base.BaseBindingActivity;

import java.util.List;

public class MiracastUtils {
    public static final String ACTION_WIFI_DISPLAY_SETTINGS = "android.settings.WIFI_DISPLAY_SETTINGS";
    public static final String ACTION_CAST_SETTINGS = "android.settings.CAST_SETTINGS";

    public static Display checkMiracastConnected(Context context) {
        final DisplayManager displayManager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);

        Display[] displays = displayManager.getDisplays();

        boolean displayDidSet = false;
        Display display = null;
        for (int j = 0; j < displays.length; j++) {
            display = displays[j];

            if (display.getDisplayId() != Display.DEFAULT_DISPLAY) {
                displayDidSet = true;
            }
        }

        if (displayDidSet) {
            return display;
        }

        return null;
    }

    public static boolean isMiraCastConnect(Context context) {
        Display display = checkMiracastConnected(context);
        return display != null;
    }

    public static boolean gotoMiracastConnect(Context context, BaseBindingActivity baseBindingActivity, boolean isNeedFinish, boolean isNeedShowAds) {
        Intent wifiActionIntent = new Intent(ACTION_WIFI_DISPLAY_SETTINGS);
        Intent castActionIntent = new Intent(ACTION_CAST_SETTINGS);

        ResolveInfo systemResolveInfo = getSystemResolveInfo(context, wifiActionIntent);
        if (systemResolveInfo != null) {
            try {
                Intent systemWifiIntent = new Intent();
                systemWifiIntent.setClassName(systemResolveInfo.activityInfo.applicationInfo.packageName,
                        systemResolveInfo.activityInfo.name);
                startSettingsActivity(baseBindingActivity, systemWifiIntent, isNeedFinish, isNeedShowAds);
                return true;
            } catch (Exception ignored) {
            }
        }

        systemResolveInfo = getSystemResolveInfo(context, castActionIntent);
        if (systemResolveInfo != null) {
            try {
                Intent systemCastIntent = new Intent();
                systemCastIntent.setClassName(systemResolveInfo.activityInfo.applicationInfo.packageName,
                        systemResolveInfo.activityInfo.name);
                startSettingsActivity(baseBindingActivity, systemCastIntent, isNeedFinish, isNeedShowAds);
                return true;
            } catch (Exception ignored) {
            }
        }

        return false;
    }

    public static void registerListenerForCast(Context context, DisplayManager.DisplayListener displayListener) {
        final DisplayManager displayManager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        displayManager.registerDisplayListener(displayListener, null);
    }

    private static ResolveInfo getSystemResolveInfo(Context context, Intent intent) {
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> list = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo info : list) {
            try {
                ApplicationInfo activityInfo = pm.getApplicationInfo(info.activityInfo.packageName, 0);
                if ((activityInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                    return info;
                }
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }
        return null;
    }

    private static void startSettingsActivity(BaseBindingActivity baseBindingActivity, Intent intent, boolean isNeedFinish, boolean isNeedShowAds) {
        try {
            if (isNeedShowAds) {
                baseBindingActivity.showAdsBeforeAction(baseBindingActivity.mPreparingInterstitialAd, () -> {
                    baseBindingActivity.startActivity(intent);
                    if (isNeedFinish) {
                        baseBindingActivity.finish();
                    }
                });
            } else {
                baseBindingActivity.startActivity(intent);
                if (isNeedFinish) {
                    baseBindingActivity.finish();
                }
            }

        } catch (Exception e) {
            baseBindingActivity.gotoActivityWithFlag(AppConstants.FLAG_START_ERROR_SCREEN);
            baseBindingActivity.finish();
        }
    }
}
