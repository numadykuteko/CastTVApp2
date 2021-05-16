package com.cast.tv.screen.mirroring.iptv.utils.adapter;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cast.tv.screen.mirroring.iptv.R;
import com.cast.tv.screen.mirroring.iptv.listener.OnListItemClickListener;
import com.cast.tv.screen.mirroring.iptv.utils.image.ImageUtils;
import com.cast.tv.screen.mirroring.iptv.utils.iptv.ChannelItem;

public class ChannelListViewHolder extends RecyclerView.ViewHolder {
    private LinearLayout mContentView;
    private ImageView mImageView;
    private TextView mNameView;
    private TextView mDescriptionView;
    private ImageView mCurrentView;
    private View mOverlayView;

    public ChannelListViewHolder(@NonNull View itemView) {
        super(itemView);
        initView();
    }

    private void initView() {
        mContentView = itemView.findViewById(R.id.item_content_view);
        mImageView = itemView.findViewById(R.id.item_image_view);
        mNameView = itemView.findViewById(R.id.item_name_view);
        mDescriptionView = itemView.findViewById(R.id.item_description_view);
        mCurrentView = itemView.findViewById(R.id.item_current_view);
        mOverlayView = itemView.findViewById(R.id.item_overlay_view);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void bindView(int position, int currentPosition, ChannelItem channelItem, OnListItemClickListener listener) {
        mNameView.setText(channelItem.name);

        if (channelItem.metadata != null && channelItem.metadata.get("tvg-logo") != null) {
            ImageUtils.loadImageHorizontalToView(itemView.getContext(), channelItem.metadata.get("tvg-logo"), mImageView);
        }

        if (channelItem.metadata != null && channelItem.metadata.get("tvg-language") != null) {
            mDescriptionView.setText(channelItem.metadata.get("tvg-language"));
        }

        if (currentPosition == position) {
            mCurrentView.setVisibility(View.VISIBLE);
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
