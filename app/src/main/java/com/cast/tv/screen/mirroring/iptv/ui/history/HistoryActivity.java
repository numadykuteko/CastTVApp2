package com.cast.tv.screen.mirroring.iptv.ui.history;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.cast.tv.screen.mirroring.iptv.BuildConfig;
import com.cast.tv.screen.mirroring.iptv.R;
import com.cast.tv.screen.mirroring.iptv.constants.AppConstants;
import com.cast.tv.screen.mirroring.iptv.constants.DataConstants;
import com.cast.tv.screen.mirroring.iptv.data.DataManager;
import com.cast.tv.screen.mirroring.iptv.data.model.HistoryData;
import com.cast.tv.screen.mirroring.iptv.data.model.ImageData;
import com.cast.tv.screen.mirroring.iptv.data.model.ImageDataList;
import com.cast.tv.screen.mirroring.iptv.databinding.ActivityHistoryBinding;
import com.cast.tv.screen.mirroring.iptv.ui.base.BaseBindingActivity;
import com.cast.tv.screen.mirroring.iptv.ui.imageviewer.ViewerActivity;
import com.cast.tv.screen.mirroring.iptv.ui.player.PlayerActivity;
import com.cast.tv.screen.mirroring.iptv.utils.DataKeeper;
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
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import java.util.Arrays;
import java.util.List;

public class HistoryActivity extends BaseBindingActivity<ActivityHistoryBinding, HistoryViewModel> implements HistoryNavigator {
    private List<String> mTypeTitle;

    private int mFirstTimePosition;
    private FragmentPagerItemAdapter mFragmentAdapter;
    private ActivityHistoryBinding mActivityHistoryBinding;
    private HistoryViewModel mHistoryViewModel;
    private ChromecastConnection mChromecastConnection;

    private HistoryData mSelectedMedia;

    private boolean mIsPlayingByChromeCast = false;
    private long mTimePlayingByChromeCast = 0;
    private boolean mIsJumpingToMiraCast = false;

    @Override
    public int getBindingVariable() {
        return 0;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_history;
    }

    @Override
    public HistoryViewModel getViewModel() {
        mHistoryViewModel = ViewModelProviders.of(this).get(HistoryViewModel.class);
        return mHistoryViewModel;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mHistoryViewModel.setNavigator(this);
        mActivityHistoryBinding = getViewDataBinding();
        mIsPlayingByChromeCast = false;
        mIsJumpingToMiraCast = false;

        mFirstTimePosition = 0;
        mTypeTitle = Arrays.asList(
                getString(R.string.activity_main_video_cast_name),
                getString(R.string.activity_main_photo_cast_name),
                getString(R.string.activity_main_audio_cast_name));

        initView();
    }

    @Override
    protected void initView() {
        setNoActionBar();
        setForClick();
        setupChromecastConnection();

        if (mFragmentAdapter == null) {
            FragmentPagerItems.Creator pagesCreator = FragmentPagerItems.with(this);

            for (String filterTitle : mTypeTitle) {
                Bundle bundle = new Bundle();
                pagesCreator.add(filterTitle, HistoryFragment.class, bundle);
            }

            FragmentPagerItems pages = pagesCreator.create();
            mFragmentAdapter = new FragmentPagerItemAdapter(getSupportFragmentManager(), pages);

            mActivityHistoryBinding.viewpagerTypeFilter.setAdapter(mFragmentAdapter);
            mActivityHistoryBinding.viewpagerTypeFilter.setOffscreenPageLimit(3);
            mActivityHistoryBinding.viewpagerTypeFilter.setCurrentItem(mFirstTimePosition);

            mActivityHistoryBinding.tabTypeFilter.setViewPager(mActivityHistoryBinding.viewpagerTypeFilter);
        }
    }

    @Override
    protected void setClick() {

    }

