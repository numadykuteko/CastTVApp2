package com.cast.tv.screen.mirroring.iptv.data.model;

import java.io.Serializable;

public class ImageData implements Serializable {
    private String mImageName;
    private String mImageUrl;

    public ImageData(String mImageName, String mImageUrl) {
        this.mImageName = mImageName;
        this.mImageUrl = mImageUrl;
    }

    public String getImageName() {
        return mImageName;
    }

    public void setImageName(String mImageName) {
        this.mImageName = mImageName;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String mImageUrl) {
        this.mImageUrl = mImageUrl;
    }
}
