package com.example.booxchange.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.widget.Toast;

import com.example.booxchange.databinding.ActivitySettingsBinding;
import com.example.booxchange.utilities.Constants;
import com.example.booxchange.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class SettingsActivity extends BaseActivity {

    ActivitySettingsBinding binding;
    FirebaseFirestore database;
    PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        setListeners();
    }

    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.layoutGoToLocationSettings.setOnClickListener( v -> {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        });
        binding.layoutGoToNotificationSettings.setOnClickListener( v -> {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_ASSISTANT_SETTINGS);
            startActivity(intent);
        });
        binding.layoutModifyUserInfo.setOnClickListener( v -> {

        });
        binding.layoutDeleteAccount.setOnClickListener( v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Confirm delete operation")
                    .setMessage("Do you want to delete your account permanently?")
                    .setCancelable(true)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteMyAccount();
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

    private final ActivityResultLauncher<Intent> takePicture = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != RESULT_OK) {
                    showToast("Problem occurred when accessing system settings");
                }
            }
    );

    private void showToast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    private void deleteMyAccount() {
        String userId = preferenceManager.getString(Constants.KEY_USER_ID);

        database.collection(Constants.KEY_COLLECTION_ADS)
                .whereEqualTo(Constants.KEY_USER_ID, userId)
                .get()
                .addOnCompleteListener( getDocumentTask -> {
                    if (getDocumentTask.isSuccessful() && getDocumentTask.getResult() != null) {
                        for (DocumentSnapshot document : getDocumentTask.getResult().getDocuments()) {
                            String adId = document.getId();
                            database.collection(Constants.KEY_COLLECTION_ADS)
                                    .document(adId)
                                    .delete();
                        }
                    }
                    else {
                        showToast("Error occurred while deleting account");
                        return;
                    }
                });

        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(userId)
                .delete()
                .addOnCompleteListener( task -> {
                    if (task.isSuccessful()) {
                        showToast("Account successfully deleted");
                        preferenceManager.clear();
                        startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                        finish();
                    }
                    else {
                        showToast("Failed to delete account");
                    }
                } );
    }

}