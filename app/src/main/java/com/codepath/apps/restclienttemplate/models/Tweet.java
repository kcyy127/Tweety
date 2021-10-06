package com.codepath.apps.restclienttemplate.models;

import com.codepath.apps.restclienttemplate.TimeFormatter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

@Parcel
public class Tweet {
    public long id;
    public String body;
    public String createdAt;
    public User user;
    public int favoriteCount;
    public int retweetCount;
    public String photoUrl = "";

    public static Tweet fromJson(JSONObject jsonObject) throws JSONException {
        Tweet tweet = new Tweet();
        tweet.body = jsonObject.getString("full_text");
        tweet.createdAt = jsonObject.getString("created_at");
        tweet.id = jsonObject.getLong("id");
        tweet.user = User.fromJson(jsonObject.getJSONObject("user"));
        tweet.favoriteCount = jsonObject.getInt("favorite_count");
        tweet.retweetCount = jsonObject.getInt("retweet_count");

        if (jsonObject.has("extended_entities")) {
            JSONObject ext = jsonObject.getJSONObject("extended_entities");
            if (ext.has("media")) {
                JSONArray media = ext.getJSONArray("media");
                int i = 0;
                while (i < media.length()) {
                    if (media.getJSONObject(i).getString("type").equals("photo")) {
                        tweet.photoUrl = media.getJSONObject(i).getString("media_url_https");
                        break;
                    }
                    i++;
                }
            }
        }

        return tweet;
    }

    public static List<Tweet> fromJsonArray(JSONArray jsonArray) throws JSONException {
        List<Tweet> tweets = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            tweets.add(fromJson(jsonArray.getJSONObject(i)));
        }
        return tweets;
    }

    public String getFormattedTimestamp() {
        return TimeFormatter.getTimeDifference(createdAt);
    }
}
