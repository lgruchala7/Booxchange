package com.example.booxchange.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.booxchange.R;
import com.example.booxchange.databinding.ActivityMainBinding;
import com.example.booxchange.utilities.Constants;
import com.example.booxchange.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.util.HashMap;

import me.ibrahimsn.lib.OnItemSelectedListener;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
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

    private void showToast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    private void setListeners() {
//        binding.smoothBottomBar.setOnItemSelectedListener(new OnItemSelectedListener() {
//            @Override
//            public boolean onItemSelect(int i) {
//                switch (i) {
//                case Constants.MENU_MAP:
//                    startActivity(new Intent(MainActivity.this, MapsActivity.class));
//                    overridePendingTransition(0, 0);
//                    finish();
//                    break;
//                case Constants.MENU_ACCOUNT:
//                    startActivity(new Intent(MainActivity.this, AccountActivity.class));
//                    overridePendingTransition(0, 0);
//                    finish();
//                    break;
//                }
//                return false;
//            }
//        });
        binding.chipNavigationBar.setOnItemSelectedListener(i -> {
                if (i == Constants.MENU_MAP) {
                    startActivity(new Intent(MainActivity.this, MapsActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                } else if (i == Constants.MENU_ACCOUNT) {
                    startActivity(new Intent(MainActivity.this, AccountActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                }
        });
        binding.layoutFollowed.setOnClickListener( v -> {
            startActivity(new Intent(MainActivity.this, FriendsActivity.class));
        });
        binding.layoutLastChats.setOnClickListener( v -> {
            startActivity(new Intent(MainActivity.this, LastChatsActivity.class));
        });
        binding.layoutNewAd.setOnClickListener( v -> {
            startActivity(new Intent(MainActivity.this, NewAdActivity.class));
        });
        binding.layoutFavorites.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, FavoritesActivity.class));
        });
    }
        private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
                .addOnFailureListener(e -> showToast("Unable to update token"));
    }

}