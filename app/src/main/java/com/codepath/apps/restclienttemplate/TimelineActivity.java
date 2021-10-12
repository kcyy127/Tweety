package com.codepath.apps.restclienttemplate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.Toolbar;

import com.codepath.apps.restclienttemplate.databinding.ActivityTimelineBinding;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class TimelineActivity extends AppCompatActivity {
    private static final String LOG_TAG = "TimelineActivity";
    private static final int REQUEST_CODE_COMPOSE_TWEET = 0;

    private ActivityTimelineBinding binding;

    private TwitterClient client;

    private List<Tweet> tweets;
    private TweetsAdapter adapter;
    private EndlessRecyclerViewScrollListener scrollListener;

    private TweetsDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_timeline);

        client = TwitterApp.getRestClient(this);

        binding.swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        binding.swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(LOG_TAG, "Fetching new data!");
                populateHomeTimeline();
            }
        });

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        dbHelper = TweetsDatabaseHelper.getInstance(this);
        tweets = dbHelper.getAllTweets();
//        Log.i(LOG_TAG, tweets.get(0).body);

        adapter = new TweetsAdapter(this, tweets);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        binding.rvTweets.setLayoutManager(layoutManager);
        binding.rvTweets.setAdapter(adapter);

        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Log.i(LOG_TAG, "onLoadMore: " + page);
                loadMoreData();
            }
        };
        binding.rvTweets.addOnScrollListener(scrollListener);

        populateHomeTimeline();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.compose:
                Toast.makeText(this, "Compose pressed!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, ComposeActivity.class);
                startActivityForResult(intent, REQUEST_CODE_COMPOSE_TWEET);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        if (requestCode == REQUEST_CODE_COMPOSE_TWEET && resultCode == RESULT_OK) {
            Tweet tweet = Parcels.unwrap(data.getParcelableExtra("tweet"));
            tweets.add(0, tweet);
            adapter.notifyDataSetChanged();
            dbHelper.addTweet(tweet);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void loadMoreData() {
        // 1. Send an API request to retrieve appropriate paginated data
        client.getNextPageOfTweets(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(LOG_TAG, "onSuccess for loadMoreData!" + json.toString());
                // 2. Deserialize and construct new model objects from the API response
                JSONArray jsonArray = json.jsonArray;
                try {
                    List<Tweet> extraTweets = Tweet.fromJsonArray(jsonArray);

                    dbHelper.loadFromList(extraTweets);

                    // use livedata someday
                    adapter.addAll(extraTweets);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "JSON exception", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(LOG_TAG, "onFailure for loadMoreData!", throwable);
            }
        }, tweets.get(tweets.size()-1).id);

    }

    private void populateHomeTimeline() {
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(LOG_TAG, "onSuccess!" + json.toString());
                JSONArray jsonArray = json.jsonArray;
                try {
                    adapter.clear();
                    dbHelper.deleteAllTweetsAndUsers();

                    List<Tweet> newTweets = Tweet.fromJsonArray(jsonArray);

                    dbHelper.loadFromList(newTweets);

                    adapter.addAll(newTweets);
                    binding.swipeContainer.setRefreshing(false);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "JSON exception", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(LOG_TAG, "client getHomeTimeline Failure ", throwable);
                binding.swipeContainer.setRefreshing(false);
            }
        });
    }
}