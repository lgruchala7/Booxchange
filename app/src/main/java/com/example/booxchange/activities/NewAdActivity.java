package com.example.booxchange.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
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
import com.example.booxchange.listeners.AdImageListaner;
import com.example.booxchange.utilities.Constants;
import com.example.booxchange.utilities.PreferenceManager;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class NewAdActivity extends BaseActivity implements AdImageListaner {

    private ActivityNewAdBinding binding;
    private ArrayList<String> encodedImages;
    private PreferenceManager preferenceManager;
    private AdImagesAdapter adImagesAdapter;

    private static final int MAX_AD_IMAGES = 5;
    private static final int REQUEST_CAMERA = 100;

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

        Drawable d = getResources().getDrawable(R.drawable.ic_add_photo_w_padding, getTheme());
        Bitmap addPhotoIconBitmap = drawableToBitmap(d);
        String addPhotoIconString = encodeImage(addPhotoIconBitmap);
        encodedImages = new ArrayList<>();
        encodedImages.add(addPhotoIconString);
        adImagesAdapter = new AdImagesAdapter(encodedImages, this, true);
        binding.adImagesRecyclerView.setAdapter(adImagesAdapter);
    }

    private void showSnackbar(String text) {
        Snackbar.make(NewAdActivity.this, binding.getRoot(), text, Toast.LENGTH_SHORT).show();
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.buttonPublish.setOnClickListener(v -> {
            if (isValidBookInputDetails()) {
                publishAd();
            }
        });
        binding.imageClose.setOnClickListener( v -> onBackPressed() );
        binding.imagePhoto.setOnClickListener( v -> {
            requestCameraPermissions();
        });
    }

    private void requestCameraPermissions() {
        if (ContextCompat.checkSelfPermission(NewAdActivity.this,
        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(NewAdActivity.this, new String[]{
                    Manifest.permission.CAMERA}, REQUEST_CAMERA);
        }
        else {
            takePicture();
        }
    }

    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 350;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.PNG, 75, byteArrayOutputStream);
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

    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        getPicture.launch(takePictureIntent);
    }

    private final ActivityResultLauncher<Intent> getPicture = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),   result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Bitmap pictureBitmap = (Bitmap) result.getData().getExtras().get("data");

                        Matrix matrix = new Matrix();
                        matrix.postRotate(90);
                        Bitmap rotatedBitmap = Bitmap.createBitmap(pictureBitmap, 0, 0,
                                pictureBitmap.getWidth(), pictureBitmap.getHeight(), matrix, true);

                        if (rotatedBitmap != null) {
                            String pictureString = encodeImage(rotatedBitmap);
                            encodedImages.add(pictureString);
                            adImagesAdapter.notifyDataSetChanged();
                            binding.adImagesRecyclerView.smoothScrollToPosition(0);
                        }
                    }
            }
    );

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data.getClipData() != null) {
                        int imageCount = data.getClipData().getItemCount();
                        for (int i = 0; i < imageCount; i++) {
                            try {
                                Uri imageUri = data.getClipData().getItemAt(i).getUri();
                                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                                Bitmap adImageBitmap = BitmapFactory.decodeStream(inputStream);
                                encodedImages.add(encodeImage(adImageBitmap));

                                if (encodedImages.size() == MAX_AD_IMAGES + 1) {
                                    showSnackbar("Max number of images");
                                    break;
                                }

                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                        adImagesAdapter.notifyDataSetChanged();
                        binding.adImagesRecyclerView.smoothScrollToPosition(0);
                        binding.adImagesRecyclerView.setVisibility(View.VISIBLE);
                    }
                    else if (data.getData() != null) {
                        try {
                            Uri imageUri = data.getData();
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap adImageBitmap = BitmapFactory.decodeStream(inputStream);
                            encodedImages.add(encodeImage(adImageBitmap));

                            adImagesAdapter.notifyDataSetChanged();
                            binding.adImagesRecyclerView.smoothScrollToPosition(0);
                            binding.adImagesRecyclerView.setVisibility(View.VISIBLE);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private boolean isValidBookInputDetails() {
        if (binding.inputTitle.getText().toString().trim().isEmpty()) {
            showSnackbar("Enter title");
        }
        else if (binding.inputAuthor.getText().toString().trim().isEmpty()) {
            showSnackbar("Enter author");
        }
        else if (binding.inputGenre.getText().toString().trim().isEmpty()) {
            showSnackbar("Enter genre");
        }
        else if (binding.inputCity.getText().toString().trim().isEmpty()) {
            showSnackbar("Enter city");
        }
        else if (binding.inputAddress.getText().toString().trim().isEmpty()) {
            showSnackbar("Enter address");
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
        bookAd.put(Constants.KEY_EMAIL, preferenceManager.getString(Constants.KEY_EMAIL));
        bookAd.put(Constants.KEY_CONDITION, Integer.toString(binding.inputCondition.getNumStars()));
        bookAd.put(Constants.KEY_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
        bookAd.put(Constants.KEY_USER_IMAGE, preferenceManager.getString(Constants.KEY_USER_IMAGE));
        if (!binding.inputDescription.getText().toString().isEmpty()) {
            bookAd.put(Constants.KEY_DESCRIPTION, binding.inputDescription.getText().toString().trim());
        }
        if (!encodedImages.isEmpty()) {
            bookAd.put(Constants.KEY_IMAGE, encodedImages.subList(1, encodedImages.size()));
        }

        database.collection(Constants.KEY_COLLECTION_ADS)
                .add(bookAd)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
//                    preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
//                    preferenceManager.putString(Constants.KEY_NAME, binding.inputName.getText().toString().trim());
//                    preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
//                    finish();
                })
                .addOnFailureListener(exception -> {
                    loading(false);
                    showSnackbar(exception.getMessage());
                });
    }

    public void onAddPhotoClicked() {
        if (encodedImages.size() < MAX_AD_IMAGES + 1) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            pickImage.launch(intent);
        }
        else {
            showSnackbar("Max number of images");
        }
    }

    @Override
    public void onAdImageClicked(View imageView) {
        if (imageView.getTag() == "image_add_photo") {
            onAddPhotoClicked();
        }
        else {
            AppCompatImageView imageFullScreen = binding.imageFullScreen;
            AppCompatImageView imageClose = binding.imageClose;
            Bitmap bitmap = ((BitmapDrawable) ((AppCompatImageView) imageView).getDrawable()).getBitmap();
            imageFullScreen.setImageBitmap(bitmap);
            imageFullScreen.setVisibility(View.VISIBLE);
            imageClose.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDeleteIconClicked(View imageView) {
        Object tag = imageView.getTag();
        if (tag != null) {
            try {
                int index = (int) tag;
                encodedImages.remove(index);
                adImagesAdapter.notifyDataSetChanged();
            } catch (ClassCastException e)
            {
                showSnackbar("Could not delete the image");
            }
        }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePicture();
                }
                break;
        }
    }
}