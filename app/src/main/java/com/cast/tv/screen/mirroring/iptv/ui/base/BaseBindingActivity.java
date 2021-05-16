package com.cast.tv.screen.mirroring.iptv.ui.base;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.IntegerRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

import com.ads.control.Admod;
import com.ads.control.funtion.AdCallback;
import com.cast.tv.screen.mirroring.iptv.constants.AppConstants;
import com.cast.tv.screen.mirroring.iptv.constants.DataConstants;
import com.cast.tv.screen.mirroring.iptv.ui.audio.AudioActivity;
import com.cast.tv.screen.mirroring.iptv.ui.bookmark.BookmarkActivity;
import com.cast.tv.screen.mirroring.iptv.ui.gallery.GalleryActivity;
import com.cast.tv.screen.mirroring.iptv.ui.googledrive.GoogleDriveActivity;
import com.cast.tv.screen.mirroring.iptv.ui.googlephoto.GooglePhotoActivity;
import com.cast.tv.screen.mirroring.iptv.ui.history.HistoryActivity;
import com.cast.tv.screen.mirroring.iptv.ui.iptv.category.CategoryActivity;
import com.cast.tv.screen.mirroring.iptv.ui.photo.PhotoActivity;
import com.cast.tv.screen.mirroring.iptv.ui.screencast.errorscreen.ErrorScreenActivity;
import com.cast.tv.screen.mirroring.iptv.ui.screencast.preparescreen.PrepareScreenActivity;
import com.cast.tv.screen.mirroring.iptv.ui.screencast.selectscreen.SelectScreenActivity;
import com.cast.tv.screen.mirroring.iptv.ui.video.VideoActivity;
import com.cast.tv.screen.mirroring.iptv.ui.weblink.WebLinkActivity;
import com.cast.tv.screen.mirroring.iptv.utils.ColorUtils;
import com.cast.tv.screen.mirroring.iptv.utils.FirebaseUtils;
import com.cast.tv.screen.mirroring.iptv.utils.ServiceUtils;
import com.cast.tv.screen.mirroring.iptv.utils.ToastUtils;
import com.cast.tv.screen.mirroring.iptv.utils.chromecast.ChromecastConnection;
import com.cast.tv.screen.mirroring.iptv.utils.chromecast.IpUtils;
import com.cast.tv.screen.mirroring.iptv.utils.chromecast.LocalFileStreamingServer;
import com.cast.tv.screen.mirroring.iptv.utils.chromecast.MediaWebService;
import com.cast.tv.screen.mirroring.iptv.utils.chromecast.webserver.SimpleWebServer;
import com.cast.tv.screen.mirroring.iptv.utils.file.FileUtils;
import com.cast.tv.screen.mirroring.iptv.utils.video.VideoUtils;
import com.google.android.gms.ads.InterstitialAd;
import com.cast.tv.screen.mirroring.iptv.BuildConfig;
import com.cast.tv.screen.mirroring.iptv.R;
import com.cast.tv.screen.mirroring.iptv.data.DataManager;
import com.cast.tv.screen.mirroring.iptv.ui.splash.SplashActivity;
import com.cast.tv.screen.mirroring.iptv.utils.DateTimeUtils;
import com.cast.tv.screen.mirroring.iptv.utils.DialogFactory;
import com.cast.tv.screen.mirroring.iptv.utils.NetworkUtils;
import com.cast.tv.screen.mirroring.iptv.utils.ads.AdsShowCountManager;
import com.cast.tv.screen.mirroring.iptv.utils.file.DirectoryUtils;
import com.rate.control.OnCallback;
import com.rate.control.funtion.RateUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import cn.pedant.SweetAlert.SweetAlertDialog;

