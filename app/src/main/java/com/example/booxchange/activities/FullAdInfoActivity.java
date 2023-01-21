package com.example.booxchange.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class FullAdInfoActivity extends BaseActivity implements AdImageListaner {

    private ActivityFullAdInfoBinding binding;
    private Ad ad;
    String adId;
    private AdImagesAdapter adImagesAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private boolean isMyProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFullAdInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
//        loadAdDetails();
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

        adImagesAdapter = new AdImagesAdapter(ad.images, this, false);
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
        binding.imageDelete.setOnClickListener( v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Confirm removal")
                .setMessage("Do you want to remove the ad permanently?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeFromMyAds();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
        });
    }

    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();

        ad = new Ad();
        ad.dateObject = (Date) getIntent().getSerializableExtra(Constants.KEY_TIMESTAMP);
        ad.userId = (String) getIntent().getSerializableExtra(Constants.KEY_USER_ID);
        ad.isInFavorites = (boolean) getIntent().getSerializableExtra(Constants.KEY_FAVORITES);
//        if (!isMyProfile) {
            getAdInfo();
//        }
    }

//    private void checkIfAdFavorite() {
//        database.collection(Constants.KEY_COLLECTION_ADS)
//                .whereEqualTo(Constants.KEY_USER_ID, ad.userId)
//                .whereEqualTo(Constants.KEY_TIMESTAMP, ad.dateObject)
//                .get()
//                .addOnCompleteListener( task -> getAdInfo(task) );
//    }
    
    private void getAdInfo() {
        database.collection(Constants.KEY_COLLECTION_ADS)
                .whereEqualTo(Constants.KEY_USER_ID, ad.userId)
                .whereEqualTo(Constants.KEY_TIMESTAMP, ad.dateObject)
                .get()
                .addOnCompleteListener( task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);

                        ad.title = document.getString(Constants.KEY_TITLE);
                        ad.author = document.getString(Constants.KEY_AUTHOR);
                        ad.genre = document.getString(Constants.KEY_GENRE);
                        ad.condition = document.getString(Constants.KEY_CONDITION);
                        ad.description = document.getString(Constants.KEY_DESCRIPTION);
                        ad.city = document.getString(Constants.KEY_CITY);
                        ad.address = document.getString(Constants.KEY_ADDRESS);
                        ad.images = (ArrayList<String>) document.get(Constants.KEY_IMAGE);
                        ad.dateTime = getReadableDateTime(document.getDate(Constants.KEY_TIMESTAMP));
                        ad.dateObject = document.getDate(Constants.KEY_TIMESTAMP);
                        ad.userId = document.getString(Constants.KEY_USER_ID);
                        ad.userName = document.getString(Constants.KEY_NAME);
                        ad.userImage = document.getString(Constants.KEY_USER_IMAGE);
                        ad.userEmail = document.getString(Constants.KEY_EMAIL);

                        adId = document.getId();
                        loadAdDetails();

                        isMyProfile = checkIfMyProfile();
                    }
                    else {
                        showSnackbar("Failed to load ad info");
                        new Handler(Looper.getMainLooper())
                                .postDelayed( () -> onBackPressed(), 500);
                    }
                });
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
                    binding.imageDelete.setVisibility(View.GONE);
                    binding.imageAddToFavorites.setVisibility(View.GONE);
                    binding.imageRemoveFromFavorites.setVisibility(View.VISIBLE);
                } else {
                    binding.imageDelete.setVisibility(View.GONE);
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

    private void showSnackbar(String text) {
        Snackbar.make(FullAdInfoActivity.this, binding.getRoot(), text, Toast.LENGTH_SHORT).show();
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
                        showSnackbar("Added to favorite ads list");
                        binding.imageAddToFavorites.setVisibility(View.GONE);
                        binding.imageRemoveFromFavorites.setVisibility(View.VISIBLE);
                    }
                    else {
                        showSnackbar("Error while adding to favorite ads list");
                    }
                });
        }
        else {
            showSnackbar("Error while adding to favorite ads list");
        }
    }

    private void removeFromFavorites() {
        DocumentReference usersDocRef = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));
        if (adId != null) {
            usersDocRef.update(Constants.KEY_FAVORITES, FieldValue.arrayRemove(adId))
                    .addOnCompleteListener( task -> {
                        if (task.isSuccessful()) {
                            showSnackbar("Removed from favorite ads list");
                            binding.imageRemoveFromFavorites.setVisibility(View.GONE);
                            binding.imageAddToFavorites.setVisibility(View.VISIBLE);
                        }
                        else {
                            showSnackbar("Error while removing from favorite ads list");
                        }
                    });
        }
        else {
            showSnackbar("Error while removing from favorite ads list");
        }
    }

    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("dd MMMM, yyyy - kk:mm", Locale.getDefault()).format(date);
    }

    private boolean checkIfMyProfile() {
        boolean isMyProfile = ad.userId.equals(preferenceManager.getString(Constants.KEY_USER_ID));
        if (isMyProfile) {
            binding.imageRemoveFromFavorites.setVisibility(View.GONE);
            binding.imageAddToFavorites.setVisibility(View.GONE);
            binding.imageChat.setVisibility(View.GONE);
            binding.imageDelete.setVisibility(View.VISIBLE);
        }
        else {
            database.collection(Constants.KEY_COLLECTION_USERS)
                    .document(preferenceManager.getString(Constants.KEY_USER_ID))
                    .get()
                    .addOnCompleteListener(getDocumentTask -> setImageFavoriteIndicator(getDocumentTask));
        }
        return isMyProfile;
    }

    private void removeFromMyAds() {
        if (adId == null) {
            database.collection(Constants.KEY_COLLECTION_ADS)
                    .whereEqualTo(Constants.KEY_USER_ID, ad.userId)
                    .whereEqualTo(Constants.KEY_TIMESTAMP, ad.dateObject)
                    .get()
                    .addOnCompleteListener( getDocumentTask -> {
                        if (getDocumentTask.isSuccessful() && getDocumentTask.getResult() != null) {
                            adId = getDocumentTask.getResult().getDocuments().get(0).getId();
                            database.collection(Constants.KEY_COLLECTION_ADS)
                                    .document(adId)
                                    .delete()
                                    .addOnCompleteListener( task -> {
                                        if (task.isSuccessful()) {
                                            showSnackbar("Ad successfully removed");
                                            removeFromAllFavorites(adId);
                                        }
                                        else {
                                            showSnackbar("Failed to remove ad");
                                        }
                                    } );
                        }
                        else {
                            showSnackbar("Failed to remove ad");
                        }
                    });
        }
        else {
            database.collection(Constants.KEY_COLLECTION_ADS)
                    .document(adId)
                    .delete()
                    .addOnCompleteListener( task -> {
                        if (task.isSuccessful()) {
                            showSnackbar("Ad successfully removed");
                            removeFromAllFavorites(adId);
                        }
                        else {
                            showSnackbar("Failed to remove ad");
                        }
                    } );
        }
    }

    private void removeFromAllFavorites(String  adId) {
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereArrayContains(Constants.KEY_FAVORITES, adId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<DocumentSnapshot> documentList = task.getResult().getDocuments();
                        if (documentList.isEmpty()) {
                            onBackPressed();
                        }
                        else {
                            for (DocumentSnapshot document : documentList) {
                                String documentId = document.getId();
                                database.collection(Constants.KEY_COLLECTION_USERS)
                                        .document(documentId)
                                        .update(Constants.KEY_FAVORITES, FieldValue.arrayRemove(adId))
                                        .addOnCompleteListener(v -> {
                                            onBackPressed();
                                        });
                            }
                        }
                    }
                    else {
                        onBackPressed();
                    }
                });
    }

    @Override
    public void onDeleteIconClicked(View imageView) {
        // NOT USED
    }

}