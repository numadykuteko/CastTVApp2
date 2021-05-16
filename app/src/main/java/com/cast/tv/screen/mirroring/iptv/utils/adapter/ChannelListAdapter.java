package com.cast.tv.screen.mirroring.iptv.utils.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cast.tv.screen.mirroring.iptv.R;
import com.cast.tv.screen.mirroring.iptv.listener.OnListItemClickListener;
import com.cast.tv.screen.mirroring.iptv.utils.iptv.ChannelList;
import com.cast.tv.screen.mirroring.iptv.utils.nativeads.NativeAdsViewHolder;

public class ChannelListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "ChannelListAdapter";
    private ChannelList mChannelList;
    private int mCurrentItem = -1;

    private OnListItemClickListener mListener;

    public ChannelListAdapter(OnListItemClickListener listener) {
        this.mListener = listener;
    }

    public static final int ADS_INDEX = 0;

    public void setData(ChannelList channelList) {
        mChannelList = channelList;
        mCurrentItem = -1;
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

    public ChannelListAdapter() {
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_channel_view, parent, false);
            return new ChannelListViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_small_native_control, parent, false);
            return new NativeAdsViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == 0) {
            ((ChannelListViewHolder) holder).bindView(position, mCurrentItem, mChannelList.items.get(position), mListener);
        } else {
            ((NativeAdsViewHolder) holder).bindView();
        }
    }

    @Override
    public int getItemCount() {
        return mChannelList == null ? 0 : mChannelList.items == null ? 0 : mChannelList.items.size();
    }
}
