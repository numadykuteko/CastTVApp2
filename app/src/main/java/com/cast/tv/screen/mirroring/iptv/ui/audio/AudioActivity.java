package com.cast.tv.screen.mirroring.iptv.ui.audio;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.cast.tv.screen.mirroring.iptv.BuildConfig;
import com.cast.tv.screen.mirroring.iptv.R;
import com.cast.tv.screen.mirroring.iptv.constants.AppConstants;
import com.cast.tv.screen.mirroring.iptv.constants.DataConstants;
import com.cast.tv.screen.mirroring.iptv.data.DataManager;
import com.cast.tv.screen.mirroring.iptv.data.model.FileData;
import com.cast.tv.screen.mirroring.iptv.databinding.ActivityAudioBinding;
import com.cast.tv.screen.mirroring.iptv.listener.OnFileItemClickListener;
import com.cast.tv.screen.mirroring.iptv.ui.base.BaseBindingActivity;

import com.cast.tv.screen.mirroring.iptv.utils.DialogFactory;
import com.cast.tv.screen.mirroring.iptv.utils.FirebaseUtils;
import com.cast.tv.screen.mirroring.iptv.utils.ToastUtils;
import com.cast.tv.screen.mirroring.iptv.utils.adapter.AudioListAdapter;
import com.cast.tv.screen.mirroring.iptv.utils.chromecast.ChromecastConnection;
import com.cast.tv.screen.mirroring.iptv.utils.chromecast.ExpandedControlsActivity;
import com.cast.tv.screen.mirroring.iptv.utils.file.FileUtils;
import com.google.android.gms.cast.MediaError;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaLoadRequestData;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class AudioActivity extends BaseBindingActivity<ActivityAudioBinding, AudioViewModel> implements AudioNavigator, OnFileItemClickListener {

    private AudioViewModel mAudioViewModel;
    private ActivityAudioBinding mActivityAudioBinding;
    private boolean mIsLoading = false;
    private List<FileData> mListFile = new ArrayList<>();
    private AudioListAdapter mAudioListAdapter;
    private SweetAlertDialog mRequestPermissionDialog;
    private final int REQUEST_EXTERNAL_PERMISSION_FOR_LOAD_FILE_CODE = 1;

    private FileData mSelectedMedia;

    private ChromecastConnection mChromecastConnection;

    @Override
    public int getBindingVariable() {
        return 0;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_audio;
    }

    @Override
    public AudioViewModel getViewModel() {
        mAudioViewModel = ViewModelProviders.of(this).get(AudioViewModel.class);
        return mAudioViewModel;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAudioViewModel.setNavigator(this);
        mActivityAudioBinding = getViewDataBinding();
        initView();
        reloadData(true);
    }

    @Override
    protected void initView() {
        setNoActionBar();
        setForLiveData();
        setForPullRefresh();
        setForClick();
        setupChromecastConnection();

        mAudioListAdapter = new AudioListAdapter(this);
        StaggeredGridLayoutManager mGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mActivityAudioBinding.dataListArea.setLayoutManager(mGridLayoutManager);
        mActivityAudioBinding.dataListArea.setAdapter(mAudioListAdapter);
    }

    @Override
    protected void setClick() {}

    @Override
    public void onFragmentDetached(String tag) {}

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_PERMISSION_FOR_LOAD_FILE_CODE:
                if ((grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    mRequestPermissionDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                    mRequestPermissionDialog.setTitleText(getString(R.string.thankyou_text));
                    mRequestPermissionDialog.setContentText(getString(R.string.get_file_now));
                    mRequestPermissionDialog.showCancelButton(false);
                    mRequestPermissionDialog.setConfirmText(getString(R.string.confirm_text));
                    mRequestPermissionDialog.setConfirmClickListener(sweetAlertDialog -> {
                        sweetAlertDialog.dismiss();
                        reloadData(true);
                    });
                } else {
                    mRequestPermissionDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    mRequestPermissionDialog.setTitleText(getString(R.string.title_need_permission_fail));
                    mRequestPermissionDialog.setContentText(getString(R.string.couldnt_get_file_now));
                    mRequestPermissionDialog.showCancelButton(false);
                    mRequestPermissionDialog.setConfirmText(getString(R.string.confirm_text));
                    mRequestPermissionDialog.setConfirmClickListener(Dialog::dismiss);
                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void setForLiveData() {
        mAudioViewModel.getListFileLiveData().observe(this, this::updateListData);
    }

    private void setForPullRefresh() {
        mActivityAudioBinding.pullToRefresh.setOnRefreshListener(() -> {
            reloadData(false);
        });
    }

    private void setForClick() {
        mActivityAudioBinding.backImg.setOnClickListener(view -> onBackPressed());
    }

    private void updateListData(List<FileData> fileDataList) {
        mListFile = new ArrayList<>();
        mListFile.addAll(fileDataList);

        if (mListFile.size() > 0) {
            mListFile.add(0, new FileData());
            Parcelable oldPosition = null;
            if (mActivityAudioBinding.dataListArea.getLayoutManager() != null) {
                oldPosition = mActivityAudioBinding.dataListArea.getLayoutManager().onSaveInstanceState();
            }
            mAudioListAdapter.setData(mListFile);
            if (oldPosition != null) {
                mActivityAudioBinding.dataListArea.getLayoutManager().onRestoreInstanceState(oldPosition);
            }
            showDataArea();
        } else {
            showNoDataArea();
        }

        mIsLoading = false;
        mActivityAudioBinding.pullToRefresh.setRefreshing(false);
    }

    private void reloadData(boolean isForceReload) {
        if (!hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) && !hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            showPermissionIssueArea();
            mIsLoading = false;
            mActivityAudioBinding.pullToRefresh.setRefreshing(false);
            return;
        }

        if (mIsLoading) return;
        mIsLoading = true;

        if (mListFile == null || mListFile.size() == 0 || isForceReload) {
            showLoadingArea();
        }

        mAudioViewModel.getFileList(FileUtils.SORT_BY_DATE);
    }

    private void startRequestPermission() {
        if (!hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) && !hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            mRequestPermissionDialog = DialogFactory.getDialogRequestSomething(this, getString(R.string.title_need_permission), getString(R.string.need_permission_to_get_file));
            mRequestPermissionDialog.setConfirmClickListener(sweetAlertDialog -> {
                requestPermissionsSafely(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_PERMISSION_FOR_LOAD_FILE_CODE);
            });
            mRequestPermissionDialog.setCancelClickListener(sweetAlertDialog -> {
                sweetAlertDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                sweetAlertDialog.setTitleText(getString(R.string.title_need_permission_fail));
                sweetAlertDialog.setContentText(getString(R.string.couldnt_get_file_now));
                sweetAlertDialog.setConfirmClickListener(Dialog::dismiss);
                sweetAlertDialog.showCancelButton(false);
                sweetAlertDialog.setConfirmText(getString(R.string.confirm_text));
            });
            mRequestPermissionDialog.show();
        } else {
            reloadData(true);
        }
    }

    private void showNoDataArea() {
        mActivityAudioBinding.dataListArea.setVisibility(View.GONE);
        mActivityAudioBinding.noDataErrorArea.setVisibility(View.VISIBLE);
        mActivityAudioBinding.noPermissionArea.setVisibility(View.GONE);
        mActivityAudioBinding.loadingArea.setVisibility(View.GONE);
    }

    private void showPermissionIssueArea() {
        mActivityAudioBinding.noPermissionArea.setOnClickListener(v -> {
            startRequestPermission();
        });
        mActivityAudioBinding.dataListArea.setVisibility(View.GONE);
        mActivityAudioBinding.noDataErrorArea.setVisibility(View.GONE);
        mActivityAudioBinding.noPermissionArea.setVisibility(View.VISIBLE);
        mActivityAudioBinding.loadingArea.setVisibility(View.GONE);
    }

    private void showDataArea() {
        mActivityAudioBinding.dataListArea.setVisibility(View.VISIBLE);
        mActivityAudioBinding.noDataErrorArea.setVisibility(View.GONE);
        mActivityAudioBinding.noPermissionArea.setVisibility(View.GONE);
        mActivityAudioBinding.loadingArea.setVisibility(View.GONE);
    }

    private void showLoadingArea() {
        mActivityAudioBinding.dataListArea.setVisibility(View.GONE);
        mActivityAudioBinding.noDataErrorArea.setVisibility(View.GONE);
        mActivityAudioBinding.loadingArea.setVisibility(View.VISIBLE);
        mActivityAudioBinding.noPermissionArea.setVisibility(View.GONE);
    }

    private void setupChromecastConnection() {
        mDefaultCastStateListener.setCastIcon(mActivityAudioBinding.castImg);
        mChromecastConnection = new ChromecastConnection(this, mDefaultCastStateListener);
        mChromecastConnection.initialize(AppConstants.CAST_APPLICATION_ID);

        mActivityAudioBinding.castImg.setOnClickListener(view -> {
            FirebaseUtils.sendEventFunctionUsed(this, FirebaseUtils.CAST_BUTTON_EVENT, "Click cast button", "Audio layout");

            if (mChromecastConnection.isChromeCastConnect()) {
                mChromecastConnection.requestEndSession(new ChromecastConnection.RequestEndSessionCallback() {
                    @Override
                    public void onSuccess() {
                        ToastUtils.showMessageLong(getApplicationContext(), getString(R.string.cast_stop_casting_success));
                        updateSelectedPosition(-1);
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

    @Override
    public void onClickItem(int position) {
        startCheckCastConnection(mListFile.get(position));
    }

    @Override
    public void onClickBookmark(int position) {
        if (position >= 0 && position < mListFile.size()) {
            mAudioViewModel.revertBookmark(mListFile.get(position));
            mListFile.get(position).revertBookmark();
            mAudioListAdapter.setDataPosition(position, mListFile.get(position));
        }
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

    private void updateSelectedPosition(int position) {
        if (position >= 0 && position < mListFile.size()) {
            mSelectedMedia = mListFile.get(position);
            mAudioListAdapter.setCurrentItem(position);
        } else {
            mSelectedMedia = null;
            mAudioListAdapter.setCurrentItem(-1);
        }
    }

    private void startCheckCastConnection(FileData fileData) {
        if (mChromecastConnection.isChromeCastConnect()) {
            startMediaService(fileData);
        } else {
            requestStartSessionWithCallback(mChromecastConnection, () -> startMediaService(fileData), () -> {});
        }
    }

    private void startMediaService(FileData fileData) {
        prepareSimpleWebServer(() -> updateSelectedPosition(mListFile.indexOf(fileData)), this::loadRemoteMedia);
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
                updateSelectedPosition(-1);
                hidePrepareServerDialog();
                mChromecastConnection.stopMediaIfPlaying();
            } else if (resultCode == DataConstants.CONNECT_DESTROY_MESSAGE) {
                ToastUtils.showMessageShort(this, getString(R.string.cast_start_casting_playing_fail));
                updateSelectedPosition(-1);
                hidePrepareServerDialog();
                mChromecastConnection.stopMediaIfPlaying();
            }
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void loadRemoteMedia() {
        if (!mChromecastConnection.isChromeCastConnect()) {
            ToastUtils.showMessageLong(getApplicationContext(), getString(R.string.cast_start_casting_error));
            updateSelectedPosition(-1);
            return;
        }

        final RemoteMediaClient remoteMediaClient = mChromecastConnection.getSession().getRemoteMediaClient();
        if (remoteMediaClient == null) {
            ToastUtils.showMessageLong(getApplicationContext(), getString(R.string.cast_start_casting_error));
            updateSelectedPosition(-1);
            return;
        }
        remoteMediaClient.registerCallback(new RemoteMediaClient.Callback() {
            @Override
            public void onStatusUpdated() {
                if (remoteMediaClient.isPlaying() || remoteMediaClient.isPaused() || remoteMediaClient.isBuffering()) {
                    Intent intent = new Intent(AudioActivity.this, ExpandedControlsActivity.class);
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
                updateSelectedPosition(-1);
            }
        });

        MediaInfo mediaInfo = buildMediaInfo();
        if (mediaInfo == null) {
            ToastUtils.showMessageLong(getApplicationContext(), getString(R.string.cast_start_casting_playing_fail));
            updateSelectedPosition(-1);
        } else {
            runOnUiThread(() -> {
                try {
                    if (remoteMediaClient.isPlaying()) {
                        remoteMediaClient.stop();
                    }
                    remoteMediaClient.load(new MediaLoadRequestData.Builder()
                            .setMediaInfo(mediaInfo)
                            .setAutoplay(true).build());
                    getViewModel().saveHistory(mSelectedMedia);
                } catch (Exception e) {
                    ToastUtils.showMessageLong(getApplicationContext(), getString(R.string.cast_start_casting_playing_fail));
                    updateSelectedPosition(-1);
                }
            });
        }
    }

    private MediaInfo buildMediaInfo() {
        if (mSelectedMedia == null || mSelectedMedia.getFilePath() == null) return null;

        MediaMetadata audioMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK);

        audioMetadata.putString(MediaMetadata.KEY_SUBTITLE, mSelectedMedia.getFileType());
        audioMetadata.putString(MediaMetadata.KEY_TITLE, mSelectedMedia.getDisplayName());

        String rootLink = Environment.getExternalStorageDirectory().getAbsolutePath();
        String ipAddress = DataManager.getInstance(this).getLastIPAddress();
        String mediaLink = "";
        if (mSelectedMedia.getFilePath().contains(rootLink)) {
            mediaLink = mSelectedMedia.getFilePath().replace(rootLink, "http://" + ipAddress + ":8080");
        } else {
            return null;
        }

        return new MediaInfo.Builder(mediaLink)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType(FileUtils.getContentType(mSelectedMedia.getFilePath()))
                .setStreamDuration(FileUtils.getDurationOfMedia(this, mSelectedMedia.getFilePath()))
                .setMetadata(audioMetadata)
                .build();
    }
}
