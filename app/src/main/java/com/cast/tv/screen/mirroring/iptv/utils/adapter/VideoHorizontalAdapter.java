package com.cast.tv.screen.mirroring.iptv.utils.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cast.tv.screen.mirroring.iptv.R;
import com.cast.tv.screen.mirroring.iptv.data.model.FileData;
import com.cast.tv.screen.mirroring.iptv.listener.OnFileItemClickListener;
import com.cast.tv.screen.mirroring.iptv.utils.nativeads.NativeAdsViewHolder;

import java.util.ArrayList;
import java.util.List;

public class VideoHorizontalAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "VideoListAdapter";
    private List<FileData> mFileList = new ArrayList<FileData>();

    public List<FileData> getListVideoData() {
        return mFileList;
    }

    public int mCurrentItem = -1;
    public static final int ADS_INDEX = 0;

    private OnFileItemClickListener mListener;

    public VideoHorizontalAdapter(OnFileItemClickListener listener) {
        this.mListener = listener;
    }

    public void setData(List<FileData> videoList) {
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

    public void setDataPosition(int position, FileData fileData) {
        mFileList.set(position, fileData);
        notifyItemChanged(position);
    }

    public VideoHorizontalAdapter() {
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
        if (viewType == 1) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_small_native_control, parent, false);
            return new NativeAdsViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_horizontal, parent, false);
            return new VideoHorizontalViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == 0) {
            ((VideoHorizontalViewHolder) holder).bindView(position, mCurrentItem, mFileList.get(position), mListener);
        } else {
            ((NativeAdsViewHolder) holder).bindView();
        }
    }

    @Override
    public int getItemCount() {
        return mFileList.size();
    }
}
