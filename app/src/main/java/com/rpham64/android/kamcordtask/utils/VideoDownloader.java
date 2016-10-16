package com.rpham64.android.kamcordtask.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.rpham64.android.kamcordtask.ui.VideoFeedPresenter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Dedicated background thread that acts as a message loop for fetching
 * flickr photos.
 */
public class VideoDownloader<T> extends HandlerThread {

    private static final String TAG = VideoDownloader.class.getName();

    // Download request identifier (also the "what" of a message)
    private static final int MESSAGE_DOWNLOAD = 0;

    private LruCache<String, Bitmap> mCache;

    private Handler mRequestHandler;                // From background thread
    private ConcurrentMap<T, String> mRequestMap    // Used to store and retrieve URL from request
            = new ConcurrentHashMap<>();

    private Handler mResponseHandler;                                  // From main thread
    private VideoDownloadListener<T> mVideoDownloadListener;   // Handles downloaded image

    public VideoDownloader(Handler responseHandler) {
        super(TAG);

        mResponseHandler = responseHandler;

        // Gets max available VM memory.
        // Exceeding this amount throws an OutOfMemory exception.
        // Stored in kilobytes as LruCache takes an int in its constructor.
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8 of available memory for memory cache
        int cacheSize = maxMemory / 8;

        mCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount() / 1024;
            }
        };

    }

    /**
     * Looper class that handles messages and sends them to their target (Handler)
     *
     * Called before the Looper checks the queue for the first time
     */
    @Override
    protected void onLooperPrepared() {

        mRequestHandler = new Handler() {

            /**
             * Called when a download message is pulled off queue and
             * ready to be processed
             *
             * @param msg
             */
            @Override
            public void handleMessage(Message msg) {

                if (msg.what == MESSAGE_DOWNLOAD) {

                    T target = (T) msg.obj;

                    Log.i(TAG, "Got a request for URL: " + mRequestMap.get(target));

                    handleRequest(target);
                }

            }
        };

    }

    /**
     * Sends message from url to its target handler
     *
     * @param target
     * @param url
     */
    public void queueThumbnail(T target, String url) {
        Log.i(TAG, "Got a URL: " + url);

        if (url == null) {
            mRequestMap.remove(target);
        } else {
            mRequestMap.put(target, url);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target)
                    .sendToTarget();
        }

    }

    /**
     * Clears message queue
     *
     * Required to avoid error when VideoDownloader holds onto invalid
     * VideoFeedHolder on configuration change (ie. rotation)
     */
    public void clearQueue() {
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
    }

    /**
     * Uses VideoFeedPresenter to download bytes from URL and turn them into a bitmap
     *
     * "Downloading" process
     *
     * @param target
     */
    private void handleRequest(final T target) {

        try {

            final String url = mRequestMap.get(target);
            final Bitmap bitmap;

            // Check: url exists in request map
            if (url == null) return;

            // Add new Bitmap to cache if the cache does not have it
            if (mCache.get(url) == null) {

                addBitmapToCache(url);

            }

            bitmap = getBitmapFromCache(url);

            /**
             * Posts downloaded image to UI
             */
            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {

                    // Check: Correct image (since RecyclerView recycles views and requests a
                    // different URL for PhotoHolder after downloading a bitmap
                    if (mRequestMap.get(target) != url) return;

                    // Remove PhotoHolder-URL mapping from requestMap and
                    // add bitmap to the UI
                    mRequestMap.remove(target);
                    mVideoDownloadListener.onThumbnailDownloadeded(target, bitmap);
                }
            });

        } catch (IOException ioe) {
            Log.e(TAG, "Error downloading image", ioe);
        }

    }

    @NonNull
    private void addBitmapToCache(String url) throws IOException {

        // Retrieve data from url in bytes and convert it into a bitmap
        byte[] bitmapBytes = new VideoFeedPresenter().getUrlBytes(url);
        Bitmap bitmap = BitmapFactory
                .decodeByteArray(bitmapBytes, 0, bitmapBytes.length);

        Log.i(TAG, "Bitmap created");

        // Add Bitmap and its url to cache
        mCache.put(url, bitmap);

        Log.i(TAG, "Bitmap added to cache");
    }

    private Bitmap getBitmapFromCache(String url) {

        Bitmap bitmap = mCache.get(url);

        Log.i(TAG, "Retrieved bitmap from cache");

        return bitmap;
    }

    public void setVideoDownloadListener(VideoDownloadListener<T> listener) {
        mVideoDownloadListener = listener;
    }

    public interface VideoDownloadListener<T> {
        void onThumbnailDownloadeded(T target, Bitmap thumbnail);
        void onVideoDownloaded(T target, String url);
    }
}
