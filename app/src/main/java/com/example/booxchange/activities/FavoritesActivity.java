package com.example.booxchange.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.example.booxchange.adapters.FavoriteItemsAdapter;
import com.example.booxchange.databinding.ActivityFavoritesBinding;
import com.example.booxchange.listeners.AdListener;
import com.example.booxchange.models.Ad;
import com.example.booxchange.models.User;
import com.example.booxchange.utilities.Constants;
import com.example.booxchange.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FavoritesActivity extends AppCompatActivity implements AdListener {

    private ActivityFavoritesBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFavoritesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        init();
        setListeners();
        getFavoriteAdsIds();
    }

    private void init() {
//        favoriteItems = new ArrayList<>();
//        favoriteItemsAdapter = new FavoriteItemsAdapter(favoriteItems, this);
//        binding.adsRecyclerView.setAdapter(favoriteItemsAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }

    private void getFavoriteAdsIds() {
        loading(true);
        ArrayList<String> favoriteAdsIdsList = new ArrayList<>();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        try {
                            favoriteAdsIdsList.addAll( (ArrayList<String>) documentSnapshot.getData().get(Constants.KEY_FAVORITES) );
                            getFavoriteAds(favoriteAdsIdsList);
                        } catch (NullPointerException e) {
                            showErrorMessage();
                            e.printStackTrace();
                        }
                    }
                    else {
                        showErrorMessage();
                    }
                });
    }

    private void getFavoriteAds(ArrayList<String> favoriteAdsIdsList) {
        if (!favoriteAdsIdsList.isEmpty()) {
            database.collection(Constants.KEY_COLLECTION_ADS)
                    .get()
                    .addOnCompleteListener(task -> {
                        loading(false);
                        if (task.isSuccessful() && task.getResult() != null) {
                            List<Ad> adList = new ArrayList<>();
                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                if (favoriteAdsIdsList.contains(queryDocumentSnapshot.getId())) {
                                    Ad ad = new Ad();
                                    ad.title = queryDocumentSnapshot.getString(Constants.KEY_TITLE);
                                    ad.author = queryDocumentSnapshot.getString(Constants.KEY_AUTHOR);
                                    ad.genre = queryDocumentSnapshot.getString(Constants.KEY_GENRE);
                                    ad.condition = queryDocumentSnapshot.getString(Constants.KEY_CONDITION);
                                    ad.description = queryDocumentSnapshot.getString(Constants.KEY_DESCRIPTION);
                                    ad.city = queryDocumentSnapshot.getString(Constants.KEY_CITY);
                                    ad.address = queryDocumentSnapshot.getString(Constants.KEY_ADDRESS);
                                    ad.images = (ArrayList<String>) queryDocumentSnapshot.get(Constants.KEY_IMAGE);
                                    ad.dateTime = getReadableDateTime(queryDocumentSnapshot.getDate(Constants.KEY_TIMESTAMP));
                                    ad.dateObject = queryDocumentSnapshot.getDate(Constants.KEY_TIMESTAMP);
                                    ad.userId = queryDocumentSnapshot.getString(Constants.KEY_USER_ID);
                                    ad.userName = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                                    ad.userImage = queryDocumentSnapshot.getString(Constants.KEY_USER_IMAGE);
                                    adList.add(ad);
                                }
                            }
                            if (!adList.isEmpty()) {
                                Collections.sort(adList, (obj1, obj2) -> obj2.dateObject.compareTo(obj1.dateObject));
                                FavoriteItemsAdapter favoriteItemsAdapter = new FavoriteItemsAdapter(adList, this);
                                binding.adsRecyclerView.setAdapter(favoriteItemsAdapter);
                                binding.adsRecyclerView.setVisibility(View.VISIBLE);
                            }
                            else {
                                showErrorMessage();
                            }
                        }
                        else {
                            showErrorMessage();
                        }
                    });
        }
    }

    private void showErrorMessage() {
        binding.textErrorMessage.setText(String.format("%s", "No ads available"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("dd MMMM, yyyy - kk:mm", Locale.getDefault()).format(date);
    }

    @Override
    public void onAdClicked(Ad ad) {
        //TODO
    }
}