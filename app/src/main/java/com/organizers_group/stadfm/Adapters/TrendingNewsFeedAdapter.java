package com.organizers_group.stadfm.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.AccessToken;
import com.organizers_group.stadfm.Activities.ArticleDetailsActivity;
import com.organizers_group.stadfm.Data.Constants;
import com.organizers_group.stadfm.Model.NewsFeed;
import com.organizers_group.stadfm.R;
import com.organizers_group.stadfm.Utils.CustomJSONHelper;
import com.organizers_group.stadfm.Utils.RequestQueueSingleton;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.List;

import static com.facebook.FacebookSdk.getApplicationContext;

public class TrendingNewsFeedAdapter extends RecyclerView.Adapter<TrendingNewsFeedAdapter.ViewHolder> {
    private Activity context;
    private List<NewsFeed> postList;

    public TrendingNewsFeedAdapter(Activity context, List<NewsFeed> postList) {
        this.context = context;
        this.postList = postList;

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_trending_stories, parent, false);
        return new ViewHolder(view , context);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        NewsFeed newsFeed = postList.get(position);
        String posterLink = newsFeed.getPostImgUrl();

        holder.trendingCategory.setText(newsFeed.getCategory());

        try{
            // parse HTML Data to readable
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                holder.trendingTitle.setText(Html.fromHtml(newsFeed.getTitle() , Html.FROM_HTML_MODE_COMPACT));
                holder.trendingSummary.setText(Html.fromHtml(newsFeed.getDescription() , Html.FROM_HTML_MODE_COMPACT));
            }else {
                holder.trendingTitle.setText(Html.fromHtml(newsFeed.getTitle()));
                holder.trendingSummary.setText(Html.fromHtml(newsFeed.getDescription()));
            }
        }catch (Exception ignored) { }

        try {
            Picasso.get().load(posterLink).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).into(holder.poster);
        }catch (Exception e){
            Picasso.get().load(R.drawable.img_not_found).into(holder.poster);
        }

        // check if the topic from the favorite
        isItemChecked(newsFeed , holder.saveArticleChkBx);
    }

    @Override
    public int getItemCount() {        return postList.size();    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView poster;
        TextView trendingCategory;
        TextView trendingTitle;
        TextView trendingSummary;
        CheckBox saveArticleChkBx;
        ProgressBar savingPB;

        ViewHolder(View itemView, final Context context) {
            super(itemView);

            poster = itemView.findViewById(R.id.postImageView);
            trendingTitle = itemView.findViewById(R.id.trendingTitle);
            trendingSummary = itemView.findViewById(R.id.trendingSummary);
            trendingCategory = itemView.findViewById(R.id.trendingCategory);
            saveArticleChkBx = itemView.findViewById(R.id.saveArticleChkBx);
            savingPB = itemView.findViewById(R.id.savingPBTrending);

            // save or dismiss article from savedArticles
            saveArticleChkBx.setOnClickListener(v -> {
                savingPB.setVisibility(View.VISIBLE);

                NewsFeed newsFeed = postList.get(getAdapterPosition());

                String accessToken = AccessToken.getCurrentAccessToken().getToken();

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, Constants.USER_ACCESS_TOKEN + accessToken,
                        response -> {
                            try {
                                // fetch user id
                                int userID = response.getInt("wp_user_id");

                                if (saveArticleChkBx.isChecked()) {
                                    // add topic to favorite
                                    CustomJSONHelper.postArticleToFav(context , String.valueOf(userID), String.valueOf(newsFeed.getPostID()), saveArticleChkBx, savingPB);
                                } else {
                                    // use userID for getting the topic id
                                    CustomJSONHelper.RemoveUserSavedArticle(String.valueOf(userID), newsFeed , saveArticleChkBx , savingPB);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        },
                        error -> { })
                {
                    @Override
                    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                        try {
                            Cache.Entry cacheEntry = HttpHeaderParser.parseCacheHeaders(response);
                            if (cacheEntry == null) {
                                cacheEntry = new Cache.Entry();
                            }
                            final long cacheHitButRefreshed = 30 * 1000; // in 3 minutes cache will be hit, but also refreshed on background
                            final long cacheExpired = 24 * 60 * 60 * 1000; // in 24 hours this cache entry expires completely
                            long now = System.currentTimeMillis();
                            final long softExpire = now + cacheHitButRefreshed;
                            final long ttl = now + cacheExpired;
                            cacheEntry.data = response.data;
                            cacheEntry.softTtl = softExpire;
                            cacheEntry.ttl = ttl;
                            String headerValue;
                            headerValue = response.headers.get("Date");
                            if (headerValue != null) {
                                cacheEntry.serverDate = HttpHeaderParser.parseDateAsEpoch(headerValue);
                            }
                            headerValue = response.headers.get("Last-Modified");
                            if (headerValue != null) {
                                cacheEntry.lastModified = HttpHeaderParser.parseDateAsEpoch(headerValue);
                            }
                            cacheEntry.responseHeaders = response.headers;
                            final String jsonString = new String(response.data,
                                    HttpHeaderParser.parseCharset(response.headers));
                            return Response.success(new JSONObject(jsonString), cacheEntry);
                        } catch (UnsupportedEncodingException | JSONException e) {
                            return Response.error(new ParseError(e));
                        }
                    }
                };

                jsonObjectRequest.setRetryPolicy(new RetryPolicy() {
                    @Override
                    public int getCurrentTimeout() {
                        return 50000;
                    }

                    @Override
                    public int getCurrentRetryCount() {
                        return 50000;
                    }

                    @Override
                    public void retry(VolleyError error) {

                    }
                });
                RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);

            });

            itemView.setOnClickListener(v -> {
                // move to next activity!
                NewsFeed newsFeed = postList.get(getAdapterPosition());

                if (saveArticleChkBx.isChecked()) newsFeed.setChosenArticle(true);

                Intent intent = new Intent(context, ArticleDetailsActivity.class);
                intent.putExtra("post", newsFeed);
                context.startActivity(intent);
            });
        }
    }


    private void isItemChecked(NewsFeed newsFeedIsChecked , CheckBox checkBox){

        String accessToken = AccessToken.getCurrentAccessToken().getToken();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, Constants.USER_ACCESS_TOKEN + accessToken,
                response -> {
                    try {
                        // fetch user id
                        int userID = response.getInt("wp_user_id");

                        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, "https://stadfm.com/wp-json/org/v1/user_articals/" /*Constants.USER_ARTICLES */+ String.valueOf(userID),
                                responseGet -> {
                                    try {
                                        for (int i = 0; i < responseGet.length(); i++) {
                                            JSONObject topicObjts = responseGet.getJSONObject(i);
                                            if (newsFeedIsChecked.getPostID() == (topicObjts.getInt("id"))){
                                                checkBox.setBackground(context.getResources().getDrawable(R.drawable.favorite_article));
                                                checkBox.setChecked(true);
                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }, error -> { })
                        {
                            @Override
                            protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
                                try {
                                    Cache.Entry cacheEntry = HttpHeaderParser.parseCacheHeaders(response);
                                    if (cacheEntry == null) {
                                        cacheEntry = new Cache.Entry();
                                    }
                                    final long cacheHitButRefreshed = 2 * 1000; // in 5 seconds cache will be hit, but also refreshed on background
                                    final long cacheExpired = 10 * 1000; // in 10 seconds this cache entry expires completely
                                    long now = System.currentTimeMillis();
                                    final long softExpire = now + cacheHitButRefreshed;
                                    final long ttl = now + cacheExpired;
                                    cacheEntry.data = response.data;
                                    cacheEntry.softTtl = softExpire;
                                    cacheEntry.ttl = ttl;
                                    String headerValue;
                                    headerValue = response.headers.get("Date");
                                    if (headerValue != null) {
                                        cacheEntry.serverDate = HttpHeaderParser.parseDateAsEpoch(headerValue);
                                    }
                                    headerValue = response.headers.get("Last-Modified");
                                    if (headerValue != null) {
                                        cacheEntry.lastModified = HttpHeaderParser.parseDateAsEpoch(headerValue);
                                    }
                                    cacheEntry.responseHeaders = response.headers;
                                    final String jsonString = new String(response.data,
                                            HttpHeaderParser.parseCharset(response.headers));
                                    return Response.success(new JSONArray(jsonString), cacheEntry);
                                } catch (UnsupportedEncodingException | JSONException e) {
                                    return Response.error(new ParseError(e));
                                }
                            }
                        };

                        jsonArrayRequest.setRetryPolicy(new RetryPolicy() {
                            @Override
                            public int getCurrentTimeout() {
                                return 50000;
                            }

                            @Override
                            public int getCurrentRetryCount() {
                                return 50000;
                            }

                            @Override
                            public void retry(VolleyError error) {

                            }
                        });
                        RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonArrayRequest);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> { })
        {
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    Cache.Entry cacheEntry = HttpHeaderParser.parseCacheHeaders(response);
                    if (cacheEntry == null) {
                        cacheEntry = new Cache.Entry();
                    }
                    final long cacheHitButRefreshed = 30 * 1000; // in 3 minutes cache will be hit, but also refreshed on background
                    final long cacheExpired = 24 * 60 * 60 * 1000; // in 24 hours this cache entry expires completely
                    long now = System.currentTimeMillis();
                    final long softExpire = now + cacheHitButRefreshed;
                    final long ttl = now + cacheExpired;
                    cacheEntry.data = response.data;
                    cacheEntry.softTtl = softExpire;
                    cacheEntry.ttl = ttl;
                    String headerValue;
                    headerValue = response.headers.get("Date");
                    if (headerValue != null) {
                        cacheEntry.serverDate = HttpHeaderParser.parseDateAsEpoch(headerValue);
                    }
                    headerValue = response.headers.get("Last-Modified");
                    if (headerValue != null) {
                        cacheEntry.lastModified = HttpHeaderParser.parseDateAsEpoch(headerValue);
                    }
                    cacheEntry.responseHeaders = response.headers;
                    final String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers));
                    return Response.success(new JSONObject(jsonString), cacheEntry);
                } catch (UnsupportedEncodingException | JSONException e) {
                    return Response.error(new ParseError(e));
                }
            }
        };

        jsonObjectRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) {

            }
        });
        RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }
}