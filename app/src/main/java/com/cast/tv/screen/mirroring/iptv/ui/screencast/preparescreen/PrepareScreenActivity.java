package com.cast.tv.screen.mirroring.iptv.ui.screencast.preparescreen;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import androidx.lifecycle.ViewModelProviders;

import com.ads.control.Admod;
import com.cast.tv.screen.mirroring.iptv.BuildConfig;
import com.cast.tv.screen.mirroring.iptv.R;
import com.cast.tv.screen.mirroring.iptv.constants.AppConstants;
import com.cast.tv.screen.mirroring.iptv.databinding.ActivityPrepareScreenBinding;
import com.cast.tv.screen.mirroring.iptv.ui.base.BaseBindingActivity;
import com.cast.tv.screen.mirroring.iptv.utils.DialogFactory;
import com.cast.tv.screen.mirroring.iptv.utils.FirebaseUtils;
import com.cast.tv.screen.mirroring.iptv.utils.miracast.MiracastUtils;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class PrepareScreenActivity extends BaseBindingActivity<ActivityPrepareScreenBinding, PrepareScreenViewModel> implements PrepareScreenNavigator {

    private PrepareScreenViewModel mPrepareScreenViewModel;
    private ActivityPrepareScreenBinding mActivityPrepareScreenBinding;
    private int mTimeLoading = 500;
    private boolean mIsDonePrepare = false;

    @Override
    public int getBindingVariable() {
        return 0;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_prepare_screen;
    }

    @Override
    public PrepareScreenViewModel getViewModel() {
        mPrepareScreenViewModel = ViewModelProviders.of(this).get(PrepareScreenViewModel.class);
        return mPrepareScreenViewModel;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrepareScreenViewModel.setNavigator(this);
        mActivityPrepareScreenBinding = getViewDataBinding();
        mIsDonePrepare = false;
        Random random = new Random();
        mTimeLoading = 500 + random.nextInt(500);
        initView();
    }

    @Override
    protected void initView() {
        setNoActionBar();
        setForLiveData();
        setForClick();

        preloadPreparingAdsIfInit();
        startPrepareConnect();

        Admod.getInstance().loadSmallNativeFragment(this, BuildConfig.native_preparing_id, mActivityPrepareScreenBinding.nativeAds);
    }

    @Override
    protected void setClick() {}

    @Override
    public void onFragmentDetached(String tag) {}

    @Override
    public void onBackPressed() {
        if (mActivityPrepareScreenBinding.btnStartConnect.getVisibility() == View.VISIBLE) {
            finish();
        } else {
            SweetAlertDialog confirmExit = DialogFactory.getDialogConfirm(this, getString(R.string.activity_prepare_screen_confirm_exit));
            confirmExit.setConfirmClickListener(sweetAlertDialog -> {
                sweetAlertDialog.dismiss();
                finish();
            });
            confirmExit.setCancelClickListener(Dialog::dismiss);
            confirmExit.show();
        }
    }

    private void setForLiveData() {

    }

    private void setForClick() {
        mActivityPrepareScreenBinding.backImg.setOnClickListener(view -> onBackPressed());
    }

    private void startPrepareConnect() {
        mActivityPrepareScreenBinding.btnStartConnect.setVisibility(View.INVISIBLE);
        mActivityPrepareScreenBinding.step1.setVisibility(View.GONE);
        mActivityPrepareScreenBinding.step2.setVisibility(View.GONE);
        mActivityPrepareScreenBinding.step3.setVisibility(View.GONE);
        mActivityPrepareScreenBinding.loadingArea.setVisibility(View.VISIBLE);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> finishStep1());
            }
        }, mTimeLoading);
    }

    private void finishStep1() {
        mActivityPrepareScreenBinding.step1.setVisibility(View.VISIBLE);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> finishStep2());
            }
        }, mTimeLoading);
    }

    private void finishStep2() {
        mActivityPrepareScreenBinding.step2.setVisibility(View.VISIBLE);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> finishStep3());
            }
        }, mTimeLoading);
    }

    private void finishStep3() {
        mActivityPrepareScreenBinding.step3.setVisibility(View.VISIBLE);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> finishPrepare());
            }
        }, mTimeLoading);
    }

    private void finishPrepare() {
        mIsDonePrepare = true;
        showMirrorButton();
    }

    private void showMirrorButton() {
        mActivityPrepareScreenBinding.loadingArea.setVisibility(View.GONE);
        mActivityPrepareScreenBinding.btnStartConnect.setVisibility(View.VISIBLE);

        mActivityPrepareScreenBinding.btnStartConnect.setOnClickListener(view -> {
            FirebaseUtils.sendEventFunctionUsed(this, FirebaseUtils.PREPARING_EVENT, "Click start connect");
            showAdsBeforeAction(mPreparingInterstitialAd, this::gotoMiracastScreen);
        });
    }

    private void gotoMiracastScreen() {
        boolean canRunMiracast = MiracastUtils.gotoMiracastConnect(this, this, true, false);

        if (!canRunMiracast) {
            gotoActivityWithFlag(AppConstants.FLAG_START_ERROR_SCREEN);
            finish();
        }
    }
}

