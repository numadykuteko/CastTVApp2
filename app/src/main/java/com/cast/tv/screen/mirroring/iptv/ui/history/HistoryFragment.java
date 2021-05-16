package com.cast.tv.screen.mirroring.iptv.ui.history;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.cast.tv.screen.mirroring.iptv.R;
import com.cast.tv.screen.mirroring.iptv.data.model.HistoryData;
import com.cast.tv.screen.mirroring.iptv.databinding.FragmentHistoryBinding;
import com.cast.tv.screen.mirroring.iptv.listener.OnSavedItemClickListener;
import com.cast.tv.screen.mirroring.iptv.ui.base.BaseFragment;
import com.cast.tv.screen.mirroring.iptv.utils.adapter.SavedDataListAdapter;
import com.cast.tv.screen.mirroring.iptv.utils.adapter.SavedDataTouchCallback;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItem;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends BaseFragment<FragmentHistoryBinding, HistoryViewModel> implements HistoryNavigator, OnSavedItemClickListener {
    private FragmentHistoryBinding mFragmentHistoryBinding;
    private HistoryViewModel mHistoryViewModel;
    private int mPosition;

    private int mNoDataString = R.string.activity_video_no_video, mNoDataImage = R.drawable.ic_video;
    private final int[] STRING_NO_DATA_LIST = {R.string.activity_video_no_video, R.string.activity_photo_no_photo, R.string.activity_audio_no_audio};
    private final int[] IMAGE_NO_DATA_LIST = {R.drawable.ic_video, R.drawable.ic_image, R.drawable.ic_audio};

    private boolean mIsLoading = false;
    private List<HistoryData> mDataList;
    private SavedDataListAdapter mSavedDataListAdapter;

    @Override
    public int getBindingVariable() {
        return 0;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_history;
    }

    @Override
    public void reloadEasyChangeData() {

    }

    @Override
    public HistoryViewModel getViewModel() {
        mHistoryViewModel = ViewModelProviders.of(this).get(HistoryViewModel.class);
        return mHistoryViewModel;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHistoryViewModel.setNavigator(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFragmentHistoryBinding = getViewDataBinding();
        mPosition = FragmentPagerItem.getPosition(getArguments());
        mHistoryViewModel.setTypeSearch(mPosition);
        mNoDataString = STRING_NO_DATA_LIST[mPosition];
        mNoDataImage = IMAGE_NO_DATA_LIST[mPosition];

        setForLiveData();
        setForRecyclerView();
        setForPullRefresh();

        startGetData(true);
    }

    private void setForLiveData() {
        mHistoryViewModel.getListFileLiveData().observe(getViewLifecycleOwner(), this::updateData);
    }

    private void updateData(List<HistoryData> dataList) {
        mIsLoading = false;
        mFragmentHistoryBinding.pullToRefresh.setRefreshing(false);

        if (dataList.size() > 0) {
            mDataList = new ArrayList<>();
            mDataList.addAll(dataList);

            mDataList.add(0, new HistoryData());

            mSavedDataListAdapter.setData(mDataList);
            showDataArea();
        } else {
            showNoDataArea();
        }
    }

    private void setForRecyclerView() {
        mSavedDataListAdapter = new SavedDataListAdapter(this);

        mFragmentHistoryBinding.dataListArea.setLayoutManager(new LinearLayoutManager(getActivity()));
        SavedDataTouchCallback callback = new SavedDataTouchCallback(mSavedDataListAdapter.getItemSwipeListenerListener());
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        mFragmentHistoryBinding.dataListArea.setItemAnimator(new DefaultItemAnimator());
        mFragmentHistoryBinding.dataListArea.setAdapter(mSavedDataListAdapter);

        itemTouchHelper.attachToRecyclerView(mFragmentHistoryBinding.dataListArea);
    }

    private void setForPullRefresh() {
        mFragmentHistoryBinding.pullToRefresh.setOnRefreshListener(() -> {
            startGetData(false);
        });
    }

    private void startGetData(boolean forceReload) {
        if (mIsLoading) {
            mFragmentHistoryBinding.pullToRefresh.setRefreshing(false);
            return;
        }

        if (forceReload) {
            showLoadingArea();
        }

        mIsLoading = true;
        mHistoryViewModel.startSeeding();
    }

    private void showLoadingArea() {
        mFragmentHistoryBinding.loadingArea.setVisibility(View.VISIBLE);

        mFragmentHistoryBinding.dataListArea.setVisibility(View.GONE);
        mFragmentHistoryBinding.noDataErrorArea.setVisibility(View.GONE);
    }

    private void showNoDataArea() {
        mFragmentHistoryBinding.noDataErrorTv.setText(mNoDataString);
        if (getActivity() != null) {
            mFragmentHistoryBinding.noDataErrorImg.setImageDrawable(getActivity().getDrawable(mNoDataImage));
        }
        mFragmentHistoryBinding.noDataErrorArea.setVisibility(View.VISIBLE);

        mFragmentHistoryBinding.dataListArea.setVisibility(View.GONE);
        mFragmentHistoryBinding.loadingArea.setVisibility(View.GONE);
    }

    private void showDataArea() {
        mFragmentHistoryBinding.dataListArea.setVisibility(View.VISIBLE);

        mFragmentHistoryBinding.noDataErrorArea.setVisibility(View.GONE);
        mFragmentHistoryBinding.loadingArea.setVisibility(View.GONE);
    }

    @Override
    public void onClickItem(int position) {
        if (position >= 0 && position < mDataList.size()) {
            HistoryData historyData = mDataList.get(position);
            if (mActivity != null && mActivity instanceof HistoryActivity) {
                HistoryActivity historyActivity = (HistoryActivity) mActivity;
                historyActivity.startCheckCastConnection(historyData);
            }
        }
    }

    @Override
    public void onRemoveItem(int position) {
        if (position >= 0 && position < mDataList.size()) {
            mSavedDataListAdapter.removeItem(position);
            mHistoryViewModel.deleteData(mDataList.get(position));
            mDataList.remove(position);

            if (mDataList.size() <= 1) {
                showNoDataArea();
            }
        }
    }
}

