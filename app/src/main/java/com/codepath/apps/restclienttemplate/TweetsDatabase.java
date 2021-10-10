package com.codepath.apps.restclienttemplate;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.TweetDao;
import com.codepath.apps.restclienttemplate.models.User;

@Database(entities = {Tweet.class, User.class}, version = 1)
public abstract class TweetsDatabase extends RoomDatabase {

    public abstract TweetDao tweetDao();

    public static final String NAME = "tweetsDatabase";
}
