package com.organizers_group.stadfm.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.google.firebase.iid.FirebaseInstanceId;
import com.organizers_group.stadfm.Data.Constants;
import com.organizers_group.stadfm.R;
import com.organizers_group.stadfm.SigninOrUp.SignInOrOut;
import com.organizers_group.stadfm.Utils.CustomJSONHelper;
import com.organizers_group.stadfm.Utils.RequestQueueSingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.facebook.FacebookSdk.getApplicationContext;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "FireBaseSA";
    Switch switchNotify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*enable full screen*/
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_setting);

        //initialize FB SDK
        FacebookSdk.sdkInitialize(getApplicationContext());
        // change the font family
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/avenir_book.otf");

        TextView fbRegisterText = findViewById(R.id.fbRegisterText);
        RelativeLayout logout = findViewById(R.id.logoutLayout);
        TextView logoutText = findViewById(R.id.logoutText);
        ImageView searchIcon = findViewById(R.id.imageSettingSearch);
        ImageView navBack = findViewById(R.id.settingNavBack);
         switchNotify = findViewById(R.id.switchNotify);

        checkForSwitch ();

        //TODO switching here

        switchNotify.setOnClickListener(view -> {
           if (switchNotify.isChecked ()){


               SharedPreferences prefs = getApplicationContext().getSharedPreferences(Constants.SHARED_PREF_NAME , Context.MODE_PRIVATE);
               String userID = prefs.getString(Constants.SHARED_PREFERENCE_USER_ID, null);

               if (userID != null){
                   String kay_id = "?t=on";
                   checkNotify ( userID , kay_id );
               }

           }else {
               SharedPreferences prefs = getApplicationContext().getSharedPreferences(Constants.SHARED_PREF_NAME , Context.MODE_PRIVATE);
               String userID = prefs.getString(Constants.SHARED_PREFERENCE_USER_ID, null);

               if (userID != null){
                   String kayId = "?t=off";
                   checkNotify ( userID , kayId );
               }
           }
        });



        fbRegisterText.setTypeface(typeface);
        logoutText.setTypeface(typeface);

        fbRegisterText.setOnClickListener(this);
        logout.setOnClickListener(this);
        searchIcon.setOnClickListener(this);
        navBack.setOnClickListener(this);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.search:
                startActivity(new Intent(getApplicationContext(),SearchActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fbRegisterText:
                SignInOrOut.logoutUser(SettingActivity.this);
                break;
            case R.id.logoutLayout:
                SignInOrOut.logoutUser(SettingActivity.this);
                break;
            case R.id.imageSettingSearch:
                startActivity(new Intent(getApplicationContext(),SearchActivity.class));
                break;
            case R.id.settingNavBack:
                SettingActivity.super.onBackPressed();
                break;
        }
    }

    public void checkNotify(String id , String kay){


        StringRequest objectRequest = new StringRequest ( Request.Method.GET, "http://stadfm.com/wp-json/org/v1/on_off/" + id + kay,
                new Response.Listener<String> ( ) {
                    @Override
                    public void onResponse(String response) {
                        Log.v ( "yes" , String.valueOf ( response ) );


                    }
                }, new Response.ErrorListener ( ) {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v ( "no" , String.valueOf ( error ) );
            }
        } );
        objectRequest.setRetryPolicy(new RetryPolicy () {
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
        RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(objectRequest);

    }


    public void checkForSwitch(){

        SharedPreferences prefs = getApplicationContext().getSharedPreferences(Constants.SHARED_PREF_NAME , Context.MODE_PRIVATE);
        String userID = prefs.getString(Constants.SHARED_PREFERENCE_USER_ID, null);

        if (userID != null) {
            Log.v ( "name" , userID );
            //final JSONObject emptyJsonObject = new JSONObject();
            JsonObjectRequest objectRequest = new JsonObjectRequest ( Request.Method.GET, "http://stadfm.com/wp-json/org/v1/on_off/"+userID ,
                    new Response.Listener<JSONObject> ( ) {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.v ( "Response" , String.valueOf ( response ) );
                            try {
                                String value = response.getString ( "on_off" );
                                Log.v ( "value" , value );
                                if (value.equals ( "on" )){
                                    switchNotify.setChecked ( true );
                                    Toast.makeText ( SettingActivity.this, "Switch On", Toast.LENGTH_SHORT ).show ( );
                                }else if (value.equals ( "off" )){
                                    switchNotify.setChecked ( false );
                                    Toast.makeText ( SettingActivity.this, "Switch Off", Toast.LENGTH_SHORT ).show ( );

                                }

                            } catch (JSONException e) {
                                e.printStackTrace ( );
                            }

                        }
                    }, new Response.ErrorListener ( ) {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.v ( "Error" , String.valueOf ( error ) );
                }
            } );
            objectRequest.setRetryPolicy(new RetryPolicy () {
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
            RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(objectRequest);

        }


    }
}