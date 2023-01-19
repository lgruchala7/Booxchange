package com.example.booxchange.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.booxchange.databinding.ActivityUserInfoBinding;
import com.example.booxchange.models.User;
import com.example.booxchange.utilities.Constants;
import com.example.booxchange.utilities.PreferenceManager;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.makeramen.roundedimageview.RoundedDrawable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UserInfoActivity extends BaseActivity {

    private ActivityUserInfoBinding binding;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private User user;
    private boolean isFriend = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        getUserData();
        setListeners();
    }

    private void getUserData() {
//        binding.imageUser.setImageBitmap(getUserImage(preferenceManager.getString(Constants.KEY_USER_IMAGE)));
//        binding.textName.setText(preferenceManager.getString(Constants.KEY_NAME));
//        binding.textEmail.setText(preferenceManager.getString(Constants.KEY_EMAIL));
//        binding.textCountry.setText(preferenceManager.getString(Constants.KEY_COUNTRY));
//        binding.textJoinDate.setText(preferenceManager.getString(Constants.KEY_TIMESTAMP).split("-")[0]);
        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(user.id)
                .get()
                .addOnCompleteListener(task -> {
                   DocumentSnapshot document = task.getResult();
                   if (task.isSuccessful() && task.getResult() != null) {
                       String userImageString = (String) document.get(Constants.KEY_USER_IMAGE);
                       binding.imageUser.setImageBitmap(getUserImage(userImageString));
                       binding.textName.setText((String) document.get(Constants.KEY_NAME));
                       binding.textEmail.setText((String) document.get(Constants.KEY_EMAIL));
                       binding.textCountry.setText((String) document.get(Constants.KEY_COUNTRY));
                       try {
                           Timestamp timestamp = (Timestamp) document.get(Constants.KEY_TIMESTAMP);
                           String dateString = getReadableDateTime(timestamp.toDate()).toString();
                           binding.textJoinDate.setText(dateString.split("-")[0]);
                       } catch (NullPointerException e) {
                           showErrorMessage(e);
                       }
                       try {
                           ArrayList<String> favoriteAdsList = (ArrayList<String>) document.get(Constants.KEY_FAVORITES);
                           binding.textFavoriteAdsCount.setText(String.valueOf(favoriteAdsList.size()));
                       } catch (Exception e) {
                           showErrorMessage(e);
                       }
                       try {
                           ArrayList<String> friendList = (ArrayList<String>) document.get(Constants.KEY_FRIENDS);
                           binding.textFriendsCount.setText(String.valueOf(friendList.size()));
                       } catch (Exception e) {
                           showErrorMessage(e);
                       }
                   }
                   else {
                       showErrorMessage(null);
                   }
                });
        database.collection(Constants.KEY_COLLECTION_ADS)
                .whereEqualTo(Constants.KEY_USER_ID, user.id)
                .get()
                .addOnCompleteListener( task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<DocumentSnapshot> documents = task.getResult().getDocuments();
                            binding.textAdsPublishedCount.setText(String.valueOf(documents.size()));
                    }
                    else {
                        showErrorMessage(null);
                    }
                });
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.imageClose.setOnClickListener( v -> onBackPressed() );
        binding.imageUser.setOnClickListener(v -> {
            AppCompatImageView imageFullScreen = binding.imageFullScreen;
            AppCompatImageView imageClose = binding.imageClose;
            Bitmap bitmap = ((RoundedDrawable) binding.imageUser.getDrawable()).getSourceBitmap();
            imageFullScreen.setImageBitmap(bitmap);
            imageFullScreen.setVisibility(View.VISIBLE);
            imageClose.setVisibility(View.VISIBLE);
        });
        binding.imageChat.setOnClickListener(v -> {
            Intent intent = new Intent(UserInfoActivity.this, ChatActivity.class);
            intent.putExtra(Constants.KEY_USER, user);
            startActivity(intent);
        });
        binding.imageFriendRemove.setOnClickListener( v -> removeFromFriends() );
        binding.imageFriendAdd.setOnClickListener( v -> addToFriends() );
    }

    private Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private void init() {
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());

        user = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        
        boolean isMyProfile = checkIfMyProfile();
        if (!isMyProfile)
        {
            checkIfFriend();
        }
    }
    
    private void checkIfFriend () {
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereArrayContains(Constants.KEY_FRIENDS, user.id)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<DocumentSnapshot> documentList = task.getResult().getDocuments();
                        for (DocumentSnapshot document : documentList) {
                            if (document.getId().equals(preferenceManager.getString(Constants.KEY_USER_ID))) {
                                isFriend = true;
                                break;
                            }
                        }
                    }
                    if (isFriend) {
                        binding.imageFriendAdd.setVisibility(View.GONE);
                        binding.imageFriendRemove.setVisibility(View.VISIBLE);
                    }
                    else {
                        binding.imageFriendRemove.setVisibility(View.GONE);
                        binding.imageFriendAdd.setVisibility(View.VISIBLE);
                    }
                });
    }
    
    private boolean checkIfMyProfile() {
        boolean isMyProfile = user.id.equals(preferenceManager.getString(Constants.KEY_USER_ID));
        if (isMyProfile) {
            binding.imageFriendRemove.setVisibility(View.GONE);
            binding.imageFriendAdd.setVisibility(View.GONE);
            binding.imageChat.setVisibility(View.GONE);
        }
        return isMyProfile;
    }
        

    private void showSnackbar(String text) {
        Snackbar.make(UserInfoActivity.this, binding.getRoot(), text, Toast.LENGTH_SHORT).show();
    }

    private void showErrorMessage(Exception e) {
        if (e != null) {
            e.printStackTrace();
        }
        showSnackbar("Error occurred while accessing user data");
    }

    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("dd MMMM, yyyy - kk:mm", Locale.getDefault()).format(date);
    }

    @Override
    public void onBackPressed() {
        AppCompatImageView imageFullScreen = binding.imageFullScreen;
        AppCompatImageView imageClose = binding.imageClose;
        if (imageFullScreen.getVisibility() == View.VISIBLE) {
            imageFullScreen.setImageBitmap(null);
            imageFullScreen.setVisibility(View.GONE);
            imageClose.setVisibility(View.GONE);
        }
        else {
            super.onBackPressed();
        }
    }

    private void addToFriends() {
        DocumentReference usersDocRef = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));
            usersDocRef.update(Constants.KEY_FRIENDS, FieldValue.arrayUnion(user.id))
                    .addOnCompleteListener( task -> {
                        if (task.isSuccessful()) {
                            showSnackbar("Added to friend list");
                            binding.imageFriendAdd.setVisibility(View.GONE);
                            binding.imageFriendRemove.setVisibility(View.VISIBLE);
                        }
                        else {
                            showSnackbar("Error while adding to friend list");
                        }
                    });
//        }
//        else {
//            showErrorMessage(null);
//        }
    }

    private void removeFromFriends() {
        DocumentReference usersDocRef = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));
//        if (usersDocRef != null) {
            usersDocRef.update(Constants.KEY_FRIENDS, FieldValue.arrayRemove(user.id))
                    .addOnCompleteListener( task -> {
                        if (task.isSuccessful()) {
                            showSnackbar("Removed from friend list");
                            binding.imageFriendRemove.setVisibility(View.GONE);
                            binding.imageFriendAdd.setVisibility(View.VISIBLE);
                        }
                        else {
                            showSnackbar("Error while removing from friend list");
                        }
                    });
//        }
//        else {
//            showErrorMessage(null);
//        }
    }
}