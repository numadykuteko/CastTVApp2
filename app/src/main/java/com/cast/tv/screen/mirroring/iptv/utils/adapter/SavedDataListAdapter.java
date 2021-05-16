package com.cast.tv.screen.mirroring.iptv.utils.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cast.tv.screen.mirroring.iptv.R;
import com.cast.tv.screen.mirroring.iptv.data.model.SavedData;
import com.cast.tv.screen.mirroring.iptv.listener.OnSavedItemClickListener;
import com.cast.tv.screen.mirroring.iptv.utils.nativeads.NativeAdsViewHolder;

import java.util.ArrayList;
import java.util.List;

public class SavedDataListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "SavedDataListAdapter";
    private List<SavedData> mFileList = new ArrayList<SavedData>();
    private int mCurrentItem = -1;

    public List<SavedData> getListData() {
        return mFileList;
    }

    private OnSavedItemClickListener mListener;
    private ItemSwipeListener mItemSwipeListener = (currentPosition) -> {
        mListener.onRemoveItem(currentPosition);
    };

    public ItemSwipeListener getItemSwipeListenerListener() {
        return mItemSwipeListener;
    }

    public SavedDataListAdapter(OnSavedItemClickListener listener) {
        this.mListener = listener;
    }

    public static final int ADS_INDEX = 0;

    public void setData(List<? extends SavedData> videoList) {
        mFileList = new ArrayList<>();
        mCurrentItem = -1;
        mFileList.addAll(videoList);
        notifyDataSetChanged();
    }

    public void setCurrentItem(int position) {
        int temp = mCurrentItem;
        mCurrentItem = position;

        if (temp >= 0 && temp < getItemCount()) {
            notifyItemChanged(temp);
        }
        if (mCurrentItem >= 0 && mCurrentItem < getItemCount()) {
            notifyItemChanged(mCurrentItem);
        }
    }

    public SavedDataListAdapter() {
    }

    public void removeItem(int position) {
        mFileList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mFileList.size());
    }

    @Override
    public int getItemViewType(int position) {
        if (position == ADS_INDEX) {
            return 1;
        } else {
            return super.getItemViewType(position);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_saved_data_view, parent, false);
            return new SavedDataListViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_small_native_control, parent, false);
            return new NativeAdsViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == 0) {
            ((SavedDataListViewHolder) holder).bindView(position, mCurrentItem, mFileList.get(position), mListener);
        } else {
            ((NativeAdsViewHolder) holder).bindView();
        }
    }

    @Override
    public int getItemCount() {
        return mFileList.size();
    }

    public interface ItemSwipeListener {
        void onSwipe(int position);
    }
}
