package com.framgia.music_22.data.model;

import java.io.Serializable;

public class User implements Serializable {

    private String mUserId;
    private String mUserName;
    private String mAvatarUrl;
    private String mEmail;
    private String mPassWord;

    public User(){

    }

    public User(String mUserId, String mUserName, String mAvatarUrl, String mEmail, String mPassWord) {
        this.mUserId = mUserId;
        this.mUserName = mUserName;
        this.mAvatarUrl = mAvatarUrl;
        this.mEmail = mEmail;
        this.mPassWord = mPassWord;
    }

    public User(String mUserName, String mAvatarUrl, String mEmail, String mPassWord) {
        this.mUserName = mUserName;
        this.mAvatarUrl = mAvatarUrl;
        this.mEmail = mEmail;
        this.mPassWord = mPassWord;
    }

    public User(String mEmail, String mPassWord) {
        this.mEmail = mEmail;
        this.mPassWord = mPassWord;
    }

    public String getUserId() {
        return mUserId;
    }

    public String getUserName() {
        return mUserName;
    }

    public String getAvatarUrl() {
        return mAvatarUrl;
    }

    public String getEmail() {
        return mEmail;
    }

    public String getPassWord() {
        return mPassWord;
    }

    public void setUserId(String mUserId) {
        this.mUserId = mUserId;
    }

    public void setUserName(String mUserName) {
        this.mUserName = mUserName;
    }

    public void setAvatarUrl(String mAvatarUrl) {
        this.mAvatarUrl = mAvatarUrl;
    }

    public void setEmail(String mEmail) {
        this.mEmail = mEmail;
    }

    public void setPassWord(String mPassWord) {
        this.mPassWord = mPassWord;
    }
}
