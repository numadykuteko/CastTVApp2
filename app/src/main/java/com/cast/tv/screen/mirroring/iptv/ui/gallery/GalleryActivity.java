package com.cast.tv.screen.mirroring.iptv.ui.gallery;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.cast.tv.screen.mirroring.iptv.BuildConfig;
import com.cast.tv.screen.mirroring.iptv.R;
import com.cast.tv.screen.mirroring.iptv.constants.AppConstants;
import com.cast.tv.screen.mirroring.iptv.constants.DataConstants;
import com.cast.tv.screen.mirroring.iptv.data.DataManager;
import com.cast.tv.screen.mirroring.iptv.data.model.FileData;
import com.cast.tv.screen.mirroring.iptv.data.model.ImageData;
import com.cast.tv.screen.mirroring.iptv.data.model.ImageDataList;
import com.cast.tv.screen.mirroring.iptv.databinding.ActivityGalleryBinding;
import com.cast.tv.screen.mirroring.iptv.listener.OnListItemClickListener;
import com.cast.tv.screen.mirroring.iptv.ui.base.BaseBindingActivity;
import com.cast.tv.screen.mirroring.iptv.ui.imageviewer.ViewerActivity;
import com.cast.tv.screen.mirroring.iptv.ui.player.PlayerActivity;
import com.cast.tv.screen.mirroring.iptv.utils.DataKeeper;
import com.cast.tv.screen.mirroring.iptv.utils.DialogFactory;
import com.cast.tv.screen.mirroring.iptv.utils.FirebaseUtils;
import com.cast.tv.screen.mirroring.iptv.utils.ToastUtils;
import com.cast.tv.screen.mirroring.iptv.utils.adapter.GalleryListAdapter;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class GalleryActivity extends BaseBindingActivity<ActivityGalleryBinding, GalleryViewModel> implements GalleryNavigator, OnListItemClickListener {

    private GalleryViewModel mGalleryViewModel;
    private ActivityGalleryBinding mActivityGalleryBinding;
    private boolean mIsLoading = false;
    private List<FileData> mListFile = new ArrayList<>();
    private GalleryListAdapter mGalleryListAdapter;
    private SweetAlertDialog mRequestPermissionDialog;
    private final int REQUEST_EXTERNAL_PERMISSION_FOR_LOAD_FILE_CODE = 1;
    private FileData mCurrentFolder;

    private FileData mSelectedMedia;

    private boolean mIsPlayingByChromeCast = false;
    private long mTimePlayingByChromeCast = 0;

    private boolean mIsJumpingToMiraCast = false;

    private ChromecastConnection mChromecastConnection;

    @Override
    public int getBindingVariable() {
        return 0;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_gallery;
    }

    @Override
    public GalleryViewModel getViewModel() {
        mGalleryViewModel = ViewModelProviders.of(this).get(GalleryViewModel.class);
        return mGalleryViewModel;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGalleryViewModel.setNavigator(this);
        mActivityGalleryBinding = getViewDataBinding();
        initView();

        File rootFile = Environment.getExternalStorageDirectory();
        String rootDir = rootFile.getAbsolutePath();

        mCurrentFolder = new FileData();
        mCurrentFolder.setFilePath(rootDir);
        mIsPlayingByChromeCast = false;
        mIsJumpingToMiraCast = false;

        reloadData(true);
    }

    @Override
    protected void initView() {
        setNoActionBar();
        setForLiveData();
        setForPullRefresh();
        setForClick();
        setupChromecastConnection();

        mGalleryListAdapter = new GalleryListAdapter(this);
        mActivityGalleryBinding.dataListArea.setLayoutManager(new LinearLayoutManager(this));
        mActivityGalleryBinding.dataListArea.setAdapter(mGalleryListAdapter);
    }

    @Override
    protected void setClick() {}

    @Override
    public void onFragmentDetached(String tag) {}

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

    @Override
    public void onBackPressed() {
        if (mCurrentFolder.getParentFile() != null && mCurrentFolder.getParentFile().getFilePath() != null && mCurrentFolder.getParentFile().getFilePath().length() > 0) {
            if (mIsLoading) return;

            File dir = new File(mCurrentFolder.getParentFile().getFilePath());
            if (dir.isDirectory()) {
                FileData temp = mCurrentFolder.getParentFile();
                mCurrentFolder = new FileData(temp);

                reloadData(false);

                return;
            }
        }

        mChromecastConnection.stopMediaIfPlaying();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        mChromecastConnection.stopMediaIfPlaying();
        super.onDestroy();
    }

    private void setForLiveData() {
        mGalleryViewModel.getListFileLiveData().observe(this, this::updateListData);
    }

    private void setForPullRefresh() {
        mActivityGalleryBinding.pullToRefresh.setOnRefreshListener(() -> {
            reloadData(false);
        });
    }

    private void setForClick() {
        mActivityGalleryBinding.backImg.setOnClickListener(view -> finish());
    }

    private void updateListData(List<FileData> fileDataList) {
        mListFile = new ArrayList<>();
        mListFile.addAll(fileDataList);

        if (mListFile.size() > 0) {
            mGalleryListAdapter.setData(mListFile);
            showDataArea();
        } else {
            showNoDataArea();
        }

        mIsLoading = false;
        mActivityGalleryBinding.pullToRefresh.setRefreshing(false);
    }

    private void reloadData(boolean isForceReload) {
        if (!hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) && !hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            mActivityGalleryBinding.pullToRefresh.setRefreshing(false);

            showPermissionIssueArea();
            mIsLoading = false;
            return;
        }

        if (mIsLoading) return;

        mIsLoading = true;

        if (mListFile == null || mListFile.size() == 0 || isForceReload) {
            showLoadingArea();
        }

        mGalleryViewModel.setCurrentPath(mCurrentFolder);
        mGalleryViewModel.getFileList();
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

    private void showPermissionIssueArea() {
        mActivityGalleryBinding.noPermissionArea.setOnClickListener(v -> {
            startRequestPermission();
        });
        mActivityGalleryBinding.dataListArea.setVisibility(View.GONE);
        mActivityGalleryBinding.noPermissionArea.setVisibility(View.VISIBLE);
        mActivityGalleryBinding.loadingArea.setVisibility(View.GONE);
        mActivityGalleryBinding.noDataErrorArea.setVisibility(View.GONE);
    }

    private void showDataArea() {
        mActivityGalleryBinding.dataListArea.setVisibility(View.VISIBLE);
        mActivityGalleryBinding.noPermissionArea.setVisibility(View.GONE);
        mActivityGalleryBinding.loadingArea.setVisibility(View.GONE);
        mActivityGalleryBinding.noDataErrorArea.setVisibility(View.GONE);
    }

    private void showNoDataArea() {
        mActivityGalleryBinding.dataListArea.setVisibility(View.GONE);
        mActivityGalleryBinding.noDataErrorArea.setVisibility(View.VISIBLE);
        mActivityGalleryBinding.noPermissionArea.setVisibility(View.GONE);
        mActivityGalleryBinding.loadingArea.setVisibility(View.GONE);
    }

    private void showLoadingArea() {
        mActivityGalleryBinding.dataListArea.setVisibility(View.GONE);
        mActivityGalleryBinding.loadingArea.setVisibility(View.VISIBLE);
        mActivityGalleryBinding.noPermissionArea.setVisibility(View.GONE);
        mActivityGalleryBinding.noDataErrorArea.setVisibility(View.GONE);
    }

    private void setupChromecastConnection() {
        mDefaultCastStateListener.setCastIcon(mActivityGalleryBinding.castImg);
        mChromecastConnection = new ChromecastConnection(this, mDefaultCastStateListener);
        mChromecastConnection.initialize(AppConstants.CAST_APPLICATION_ID);

        mActivityGalleryBinding.castImg.setOnClickListener(view -> {
            FirebaseUtils.sendEventFunctionUsed(this, FirebaseUtils.CAST_BUTTON_EVENT, "Click cast button", "Gallery layout");

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
        if (position >= 0 && position < mListFile.size()) {
            FileData fileData = mListFile.get(position);

            if (fileData.getFileType().equals(DataConstants.FILE_TYPE_DIRECTORY)) {
                mCurrentFolder = fileData;
                reloadData(false);
                return;
            }

            startCheckCastConnection(mListFile.get(position));
        }
    }

    private void updateSelectedPosition(int position) {
        if (position >= 0 && position < mListFile.size()) {
            mSelectedMedia = mListFile.get(position);
        } else {
            mSelectedMedia = null;
        }
    }

    public void startCheckCastConnection(FileData fileData) {
        if (fileData.getFileType().equals(DataConstants.FILE_TYPE_VIDEO) || fileData.getFileType().equals(DataConstants.FILE_TYPE_PHOTO)) {
            if (MiracastUtils.isMiraCastConnect(this)) {
                startPlayerConnection(fileData);
            } else if (mChromecastConnection.isChromeCastConnect() &&
                    fileData.getFileType().equals(DataConstants.FILE_TYPE_PHOTO)) {
                startChromeCastConnection(fileData);
            } else {
                final String[] fonts = {
                        getString(R.string.play_by_chrome_cast), getString(R.string.play_by_mirror_cast)
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.play_select_type));
                builder.setItems(fonts, (dialog, which) -> {
                    if (getString(R.string.play_by_chrome_cast).equals(fonts[which])) {
                        startChromeCastConnection(fileData);
                    } else if (getString(R.string.play_by_mirror_cast).equals(fonts[which])) {
                        startMiracastConnection(fileData);
                    }
                });
                builder.show();
            }
        } else {
            startChromeCastConnection(fileData);
        }
    }

    private void startMiracastConnection(FileData fileData) {
        mIsJumpingToMiraCast = true;
        mSelectedMedia = fileData;
        gotoActivityWithFlag(AppConstants.FLAG_START_SCREEN_CAST);
    }

    private void startPlayerConnection(FileData fileData) {
        if (fileData == null) {
            ToastUtils.showMessageLong(this, getString(R.string.activity_player_can_not_connect_to_miracast));
            return;
        }

        Intent intent;
        if (fileData.getFileType().equals(DataConstants.FILE_TYPE_VIDEO)) {
            intent = new Intent(GalleryActivity.this, PlayerActivity.class);
        } else {
            intent = new Intent(GalleryActivity.this, ViewerActivity.class);
            List<ImageData> imageDatas = mGalleryViewModel.getImageListFromFileData(mGalleryViewModel.getListFileLiveData().getValue());
            if (imageDatas != null && imageDatas.size() > 0) {
                ImageDataList sendWith = new ImageDataList(imageDatas);
                DataKeeper.setImageDataList(sendWith);
            }
        }
        intent.putExtra(EXTRA_FILE_NAME, fileData.getDisplayName());
        intent.putExtra(EXTRA_FILE_PATH, fileData.getFilePath());

        startActivity(intent);

        getViewModel().saveHistory(fileData);
    }

    private void startChromeCastConnection(FileData fileData) {
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
                if (mSelectedMedia != null && mSelectedMedia.getFileType() != null && !mSelectedMedia.getFileType().equals(DataConstants.FILE_TYPE_PHOTO)) {
                    if (remoteMediaClient.isPlaying() || remoteMediaClient.isPaused() || remoteMediaClient.isBuffering()) {
                        mIsPlayingByChromeCast = true;
                        mTimePlayingByChromeCast = System.currentTimeMillis();

                        Intent intent = new Intent(GalleryActivity.this, ExpandedControlsActivity.class);
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
                updateSelectedPosition(-1);
            }
        } else if (mIsJumpingToMiraCast) {
            mIsJumpingToMiraCast = false;

            if (MiracastUtils.isMiraCastConnect(this)) {
                ToastUtils.showMessageShort(this, getString(R.string.activity_player_connect_to_miracast_success));
                startPlayerConnection(mSelectedMedia);
            } else {
                ToastUtils.showMessageLong(this, getString(R.string.activity_player_can_not_connect_to_miracast));
                updateSelectedPosition(-1);
            }
        }
    }

    private MediaInfo buildMediaInfo() {
        if (mSelectedMedia == null || mSelectedMedia.getFilePath() == null) return null;

        String rootLink = Environment.getExternalStorageDirectory().getAbsolutePath();
        String ipAddress = DataManager.getInstance(this).getLastIPAddress();
        String mediaLink = "";
        if (mSelectedMedia != null && mSelectedMedia.getFilePath() != null && mSelectedMedia.getFilePath().contains(rootLink)) {
            mediaLink = mSelectedMedia.getFilePath().replace(rootLink, "http://" + ipAddress + ":8080");
        } else {
            return null;
        }

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

