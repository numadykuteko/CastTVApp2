package com.cast.tv.screen.mirroring.iptv.ui.screencast.selectscreen;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.Display;
import android.view.View;

import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.cast.tv.screen.mirroring.iptv.BuildConfig;
import com.cast.tv.screen.mirroring.iptv.R;
import com.cast.tv.screen.mirroring.iptv.constants.AppConstants;
import com.cast.tv.screen.mirroring.iptv.constants.DataConstants;
import com.cast.tv.screen.mirroring.iptv.databinding.ActivitySelectScreenBinding;
import com.cast.tv.screen.mirroring.iptv.listener.OnListItemClickListener;
import com.cast.tv.screen.mirroring.iptv.ui.base.BaseBindingActivity;
import com.cast.tv.screen.mirroring.iptv.utils.DialogFactory;
import com.cast.tv.screen.mirroring.iptv.utils.FirebaseUtils;
import com.cast.tv.screen.mirroring.iptv.utils.adapter.DataListAdapter;
import com.cast.tv.screen.mirroring.iptv.utils.miracast.MiracastUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SelectScreenActivity extends BaseBindingActivity<ActivitySelectScreenBinding, SelectScreenViewModel> implements SelectScreenNavigator, OnListItemClickListener {

    private SelectScreenViewModel mSelectScreenViewModel;
    private ActivitySelectScreenBinding mActivitySelectScreenBinding;
    private List<String> mListScreen = new ArrayList<>();
    private DataListAdapter mDataListAdapter;

    @Override
    public int getBindingVariable() {
        return 0;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_select_screen;
    }

    @Override
    public SelectScreenViewModel getViewModel() {
        mSelectScreenViewModel = ViewModelProviders.of(this).get(SelectScreenViewModel.class);
        return mSelectScreenViewModel;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSelectScreenViewModel.setNavigator(this);
        mActivitySelectScreenBinding = getViewDataBinding();
        initView();
    }

    @Override
    protected void initView() {
        setNoActionBar();
        setForLiveData();
        setForPullRefresh();
        setForClick();

        mDataListAdapter = new DataListAdapter(this);
        StaggeredGridLayoutManager mGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mActivitySelectScreenBinding.dataListArea.setLayoutManager(mGridLayoutManager);
        mActivitySelectScreenBinding.dataListArea.setAdapter(mDataListAdapter);

        Display display = MiracastUtils.checkMiracastConnected(this);
        if (display != null) {
            String tiviName = display.getName() == null || display.getName().length() == 0 ? "TV" : display.getName();

            SweetAlertDialog dialogConfirm = DialogFactory.getDialogConfirm(this, getString(R.string.activity_select_screen_confirm_reconnect, tiviName));
            dialogConfirm.setCancelClickListener(sweetAlertDialog -> {
                sweetAlertDialog.dismiss();
                finish();
            });
            dialogConfirm.setConfirmClickListener(sweetAlertDialog -> {
                sweetAlertDialog.dismiss();
                reloadData(true);
            });

            dialogConfirm.show();
        } else {
            reloadData(true);
        }
    }

    @Override
    protected void setClick() {}

    @Override
    public void onFragmentDetached(String tag) {}

    private void setForLiveData() {

    }

    private void setForPullRefresh() {
        mActivitySelectScreenBinding.pullToRefresh.setOnRefreshListener(() -> {
            reloadData(false);
        });
    }

    private void setForClick() {
        mActivitySelectScreenBinding.backImg.setOnClickListener(view -> onBackPressed());
    }

    private void updateListData(List<String> fileDataList) {
        mListScreen = new ArrayList<>();
        mListScreen.addAll(fileDataList);

        if (mListScreen.size() > 0) {
            mListScreen.add(0, "");
            Parcelable oldPosition = null;
            if (mActivitySelectScreenBinding.dataListArea.getLayoutManager() != null) {
                oldPosition = mActivitySelectScreenBinding.dataListArea.getLayoutManager().onSaveInstanceState();
            }
            mDataListAdapter.setData(mListScreen);
            if (oldPosition != null) {
                mActivitySelectScreenBinding.dataListArea.getLayoutManager().onRestoreInstanceState(oldPosition);
            }
            showDataArea();
        }

        mActivitySelectScreenBinding.pullToRefresh.setRefreshing(false);
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
                    updateListData(DataConstants.SCREEN_LIST);
                });
            }
        }, 1000);
    }

    private void showDataArea() {
        mActivitySelectScreenBinding.dataListArea.setVisibility(View.VISIBLE);
        mActivitySelectScreenBinding.loadingArea.setVisibility(View.GONE);
    }

    private void showLoadingArea() {
        mActivitySelectScreenBinding.dataListArea.setVisibility(View.GONE);
        mActivitySelectScreenBinding.loadingArea.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClickItem(int position) {
        FirebaseUtils.sendEventFunctionUsed(this, FirebaseUtils.SELECT_SCREEN_EVENT, "Click item", mListScreen.get(position));

        gotoActivityWithFlag(AppConstants.FLAG_START_PREPARE_SCREEN_CAST);
        finish();
    }
}
