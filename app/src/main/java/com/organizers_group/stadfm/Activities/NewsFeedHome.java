package com.organizers_group.stadfm.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.organizers_group.stadfm.Adapters.StoriesNewsFeedAdapter;
import com.organizers_group.stadfm.Adapters.TrendingNewsFeedAdapter;
import com.organizers_group.stadfm.Data.Constants;
import com.organizers_group.stadfm.Model.NewsFeed;
import com.organizers_group.stadfm.R;
import com.organizers_group.stadfm.Utils.CustomNavigationHandler;
import com.organizers_group.stadfm.Utils.EndlessRecyclerViewScrollListener;
import com.organizers_group.stadfm.Utils.RequestQueueSingleton;
import com.organizers_group.stadfm.Utils.TouchDetectableScrollView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.supercharge.shimmerlayout.ShimmerLayout;

public class NewsFeedHome extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private List<NewsFeed> postsList;
    private List<NewsFeed> mixedList;
    private StoriesNewsFeedAdapter storiesNewsFeedAdapter;
    private TrendingNewsFeedAdapter trendingNewsFeedAdapter;
    private ShimmerLayout horizontalShimmer;
    private ShimmerLayout ShimmerVertical;
    private ProgressBar progressBar;
    private int progressStatus = 0;
    private Handler handler = new Handler();
    private TouchDetectableScrollView detectableScrollView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*enable full screen*/
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_news_feed_home);

        horizontalShimmer = findViewById ( R.id.shimmer_horizontal );
        ShimmerVertical =  findViewById ( R.id.shimmer_vertical );


        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setBackground(ContextCompat.getDrawable(this, R.drawable.gradient) );
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);


        toggle.setHomeAsUpIndicator(R.drawable.dawer_icon);
        toggle.setDrawerIndicatorEnabled(false);
        toggle.setToolbarNavigationClickListener(view -> {
            if (drawer.isDrawerVisible(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                drawer.openDrawer(GravityCompat.START);
            }
        });

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        ImageView seeMoreStories = findViewById(R.id.moreStoriesImg);
        seeMoreStories.setOnClickListener (v -> startActivity ( new Intent ( NewsFeedHome.this, MoreStoriesActivity.class ) ));

        postsList = new ArrayList<>();

        progressBar = (ProgressBar)findViewById ( R.id.vertivalProgress );

        // Handle event listener for the Navigation view
        new CustomNavigationHandler(NewsFeedHome.this , drawer);

        RecyclerView trendingRecyclerView = findViewById(R.id.trendingStories);
        trendingRecyclerView.setHasFixedSize(true);
        LinearLayoutManager trendingLayoutManager = new LinearLayoutManager(this , LinearLayoutManager.HORIZONTAL , false);
        trendingRecyclerView.setLayoutManager(trendingLayoutManager);

        // get trending post
        final int[] trendingPage = {1};
        trendingNewsFeedAdapter = new TrendingNewsFeedAdapter( this , getTrendingPosts(trendingPage[0]));
        trendingRecyclerView.setAdapter(trendingNewsFeedAdapter);
        trendingNewsFeedAdapter.notifyDataSetChanged();

        // Retain an instance so that you can call `resetState()` for fresh searches
        EndlessRecyclerViewScrollListener scrollListener = new EndlessRecyclerViewScrollListener(trendingLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list

                progressBar.setVisibility ( View.VISIBLE );

                getTrendingPosts(page);

            }
        };
        // Adds the scroll listener to RecyclerView
        trendingRecyclerView.addOnScrollListener(scrollListener);
        // End Of Trending

        // Stories RecyclerView
        RecyclerView yourStoriesRecyclerView = findViewById(R.id.yourStories);
        yourStoriesRecyclerView.setNestedScrollingEnabled(false);
        yourStoriesRecyclerView.setHasFixedSize(true);
        LinearLayoutManager storiesLayoutManager = new LinearLayoutManager(this);
        yourStoriesRecyclerView.setLayoutManager(storiesLayoutManager);




        // get mixed posts
        final int[] storiesPage = {1};
        storiesNewsFeedAdapter = new StoriesNewsFeedAdapter( this , getStoriesPosts(storiesPage[0]));
        yourStoriesRecyclerView.setAdapter(storiesNewsFeedAdapter);
       // ViewCompat.setNestedScrollingEnabled(yourStoriesRecyclerView, false);
        storiesNewsFeedAdapter.notifyDataSetChanged();

//        detectableScrollView =  findViewById ( R.id. nestedScroll);
//        detectableScrollView.setMyScrollChangeListener ( new TouchDetectableScrollView.OnMyScrollChangeListener ( ) {
//            @Override
//            public void onScrollUp() {
//
//            }
//
//            @Override
//            public void onScrollDown() {
//
//            }
//
//            @Override
//            public void onBottomReached() {
//                getStoriesPosts (2 );
//                Toast.makeText ( NewsFeedHome.this, "Scroll", Toast.LENGTH_SHORT ).show ( );
//            }
//        } );


        // Retain an instance so that you can call `resetState()` for fresh searches
        EndlessRecyclerViewScrollListener storiesScrollListener = new EndlessRecyclerViewScrollListener(storiesLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                getStoriesPosts(page);
            }
        };
        // Adds the scroll listener to RecyclerView
        yourStoriesRecyclerView.setNestedScrollingEnabled ( false );
        yourStoriesRecyclerView.addOnScrollListener(storiesScrollListener);
