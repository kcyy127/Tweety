package com.codepath.apps.restclienttemplate.models;

import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey.Action;

import com.codepath.apps.restclienttemplate.TimeFormatter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Parcel
@Entity(foreignKeys = @ForeignKey(entity = User.class,
        parentColumns = "id",
        childColumns = "userId",
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE), tableName = "tweets")
public class Tweet {
    @ColumnInfo
    @PrimaryKey
    public long id;
    @ColumnInfo
    public String body;
    @ColumnInfo
    public String createdAt;
    @ColumnInfo
    public long createdMilli;

    @ColumnInfo
    public int favoriteCount;
    @ColumnInfo
    public int retweetCount;
    @ColumnInfo
    public String photoUrl = "";

    @ColumnInfo
    public long userId;

    @Ignore
    public User user;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static Tweet fromJson(JSONObject jsonObject) throws JSONException {
        Tweet tweet = new Tweet();
        tweet.id = jsonObject.getLong("id");
        tweet.body = jsonObject.getString("full_text");
        tweet.createdAt = jsonObject.getString("created_at");

        String date_string = tweet.createdAt;
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(date_string,
                DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss Z yyyy"));
        tweet.createdMilli = zonedDateTime.toInstant().toEpochMilli();

        tweet.favoriteCount = jsonObject.getInt("favorite_count");
        tweet.retweetCount = jsonObject.getInt("retweet_count");

        tweet.user = User.fromJson(jsonObject.getJSONObject("user"));
        tweet.userId = tweet.user.id;

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
