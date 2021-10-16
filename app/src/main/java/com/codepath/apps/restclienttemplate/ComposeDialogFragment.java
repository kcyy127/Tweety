package com.codepath.apps.restclienttemplate;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.codepath.apps.restclienttemplate.activities.ComposeActivity;
import com.codepath.apps.restclienttemplate.activities.TimelineActivity;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import okhttp3.Headers;

public class ComposeDialogFragment extends DialogFragment {
    public static final String TAG = "ComposeDialogFragment";
    private static final String LOG_TAG = "ComposeDialogFragment";
    public static final int MAX_TWEET_LENGTH = 140;

    private String name;
    private String screenName;
    private String profileUrl;
    private String content;

    private EditText etCompose;

    private TwitterClient client;

    public ComposeDialogFragment() {
    }

    public static ComposeDialogFragment newInstance(String name, String screenName, String profileUrl) {

        Bundle args = new Bundle();
        args.putString("name", name);
        args.putString("screenName", screenName);
        args.putString("profileUrl", profileUrl);

        ComposeDialogFragment fragment = new ComposeDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static ComposeDialogFragment newInstance(String name, String screenName, String profileUrl, String content) {

        Bundle args = new Bundle();
        args.putString("name", name);
        args.putString("screenName", screenName);
        args.putString("profileUrl", profileUrl);
        args.putString("content", content);

        ComposeDialogFragment fragment = new ComposeDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        client = TwitterApp.getRestClient(getContext());

        this.name = getArguments().getString("name");
        this.screenName = getArguments().getString("screenName");
        this.profileUrl = getArguments().getString("profileUrl");

        if (getArguments().getString("content") != null) {
            this.content = getArguments().getString("content");
        }
    }

    @NonNull
    @NotNull
    @Override
    public Dialog onCreateDialog(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        View composeView = LayoutInflater.from(getActivity()).inflate(
                            R.layout.fragment_compose, null);
        TextView tvName = composeView.findViewById(R.id.tvName);
        tvName.setText(name);

        TextView tvScreenName = composeView.findViewById(R.id.tvScreenName);
        tvScreenName.setText("@" + screenName);

        Glide.with(getActivity())
                .load(profileUrl)
                .fitCenter()
                .transform(new CircleCrop())
                .into((ImageView) composeView.findViewById(R.id.ivProfileImage));

        this.etCompose = composeView.findViewById(R.id.etCompose);

        if (content != null) {
            this.etCompose.setText(content);
        }

        Button btnTweet = composeView.findViewById(R.id.btnTweet);
        btnTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(LOG_TAG, "Clicked!");
                String tweetContent = etCompose.getText().toString();
                if (tweetContent.isEmpty()) {
                    Log.d(LOG_TAG, "It's empty!");
                    Toast.makeText(getContext(), "It's empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (tweetContent.length() > MAX_TWEET_LENGTH) {
                    Toast.makeText(getContext(), "Too long", Toast.LENGTH_SHORT).show();
                    return;
                }
                client.publishTweet(tweetContent, new JsonHttpResponseHandler() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.i(LOG_TAG, "Successfully tweeted");
                        try {
                            Tweet tweet = Tweet.fromJson(json.jsonObject);
                            Log.i(LOG_TAG, tweet.body);

                            TweetActionListener listener = (TweetActionListener) getActivity();
                            Tweet newTweet = Tweet.fromJson(json.jsonObject);
                            listener.onTweet(newTweet);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        dismiss();
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.e(LOG_TAG, "Failed to tweet", throwable);
                        dismiss();
                    }
                });
            }
        });

        alertDialogBuilder.setView(composeView);
        AlertDialog alertDialog = alertDialogBuilder.create();



        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        return alertDialog;
    }


    public interface TweetActionListener {
        void onTweet(Tweet tw);
    }
}
