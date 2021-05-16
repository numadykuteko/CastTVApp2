package com.cast.tv.screen.mirroring.iptv.utils.glide;

import android.text.TextUtils;

import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;

public class HttpHeader {

    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.115 Safari/537.36";

    public static GlideUrl getUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        else return new GlideUrl(url, new LazyHeaders.Builder()
                .setHeader("User-Agent", USER_AGENT)
                .build());
    }
}