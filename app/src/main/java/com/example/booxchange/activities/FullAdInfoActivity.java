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

import com.example.booxchange.adapters.AdImagesAdapter;
import com.example.booxchange.databinding.ActivityFullAdInfoBinding;

import com.example.booxchange.listeners.AdImageListaner;
import com.example.booxchange.models.Ad;
import com.example.booxchange.models.User;
import com.example.booxchange.utilities.Constants;

public class FullAdInfoActivity extends AppCompatActivity implements AdImageListaner {

    private ActivityFullAdInfoBinding binding;
    private Ad ad;
    private AdImagesAdapter adImagesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFullAdInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadAdDetails();
        init();
        setListeners();
    }

    private void loadAdDetails() {
        ad = (Ad) getIntent().getSerializableExtra(Constants.KEY_AD);

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

//        ArrayList<String> adImages = ad.images;
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
        binding.imageClose.setOnClickListener(v -> onBackPressed() );
    }

    private void init() {

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

}