/**
        // detect recyclerView end
        // using the nestedScrollView
        final boolean[] storiesLoading = {true};
        final int[] pastStoriesVisibleItems = new int[1];
        final int[] visibleStoriesItemCount = new int[1];
        final int[] totalStoriesItemCount = new int[1];
        NestedScrollView nestedScrollView = findViewById(R.id.nestedScroll);
        nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if ((scrollY >= (v.getChildAt(v.getChildCount() - 1).getMeasuredHeight() - v.getMeasuredHeight()))
                    && scrollY > oldScrollY){
                visibleStoriesItemCount[0] = storiesLayoutManager.getChildCount();
                totalStoriesItemCount[0] = storiesLayoutManager.getItemCount();
                pastStoriesVisibleItems[0] = storiesLayoutManager.findFirstVisibleItemPosition();

                if (storiesLoading[0]) {
                    if ((visibleStoriesItemCount[0] + pastStoriesVisibleItems[0]) >= totalStoriesItemCount[0]) {
                        storiesLoading[0] = false;
                        //Do pagination.. i.e. fetch new data
                        Toast.makeText(NewsFeedHome.this, "doing pagination...", Toast.LENGTH_SHORT).show();
                        storiesPage[0] =+ 1;
                        storiesNewsFeedAdapter = new StoriesNewsFeedAdapter( this , getStoriesPosts(storiesPage[0]));
//                        yourStoriesRecyclerView.setAdapter(storiesNewsFeedAdapter);
                        storiesNewsFeedAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
 */

    }

    // get json array
    public List<NewsFeed> getTrendingPosts(int trendingPage){
        JsonArrayRequest arrayRequest = new JsonArrayRequest(Request.Method.GET, Constants.TRENDING +"/?p="+ trendingPage,
                response -> {
                    try {
                        // check if the status is ok and we have values
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject postsObjs = response.getJSONObject(i);

                            NewsFeed newsFeed = new NewsFeed();
                            newsFeed.setPostID(postsObjs.getInt("id"));
                            newsFeed.setTitle(postsObjs.getString("title"));
                            newsFeed.setDescription(postsObjs.getString("content"));
                            newsFeed.setCategory(postsObjs.getString("topic"));
                            newsFeed.setPostImgUrl(postsObjs.getString("poster"));
                            newsFeed.setPostedSince(postsObjs.getString("post_date"));
                            newsFeed.setPostURl(postsObjs.getString("url"));

                            int readinTime = postsObjs.getJSONObject("timef").getInt("min");
                            if ( readinTime == 0){
                                newsFeed.setReadingTime("30 Seconds Reading");
                            }else if (readinTime == 1){
                                newsFeed.setReadingTime(String.valueOf(readinTime) + " Minute Reading");
                            } else {
                                newsFeed.setReadingTime(String.valueOf(readinTime) + " Minutes Reading");
                            }

                            if (postsObjs.has ( "url" )){
                                newsFeed.setPostURl ( postsObjs.getString ( "url" ) );
                            }else {
                                Toast.makeText ( NewsFeedHome.this, "No Result For URL", Toast.LENGTH_LONG ).show ( );
                            }

                            // add newsFeed to the ArrayList
                            postsList.add(newsFeed);
                        }
                        horizontalShimmer.stopShimmerAnimation();
                        horizontalShimmer.setVisibility( View.GONE);
                        progressBar.setVisibility ( View.GONE );
                        trendingNewsFeedAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    // return no result found
                    Log.v ( "error" ,""+ error.getMessage () );
                    Toast.makeText(this, "JSON Error " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });
        arrayRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(arrayRequest);
        return postsList;
    }

    // get json array
    public List<NewsFeed> getStoriesPosts(int storiesPage){
        mixedList = new ArrayList<>();
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(Constants.SHARED_PREF_NAME , Context.MODE_PRIVATE);
        String userID = prefs.getString(Constants.SHARED_PREFERENCE_USER_ID, null);
        if (userID != null) {
            JsonArrayRequest arrayRequest = new JsonArrayRequest(Request.Method.GET, Constants.USER_ARTICLES_MIXED + userID +"/?p="+ String.valueOf(storiesPage),
                    response1 -> {
                        try {
                            for (int i = 0; i < response1.length(); i++) {
                                JSONObject postsObjs = response1.getJSONObject(i);

                                NewsFeed newsFeed = new NewsFeed();
                                newsFeed.setPostID(postsObjs.getInt("id"));
                                newsFeed.setTitle(postsObjs.getString("title"));
                                newsFeed.setDescription(postsObjs.getString("content"));
                                newsFeed.setCategory(postsObjs.getString("topic"));
                                newsFeed.setPostImgUrl(postsObjs.getString("poster"));
                                newsFeed.setPostURl(postsObjs.getString("url"));
                                newsFeed.setPostedSince(postsObjs.getString("post_date"));

                                int readinTime = postsObjs.getJSONObject("timef").getInt("min");
                                if ( readinTime == 0){
                                    newsFeed.setReadingTime("30 Seconds Reading");
                                }else if (readinTime == 1){
                                    newsFeed.setReadingTime(String.valueOf(readinTime) + " Minute Reading");
                                } else {
                                    newsFeed.setReadingTime(String.valueOf(readinTime) + " Minutes Reading");
                                }

                                // add newsFeed to the ArrayList
                                ShimmerVertical.stopShimmerAnimation();
                                ShimmerVertical.setVisibility( View.GONE);
                                mixedList.add(newsFeed);
                            }
                            storiesNewsFeedAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    },
                    error1 -> {
                        // return no result found
                        Toast.makeText(this, "JSON Error " + error1.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            arrayRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(arrayRequest);
        }
        return mixedList;
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.news_feed_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.search) {
            startActivity(new Intent(getApplicationContext(),SearchActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        horizontalShimmer.startShimmerAnimation();
        ShimmerVertical.startShimmerAnimation ();
    }

    @Override
    protected void onPause() {
        horizontalShimmer.stopShimmerAnimation ();
        ShimmerVertical.startShimmerAnimation ();
        super.onPause();
    }
}