package com.example.booxchange.activities;

import static android.view.View.VISIBLE;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.booxchange.adapters.UsersAdapter;
import com.example.booxchange.databinding.ActivityUsersBinding;
import com.example.booxchange.listeners.UserListener;
import com.example.booxchange.models.Ad;
import com.example.booxchange.models.User;
import com.example.booxchange.utilities.Constants;
import com.example.booxchange.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FriendsActivity extends AppCompatActivity implements UserListener {

    private ActivityUsersBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        init();
        setListeners();
        getFriendsIds();
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }

    private void init() {
        database = FirebaseFirestore.getInstance();
    }
    
    private void getFriendsIds() {
        loading(true);
        ArrayList<String> usersIdsList = new ArrayList<>();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        try {
                            usersIdsList.addAll((ArrayList<String>) documentSnapshot.getData().get(Constants.KEY_FRIENDS));
                            getUsers(usersIdsList);
                        } catch (NullPointerException e) {
                            showErrorMessage();
                            loading(false);
                            e.printStackTrace();
                        }
                    }
                    else {
                        showErrorMessage();
                        loading(false);
                    }
                });
    }
    
    private void getUsers(ArrayList<String> usersIdsList) {
        if (!usersIdsList.isEmpty())
            database.collection(Constants.KEY_COLLECTION_USERS)
                    .get()
                    .addOnCompleteListener(task -> {
                        loading(false);
                        if (task.isSuccessful() && task.getResult() != null) {
                            List<User> usersList = new ArrayList<>();
                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                if (usersIdsList.contains(queryDocumentSnapshot.getId())) {
                                    User user = new User();
                                    user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                                    user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                                    user.image = queryDocumentSnapshot.getString(Constants.KEY_USER_IMAGE);
                                    user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                                    user.id = queryDocumentSnapshot.getId();
                                    usersList.add(user);
                                }
                            }
                            if (!usersList.isEmpty()) {
                                UsersAdapter usersAdapter = new UsersAdapter(usersList, this);
                                binding.usersRecyclerView.setAdapter(usersAdapter);
                                binding.usersRecyclerView.setVisibility(VISIBLE);
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
        binding.textErrorMessage.setText(String.format("%s", "No user available"));
        binding.textErrorMessage.setVisibility(VISIBLE);
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(VISIBLE);
        }
        else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
    }
}