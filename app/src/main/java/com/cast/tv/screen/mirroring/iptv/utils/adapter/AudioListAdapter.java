package com.cast.tv.screen.mirroring.iptv.utils.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.cast.tv.screen.mirroring.iptv.R;
import com.cast.tv.screen.mirroring.iptv.data.model.FileData;
import com.cast.tv.screen.mirroring.iptv.listener.OnFileItemClickListener;
import com.cast.tv.screen.mirroring.iptv.utils.nativeads.NativeAdsViewHolder;

import java.util.ArrayList;
import java.util.List;

public class AudioListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "FileListAdapter";
    private List<FileData> mFileList = new ArrayList<FileData>();
    private int mCurrentItem = -1;
    public static final int ADS_INDEX = 0;

    private OnFileItemClickListener mListener;

    public AudioListAdapter(OnFileItemClickListener listener) {
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

    public AudioListAdapter() {
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
            view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    final ViewGroup.LayoutParams lp = view.getLayoutParams();
                    if (lp instanceof StaggeredGridLayoutManager.LayoutParams) {
                        StaggeredGridLayoutManager.LayoutParams sglp = (StaggeredGridLayoutManager.LayoutParams) lp;
                        sglp.setFullSpan(true);
                        view.setLayoutParams(sglp);
                        final StaggeredGridLayoutManager lm =
                                (StaggeredGridLayoutManager) ((RecyclerView) parent).getLayoutManager();
                        if (lm != null) {
                            lm.invalidateSpanAssignments();
                        }
                    }
                    view.getViewTreeObserver().removeOnPreDrawListener(this);
                    return true;
                }
            });

            return new NativeAdsViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_audio_view, parent, false);
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            int width = layoutParams.width = (int) (parent.getWidth() / 2);
            layoutParams.height = (int) (width * 1.0);

            view.setLayoutParams(layoutParams);
            return new AudioListViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == 0) {
            ((AudioListViewHolder) holder).bindView(position, mCurrentItem, mFileList.get(position), mListener);
        } else {
            ((NativeAdsViewHolder) holder).bindView();
        }
    }

    @Override
    public int getItemCount() {
        return mFileList.size();
    }
}
