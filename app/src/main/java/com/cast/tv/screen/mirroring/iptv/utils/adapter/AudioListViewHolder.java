package com.cast.tv.screen.mirroring.iptv.utils.adapter;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.cast.tv.screen.mirroring.iptv.R;
import com.cast.tv.screen.mirroring.iptv.data.model.FileData;
import com.cast.tv.screen.mirroring.iptv.listener.OnFileItemClickListener;

public class AudioListViewHolder extends RecyclerView.ViewHolder {
    private CardView mContentView;
    private TextView mNameView;
    private ImageView mBookmarkView;
    private ImageView mCurrentView;

    public AudioListViewHolder(@NonNull View itemView) {
        super(itemView);
        initView();
    }

    private void initView() {
        mContentView = itemView.findViewById(R.id.item_content_view);
        mNameView = itemView.findViewById(R.id.item_name_view);
        mBookmarkView = itemView.findViewById(R.id.item_bookmark_view);
        mCurrentView = itemView.findViewById(R.id.item_current_view);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void bindView(int position, int currentPosition, FileData fileData, OnFileItemClickListener listener) {
        if (fileData.getDisplayName() != null && fileData.getDisplayName().length() > 0) {
            mNameView.setText(fileData.getDisplayName());
        } else {
            mNameView.setText("No name");
        }

        if (fileData.isBookmarked()) {
            mBookmarkView.setImageDrawable(itemView.getContext().getDrawable(R.drawable.ic_bookmark_done));
        } else {
            mBookmarkView.setImageDrawable(itemView.getContext().getDrawable(R.drawable.ic_bookmark_not_done));
        }

        mBookmarkView.setOnClickListener(view -> {
            listener.onClickBookmark(position);
        });

        if (currentPosition == position) {
            mCurrentView.setVisibility(View.VISIBLE);
        } else {
            mCurrentView.setVisibility(View.GONE);
        }

        mContentView.setOnClickListener(v -> {
            listener.onClickItem(position);
        });
    }
}
