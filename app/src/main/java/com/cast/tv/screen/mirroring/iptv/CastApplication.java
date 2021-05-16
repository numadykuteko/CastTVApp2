package com.cast.tv.screen.mirroring.iptv;

import android.app.Application;

import com.cast.tv.screen.mirroring.iptv.utils.ads.AppOpenManager;
import com.google.android.gms.ads.MobileAds;

public class CastApplication extends Application {

    AppOpenManager appOpenManager;

    @Override
    public void onCreate() {
        super.onCreate();

        MobileAds.initialize(
                this,
                initializationStatus -> {});

        appOpenManager = new AppOpenManager(this);
    }
}
