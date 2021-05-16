package com.cast.tv.screen.mirroring.iptv.ui.weblink;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.lifecycle.ViewModelProviders;

import com.ads.control.Admod;
import com.cast.tv.screen.mirroring.iptv.BuildConfig;
import com.cast.tv.screen.mirroring.iptv.R;
import com.cast.tv.screen.mirroring.iptv.constants.AppConstants;
import com.cast.tv.screen.mirroring.iptv.constants.DataConstants;
import com.cast.tv.screen.mirroring.iptv.databinding.ActivityWebLinkBinding;
import com.cast.tv.screen.mirroring.iptv.ui.base.BaseBindingActivity;
import com.cast.tv.screen.mirroring.iptv.ui.imageviewer.ViewerActivity;
import com.cast.tv.screen.mirroring.iptv.ui.player.PlayerActivity;
import com.cast.tv.screen.mirroring.iptv.utils.FirebaseUtils;
import com.cast.tv.screen.mirroring.iptv.utils.ToastUtils;
import com.cast.tv.screen.mirroring.iptv.utils.chromecast.ChromecastConnection;
import com.cast.tv.screen.mirroring.iptv.utils.chromecast.ExpandedControlsActivity;
import com.cast.tv.screen.mirroring.iptv.utils.file.FileUtils;
import com.cast.tv.screen.mirroring.iptv.utils.miracast.MiracastUtils;
import com.google.android.gms.cast.MediaError;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaLoadRequestData;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.images.WebImage;

import java.util.Random;

public class WebLinkActivity extends BaseBindingActivity<ActivityWebLinkBinding, WebLinkViewModel> implements WebLinkNavigator {
    private WebLinkViewModel mWebLinkViewModel;
    private ActivityWebLinkBinding mActivityWebLinkBinding;
    private int mTimeLoading = 500;
    private String mAnalysisLink = null;
    private String mFormatLink = null;
    private int mFormatType;
    private ChromecastConnection mChromecastConnection;

    private boolean mIsPlayingByChromeCast = false;
    private long mTimePlayingByChromeCast = 0;
    private boolean mIsJumpingToMiraCast = false;

    @Override
    public int getBindingVariable() {
        return 0;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_web_link;
    }

    @Override
    public WebLinkViewModel getViewModel() {
        mWebLinkViewModel = ViewModelProviders.of(this).get(WebLinkViewModel.class);
        return mWebLinkViewModel;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWebLinkViewModel.setNavigator(this);
        mActivityWebLinkBinding = getViewDataBinding();
        mIsPlayingByChromeCast = false;

        Random random = new Random();
        mTimeLoading = 500 + random.nextInt(500);
        initView();
    }

    @Override
    protected void initView() {
        setNoActionBar();
        setForClick();
        setForInputText();
        Admod.getInstance().loadSmallNativeFragment(this, BuildConfig.native_click_item_local_data_id, mActivityWebLinkBinding.nativeAds);

        setupChromecastConnection();
    }

    @Override
    protected void setClick() {

    }

    @Override
    public void onFragmentDetached(String tag) {

    }

    private void setForClick() {
        mActivityWebLinkBinding.backImg.setOnClickListener(view -> onBackPressed());
        mActivityWebLinkBinding.btnStartConnect.setOnClickListener(view -> {
            FirebaseUtils.sendEventFunctionUsed(this, FirebaseUtils.WEB_LINK_EVENT, "Click start connect");

            String text = mActivityWebLinkBinding.inputLink.getText().toString().trim();
            if (text == null || text.length() == 0) {
                ToastUtils.showMessageShort(this, getString(R.string.activity_web_link_input_empty));
                return;
            }

            startAnalysisLink(text);
        });
    }

    private void setForInputText() {
        mActivityWebLinkBinding.inputLink.setText("");
    }

