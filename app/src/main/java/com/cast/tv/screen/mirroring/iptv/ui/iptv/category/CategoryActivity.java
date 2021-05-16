package com.cast.tv.screen.mirroring.iptv.ui.iptv.category;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;

import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.cast.tv.screen.mirroring.iptv.BuildConfig;
import com.cast.tv.screen.mirroring.iptv.R;
import com.cast.tv.screen.mirroring.iptv.constants.AppConstants;
import com.cast.tv.screen.mirroring.iptv.constants.DataConstants;
import com.cast.tv.screen.mirroring.iptv.databinding.ActivityIptvCategoryBinding;
import com.cast.tv.screen.mirroring.iptv.listener.OnListItemClickListener;
import com.cast.tv.screen.mirroring.iptv.ui.base.BaseBindingActivity;
import com.cast.tv.screen.mirroring.iptv.ui.iptv.channel.ChannelActivity;
import com.cast.tv.screen.mirroring.iptv.utils.FirebaseUtils;
import com.cast.tv.screen.mirroring.iptv.utils.ToastUtils;
import com.cast.tv.screen.mirroring.iptv.utils.adapter.DataListAdapter;
import com.cast.tv.screen.mirroring.iptv.utils.chromecast.ChromecastConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CategoryActivity extends BaseBindingActivity<ActivityIptvCategoryBinding, CategoryViewModel> implements CategoryNavigator, OnListItemClickListener {

    private CategoryViewModel mCategoryViewModel;
    private ActivityIptvCategoryBinding mActivityIptvCategoryBinding;
    private List<String> mListScreen = new ArrayList<>();
    private DataListAdapter mDataListAdapter;

    private ChromecastConnection mChromecastConnection;

    @Override
    public int getBindingVariable() {
        return 0;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_iptv_category;
    }

    @Override
    public CategoryViewModel getViewModel() {
        mCategoryViewModel = ViewModelProviders.of(this).get(CategoryViewModel.class);
        return mCategoryViewModel;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCategoryViewModel.setNavigator(this);
        mActivityIptvCategoryBinding = getViewDataBinding();
        initView();
    }

    @Override
    protected void initView() {
        setNoActionBar();
        setForLiveData();
        setForPullRefresh();
        setForClick();
        setupChromecastConnection();

        preloadCateIptvAdsIfInit();

        mDataListAdapter = new DataListAdapter(this);
        StaggeredGridLayoutManager mGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mActivityIptvCategoryBinding.dataListArea.setLayoutManager(mGridLayoutManager);
        mActivityIptvCategoryBinding.dataListArea.setAdapter(mDataListAdapter);

        reloadData(true);

    }

    @Override
    protected void setClick() {}

    @Override
    public void onFragmentDetached(String tag) {}

    private void setForLiveData() {

    }

    private void setForPullRefresh() {
        mActivityIptvCategoryBinding.pullToRefresh.setOnRefreshListener(() -> {
            reloadData(false);
        });
    }

    private void setForClick() {
        mActivityIptvCategoryBinding.backImg.setOnClickListener(view -> onBackPressed());
    }

    private void updateListData(List<String> fileDataList) {
        mListScreen = new ArrayList<>();
        mListScreen.addAll(fileDataList);
        mListScreen.add(0, "");

        if (mListScreen.size() > 0) {

            Parcelable oldPosition = null;
            if (mActivityIptvCategoryBinding.dataListArea.getLayoutManager() != null) {
                oldPosition = mActivityIptvCategoryBinding.dataListArea.getLayoutManager().onSaveInstanceState();
            }
            mDataListAdapter.setData(mListScreen);
            if (oldPosition != null) {
                mActivityIptvCategoryBinding.dataListArea.getLayoutManager().onRestoreInstanceState(oldPosition);
            }
            showDataArea();
        }

        mActivityIptvCategoryBinding.pullToRefresh.setRefreshing(false);
    }

    private void reloadData(boolean isForceReload) {
        if (mListScreen == null || mListScreen.size() == 0 || isForceReload) {
            showLoadingArea();
        }

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    updateListData(DataConstants.IPTV_CATEGORY_NAME_LIST);
                });
            }
        }, 1000);
    }

    private void showDataArea() {
        mActivityIptvCategoryBinding.dataListArea.setVisibility(View.VISIBLE);
        mActivityIptvCategoryBinding.loadingArea.setVisibility(View.GONE);
    }

    private void showLoadingArea() {
        mActivityIptvCategoryBinding.dataListArea.setVisibility(View.GONE);
        mActivityIptvCategoryBinding.loadingArea.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClickItem(int position) {
        String categoryName = mListScreen.get(position);
        FirebaseUtils.sendEventFunctionUsed(this, FirebaseUtils.IPTV_EVENT, "Click Item", categoryName);

        showAdsBeforeAction(mCateIptvInterstitialAd, () -> {
            Intent intent = new Intent(CategoryActivity.this, ChannelActivity.class);
            intent.putExtra(EXTRA_FILE_NAME, categoryName);
            startActivity(intent);
        });
    }

    private void setupChromecastConnection() {
        mDefaultCastStateListener.setCastIcon(mActivityIptvCategoryBinding.castImg);
        mChromecastConnection = new ChromecastConnection(this, mDefaultCastStateListener);
        mChromecastConnection.initialize(AppConstants.CAST_APPLICATION_ID);

        mActivityIptvCategoryBinding.castImg.setOnClickListener(view -> {
            FirebaseUtils.sendEventFunctionUsed(this, FirebaseUtils.CAST_BUTTON_EVENT, "Click cast button", "IPTV category layout");

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
}

