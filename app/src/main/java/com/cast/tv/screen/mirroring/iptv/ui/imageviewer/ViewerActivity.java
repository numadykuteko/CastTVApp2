package com.cast.tv.screen.mirroring.iptv.ui.imageviewer;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.cast.tv.screen.mirroring.iptv.R;
import com.cast.tv.screen.mirroring.iptv.constants.AppConstants;
import com.cast.tv.screen.mirroring.iptv.data.model.ImageData;
import com.cast.tv.screen.mirroring.iptv.data.model.ImageDataList;
import com.cast.tv.screen.mirroring.iptv.databinding.ActivityViewerBinding;
import com.cast.tv.screen.mirroring.iptv.ui.base.BaseBindingActivity;
import com.cast.tv.screen.mirroring.iptv.utils.DataKeeper;
import com.cast.tv.screen.mirroring.iptv.utils.DialogFactory;
import com.cast.tv.screen.mirroring.iptv.utils.OnSwipeTouchListener;
import com.cast.tv.screen.mirroring.iptv.utils.ToastUtils;
import com.cast.tv.screen.mirroring.iptv.utils.file.FileUtils;
import com.cast.tv.screen.mirroring.iptv.utils.glide.GlideApp;
import com.cast.tv.screen.mirroring.iptv.utils.miracast.MiracastUtils;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ViewerActivity extends BaseBindingActivity<ActivityViewerBinding, ViewerViewModel> implements ViewerNavigator {
    private ViewerViewModel mViewerViewModel;
    private ActivityViewerBinding mActivityViewerBinding;

    private boolean mIsShowOption;
    private boolean mIsCastingScreen = false;
    private boolean mIsJumpingToMiraCast = false;

    private int mCurrentOrientation;

    private int mCurrentDisplayId = 0;

    private String mImageName;
    private String mImageUrl;

    private ImageDataList mSendWithList = null;
    private int mCurrentIndex;
    private boolean mIsNoticeSwipe = false;

    @Override
    public int getBindingVariable() {
        return 0;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_viewer;
    }

    @Override
    public ViewerViewModel getViewModel() {
        mViewerViewModel = ViewModelProviders.of(this).get(ViewerViewModel.class);
        return mViewerViewModel;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityViewerBinding = getViewDataBinding();
        mViewerViewModel.setNavigator(this);

        mIsShowOption = true;
        initView();

        boolean checkExtra = checkExtraFromApp(getIntent());

        if (!checkExtra) {
            ToastUtils.showMessageLong(this, getString(R.string.activity_player_not_valid_file));
            finishTotally();
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mCurrentOrientation = newConfig.orientation;
        setForOrientation(true);
    }

    @Override
    protected void initView() {
        setForBannerAds();
        setForClick();
        setForRotateOption();
        initForCastScreenState();
        setForCastScreenState();
        setForShowOption();

        preloadPreparingAdsIfInit();

        mCurrentOrientation = getResources().getConfiguration().orientation;
        setForOrientation(false);
        mActivityViewerBinding.fabOptionLock.setAlpha(.8f);
    }

    private void setForBannerAds() {
    }

    private void setForOrientation(boolean isRenew) {
        setForShowNavigationBar(mIsShowOption);

        if (isRenew) {
            preparePlayer(false);
        }
    }

    @Override
    protected void setClick() {
    }

    @Override
    public void onFragmentDetached(String tag) {
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void finishTotally() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAndRemoveTask();
        } else {
            finish();
        }
    }

    private boolean checkExtraFromApp(Intent intent) {
        String receivedText = intent.getStringExtra(EXTRA_FILE_PATH);

        if (receivedText == null || receivedText.length() == 0) {
            return false;
        } else {
            mImageUrl = receivedText;
        }

        String videoName = intent.getStringExtra(EXTRA_FILE_NAME);

        if (videoName == null || videoName.length() == 0) {
            mImageName = "Local image";
        } else {
            mImageName = videoName;
        }

        startAnalysisVideoLink(intent);
        return true;
    }

    private void startAnalysisVideoLink(Intent intent) {
        if (mImageUrl.toLowerCase().startsWith("https") || mImageUrl.toLowerCase().startsWith("http")) {
            preparePlayer(true);
        } else {
            // check file exist.

            if (FileUtils.checkFileExist(mImageUrl)) {
                mIsShowOption = true;

                mSendWithList = DataKeeper.getImageDataList();

                if (mSendWithList == null || mSendWithList.getImageDataList() == null || mSendWithList.getImageDataList().size() == 0) {
                    mSendWithList = new ImageDataList();
                    List<ImageData> temp = new ArrayList<>();
                    temp.add(new ImageData(mImageName, mImageUrl));
                    mSendWithList.setImageDataList(temp);
                }

                mCurrentIndex = findCurrentIndex();

                preparePlayer(true);
            } else {
                ToastUtils.showMessageLong(this, getString(R.string.activity_player_not_valid_file));
                finishTotally();
            }
        }
    }

    private int findCurrentIndex() {
        if (mSendWithList == null || mSendWithList.getImageDataList() == null || mSendWithList.getImageDataList().size() == 0) {
            return -1;
        }

        for (ImageData imageData : mSendWithList.getImageDataList()) {
            if (imageData.getImageName().equals(mImageName) && imageData.getImageUrl().equals(mImageUrl)) {
                return mSendWithList.getImageDataList().indexOf(imageData);
            }
        }

        return -1;
    }

    private void setForClick() {
        mActivityViewerBinding.playerOptionTopBackBtn.setOnClickListener(v -> {
            onBackPressed();
        });

        mActivityViewerBinding.fabOptionCastToScreen.setOnClickListener(v -> {
            startCastScreen();
        });
    }

    private void startCastScreen() {
        if (mCurrentDisplayId == Display.DEFAULT_DISPLAY) {
            mIsJumpingToMiraCast = true;
            gotoActivityWithFlag(AppConstants.FLAG_START_SCREEN_CAST);
        } else {
            Display currentDisplay = MiracastUtils.checkMiracastConnected(this);
            if (currentDisplay != null) {
                String tiviName = currentDisplay.getName() == null || currentDisplay.getName().length() == 0 ? "TV" : currentDisplay.getName();

                SweetAlertDialog dialogDisconnect = DialogFactory.getDialogDoSomething(this, "Miracast", "Your device is already connected to " + tiviName);
                dialogDisconnect.setConfirmButton("Disconnect", sweetAlertDialog -> MiracastUtils.gotoMiracastConnect(getApplicationContext(), ViewerActivity.this, false, true));
                dialogDisconnect.setCancelButton("Cancel", Dialog::dismiss);
                dialogDisconnect.show();
            } else {
                mCurrentDisplayId = Display.DEFAULT_DISPLAY;
                setForCastScreenState();
            }
        }
    }

    @Override
    protected void onResume() {
        if (mIsJumpingToMiraCast) {
            mIsJumpingToMiraCast = false;

            Display currentDisplay = MiracastUtils.checkMiracastConnected(this);
            if (currentDisplay != null) {
                mCurrentDisplayId = currentDisplay.getDisplayId();
            } else {
                mCurrentDisplayId = Display.DEFAULT_DISPLAY;
            }

            setForCastScreenState();

            if (mCurrentDisplayId != Display.DEFAULT_DISPLAY) {
                ToastUtils.showMessageShort(this, getString(R.string.activity_player_connect_to_miracast_success));
            } else {
                ToastUtils.showMessageLong(this, getString(R.string.activity_player_can_not_connect_to_miracast));
            }
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        DataKeeper.releaseList();
        super.onDestroy();
    }

    private void initForCastScreenState() {
        Display currentDisplay = MiracastUtils.checkMiracastConnected(this);
        if (currentDisplay != null) {
            mCurrentDisplayId = currentDisplay.getDisplayId();
        } else {
            mCurrentDisplayId = Display.DEFAULT_DISPLAY;
        }

        MiracastUtils.registerListenerForCast(this, new DisplayManager.DisplayListener() {
            @Override
            public void onDisplayAdded(int i) {
                mCurrentDisplayId = i;
                setForCastScreenState();
            }

            @Override
            public void onDisplayRemoved(int i) {
                if (i == mCurrentDisplayId) {
                    mCurrentDisplayId = Display.DEFAULT_DISPLAY;
                }
                setForCastScreenState();
            }

            @Override
            public void onDisplayChanged(int i) {
                mCurrentDisplayId = i;
                setForCastScreenState();
            }
        });
    }

    @SuppressLint({"UseCompatLoadingForColorStateLists", "UseCompatLoadingForDrawables"})
    private void setForCastScreenState() {
        mIsCastingScreen = mCurrentDisplayId != Display.DEFAULT_DISPLAY;

        if (mIsCastingScreen) {
            mActivityViewerBinding.fabOptionCastToScreen.setAlpha(1.0f);
            mActivityViewerBinding.fabOptionCastToScreen.setBackgroundTintList(
                    getResources().getColorStateList(R.color.color_selected));
            mActivityViewerBinding.fabOptionCastToScreen.setImageDrawable(getDrawable(R.drawable.ic_screen_mirroring_black));
        } else {
            mActivityViewerBinding.fabOptionCastToScreen.setAlpha(.8f);
            mActivityViewerBinding.fabOptionCastToScreen.setBackgroundTintList(
                    getResources().getColorStateList(R.color.color_unselected));
            mActivityViewerBinding.fabOptionCastToScreen.setImageDrawable(getDrawable(R.drawable.ic_screen_mirroring_gray));
        }
    }

    private void setForRotateOption() {
        mActivityViewerBinding.playerOptionRotate.setOnClickListener(v -> {
            if (mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
                setRequestedOrientation(ActivityInfo. SCREEN_ORIENTATION_LANDSCAPE);
            }
            setForOrientation(true);
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        setForShowNavigationBar(mIsShowOption);
        super.onWindowFocusChanged(hasFocus);
    }

    private void setForShowNavigationBar(boolean isShow) {
        View decorView = getWindow().getDecorView();
        if (isShow) {

            if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                // portrait show || mode web

                decorView.setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            } else {
                // Landscape show

                decorView.setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }
        } else {
            if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                // portrait hide

                decorView.setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            } else {
                // Landscape hide

                decorView.setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }
        }
    }

    private void setForShowOption() {
        setForShowNavigationBar(mIsShowOption);

        if (mIsShowOption) {
            mActivityViewerBinding.playerViewOption.setVisibility(View.VISIBLE);
        } else {
            mActivityViewerBinding.playerViewOption.setVisibility(View.GONE);
        }

        mActivityViewerBinding.playerViewTypeImage.setEnabled(true);

        mActivityViewerBinding.playerViewTypeImage.setLongClickable(true);
        mActivityViewerBinding.playerViewTypeImage.setOnLongClickListener(v -> {
            ToastUtils.showMessageShort(this, getString(R.string.activity_player_notice_about_lock_control));
            mIsShowOption = !mIsShowOption;
            setForShowOption();
            return false;
        });
        mActivityViewerBinding.fabOptionLock.setOnClickListener(v -> {
            ToastUtils.showMessageShort(this, getString(R.string.activity_player_notice_about_lock_control));
            mIsShowOption = !mIsShowOption;
            setForShowOption();
        });
    }

    @Override
    public void onBackPressed() {
        if (!mIsShowOption) {
            mIsShowOption = !mIsShowOption;
            setForShowOption();
            return;
        }

        super.onBackPressed();
    }

    private void preparePlayer(boolean isFirstTime) {
        mActivityViewerBinding.playerOptionTopInformationTv.setText(mImageName);

        GlideApp.with(this)
                .load(mImageUrl)
                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        if (isFirstTime) {
                            Handler handler = new Handler();
                            handler.post(() -> {
                                ToastUtils.showMessageLong(getApplicationContext(), getString(R.string.activity_player_not_valid_file));
                                finishTotally();
                            });
                        }

                        return false;
                    }

                    @SuppressLint("ClickableViewAccessibility")
                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        if (isFirstTime && mSendWithList != null && mSendWithList.getImageDataList() != null && mSendWithList.getImageDataList().size() > 0) {
                            if (!mIsNoticeSwipe) {
                                runOnUiThread(() -> ToastUtils.showMessageLong(getApplicationContext(), getString(R.string.activity_player_swipe_to_next)));
                                mIsNoticeSwipe = true;
                            }

                            mActivityViewerBinding.playerViewTypeImage.setOnTouchListener(new OnSwipeTouchListener(ViewerActivity.this) {
                                public void onSwipeTop() {
                                    nextImage();
                                }
                                public void onSwipeRight() {
                                    previousImage();
                                }
                                public void onSwipeLeft() {
                                    nextImage();
                                }
                                public void onSwipeBottom() {
                                    previousImage();
                                }

                            });
                        }
                        return false;
                    }
                })
                .into(mActivityViewerBinding.playerViewTypeImage);
    }

    private void previousImage() {
        if (mSendWithList == null || mSendWithList.getImageDataList() == null || mSendWithList.getImageDataList().size() == 0) {
            return;
        }
        if (mCurrentIndex == 0) {
            return;
        }
        mCurrentIndex--;

        ImageData imageData = mSendWithList.getImageDataList().get(mCurrentIndex);
        mImageName = imageData.getImageName();
        mImageUrl = imageData.getImageUrl();
        preparePlayer(false);
    }

    private void nextImage() {
        if (mSendWithList == null || mSendWithList.getImageDataList() == null || mSendWithList.getImageDataList().size() == 0) {
            return;
        }
        if (mCurrentIndex == mSendWithList.getImageDataList().size() - 1) {
            return;
        }
        mCurrentIndex++;

        ImageData imageData = mSendWithList.getImageDataList().get(mCurrentIndex);
        mImageName = imageData.getImageName();
        mImageUrl = imageData.getImageUrl();
        preparePlayer(false);
    }
}
