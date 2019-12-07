package com.framgia.music_22.data.source.remote;

import android.support.annotation.NonNull;

import com.framgia.music_22.data.SongDataSource;
import com.framgia.music_22.data.OnFetchDataListener;
import com.framgia.music_22.data.RequestCallbackData;
import com.framgia.music_22.data.model.Artist;
import com.framgia.music_22.data.model.MoreSong;
import com.framgia.music_22.data.model.Song;
import com.framgia.music_22.data.model.UserRating;

import com.framgia.music_22.utils.Constant;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SongRemoteDataSource implements SongDataSource.RemoteDataSource {
    private static SongRemoteDataSource sInstance;
    private DatabaseReference mDataFireBase ;

    public static synchronized SongRemoteDataSource getInstance() {
        if (sInstance == null) {
            sInstance = new SongRemoteDataSource();
        }
        return sInstance;
    }

    public MoreSong parseSongList(String jsonInput) {
        final ArrayList<Song> songs = new ArrayList<>();
        MoreSong moreSong = null;
        try {
            JSONObject jsonObject = new JSONObject(jsonInput);
            JSONArray songJsons = jsonObject.getJSONArray(Constant.ARRAY_JSON_NAME);
            int jsonArrayLength = songJsons.length();

            for (int i = 0; i < jsonArrayLength; i++) {
                JSONObject songJson = songJsons.getJSONObject(i);
                JSONObject artistJson = songJson.getJSONObject("user");
                Song song = new Song.Builder().withSongId(
                        songJson.getString(Song.APISongProperties.SONG_ID))
                        .withTitle(songJson.getString(Song.APISongProperties.TITLE))
                        .withGenre(songJson.getString(Song.APISongProperties.GENRE))
                        .withUserId(songJson.getString(Song.APISongProperties.USER_ID))
                        .withStreamUrl(songJson.getString(Song.APISongProperties.STREAM_URL))
                        .withDuaration(Integer.parseInt(
                                songJson.getString(Song.APISongProperties.DUARATION)))
                        .withUri(songJson.getString(Song.APISongProperties.SONG_URI))
                        .withArtist(
                                new Artist(artistJson.getString(Artist.APIArtistProperties.USER_ID),
                                        artistJson.getString(Artist.APIArtistProperties.USER_NAME),
                                        artistJson.getString(
                                                Artist.APIArtistProperties.AVATAR_URL)))
                        .build();
                songs.add(song);
            }
            String nextHref = jsonObject.getString(Constant.NEXT_HREF);

            //handle database to RS
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
           // if(user!=null){
                mDataFireBase = FirebaseDatabase.getInstance().getReference("UserRating");
                Query query = mDataFireBase.orderByChild("userID").equalTo(user.getUid());

                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                            Map<String, Object> objectMap = (HashMap<String, Object>) dataSnapshot.getValue();
                            List<UserRating> curUserRating = new ArrayList<>();
                            for (Object obj : objectMap.values()) {
                                if (obj instanceof Map) {
                                    Map<String, Object> mapObj = (Map<String, Object>) obj;
                                    UserRating userRating = new UserRating();
                                    userRating.setRatingPoint(Double.parseDouble(mapObj.get("ratingPoint").toString()));
                                    userRating.setRatingID((String) mapObj.get("ratingID"));
                                    userRating.setSongID((String) mapObj.get("songID"));
                                    userRating.setUserID((String) mapObj.get("userID"));

                                    curUserRating.add(userRating);

                                }
                            }
                            Collections.sort(curUserRating, new Comparator<UserRating>() {
                                @Override
                                public int compare(UserRating userRating, UserRating t1) {
                                    return userRating.getRatingPoint()<t1.getRatingPoint()? -1 : 1;
                                }
                            });
                            for (UserRating userRating :curUserRating){
                                int songPos =  getPosition(songs,userRating.getSongID());
                                Song mSong = songs.get(songPos);
                                songs.remove(mSong);
                                songs.add(0,mSong);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        //    }

            moreSong = new MoreSong(songs, nextHref);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return moreSong;
    }

    @Override
    public void getSongByGenre(String genre, final RequestCallbackData<MoreSong> callbackData) {

        new GetDataFromUrl(new OnFetchDataListener() {

            @Override
            public void onFetchDataSuccess(String resutlData) {
                MoreSong moreSong = null;
                moreSong = parseSongList(resutlData);
                callbackData.onGetDataSuccess(moreSong);
            }

            @Override
            public void onFetchDataError(Exception errorEx) {
                callbackData.onGetDataError(errorEx);
            }
        }).execute(genre + Constant.LIMIT_NUMBER);

        new doCollaborativeFilter().execute();
    }
    private int getPosition(ArrayList<Song> songs, String songid){
        for(int i=0;i<songs.size();i++){
            if(songs.get(i).getSongId().equals(songid)){{
                return i;
            }}
        }
        return -1;
    }
}
