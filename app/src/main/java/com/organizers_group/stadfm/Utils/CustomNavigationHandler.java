package com.organizers_group.stadfm.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import com.facebook.GraphRequest;
import com.organizers_group.stadfm.Activities.MainActivity;
import com.organizers_group.stadfm.Activities.SettingActivity;
import com.organizers_group.stadfm.Data.Constants;
import com.organizers_group.stadfm.R;
import com.organizers_group.stadfm.SigninOrUp.SignInOrOut;
import com.squareup.picasso.Picasso;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import static com.facebook.FacebookSdk.getApplicationContext;

public class CustomNavigationHandler implements View.OnClickListener {

    private Context context;
    private DrawerLayout drawerLayout;

    public CustomNavigationHandler(final Context context, final DrawerLayout drawerLayout ) {
        this.context = context;
        this.drawerLayout = drawerLayout;

        // RecyclerView with API
        final ListView navTopicList = drawerLayout.findViewById(R.id.topicList);
        final ListView navArticleList = drawerLayout.findViewById(R.id.articleList);

        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {

                try {
                    String accessToken = AccessToken.getCurrentAccessToken().getToken();
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, Constants.USER_ACCESS_TOKEN + accessToken,
                            response -> {
                                try {
                                    // fetch user id
                                    int userID = response.getInt("wp_user_id");
                                    // get topics from favorite
                                    String topicAPI = Constants.GET_USER_TOPICS + String.valueOf(userID);
                                    CustomJSONHelper.getTopicFromFav(context , navTopicList , topicAPI);

                                    // get Articles from favorite
                                    String articleAPI = Constants.GET_ARTICLE + String.valueOf(userID);
                                    CustomJSONHelper.getArticleFromFav(context , navArticleList , articleAPI);

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
                }catch (Exception ignored){}

            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        // Handle event listener for the upper Navigation view
        NavigationView navigationView = drawerLayout.findViewById(R.id.parentNav);

        // Handle event listener for the Navigation view
        // adding topic
        LinearLayout addTopics = navigationView.findViewById(R.id.add_topicsList);
        addTopics.setOnClickListener(this);
        //set action to setting icon
        LinearLayout setting = navigationView.findViewById(R.id.nav_setting);
        setting.setOnClickListener(this);
        //set action to setting icon
        LinearLayout logoutLayout = navigationView.findViewById(R.id.nav_logout);
        logoutLayout.setOnClickListener(this);

        // set radius for images
        final ImageView circularImageView = navigationView.findViewById(R.id.userImageView);
        final TextView userNameTxt= navigationView.findViewById(R.id.userName);

        // get user data
        userData(circularImageView , userNameTxt);

    }

    private void userData(ImageView circularImageView , TextView userNameTxt) {
        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken() , (object, response) -> {
                    try {
                        String first_name = object.getString("first_name");
                        String last_name = object.getString("last_name");
                        String id = object.getString("id");
                        String image_url = "https://graph.facebook.com/" + id + "/picture?type=normal";

                        //setup user data to drawer
                        Picasso.get().load(image_url).transform(new ImageConverter()).into(circularImageView);
                        userNameTxt.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/avenir_book.otf"));
                        String fullName = first_name + "_" +last_name;
                        userNameTxt.setText(fullName);
                        if (object.has("email")){
                            String email = object.getString("email");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "first_name,last_name,email,id");
        request.setParameters(parameters);
        request.executeAsync();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.nav_setting:
                context.startActivity(new Intent(context , SettingActivity.class));
                drawerLayout.closeDrawer(GravityCompat.START);
                break;
            case R.id.nav_logout:
                drawerLayout.closeDrawer(GravityCompat.START);
                SignInOrOut.logoutUser((Activity) context);
                break;
            case R.id.add_topicsList:
                context.startActivity(new Intent(context , MainActivity.class));
                ((Activity)context).finish();
                drawerLayout.closeDrawer(GravityCompat.START);
                break;
        }
    }
}