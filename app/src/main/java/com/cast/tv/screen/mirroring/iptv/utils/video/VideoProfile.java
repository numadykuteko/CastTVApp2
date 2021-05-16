package com.cast.tv.screen.mirroring.iptv.utils.video;

public class VideoProfile {
    private int mProfileCode;
    private String mMimeType;
    private String mProfileAac;
    private int mLevel;
    private int mColorStandard = -1999;
    private int mColorTransfer = -1999;
    private int mColorRange = -1999;

    public int getProfileCode() {
        return mProfileCode;
    }

    public void setProfileCode(int mProfileCode) {
        this.mProfileCode = mProfileCode;
    }

    public String getMimeType() {
        return mMimeType;
    }

    public void setMimeType(String mMimeType) {
        this.mMimeType = mMimeType;
    }

    public String getProfileAac() {
        return mProfileAac;
    }

    public void setProfileAac(String mProfileAac) {
        this.mProfileAac = mProfileAac;
    }

    public int getLevel() {
        return mLevel;
    }

    public void setLevel(int mLevel) {
        this.mLevel = mLevel;
    }

    public int getColorStandard() {
        return mColorStandard;
    }

    public void setColorStandard(int mColorStandard) {
        this.mColorStandard = mColorStandard;
    }

    public int getColorRange() {
        return mColorRange;
    }

    public void setColorRange(int mColorRange) {
        this.mColorRange = mColorRange;
    }

    public int getColorTransfer() {
        return mColorTransfer;
    }

    public void setColorTransfer(int mColorTransfer) {
        this.mColorTransfer = mColorTransfer;
    }
}
