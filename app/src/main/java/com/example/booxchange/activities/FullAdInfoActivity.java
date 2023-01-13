package com.example.booxchange.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.example.booxchange.adapters.AdImagesAdapter;
import com.example.booxchange.databinding.ActivityFullAdInfoBinding;

import com.example.booxchange.listeners.AdImageListaner;
import com.example.booxchange.models.Ad;
import com.example.booxchange.models.User;
import com.example.booxchange.utilities.Constants;
import com.example.booxchange.utilities.PreferenceManager;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FullAdInfoActivity extends AppCompatActivity implements AdImageListaner {

    private ActivityFullAdInfoBinding binding;
    private Ad ad;
    String adId;
    private AdImagesAdapter adImagesAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFullAdInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
        loadAdDetails();
        setListeners();
    }

    private void loadAdDetails() {
        binding.textTitle.setText(ad.title);
        binding.userInfo.textName.setText("@" + ad.userName);
        binding.userInfo.textEmail.setText(ad.userEmail);
        binding.userInfo.imageProfile.setImageBitmap(getUserImage(ad.userImage));
        binding.textAuthor.setText(ad.author);
        binding.textGenre.setText(ad.genre);
        binding.textCity.setText(ad.city);
        binding.textAddress.setText(ad.address);
        binding.textDateTime.setText(ad.dateTime.split("-")[0]);
        binding.ratingBarCondition.setRating(Integer.parseInt(ad.condition));
        binding.textDescription.setText(ad.description);

        adImagesAdapter = new AdImagesAdapter(ad.images, this);
        binding.adImagesRecyclerView.setAdapter(adImagesAdapter);
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.imageChat.setOnClickListener(v -> {
            User user = new User(ad.userId, ad.userName, ad.userImage);
            Intent intent = new Intent(FullAdInfoActivity.this, ChatActivity.class);
            intent.putExtra(Constants.KEY_USER, user);
            startActivity(intent);
        });
        binding.imageClose.setOnClickListener( v -> onBackPressed() );
        binding.imageSeeProfile.setOnClickListener( v -> {
            User user = new User(ad.userId, ad.userName, ad.userImage);
            Intent intent = new Intent(FullAdInfoActivity.this, UserInfoActivity.class);
            intent.putExtra(Constants.KEY_USER, user);
            startActivity(intent);
        } );
        binding.imageAddToFavorites.setOnClickListener( v -> addToFavorites());
        binding.imageRemoveFromFavorites.setOnClickListener( v -> removeFromFavorites());
    }

    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();


        ad = (Ad) getIntent().getSerializableExtra(Constants.KEY_AD);
        boolean isMyProfile = checkIfMyProfile();
        if (!isMyProfile) {
            findAdId();
        }
    }

    private void findAdId() {
        Task<QuerySnapshot> taskAdId = database.collection(Constants.KEY_COLLECTION_ADS)
                .whereEqualTo(Constants.KEY_USER_ID, ad.userId)
                .whereEqualTo(Constants.KEY_TIMESTAMP, ad.dateObject)
                .get()
                .addOnCompleteListener( task -> checkIfAdFavorite(task));
    }

    private void checkIfAdFavorite(Task<QuerySnapshot> task) {
            if (task.isSuccessful() && task.getResult() != null) {
                adId = task.getResult().getDocuments().get(0).getId();

                database.collection(Constants.KEY_COLLECTION_USERS)
                        .document(preferenceManager.getString(Constants.KEY_USER_ID))
                        .get()
                        .addOnCompleteListener(newTask -> setImageFavoriteIndicator(newTask));
            }
    }

    private void setImageFavoriteIndicator(Task<DocumentSnapshot> task) {
            if (task.isSuccessful() && task.getResult() != null) {
                ArrayList<String> favoriteAdList = new ArrayList<>();
                try {
                    favoriteAdList = (ArrayList<String>) task.getResult().get(Constants.KEY_FAVORITES);
                } catch (ClassCastException e) {
                    e.printStackTrace();
                }
                if (favoriteAdList != null && favoriteAdList.contains(adId)) {
                    binding.imageAddToFavorites.setVisibility(View.GONE);
                    binding.imageRemoveFromFavorites.setVisibility(View.VISIBLE);
                } else {
                    binding.imageRemoveFromFavorites.setVisibility(View.GONE);
                    binding.imageAddToFavorites.setVisibility(View.VISIBLE);
                }
            }
    }

    private Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    @Override
    public void onAdImageClicked(View imageView) {
        AppCompatImageView imageFullScreen = binding.imageFullScreen;
        AppCompatImageView imageClose = binding.imageClose;
        Bitmap bitmap = ((BitmapDrawable) ((AppCompatImageView) imageView).getDrawable()).getBitmap();
        imageFullScreen.setImageBitmap(bitmap);
        imageFullScreen.setVisibility(View.VISIBLE);
        imageClose.setVisibility(View.VISIBLE);
    }

    private void showToast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
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

    private void addToFavorites() {
        DocumentReference usersDocRef = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));
        if (adId != null) {
        usersDocRef.update(Constants.KEY_FAVORITES, FieldValue.arrayUnion(adId))
                .addOnCompleteListener( task -> {
                    if (task.isSuccessful()) {
                        showToast("Added to favorite ads list");
                        binding.imageAddToFavorites.setVisibility(View.GONE);
                        binding.imageRemoveFromFavorites.setVisibility(View.VISIBLE);
                    }
                    else {
                        showToast("Error while adding to favorite ads list");
                    }
                });
        }
        else {
            showToast("Error while adding to favorite ads list");
        }
    }

    private void removeFromFavorites() {
        DocumentReference usersDocRef = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));
        if (adId != null) {
            usersDocRef.update(Constants.KEY_FAVORITES, FieldValue.arrayRemove(adId))
                    .addOnCompleteListener( task -> {
                        if (task.isSuccessful()) {
                            showToast("Removed from favorite ads list");
                            binding.imageRemoveFromFavorites.setVisibility(View.GONE);
                            binding.imageAddToFavorites.setVisibility(View.VISIBLE);
                        }
                        else {
                            showToast("Error while removing from favorite ads list");
                        }
                    });
        }
        else {
            showToast("Error while removing from favorite ads list");
        }
    }

    private boolean checkIfMyProfile() {
        boolean isMyProfile = ad.userId.equals(preferenceManager.getString(Constants.KEY_USER_ID));
        if (isMyProfile) {
            binding.imageRemoveFromFavorites.setVisibility(View.GONE);
            binding.imageAddToFavorites.setVisibility(View.GONE);
            binding.imageChat.setVisibility(View.GONE);
        }
        return isMyProfile;
    }

}