    private void startAnalysisLink(String text) {
        if (mAnalysisLink == null || !mAnalysisLink.equals(text)) {
            mActivityWebLinkBinding.step1.setVisibility(View.INVISIBLE);
            mActivityWebLinkBinding.step2.setVisibility(View.INVISIBLE);

            if (text.toLowerCase().startsWith("https")) {
                String protocol = getString(R.string.activity_web_link_protocol_information, "https");
                mActivityWebLinkBinding.textStep1.setText(protocol);
                mActivityWebLinkBinding.step1.setVisibility(View.VISIBLE);
            } else if (text.toLowerCase().startsWith("http")) {
                String protocol = getString(R.string.activity_web_link_protocol_information, "http");
                mActivityWebLinkBinding.textStep1.setText(protocol);
                mActivityWebLinkBinding.step1.setVisibility(View.VISIBLE);
            } else {
                ToastUtils.showMessageShort(this, getString(R.string.activity_web_link_input_not_valid));
                return;
            }

            if (text.contains(".") && text.lastIndexOf(".") < text.length() - 1) {
                String format = text.substring(text.lastIndexOf(".") + 1);

                if (format.length() == 0) {
                    mFormatLink = "Unknown";
                    mFormatType = MediaMetadata.MEDIA_TYPE_MOVIE;
                } else {
                    if (format.toLowerCase().equals(DataConstants.VIDEO_MIME_1) ||
                            format.toLowerCase().equals(DataConstants.VIDEO_MIME_2) ||
                            format.toLowerCase().equals(DataConstants.VIDEO_MIME_3) ||
                            format.toLowerCase().equals(DataConstants.VIDEO_MIME_4) ||
                            format.toLowerCase().equals(DataConstants.VIDEO_MIME_5)) {
                        mFormatLink = getString(R.string.activity_web_link_format_type_video, format.toLowerCase());
                        mFormatType = MediaMetadata.MEDIA_TYPE_MOVIE;
                    } else if (format.toLowerCase().equals(DataConstants.PHOTO_MIME_1) ||
                            format.toLowerCase().equals(DataConstants.PHOTO_MIME_2) ||
                            format.toLowerCase().equals(DataConstants.PHOTO_MIME_3) ||
                            format.toLowerCase().equals(DataConstants.PHOTO_MIME_4) ||
                            format.toLowerCase().equals(DataConstants.PHOTO_MIME_5) ||
                            format.toLowerCase().equals(DataConstants.PHOTO_MIME_6) ||
                            format.toLowerCase().equals(DataConstants.PHOTO_MIME_7)) {
                        mFormatLink = getString(R.string.activity_web_link_format_type_photo, format.toLowerCase());
                        mFormatType = MediaMetadata.MEDIA_TYPE_PHOTO;
                    } else if (format.toLowerCase().equals(DataConstants.AUDIO_MIME_1) ||
                            format.toLowerCase().equals(DataConstants.AUDIO_MIME_2) ||
                            format.toLowerCase().equals(DataConstants.AUDIO_MIME_3) ||
                            format.toLowerCase().equals(DataConstants.AUDIO_MIME_4)) {
                        mFormatLink = getString(R.string.activity_web_link_format_type_audio, format.toLowerCase());
                        mFormatType = MediaMetadata.MEDIA_TYPE_MUSIC_TRACK;
                    } else if (format.toLowerCase().equals(DataConstants.STREAM_MIME_1)) {
                        mFormatLink = getString(R.string.activity_web_link_format_type_video, format.toLowerCase());
                        mFormatType = MediaMetadata.MEDIA_TYPE_MOVIE;
                    } else {
                        mFormatLink = "Unknown";
                        mFormatType = MediaMetadata.MEDIA_TYPE_MOVIE;
                    }
                }

                String formatInformation = getString(R.string.activity_web_link_format_information, mFormatLink);
                mActivityWebLinkBinding.textStep2.setText(formatInformation);
                mActivityWebLinkBinding.step2.setVisibility(View.VISIBLE);
            }

            mAnalysisLink = text;
        }

        startCheckCastConnection();
    }

