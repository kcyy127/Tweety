package com.codepath.apps.restclienttemplate;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.User;

import java.util.ArrayList;
import java.util.List;

public class TweetsDatabaseHelper extends SQLiteOpenHelper {

    private static TweetsDatabaseHelper sInstance;
    private static final String LOG_TAG = "TweetsDatabaseHelper";

    private static final String DATABASE_NAME = "tweetsDatabase";
    private static final int DATABASE_VERSION = 3;

    // Table Names
    private static final String TABLE_TWEETS = "tweets";
    private static final String TABLE_USERS = "users";

    // Tweet Table Columns
    private static final String KEY_TWEET_ID = "id";
    private static final String KEY_TWEET_BODY = "body";
    private static final String KEY_TWEET_CREATED_AT = "createdAt";
    private static final String KEY_TWEET_CREATED_MILLI = "createdMilli";
    private static final String KEY_TWEET_FAVORITE_COUNT = "favoriteCount";
    private static final String KEY_TWEET_RETWEET_COUNT = "retweetCount";
    private static final String KEY_TWEET_PHOTO_URL = "photoUrl";
    private static final String KEY_TWEET_USER_ID_FK = "userId";


    // User Table Columns
    private static final String KEY_USER_ID = "id";
    private static final String KEY_USER_NAME = "name";
    private static final String KEY_USER_SCREEN_NAME = "screenName";
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
                KEY_TWEET_USER_ID_FK + " INTEGER REFERENCES " + TABLE_USERS + "," + // Define a foreign key
                KEY_TWEET_BODY + " TEXT," +
                KEY_TWEET_CREATED_AT + " TEXT," +
                KEY_TWEET_CREATED_MILLI + " INTEGER," +
                KEY_TWEET_FAVORITE_COUNT + " INTEGER," +
                KEY_TWEET_RETWEET_COUNT + " INTEGER," +
                KEY_TWEET_PHOTO_URL + " TEXT" +
                ")";

        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS +
                "(" +
                KEY_USER_ID + " INTEGER PRIMARY KEY," +
                KEY_USER_SCREEN_NAME + " TEXT," +
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

    public void addTweet(Tweet tweet) {
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            long userId = addOrUpdateUser(tweet.user);

            ContentValues values = new ContentValues();
            values.put(KEY_TWEET_USER_ID_FK, userId);
            values.put(KEY_TWEET_ID, tweet.id);
            values.put(KEY_TWEET_BODY, tweet.body);
            values.put(KEY_TWEET_CREATED_AT, tweet.createdAt);
            values.put(KEY_TWEET_CREATED_MILLI, tweet.createdMilli);
            values.put(KEY_TWEET_FAVORITE_COUNT, tweet.favoriteCount);
            values.put(KEY_TWEET_RETWEET_COUNT, tweet.retweetCount);
            values.put(KEY_TWEET_PHOTO_URL, tweet.photoUrl);

            db.insertOrThrow(TABLE_TWEETS, null, values);
            db.setTransactionSuccessful();
            Log.d(LOG_TAG, String.valueOf(tweet.id));
        } catch (Exception e) {
            Log.d(LOG_TAG, "Error while trying to add tweet to database");
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public long addOrUpdateUser(User user) {
        SQLiteDatabase db = getWritableDatabase();
        long userId = -1;

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
//            values.put(KEY_USER_ID, user.id);
            values.put(KEY_USER_NAME, user.name);
            values.put(KEY_USER_SCREEN_NAME, user.screenName);
            values.put(KEY_USER_PROFILE_IMAGE_URL, user.profileImageUrl);
            values.put(KEY_USER_VERIFIED, user.verified);

            // update user in case user already exists
            // assumes user name is unique
            int rows = db.update(TABLE_USERS, values,
                    KEY_USER_NAME + "= ?",
                    new String[]{user.name});

            // check if update succeeded
            if (rows == 1) {
                // get primary key of user updated
                String usersSelectQuery = String.format("SELECT %s FROM %s WHERE %s = ?",
                        KEY_USER_ID, TABLE_USERS, KEY_USER_NAME);
                Cursor cursor = db.rawQuery(usersSelectQuery, new String[]{String.valueOf(user.name)});
                try {
                    if (cursor.moveToFirst()) {
                        userId = cursor.getInt(0);
                        db.setTransactionSuccessful();
                    }
                } finally {
                    if (cursor != null && !cursor.isClosed()) {
                        cursor.close();
                    }
                }
            } else {
                // user with this username did not exist
                // insert new user
                userId = db.insertOrThrow(TABLE_USERS, null ,values);
                db.setTransactionSuccessful();
            }
        } catch (Exception e) {
            Log.d(LOG_TAG, "Error while trying to add or update user");
        } finally {
            db.endTransaction();
        }
        return userId;
    }

    public List<Tweet> getAllTweets() {
        List<Tweet> tweets = new ArrayList<>();

        String TWEETS_SELECT_QUERY =
                String.format("SELECT * FROM %s LEFT OUTER JOIN %s ON %s.%s = %s.%s ORDER BY %s DESC",
                        TABLE_TWEETS,
                        TABLE_USERS,
                        TABLE_TWEETS, KEY_TWEET_USER_ID_FK,
                        TABLE_USERS, KEY_USER_ID,
                        KEY_TWEET_CREATED_MILLI);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(TWEETS_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    User newUser = new User();
                    newUser.name = cursor.getString(cursor.getColumnIndex(KEY_USER_NAME));
                    newUser.screenName = cursor.getString(cursor.getColumnIndex(KEY_USER_SCREEN_NAME));
                    newUser.profileImageUrl = cursor.getString(cursor.getColumnIndex(KEY_USER_PROFILE_IMAGE_URL));
                    newUser.verified = cursor.getInt(cursor.getColumnIndex(KEY_USER_VERIFIED));

                    Tweet newTweet = new Tweet();
                    newTweet.body = cursor.getString(cursor.getColumnIndex(KEY_TWEET_BODY));
                    newTweet.createdAt = cursor.getString(cursor.getColumnIndex(KEY_TWEET_CREATED_AT));
                    newTweet.createdMilli = cursor.getLong(cursor.getColumnIndex(KEY_TWEET_CREATED_MILLI));
                    newTweet.favoriteCount = cursor.getInt(cursor.getColumnIndex(KEY_TWEET_FAVORITE_COUNT));
                    newTweet.retweetCount = cursor.getInt(cursor.getColumnIndex(KEY_TWEET_RETWEET_COUNT));
                    newTweet.photoUrl = cursor.getString(cursor.getColumnIndex(KEY_TWEET_PHOTO_URL));
                    newTweet.user = newUser;
                    newTweet.userId = cursor.getLong(cursor.getColumnIndex(KEY_TWEET_USER_ID_FK));
                    tweets.add(newTweet);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(LOG_TAG, "Error while trying to get posts from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return tweets;
    }

    public int updateUserProfilePicture(User user) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_USER_PROFILE_IMAGE_URL, user.profileImageUrl);

        return db.update(TABLE_USERS, values,
                KEY_USER_NAME + " = ?",
                new String[] {String.valueOf(user.name)});
    }

    public void deleteAllTweetsAndUsers() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_TWEETS, null, null);
            db.delete(TABLE_USERS, null, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(LOG_TAG, "Error while trying to delete all posts and users");
        } finally {
            db.endTransaction();
        }
    }

    public void loadFromList(List<Tweet> tweets) {
        for (int i = 0; i< tweets.size(); i++) {
            addTweet(tweets.get(i));
        }
    }
}
