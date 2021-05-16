package com.cast.tv.screen.mirroring.iptv.utils.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cast.tv.screen.mirroring.iptv.R;
import com.cast.tv.screen.mirroring.iptv.data.model.FileData;
import com.cast.tv.screen.mirroring.iptv.listener.OnFileItemClickListener;
import com.cast.tv.screen.mirroring.iptv.utils.DateTimeUtils;
import com.cast.tv.screen.mirroring.iptv.utils.image.ImageUtils;

import java.io.File;

class VideoHorizontalViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "VideoListViewHolder";
    private ImageView mImageView;
    private ImageView mBookmarkView;
    private ImageView mCurrentView;
    private TextView mName;
    private TextView mDuration;
    private TextView mDate;

    public VideoHorizontalViewHolder(@NonNull View itemView) {
        super(itemView);
        initView();
    }

    private void initView() {
        mImageView = itemView.findViewById(R.id.video_view_img);
        mName = itemView.findViewById(R.id.video_view_name_tv);
        mDuration = itemView.findViewById(R.id.video_view_duration_tv);
        mDate = itemView.findViewById(R.id.video_view_date_tv);
        mBookmarkView = itemView.findViewById(R.id.item_bookmark_view);
        mCurrentView = itemView.findViewById(R.id.item_current_view);
    }

    public void bindView(int position, int currentPosition, FileData fileData, OnFileItemClickListener listener) {
        if (fileData.getThumbnail() != null) {
            ImageUtils.loadImageFromUriToView(itemView.getContext(), new File(fileData.getThumbnail()), mImageView);
        }

        if (fileData.getDisplayName() != null && fileData.getDisplayName().length() > 0) {
            mName.setText(fileData.getDisplayName());
        } else {
            mName.setText("No name");
        }

        if (fileData.isBookmarked()) {
            mBookmarkView.setImageDrawable(itemView.getContext().getDrawable(R.drawable.ic_bookmark_done));
        } else {
            mBookmarkView.setImageDrawable(itemView.getContext().getDrawable(R.drawable.ic_bookmark_not_done));
        }

        mBookmarkView.setOnClickListener(view -> {
            listener.onClickBookmark(position);
        });

        mDuration.setText(DateTimeUtils.fromDurationToString(fileData.getDuration()));

        if (fileData.getDateAdded() != -1) {
            mDate.setText(DateTimeUtils.fromTimeUnixToDateTimeString(fileData.getDateAdded()));
        } else {
            mDate.setText("NA");
        }

        if (currentPosition == position) {
            mCurrentView.setVisibility(View.VISIBLE);
        } else {
            mCurrentView.setVisibility(View.GONE);
        }

        itemView.setOnClickListener(v -> {
            listener.onClickItem(position);
        });
    }
}