package com.cast.tv.screen.mirroring.iptv.ui.splash;

import android.app.Application;

import androidx.annotation.NonNull;

import com.cast.tv.screen.mirroring.iptv.ui.base.BaseViewModel;

public class SplashViewModel extends BaseViewModel<SplashNavigator> {
    private static final String TAG = "SplashViewModel";

    public SplashViewModel(@NonNull Application application) {
        super(application);
    }
}
