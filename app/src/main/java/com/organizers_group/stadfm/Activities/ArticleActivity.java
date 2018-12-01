package com.organizers_group.stadfm.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.organizers_group.stadfm.Adapters.StoriesNewsFeedAdapter;
import com.organizers_group.stadfm.Data.Constants;
import com.organizers_group.stadfm.Model.NewsFeed;
import com.organizers_group.stadfm.R;
import com.organizers_group.stadfm.Utils.RequestQueueSingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.supercharge.shimmerlayout.ShimmerLayout;

public class ArticleActivity extends AppCompatActivity {

    private List<NewsFeed> postsList;
    private StoriesNewsFeedAdapter storiesNewsFeedAdapter;
    private String notifiedTopic;
    private ShimmerLayout shimmerNotified;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*enable full screen*/
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_article);

        //Shimmer
        shimmerNotified = findViewById(R.id.shimmer_notified);

        // navigate to search activity
        ImageView searchIcon = findViewById(R.id.articleSearch);
        searchIcon.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),SearchActivity.class)));
        // set on Click Listener for navigation back button
        ImageView navBack = findViewById(R.id.articleNavBack);
        navBack.setOnClickListener(v -> ArticleActivity.super.onBackPressed());

        notifiedTopic = (String) getIntent().getSerializableExtra("topic");
        Toast.makeText(this, notifiedTopic, Toast.LENGTH_SHORT).show();

        postsList = new ArrayList<>();
        postsList = getPosts();
        Toast.makeText(this, String.valueOf(getPosts().size()) + "  " + Constants.TAG_POSTS + notifiedTopic, Toast.LENGTH_SHORT).show();

        RecyclerView recyclerView = findViewById(R.id.articleRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        storiesNewsFeedAdapter = new StoriesNewsFeedAdapter( this , postsList);
        recyclerView.setAdapter(storiesNewsFeedAdapter);
        storiesNewsFeedAdapter.notifyDataSetChanged();

    }

    // retrieving data from JSON of Specific Tag
    public List<NewsFeed> getPosts(){
        postsList.clear();

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, Constants.TAG_POSTS + notifiedTopic,
                response -> {
                    try {
                        if (!response.has("posts")){

                            Toast.makeText(ArticleActivity.this, "Something Went Wrong!" , Toast.LENGTH_SHORT).show();
                            // in case of err
                            startActivity(new Intent(ArticleActivity.this , MainActivity.class));
                            this.finish();

                        }else {

                            JSONArray postsArray = response.getJSONArray("posts");

                            for (int i = 0; i < postsArray.length(); i++) {
                                JSONObject postsObj = postsArray.getJSONObject(i);

                                NewsFeed newsFeed = new NewsFeed();
                                newsFeed.setPostID(postsObj.getInt("id"));
                                newsFeed.setTitle(postsObj.getString("title"));
                                newsFeed.setDescription(postsObj.getString("content"));
                                newsFeed.setPostURl(postsObj.getString("url"));
                                newsFeed.setPostedSince(postsObj.getString("date"));

                                newsFeed.setCategory(getCat(postsObj));
                                newsFeed.setPostImgUrl(postsObj.getJSONArray("attachments").getJSONObject(0).getString("url"));
                                newsFeed.setReadingTime("UNKNOWN");

                                postsList.add(newsFeed);
                            }
                        }
                        // notify the adapter for changes! very important...
                        storiesNewsFeedAdapter.notifyDataSetChanged();
                        shimmerNotified.stopShimmerAnimation();
                        shimmerNotified.setVisibility(View.GONE);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Toast.makeText(ArticleActivity.this, "Connection Time Out!" , Toast.LENGTH_LONG).show());

        objectRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(objectRequest);
        return postsList;
    }
    // get the post category
    private String getCat(JSONObject postsObj) {
        String catType ="";
        try {
            JSONArray cat = postsObj.getJSONArray("categories");
            for (int i = 0 ; i< cat.length() ; i++ ){

                JSONObject category = cat.getJSONObject(i);

                catType = category.getString("title");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return catType;
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(ArticleActivity.this , NewsFeedHome.class));
        super.onBackPressed();
    }

    @Override
    public void onResume() {
        super.onResume();
        shimmerNotified.startShimmerAnimation();
    }

    @Override
    protected void onPause() {
        shimmerNotified.stopShimmerAnimation();
        super.onPause();
    }
}