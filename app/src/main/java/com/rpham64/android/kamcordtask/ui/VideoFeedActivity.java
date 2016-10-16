package com.rpham64.android.kamcordtask.ui;

import android.support.v4.app.Fragment;

import com.rpham64.android.kamcordtask.SingleFragmentActivity;

public class VideoFeedActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return VideoFeedFragment.newInstance();
    }
}
