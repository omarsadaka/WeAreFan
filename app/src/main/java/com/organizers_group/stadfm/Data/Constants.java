package com.organizers_group.stadfm.Data;

public class Constants {

    private static final String domain = "https://stadfm.com/";

    public static final String SHARED_PREF_NAME = "User_Data";
    public static final String SHARED_PREFERENCE_USER_TOPIC = "USER_TOPIC ";
    public static String SHARED_PREFERENCE_USER_ID = "USER_DATA";



    public static final String RECENT_POSTS_URL = domain + "api/get_posts/";
    public static final String TAG_INDEX = domain + "api/get_tag_index/";
    public static final String TAG_POSTS = domain + "api/get_tag_posts/?slug=";// + topic name(slug)

    // get user accessToken
    // create new user or login by send ?access_token
    public static final String USER_ACCESS_TOKEN = domain + "api/user/fb_connect/?access_token=" ;//+ AccessToken.getCurrentAccessToken().getToken();

    //Search
    public static final String SEARCH_FIRST_TERM = domain + "api/get_search_results/?search=";


    //Notification API
    public static final String NOTIFY_URL = domain + "wp-json/org/v1/user_notfiction";
    // API for getting and posting user data
    //get user topics
    public static final String GET_USER_TOPICS = domain + "wp-json/org/v1/user_topics/" ;//+ USER_ID;
    //post user topics
    public static final String POST_URL = domain + "wp-json/org/v1/user_topics/";
    //Dismiss topics
    public static final String DISMISS_TOPIC = domain + "wp-json/org/v1/user_topics/"; // + "topic id"
    // Get User Avatar
    public static final String USER_AVATAR = domain + "api/user/get_avatar/?user_id=" + "&type=%27%27";
    // Get User Info
    public static final String USER_INFO = domain + "api/user/get_userinfo/?user_id="; // + USER_ID;
    // Get User Articles MIXED
    public static final String USER_ARTICLES_MIXED = domain + "wp-json/org/v1/user_articals_mixid/";// + USER_ID;
    // Get User Articles
    public static final String USER_ARTICLES = domain + "wp-json/org/v1/user_articals/";// + USER_ID;
    // Get All Topic (TAGs)
    public static final String GET_ALL_TOPICS = domain + "api/get_tag_index/";
    // Get Articles by ID
    public static final String GET_ARTICLE = domain + "wp-json/org/v1/user_articals/"; // + "article id"
    // Get trend topics
    public static final String TRENDING = domain + "wp-json/org/v1/trend/";
    // Add to User topics
    public static final String ADD_USER_TOPICS = domain + "wp-json/org/v1/user_topics/";
    // Add to User Articles
    public static final String ADD_USER_ARTICLES = domain + "wp-json/org/v1/user_articals/";
    // DISMISS User article
    public static final String DISMISS_USER_ARTICLES = domain + "wp-json/org/v1/user_articals/"; // + article_ID;

}