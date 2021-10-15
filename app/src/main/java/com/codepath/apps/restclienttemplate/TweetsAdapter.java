package com.codepath.apps.restclienttemplate;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.codepath.apps.restclienttemplate.activities.TimelineActivity;
import com.codepath.apps.restclienttemplate.activities.TweetDetailActivity;
import com.codepath.apps.restclienttemplate.databinding.ItemTweetImageBinding;
import com.codepath.apps.restclienttemplate.databinding.ItemTweetTextBinding;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.jetbrains.annotations.NotNull;
import org.parceler.Parcels;

import java.util.List;

import okhttp3.Headers;

public class TweetsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String LOG_TAG = "TweetsAdapter";

    private Context context;
    private List<Tweet> tweets;

    private TwitterClient client;

    private static final int TYPE_TEXT = 0;
    private static final int TYPE_IMAGE = 1;
    private static final int TYPE_GIF = 2;
    private static final int TYPE_VIDEO = 3;

    public TweetsAdapter(Context context, List<Tweet> tweets) {
        this.context = context;
        this.tweets = tweets;
        this.client = TwitterApp.getRestClient(context);
    }

    @NonNull
    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            default:
                ItemTweetTextBinding bindingText = ItemTweetTextBinding.inflate(layoutInflater, parent, false);
                viewHolder = new mViewHolderText(bindingText);
                break;
            case TYPE_IMAGE:
                ItemTweetImageBinding bindingImage = ItemTweetImageBinding.inflate(layoutInflater, parent, false);
                viewHolder = new mViewHolderImage(bindingImage);
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {
        Tweet tweet = tweets.get(position);
        int viewType = holder.getItemViewType();

        switch (viewType) {
            case TYPE_IMAGE:
                mViewHolderImage viewHolderImage = (mViewHolderImage) holder;
                viewHolderImage.bind(tweet);
                break;
            default:
                mViewHolderText viewHolderText = (mViewHolderText) holder;
                viewHolderText.bind(tweet);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }

    @Override
    public int getItemViewType(int position) {
        Tweet tweet = tweets.get(position);

        if (!tweet.photoUrl.equals("")) {
            return TYPE_IMAGE;
        } else {
            return TYPE_TEXT;
        }

    }

    public void clear() {
        tweets.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Tweet> tweetList) {
        tweets.addAll(tweetList);
        notifyDataSetChanged();
    }

    public String formatNumber(int i) {
        if (i < 1000) {
            return String.valueOf(i);
        } else if (i < 1_000_000) {
            double f1 = i /1000.0;
            return String.format("%.1fk", f1);
        } else if (i < 1_000_000_000){
            double f2 = i / 1000_000.0;
            return String.format("%.1fm", f2);
        } else {
            return "--";
        }
    }

    public class mViewHolderText extends RecyclerView.ViewHolder {
        private ItemTweetTextBinding bindingText;

        public mViewHolderText(@NonNull @NotNull ItemTweetTextBinding bindingText) {
            super(bindingText.getRoot());
            this.bindingText = bindingText;
        }

        public void bind(Tweet tweet) {
            bindingText.tvBody.setText(tweet.body);
            bindingText.tvName.setText(tweet.user.name);
            bindingText.tvScreenName.setText("@" + tweet.user.screenName);
            bindingText.tvLikes.setText(formatNumber(tweet.favoriteCount));
            bindingText.tvRetweets.setText(formatNumber(tweet.retweetCount));
            Glide.with(context)
                    .load(tweet.user.profileImageUrl)
                    .fitCenter()
                    .transform(new CircleCrop())
                    .into(bindingText.ivProfileImage);
            bindingText.tvTime.setText(tweet.getFormattedTimestamp());

            if (tweet.user.verified == 1) {
                bindingText.ivVerified.setVisibility(View.VISIBLE);
            } else {
                bindingText.ivVerified.setVisibility(View.INVISIBLE);
            }
            bindingText.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, TweetDetailActivity.class);
                    intent.putExtra("tweet", Parcels.wrap(tweet));

                    Pair<View, String> p1 = Pair.create(bindingText.ivProfileImage, "ivProfileImage");
                    Pair<View, String> p2 = Pair.create(bindingText.tvBody, "tvBody");
                    Pair<View, String> p3 = Pair.create(bindingText.tvName, "tvName");
                    Pair<View, String> p4 = Pair.create(bindingText.tvScreenName, "tvScreenName");
                    Pair<View, String> p5 = Pair.create(bindingText.tvTime, "tvTime");
//                    Pair<View, String> p6 = Pair.create(bindingText.ivVerified, "ivVerified");
                    ActivityOptionsCompat options = ActivityOptionsCompat.
                            makeSceneTransitionAnimation((Activity) context, p1, p2, p3, p4, p5);

                    context.startActivity(intent, options.toBundle());
                }
            });

            setFavorites(bindingText.buttonLike, tweet);

        }
    }

    private void setFavorites(CheckBox button, Tweet tweet) {
        button.setOnCheckedChangeListener(null);
        button.setChecked(tweet.favorited);
        button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    client.likeTweet(tweet.id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.d(LOG_TAG, "Successfully liked " + tweet.body);
                            tweet.favorited = true;
                            notifyDataSetChanged();

                            ((TimelineActivity)context).dbUpdateFavorited(tweet);

                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.e(LOG_TAG, "Failed to like " + tweet.body, throwable);
                        }
                    });
                } else {
                    client.dislikeTweet(tweet.id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.d(LOG_TAG, "Successfully disliked " + tweet.body);
                            tweet.favorited = false;
                            notifyDataSetChanged();

                            ((TimelineActivity)context).dbUpdateFavorited(tweet);
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.e(LOG_TAG, "Failed to dislike " + tweet.body, throwable);
                        }
                    });
                }
            }
        });
    }

    public class mViewHolderImage extends RecyclerView.ViewHolder {
        private ItemTweetImageBinding bindingImage;

        public mViewHolderImage(@NonNull @NotNull ItemTweetImageBinding bindingImage) {
            super(bindingImage.getRoot());
            this.bindingImage = bindingImage;
        }

        public void bind(Tweet tweet) {
            bindingImage.tvBody.setText(tweet.body);
            bindingImage.tvName.setText(tweet.user.name);
            bindingImage.tvScreenName.setText("@" + tweet.user.screenName);
            bindingImage.tvLikes.setText(formatNumber(tweet.favoriteCount));
            bindingImage.tvRetweets.setText(formatNumber(tweet.retweetCount));
            Glide.with(context)
                    .load(tweet.user.profileImageUrl)
                    .fitCenter()
                    .transform(new CircleCrop())
                    .into(bindingImage.ivProfileImage);
            bindingImage.tvTime.setText(tweet.getFormattedTimestamp());
            Glide.with(context)
                    .load(tweet.photoUrl)
                    .fitCenter()
                    .transform(new RoundedCorners(48))
                    .into(bindingImage.ivMedia);
            if (tweet.user.verified == 1) {
                bindingImage.ivVerified.setVisibility(View.VISIBLE);
            } else {
                bindingImage.ivVerified.setVisibility(View.INVISIBLE);
            }

            bindingImage.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, TweetDetailActivity.class);
                    intent.putExtra("tweet", Parcels.wrap(tweet));

                    Pair<View, String> p1 = Pair.create(bindingImage.ivProfileImage, "ivProfileImage");
                    Pair<View, String> p2 = Pair.create(bindingImage.tvBody, "tvBody");
                    Pair<View, String> p3 = Pair.create(bindingImage.tvName, "tvName");
                    Pair<View, String> p4 = Pair.create(bindingImage.tvScreenName, "tvScreenName");
                    Pair<View, String> p5 = Pair.create(bindingImage.tvTime, "tvTime");
                    Pair<View, String> p6 = Pair.create(bindingImage.ivVerified, "ivVerified");
                    Pair<View, String> p7 = Pair.create(bindingImage.ivMedia, "ivMedia");
                    ActivityOptionsCompat options = ActivityOptionsCompat.
                            makeSceneTransitionAnimation((Activity) context, p1, p2, p3, p4, p5, p6, p7);

                    context.startActivity(intent, options.toBundle());
                }
            });

            setFavorites(bindingImage.buttonLike, tweet);

