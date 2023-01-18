package com.example.booxchange.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.booxchange.databinding.ActivityHomeBinding;
import com.example.booxchange.utilities.Constants;
import com.example.booxchange.utilities.PreferenceManager;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class HomeActivity extends BaseActivity {

    private ActivityHomeBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        init();
        setListeners();
//        loadUserDetails();
        getToken();
    }

//    private void loadUserDetails() {
//        binding.welcomeText.setText("Welcome " + preferenceManager.getString(Constants.KEY_NAME));
//        try {
//            byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_USER_IMAGE), Base64.DEFAULT);
//            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//            binding.imageProfile.setImageBitmap(bitmap);
//        } catch (NullPointerException e) {
//            e.printStackTrace();
//        }
//    }

    private void init() {
        for (int i = 0; i < binding.chipNavigationBar.getChildCount(); i++) {
            switch (i) {
                case 0:
                    Constants.MENU_HOME = binding.chipNavigationBar.getChildAt(i).getId();
                    break;
                case 1:
                    Constants.MENU_MAP = binding.chipNavigationBar.getChildAt(i).getId();
                    break;
                case 2:
                    Constants.MENU_ACCOUNT = binding.chipNavigationBar.getChildAt(i).getId();
                    break;
            }
        }
        binding.chipNavigationBar.setItemSelected(Constants.MENU_HOME, true);
    }

    private void showSnackbar(String text) {
        Snackbar.make(getApplicationContext(), binding.getRoot(), text, Toast.LENGTH_SHORT).show();
    }

    private void setListeners() {
        binding.chipNavigationBar.setOnItemSelectedListener(i -> {
                if (i == Constants.MENU_MAP) {
                    startActivity(new Intent(HomeActivity.this, MapsActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                } else if (i == Constants.MENU_ACCOUNT) {
                    startActivity(new Intent(HomeActivity.this, AccountActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                }
        });
        binding.layoutFriends.setOnClickListener( v ->
            startActivity(new Intent(HomeActivity.this, FriendsActivity.class))
        );
        binding.layoutLastChats.setOnClickListener( v ->
            startActivity(new Intent(HomeActivity.this, LastChatsActivity.class))
        );
        binding.layoutNewAd.setOnClickListener( v ->
            startActivity(new Intent(HomeActivity.this, NewAdActivity.class))
        );
        binding.layoutFavorites.setOnClickListener(v ->
            startActivity(new Intent(HomeActivity.this, FavoritesActivity.class))
        );
        binding.layoutSettings.setOnClickListener( v ->
                startActivity(new Intent(HomeActivity.this, SettingsActivity.class))
        );
    }
        private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token) {
        preferenceManager.putString(Constants.KEY_FCM_TOKEN, token);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
                .addOnFailureListener(e -> showSnackbar("Unable to update token"));
    }

}