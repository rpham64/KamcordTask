package com.rpham64.android.kamcordtask.ui;

import android.content.res.Resources;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;

import com.rpham64.android.kamcordtask.models.KamcordVideo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rudolf on 10/14/2016.
 */

public class VideoFeedPresenter {

    private static final String TAG = VideoFeedPresenter.class.getName();

//    private static final String ENDPOINT = "https://api.staging.kamcord.com/v1/feed/set/featuredShots";
    private static final String ENDPOINT = "https://api.kamcord.com/v1/feed/set/featuredShots";

    public VideoFeedPresenter() {
    }

    public List<KamcordVideo> downloadVideos(int page) {

        List<KamcordVideo> videos = new ArrayList<>();

        try {

            String url = Uri.parse(ENDPOINT)
                    .buildUpon()
                    .appendQueryParameter("count", String.valueOf(20))
                    .build()
                    .toString();

            String json = getUrlString(url);

            Log.i(TAG, "Received JSON: " + json);

            parseItems(videos, new JSONObject(json));

        } catch (JSONException jsonException) {
            Log.e(TAG, "Failed to parse JSON", jsonException);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch videos", ioe);
        }

        return videos;
    }

    /**
     * Fetches raw data from URL and returns it as an array of bytes
     *
     * @param urlSpec
     * @return
     * @throws IOException
     */
    public static byte[] getUrlBytes(String urlSpec) throws IOException {

        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");

        // Header properties
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("accept-language", "en");
        connection.setRequestProperty("device-token", "abc123");
        connection.setRequestProperty("client-name", "android");

        try {

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            InputStream inputStream = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {

                throw new IOException(
                        connection.getResponseMessage() + ": with " + urlSpec);

            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];

            // Read in data to outputStream until connection runs out of data
            while ((bytesRead = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();

            return outputStream.toByteArray();

        } finally {
            connection.disconnect();
        }
    }

    /**
     * Converts result from getUrlBytes to a JSON String
     *
     * @param urlSpec
     * @return
     * @throws IOException
     */
    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    /**
     * Deserializes JSON into Java objects
     *
     * @param videos
     * @param jsonObject
     * @throws IOException
     * @throws JSONException
     */
    private void parseItems(List<KamcordVideo> videos, JSONObject jsonObject)
            throws IOException, JSONException {

        JSONArray cards = jsonObject.getJSONArray("groups")
                                    .getJSONObject(0)
                                    .getJSONArray("cards");

        for (int i = 0; i < cards.length(); ++i) {

            JSONObject card = cards.getJSONObject(i);
            JSONObject shotCardData = card.getJSONObject("shotCardData");

            KamcordVideo video = new KamcordVideo();

            try {

                Log.i(TAG, "HeartCount: " + shotCardData.getInt("heartCount"));
                Log.i(TAG, "String version: " + String.valueOf(shotCardData.getInt("heartCount")));

                setHeartCount(shotCardData, video);
                setThumbnail(shotCardData, video);
                setUrl(shotCardData, video);

            } catch (JSONException jsonEx) {
                Log.d(TAG, "JSONException in VideoFeedPresenter#parseItems: " + jsonEx.toString());
            }

            videos.add(video);
        }

    }

    private void setHeartCount(JSONObject shotCardData, KamcordVideo video) throws JSONException {
        video.setHeartCount(shotCardData.getInt("heartCount"));
    }

    private void setThumbnail(JSONObject shotCardData, KamcordVideo video) throws JSONException {
        // Thumbnail sizes: small, medium, large
        JSONObject shotThumbnail = shotCardData.getJSONObject("shotThumbnail");
        float density = Resources.getSystem().getDisplayMetrics().density * 160f;
        String thumbnail = null;

        // Set video thumbnail to appropriate size
        if (density == DisplayMetrics.DENSITY_LOW) {
            thumbnail = shotThumbnail.getString("small");

        } else if (density == DisplayMetrics.DENSITY_MEDIUM) {
            thumbnail = shotThumbnail.getString("medium");

        } else {
            thumbnail = shotThumbnail.getString("large");

        }

        video.setThumbnail(thumbnail);
    }

    private void setUrl(JSONObject shotCardData, KamcordVideo video) throws JSONException {
        JSONObject play = shotCardData.getJSONObject("play");
        video.setUrl(play.getString("mp4"));
    }
}
