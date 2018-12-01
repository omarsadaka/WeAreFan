package com.organizers_group.stadfm.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.organizers_group.stadfm.Data.Constants;
import com.organizers_group.stadfm.R;
import com.organizers_group.stadfm.Utils.CustomJSONHelper;
import com.organizers_group.stadfm.Utils.CustomNavigationHandler;

import io.supercharge.shimmerlayout.ShimmerLayout;

public class MainActivity extends AppCompatActivity {

    public DrawerLayout drawerLayout;
    private ShimmerLayout shimmerMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*enable full screen*/
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        shimmerMain = findViewById(R.id.shimmer_main);
        drawerLayout = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.setHomeAsUpIndicator(R.drawable.dawer_icon);
        toggle.setDrawerIndicatorEnabled(false);
        toggle.setToolbarNavigationClickListener(view -> {
            if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        new CustomNavigationHandler(MainActivity.this, drawerLayout);

        RecyclerView recyclerView = findViewById(R.id.verticalRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // invoke
        new CustomJSONHelper(this).getTopics(recyclerView, shimmerMain);

        ProgressBar nextPB = findViewById(R.id.nextPB);
        Button nextText = findViewById(R.id.nextText);
        nextText.setOnClickListener(view -> {
            String topicGaming = getApplicationContext().getSharedPreferences(Constants.SHARED_PREF_NAME , Context.MODE_PRIVATE).getString(Constants.SHARED_PREFERENCE_USER_TOPIC + "gaming", null);
            String topicMusic = getApplicationContext().getSharedPreferences(Constants.SHARED_PREF_NAME , Context.MODE_PRIVATE).getString(Constants.SHARED_PREFERENCE_USER_TOPIC + "music", null);
            if (topicGaming != null || topicMusic != null) {

                startActivity(new Intent(getApplicationContext(), NewsFeedHome.class));
                nextPB.setVisibility(View.INVISIBLE);
                this.finish();
            } else {
                Toast.makeText(this, R.string.have_to_choose, Toast.LENGTH_SHORT).show();
                nextPB.setVisibility(View.INVISIBLE);
            }
        });
//        nextText.setOnClickListener(view -> {
//
//            nextPB.setVisibility(View.VISIBLE);
//
//            SharedPreferences prefs = getApplicationContext().getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
//            String restoredID = prefs.getString(Constants.SHARED_PREFERENCE_USER_ID, null);
//
//            if (restoredID != null) {
//                String topicAPI = Constants.GET_USER_TOPICS + restoredID;
//
//                ArrayList<Integer> haveTopic = new ArrayList<>();
//                JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, topicAPI,
//                        responseTopic -> {
//                            try {
//                                for (int i = 0; i < responseTopic.length(); i++) {
//                                    JSONObject topicObjts = responseTopic.getJSONObject(i);
//                                    if (topicObjts.has("checked") && topicObjts.getString("checked").equals("checked")) {
//                                        haveTopic.add(i);
//                                    }
//                                }
//                                if (haveTopic.size() > 0) {
//                                    startActivity(new Intent(getApplicationContext(), NewsFeedHome.class));
//                                    nextPB.setVisibility(View.INVISIBLE);
//                                    this.finish();
//                                } else {
//                                    Toast.makeText(this, R.string.have_to_choose, Toast.LENGTH_SHORT).show();
//                                    nextPB.setVisibility(View.INVISIBLE);
//                                }
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        }, error -> {
//                });
//
//                RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonArrayRequest);
//
//            } else {
//                String accessToken = AccessToken.getCurrentAccessToken().getToken();
//
//                ArrayList<Integer> haveTopic = new ArrayList<>();
//                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, Constants.USER_ACCESS_TOKEN + accessToken,
//                        response -> {
//                            try {
//                                // fetch user id
//                                int userID = response.getInt("wp_user_id");
//                                // use userID for getting the topic id
//                                JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, Constants.GET_USER_TOPICS + userID,
//                                        responseTopic -> {
//                                            try {
//                                                for (int i = 0; i < responseTopic.length(); i++) {
//                                                    JSONObject topicObjts = responseTopic.getJSONObject(i);
//                                                    if (topicObjts.has("checked") && topicObjts.getString("checked").equals("checked")) {
//                                                        haveTopic.add(i);
//                                                    }
//                                                }
//                                                if (haveTopic.size() > 0) {
//                                                    startActivity(new Intent(getApplicationContext(), NewsFeedHome.class));
//                                                    nextPB.setVisibility(View.INVISIBLE);
//                                                    this.finish();
//                                                } else {
//                                                    Toast.makeText(this, R.string.have_to_choose, Toast.LENGTH_SHORT).show();
//                                                    nextPB.setVisibility(View.INVISIBLE);
//                                                }
//                                            } catch (JSONException e) {
//                                                e.printStackTrace();
//                                            }
//                                        }, error -> {
//                                });
//
//                                RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonArrayRequest);
//
//
//                            } catch (JSONException | IndexOutOfBoundsException e) {
//                                e.printStackTrace();
//                            }
//                        },
//                        error -> {
//                        }) {
//                    @Override
//                    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
//                        try {
//                            Cache.Entry cacheEntry = HttpHeaderParser.parseCacheHeaders(response);
//                            if (cacheEntry == null) {
//                                cacheEntry = new Cache.Entry();
//                            }
//                            final long cacheHitButRefreshed = 1000; // in 1 second cache will be hit, but also refreshed on background
//                            final long cacheExpired = 60 * 1000; // in i minute this cache entry expires completely
//                            long now = System.currentTimeMillis();
//                            final long softExpire = now + cacheHitButRefreshed;
//                            final long ttl = now + cacheExpired;
//                            cacheEntry.data = response.data;
//                            cacheEntry.softTtl = softExpire;
//                            cacheEntry.ttl = ttl;
//                            String headerValue;
//                            headerValue = response.headers.get("Date");
//                            if (headerValue != null) {
//                                cacheEntry.serverDate = HttpHeaderParser.parseDateAsEpoch(headerValue);
//                            }
//                            headerValue = response.headers.get("Last-Modified");
//                            if (headerValue != null) {
//                                cacheEntry.lastModified = HttpHeaderParser.parseDateAsEpoch(headerValue);
//                            }
//                            cacheEntry.responseHeaders = response.headers;
//                            final String jsonString = new String(response.data,
//                                    HttpHeaderParser.parseCharset(response.headers));
//                            return Response.success(new JSONObject(jsonString), cacheEntry);
//                        } catch (UnsupportedEncodingException | JSONException e) {
//                            return Response.error(new ParseError(e));
//                        }
//                    }
//                };
//
//                jsonObjectRequest.setRetryPolicy(new RetryPolicy() {
//                    @Override
//                    public int getCurrentTimeout() {
//                        return 50000;
//                    }
//
//                    @Override
//                    public int getCurrentRetryCount() {
//                        return 50000;
//                    }
//
//                    @Override
//                    public void retry(VolleyError error) {
//
//                    }
//                });
//                RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
//
//            }
//        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        shimmerMain.startShimmerAnimation();
    }

    @Override
    protected void onPause() {
        shimmerMain.stopShimmerAnimation();
        super.onPause();
    }
}