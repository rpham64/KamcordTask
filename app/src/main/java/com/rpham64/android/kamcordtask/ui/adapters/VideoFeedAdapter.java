package com.rpham64.android.kamcordtask.ui.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rpham64.android.kamcordtask.R;
import com.rpham64.android.kamcordtask.models.KamcordVideo;

import java.util.List;

/**
 * Created by Rudolf on 10/14/2016.
 */

public class VideoFeedAdapter extends RecyclerView.Adapter<VideoFeedHolder> {

    private Context mContext;
    private List<KamcordVideo> mVideos;
    private int mLastBoundPosition;

    public VideoFeedAdapter(Context context, List<KamcordVideo> videos) {
        this.mContext = context;
        this.mVideos = videos;
    }

    @Override
    public VideoFeedHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.item_video, parent, false);
        return new VideoFeedHolder(mContext, view);
    }

    @Override
    public void onBindViewHolder(VideoFeedHolder holder, int position) {
        KamcordVideo video = mVideos.get(position);
        mLastBoundPosition = position;

        holder.bindItem(video);
    }

    @Override
    public int getItemCount() {
        if (mVideos != null) return mVideos.size();
        return 0;
    }

    public int getLastBoundPosition() {
        return mLastBoundPosition;
    }
}

