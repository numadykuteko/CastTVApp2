package com.cast.tv.screen.mirroring.iptv.utils.adapter;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.cast.tv.screen.mirroring.iptv.R;
import com.cast.tv.screen.mirroring.iptv.data.model.FileData;
import com.cast.tv.screen.mirroring.iptv.listener.OnListItemClickListener;
import com.cast.tv.screen.mirroring.iptv.utils.image.ImageUtils;

import java.io.File;

public class VideoListViewHolder extends RecyclerView.ViewHolder {
    private CardView mContentView;
    private ImageView mImageView;

    public VideoListViewHolder(@NonNull View itemView) {
        super(itemView);
        initView();
    }

    private void initView() {
        mContentView = itemView.findViewById(R.id.item_content_view);
        mImageView = itemView.findViewById(R.id.item_image_view);
    }

    public void bindView(int position, FileData fileData, OnListItemClickListener listener) {
        if (fileData.getThumbnail() != null) {
            ImageUtils.loadImageFromUriToView(itemView.getContext(), new File(fileData.getThumbnail()), mImageView);
        }

        mContentView.setOnClickListener(v -> {
            listener.onClickItem(position);
        });
    }
}
