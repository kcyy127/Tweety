package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class TweetsDatabaseHelper extends SQLiteOpenHelper {

    private static TweetsDatabaseHelper sInstance;

    private static final String DATABASE_NAME = "tweetsDatabase";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_TWEETS = "tweets";
    private static final String TABLE_USERS = "users";

    // Tweet Table Columns
    private static final String KEY_TWEET_ID = "id";
    private static final String KEY_TWEET_USER_SCREEN_NAME_FK = "userScreenName";
    private static final String KEY_TWEET_BODY = "body";
    private static final String KEY_TWEET_CREATED_AT = "createdAt";
    private static final String KEY_TWEET_FAVORITE_COUNT = "favoriteCount";
    private static final String KEY_TWEET_RETWEET_COUNT = "retweetCount";
    private static final String KEY_TWEET_PHOTO_URL = "photoUrl";

    // User Table Columns
    private static final String KEY_USER_SCREEN_NAME = "screenName";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_PROFILE_IMAGE_URL = "profileImageUrl";
    private static final String KEY_USER_VERIFIED = "verified";

    public static synchronized TweetsDatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new TweetsDatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    private TweetsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TWEETS_TABLE = "CREATE TABLE " + TABLE_TWEETS +
                "(" +
                KEY_TWEET_ID + " INTEGER PRIMARY KEY," + // Define a primary key
                KEY_TWEET_USER_SCREEN_NAME_FK + " INTEGER REFERENCES " + TABLE_USERS + "," + // Define a foreign key
                KEY_TWEET_BODY + " TEXT," +
                KEY_TWEET_CREATED_AT + " TEXT," +
                KEY_TWEET_FAVORITE_COUNT + " INTEGER," +
                KEY_TWEET_RETWEET_COUNT + " INTEGER," +
                KEY_TWEET_PHOTO_URL + " TEXT" +
                ")";

        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS +
                "(" +
                KEY_USER_SCREEN_NAME + " INTEGER PRIMARY KEY," +
                KEY_USER_NAME + " TEXT," +
                KEY_USER_PROFILE_IMAGE_URL + " TEXT," +
                KEY_USER_VERIFIED + " INTEGER" +
                ")";

        db.execSQL(CREATE_TWEETS_TABLE);
        db.execSQL(CREATE_USERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TWEETS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
            onCreate(db);
        }
    }
}
