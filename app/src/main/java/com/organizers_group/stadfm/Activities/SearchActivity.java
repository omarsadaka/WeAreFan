package com.organizers_group.stadfm.Activities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.organizers_group.stadfm.R;
import com.organizers_group.stadfm.Utils.SearchableJson;

import io.supercharge.shimmerlayout.ShimmerLayout;

public class SearchActivity extends AppCompatActivity {
    private EditText searchableWord;

    RecyclerView recyclerView;
    private ShimmerLayout mShimmerViewContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*enable full screen*/
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_search);


        mShimmerViewContainer = findViewById(R.id.shimmer_container);
        mShimmerViewContainer.setVisibility ( View.INVISIBLE );

        recyclerView = findViewById ( R.id.recyclerSearch );
        recyclerView.setHasFixedSize ( true );
        GridLayoutManager gridlm = new GridLayoutManager(this, 1);
        gridlm.setOrientation(GridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager ( gridlm );

        ImageView navBack = findViewById(R.id.searchNavBack);
        searchableWord = findViewById ( R.id.search_box );
        Typeface typeface = Typeface.createFromAsset ( getAssets ( ), "fonts/avenir_book.otf" );
        searchableWord.setTypeface ( typeface );

        searchableWord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if ( charSequence.length() > 1){
                    mShimmerViewContainer.setVisibility ( View.VISIBLE );
                    String searchWord = searchableWord.getText().toString();
                    SearchableJson searchableJson = new SearchableJson(searchWord, SearchActivity.this);
                    searchableJson.getPosts(recyclerView , mShimmerViewContainer);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        navBack.setOnClickListener (v -> SearchActivity.super.onBackPressed ( ));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.setting_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }



    @Override
    public void onResume() {
        super.onResume();
        mShimmerViewContainer.startShimmerAnimation();
    }

    @Override
    protected void onPause() {
        mShimmerViewContainer.stopShimmerAnimation();
        super.onPause();
    }


}