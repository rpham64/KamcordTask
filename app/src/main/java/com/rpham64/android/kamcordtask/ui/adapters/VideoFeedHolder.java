package com.rpham64.android.kamcordtask.ui.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.rpham64.android.kamcordtask.R;
import com.rpham64.android.kamcordtask.models.KamcordVideo;

import java.io.InputStream;

public class VideoFeedHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    private static final String TAG = VideoFeedHolder.class.getName();

    private Context mContext;

    private ImageView imgThumbnail;
    private VideoView videoView;
    private TextView txtHeartCount;

    public VideoFeedHolder(Context context, View itemView) {
        super(itemView);
        this.mContext = context;

        imgThumbnail = (ImageView) itemView.findViewById(R.id.feed_video_thumbnail);
        videoView = (VideoView) itemView.findViewById(R.id.feed_video);
        txtHeartCount = (TextView) itemView.findViewById(R.id.feed_heart_count);

        itemView.setOnClickListener(this);
    }

    public void bindItem(KamcordVideo kamcordVideo) {

        new DownloadImageTask(imgThumbnail).execute(kamcordVideo.getThumbnail());

        videoView.setVideoURI(Uri.parse(kamcordVideo.getUrl()));

        Log.i(TAG, "Thumbnail: " + kamcordVideo.getThumbnail());
        Log.i(TAG, "Url: " + kamcordVideo.getUrl());
        Log.i(TAG, "Heart Count: " + kamcordVideo.getHeartCount());

        txtHeartCount.setText(Integer.toString(kamcordVideo.getHeartCount()));
    }

    @Override
    public void onClick(View v) {
        Toast.makeText(mContext, "Video clicked", Toast.LENGTH_SHORT).show();
        imgThumbnail.setVisibility(View.GONE);
        videoView.setVisibility(View.VISIBLE);
        videoView.start();
    }

    /**
     * Downloads image from url in background and displays on imgThumbnail ImageView
     */
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
