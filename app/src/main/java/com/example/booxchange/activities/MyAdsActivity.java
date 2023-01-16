package com.example.booxchange.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.booxchange.adapters.AdsMiniaturesAdapter;
import com.example.booxchange.databinding.ActivityMyAdsBinding;
import com.example.booxchange.listeners.AdListener;
import com.example.booxchange.models.Ad;
import com.example.booxchange.utilities.Constants;
import com.example.booxchange.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyAdsActivity extends BaseActivity implements AdListener {

    ActivityMyAdsBinding binding;
    FirebaseFirestore database;
    PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyAdsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        getMyAds();
        setListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getMyAds();
    }

    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }

    private void getMyAds() {
        loading(true);
        database.collection(Constants.KEY_COLLECTION_ADS)
                .whereEqualTo(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Ad> adList = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult().getDocuments()) {
                            Ad ad = new Ad();
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
                            adList.add(ad);
                        }
                        if (!adList.isEmpty()) {
                            Collections.sort(adList, (obj1, obj2) -> obj2.dateObject.compareTo(obj1.dateObject));
                            AdsMiniaturesAdapter adsMiniaturesAdapter = new AdsMiniaturesAdapter(adList, this,true);
                            binding.adsRecyclerView.setAdapter(adsMiniaturesAdapter);
                            binding.adsRecyclerView.setVisibility(View.VISIBLE);
                            loading(false);
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

    private void showErrorMessage() {
        loading(false);
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
        Intent intent = new Intent(MyAdsActivity.this, FullAdInfoActivity.class);
        intent.putExtra(Constants.KEY_AD, ad);
        startActivity(intent);
    }
}