public abstract class BaseBindingActivity<T extends ViewDataBinding, V extends BaseViewModel>
        extends AppCompatActivity implements BaseFragment.Callback {

    protected static final String EXTRA_FILE_PATH = "EXTRA_FILE_PATH";
    protected static final String EXTRA_FILE_NAME = "EXTRA_FILE_NAME";
    protected static final String EXTRA_FILE_SEND_WITH = "EXTRA_FILE_SEND_WITH";
    protected static final int TAKE_FILE_FROM_GOOGLE_DRIVE_REQUEST = 2365;
    protected static final int TAKE_FILE_FROM_GOOGLE_PHOTO_REQUEST = 2366;
    protected static final int OPEN_ACTIVITY_RATE_REQUEST = 2367;
    public static final int START_MEDIA_SERVICE = 2367;
    public static final int DURATION_FOR_ERROR_PLAYER = 1500;

    protected double mStartActivityTime = 0;

    protected boolean isNeedToSetTheme = true;

    private static final String TAG = "BaseBindingActivity";
    private V mViewModel;
    private T mViewDataBinding;

    private SweetAlertDialog mDownloadFromGgDriveDialog;

    protected InterstitialAd mScreenMirroringInterstitialAd;
    protected InterstitialAd mClickItemLocalInterstitialAd;
    public InterstitialAd mPreparingInterstitialAd;
    protected InterstitialAd mGooglePhotoInterstitialAd;
    protected InterstitialAd mGoogleDriveInterstitialAd;
    protected InterstitialAd mCateIptvInterstitialAd;

    protected ChromecastConnection.CastStateUpdateListener mDefaultCastStateListener;
    protected SweetAlertDialog mPreparingConnectionDialog;
    protected ChromecastConnection.RequestSessionCallback mDefaultRequestSessionCallback = new ChromecastConnection.RequestSessionCallback() {
        @Override
        public void onError(int errorCode) {
            ToastUtils.showMessageLong(getApplicationContext(), getString(R.string.cast_start_casting_error));
        }

        @Override
        public void onCancel() {
            ToastUtils.showMessageLong(getApplicationContext(), getString(R.string.cast_start_casting_error));
        }

        @Override
        public void onDialogShow() {
            hidePrepareConnectionDialog();
        }

        @Override
        public void onDialogCanNotShow() {
            hidePrepareConnectionDialog();
            ToastUtils.showMessageLong(getApplicationContext(), getString(R.string.cast_start_casting_error));
        }

        @Override
        public void onJoinedSuccess() {
            ToastUtils.showMessageLong(getApplicationContext(), getString(R.string.cast_start_casting_success));
        }
    };

    protected SweetAlertDialog mPrepareServerDialog;
    protected LocalFileStreamingServer mServer;

    /**
     * Override for set binding variable
     *
     * @return variable id
     */
    public abstract int getBindingVariable();

    /**
     * @return layout resource id
     */
    public abstract
    @LayoutRes
    int getLayoutId();

    /**
     * Override for set view model
     *
     * @return view model instance
     */
    public abstract V getViewModel();

    @Override
    public void onFragmentAttached() {
    }

    @SuppressLint({"SourceLockedOrientationActivity", "UseCompatLoadingForDrawables"})
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setNoActionBar();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        performDataBinding();
        mStartActivityTime = 0;

        mDefaultCastStateListener = new ChromecastConnection.CastStateUpdateListener() {
            @Override
            public void onReceiverAvailableUpdate(boolean available) {

            }
        };
        mDefaultCastStateListener.setCastDrawable(getDrawable(R.drawable.ic_cast_connected),
                getDrawable(R.drawable.ic_cast), getDrawable(R.drawable.ic_cast_not_available));
    }

    private void cleanTempFolder() {
        VideoUtils.deleteVideoTempFolder(this);
    }

    @Override
    protected void onDestroy() {
        if (null != mServer && mServer.isRunning()) {
            mServer.stop();
            mServer = null;
        }

//        cleanTempFolder();

        if (SimpleWebServer.isServerRunning()) {
            SimpleWebServer.stopServer();
        }

        super.onDestroy();
    }

    protected void showPrepareConnectionDialog() {
        try {
            if (mPreparingConnectionDialog == null) {
                mPreparingConnectionDialog = DialogFactory.getDialogProgress(this, getString(R.string.prepare_connection));
            }

            mPreparingConnectionDialog.show();
        } catch (Exception ignored) {}
    }

    protected void hidePrepareConnectionDialog() {
        try {
            if (mPreparingConnectionDialog != null && mPreparingConnectionDialog.isShowing()) {
                mPreparingConnectionDialog.dismiss();
            }
        } catch (Exception ignored) {}
    }

    protected void hidePrepareServerDialog() {
        try {
            if (mPrepareServerDialog != null && mPrepareServerDialog.isShowing()) {
                mPrepareServerDialog.dismiss();
            }
        } catch (Exception ignored) {}
    }

    protected void requestStartSessionWithCallback(ChromecastConnection chromecastConnection, Runnable successCallback, Runnable failCallback) {
        showPrepareConnectionDialog();
        chromecastConnection.requestStartSession(new ChromecastConnection.RequestSessionCallback() {
            @Override
            public void onError(int errorCode) {
                ToastUtils.showMessageLong(getApplicationContext(), getString(R.string.cast_start_casting_error));
                failCallback.run();
            }

            @Override
            public void onCancel() {
                ToastUtils.showMessageLong(getApplicationContext(), getString(R.string.cast_start_casting_error));
            }

            @Override
            public void onDialogShow() {
                hidePrepareConnectionDialog();
            }

            @Override
            public void onDialogCanNotShow() {
                hidePrepareConnectionDialog();
                ToastUtils.showMessageLong(getApplicationContext(), getString(R.string.cast_start_casting_error));
            }

            @Override
            public void onJoinedSuccess() {
                successCallback.run();
            }
        });
    }

    protected void prepareLocalServer(Runnable successCallback, String filePath) {
        mPrepareServerDialog = DialogFactory.getDialogProgress(this, getString(R.string.cast_start_casting_message));
        mPrepareServerDialog.show();

        String deviceIpAddress = IpUtils.Companion.findIPAddress(getApplicationContext());

        if (deviceIpAddress == null) {
            ToastUtils.showMessageLong(this, getString(R.string.cast_start_casting_error));
            hidePrepareServerDialog();
            return;
        } 

        if (mServer != null && mServer.isRunning()) {
            mServer.stop();
            mServer = null;
        }

        mServer = new LocalFileStreamingServer(new File(filePath));
        String streamUrl = mServer.init(deviceIpAddress);

        if (streamUrl == null) {
            ToastUtils.showMessageLong(this, getString(R.string.cast_start_casting_error));
            hidePrepareServerDialog();
            return;
        }

        mServer.start();

        if (mPrepareServerDialog != null && mPrepareServerDialog.isShowing()) {
            mPrepareServerDialog.dismiss();
        }
        successCallback.run();
    }

    protected void prepareSimpleWebServer(Runnable primaryCallback, Runnable successCallback) {
        mPrepareServerDialog = DialogFactory.getDialogProgress(this, getString(R.string.cast_start_casting_message));
        mPrepareServerDialog.show();

        String deviceIpAddress = IpUtils.Companion.findIPAddress(getApplicationContext());

        if (deviceIpAddress == null) {
            ToastUtils.showMessageLong(this, getString(R.string.cast_start_casting_error));
            hidePrepareServerDialog();
            return;
        }

        if (ServiceUtils.isMyServiceRunning(MediaWebService.class, this) && deviceIpAddress.equals(DataManager.getInstance(this).getLastIPAddress())) {
            primaryCallback.run();
            successCallback.run();
            hidePrepareServerDialog();
        } else {
            primaryCallback.run();
            PendingIntent pendingResult = createPendingResult(START_MEDIA_SERVICE, new Intent(), 0);
            Intent serviceIntent = new Intent(BaseBindingActivity.this, MediaWebService.class);
            Bundle extras = new Bundle();
            extras.putString(DataConstants.IP_LINK_KEY, deviceIpAddress);
            serviceIntent.putExtra(DataConstants.PENDING_INTENT_SERVICE, pendingResult);
            serviceIntent.putExtras(extras);

            startService(serviceIntent);
        }
    }

    // Ads

    protected void preloadScreenMirroringAdsIfInit() {
        mScreenMirroringInterstitialAd = Admod.getInstance().getInterstitalAds(this, BuildConfig.interstitial_screen_mirroring_id);
    }

    protected void preloadClickItemLocalAdsIfInit() {
        mClickItemLocalInterstitialAd = Admod.getInstance().getInterstitalAds(this, BuildConfig.interstitial_click_item_local_data_id);
    }

    protected void preloadPreparingAdsIfInit() {
        mPreparingInterstitialAd = Admod.getInstance().getInterstitalAds(this, BuildConfig.interstitial_preparing_id);
    }

    protected void preloadGooglePhotoAdsIfInit() {
        mGooglePhotoInterstitialAd = Admod.getInstance().getInterstitalAds(this, BuildConfig.interstitial_google_photo_id);
    }

    protected void preloadGoogleDriveAdsIfInit() {
        mGoogleDriveInterstitialAd = Admod.getInstance().getInterstitalAds(this, BuildConfig.interstitial_google_drive_id);
    }

    protected void preloadCateIptvAdsIfInit() {
        mCateIptvInterstitialAd = Admod.getInstance().getInterstitalAds(this, BuildConfig.interstitial_cate_iptv_id);
    }


    public void showAdsBeforeAction(InterstitialAd interstitialAd, Runnable callback) {
        Admod.getInstance().forceShowInterstitial(this, interstitialAd, new AdCallback() {
            @Override
            public void onAdClosed() {
                callback.run();
            }
        });
    }

    public void showOnePerTwoAdsBeforeAction(InterstitialAd interstitialAd, Runnable callback) {
        if (AdsShowCountManager.getInstance(this).checkShowAdsForClickItem()) {
            Admod.getInstance().forceShowInterstitial(this, interstitialAd, new AdCallback() {
                @Override
                public void onAdClosed() {
                    callback.run();
                    AdsShowCountManager.getInstance(getApplicationContext()).increaseCountForClickItem();
                }
            });
        } else {
            AdsShowCountManager.getInstance(this).increaseCountForClickItem();
            callback.run();
        }
    }

    /**
     * create view component
     */
    protected abstract void initView();

    /**
     * set on-click listener for view component
     */
    protected abstract void setClick();

    public T getViewDataBinding() {
        return mViewDataBinding;
    }

    private void performDataBinding() {
        mViewDataBinding = DataBindingUtil.setContentView(this, getLayoutId());
        this.mViewModel = mViewModel == null ? getViewModel() : mViewModel;
        mViewDataBinding.setLifecycleOwner(this);
        mViewDataBinding.setVariable(getBindingVariable(), mViewModel);
        mViewDataBinding.executePendingBindings();
    }

    public boolean hasPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    public void requestPermissionsSafely(String[] permissions, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, requestCode);
        }

    }

    @SuppressLint("RestrictedApi")
    public void setNoActionBar() {
        if (getSupportActionBar() != null) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setShowHideAnimationEnabled(false);

            actionBar.hide();
            actionBar.setDisplayShowTitleEnabled(false);

            Window window = getWindow();

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getColorByResource(R.color.black_totally));
            window.setNavigationBarColor(getColorByResource(R.color.black_totally));
        }
    }

    @SuppressLint("RestrictedApi")
    public void setActionBar(String title, boolean isShowBackButton) {
        if (getSupportActionBar() != null) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setShowHideAnimationEnabled(false);

            actionBar.show();
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(title);

            actionBar.setHomeButtonEnabled(isShowBackButton);
            actionBar.setDisplayHomeAsUpEnabled(isShowBackButton);
        }
    }

    protected void hideActivityName() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    protected void setBlackActionBar() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getColorByResource(R.color.black_totally));
    }

    public int getColorByResource(@ColorRes int color) {
        return ColorUtils.getColorFromResource(this, color);
    }

    public void restartApp() {
        Intent intent = new Intent(BaseBindingActivity.this, SplashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        finish();
    }

    public boolean isNetworkConnected() {
        return NetworkUtils.isNetworkConnected(getApplicationContext());
    }

    public int getIntegerByResource(@IntegerRes int integer) {
        return getResources().getInteger(integer);
    }

    /**
     * for set full screen without action bar and navigation bar()
     */
    protected void setActivityFullScreen() {

    }

    protected void setActivityWithActionBar() {

    }

    /**
     * Show popup download from cloud server
     */

    protected void updateFilePathFromCloudServer(Uri uri, String filePath) {
        if (mDownloadFromGgDriveDialog != null) {
            mDownloadFromGgDriveDialog.dismiss();
        }
    }

    protected void startDownloadFromCloudServer(Uri uri) {
        String pathFile = uri.getPath();
        if (FileUtils.checkFileExist(pathFile)) {
            updateFilePathFromCloudServer(uri, pathFile);
            return;
        }

        pathFile = getPathFile(getContentResolver(), uri);
        if (FileUtils.checkFileExist(pathFile)) {
            updateFilePathFromCloudServer(uri, pathFile);
            return;
        }

        if (!NetworkUtils.isNetworkConnected(this)) {
            ToastUtils.showNoNetworkToast(this);
            return;
        }

        mDownloadFromGgDriveDialog = DialogFactory.getDialogProgress(this, getString(R.string.downloading_from_gg_drive_text));
        mDownloadFromGgDriveDialog.show();

        AsyncTask.execute(() -> {
            try {
                @SuppressLint("Recycle")
                Cursor returnCursor = getContentResolver().query(uri, null, null, null, null);

                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                returnCursor.moveToFirst();
                String originalName = (returnCursor.getString(nameIndex));

                if (originalName == null || !FileUtils.checkSupportedFile(originalName)) {
                    runOnUiThread(() -> {
                        updateFilePathFromCloudServer(uri, null);
                    });
                    return;
                }

                File file = new File(DirectoryUtils.getDefaultStorageLocation(), originalName);
                InputStream inputStream = getContentResolver().openInputStream(uri);
                FileOutputStream outputStream = new FileOutputStream(file);
                int read = 0;
                int maxBufferSize = 1024 * 1024;
                int bytesAvailable = inputStream.available();

                int bufferSize = Math.min(bytesAvailable, maxBufferSize);

                final byte[] buffers = new byte[bufferSize];
                while ((read = inputStream.read(buffers)) != -1) {
                    outputStream.write(buffers, 0, read);
                }
                inputStream.close();
                outputStream.close();

                runOnUiThread(() -> {
                    updateFilePathFromCloudServer(uri, file.getPath());
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    updateFilePathFromCloudServer(uri, null);
                });
            }
        });
    }

    private String getPathFile(final ContentResolver cr, final Uri uri) {
        try {
            @SuppressLint("Recycle")
            final Cursor c = cr.query(uri, null, null, null, null);
            if (c != null) {
                c.moveToFirst();
                final int fileNameColumnId = c.getColumnIndex(MediaStore.MediaColumns.DATA);
                if (fileNameColumnId >= 0) {
                    final String attachmentFileName = c.getString(fileNameColumnId);
                    return attachmentFileName == null || attachmentFileName.length() == 0 ? null : attachmentFileName;
                }
            }

        } catch (Exception ignored) {}
        return null;
    }

    protected void updateFilePathFromCloudServerList(int index, ArrayList<Uri> uriList, String filePath) {
        if (mDownloadFromGgDriveDialog != null) {
            if (index == uriList.size() - 1)
                mDownloadFromGgDriveDialog.dismiss();
        }
    }

    protected void startDownloadFromCloudServerList(ArrayList<Uri> uriList) {
        runOnUiThread(() -> {
            mDownloadFromGgDriveDialog = DialogFactory.getDialogProgress(this, getString(R.string.downloading_from_gg_drive_text));
            mDownloadFromGgDriveDialog.show();
        });

        for (int i = 0; i < uriList.size(); i++) {
            int finalIndex = i;
            Uri uri = uriList.get(i);
            if (uri == null) {
                runOnUiThread(() -> {
                    updateFilePathFromCloudServerList(finalIndex, uriList, null);
                });
                return;
            }

            try {
                @SuppressLint("Recycle")
                Cursor returnCursor = getContentResolver().query(uri, null, null, null, null);

                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                returnCursor.moveToFirst();
                String originalName = (returnCursor.getString(nameIndex));
                String size = (Long.toString(returnCursor.getLong(sizeIndex)));

                if (originalName == null) {
                    originalName = getString(R.string.prefix_for_google_drive) + DateTimeUtils.currentTimeToNaming();
                }

                File file = new File(DirectoryUtils.getDefaultStorageLocation(), originalName);
                InputStream inputStream = getContentResolver().openInputStream(uri);
                FileOutputStream outputStream = new FileOutputStream(file);
                int read = 0;
                int maxBufferSize = 1024 * 1024;
                int bytesAvailable = inputStream.available();

                int bufferSize = Math.min(bytesAvailable, maxBufferSize);

                final byte[] buffers = new byte[bufferSize];
                while ((read = inputStream.read(buffers)) != -1) {
                    outputStream.write(buffers, 0, read);
                }
                inputStream.close();
                outputStream.close();

                runOnUiThread(() -> {
                    updateFilePathFromCloudServerList(finalIndex, uriList, file.getPath());
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    updateFilePathFromCloudServerList(finalIndex, uriList, null);
                });
            }
        }
    }

    /**
     * for activity move - all activity moving should put here - ads coverage
     */

    public void gotoActivityFromNavigationWithFlag(int flag) {
        Intent intent;

        switch (flag) {
            case AppConstants.FLAG_START_HOME_ACTIVITY:
                // donothing
                break;
            case AppConstants.FLAG_START_SHARE_ACTIVITY:
                shareApplicationLink();
                break;
            case AppConstants.FLAG_START_CONTACT_ACTIVITY:
                feedBackApplication();
                break;
            case AppConstants.FLAG_START_RATE_ACTIVITY:
                DataManager dataManager = DataManager.getInstance(getApplicationContext());

                if (!dataManager.checkRatingUsDone()) {
                    showRatingUsPopup(() -> {});
                } else {
                    ToastUtils.showMessageLong(this, "You have already rated this app. Thank you!");
                }
                break;
            case AppConstants.FLAG_START_UPGRADE_ACTIVITY:
                FirebaseUtils.sendEventFunctionUsed(this, FirebaseUtils.LEFT_MENU_EVENT, "Click Upgrade to Premium");
                ToastUtils.showFunctionNotSupportToast(this);
                break;
        }
    }

    public void gotoActivityWithFlag(int flag) {
        Intent intent;

        switch (flag) {
            case AppConstants.FLAG_START_PHOTO_CAST:
                FirebaseUtils.sendEventFunctionUsed(this, FirebaseUtils.HOME_EVENT, "Click Item", "Photo");

                intent = new Intent(BaseBindingActivity.this, PhotoActivity.class);
                startActivityForResult(intent, OPEN_ACTIVITY_RATE_REQUEST);
                break;

            case AppConstants.FLAG_START_VIDEO_CAST:
                FirebaseUtils.sendEventFunctionUsed(this, FirebaseUtils.HOME_EVENT, "Click Item", "Video");

                intent = new Intent(BaseBindingActivity.this, VideoActivity.class);
                startActivityForResult(intent, OPEN_ACTIVITY_RATE_REQUEST);
                break;

            case AppConstants.FLAG_START_AUDIO_CAST:
                FirebaseUtils.sendEventFunctionUsed(this, FirebaseUtils.HOME_EVENT, "Click Item", "Audio");

                intent = new Intent(BaseBindingActivity.this, AudioActivity.class);
                startActivityForResult(intent, OPEN_ACTIVITY_RATE_REQUEST);
                break;

            case AppConstants.FLAG_START_GOOGLE_DRIVE_CAST:
                FirebaseUtils.sendEventFunctionUsed(this, FirebaseUtils.HOME_EVENT, "Click Item", "Google Drive");

                intent = new Intent(BaseBindingActivity.this, GoogleDriveActivity.class);
                startActivityForResult(intent, OPEN_ACTIVITY_RATE_REQUEST);
                break;

            case AppConstants.FLAG_START_GOOGLE_PHOTO_CAST:
                FirebaseUtils.sendEventFunctionUsed(this, FirebaseUtils.HOME_EVENT, "Click Item", "Google Photo");

                intent = new Intent(BaseBindingActivity.this, GooglePhotoActivity.class);
                startActivityForResult(intent, OPEN_ACTIVITY_RATE_REQUEST);
                break;

            case AppConstants.FLAG_START_SCREEN_CAST:
                FirebaseUtils.sendEventFunctionUsed(this, FirebaseUtils.HOME_EVENT, "Click Item", "Screen mirror");

                intent = new Intent(BaseBindingActivity.this, SelectScreenActivity.class);
                startActivityForResult(intent, OPEN_ACTIVITY_RATE_REQUEST);
                break;

            case AppConstants.FLAG_START_PREPARE_SCREEN_CAST:

                intent = new Intent(BaseBindingActivity.this, PrepareScreenActivity.class);
                startActivity(intent);
                break;

            case AppConstants.FLAG_START_ERROR_SCREEN:
                intent = new Intent(BaseBindingActivity.this, ErrorScreenActivity.class);
                startActivity(intent);
                break;

            case AppConstants.FLAG_START_GALLERY_CAST:
                FirebaseUtils.sendEventFunctionUsed(this, FirebaseUtils.HOME_EVENT, "Click Item", "Gallery");

                intent = new Intent(BaseBindingActivity.this, GalleryActivity.class);
                startActivityForResult(intent, OPEN_ACTIVITY_RATE_REQUEST);
                break;

            case AppConstants.FLAG_START_BOOKMARK:
                FirebaseUtils.sendEventFunctionUsed(this, FirebaseUtils.HOME_EVENT, "Click Item", "Bookmark");

                intent = new Intent(BaseBindingActivity.this, BookmarkActivity.class);
                startActivityForResult(intent, OPEN_ACTIVITY_RATE_REQUEST);
                break;

            case AppConstants.FLAG_START_HISTORY:
                FirebaseUtils.sendEventFunctionUsed(this, FirebaseUtils.HOME_EVENT, "Click Item", "History");

                intent = new Intent(BaseBindingActivity.this, HistoryActivity.class);
                startActivityForResult(intent, OPEN_ACTIVITY_RATE_REQUEST);
                break;

            case AppConstants.FLAG_START_WEB_LINK_CAST:
                FirebaseUtils.sendEventFunctionUsed(this, FirebaseUtils.HOME_EVENT, "Click Item", "Weblink");

                intent = new Intent(BaseBindingActivity.this, WebLinkActivity.class);
                startActivityForResult(intent, OPEN_ACTIVITY_RATE_REQUEST);
                break;

            case AppConstants.FLAG_START_IPTV:
                FirebaseUtils.sendEventFunctionUsed(this, FirebaseUtils.HOME_EVENT, "Click Item", "IPTV");

                intent = new Intent(BaseBindingActivity.this, CategoryActivity.class);
                startActivityForResult(intent, OPEN_ACTIVITY_RATE_REQUEST);
                break;
        }

        mStartActivityTime = System.currentTimeMillis();
    }

    /**
     * Request rating
     */

    public void requestForRating(final Runnable rejectRunnable) {
        DataManager dataManager = DataManager.getInstance(getApplicationContext());

        if (!dataManager.checkRatingUsDone()) {
            showRatingUsPopup(rejectRunnable);
        }
    }

    @SuppressLint("ResourceAsColor")
    public void showRatingUsPopup(final Runnable rejectRunnable) {
        RateUtils.showRateDialog(this, new OnCallback() {
            @Override
            public void onMaybeLater() {
                rejectRunnable.run();
            }

            @Override
            public void onSubmit(String review) {
                FirebaseUtils.sendEventFunctionUsed(BaseBindingActivity.this, "User feedback", review);
                ToastUtils.showMessageShort(BaseBindingActivity.this, getString(R.string.thank_you_for_rate_us));
                DataManager dataManager = DataManager.getInstance(getApplicationContext());
                dataManager.setRatingUsDone();
                rejectRunnable.run();
            }

            @Override
            public void onRate() {
                gotoRateUsActivity();
                rejectRunnable.run();
            }
        });
    }

    public void gotoRateUsActivity() {
        DataManager dataManager = DataManager.getInstance(this);
        dataManager.setRatingUsDone();

        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);

        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
        }
    }

    private void shareApplicationLink() {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            String shareMessage= "\nLet me recommend you this cast screen application\n\n";
            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + getPackageName() +"\n\n";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "Choose one"));
        } catch(Exception e) {
            ToastUtils.showMessageShort(this, getString(R.string.can_not_action_option_now));
        }
    }

    private void feedBackApplication() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{DataConstants.EMAIL_DEV});
        startActivity(intent);
    }
}
