package com.cast.tv.screen.mirroring.iptv.utils.adapter;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.cast.tv.screen.mirroring.iptv.R;
import com.cast.tv.screen.mirroring.iptv.data.model.FileData;
import com.cast.tv.screen.mirroring.iptv.listener.OnFileItemClickListener;
import com.cast.tv.screen.mirroring.iptv.utils.image.ImageUtils;

import java.io.File;

public class PhotoListViewHolder extends RecyclerView.ViewHolder {
    private CardView mContentView;
    private ImageView mImageView;
    private ImageView mBookmarkView;
    private ImageView mCurrentView;

    public PhotoListViewHolder(@NonNull View itemView) {
        super(itemView);
        initView();
    }

    private void initView() {
        mContentView = itemView.findViewById(R.id.item_content_view);
        mImageView = itemView.findViewById(R.id.item_image_view);
        mBookmarkView = itemView.findViewById(R.id.item_bookmark_view);
        mCurrentView = itemView.findViewById(R.id.item_current_view);
    }

    public void bindView(int position, int currentPosition, FileData fileData, OnFileItemClickListener listener) {
        ImageUtils.loadImageFromUriToView(itemView.getContext(), new File(fileData.getFilePath()), mImageView);

        mContentView.setOnClickListener(v -> {
            listener.onClickItem(position);
        });

        if (fileData.isBookmarked()) {
            mBookmarkView.setImageDrawable(itemView.getContext().getDrawable(R.drawable.ic_bookmark_done));
        } else {
            mBookmarkView.setImageDrawable(itemView.getContext().getDrawable(R.drawable.ic_bookmark_not_done));
        }

        if (currentPosition == position) {
            mCurrentView.setVisibility(View.VISIBLE);
        } else {
            mCurrentView.setVisibility(View.GONE);
        }

        mBookmarkView.setOnClickListener(view -> {
            listener.onClickBookmark(position);
        });
    }
}
