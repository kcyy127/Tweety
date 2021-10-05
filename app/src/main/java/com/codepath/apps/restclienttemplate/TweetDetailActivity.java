package com.codepath.apps.restclienttemplate;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.codepath.apps.restclienttemplate.databinding.ActivityTweetDetailBinding;
import com.codepath.apps.restclienttemplate.models.Tweet;

import org.parceler.Parcels;

public class TweetDetailActivity extends AppCompatActivity {

    private ActivityTweetDetailBinding binding;
    private Tweet tweet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_tweet_detail);

        Intent intent = getIntent();
        tweet = Parcels.unwrap(intent.getParcelableExtra("tweet"));

        binding.tvName.setText(tweet.user.name);
        binding.tvScreenName.setText("@" + tweet.user.screenName);
        Glide.with(this)
                .load(tweet.user.profileImageUrl)
                .fitCenter()
                .transform(new CircleCrop())
                .into(binding.ivProfileImage);
        binding.tvBody.setText(tweet.body);
        binding.tvTime.setText(tweet.getFormattedTimestamp());
        if (tweet.user.verified) {
            binding.ivVerified.setVisibility(View.VISIBLE);
        } else {
            binding.ivVerified.setVisibility(View.INVISIBLE);
        }

        if (tweet.media.size() != 0) {
            if (tweet.media.get(0).mediaType.equals("photo")) {
                binding.ivMedia.setVisibility(View.VISIBLE);
                Glide.with(this)
                        .load(tweet.media.get(0).url)
                        .fitCenter()
                        .transform(new RoundedCorners(48))
                        .into(binding.ivMedia);
            }
        }

    }
}