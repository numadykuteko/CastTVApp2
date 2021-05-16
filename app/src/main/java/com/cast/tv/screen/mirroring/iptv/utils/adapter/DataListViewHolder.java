package com.cast.tv.screen.mirroring.iptv.utils.adapter;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.cast.tv.screen.mirroring.iptv.R;
import com.cast.tv.screen.mirroring.iptv.listener.OnListItemClickListener;

public class DataListViewHolder extends RecyclerView.ViewHolder {
    private CardView mContentView;
    private TextView mNameView;

    public DataListViewHolder(@NonNull View itemView) {
        super(itemView);
        initView();
    }

    private void initView() {
        mContentView = itemView.findViewById(R.id.item_content_view);
        mNameView = itemView.findViewById(R.id.item_name_view);
    }

    public void bindView(int position, String screenName, OnListItemClickListener listener) {
        mNameView.setText(screenName);

        mContentView.setOnClickListener(v -> {
            listener.onClickItem(position);
        });
    }
}
