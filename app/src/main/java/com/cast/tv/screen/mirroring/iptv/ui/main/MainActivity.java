package com.cast.tv.screen.mirroring.iptv.ui.main;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.lifecycle.ViewModelProviders;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;

import com.ads.control.Admod;
import com.cast.tv.screen.mirroring.iptv.BuildConfig;
import com.cast.tv.screen.mirroring.iptv.R;
import com.cast.tv.screen.mirroring.iptv.constants.AppConstants;
import com.cast.tv.screen.mirroring.iptv.constants.DataConstants;
import com.cast.tv.screen.mirroring.iptv.data.DataManager;
import com.cast.tv.screen.mirroring.iptv.databinding.ActivityMainBinding;
import com.cast.tv.screen.mirroring.iptv.databinding.ItemHomeFunctionBinding;
import com.cast.tv.screen.mirroring.iptv.databinding.MenuViewNavigationItemBinding;
import com.cast.tv.screen.mirroring.iptv.ui.base.BaseBindingActivity;
import com.cast.tv.screen.mirroring.iptv.utils.FirebaseUtils;
import com.cast.tv.screen.mirroring.iptv.utils.ToastUtils;
import com.cast.tv.screen.mirroring.iptv.utils.ads.RateShowCountManager;
import com.cast.tv.screen.mirroring.iptv.utils.chromecast.ChromecastConnection;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.cast.tv.screen.mirroring.iptv.constants.AppConstants.FLAG_START_SCREEN_CAST;

public class MainActivity extends BaseBindingActivity<ActivityMainBinding, MainViewModel> implements MainNavigator {

    private MainViewModel mMainViewModel;
    private ActivityMainBinding mActivityMainBinding;
    private List<MenuViewNavigationItemBinding> mListMenuView = new ArrayList<>();
    private final List<Integer> mListMenuTitle = Arrays.asList(
            R.string.title_navigation_home,
            R.string.title_navigation_share,
            R.string.title_navigation_contact,
            R.string.title_navigation_rate);
    private final List<Integer> mListMenuIcon = Arrays.asList(
            R.drawable.ic_home,
            R.drawable.ic_share,
            R.drawable.ic_contact,
            R.drawable.ic_rate);
    private final ArrayList<ItemHomeFunctionBinding> mFunctionLayoutList = new ArrayList<>();
    private final ArrayList<Integer> mFunctionNameList = new ArrayList<>();
    private final ArrayList<Integer> mFunctionIconList = new ArrayList<>();

    private ActionBarDrawerToggle drawerToggle;
    private ChromecastConnection mChromecastConnection;

