package com.organizers_group.stadfm.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
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
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.organizers_group.stadfm.Data.Constants;
import com.organizers_group.stadfm.Model.NewsFeed;
import com.organizers_group.stadfm.R;
import com.organizers_group.stadfm.Utils.CustomJSONHelper;
import com.organizers_group.stadfm.Utils.RequestQueueSingleton;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.List;

import static com.facebook.FacebookSdk.getApplicationContext;

public class NewsFeedRecyclerViewAdapter extends RecyclerView.Adapter<NewsFeedRecyclerViewAdapter.ViewHolder> {
    private Activity context;
    private List<NewsFeed> postList;

    public NewsFeedRecyclerViewAdapter() {
    }

    public NewsFeedRecyclerViewAdapter(Activity context, List<NewsFeed> postList) {
        this.context = context;
        this.postList = postList;

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);

        return new ViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        NewsFeed newsFeed = postList.get(position);
        String posterLink = newsFeed.getPostImgUrl();

        holder.category.setText(newsFeed.getCategory());
        try {
            Picasso.get().load(posterLink).into(holder.poster);
        } catch (Exception e) {
            Picasso.get().load(R.drawable.img_not_found).into(holder.poster);
        }

        // check if the topic from the favorite
        isItemChecked(newsFeed , holder.favBtn , holder.savingPB);

        holder.favBtn.setOnClickListener(v -> {
            holder.savingPB.setVisibility(View.VISIBLE);

            SharedPreferences sharedPreferences ;
            sharedPreferences = getApplicationContext().getSharedPreferences(Constants.SHARED_PREF_NAME , Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit() ;

            String accessToken = AccessToken.getCurrentAccessToken().getToken();
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, Constants.USER_ACCESS_TOKEN + accessToken,
                    response -> {
                        try {
                            // fetch user id
                            int userID = response.getInt("wp_user_id");

                            if (holder.favBtn.isChecked()) {
                                // add topic to favorite
                                CustomJSONHelper.postTopicToFav(context , String.valueOf(userID), newsFeed.getCategory() , holder.favBtn , holder.savingPB);

                                // [START subscribe_topics]
//                                FirebaseMessaging.getInstance().subscribeToTopic(newsFeed.getCategory());
                                CustomJSONHelper.registerNotification(FirebaseInstanceId.getInstance().getToken() , String.valueOf(userID));
//                                CustomJSONHelper.registerNotification(FirebaseInstanceId.getInstance().getToken() , String.valueOf(userID));
                                // [END subscribe_topics]

                                // add item to sharedPreference

                                editor.putString(Constants.SHARED_PREFERENCE_USER_TOPIC + newsFeed.getCategory() , newsFeed.getCategory());
                                editor.apply();

                            } else {
                                // use userID for getting the topic id
                                CustomJSONHelper.RemoveUserTopic(String.valueOf(userID), newsFeed , holder.favBtn , holder.savingPB);
                                editor.remove(Constants.SHARED_PREFERENCE_USER_TOPIC + newsFeed.getCategory());
                                editor.apply();
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
                        final long cacheHitButRefreshed = 1000; // in second  cache will be hit, but also refreshed on background
                        final long cacheExpired = 2 * 1000; // in 24 minutes this cache entry expires completely
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
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView poster;
        CheckBox favBtn;
        TextView category;
        ProgressBar savingPB;

        ViewHolder(View itemView, final Context context) {
            super(itemView);

            poster = itemView.findViewById(R.id.postImageView);
            category = itemView.findViewById(R.id.postCat);
            favBtn = itemView.findViewById(R.id.chosenArticleBtn);
            savingPB = itemView.findViewById(R.id.savingPBListItem);

            savingPB.setVisibility(View.INVISIBLE);
        }

    }
    private void isItemChecked(NewsFeed newsFeedIsChecked, CheckBox checkBox, ProgressBar savingPB){
        savingPB.setVisibility(View.VISIBLE);

        String accessToken = AccessToken.getCurrentAccessToken().getToken();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, Constants.USER_ACCESS_TOKEN + accessToken,
                response -> {
                    try {
                        // fetch user id
                        int userID = response.getInt("wp_user_id");

                        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, Constants.GET_USER_TOPICS + String.valueOf(userID),
                                responseGet -> {
                                    try {
                                        for (int i = 0; i < responseGet.length(); i++) {
                                            JSONObject topicObjts = responseGet.getJSONObject(i);

                                            if (topicObjts.has("checked") && topicObjts.getString("checked").equals("checked")){
                                                if (newsFeedIsChecked.getCategory().equals(topicObjts.getString("slug"))){
                                                    checkBox.setBackground(context.getResources().getDrawable(R.drawable.checked));
                                                    checkBox.setChecked(true);
                                                    savingPB.setVisibility(View.INVISIBLE);

                                                    FirebaseMessaging.getInstance().subscribeToTopic(newsFeedIsChecked.getCategory());
//                                                    CustomJSONHelper.registerNotification(FirebaseInstanceId.getInstance().getToken() , String.valueOf(userID));
                                                }
                                            }else
                                                savingPB.setVisibility(View.INVISIBLE);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }, error -> savingPB.setVisibility(View.INVISIBLE))
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