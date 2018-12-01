package com.organizers_group.stadfm.Activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.organizers_group.stadfm.Data.Constants;
import com.organizers_group.stadfm.Model.NewsFeed;
import com.organizers_group.stadfm.R;
import com.organizers_group.stadfm.Utils.CustomJSONHelper;
import com.organizers_group.stadfm.Utils.RequestQueueSingleton;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class ArticleDetailsActivity extends AppCompatActivity {

    private ImageView postPoster;
    private TextView content;
    private TextView postTitle;
    private NewsFeed newsFeed;
    private TextView readingTime;
    private TextView detPostedSince;
    private TextView contentTitle;
    private CheckBox saveArticleChkBx;
    private ProgressBar savingPB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*enable full screen*/
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_article_details);

        setUpUI();

        try {
            newsFeed = (NewsFeed) getIntent().getSerializableExtra("post");

            // just for lower nav menu adapter onClick
            if (newsFeed.isFromLowerNav()) saveArticleChkBx.setVisibility(View.GONE);

            if (newsFeed.isChosenArticle()) {
                saveArticleChkBx.setChecked(true);
                saveArticleChkBx.setBackground(ArticleDetailsActivity.this.getResources().getDrawable(R.drawable.favorite_article));
            }

            // parse HTML Data to readable
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                postTitle.setText(Html.fromHtml(newsFeed.getTitle() , Html.FROM_HTML_MODE_COMPACT));
                contentTitle.setText(Html.fromHtml(newsFeed.getTitle() , Html.FROM_HTML_MODE_COMPACT));
                content.setText(Html.fromHtml(newsFeed.getDescription() , Html.FROM_HTML_MODE_COMPACT));
                readingTime.setText(Html.fromHtml(newsFeed.getReadingTime() , Html.FROM_HTML_MODE_COMPACT));
                detPostedSince.setText(Html.fromHtml(newsFeed.getPostedSince() , Html.FROM_HTML_MODE_COMPACT));
            }else {
                postTitle.setText(Html.fromHtml(newsFeed.getTitle()));
                contentTitle.setText(Html.fromHtml(newsFeed.getTitle()));
                content.setText(Html.fromHtml(newsFeed.getDescription()));
                readingTime.setText(Html.fromHtml(newsFeed.getReadingTime()));
                detPostedSince.setText(Html.fromHtml(newsFeed.getPostedSince()));
            }


        }catch (Exception ignored){

        }

        try{
            Picasso.get().load(newsFeed.getPostImgUrl()).into(postPoster);
        }catch (Exception e){
            Picasso.get().load(R.drawable.img_not_found).into(postPoster);
        }

    }

    private void setUpUI() {
        ImageView detNavBack = findViewById(R.id.detNavBackImg);
        postPoster = findViewById(R.id.detPostImageView);
        postTitle = findViewById(R.id.detTitle);
        content = findViewById(R.id.detContent);
        readingTime = findViewById(R.id.detReadingTime);
        detPostedSince = findViewById(R.id.detPostedSince);
        contentTitle = findViewById(R.id.detContentTitle);
        Button sharePost = findViewById(R.id.detShare);
        saveArticleChkBx = findViewById(R.id.saveArticleChkBx);
        savingPB = findViewById(R.id.savingPBArticleDet);

        // save or dismiss article from savedArticles
        saveArticleChkBx.setOnClickListener(v -> {
            savingPB.setVisibility(View.VISIBLE);

            String accessToken = AccessToken.getCurrentAccessToken().getToken();

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, Constants.USER_ACCESS_TOKEN + accessToken,
                    response -> {
                        try {
                            // fetch user id
                            int userID = response.getInt("wp_user_id");
                            if (saveArticleChkBx.isChecked()) {
                                // add topic to favorite

                                CustomJSONHelper.postArticleToFav(this , String.valueOf(userID), String.valueOf(newsFeed.getPostID()), saveArticleChkBx, savingPB);
                            } else {
                                // use userID for getting the topic id
                                CustomJSONHelper.RemoveUserSavedArticle(String.valueOf(userID), newsFeed , saveArticleChkBx , savingPB);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    },
                    error -> {})
            {
                @Override
                protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                    try {
                        Cache.Entry cacheEntry = HttpHeaderParser.parseCacheHeaders(response);
                        if (cacheEntry == null) {
                            cacheEntry = new Cache.Entry();
                        }
                        final long cacheHitButRefreshed = 3 * 1000; // in 3 minutes cache will be hit, but also refreshed on background
                        final long cacheExpired = 60 * 1000; // in 1 hour this cache entry expires completely
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


        detNavBack.setOnClickListener(view -> ArticleDetailsActivity.super.onBackPressed());
        sharePost.setOnClickListener(view -> share());

    }

    private void share() {
        Toast.makeText(this, "Sharing", Toast.LENGTH_SHORT).show();
        Intent sharingIntent = new Intent ( Intent.ACTION_SEND );
        sharingIntent.setType ( "text/plain" );
        sharingIntent.putExtra ( Intent.EXTRA_SUBJECT, "See This Article Here" );
        sharingIntent.putExtra ( Intent.EXTRA_TEXT, newsFeed.getPostURl () );
        startActivity ( Intent.createChooser ( sharingIntent ,"Sharing By" ));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout_feed_news);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.setting_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.search:
                Intent intent = new Intent(getApplicationContext(),SearchActivity.class);
                startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }


}