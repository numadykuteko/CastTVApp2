package com.cast.tv.screen.mirroring.iptv.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;

import com.cast.tv.screen.mirroring.iptv.R;

public class PackageUtils {
    public static boolean isAppAvailable(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    public static void gotoDownloadApp(Context context, String packageName) {
        Uri uri = Uri.parse("market://details?id=" + packageName);
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            context.startActivity(goToMarket);
        } catch (Exception e) {
            Toast.makeText(context, context.getString(R.string.can_not_open_google_play), Toast.LENGTH_SHORT).show();
        }
    }
}
