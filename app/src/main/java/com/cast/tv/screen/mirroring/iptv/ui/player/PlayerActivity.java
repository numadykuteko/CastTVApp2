package com.cast.tv.screen.mirroring.iptv.ui.player;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.cast.tv.screen.mirroring.iptv.R;
import com.cast.tv.screen.mirroring.iptv.constants.AppConstants;
import com.cast.tv.screen.mirroring.iptv.databinding.ActivityPlayerBinding;
import com.cast.tv.screen.mirroring.iptv.ui.base.BaseBindingActivity;
import com.cast.tv.screen.mirroring.iptv.utils.DialogFactory;
import com.cast.tv.screen.mirroring.iptv.utils.NetworkUtils;
import com.cast.tv.screen.mirroring.iptv.utils.ToastUtils;
import com.cast.tv.screen.mirroring.iptv.utils.file.FileUtils;
import com.cast.tv.screen.mirroring.iptv.utils.miracast.MiracastUtils;
import com.cast.tv.screen.mirroring.iptv.utils.webview.VideoEnabledWebChromeClient;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.Timer;
import java.util.TimerTask;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class PlayerActivity extends BaseBindingActivity<ActivityPlayerBinding, PlayerViewModel> implements
        PlayerNavigator, VideoRendererEventListener {
    private PlayerViewModel mPlayerViewModel;
    private ActivityPlayerBinding mActivityPlayerBinding;
    private SweetAlertDialog mErrorVideoLoading, mAlertAboutDataUsing;

    private boolean mIsOpenWebView = false;

    private boolean mIsShowOption;
    private boolean mIsLockingControl = false;
    private boolean mIsRepeat = true;
    private boolean mIsCastingScreen = false;
    private boolean mIsPause = false;
    private boolean isNoticeAfterRender = false;
    private boolean mIsRedirectOnceTime = false;
    private boolean mLoadVideoDone = false;
    private boolean mIsJumpingToMiraCast = false;

    private long mCurrentPosition = 0;
    private int mCurrentOrientation;

    private int mCurrentDisplayId = 0;

    private PlayerView mSimpleExoPlayerView;
    private SimpleExoPlayer mPlayer;

    private String mVideoName;
    private String mVideoUrl;

    @Override
    public int getBindingVariable() {
        return 0;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_player;
    }

    @Override
    public PlayerViewModel getViewModel() {
        mPlayerViewModel = ViewModelProviders.of(this).get(PlayerViewModel.class);
        return mPlayerViewModel;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityPlayerBinding = getViewDataBinding();
        mPlayerViewModel.setNavigator(this);

        mIsShowOption = false;
        mIsLockingControl = false;
        mIsRepeat = false;
        mCurrentPosition = 0;

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
        setForOrientation();
    }

    @Override
    protected void initView() {
        setForBannerAds();
        setForShowVideoView();
        setForClick();
        setForLockOption(true);
        setForRepeatOption();
        setForRotateOption();
        setForActionWhenPauseVideo();
        initForCastScreenState();
        setForCastScreenState();

        preloadPreparingAdsIfInit();

        mCurrentOrientation = getResources().getConfiguration().orientation;
        setForOrientation();
    }

    private void setForBannerAds() {
    }

    private void setForOrientation() {
        setForShowNavigationBar(mIsShowOption);
    }

    @Override
    protected void setClick() {
    }

    @Override
    public void onFragmentDetached(String tag) {
    }

    @Override
    public void onBackPressed() {
        if (mIsLockingControl) {
            mIsLockingControl = false;
            mIsShowOption = true;
            setForLockOption(false);
            setForShowOption();
        } else {
            finishTotally();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mPlayer != null) mPlayer.release();

        mActivityPlayerBinding.playerViewExoPlayer.setPlayer(null);
        mActivityPlayerBinding.playerViewWebview.clearCache(true);

        super.onDestroy();
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
            mVideoUrl = receivedText;
        }

        String videoName = intent.getStringExtra(EXTRA_FILE_NAME);

        if (videoName == null || videoName.length() == 0) {
            mVideoName = "Local video";
        } else {
            mVideoName = videoName;
        }

        startAnalysisVideoLink();
        return true;
    }

    private void startAnalysisVideoLink() {
        if (mVideoUrl.toLowerCase().startsWith("https") || mVideoUrl.toLowerCase().startsWith("http")) {
            mActivityPlayerBinding.playerViewTypeLoadingTv.setText("0 %");

            if (NetworkUtils.isNetworkConnected(this)) {
                checkUserUse3G();
            } else {
                ToastUtils.showMessageLong(this, getString(R.string.activity_player_not_valid_file));
                finishTotally();
            }
        } else {
            // check file exist.

            if (FileUtils.checkFileExist(mVideoUrl)) {
                mIsShowOption = true;
                preparePlayer();
            } else {
                ToastUtils.showMessageLong(this, getString(R.string.activity_player_not_valid_file));
                finishTotally();
            }
        }
    }

    private void checkUserUse3G() {
        if (NetworkUtils.getNetworkStatus(this) == NetworkUtils.TYPE_3G) {
            mAlertAboutDataUsing = DialogFactory.getDialogConfirm(this, getString(R.string.activity_player_watch_movie_need_use_3g));
            mAlertAboutDataUsing.setCancelText(getString(R.string.exit_text));
            mAlertAboutDataUsing.show();
            mAlertAboutDataUsing.setCancelClickListener(sweetAlertDialog -> {
                sweetAlertDialog.dismiss();
                finishTotally();
            });
            mAlertAboutDataUsing.setConfirmClickListener(sweetAlertDialog -> {
                sweetAlertDialog.dismiss();
                preparePlayer();
            });
        } else {
            preparePlayer();
        }
    }

    @SuppressLint("NonConstantResourceId")
    private void setForShowVideoView() {
        mActivityPlayerBinding.playerViewTypeWebview.setVisibility(View.GONE);

        if (mLoadVideoDone) {
            mActivityPlayerBinding.playerViewTypeLoading.setVisibility(View.GONE);
            mActivityPlayerBinding.fabOptionLock.setVisibility(View.VISIBLE);
            mActivityPlayerBinding.fabOptionRepeat.setVisibility(View.VISIBLE);
            mActivityPlayerBinding.fabOptionCastToScreen.setVisibility(View.VISIBLE);
            setForShowOption();

            mActivityPlayerBinding.playerViewTypeContent.setVisibility(View.VISIBLE);
            mActivityPlayerBinding.playerViewTypeContent.setVisibility(View.VISIBLE);
        } else {
            mActivityPlayerBinding.playerViewOption.setVisibility(View.GONE);
            mActivityPlayerBinding.playerViewTypeContent.setVisibility(View.GONE);
            mActivityPlayerBinding.playerViewTypeLoading.setVisibility(View.VISIBLE);

            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    private void setForShowWebView() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    setForShowNavigationBar(true);
                    mActivityPlayerBinding.playerViewTypeLoading.setVisibility(View.GONE);

                    mActivityPlayerBinding.playerViewOption.setVisibility(View.VISIBLE);
                    mActivityPlayerBinding.fabOptionLock.setVisibility(View.GONE);
                    mActivityPlayerBinding.fabOptionRepeat.setVisibility(View.GONE);
                    mActivityPlayerBinding.fabOptionCastToScreen.setVisibility(View.VISIBLE);

                    mActivityPlayerBinding.playerViewTypeContent.setVisibility(View.GONE);
                    mActivityPlayerBinding.playerViewTypeWebview.setVisibility(View.VISIBLE);
                });
            }
        }, 2000);

    }

    private void setForClick() {
        mActivityPlayerBinding.playerOptionTopBackBtn.setOnClickListener(v -> {
            onBackPressed();
        });

        mActivityPlayerBinding.fabOptionCastToScreen.setOnClickListener(v -> {
            startCastScreen();
        });

        mActivityPlayerBinding.fabOptionRepeat.setOnClickListener(v -> {
            mIsRepeat = !mIsRepeat;
            setForRepeatOption();

            if (mIsRepeat && mPlayer != null && mPlayer.getPlaybackState() == Player.STATE_ENDED) {
                mPlayer.seekTo(0);
                mPlayer.setPlayWhenReady(true);
            }
        });

        mActivityPlayerBinding.fabOptionLock.setOnClickListener(v -> {
            mIsLockingControl = !mIsLockingControl;
            if (mIsLockingControl) {
                mIsShowOption = false;
            }
            setForLockOption(false);
        });
    }

    private void startCastScreen() {
        if (mCurrentDisplayId == Display.DEFAULT_DISPLAY) {
            pauseAllVideoView();
            mIsJumpingToMiraCast = true;
            gotoActivityWithFlag(AppConstants.FLAG_START_SCREEN_CAST);
        } else {
            Display currentDisplay = MiracastUtils.checkMiracastConnected(this);
            if (currentDisplay != null) {
                String tiviName = currentDisplay.getName() == null || currentDisplay.getName().length() == 0 ? "TV" : currentDisplay.getName();

                SweetAlertDialog dialogDisconnect = DialogFactory.getDialogDoSomething(this, "Miracast", "Your device is already connected to " + tiviName);
                dialogDisconnect.setConfirmButton("Disconnect", sweetAlertDialog -> MiracastUtils.gotoMiracastConnect(getApplicationContext(), PlayerActivity.this, false, true));
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
                resumeAllVideoView();
            } else {
                ToastUtils.showMessageLong(this, getString(R.string.activity_player_can_not_connect_to_miracast));
            }
        }
        super.onResume();
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
            mActivityPlayerBinding.fabOptionCastToScreen.setAlpha(1.0f);
            mActivityPlayerBinding.fabOptionCastToScreen.setBackgroundTintList(
                    getResources().getColorStateList(R.color.color_selected));
            mActivityPlayerBinding.fabOptionCastToScreen.setImageDrawable(getDrawable(R.drawable.ic_screen_mirroring_black));
        } else {
            mActivityPlayerBinding.fabOptionCastToScreen.setAlpha(.8f);
            mActivityPlayerBinding.fabOptionCastToScreen.setBackgroundTintList(
                    getResources().getColorStateList(R.color.color_unselected));
            mActivityPlayerBinding.fabOptionCastToScreen.setImageDrawable(getDrawable(R.drawable.ic_screen_mirroring_gray));
        }
    }

    @SuppressLint({"SourceLockedOrientationActivity", "UseCompatLoadingForColorStateLists",
            "UseCompatLoadingForDrawables"})
    private void setForLockOption(boolean firstTime) {
        if (mIsLockingControl) {
            ToastUtils.showMessageShort(this, getString(R.string.activity_player_notice_about_lock_control));

            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
            mActivityPlayerBinding.fabOptionLock.setAlpha(1.0f);
            mActivityPlayerBinding.fabOptionLock.setBackgroundTintList(
                    getResources().getColorStateList(R.color.color_selected));
            mActivityPlayerBinding.fabOptionLock.setImageDrawable(getDrawable(R.drawable.ic_lock));
        } else {
            if (!firstTime) {
                ToastUtils.showMessageShort(this, getString(R.string.activity_player_notice_about_unlock_control));
            }

            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
            mActivityPlayerBinding.fabOptionLock.setAlpha(.8f);
            mActivityPlayerBinding.fabOptionLock.setBackgroundTintList(
                    getResources().getColorStateList(R.color.color_unselected));
            mActivityPlayerBinding.fabOptionLock.setImageDrawable(getDrawable(R.drawable.ic_unlock));
        }

        setForShowOption();
    }

    @SuppressLint({"UseCompatLoadingForColorStateLists", "UseCompatLoadingForDrawables"})
    private void setForRepeatOption() {
        if (mIsRepeat) {
            mActivityPlayerBinding.fabOptionRepeat.setAlpha(1.0f);
            mActivityPlayerBinding.fabOptionRepeat.setImageDrawable(getDrawable(R.drawable.ic_repeat_on));
            mActivityPlayerBinding.fabOptionRepeat.setBackgroundTintList(
                    getResources().getColorStateList(R.color.color_selected));
            if (mPlayer != null) {
                mPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);
            }
        } else {
            mActivityPlayerBinding.fabOptionRepeat.setAlpha(.8f);
            mActivityPlayerBinding.fabOptionRepeat.setBackgroundTintList(
                    getResources().getColorStateList(R.color.color_unselected));
            mActivityPlayerBinding.fabOptionRepeat.setImageDrawable(getDrawable(R.drawable.ic_repeat_off));
            if (mPlayer != null) {
                mPlayer.setRepeatMode(Player.REPEAT_MODE_OFF);
            }
        }
    }

    private void setForRotateOption() {
        mActivityPlayerBinding.playerOptionRotate.setOnClickListener(v -> {
            if (mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
                setRequestedOrientation(ActivityInfo. SCREEN_ORIENTATION_LANDSCAPE);
            }
            setForOrientation();
        });
    }

    private void setForActionWhenPauseVideo() {
        if (mIsPause && mLoadVideoDone) {
            // do some thing
        } else {
            // do other thing
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        setForShowNavigationBar(mIsShowOption);
        super.onWindowFocusChanged(hasFocus);
    }

    private void setForShowNavigationBar(boolean isShow) {
        View decorView = getWindow().getDecorView();
        if (isShow) {

            if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT || mIsOpenWebView) {
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
            mActivityPlayerBinding.playerViewOption.setVisibility(View.VISIBLE);

            if (mIsLockingControl) {
                mActivityPlayerBinding.fabOptionRepeat.setVisibility(View.GONE);
                mActivityPlayerBinding.fabOptionCastToScreen.setVisibility(View.GONE);
                mActivityPlayerBinding.playerOptionTopArea.setVisibility(View.GONE);
                mActivityPlayerBinding.playerViewExoPlayer.setUseController(false);
                mActivityPlayerBinding.playerViewExoPlayer.hideController();
            } else {
                mActivityPlayerBinding.fabOptionRepeat.setVisibility(View.VISIBLE);
                mActivityPlayerBinding.fabOptionCastToScreen.setVisibility(View.VISIBLE);
                mActivityPlayerBinding.playerOptionTopArea.setVisibility(View.VISIBLE);
                mActivityPlayerBinding.playerViewExoPlayer.setUseController(true);
                mActivityPlayerBinding.playerViewExoPlayer.showController();
            }
        } else {
            mActivityPlayerBinding.playerViewOption.setVisibility(View.GONE);
            mActivityPlayerBinding.playerViewExoPlayer.setUseController(false);
            mActivityPlayerBinding.playerViewExoPlayer.hideController();
        }
    }

    private void releaseAllView() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }

        if (mSimpleExoPlayerView != null) {
            mSimpleExoPlayerView.setPlayer(null);
        }

        if (mIsOpenWebView) {
            mActivityPlayerBinding.playerViewWebview.clearCache(true);
            mActivityPlayerBinding.playerViewWebview.loadUrl("about:blank");
        }

        mIsOpenWebView = false;
    }

    private void preparePlayer() {
        releaseAllView();

        mActivityPlayerBinding.playerViewTypeLoadingTv.setText(getString(R.string.activity_player_loading_video_text));
        isNoticeAfterRender = false;

        mActivityPlayerBinding.playerOptionTopInformationTv.setText(mVideoName);

        Uri uri = Uri.parse(mVideoUrl);

        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        mPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
        mSimpleExoPlayerView = new SimpleExoPlayerView(this);
        mSimpleExoPlayerView = mActivityPlayerBinding.playerViewExoPlayer;

        mSimpleExoPlayerView.setUseController(true);
        mSimpleExoPlayerView.requestFocus();

        mSimpleExoPlayerView.setPlayer(mPlayer);

        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "prox_movieplayer"), bandwidthMeter);
        MediaSource mediaSource;

        if (mVideoUrl.toLowerCase().startsWith("http")) {
            if (Util.inferContentType(uri) == C.TYPE_HLS) {
                mediaSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
            } else if (Util.inferContentType(uri) == C.TYPE_DASH) {
                mediaSource = new DashMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
            } else {
                mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory).setExtractorsFactory(
                        new DefaultExtractorsFactory()).createMediaSource(uri);
            }
        } else {
            mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory).setExtractorsFactory(
                    new DefaultExtractorsFactory()).createMediaSource(uri);
        }

        mPlayer.prepare(mediaSource);
        mPlayer.setRepeatMode(mIsRepeat ? Player.REPEAT_MODE_ONE : Player.REPEAT_MODE_OFF);
        mPlayer.addListener(new ExoPlayer.EventListener() {

            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            }

            @Override
            public void onLoadingChanged(boolean isLoading) {
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == Player.STATE_IDLE || playbackState == Player.STATE_ENDED || !playWhenReady) {
                    mSimpleExoPlayerView.setKeepScreenOn(false);
                } else {
                    mSimpleExoPlayerView.setKeepScreenOn(true);
                }

                if (playbackState == Player.STATE_READY && !isNoticeAfterRender) {
                    isNoticeAfterRender = true;

                    if (mActivityPlayerBinding.playerViewExoPlayer.getVideoSurfaceView() != null) {
                        mActivityPlayerBinding.playerViewExoPlayer.getVideoSurfaceView().setOnClickListener(v -> {
                            mIsShowOption = !mIsShowOption;
                            setForShowOption();
                        });

                        mActivityPlayerBinding.playerViewExoPlayer.getVideoSurfaceView().setOnLongClickListener(v -> {

                            if (mIsShowOption || mIsLockingControl) {
                                mIsLockingControl = !mIsLockingControl;
                                mIsShowOption = true;

                                setForLockOption(false);
                            } else {
                                mIsShowOption = true;
                                setForShowOption();
                            }

                            return true;
                        });
                    }

                    if (mCurrentPosition != 0) {
                        mPlayer.seekTo(mCurrentPosition);
                        mCurrentPosition = 0;
                    }

                    startVideoView(true);
                }

                if (mPlayer != null && mPlayer.getPlaybackState() == Player.STATE_ENDED) {
                    if (!isNoticeAfterRender) {
                        ToastUtils.showMessageShort(PlayerActivity.this, getString(R.string.activity_player_nothing_to_play));
                        finishTotally();
                    }
                    if (!mIsRepeat) {
                        mIsShowOption = true;
                        mIsLockingControl = false;
                        setForShowOption();
                    }
                }

                if (playWhenReady) {
                    mIsPause = false;
                    setForActionWhenPauseVideo();
                } else {
                    mIsPause = true;
                    setForActionWhenPauseVideo();
                }
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {
            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
            }

            @SuppressLint("SetJavaScriptEnabled")
            @Override
            public void onPlayerError(ExoPlaybackException error) {

                if (mVideoUrl.toLowerCase().startsWith("http")) {
                    // start webview:
                    startWebview();
                } else {
                    mErrorVideoLoading = DialogFactory.getDialogConfirm(PlayerActivity.this,
                            getString(R.string.activity_player_can_not_play_video));
                    mErrorVideoLoading.setConfirmClickListener(sweetAlertDialog -> {
                        sweetAlertDialog.dismiss();

                        mLoadVideoDone = false;
                        setForShowVideoView();
                        preparePlayer();
                    });
                    mErrorVideoLoading.setConfirmText(getString(R.string.activity_player_reload_text));
                    mErrorVideoLoading.setCancelText(getString(R.string.exit_text));
                    mErrorVideoLoading.setCancelClickListener(sweetAlertDialog -> {
                        sweetAlertDialog.dismiss();
                        finishTotally();
                    });

                    mErrorVideoLoading.show();
                }
            }

            @Override
            public void onPositionDiscontinuity(int reason) {
            }

            @Override
            public void onPlaybackParametersChanged(@NotNull PlaybackParameters playbackParameters) {
            }

            @Override
            public void onSeekProcessed() {
            }
        });
        mPlayer.setVideoDebugListener(this);
    }

    private void startVideoView(boolean needToPlay) {
        mLoadVideoDone = true;
        setForShowVideoView();
        mIsShowOption = true;
        setForShowOption();
        mPlayer.setPlayWhenReady(needToPlay);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void startWebview() {
        mActivityPlayerBinding.playerViewTypeLoadingTv.setText(getString(R.string.activity_player_loading_web_text));

        mIsOpenWebView = true;

        mActivityPlayerBinding.playerViewWebview.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        CookieManager.getInstance().setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= 21) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(mActivityPlayerBinding.playerViewWebview, true);
        }

        WebSettings settings = mActivityPlayerBinding.playerViewWebview.getSettings();
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setSupportMultipleWindows(false);
        settings.setJavaScriptEnabled(true);
        settings.setLoadsImagesAutomatically(true);

        VideoEnabledWebChromeClient webChromeClient = new VideoEnabledWebChromeClient(
                mActivityPlayerBinding.playerViewWebviewVideoLayout, mActivityPlayerBinding.playerViewWebview) {
            @Override
            public void onProgressChanged(WebView view, int progress) {
            }
        };
        webChromeClient.setOnToggledFullscreen(fullscreen -> {
            // Your code to handle the full-screen change, for example showing and hiding the title bar. Example:
            if (fullscreen) {
                WindowManager.LayoutParams attrs = getWindow().getAttributes();
                attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                attrs.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                getWindow().setAttributes(attrs);
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
            } else {
                WindowManager.LayoutParams attrs = getWindow().getAttributes();
                attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
                attrs.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                getWindow().setAttributes(attrs);
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }

        });

        mActivityPlayerBinding.playerViewWebview.setWebChromeClient(webChromeClient);
        mIsRedirectOnceTime = false;

        mActivityPlayerBinding.playerViewWebview.setWebViewClient(new WebViewClient() {
            boolean loadingDone = false;
            boolean showWeb = false;

            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                loadingDone = true;

                int percentage = mActivityPlayerBinding.playerViewWebview.getProgress();

                if (percentage < 15) {
                    if (!showWeb && mIsOpenWebView) {
                        mIsOpenWebView = false;
                        mErrorVideoLoading = DialogFactory.getDialogError(PlayerActivity.this,
                                getString(R.string.activity_player_not_valid_file))
                                .setConfirmClickListener(sweetAlertDialog -> {
                                    sweetAlertDialog.dismiss();
                                    finishTotally();
                                })
                                .showCancelButton(false);
                        mErrorVideoLoading.show();
                    }
                } else {
                    if (!showWeb && mIsOpenWebView) {
                        setForShowWebView();
                    }
                }
            }

            @Override
            public void onPageCommitVisible(WebView view, String url) {
                mIsRedirectOnceTime = true;
                showWeb = true;
                if (!loadingDone && mIsOpenWebView) {
                    setForShowWebView();
                }

                super.onPageCommitVisible(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                new Thread(() -> {
                    loadingDone = false;
                    showWeb = false;

                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (mIsOpenWebView) {
                        if (!showWeb) {
                            runOnUiThread(() -> {
                                ToastUtils.showMessageShort(PlayerActivity.this,
                                        getString(R.string.activity_player_notice_about_web_view_not_response));
                            });
                        }
                    }
                }).start();
            }

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!mIsRedirectOnceTime) {
                    return super.shouldOverrideUrlLoading(view, url);
                } else {
                    return true;
                }
            }
        });

        mActivityPlayerBinding.playerViewWebview.loadUrl(mVideoUrl);
    }

    @Override
    public void onVideoEnabled(DecoderCounters counters) {
    }

    @Override
    public void onVideoDecoderInitialized(String decoderName, long initializedTimestampMs,
                                          long initializationDurationMs) {
    }

    @Override
    public void onVideoInputFormatChanged(Format format) {
    }

    @Override
    public void onDroppedFrames(int count, long elapsedMs) {
    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
    }

    @Override
    public void onRenderedFirstFrame(Surface surface) {
    }

    @Override
    public void onVideoDisabled(DecoderCounters counters) {
    }

    private void pauseAllVideoView() {
        if (mIsOpenWebView) {
            mActivityPlayerBinding.playerViewWebview.onPause();
        }else if (mLoadVideoDone) {
            mPlayer.setPlayWhenReady(false);
        }
    }

    private void resumeAllVideoView() {
        if (mIsOpenWebView) {
            mActivityPlayerBinding.playerViewWebview.onResume();
        } else if (mLoadVideoDone) {
            mPlayer.setPlayWhenReady(true);
        }
    }
}
