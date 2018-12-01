package com.organizers_group.stadfm.Adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import static com.facebook.FacebookSdk.getApplicationContext;

public class LowerNavMenuAdapter extends ArrayAdapter<NewsFeed> {
private final Context context;
private final int layoutResourceId;
private ArrayList<NewsFeed> data;

    public LowerNavMenuAdapter(Context context, ArrayList<NewsFeed> data) {
        super(context, R.layout.menu_item_model, data);
        this.context = context;
        this.layoutResourceId = R.layout.menu_item_model;
        this.data = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        @SuppressLint("ViewHolder") View v = ((Activity) context).getLayoutInflater().inflate(layoutResourceId, parent, false);

        TextView name = v.findViewById(R.id.itemTitle);
        TextView dismissBtn = v.findViewById(R.id.dismissBtn);

        final NewsFeed newsFeed = data.get(position);

        name.setText(newsFeed.getTitle());

        dismissBtn.setOnClickListener(v1 -> {
            String accessToken = AccessToken.getCurrentAccessToken().getToken();

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, Constants.USER_ACCESS_TOKEN + accessToken,
                    response -> {
                        try {
                            // fetch user id
                            int userID = response.getInt("wp_user_id");

                            // use userID for getting the  id
                            CustomJSONHelper.deleteArticle(this , String.valueOf(userID), newsFeed , position);

                            remove(data.get(position));
                            notifyDataSetChanged();

                        } catch (JSONException | IndexOutOfBoundsException e) {
                            e.printStackTrace();
                        }
                    },
                    error ->{})
            {
                @Override
                protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                    try {
                        Cache.Entry cacheEntry = HttpHeaderParser.parseCacheHeaders(response);
                        if (cacheEntry == null) {
                            cacheEntry = new Cache.Entry();
                        }
                        final long cacheHitButRefreshed = 1000; // in 1 second cache will be hit, but also refreshed on background
                        final long cacheExpired = 60 * 1000; // in i minute this cache entry expires completely
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

        (new NewsFeedRecyclerViewAdapter()).notifyDataSetChanged();

        v.setOnClickListener(view -> {
            // move to next activity!
            NewsFeed feed = data.get(position);
            Intent intent = new Intent(context, ArticleDetailsActivity.class);
            feed.setFromLowerNav(true);
            intent.putExtra("post", feed);
            context.startActivity(intent);
        });
        return v;
    }

}