//            bindingImage.buttonLike.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
//                    if (isChecked) {
//                        client.likeTweet(tweet.id, new JsonHttpResponseHandler() {
//                            @Override
//                            public void onSuccess(int statusCode, Headers headers, JSON json) {
//                                Log.d(LOG_TAG, "Successfully liked " + tweet.body);
//                            }
//
//                            @Override
//                            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
//                                Log.e(LOG_TAG, "Failed to like " + tweet.body, throwable);
//                                bindingImage.buttonLike.setChecked(false);
//                            }
//                        });
//                    } else {
//                        client.dislikeTweet(tweet.id, new JsonHttpResponseHandler() {
//                            @Override
//                            public void onSuccess(int statusCode, Headers headers, JSON json) {
//                                Log.d(LOG_TAG, "Successfully disliked " + tweet.body);
//                            }
//
//                            @Override
//                            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
//                                Log.e(LOG_TAG, "Failed to dislike " + tweet.body, throwable);
//                                bindingImage.buttonLike.setChecked(true);
//                            }
//                        });
//                    }
//                }
//            });

        }
    }

//    public class mViewHolderVideo extends RecyclerView.ViewHolder {
//        View container;
//        ImageView ivProfileImage;
////        VideoView vvMedia;
//        ImageView ivVerified;
//        TextView tvBody;
//        TextView tvScreenName;
//        TextView tvName;
//        TextView tvTime;
//
//        public mViewHolderVideo(@NonNull @NotNull View itemView) {
//            super(itemView);
//            container = itemView.findViewById(R.id.container);
//            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
////            vvMedia = itemView.findViewById(R.id.vvMedia);
//            tvBody = itemView.findViewById(R.id.tvBody);
//            tvName = itemView.findViewById(R.id.tvName);
//            tvScreenName = itemView.findViewById(R.id.tvScreenName);
//            tvTime = itemView.findViewById(R.id.tvTime);
//            ivVerified = itemView.findViewById(R.id.ivVerified);
//        }
//
//        public void bind(Tweet tweet) {
//            tvBody.setText(tweet.body);
//            tvName.setText(tweet.user.name);
//            tvScreenName.setText("@" + tweet.user.screenName);
//            Glide.with(context)
//                    .load(tweet.user.profileImageUrl)
//                    .fitCenter()
//                    .transform(new CircleCrop())
//                    .into(ivProfileImage);
//            tvTime.setText(tweet.getFormattedTimestamp());
//
//            //videoview
////            vvMedia.setVideoPath("https://video.twimg.com/ext_tw_video/1444751963174981636/pu/vid/480x270/LwhtbAlU9EOReXdV.mp4?tag=12");
////            Log.d(LOG_TAG, tweet.media.get(0).url);
////            MediaController mediaController = new MediaController(context);
////            mediaController.setAnchorView(vvMedia);
////            vvMedia.setMediaController(mediaController);
////            vvMedia.requestFocus();
////            vvMedia.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
////                // Close the progress bar and play the video
////                public void onPrepared(MediaPlayer mp) {
////                    vvMedia.start();
////                }
////            });
//
////            Uri video = Uri.parse("https:\\/\\/video.twimg.com\\/ext_tw_video\\/1444751963174981636\\/pu\\/pl\\/5nV1gmnWEtCubrS5.m3u8?tag=12&container=fmp4");
////            vvMedia.setVideoURI(video);
////            vvMedia.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
////                @Override
////                public void onPrepared(MediaPlayer mp) {
////                    mp.setLooping(true);
////                    vvMedia.start();
////                }
////            });
//
//
//            if (tweet.user.verified) {
//                ivVerified.setVisibility(View.VISIBLE);
//            } else {
//                ivVerified.setVisibility(View.INVISIBLE);
//            }
//
//            container.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Intent intent = new Intent(context, TweetDetailActivity.class);
//                    intent.putExtra("tweet", Parcels.wrap(tweet));
//
//                    Pair<View, String> p1 = Pair.create(ivProfileImage, "ivProfileImage");
//                    Pair<View, String> p2 = Pair.create(tvBody, "tvBody");
//                    Pair<View, String> p3 = Pair.create(tvName, "tvName");
//                    Pair<View, String> p4 = Pair.create(tvScreenName, "tvScreenName");
//                    Pair<View, String> p5 = Pair.create(tvTime, "tvTime");
//                    Pair<View, String> p6 = Pair.create(ivVerified, "ivVerified");
////                    Pair<View, String> p7 = Pair.create(vvMedia, "vvMedia");
//                    ActivityOptionsCompat options = ActivityOptionsCompat.
//                            makeSceneTransitionAnimation((Activity) context, p1, p2, p3, p4, p5, p6);
//
//                    context.startActivity(intent, options.toBundle());
//                }
//            });
//        }
//    }


}