    @Override
    public int getBindingVariable() {
        return 0;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public MainViewModel getViewModel() {
        mMainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        return mMainViewModel;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMainViewModel.setNavigator(this);
        mActivityMainBinding = getViewDataBinding();

        setupCastButton();

        initView();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void initView() {
        preloadScreenMirroringAdsIfInit();
        preloadClickItemLocalAdsIfInit();

        Admod.getInstance().loadNativeFragment(this, BuildConfig.native_home_id, mActivityMainBinding.nativeAds);

        setNoActionBar();
        setForNavMenu();
        initCommonView();

        for (ItemHomeFunctionBinding itemFunction: mFunctionLayoutList) {
            int index = mFunctionLayoutList.indexOf(itemFunction);

            itemFunction.titleItem.setText(getString(mFunctionNameList.get(index)));
            itemFunction.iconItem.setImageDrawable(getDrawable(mFunctionIconList.get(index)));
            itemFunction.contentItem.setOnClickListener(view -> {
                showAdsBeforeAction(mClickItemLocalInterstitialAd, () -> gotoActivityWithFlag(index));
            });
        }

        mActivityMainBinding.mainFunction.setOnClickListener(view -> {
            FirebaseUtils.sendEventFunctionUsed(this, FirebaseUtils.HOME_EVENT, "Click Item", "Screen mirror");
            showAdsBeforeAction(mScreenMirroringInterstitialAd, () -> gotoActivityWithFlag(FLAG_START_SCREEN_CAST));
        });
    }

    @Override
    protected void setClick() {

    }

    @Override
    public void onFragmentDetached(String tag) {

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NotNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == OPEN_ACTIVITY_RATE_REQUEST) {
            DataManager dataManager = DataManager.getInstance(getApplicationContext());

            if (dataManager.checkRatingUsDone()) {
                return;
            }

            double timeUsing = System.currentTimeMillis() - mStartActivityTime;
            if (timeUsing >= DataConstants.MIN_TIME_USING_TO_SHOW_RATE) {
                if (RateShowCountManager.getInstance(this).checkShowRateForBackHome()) {
                    requestForRating(() -> {});
                }
                RateShowCountManager.getInstance(this).increaseCountForBackHome();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void setForNavMenu() {
        drawerToggle = new ActionBarDrawerToggle(this, mActivityMainBinding.drawerLayout, R.string.app_name, R.string.app_name);
        mActivityMainBinding.drawerLayout.addDrawerListener(drawerToggle);

        mActivityMainBinding.navigationViewHome.getBackground().setColorFilter(0xCC000000, PorterDuff.Mode.MULTIPLY);
        mActivityMainBinding.drawerLayout.setDrawerElevation(0);

        mListMenuView.add(AppConstants.FLAG_START_HOME_ACTIVITY, mActivityMainBinding.navMenuItemHome);
        mListMenuView.add(AppConstants.FLAG_START_SHARE_ACTIVITY, mActivityMainBinding.navMenuItemShare);
        mListMenuView.add(AppConstants.FLAG_START_CONTACT_ACTIVITY, mActivityMainBinding.navMenuItemContact);
        mListMenuView.add(AppConstants.FLAG_START_RATE_ACTIVITY, mActivityMainBinding.navMenuItemRating);

        int index = 0;
        for (MenuViewNavigationItemBinding item : mListMenuView) {
            item.title.setText(getString(mListMenuTitle.get(index)));
            item.icon.setBackground(getDrawable(mListMenuIcon.get(index)));

            int finalIndex = index;
            item.menuItem.setOnClickListener(v -> {
                if (finalIndex == AppConstants.FLAG_START_HOME_ACTIVITY) {
                    mActivityMainBinding.drawerLayout.closeDrawer(Gravity.LEFT);
                } else {
                    gotoActivityFromNavigationWithFlag(finalIndex);
                }
            });
            index++;
        }

        mActivityMainBinding.menuImg.setOnClickListener(view -> {
            try {
                mActivityMainBinding.drawerLayout.openDrawer(Gravity.LEFT);
            } catch (Exception e) {
                Log.d("MainActivity", "Can not open drawer");
            }
        });

        mActivityMainBinding.upgradeBtn.setOnClickListener(view -> {
            gotoActivityFromNavigationWithFlag(AppConstants.FLAG_START_UPGRADE_ACTIVITY);
        });
    }

    private void initCommonView() {
        mFunctionLayoutList.add(AppConstants.FLAG_START_PHOTO_CAST, mActivityMainBinding.photoCast);
        mFunctionLayoutList.add(AppConstants.FLAG_START_VIDEO_CAST, mActivityMainBinding.videoCast);
        mFunctionLayoutList.add(AppConstants.FLAG_START_AUDIO_CAST, mActivityMainBinding.audioCast);
        mFunctionLayoutList.add(AppConstants.FLAG_START_GOOGLE_PHOTO_CAST, mActivityMainBinding.googlePhotoCast);
        mFunctionLayoutList.add(AppConstants.FLAG_START_GOOGLE_DRIVE_CAST, mActivityMainBinding.googleDriveCast);
        mFunctionLayoutList.add(AppConstants.FLAG_START_WEB_LINK_CAST, mActivityMainBinding.webLinkCast);
        mFunctionLayoutList.add(AppConstants.FLAG_START_GALLERY_CAST, mActivityMainBinding.galleryCast);
        mFunctionLayoutList.add(AppConstants.FLAG_START_HISTORY, mActivityMainBinding.history);
        mFunctionLayoutList.add(AppConstants.FLAG_START_BOOKMARK, mActivityMainBinding.bookMarks);
        mFunctionLayoutList.add(AppConstants.FLAG_START_IPTV, mActivityMainBinding.iptvCast);
        mFunctionLayoutList.add(AppConstants.FLAG_START_SCREEN_CAST, mActivityMainBinding.screenCast);

        mFunctionIconList.add(AppConstants.FLAG_START_PHOTO_CAST, R.drawable.ic_image);
        mFunctionIconList.add(AppConstants.FLAG_START_VIDEO_CAST, R.drawable.ic_video);
        mFunctionIconList.add(AppConstants.FLAG_START_AUDIO_CAST, R.drawable.ic_audio);
        mFunctionIconList.add(AppConstants.FLAG_START_GOOGLE_PHOTO_CAST, R.drawable.ic_google_photo);
        mFunctionIconList.add(AppConstants.FLAG_START_GOOGLE_DRIVE_CAST, R.drawable.ic_google_drive);
        mFunctionIconList.add(AppConstants.FLAG_START_WEB_LINK_CAST, R.drawable.ic_web);
        mFunctionIconList.add(AppConstants.FLAG_START_GALLERY_CAST, R.drawable.ic_gallery);
        mFunctionIconList.add(AppConstants.FLAG_START_HISTORY, R.drawable.ic_history);
        mFunctionIconList.add(AppConstants.FLAG_START_BOOKMARK, R.drawable.ic_bookmark);
        mFunctionIconList.add(AppConstants.FLAG_START_IPTV, R.drawable.ic_tv);
        mFunctionIconList.add(AppConstants.FLAG_START_SCREEN_CAST, R.drawable.ic_screen_mirroring);

        mFunctionNameList.add(AppConstants.FLAG_START_PHOTO_CAST, R.string.activity_main_photo_cast_name);
        mFunctionNameList.add(AppConstants.FLAG_START_VIDEO_CAST, R.string.activity_main_video_cast_name);
        mFunctionNameList.add(AppConstants.FLAG_START_AUDIO_CAST, R.string.activity_main_audio_cast_name);
        mFunctionNameList.add(AppConstants.FLAG_START_GOOGLE_PHOTO_CAST, R.string.activity_main_google_photo_cast_name);
        mFunctionNameList.add(AppConstants.FLAG_START_GOOGLE_DRIVE_CAST, R.string.activity_main_google_drive_cast_name);
        mFunctionNameList.add(AppConstants.FLAG_START_WEB_LINK_CAST, R.string.activity_main_weblink_cast_name);
        mFunctionNameList.add(AppConstants.FLAG_START_GALLERY_CAST, R.string.activity_main_gallery_cast_name);
        mFunctionNameList.add(AppConstants.FLAG_START_HISTORY, R.string.activity_main_history_name);
        mFunctionNameList.add(AppConstants.FLAG_START_BOOKMARK, R.string.activity_main_bookmark_name);
        mFunctionNameList.add(AppConstants.FLAG_START_IPTV, R.string.activity_main_iptv_name);
        mFunctionNameList.add(AppConstants.FLAG_START_SCREEN_CAST, R.string.activity_main_screen_mirror);
    }

    private void setupCastButton() {
        mDefaultCastStateListener.setCastIcon(mActivityMainBinding.castImg);
        mChromecastConnection = new ChromecastConnection(this, mDefaultCastStateListener);
        mChromecastConnection.initialize(AppConstants.CAST_APPLICATION_ID);

        mActivityMainBinding.castImg.setOnClickListener(view -> {
            FirebaseUtils.sendEventFunctionUsed(this, FirebaseUtils.CAST_BUTTON_EVENT, "Click cast button", "Home layout");

            if (mChromecastConnection.isChromeCastConnect()) {
                mChromecastConnection.requestEndSession(new ChromecastConnection.RequestEndSessionCallback() {

                    @Override
                    public void onSuccess() {
                        ToastUtils.showMessageLong(getApplicationContext(), getString(R.string.cast_stop_casting_success));
                    }

                    @Override
                    public void onCancel() {
                        // do nothing
                    }

                });
            } else {
                showPrepareConnectionDialog();
                mChromecastConnection.requestStartSession(mDefaultRequestSessionCallback);
            }
        });
    }
}