    @Override
    public void onBackPressed() {
        mChromecastConnection.stopMediaIfPlaying();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        mChromecastConnection.stopMediaIfPlaying();
        super.onDestroy();
    }

    private void setupChromecastConnection() {
        mDefaultCastStateListener.setCastIcon(mActivityWebLinkBinding.castImg);
        mChromecastConnection = new ChromecastConnection(this, mDefaultCastStateListener);
        mChromecastConnection.initialize(AppConstants.CAST_APPLICATION_ID);

        mActivityWebLinkBinding.castImg.setOnClickListener(view -> {
            FirebaseUtils.sendEventFunctionUsed(this, FirebaseUtils.CAST_BUTTON_EVENT, "Click cast button", "Weblink layout");

            if (mChromecastConnection.isChromeCastConnect()) {
                mChromecastConnection.requestEndSession(new ChromecastConnection.RequestEndSessionCallback() {

                    @Override
                    public void onSuccess() {
                        mChromecastConnection.stopMediaIfPlaying();

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

    public void startCheckCastConnection() {
        if (mFormatType == MediaMetadata.MEDIA_TYPE_MOVIE || mFormatType == MediaMetadata.MEDIA_TYPE_PHOTO) {
            if (MiracastUtils.isMiraCastConnect(this)) {
                startPlayerConnection("Web link", mAnalysisLink);
            } else if (mChromecastConnection.isChromeCastConnect() && mFormatType == MediaMetadata.MEDIA_TYPE_PHOTO) {
                startChromeCastConnection();
            } else {
                final String[] fonts = {
                        getString(R.string.play_by_chrome_cast), getString(R.string.play_by_mirror_cast)
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.play_select_type));
                builder.setItems(fonts, (dialog, which) -> {
                    if (getString(R.string.play_by_chrome_cast).equals(fonts[which])) {
                        startChromeCastConnection();
                    } else if (getString(R.string.play_by_mirror_cast).equals(fonts[which])) {
                        startMiracastConnection();
                    }
                });
                builder.show();
            }
        } else {
            startChromeCastConnection();
        }
    }

    private void startMiracastConnection() {
        mIsJumpingToMiraCast = true;
        gotoActivityWithFlag(AppConstants.FLAG_START_SCREEN_CAST);
    }

    private void startPlayerConnection(String fileName, String filePath) {
        if (filePath == null) {
            ToastUtils.showMessageLong(this, getString(R.string.activity_player_can_not_connect_to_miracast));
            return;
        }

        Intent intent;
        if (mFormatType == MediaMetadata.MEDIA_TYPE_MOVIE) {
            intent = new Intent(WebLinkActivity.this, PlayerActivity.class);
        } else {
            intent = new Intent(WebLinkActivity.this, ViewerActivity.class);
        }
        intent.putExtra(EXTRA_FILE_NAME, fileName);
        intent.putExtra(EXTRA_FILE_PATH, filePath);
        startActivity(intent);
    }

    private void startChromeCastConnection() {
        if (mChromecastConnection.isChromeCastConnect()) {
            runOnUiThread(this::loadRemoteMedia);
        } else {
            requestStartSessionWithCallback(mChromecastConnection, () -> runOnUiThread(this::loadRemoteMedia), () -> mAnalysisLink = null);
        }
    }

    private void loadRemoteMedia() {
        if (!mChromecastConnection.isChromeCastConnect()) {
            ToastUtils.showMessageLong(getApplicationContext(), getString(R.string.cast_start_casting_error));
            mAnalysisLink = null;
            return;
        }

        final RemoteMediaClient remoteMediaClient = mChromecastConnection.getSession().getRemoteMediaClient();
        if (remoteMediaClient == null) {
            ToastUtils.showMessageLong(getApplicationContext(), getString(R.string.cast_start_casting_error));
            mAnalysisLink = null;
            return;
        }
        remoteMediaClient.registerCallback(new RemoteMediaClient.Callback() {
            @Override
            public void onStatusUpdated() {
                if (remoteMediaClient.isPlaying() || remoteMediaClient.isPaused() || remoteMediaClient.isBuffering()) {
                    mIsPlayingByChromeCast = true;
                    mTimePlayingByChromeCast = System.currentTimeMillis();

                    Intent intent = new Intent(WebLinkActivity.this, ExpandedControlsActivity.class);
                    startActivity(intent);
                    remoteMediaClient.unregisterCallback(this);
                }
            }

            @Override
            public void onMetadataUpdated() {
                super.onMetadataUpdated();
            }

            @Override
            public void onMediaError(MediaError mediaError) {
                super.onMediaError(mediaError);
                ToastUtils.showMessageLong(getApplicationContext(), getString(R.string.cast_start_casting_playing_fail));
                mAnalysisLink = null;
            }
        });

        MediaInfo mediaInfo = buildMediaInfo();
        if (mediaInfo == null) {
            ToastUtils.showMessageLong(getApplicationContext(), getString(R.string.cast_start_casting_playing_fail));
            mAnalysisLink = null;
        } else {
            runOnUiThread(() -> {
                if (remoteMediaClient.isPlaying()) {
                    remoteMediaClient.stop();
                }
                remoteMediaClient.load(new MediaLoadRequestData.Builder()
                        .setMediaInfo(mediaInfo)
                        .setAutoplay(true).build());
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mIsPlayingByChromeCast) {
            mIsPlayingByChromeCast = false;
            long currentTime = System.currentTimeMillis();
            if (currentTime - mTimePlayingByChromeCast > DURATION_FOR_ERROR_PLAYER) {
                return;
            }

            if (!mChromecastConnection.isPlayingSomething()) {
                ToastUtils.showMessageLong(this, getString(R.string.play_can_not_cast_this_video));
            }
        } else if (mIsJumpingToMiraCast) {
            mIsJumpingToMiraCast = false;

            if (MiracastUtils.isMiraCastConnect(this)) {
                ToastUtils.showMessageShort(this, getString(R.string.activity_player_connect_to_miracast_success));
                startPlayerConnection("Web link", mAnalysisLink);
            } else {
                ToastUtils.showMessageLong(this, getString(R.string.activity_player_can_not_connect_to_miracast));
            }
        }
    }

    private MediaInfo buildMediaInfo() {
        MediaMetadata mediaMetadata = new MediaMetadata(mFormatType);

        mediaMetadata.putString(MediaMetadata.KEY_SUBTITLE, mFormatLink);
        mediaMetadata.putString(MediaMetadata.KEY_TITLE, mAnalysisLink);

        if (mFormatType == MediaMetadata.MEDIA_TYPE_PHOTO) {
            mediaMetadata.addImage(new WebImage(Uri.parse(mAnalysisLink)));
        }

        if (mFormatType == MediaMetadata.MEDIA_TYPE_MUSIC_TRACK) {
            return new MediaInfo.Builder(mAnalysisLink)
                    .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                    .setContentType(FileUtils.getContentType(mAnalysisLink))
                    .setStreamDuration(MediaInfo.UNKNOWN_DURATION)
                    .setMetadata(mediaMetadata)
                    .build();
        } else if (mFormatType == MediaMetadata.MEDIA_TYPE_MOVIE) {
            return new MediaInfo.Builder(mAnalysisLink)
                    .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                    .setContentType(FileUtils.getContentType(mAnalysisLink))
                    .setStreamDuration(MediaInfo.UNKNOWN_DURATION)
                    .setMetadata(mediaMetadata)
                    .build();
        } else {
            return new MediaInfo.Builder(mAnalysisLink)
                    .setContentType(FileUtils.getContentType(mAnalysisLink))
                    .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                    .setMetadata(mediaMetadata)
                    .build();
        }
    }
}
