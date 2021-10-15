package com.codepath.apps.restclienttemplate;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.codepath.apps.restclienttemplate.activities.ComposeActivity;
import com.codepath.apps.restclienttemplate.activities.TimelineActivity;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Headers;

public class ComposeDialogFragment extends DialogFragment {
    public static final String TAG = "ComposeDialogFragment";
    private static final String LOG_TAG = "ComposeDialogFragment";

    private TwitterClient client;

    private String name;
    private String screenName;
    private String profileUrl;

    public ComposeDialogFragment() {
        client = TwitterApp.getRestClient(getActivity());
        getUser();
    }

    private void getUser() {
        client.getUser(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                JSONObject userJsonObject = json.jsonObject;
                try {
                    name = userJsonObject.getString("name");
                    Log.d(LOG_TAG, name);
                    screenName = userJsonObject.getString("screen_name");
                    profileUrl = userJsonObject.getString("profile_image_url_https");
//                    binding.tvName.setText(name);
//                    binding.tvScreenName.setText("@" + screenName);
//                    Glide.with(ComposeActivity.this)
//                            .load(profileUrl)
//                            .fitCenter()
//                            .transform(new CircleCrop())
//                            .into(binding.ivProfileImage);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Json failure", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(LOG_TAG, "Failed to load user info", throwable);
            }
        });
    }


    @NonNull
    @NotNull
    @Override
    public Dialog onCreateDialog(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
//        Dialog dialog = super.onCreateDialog(savedInstanceState);
//        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
//        return dialog;

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        View composeView = LayoutInflater.from(getActivity()).inflate(
                            R.layout.fragment_compose, null);
        TextView tvName = composeView.findViewById(R.id.tvName);
        tvName.setText(name);
        alertDialogBuilder.setView(composeView);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        return alertDialog;
    }
}
