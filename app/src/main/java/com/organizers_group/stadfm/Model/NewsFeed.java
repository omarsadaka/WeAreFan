package com.organizers_group.stadfm.Model;

import java.io.Serializable;

public class NewsFeed implements Serializable {

    private final static long serializable = 10L;
    private String title;
    private String description;
    private String PostURl;
    private String postImgUrl;
    private String category;
    private int postID;
    private int articleID;
    private String readingTime;
    private String postedSince;
    private boolean chosenArticle;
    private boolean fromLowerNav;

    public NewsFeed() {
    }

    public NewsFeed(String title, String description, String category, int postID) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.postID = postID;
    }


    public static long getSerializable() {
        return serializable;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPostURl() {
        return PostURl;
    }

    public void setPostURl(String postURl) {
        PostURl = postURl;
    }

    public String getPostImgUrl() {
        return postImgUrl;
    }

    public void setPostImgUrl(String postImgUrl) {
        this.postImgUrl = postImgUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getPostID() {
        return postID;
    }

    public void setPostID(int postID) {
        this.postID = postID;
    }

    public String getReadingTime() {
        return readingTime;
    }

    public void setReadingTime(String readingTime) {
        this.readingTime = readingTime;
    }

    public String getPostedSince() {
        return postedSince;
    }

    public void setPostedSince(String postedSince) {
        this.postedSince = postedSince;
    }

    public int getArticleID() {
        return articleID;
    }

    public void setArticleID(int articleID) {
        this.articleID = articleID;
    }

    public boolean isChosenArticle() {
        return chosenArticle;
    }

    public void setChosenArticle(boolean chosenArticle) {
        this.chosenArticle = chosenArticle;
    }

    public boolean isFromLowerNav() {
        return fromLowerNav;
    }

    public void setFromLowerNav(boolean fromLowerNav) {
        this.fromLowerNav = fromLowerNav;
    }
}
