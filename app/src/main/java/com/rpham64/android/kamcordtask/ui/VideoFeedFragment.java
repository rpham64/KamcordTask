package com.rpham64.android.kamcordtask.ui;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.rpham64.android.kamcordtask.R;
import com.rpham64.android.kamcordtask.models.KamcordVideo;
import com.rpham64.android.kamcordtask.ui.adapters.VideoFeedAdapter;
import com.rpham64.android.kamcordtask.ui.adapters.VideoFeedHolder;
import com.rpham64.android.kamcordtask.utils.VideoDownloader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rudolf on 10/14/2016.
 */

public class VideoFeedFragment extends Fragment {

    private static final String TAG = VideoFeedFragment.class.getName();

    private RecyclerView mRecyclerView;
    private VideoFeedAdapter mAdapter;

    private VideoDownloader<VideoFeedHolder> mVideoDownloader;

    private List<KamcordVideo> mVideos;
    private int lastPagedFetched = 1;

    public static VideoFeedFragment newInstance() {
        return new VideoFeedFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mVideos = new ArrayList<>();

        Handler responseHandler = new Handler();
        mVideoDownloader = new VideoDownloader<>(responseHandler);

        mVideoDownloader.setVideoDownloadListener(
                new VideoDownloader.VideoDownloadListener<VideoFeedHolder>() {
                    @Override
                    public void onThumbnailDownloadeded(VideoFeedHolder target, Bitmap thumbnail) {

                    }

                    @Override
                    public void onVideoDownloaded(VideoFeedHolder target, String url) {

                    }
                }
        );

        mVideoDownloader.start();
        mVideoDownloader.getLooper();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_video_feed, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_video_feed_recycler_view);

        final GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        mRecyclerView.setLayoutManager(gridLayoutManager);

//        setEndlessPageScrolling(gridLayoutManager);

        new FetchItemsTask().execute();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mVideoDownloader.clearQueue();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mVideoDownloader.quit();
        Log.i(TAG, "Background thread VideoDownloader has been destroyed.");
    }

    private void setupAdapter() {
        if (isAdded()) {
            mAdapter = new VideoFeedAdapter(getActivity(), mVideos);
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    /**
     * Enables pagination
     *
     * @param mLayoutManager
     */
    private void setEndlessPageScrolling(final GridLayoutManager mLayoutManager) {

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

                VideoFeedAdapter adapter = (VideoFeedAdapter) recyclerView.getAdapter();
                int lastPosition = adapter.getLastBoundPosition();
                int totalNumberOfItems = adapter.getItemCount();
                int numColumns = mLayoutManager.getSpanCount();
                int loadBufferPosition = 1;

                // If user scrolled to bottom of page, fetch another page of items
                if (lastPosition >= totalNumberOfItems - numColumns - loadBufferPosition) {
                    new FetchItemsTask().execute(lastPosition + 1);
                }

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    private class FetchItemsTask extends AsyncTask<Integer, Void, List<KamcordVideo>> {

        @Override
        protected List<KamcordVideo> doInBackground(Integer... params) {
            return new VideoFeedPresenter().downloadVideos(lastPagedFetched);
        }

        @Override
        protected void onPostExecute(List<KamcordVideo> kamcordVideos) {

            Toast.makeText(getContext(), "Page " + lastPagedFetched, Toast.LENGTH_SHORT).show();

            if (lastPagedFetched > 1) {
                mVideos.addAll(kamcordVideos);
                mRecyclerView.getAdapter().notifyDataSetChanged();
            } else {
                mVideos = kamcordVideos;
                setupAdapter();
            }

            lastPagedFetched++;
        }
    }
}
