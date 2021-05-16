package com.cast.tv.screen.mirroring.iptv.ui.splash;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.lifecycle.ViewModelProviders;

import com.ads.control.Admod;
import com.ads.control.funtion.AdCallback;
import com.cast.tv.screen.mirroring.iptv.BuildConfig;
import com.cast.tv.screen.mirroring.iptv.R;
import com.cast.tv.screen.mirroring.iptv.databinding.ActivitySplashBinding;
import com.cast.tv.screen.mirroring.iptv.ui.base.BaseBindingActivity;
import com.cast.tv.screen.mirroring.iptv.ui.main.MainActivity;
import com.cast.tv.screen.mirroring.iptv.utils.NetworkUtils;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends BaseBindingActivity<ActivitySplashBinding, SplashViewModel> implements SplashNavigator {
    private SplashViewModel mSplashViewModel;
    private ActivitySplashBinding mActivitySplashBinding;

    @Override
    public int getBindingVariable() {
        return 0;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_splash;
    }

    @Override
    public SplashViewModel getViewModel() {
        mSplashViewModel = ViewModelProviders.of(this).get(SplashViewModel.class);
        return null;
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        isNeedToSetTheme = false;
        super.onCreate(savedInstanceState);

        mActivitySplashBinding = getViewDataBinding();
        mSplashViewModel.setNavigator(this);

        if (getSupportActionBar() != null) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setShowHideAnimationEnabled(false);

            actionBar.hide();
            actionBar.setDisplayShowTitleEnabled(false);

            Window window = getWindow();

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getColorByResource(R.color.activity_main_function_bg));
            window.setNavigationBarColor(getColorByResource(R.color.activity_main_function_bg));
        }

        if (!NetworkUtils.isNetworkConnected(this)) {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    gotoMainActivity();
                }
            }, 1000);
            return;
        }

        Admod.getInstance().loadSplashInterstitalAds(this,
                BuildConfig.interstitial_splash_id,
                20000,
                new AdCallback() {
                    @Override
                    public void onAdClosed() {
                        gotoMainActivity();
                    }

                    @Override
                    public void onAdFailedToLoad(int i) {
                        gotoMainActivity();
                    }
                });
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void setClick() {

    }

    @Override
    public void onFragmentDetached(String tag) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void gotoMainActivity() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
