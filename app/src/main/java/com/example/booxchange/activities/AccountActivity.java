package com.example.booxchange.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;

import com.example.booxchange.databinding.ActivityAccountBinding;
import com.example.booxchange.models.User;
import com.example.booxchange.utilities.Constants;
import com.example.booxchange.utilities.PreferenceManager;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class AccountActivity extends BaseActivity {

    private ActivityAccountBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        getUserData();
        setListeners();
    }

    private void init() {
        int menuItemCount = binding.chipNavigationBar.getChildCount();
        for (int i = 0; i < menuItemCount; i++) {
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
        binding.chipNavigationBar.setItemSelected(Constants.MENU_ACCOUNT, true);
        preferenceManager = new PreferenceManager(getApplicationContext());
    }

    private void getUserData() {
        binding.userInfo.imageProfile.setImageBitmap(getUserImage(preferenceManager.getString(Constants.KEY_USER_IMAGE)));
        binding.userInfo.textName.setText(preferenceManager.getString(Constants.KEY_NAME));
        binding.userInfo.textEmail.setText(preferenceManager.getString(Constants.KEY_EMAIL));
    }

    private void setListeners() {
        binding.chipNavigationBar.setOnItemSelectedListener(i -> {
            if (i == Constants.MENU_MAP) {
                startActivity(new Intent(AccountActivity.this, MapsActivity.class));
                overridePendingTransition(0, 0);
                finish();
            }
            else if (i == Constants.MENU_HOME) {
                startActivity(new Intent(AccountActivity.this, HomeActivity.class));
                overridePendingTransition(0, 0);
                finish();
            }
        });

        binding.layoutGoToMyAds.setOnClickListener(v -> startActivity(new Intent(AccountActivity.this, MyAdsActivity.class)));
        binding.layoutGoToUserInfo.setOnClickListener(v -> {
            User user = new User(preferenceManager.getString(Constants.KEY_USER_ID),
                    preferenceManager.getString(Constants.KEY_NAME),
                    preferenceManager.getString(Constants.KEY_USER_IMAGE));
            Intent intent = new Intent(AccountActivity.this, UserInfoActivity.class);
            intent.putExtra(Constants.KEY_USER, user);
            startActivity(intent);
//            overridePendingTransition(0, 0);
        });
        binding.layoutSignOut.setOnClickListener( v -> signOut() );
    }

    private void signOut() {
        showSnackbar("Signing out...");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    preferenceManager.clear();
                    startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> showSnackbar("Unable to sign out"));
    }

    private void showSnackbar(String text) {
        Snackbar.make(getApplicationContext(), binding.getRoot(), text, Toast.LENGTH_SHORT).show();
    }

    private Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}