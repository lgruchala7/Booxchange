package com.example.booxchange.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.example.booxchange.R;
import com.example.booxchange.adapters.AdImagesAdapter;
import com.example.booxchange.databinding.ActivityNewAdBinding;
import com.example.booxchange.listeners.AddPhotoListener;
import com.example.booxchange.utilities.Constants;
import com.example.booxchange.utilities.PreferenceManager;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

public class NewAdActivity extends AppCompatActivity implements AddPhotoListener {

    private ActivityNewAdBinding binding;
    private ArrayList<String> encodedImages;
    private PreferenceManager preferenceManager;
    private AdImagesAdapter adImagesAdapter;

    private static final int MAX_AD_IMAGES = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewAdBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        setListeners();
    }

    private void init() {
        AutoCompleteTextView textViewGenre = binding.inputGenre;
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.genres, android.R.layout.simple_list_item_1);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        textViewGenre.setAdapter(adapter);

        preferenceManager = new PreferenceManager(getApplicationContext());
        encodedImages = new ArrayList<>();

        adImagesAdapter = new AdImagesAdapter(encodedImages, this);
        binding.adImagesRecyclerView.setAdapter(adImagesAdapter);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Drawable d = getResources().getDrawable(R.drawable.ic_add_photo_w_padding, getTheme());
        Bitmap addPhotoIconBitmap = drawableToBitmap(d);
//        Bitmap addPhotoIconBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_add_photo, getApplicationInfo().theme);
        addPhotoIconBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);

        byte[] bytes = byteArrayOutputStream.toByteArray();
        String addPhotoIconString = Base64.encodeToString(bytes, Base64.DEFAULT);
        encodedImages.add(addPhotoIconString);
        adImagesAdapter.notifyDataSetChanged();
    }

    private void showToast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.buttonPublish.setOnClickListener(v -> {
            if (isValidBookInputDetails()) {
                publishAd();
            }
        });
//        binding.imageAddPhoto.setOnClickListener(v -> {
//            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            pickImage.launch(intent);
//        });
//        binding.imageBook1.setOnClickListener(v -> {
//            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            pickImage.launch(intent);
//        });
//        binding.imageBook2.setOnClickListener(v -> {
//            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            pickImage.launch(intent);
//        });
//        binding.imageBook3.setOnClickListener(v -> {
//            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            pickImage.launch(intent);
//        });
    }

    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 350;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap adImageBitmap = BitmapFactory.decodeStream(inputStream);
//                            if (binding.layoutImageBook2.getVisibility() == View.GONE) {
//                                binding.imageBook1.setBackground(null);
//                                binding.imageBook1.setImageBitmap(bitmap);
//                                binding.textAddImage1.setVisibility(View.GONE);
//                                binding.imageBook1.setOnClickListener(null);
//                                binding.layoutImageBook2.setVisibility(View.VISIBLE);
//                            }
//                            else if (binding.layoutImageBook3.getVisibility() == View.GONE) {
//                                binding.imageBook2.setBackground(null);
//                                binding.imageBook2.setImageBitmap(bitmap);
//                                binding.textAddImage2.setVisibility(View.GONE);
//                                binding.imageBook2.setOnClickListener(null);
//                                binding.layoutImageBook3.setVisibility(View.VISIBLE);
//                            }
//                            else {
//                                binding.imageBook3.setBackground(null);
//                                binding.imageBook3.setImageBitmap(bitmap);
//                                binding.textAddImage3.setVisibility(View.GONE);
//                                binding.imageBook3.setOnClickListener(null);
//                            }

                            encodedImages.add(encodeImage(adImageBitmap));
//                            Collections.swap(encodedImages, encodedImages.size() - 1, encodedImages.size() - 2);

                            adImagesAdapter.notifyDataSetChanged();
                            binding.adImagesRecyclerView.smoothScrollToPosition(0);
//                            binding.adImagesRecyclerView.setVisibility(View.VISIBLE);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private boolean isValidBookInputDetails() {
        if (binding.inputTitle.getText().toString().trim().isEmpty()) {
            showToast("Enter title");
        }
        else if (binding.inputAuthor.getText().toString().trim().isEmpty()) {
            showToast("Enter author");
        }
        else if (binding.inputGenre.getText().toString().trim().isEmpty()) {
            showToast("Enter genre");
        }
        else if (binding.inputCity.getText().toString().trim().isEmpty()) {
            showToast("Enter city");
        }
        else if (binding.inputAddress.getText().toString().trim().isEmpty()) {
            showToast("Enter address");
        }
        else {
            return true;
        }
        return false;
    }

    private void loading (boolean isLoading) {
        if (isLoading) {
            binding.buttonPublish.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonPublish.setVisibility(View.VISIBLE);
        }
    }

    private void publishAd() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> bookAd = new HashMap<>();
        bookAd.put(Constants.KEY_TITLE, binding.inputTitle.getText().toString().trim());
        bookAd.put(Constants.KEY_AUTHOR, binding.inputAuthor.getText().toString().trim());
        bookAd.put(Constants.KEY_GENRE, binding.inputGenre.getText().toString().trim());
        bookAd.put(Constants.KEY_CITY, binding.inputCity.getText().toString().trim());
        bookAd.put(Constants.KEY_ADDRESS, binding.inputAddress.getText().toString().trim());
        bookAd.put(Constants.KEY_TIMESTAMP, new Date());
        bookAd.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        bookAd.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME));
        bookAd.put(Constants.KEY_CONDITION, Integer.toString(binding.inputCondition.getNumStars()));
        bookAd.put(Constants.KEY_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
        bookAd.put(Constants.KEY_USER_IMAGE, preferenceManager.getString(Constants.KEY_USER_IMAGE));
        if (!binding.inputDescription.getText().toString().isEmpty()) {
            bookAd.put(Constants.KEY_DESCRIPTION, binding.inputDescription.getText().toString().trim());
        }
        if (!encodedImages.isEmpty()) {
            bookAd.put(Constants.KEY_IMAGE, encodedImages.subList(1, encodedImages.size() - 1));
        }

        database.collection(Constants.KEY_COLLECTION_ADS)
                .add(bookAd)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
//                    preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
//                    preferenceManager.putString(Constants.KEY_NAME, binding.inputName.getText().toString().trim());
//                    preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
//                    finish();
                })
                .addOnFailureListener(exception -> {
                    loading(false);
                    showToast(exception.getMessage());
                });
    }

    @Override
    public void onAddPhotoClicked() {
        if (encodedImages.size() < MAX_AD_IMAGES + 1) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        }
        else {
            showToast("Max number of ad images");
        }
    }
}