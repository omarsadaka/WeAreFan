package com.organizers_group.stadfm.Activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.organizers_group.stadfm.Adapters.MoreStoriesAdapter;
import com.organizers_group.stadfm.Data.Constants;
import com.organizers_group.stadfm.Model.NewsFeed;
import com.organizers_group.stadfm.R;
import com.organizers_group.stadfm.Utils.EndlessRecyclerViewScrollListener;
import com.organizers_group.stadfm.Utils.RequestQueueSingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import io.supercharge.shimmerlayout.ShimmerLayout;

public class MoreStoriesActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    List<NewsFeed> feedList;
    MoreStoriesAdapter moreStoriesAdapter;
    ImageView back;
    ImageView search;
    RelativeLayout headerLayout;
    TextView headerTitle;
    ShimmerLayout shimmerLayout;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        /*enable full screen*/
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //TODO or just visibility == gone to header activity
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            //todo instead put a floatBtn
            setContentView(R.layout.activity_more_stories_landscape);
        }else {
            setContentView(R.layout.activity_more_stories);
        }

        feedList = new ArrayList<>(  );

        progressBar = (ProgressBar)findViewById ( R.id.moreStoriesProgress );

        shimmerLayout = findViewById ( R.id.shimmer_view_container );

        back = findViewById ( R.id.newsfeedNavBack );
        search = findViewById ( R.id.newsfeedSearch );
        back.setOnClickListener ( v -> {
            onBackPressed ( );

        } );
        search.setOnClickListener (v -> startActivity ( new Intent( MoreStoriesActivity.this , SearchActivity.class ) ));

        headerLayout = findViewById(R.id.headerLayout);
        headerTitle = findViewById(R.id.headerTitle);

        recyclerView = findViewById ( R.id.moreStoriesRecyclerview);
        recyclerView.setHasFixedSize ( true );

        LinearLayoutManager manager = new LinearLayoutManager(this , LinearLayoutManager.HORIZONTAL , false);
        recyclerView.setLayoutManager ( manager );

        final int[] trendingPage = {1};
        //feedList = getNewsfeedPosts ( trendingPage[0] );
        moreStoriesAdapter = new MoreStoriesAdapter( this , getNewsfeedPosts (trendingPage[0]) );
        recyclerView.setAdapter (moreStoriesAdapter);
        moreStoriesAdapter.notifyDataSetChanged ();

        EndlessRecyclerViewScrollListener viewScrollListener = new EndlessRecyclerViewScrollListener (manager ) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                progressBar.setVisibility ( View.VISIBLE );
                getNewsfeedPosts ( page );
            }
        } ;

        recyclerView.addOnScrollListener ( viewScrollListener );
    }

    public List<NewsFeed> getNewsfeedPosts(int trendingPage){

        JsonArrayRequest arrayRequest = new JsonArrayRequest ( Request.Method.GET, Constants.TRENDING +"/?p="+ trendingPage,
                response -> {

                try {
                    for (int i = 0; i < response.length ( ); i++) {
                        JSONObject jsonObject = response.getJSONObject ( i );
                        NewsFeed newsFeed = new NewsFeed ( );
                        newsFeed.setPostID ( jsonObject.getInt ( "id" ) );
                        newsFeed.setPostImgUrl ( jsonObject.getString ( "poster" ) );
                        newsFeed.setTitle ( jsonObject.getString ( "title" ) );
                        newsFeed.setDescription ( jsonObject.getString ( "content" ) );
                        newsFeed.setCategory ( jsonObject.getString ( "topic" ) );
                        newsFeed.setPostedSince ( jsonObject.getString ( "post_date" ) );

                        int readinTime = jsonObject.getJSONObject ( "timef" ).getInt ( "min" );
                        if (readinTime == 0) {
                            newsFeed.setReadingTime ( "30 Seconds Reading" );
                        } else if (readinTime == 1) {
                            newsFeed.setReadingTime ( String.valueOf ( readinTime ) + " Minute Reading" );
                        } else {
                            newsFeed.setReadingTime ( String.valueOf ( readinTime ) + " Minutes Reading" );
                        }

                        //check for the post URL /*use it for sharing*/
                        if (jsonObject.has ( "url" )) {
                            newsFeed.setPostURl ( jsonObject.getString ( "url" ) );
                        } else {
                            Toast.makeText ( MoreStoriesActivity.this, getString ( R.string.article_not_hosted ), Toast.LENGTH_LONG ).show ( );
                        }

                        feedList.add ( newsFeed );
                    }
                    shimmerLayout.stopShimmerAnimation ();
                    shimmerLayout.setVisibility ( View.GONE );
                    progressBar.setVisibility ( View.GONE );
                }
                catch (JSONException e) {
                    e.printStackTrace ( );
                }

        }, error -> Toast.makeText ( MoreStoriesActivity.this, getString(R.string.more_stories_toast), Toast.LENGTH_SHORT ).show ( ))
        {
            @Override
            protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
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
                    return Response.success(new JSONArray(jsonString), cacheEntry);
                } catch (UnsupportedEncodingException | JSONException e) {
                    return Response.error(new ParseError(e));
                }
            }
        };

        arrayRequest.setRetryPolicy(new RetryPolicy() {
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

        RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(arrayRequest);
        return feedList;
    }

    @Override
    protected void onResume() {
        super.onResume ( );
        shimmerLayout.startShimmerAnimation ();

    }

    @Override
    protected void onPause() {
        shimmerLayout.stopShimmerAnimation ();
        super.onPause ( );

    }


}