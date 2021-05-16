package com.cast.tv.screen.mirroring.iptv.ui.googledrive;

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

import com.ads.control.Admod;
import com.cast.tv.screen.mirroring.iptv.BuildConfig;
import com.cast.tv.screen.mirroring.iptv.R;
import com.cast.tv.screen.mirroring.iptv.constants.AppConstants;
import com.cast.tv.screen.mirroring.iptv.constants.DataConstants;
import com.cast.tv.screen.mirroring.iptv.data.DataManager;
import com.cast.tv.screen.mirroring.iptv.data.model.FileData;
import com.cast.tv.screen.mirroring.iptv.databinding.ActivityGoogleDriveBinding;
import com.cast.tv.screen.mirroring.iptv.ui.base.BaseBindingActivity;
import com.cast.tv.screen.mirroring.iptv.ui.imageviewer.ViewerActivity;
import com.cast.tv.screen.mirroring.iptv.ui.player.PlayerActivity;
import com.cast.tv.screen.mirroring.iptv.utils.DialogFactory;
import com.cast.tv.screen.mirroring.iptv.utils.FirebaseUtils;
import com.cast.tv.screen.mirroring.iptv.utils.PackageUtils;
import com.cast.tv.screen.mirroring.iptv.utils.ToastUtils;
import com.cast.tv.screen.mirroring.iptv.utils.chromecast.ChromecastConnection;
import com.cast.tv.screen.mirroring.iptv.utils.chromecast.ExpandedControlsActivity;
import com.cast.tv.screen.mirroring.iptv.utils.file.FileUtils;
import com.cast.tv.screen.mirroring.iptv.utils.file.RealPathUtil;
import com.cast.tv.screen.mirroring.iptv.utils.miracast.MiracastUtils;
import com.google.android.gms.cast.MediaError;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaLoadRequestData;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.images.WebImage;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class GoogleDriveActivity extends BaseBindingActivity<ActivityGoogleDriveBinding, GoogleDriveViewModel> implements GoogleDriveNavigator {

    private GoogleDriveViewModel mGoogleDriveViewModel;
    private ActivityGoogleDriveBinding mActivityGoogleDriveBinding;
    private SweetAlertDialog mRequestPermissionDialog;
    private final int REQUEST_EXTERNAL_PERMISSION_FOR_LOAD_FILE_CODE = 1;

    private ChromecastConnection mChromecastConnection;

    private FileData mSelectedMedia;

    private boolean mIsOpening = false;

    private boolean mIsPlayingByChromeCast = false;
    private long mTimePlayingByChromeCast = 0;
    private boolean mIsJumpingToMiraCast = false;

    @Override
    public int getBindingVariable() {
        return 0;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_google_drive;
    }

    @Override
    public GoogleDriveViewModel getViewModel() {
        mGoogleDriveViewModel = ViewModelProviders.of(this).get(GoogleDriveViewModel.class);
        return mGoogleDriveViewModel;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGoogleDriveViewModel.setNavigator(this);
        mActivityGoogleDriveBinding = getViewDataBinding();
        mIsPlayingByChromeCast = false;
        mIsJumpingToMiraCast = false;

        initView();
        reloadData();
    }

    @Override
    protected void initView() {
        setNoActionBar();
        setForLiveData();
        setForPullRefresh();
        setForClick();
        setupChromecastConnection();

        preloadGoogleDriveAdsIfInit();
        Admod.getInstance().loadSmallNativeFragment(this, BuildConfig.native_click_item_local_data_id, mActivityGoogleDriveBinding.nativeAds);
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
                        reloadData();
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
        mChromecastConnection.stopMediaIfPlaying();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        mChromecastConnection.stopMediaIfPlaying();
        super.onDestroy();
    }

    private void reloadData() {
        if (!hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) && !hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            showPermissionIssueArea();
        } else {
            showAccessButtonArea();
        }
    }

    private void setForLiveData() {
    }

    private void setForPullRefresh() {
    }

    private void setupChromecastConnection() {
        mDefaultCastStateListener.setCastIcon(mActivityGoogleDriveBinding.castImg);
        mChromecastConnection = new ChromecastConnection(this, mDefaultCastStateListener);
        mChromecastConnection.initialize(AppConstants.CAST_APPLICATION_ID);

        mActivityGoogleDriveBinding.castImg.setOnClickListener(view -> {
            FirebaseUtils.sendEventFunctionUsed(this, FirebaseUtils.CAST_BUTTON_EVENT, "Click cast button", "Google Drive layout");

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

    private void setForClick() {
        mActivityGoogleDriveBinding.backImg.setOnClickListener(view -> onBackPressed());
        mActivityGoogleDriveBinding.accessButtonArea.setOnClickListener(view -> {
            FirebaseUtils.sendEventFunctionUsed(this, FirebaseUtils.GOOGLE_DRIVE_EVENT, "Click go to Google Drive");
            startSelectFileFromGoogleDrive();
        });
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
            reloadData();
        }
    }


    private void showPermissionIssueArea() {
        mActivityGoogleDriveBinding.noPermissionArea.setOnClickListener(v -> {
            startRequestPermission();
        });
        mActivityGoogleDriveBinding.noPermissionArea.setVisibility(View.VISIBLE);
        mActivityGoogleDriveBinding.accessButtonArea.setVisibility(View.GONE);
    }

    private void showAccessButtonArea() {
        mActivityGoogleDriveBinding.noPermissionArea.setVisibility(View.GONE);
        mActivityGoogleDriveBinding.accessButtonArea.setVisibility(View.VISIBLE);
    }

    private void startSelectFileFromGoogleDrive() {
        if (!PackageUtils.isAppAvailable(this, AppConstants.GOOGLE_DRIVE_PACKAGE_NAME)) {
            SweetAlertDialog packageNotFound = DialogFactory.getDialogRequestSomething(this, getString(R.string.package_not_found_title), getString(R.string.package_not_found_message));
            packageNotFound.setCancelButton(getString(R.string.cancel_text), Dialog::dismiss);
            packageNotFound.setConfirmButton(getString(R.string.confirm_text), sweetAlertDialog -> {
                sweetAlertDialog.dismiss();
                PackageUtils.gotoDownloadApp(this, AppConstants.GOOGLE_DRIVE_PACKAGE_NAME);
            });
            packageNotFound.show();

            return;
        }

        if (mIsOpening) return;

        Uri uri = Uri.parse(Environment.getRootDirectory() + "/");

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setDataAndType(uri, "*/*");

        intent.setPackage(AppConstants.GOOGLE_DRIVE_PACKAGE_NAME);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        showAdsBeforeAction(mGoogleDriveInterstitialAd, () -> {
            try {
                startActivityForResult(Intent.createChooser(intent, String.valueOf(R.string.select_file_title)), TAKE_FILE_FROM_GOOGLE_DRIVE_REQUEST);
                mIsOpening = true;
            } catch (android.content.ActivityNotFoundException ex) {
                // TODO show error
            }
        });

    }

    @Override
    protected void updateFilePathFromCloudServer(Uri uri, String filePath) {
        super.updateFilePathFromCloudServer(uri, filePath);
        if (filePath != null && FileUtils.checkSupportedFile(filePath)) {
            FileData fileData = new FileData();
            fileData.setFilePath(filePath);
            fileData.setFileUri(uri);
            if (FileUtils.isVideoFile(filePath)) {
                fileData.setFileType(DataConstants.FILE_TYPE_VIDEO);
            } else if (FileUtils.isPhotoFile(filePath)) {
                fileData.setFileType(DataConstants.FILE_TYPE_PHOTO);
            } else if (FileUtils.isAudioFile(filePath)) {
                fileData.setFileType(DataConstants.FILE_TYPE_AUDIO);
            } else {
                SweetAlertDialog errorLoadFile = DialogFactory.getDialogError(this, getString(R.string.activity_google_drive_not_valid_file));
                errorLoadFile.show();

                FileUtils.deleteFileOnExist(filePath);
                return;
            }

            startCheckCastConnection(fileData);
        } else {
            SweetAlertDialog errorLoadFile = DialogFactory.getDialogError(this, getString(R.string.activity_google_drive_not_valid_file));
            errorLoadFile.show();

            FileUtils.deleteFileOnExist(filePath);
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
            intent = new Intent(GoogleDriveActivity.this, PlayerActivity.class);
        } else {
            intent = new Intent(GoogleDriveActivity.this, ViewerActivity.class);
        }
        intent.putExtra(EXTRA_FILE_NAME, fileData.getDisplayName());
        intent.putExtra(EXTRA_FILE_PATH, fileData.getFilePath());
        startActivity(intent);

        getViewModel().saveHistory(fileData);
    }

    private void startChromeCastConnection(FileData fileData) {
        mSelectedMedia = fileData;

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
        if (requestCode == TAKE_FILE_FROM_GOOGLE_DRIVE_REQUEST) {
            mIsOpening = false;

            if (data == null) return;
            Uri uri = data.getData();
            if (uri == null) return;

            if (RealPathUtil.getInstance().isGoogleDriveFile(uri)) {
                startDownloadFromCloudServer(uri);
            }
        } else if (requestCode == START_MEDIA_SERVICE) {
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

                        Intent intent = new Intent(GoogleDriveActivity.this, ExpandedControlsActivity.class);
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
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        reloadData();

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

