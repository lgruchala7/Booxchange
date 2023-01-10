package com.example.booxchange.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.booxchange.databinding.ActivityUserInfoBinding;

public class UserInfoActivity extends AppCompatActivity {

    ActivityUserInfoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}