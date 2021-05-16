package com.cast.tv.screen.mirroring.iptv.utils.adapter;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cast.tv.screen.mirroring.iptv.R;
import com.cast.tv.screen.mirroring.iptv.constants.DataConstants;
import com.cast.tv.screen.mirroring.iptv.data.model.FileData;
import com.cast.tv.screen.mirroring.iptv.listener.OnListItemClickListener;
import com.cast.tv.screen.mirroring.iptv.utils.DateTimeUtils;
import com.cast.tv.screen.mirroring.iptv.utils.glide.GlideApp;

public class GalleryListViewHolder extends RecyclerView.ViewHolder {
    private LinearLayout mContentView;
    private ImageView mImageView;
    private ImageView mVideoPlayView;
    private TextView mNameView;
    private TextView mDescriptionView;
    private ImageView mCurrentView;
    private View mOverlayView;

    public GalleryListViewHolder(@NonNull View itemView) {
        super(itemView);
        initView();
    }

    private void initView() {
        mContentView = itemView.findViewById(R.id.item_content_view);
        mImageView = itemView.findViewById(R.id.item_image_view);
        mNameView = itemView.findViewById(R.id.item_name_view);
        mDescriptionView = itemView.findViewById(R.id.item_description_view);
        mVideoPlayView = itemView.findViewById(R.id.item_video_play_view);
        mCurrentView = itemView.findViewById(R.id.item_current_view);
        mOverlayView = itemView.findViewById(R.id.item_overlay_view);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void bindView(int position, int currentPosition, FileData fileData, OnListItemClickListener listener) {
        mNameView.setText(fileData.getDisplayName());

        if (fileData.getFileType().equals(DataConstants.FILE_TYPE_VIDEO) || fileData.getFileType().equals(DataConstants.FILE_TYPE_PHOTO)) {
            GlideApp.with(itemView.getContext())
                    .load(fileData.getFilePath())
                    .into(mImageView);
        } else if (fileData.getFileType().equals(DataConstants.FILE_TYPE_AUDIO)) {
            mImageView.setImageDrawable(itemView.getContext().getDrawable(R.drawable.ic_audio_color));
        } else {
            mImageView.setImageDrawable(itemView.getContext().getDrawable(R.drawable.ic_folder));
        }

        if (fileData.getFileType().equals(DataConstants.FILE_TYPE_VIDEO)) {
            mVideoPlayView.setVisibility(View.VISIBLE);
        } else {
            mVideoPlayView.setVisibility(View.GONE);
        }

        if (fileData.getDateAdded() != -1) {
            mDescriptionView.setText(DateTimeUtils.fromTimeUnixToDateTimeString(fileData.getDateAdded()));
        } else {
            if (fileData.getFileType().equals(DataConstants.FILE_TYPE_VIDEO)) {
                mDescriptionView.setText("video");
            } else if (fileData.getFileType().equals(DataConstants.FILE_TYPE_PHOTO)) {
                mDescriptionView.setText("photo");
            } else if (fileData.getFileType().equals(DataConstants.FILE_TYPE_AUDIO)) {
                mDescriptionView.setText("audio");
            } else {
                mDescriptionView.setText("folder");
            }
        }

        if (currentPosition == position) {
            mCurrentView.setVisibility(View.VISIBLE);
            mVideoPlayView.setVisibility(View.GONE);
            mOverlayView.setVisibility(View.VISIBLE);
        } else {
            mCurrentView.setVisibility(View.GONE);
            mOverlayView.setVisibility(View.GONE);
        }

        mContentView.setOnClickListener(v -> {
            listener.onClickItem(position);
        });
    }
}
