package com.framgia.music_22.data.model;

import com.google.firebase.database.GenericTypeIndicator;

import java.util.List;

public class UserRating {

    private String ratingID;
    private String userID;
    private String songID;
    private double ratingPoint;

    public UserRating() {
    }

    public UserRating(String userID, String songID, double ratingPoint) {
        this.userID = userID;
        this.songID = songID;
        this.ratingPoint = ratingPoint;
    }
    public UserRating(String ratingID, String userID, String songID, double ratingPoint) {
        this.ratingID = ratingID;
        this.userID = userID;
        this.songID = songID;
        this.ratingPoint = ratingPoint;
    }

    public String getRatingID() {
        return ratingID;
    }

    public void setRatingID(String ratingID) {
        this.ratingID = ratingID;
    }


    public String getUserID() {
        return userID;
    }

    public String getSongID() {
        return songID;
    }

    public double getRatingPoint() {
        return ratingPoint;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setSongID(String songID) {
        this.songID = songID;
    }

    public void setRatingPoint(double ratingPoint) {
        this.ratingPoint = ratingPoint;
    }

}
