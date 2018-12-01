package com.organizers_group.stadfm.SigninOrUp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.organizers_group.stadfm.Activities.IntroActivity;
import com.organizers_group.stadfm.Activities.MainActivity;
import com.organizers_group.stadfm.Data.Constants;
import com.organizers_group.stadfm.R;
import com.organizers_group.stadfm.Utils.RequestQueueSingleton;
import org.json.JSONException;

import java.util.Arrays;

import static com.facebook.FacebookSdk.getApplicationContext;

public class SignInOrOut {
    private Context context;

    public SignInOrOut() {
        this.context = getApplicationContext();
    }

    public static void logoutUser(Activity context){
        new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE, graphResponse -> {

            LoginManager.getInstance().logOut();
            Toast.makeText(context,"Logged out!",Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getApplicationContext(), IntroActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            context.finish();

        }).executeAsync();

        LoginManager.getInstance().logOut();

    }

    public void LogInWithFaceBook(Context context, LoginButton fbLoginButton, CallbackManager callbackManager){

        fbLoginButton.setReadPermissions(Arrays.asList("email", "public_profile"));

        // Callback registration
        fbLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                NavToMainActivity();

                getUserID(AccessToken.getCurrentAccessToken().getToken());
                ((Activity)context).finish();

            }
            @Override
            public void onCancel() {

                Toast.makeText(context, "onCancel", Toast.LENGTH_SHORT).show();

            }
            @Override
            public void onError(FacebookException error) {
                Toast.makeText(context, R.string.facebook_login_error, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void NavToMainActivity() {
        context.startActivity(new Intent(context, MainActivity.class));
    }


    private int getUserID(String accessToken) {
        SharedPreferences sharedPreferences ;
        sharedPreferences = getApplicationContext().getSharedPreferences(Constants.SHARED_PREF_NAME , Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit() ;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, Constants.USER_ACCESS_TOKEN + accessToken,
                response -> {
                    try {
                        // fetch user id
                        int userID = response.getInt("wp_user_id");
                        editor.putString(Constants.SHARED_PREFERENCE_USER_ID, String.valueOf(userID));
                        editor.apply();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
            //ERROR
        });
        RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
        return Integer.parseInt(sharedPreferences.getString(Constants.SHARED_PREFERENCE_USER_ID , "0"));
    }
}