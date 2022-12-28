package com.example.booxchange.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.booxchange.R;
import com.example.booxchange.databinding.ActivityMainBinding;

import me.ibrahimsn.lib.OnItemSelectedListener;
import me.ibrahimsn.lib.SmoothBottomBar;

public class MainActivity extends AppCompatActivity {

    private static final int MENU_HOME = 0;
    private static final int MENU_MAP = 1;
    private static final int MENU_ACCOUNT = 2;

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SmoothBottomBar smoothBottomBar = (SmoothBottomBar)findViewById(R.id.smooth_bottom_bar);
        smoothBottomBar.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public boolean onItemSelect(int i) {
                if (i == MENU_MAP) {
                    Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                    startActivity(intent);
                    finish();
                }
                return false;
            }
        });
    }
}