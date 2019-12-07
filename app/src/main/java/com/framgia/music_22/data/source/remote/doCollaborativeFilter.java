package com.framgia.music_22.data.source.remote;

import android.media.Rating;
import android.os.AsyncTask;
import android.os.UserHandle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.framgia.music_22.data.model.Song;
import com.framgia.music_22.data.model.User;
import com.framgia.music_22.data.model.UserRating;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class doCollaborativeFilter extends AsyncTask {

    private DatabaseReference mDataFireBase ;

    public doCollaborativeFilter(){
        mDataFireBase = FirebaseDatabase.getInstance().getReference();

    }
    @Override
    protected Object doInBackground(Object[] objects) {

        mDataFireBase = FirebaseDatabase.getInstance().getReference("UserRating");

        Query queryRef = mDataFireBase.orderByKey();
            queryRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                        Log.e("Count ", "" + dataSnapshot.getChildrenCount());
                        Map<String, Object> objectMap = (HashMap<String, Object>) dataSnapshot.getValue();
                        ArrayList<UserRating> itemsReceivedList = new ArrayList<>();

                        for (Object obj : objectMap.values()) {
                            if (obj instanceof Map) {
                                Map<String, Object> mapObj = (Map<String, Object>) obj;
                                UserRating userRating = new UserRating();
                                userRating.setRatingPoint(Double.parseDouble(mapObj.get("ratingPoint").toString()));
                                userRating.setRatingID((String) mapObj.get("ratingID"));
                                userRating.setSongID((String) mapObj.get("songID"));
                                userRating.setUserID((String) mapObj.get("userID"));

                                itemsReceivedList.add(userRating);

                            }
                        }
                        tryCollaborateFilter(itemsReceivedList);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        return null;
    }

    public void tryCollaborateFilter(List<UserRating> userRatings){

        List<String> UserID  =  new ArrayList<>();
        List<String> SongID  =  new ArrayList<>();

        List<String> RatingID = new ArrayList<>();

            // ratingID = songID+userID;
            for(UserRating rate : userRatings){
                if(!UserID.contains(rate.getUserID()))
                    UserID.add(rate.getUserID());
                if(!SongID.contains(rate.getSongID()))
                    SongID.add(rate.getSongID());
                RatingID.add(rate.getRatingID());
            }

            for(int i = 0 ;i<SongID.size();i++){  //put zero point
                for(int j=0;j<UserID.size();j++){

                    String userid = UserID.get(j);
                    String songid =  SongID.get(i);
                    String ratingid = songid+userid;

                    if(!RatingID.contains(ratingid)){  // if ratingid not exist
                        double ratingPoint = 0;
                        final UserRating rate = new UserRating(ratingid,userid,songid,ratingPoint);
                        mDataFireBase = FirebaseDatabase.getInstance().getReference("UserRating");
                        mDataFireBase.push().setValue(rate);
                        return;
                    }
                }
            }

            if(SongID.size()*UserID.size()==RatingID.size()){ // if array full

                //Create Mean user rating matrix
                double[] meanRating = new double[UserID.size()];
                for (int j=0;j<UserID.size();j++){
                    double mean = 0;
                    int count=0;
                    for(int i=0;i<SongID.size();i++){
                        String rateidIJ = SongID.get(i)+UserID.get(j);
                        double ratePonitIJ = findRatingPoint(userRatings,rateidIJ);
                        if(ratePonitIJ>0){
                            count++;
                            mean += ratePonitIJ;
                        }
                    }
                    meanRating[j] = mean/count;
                }

                //Create Normalized utility matrix

                for(int j=0;j<UserID.size();j++){
                    for(int i=0;i<SongID.size();i++){
                        String rateidIJ = SongID.get(i)+UserID.get(j);
                        double ratePonitIJ = findRatingPoint(userRatings,rateidIJ);
                        if(ratePonitIJ>0){
                            updateRaingPoint(userRatings,rateidIJ,ratePonitIJ-meanRating[j]);
                        }
                    }
                }

                // Create User Similarity matrix
                double[][] userSimilar = new double[UserID.size()][UserID.size()];
                for (int i=0;i<UserID.size();i++){
                    for (int j=0;j<UserID.size();j++){
                        String user_1 = UserID.get(i);
                        String user_2 = UserID.get(j);
                        userSimilar[i][j] = cosine_similarity(user_1,user_2,userRatings,SongID);
                    }
                }

                // create Predict rating
                List<UserRating> preUserRating =  userRatings;

                for (int i =0;i<SongID.size();i++){
                    for (int j=0;j<UserID.size();j++){
                        String rateID = SongID.get(i)+UserID.get(j);
                        double resultIJ = findRatingPoint(userRatings,rateID);
                        if (resultIJ==0){
                            double preValue = mathPredictValue(i,j,userRatings,userSimilar,SongID,UserID);
                            updateRaingPoint(preUserRating,rateID,preValue);
                        }
                    }
                }

                // final result

                for(int j=0;j<UserID.size();j++)
                {
                    for(int i=0;i<SongID.size();i++)
                    {
                        String rateID = SongID.get(i)+UserID.get(j);
                        double curValue =  findRatingPoint(preUserRating,rateID);
                        updateRaingPoint(preUserRating,rateID,curValue+meanRating[j]);
                    }
                }

                //update value
                updateToServer(preUserRating,SongID,UserID);
            }
        }

        private double findRatingPoint(List<UserRating> list, String ratingID){
            for(int i =0;i<list.size();i++){
                if(list.get(i).getRatingID().equals(ratingID)){
                    return list.get(i).getRatingPoint();
                }
            }
            return -1;
        }
        private void updateRaingPoint(List<UserRating> list, String ratingID,double value){
            for(int i =0;i<list.size();i++){
                if(list.get(i).getRatingID().equals(ratingID)){
                    list.get(i).setRatingPoint(value);
                }
            }
        }
        private double cosine_similarity(String userID1, String userID2,List<UserRating> list, List<String> songList){
            double result = 0;
            double up = 0, down1 = 0, down2 = 0;

            for (int i=0;i<songList.size();i++){
                double user1_song = findRatingPoint(list,songList.get(i)+userID1);
                double user2_song = findRatingPoint(list,songList.get(i)+userID2);

                up += user1_song*user2_song;
                down1 += Math.pow(user1_song,2);
                down2 += Math.pow(user2_song,2);

            }
            result = (up/(Math.sqrt(down1)*Math.sqrt(down2)));
            return result;
        }
        private double mathPredictValue(int song_i, int user_j, List<UserRating> list, double[][] simUser, List<String> songlist, List<String> userlist){

            double[] nearestUser = new double[userlist.size()];
            double up =0;
            double down =0;

            //create nearestUser Matrix

            for(int b=0;b<userlist.size();b++)
            {
                double ratePoint =  findRatingPoint(list,songlist.get(song_i)+userlist.get(b));
                if(ratePoint!=0)
                {
                    if(simUser[user_j][b]==1) nearestUser[b] = -100;
                    else
                        nearestUser[b] = simUser[user_j][b];
                }
                else
                    nearestUser[b] = -100;

            }

            Arrays.sort(nearestUser);
            //k=2
            for(int c=1;c<=2;c++)
            {
                for(int b=0;b<userlist.size();b++)
                {
                    if(simUser[user_j][b] == nearestUser[userlist.size()-c])
                    {
                        double ratePoint =  findRatingPoint(list,songlist.get(song_i)+userlist.get(b));
                        up += ratePoint*simUser[user_j][b];
                        down += Math.abs(simUser[user_j][b]);
                    }
                }

            }
            return up/down;
        }
        private void updateToServer(final List<UserRating> preUserRating, List<String> Songs, List<String> Users){

            mDataFireBase = FirebaseDatabase.getInstance().getReference();
            for(int i=0;i<Songs.size();i++){
                for (int j=0;j<Users.size();j++){
                    final String rateID = Songs.get(i)+Users.get(j);
                    Query query = mDataFireBase.child("UserRating").orderByChild("ratingID").equalTo(rateID);

                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){ // update record

                                DataSnapshot nodeDataSnapshot = dataSnapshot.getChildren().iterator().next();

                                UserRating curRate = nodeDataSnapshot.getValue(UserRating.class); // bay loi
                                String key = nodeDataSnapshot.getKey();

                                String path = "/" + dataSnapshot.getKey() + "/" + key;
                                HashMap<String, Object> result = new HashMap<>();

                                result.put("ratingPoint", findRatingPoint(preUserRating,rateID));

                                try {
                                    mDataFireBase.child(path).updateChildren(result);
                                }catch (DatabaseException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
            }
        }
    }
