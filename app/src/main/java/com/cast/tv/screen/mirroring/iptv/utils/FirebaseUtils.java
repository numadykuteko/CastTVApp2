package com.cast.tv.screen.mirroring.iptv.utils;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

public class FirebaseUtils {

    public static final String EVENT_TYPE = "event_type";
    public static final String ITEM_NAME = "item_name";

    public static final String CAST_BUTTON_EVENT = "prox_cast_button_click";
    public static final String HOME_EVENT = "prox_home_layout";
    public static final String SELECT_SCREEN_EVENT = "prox_select_screen_layout";
    public static final String PREPARING_EVENT = "prox_preparing_layout";
    public static final String GOOGLE_PHOTO_EVENT = "prox_google_photo_layout";
    public static final String GOOGLE_DRIVE_EVENT = "prox_google_drive_layout";
    public static final String WEB_LINK_EVENT = "prox_web_link_layout";
    public static final String IPTV_EVENT = "prox_iptv_layout";
    public static final String LEFT_MENU_EVENT = "prox_left_menu_layout";

    public static void sendEventFunctionUsed(Context context, String eventName, String eventType) {
        sendEventFunctionUsed(context, eventName, eventType, null);
    }

    public static void sendEventFunctionUsed(Context context, String eventName, String eventType, String itemName) {
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(context);

        Bundle bundle = new Bundle();
        bundle.putString(EVENT_TYPE, eventType);

        if (itemName != null) {
            bundle.putString(ITEM_NAME, itemName);
        }
        firebaseAnalytics.logEvent(eventName, bundle);
    }
}