    @Override
    public void onFragmentDetached(String tag) {

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

    private void setForClick() {
        mActivityHistoryBinding.backImg.setOnClickListener(view -> onBackPressed());
    }

    private void setupChromecastConnection() {
        mDefaultCastStateListener.setCastIcon(mActivityHistoryBinding.castImg);
        mChromecastConnection = new ChromecastConnection(this, mDefaultCastStateListener);
        mChromecastConnection.initialize(AppConstants.CAST_APPLICATION_ID);

        mActivityHistoryBinding.castImg.setOnClickListener(view -> {
            FirebaseUtils.sendEventFunctionUsed(this, FirebaseUtils.CAST_BUTTON_EVENT, "Click cast button", "History layout");

            if (mChromecastConnection.isChromeCastConnect()) {
                mChromecastConnection.requestEndSession(new ChromecastConnection.RequestEndSessionCallback() {

                    @Override
                    public void onSuccess() {
                        ToastUtils.showMessageLong(getApplicationContext(), getString(R.string.cast_stop_casting_success));
                        mChromecastConnection.stopMediaIfPlaying();
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

    public void startCheckCastConnection(HistoryData historyData) {
        if (historyData.getFileType().equals(DataConstants.FILE_TYPE_VIDEO) || historyData.getFileType().equals(DataConstants.FILE_TYPE_PHOTO)) {
            if (MiracastUtils.isMiraCastConnect(this)) {
                startPlayerConnection(historyData);
            } else if (mChromecastConnection.isChromeCastConnect() &&
                    historyData.getFileType().equals(DataConstants.FILE_TYPE_PHOTO)) {
                startChromeCastConnection(historyData);
            } else {
                final String[] fonts = {
                        getString(R.string.play_by_chrome_cast), getString(R.string.play_by_mirror_cast)
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.play_select_type));
                builder.setItems(fonts, (dialog, which) -> {
                    if (getString(R.string.play_by_chrome_cast).equals(fonts[which])) {
                        startChromeCastConnection(historyData);
                    } else if (getString(R.string.play_by_mirror_cast).equals(fonts[which])) {
                        startMiracastConnection(historyData);
                    }
                });
                builder.setOnDismissListener(dialog -> {
                    mSelectedMedia = null;
                });
                builder.show();
            }
        } else {
            startChromeCastConnection(historyData);
        }
    }

    private void startMiracastConnection(HistoryData historyData) {
        mIsJumpingToMiraCast = true;
        mSelectedMedia = historyData;
        gotoActivityWithFlag(AppConstants.FLAG_START_SCREEN_CAST);
    }

    private void startPlayerConnection(HistoryData historyData) {
        if (historyData == null) {
            ToastUtils.showMessageLong(this, getString(R.string.activity_player_can_not_connect_to_miracast));
            return;
        }

        Intent intent;
        if (historyData.getFileType().equals(DataConstants.FILE_TYPE_VIDEO)) {
            intent = new Intent(HistoryActivity.this, PlayerActivity.class);
        } else {
            intent = new Intent(HistoryActivity.this, ViewerActivity.class);
            List<ImageData> imageDatas = mHistoryViewModel.getImageListFromSavedData(mHistoryViewModel.getListFileLiveData().getValue());
            if (imageDatas != null && imageDatas.size() > 0) {
                ImageDataList sendWith = new ImageDataList(imageDatas);
                DataKeeper.setImageDataList(sendWith);
            }
        }
        intent.putExtra(EXTRA_FILE_NAME, historyData.getDisplayName());
        intent.putExtra(EXTRA_FILE_PATH, historyData.getFilePath());
        startActivity(intent);
    }

    private void startChromeCastConnection(HistoryData historyData) {
        mSelectedMedia = historyData;

        if (mChromecastConnection.isChromeCastConnect()) {
            startMediaService();
        } else {
            requestStartSessionWithCallback(mChromecastConnection, this::startMediaService, () -> {});
        }
    }

    private void startMediaService() {
        prepareSimpleWebServer(() -> {}, this::loadRemoteMedia);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == START_MEDIA_SERVICE) {
            if (resultCode == DataConstants.CONNECT_SUCCESS_MESSAGE) {
                ToastUtils.showMessageShort(this, getString(R.string.cast_start_casting_success));
                hidePrepareServerDialog();
                loadRemoteMedia();
            } else if (resultCode == DataConstants.CONNECT_ERROR_MESSAGE) {
                ToastUtils.showMessageLong(this, getString(R.string.cast_start_casting_error));
                hidePrepareServerDialog();
                mChromecastConnection.stopMediaIfPlaying();
            } else if (resultCode == DataConstants.CONNECT_DESTROY_MESSAGE) {
                ToastUtils.showMessageShort(this, getString(R.string.cast_start_casting_playing_fail));
                hidePrepareServerDialog();
                mChromecastConnection.stopMediaIfPlaying();
            }
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void loadRemoteMedia() {
        if (!mChromecastConnection.isChromeCastConnect()) {
            ToastUtils.showMessageLong(this, getString(R.string.cast_start_casting_error));
            return;
        }

        final RemoteMediaClient remoteMediaClient = mChromecastConnection.getSession().getRemoteMediaClient();
        if (remoteMediaClient == null) {
            ToastUtils.showMessageLong(this, getString(R.string.cast_start_casting_error));
            return;
        }
        remoteMediaClient.registerCallback(new RemoteMediaClient.Callback() {
            @Override
            public void onStatusUpdated() {
                if (mSelectedMedia != null && mSelectedMedia.getFileType() != null && !mSelectedMedia.getFileType().equals(DataConstants.FILE_TYPE_PHOTO)) {
                    if (remoteMediaClient.isPlaying() || remoteMediaClient.isPaused() || remoteMediaClient.isBuffering()) {
                        mIsPlayingByChromeCast = true;
                        mTimePlayingByChromeCast = System.currentTimeMillis();

                        Intent intent = new Intent(HistoryActivity.this, ExpandedControlsActivity.class);
                        startActivity(intent);
                        remoteMediaClient.unregisterCallback(this);
                    }
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
            }
        });

        MediaInfo mediaInfo = buildMediaInfo();
        if (mediaInfo == null) {
            ToastUtils.showMessageLong(this, getString(R.string.cast_start_casting_playing_fail));
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
                startPlayerConnection(mSelectedMedia);
            } else {
                ToastUtils.showMessageLong(this, getString(R.string.activity_player_can_not_connect_to_miracast));
                mSelectedMedia = null;
            }
        }
    }

    private MediaInfo buildMediaInfo() {
        if (mSelectedMedia == null || mSelectedMedia.getFilePath() == null) return null;

        MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);

        if (mSelectedMedia.getFileType().equals(DataConstants.FILE_TYPE_AUDIO)) {
            mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK);
        } else if (mSelectedMedia.getFileType().equals(DataConstants.FILE_TYPE_PHOTO)) {
            mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_PHOTO);
        } else if (mSelectedMedia.getFileType().equals(DataConstants.FILE_TYPE_VIDEO)) {
            mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
        }

        mediaMetadata.putString(MediaMetadata.KEY_SUBTITLE, mSelectedMedia.getFileType());
        mediaMetadata.putString(MediaMetadata.KEY_TITLE, mSelectedMedia.getDisplayName());

        String rootLink = Environment.getExternalStorageDirectory().getAbsolutePath();
        String ipAddress = DataManager.getInstance(this).getLastIPAddress();
        String mediaLink = "";
        if (mSelectedMedia.getFilePath().contains(rootLink)) {
            mediaLink = mSelectedMedia.getFilePath().replace(rootLink, "http://" + ipAddress + ":8080");
        } else {
            return null;
        }

        if (mSelectedMedia.getFileType().equals(DataConstants.FILE_TYPE_PHOTO)) {
            mediaMetadata.addImage(new WebImage(Uri.parse(mediaLink)));
        }

        if (mSelectedMedia.getFileType().equals(DataConstants.FILE_TYPE_VIDEO)) {
            return new MediaInfo.Builder(mediaLink)
                    .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                    .setContentType(FileUtils.getContentType(mSelectedMedia.getFilePath()))
                    .setStreamDuration(FileUtils.getDurationOfMedia(this, mSelectedMedia.getFilePath()))
                    .setMetadata(mediaMetadata)
                    .build();
        } else if (mSelectedMedia.getFileType().equals(DataConstants.FILE_TYPE_AUDIO)) {
            return new MediaInfo.Builder(mediaLink)
                    .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                    .setContentType(FileUtils.getContentType(mSelectedMedia.getFilePath()))
                    .setStreamDuration(FileUtils.getDurationOfMedia(this, mSelectedMedia.getFilePath()))
                    .setMetadata(mediaMetadata)
                    .build();
        } else {
            return new MediaInfo.Builder(mediaLink)
                    .setContentType(FileUtils.getContentType(mSelectedMedia.getFilePath()))
                    .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                    .setMetadata(mediaMetadata)
                    .build();
        }
    }
}

