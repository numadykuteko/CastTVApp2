package com.cast.tv.screen.mirroring.iptv.utils.nativeads;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ads.control.Admod;
import com.cast.tv.screen.mirroring.iptv.BuildConfig;
import com.cast.tv.screen.mirroring.iptv.ui.base.BaseBindingActivity;

public class NativeAdsViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "NativeAdsViewHolder";

    public NativeAdsViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public void bindView() {
        if (itemView.getContext() != null && itemView.getContext() instanceof BaseBindingActivity) {
            Admod.getInstance().loadSmallNativeFragment((BaseBindingActivity) itemView.getContext(), BuildConfig.native_click_item_local_data_id, itemView);
        }
    }
}
