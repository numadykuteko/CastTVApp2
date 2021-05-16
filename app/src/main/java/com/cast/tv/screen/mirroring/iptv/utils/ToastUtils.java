package com.cast.tv.screen.mirroring.iptv.utils;

import android.content.Context;
import android.widget.Toast;

import com.cast.tv.screen.mirroring.iptv.R;

public class ToastUtils {

    public static void showSystemIssueToast(Context context) {
        if (context == null)    return;
        Toast.makeText(context, R.string.system_data_issue_text, Toast.LENGTH_SHORT).show();
    }

    public static void showNoNetworkToast(Context context) {
        if (context == null)    return;
        Toast.makeText(context, R.string.can_not_connect_to_network_text, Toast.LENGTH_SHORT).show();
    }

    public static void showToastAboutOnlineMode(Context context) {
        if (context == null)    return;
        Toast.makeText(context, R.string.toast_about_online_mode, Toast.LENGTH_SHORT).show();
    }

    public static void showConnectSuccessToast(Context context) {
        if (context == null)    return;
        Toast.makeText(context, R.string.connect_to_network_success_text, Toast.LENGTH_LONG).show();
    }

    public static void showFunctionNotSupportToast(Context context) {
        if (context == null)    return;
        Toast.makeText(context, R.string.function_not_support_text, Toast.LENGTH_LONG).show();
    }

    public static void showMessageShort(Context context, String message) {
        if (context == null)    return;
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
    public static void showMessageLong(Context context, String message) {
        if (context == null)    return;
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
    public static void showMessageDeleteSuccess(Context context) {
        Toast.makeText(context, context.getString(R.string.delete_success_text), Toast.LENGTH_SHORT).show();
    }
    public static void showMessageDeleteFail(Context context) {
        Toast.makeText(context, context.getString(R.string.delete_fail_text), Toast.LENGTH_SHORT).show();
    }
}
