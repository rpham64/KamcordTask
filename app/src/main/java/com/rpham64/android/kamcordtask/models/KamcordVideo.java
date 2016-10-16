package com.rpham64.android.kamcordtask.models;

/**
 * Created by Rudolf on 10/14/2016.
 */

public class KamcordVideo {

    private int mHeartCount;
    private String mThumbnail;
    private String mUrl;

    public int getHeartCount() {
        return mHeartCount;
    }

    public void setHeartCount(int heartCount) {
        mHeartCount = heartCount;
    }

    public String getThumbnail() {
        return mThumbnail;
    }

    public void setThumbnail(String thumbnail) {
        mThumbnail = thumbnail;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }
}
