package com.organizers_group.stadfm.Adapters;

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
import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.AccessToken;
import com.organizers_group.stadfm.Activities.ArticleDetailsActivity;
import com.organizers_group.stadfm.Data.Constants;
import com.organizers_group.stadfm.Model.NewsFeed;
import com.organizers_group.stadfm.R;
import com.organizers_group.stadfm.Utils.CustomJSONHelper;
import com.organizers_group.stadfm.Utils.RequestQueueSingleton;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.List;

import static com.facebook.FacebookSdk.getApplicationContext;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {
    private Context context;
    private List<NewsFeed> newsFeedList;

    public SearchAdapter(Context context, List<NewsFeed> newsFeedList) {
        this.context = context;
        this.newsFeedList = newsFeedList;
    }

    @NonNull
    @Override
    public SearchAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_your_search , parent ,false);
        return new ViewHolder(view , context);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchAdapter.ViewHolder holder, int position) {
        NewsFeed newsFeed = newsFeedList.get(position);
        String posterLink = newsFeed.getPostImgUrl();

        holder.category.setText(newsFeed.getCategory());

        try{
            // parse HTML Data to readable
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                holder.title.setText(Html.fromHtml(newsFeed.getTitle() , Html.FROM_HTML_MODE_COMPACT));
                holder.content.setText(Html.fromHtml(newsFeed.getDescription() , Html.FROM_HTML_MODE_COMPACT));
            }else {
                holder.title.setText(Html.fromHtml(newsFeed.getTitle()));
                holder.content.setText(Html.fromHtml(newsFeed.getDescription()));
            }
        }catch (Exception ignored) { }
        try{
            Picasso.get().load(posterLink).into(holder.poster);
        }catch (Exception e){
            Picasso.get().load(R.drawable.img_not_found).into(holder.poster);
        }
    }

    @Override
    public int getItemCount() {
        return newsFeedList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView poster;
        TextView category;
        TextView title;
        TextView content;
        CheckBox saveArticleChkBx;
        ProgressBar savingPB;

        ViewHolder(View itemView, final Context contextActivity) {
            super(itemView);

            poster = itemView.findViewById(R.id.postImageView);
            category = itemView.findViewById(R.id.trendingCategory);
            title = itemView.findViewById ( R.id.trendingTitle );
            content = itemView.findViewById ( R.id.trendingSummary );
            savingPB = itemView.findViewById(R.id.savingPBSearch);
            saveArticleChkBx = itemView.findViewById(R.id.saveArticleChkBx);

            // save or dismiss article from savedArticles
            saveArticleChkBx.setOnClickListener(v -> {
                savingPB.setVisibility(View.VISIBLE);

                NewsFeed newsFeed = newsFeedList.get(getAdapterPosition());

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
                NewsFeed newsFeed = newsFeedList.get(getAdapterPosition());

                if (saveArticleChkBx.isChecked()) newsFeed.setChosenArticle(true);

                Intent intent = new Intent(contextActivity, ArticleDetailsActivity.class);
                intent.putExtra("post", newsFeed);
                contextActivity.startActivity(intent);
            });
        }
    }
}