package com.cast.tv.screen.mirroring.iptv.ui.iptv.channel;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;

import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.cast.tv.screen.mirroring.iptv.BuildConfig;
import com.cast.tv.screen.mirroring.iptv.R;
import com.cast.tv.screen.mirroring.iptv.constants.AppConstants;
import com.cast.tv.screen.mirroring.iptv.constants.DataConstants;
import com.cast.tv.screen.mirroring.iptv.databinding.ActivityIptvChannelBinding;
import com.cast.tv.screen.mirroring.iptv.listener.OnListItemClickListener;
import com.cast.tv.screen.mirroring.iptv.ui.base.BaseBindingActivity;
import com.cast.tv.screen.mirroring.iptv.ui.player.PlayerActivity;
import com.cast.tv.screen.mirroring.iptv.utils.FirebaseUtils;
import com.cast.tv.screen.mirroring.iptv.utils.ToastUtils;
import com.cast.tv.screen.mirroring.iptv.utils.adapter.ChannelListAdapter;
import com.cast.tv.screen.mirroring.iptv.utils.chromecast.ChromecastConnection;
import com.cast.tv.screen.mirroring.iptv.utils.chromecast.ExpandedControlsActivity;
import com.cast.tv.screen.mirroring.iptv.utils.file.FileUtils;
import com.cast.tv.screen.mirroring.iptv.utils.iptv.ChannelItem;
import com.cast.tv.screen.mirroring.iptv.utils.iptv.ChannelList;
import com.cast.tv.screen.mirroring.iptv.utils.iptv.IptvParser;
import com.cast.tv.screen.mirroring.iptv.utils.miracast.MiracastUtils;
import com.google.android.gms.cast.MediaError;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaLoadRequestData;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.images.WebImage;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ChannelActivity extends BaseBindingActivity<ActivityIptvChannelBinding, ChannelViewModel> implements ChannelNavigator, OnListItemClickListener {

    private ChannelViewModel mChannelViewModel;
    private ActivityIptvChannelBinding mActivityIptvChannelBinding;
    private ChannelList mChannelList;
    private ChannelListAdapter mChannelListAdapter;

    private String mCategoryName;
    private String mCategoryLink;

    private ChannelItem mSelectedMedia;
    private int mSelectedPosition = -1;
    private boolean mIsJumpingToMiraCast = false;

    private ChromecastConnection mChromecastConnection;

    @Override
    public int getBindingVariable() {
        return 0;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_iptv_channel;
    }

    @Override
    public ChannelViewModel getViewModel() {
        mChannelViewModel = ViewModelProviders.of(this).get(ChannelViewModel.class);
        return mChannelViewModel;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mChannelViewModel.setNavigator(this);
        mActivityIptvChannelBinding = getViewDataBinding();
        mCategoryName = getIntent().getStringExtra(EXTRA_FILE_NAME);
        if (mCategoryName == null || mCategoryName.length() == 0) {
            mCategoryName = DataConstants.IPTV_CATEGORY_NAME_LIST.get(0);
        }

        mCategoryLink = getString(R.string.activity_iptv_download_link, mCategoryName.toLowerCase());
        mActivityIptvChannelBinding.nameCategory.setText(mCategoryName);

        initView();
    }

    @Override
    protected void initView() {
        setNoActionBar();
        setForLiveData();
        setForPullRefresh();
        setForClick();
        setupChromecastConnection();

        mChannelListAdapter = new ChannelListAdapter(this);
        mActivityIptvChannelBinding.dataListArea.setLayoutManager(new LinearLayoutManager(this));
        mActivityIptvChannelBinding.dataListArea.setAdapter(mChannelListAdapter);

        reloadData(true);

    }

    @Override
    protected void setClick() {}

    @Override
    public void onFragmentDetached(String tag) {}

    private void setForLiveData() {

    }

    private void setForPullRefresh() {
        mActivityIptvChannelBinding.pullToRefresh.setOnRefreshListener(() -> {
            reloadData(false);
        });
    }

    private void setForClick() {
        mActivityIptvChannelBinding.backImg.setOnClickListener(view -> onBackPressed());
    }

    private void updateListData(ChannelList channelList) {
        mChannelList = channelList;

        if (mChannelList != null && mChannelList.items != null && mChannelList.items.size() > 0) {
            mChannelList.items.add(0, new ChannelItem());
            Parcelable oldPosition = null;
            if (mActivityIptvChannelBinding.dataListArea.getLayoutManager() != null) {
                oldPosition = mActivityIptvChannelBinding.dataListArea.getLayoutManager().onSaveInstanceState();
            }
            mChannelListAdapter.setData(mChannelList);
            if (oldPosition != null) {
                mActivityIptvChannelBinding.dataListArea.getLayoutManager().onRestoreInstanceState(oldPosition);
            }
            showDataArea();
        } else {
            showNoDataArea();
        }

        mActivityIptvChannelBinding.pullToRefresh.setRefreshing(false);
    }

    private void reloadData(boolean isForceReload) {
        if (mChannelList == null || mChannelList.items == null || mChannelList.items.size() == 0 || isForceReload) {
            showLoadingArea();
        }

        try {
            OkHttpClient okHttpClient = new OkHttpClient();

            Request request = new Request.Builder().url(mCategoryLink).build();
            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        showNoDataArea();
                        mActivityIptvChannelBinding.pullToRefresh.setRefreshing(false);
                    });
                }

                @Override
                public void onResponse(Call call, Response response) {
                    try {
                        InputStream inputStream = null;
                        if (response.body() != null) {
                            inputStream = response.body().byteStream();
                        }

                        ChannelList output = IptvParser.parse(inputStream);

                        runOnUiThread(() -> {
                            updateListData(output);
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            updateListData(new ChannelList());
                        });
                    }

                }
            });
        } catch (Exception e) {
            showNoDataArea();
        }
    }

    private void showDataArea() {
        mActivityIptvChannelBinding.dataListArea.setVisibility(View.VISIBLE);
        mActivityIptvChannelBinding.loadingArea.setVisibility(View.GONE);
        mActivityIptvChannelBinding.noDataErrorArea.setVisibility(View.GONE);
    }

    private void showLoadingArea() {
        mActivityIptvChannelBinding.dataListArea.setVisibility(View.GONE);
        mActivityIptvChannelBinding.loadingArea.setVisibility(View.VISIBLE);
        mActivityIptvChannelBinding.noDataErrorArea.setVisibility(View.GONE);
    }

    private void showNoDataArea() {
        mActivityIptvChannelBinding.dataListArea.setVisibility(View.GONE);
        mActivityIptvChannelBinding.noDataErrorArea.setVisibility(View.VISIBLE);
        mActivityIptvChannelBinding.loadingArea.setVisibility(View.GONE);
    }

    private void setupChromecastConnection() {
        mDefaultCastStateListener.setCastIcon(mActivityIptvChannelBinding.castImg);
        mChromecastConnection = new ChromecastConnection(this, mDefaultCastStateListener);
        mChromecastConnection.initialize(AppConstants.CAST_APPLICATION_ID);

        mActivityIptvChannelBinding.castImg.setOnClickListener(view -> {
            FirebaseUtils.sendEventFunctionUsed(this, FirebaseUtils.CAST_BUTTON_EVENT, "Click cast button", "IPTV channel layout");

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

    @Override
    public void onClickItem(int position) {
        startCheckCastConnection(position);
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

    private void startCheckCastConnection(int position) {
        if (MiracastUtils.isMiraCastConnect(this)) {
            startPlayerConnection(mChannelList.items.get(position));
        } else {
            final String[] fonts = {
                    getString(R.string.play_by_chrome_cast), getString(R.string.play_by_mirror_cast)
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.play_select_type));
            builder.setItems(fonts, (dialog, which) -> {
                if (getString(R.string.play_by_chrome_cast).equals(fonts[which])) {
                    startChromeCastConnection(position);
                } else if (getString(R.string.play_by_mirror_cast).equals(fonts[which])) {
                    startMiracastConnection(mChannelList.items.get(position));
                }
            });
            builder.show();
        }
    }

    private void startMiracastConnection(ChannelItem channelItem) {
        mIsJumpingToMiraCast = true;
        mSelectedMedia = channelItem;
        gotoActivityWithFlag(AppConstants.FLAG_START_SCREEN_CAST);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mIsJumpingToMiraCast) {
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


    private void startPlayerConnection(ChannelItem channelItem) {
        if (channelItem == null) {
            ToastUtils.showMessageLong(this, getString(R.string.activity_player_can_not_connect_to_miracast));
            return;
        }

        Intent intent = new Intent(ChannelActivity.this, PlayerActivity.class);
        intent.putExtra(EXTRA_FILE_NAME, channelItem.name);
        intent.putExtra(EXTRA_FILE_PATH, channelItem.url);
        startActivity(intent);
    }

    private void startChromeCastConnection(int position) {
        if (mChromecastConnection.isChromeCastConnect()) {
            updateSelectedPosition(position);
            runOnUiThread(this::loadRemoteMedia);

        } else {
            requestStartSessionWithCallback(mChromecastConnection, () -> {updateSelectedPosition(position);runOnUiThread(this::loadRemoteMedia);}, () -> {});
        }
    }

    private void updateSelectedPosition(int position) {
        if (position >= 0 && position < mChannelList.items.size()) {
            mSelectedMedia = mChannelList.items.get(position);
            mSelectedPosition = position;

            mChannelListAdapter.setCurrentItem(position);
        } else {
            mSelectedPosition = -1;
            mSelectedMedia = null;

            mChannelListAdapter.setCurrentItem(-1);
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
                Intent intent = new Intent(ChannelActivity.this, ExpandedControlsActivity.class);
                startActivity(intent);
                remoteMediaClient.unregisterCallback(this);
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
                if (remoteMediaClient.isPlaying()) {
                    remoteMediaClient.stop();
                }
                remoteMediaClient.load(new MediaLoadRequestData.Builder()
                        .setMediaInfo(mediaInfo)
                        .setAutoplay(true).build());
            });
        }
    }

    private MediaInfo buildMediaInfo() {
        if (mSelectedMedia == null || mSelectedMedia.url == null) return null;

        MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);

        movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, mSelectedMedia.metadata.get("tvg-language") == null ? "IPTV Channel" : mSelectedMedia.metadata.get("tvg-language"));
        movieMetadata.putString(MediaMetadata.KEY_TITLE, mSelectedMedia.name);
        if (mSelectedMedia.metadata.get("tvg-logo") != null) {
            movieMetadata.addImage(new WebImage(Uri.parse(mSelectedMedia.metadata.get("tvg-logo"))));
        }

        String mediaLink = mSelectedMedia.url;

        return new MediaInfo.Builder(mediaLink)
                .setStreamType(MediaInfo.STREAM_TYPE_LIVE)
                .setContentType(FileUtils.getContentType(mSelectedMedia.url))
                .setStreamDuration(MediaInfo.UNKNOWN_DURATION)
                .setMetadata(movieMetadata)
                .setStreamDuration(mSelectedMedia.duration)
                .build();
    }
}


