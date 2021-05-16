package com.cast.tv.screen.mirroring.iptv.utils.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.cast.tv.screen.mirroring.iptv.R;
import com.cast.tv.screen.mirroring.iptv.data.model.FileData;
import com.cast.tv.screen.mirroring.iptv.listener.OnListItemClickListener;
import com.cast.tv.screen.mirroring.iptv.utils.ColorUtils;
import com.cast.tv.screen.mirroring.iptv.utils.DateTimeUtils;

public class FileListViewHolder extends RecyclerView.ViewHolder {
    private CardView mContentView;
    private ImageView mImageView;
    private TextView mNameView;
    private LinearLayout mDateView;
    private TextView mDateTextView;

    public FileListViewHolder(@NonNull View itemView) {
        super(itemView);
        initView();
    }

    private void initView() {
//        mContentView = itemView.findViewById(R.id.item_content_view);
//        mImageView = itemView.findViewById(R.id.item_image_view);
//        mNameView = itemView.findViewById(R.id.item_name_view);

    }

    public void bindView(int position, FileData fileData, int currentItem, OnListItemClickListener listener) {
        mNameView.setText(fileData.getDisplayName());
        if (currentItem == position) {
            mNameView.setTextColor(ColorUtils.getColorFromResource(itemView.getContext(), R.color.blue_light));
        } else {
            mNameView.setTextColor(ColorUtils.getColorFromResource(itemView.getContext(), R.color.black_totally));
        }

        if (fileData.getDateAdded() > 0) {
            mDateView.setVisibility(View.VISIBLE);
            mDateTextView.setText(DateTimeUtils.fromTimeUnixToDateString(fileData.getDateAdded()));
        } else {
            mDateView.setVisibility(View.GONE);
        }

        mContentView.setOnClickListener(v -> {
            listener.onClickItem(position);
        });
    }
}
