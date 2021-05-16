package com.cast.tv.screen.mirroring.iptv.ui.bookmark;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.cast.tv.screen.mirroring.iptv.R;
import com.cast.tv.screen.mirroring.iptv.data.model.BookmarkData;
import com.cast.tv.screen.mirroring.iptv.databinding.FragmentBookmarkBinding;
import com.cast.tv.screen.mirroring.iptv.listener.OnSavedItemClickListener;
import com.cast.tv.screen.mirroring.iptv.ui.base.BaseFragment;

import com.cast.tv.screen.mirroring.iptv.utils.adapter.SavedDataListAdapter;
import com.cast.tv.screen.mirroring.iptv.utils.adapter.SavedDataTouchCallback;

import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItem;

import java.util.ArrayList;
import java.util.List;

public class BookmarkFragment extends BaseFragment<FragmentBookmarkBinding, BookmarkViewModel> implements BookmarkNavigator, OnSavedItemClickListener {
    private FragmentBookmarkBinding mFragmentBookmarkBinding;
    private BookmarkViewModel mBookmarkViewModel;
    private int mPosition;

    private int mNoDataString = R.string.activity_video_no_video, mNoDataImage = R.drawable.ic_video;
    private final int[] STRING_NO_DATA_LIST = {R.string.activity_video_no_video, R.string.activity_photo_no_photo, R.string.activity_audio_no_audio};
    private final int[] IMAGE_NO_DATA_LIST = {R.drawable.ic_video, R.drawable.ic_image, R.drawable.ic_audio};

    private boolean mIsLoading = false;
    private List<BookmarkData> mDataList;
    private SavedDataListAdapter mSavedDataListAdapter;

    @Override
    public int getBindingVariable() {
        return 0;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_bookmark;
    }

    @Override
    public void reloadEasyChangeData() {

    }

    @Override
    public BookmarkViewModel getViewModel() {
        mBookmarkViewModel = ViewModelProviders.of(this).get(BookmarkViewModel.class);
        return mBookmarkViewModel;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBookmarkViewModel.setNavigator(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFragmentBookmarkBinding = getViewDataBinding();
        mPosition = FragmentPagerItem.getPosition(getArguments());
        mBookmarkViewModel.setTypeSearch(mPosition);
        mNoDataString = STRING_NO_DATA_LIST[mPosition];
        mNoDataImage = IMAGE_NO_DATA_LIST[mPosition];

        setForLiveData();
        setForRecyclerView();
        setForPullRefresh();

        startGetData(true);
    }

    private void setForLiveData() {
        mBookmarkViewModel.getListFileLiveData().observe(getViewLifecycleOwner(), this::updateData);
    }

    private void updateData(List<BookmarkData> dataList) {
        mIsLoading = false;
        mFragmentBookmarkBinding.pullToRefresh.setRefreshing(false);

        if (dataList.size() > 0) {
            mDataList = new ArrayList<>();
            mDataList.addAll(dataList);

            mDataList.add(0, new BookmarkData());

            mSavedDataListAdapter.setData(mDataList);
            showDataArea();
        } else {
            showNoDataArea();
        }
    }

    private void setForRecyclerView() {
        mSavedDataListAdapter = new SavedDataListAdapter(this);

        mFragmentBookmarkBinding.dataListArea.setLayoutManager(new LinearLayoutManager(getActivity()));
        SavedDataTouchCallback callback = new SavedDataTouchCallback(mSavedDataListAdapter.getItemSwipeListenerListener());
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        mFragmentBookmarkBinding.dataListArea.setItemAnimator(new DefaultItemAnimator());
        mFragmentBookmarkBinding.dataListArea.setAdapter(mSavedDataListAdapter);

        itemTouchHelper.attachToRecyclerView(mFragmentBookmarkBinding.dataListArea);
    }

    private void setForPullRefresh() {
        mFragmentBookmarkBinding.pullToRefresh.setOnRefreshListener(() -> {
            startGetData(false);
        });
    }

    private void startGetData(boolean forceReload) {
        if (mIsLoading) {
            mFragmentBookmarkBinding.pullToRefresh.setRefreshing(false);
            return;
        }

        if (forceReload) {
            showLoadingArea();
        }

        mIsLoading = true;
        mBookmarkViewModel.startSeeding();
    }

    private void showLoadingArea() {
        mFragmentBookmarkBinding.loadingArea.setVisibility(View.VISIBLE);

        mFragmentBookmarkBinding.dataListArea.setVisibility(View.GONE);
        mFragmentBookmarkBinding.noDataErrorArea.setVisibility(View.GONE);
    }

    private void showNoDataArea() {
        mFragmentBookmarkBinding.noDataErrorTv.setText(mNoDataString);
        if (getActivity() != null) {
            mFragmentBookmarkBinding.noDataErrorImg.setImageDrawable(getActivity().getDrawable(mNoDataImage));
        }
        mFragmentBookmarkBinding.noDataErrorArea.setVisibility(View.VISIBLE);

        mFragmentBookmarkBinding.dataListArea.setVisibility(View.GONE);
        mFragmentBookmarkBinding.loadingArea.setVisibility(View.GONE);
    }

    private void showDataArea() {
        mFragmentBookmarkBinding.dataListArea.setVisibility(View.VISIBLE);

        mFragmentBookmarkBinding.noDataErrorArea.setVisibility(View.GONE);
        mFragmentBookmarkBinding.loadingArea.setVisibility(View.GONE);
    }

    @Override
    public void onClickItem(int position) {
        if (position >= 0 && position < mDataList.size()) {
            BookmarkData bookmarkData = mDataList.get(position);
            if (mActivity != null && mActivity instanceof BookmarkActivity) {
                BookmarkActivity bookmarkActivity = (BookmarkActivity) mActivity;
                bookmarkActivity.startCheckCastConnection(bookmarkData);
            }
        }
    }

    @Override
    public void onRemoveItem(int position) {
        if (position >= 0 && position < mDataList.size()) {
            mSavedDataListAdapter.removeItem(position);
            mBookmarkViewModel.deleteData(mDataList.get(position));
            mDataList.remove(position);

            if (mDataList.size() <= 1) {
                showNoDataArea();
            }
        }
    }
}
