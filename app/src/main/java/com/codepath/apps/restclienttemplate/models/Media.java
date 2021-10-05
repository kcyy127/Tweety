package com.codepath.apps.restclienttemplate.models;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

@Parcel
public class Media {
    public String url;
    public String mediaType;

    public static Media fromJson(JSONObject jsonObject) throws JSONException {
        Media media = new Media();
        media.mediaType = jsonObject.getString("type");
        switch (media.mediaType) {
            default:
                media.url = jsonObject.getString("media_url_https");
                break;
            case "video":
                media.url = jsonObject.getString("media_url_https");
//                Log.d("Media", media.mediaType + ";" + jsonObject.toString());
                JSONArray variants = jsonObject.getJSONObject("video_info").getJSONArray("variants");
//                Log.d("Media", variants.toString());
                int i = 0;;
                while (i < variants.length()) {
                    if (variants.getJSONObject(i).getString("content_type").equals("video/mp4")) {
                        media.url = variants.getJSONObject(i).getString("url");
                        break;
                    }
                    i++;
                }
                break;
        }
        return media;
    }

    public static List<Media> fromJsonArray(JSONArray jsonArray) throws JSONException {
        List<Media> medias = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            medias.add(fromJson(jsonArray.getJSONObject(i)));
        }
        return medias;
    }
}
