package com.example.booxchange.activities;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationRequest;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.booxchange.R;
import com.example.booxchange.adapters.AdInfoWindowAdapter;
import com.example.booxchange.databinding.ActivityMapsBinding;
import com.example.booxchange.models.Ad;
import com.example.booxchange.models.User;
import com.example.booxchange.utilities.Constants;
import com.example.booxchange.utilities.PreferenceManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {

    private static final int REQUEST_CODE = 101;

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private Location currentLocation;
    private boolean isPermissionGranted;
    private List<Ad> ads;
    private PreferenceManager preferenceManager;
    private Marker currentMarker;
    FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getCurrentLocation();

        if (isPermissionGranted) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }

        init();
        setListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();

//        binding.smoothBottomBar.setItemActiveIndex(Constants.MENU_MAP);
    }

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
        binding.chipNavigationBar.setItemSelected(Constants.MENU_MAP, true);
        database = FirebaseFirestore.getInstance();
        ads = new ArrayList<>();
        preferenceManager = new PreferenceManager(getApplicationContext());
    }

    private void setListeners() {
        binding.chipNavigationBar.setOnItemSelectedListener( i -> {
            if (i == Constants.MENU_HOME) {
                startActivity(new Intent(MapsActivity.this, HomeActivity.class));
                overridePendingTransition(0, 0);
                finish();
            }
            else if (i == Constants.MENU_ACCOUNT) {
                startActivity(new Intent(MapsActivity.this, AccountActivity.class));
                overridePendingTransition(0, 0);
                finish();
            }
        });
        binding.fabStartChat.setOnClickListener(v -> {
            if (currentMarker != null) {
                int adIndex = Integer.parseInt(currentMarker.getSnippet());
                Ad ad = ads.get(adIndex);

                User user = new User(ad.userId, ad.userName, ad.userImage);

                Intent intent = new Intent(MapsActivity.this, ChatActivity.class);
                intent.putExtra(Constants.KEY_USER, user);
                startActivity(intent);
            }
        });
        binding.fabAddToFav.setOnClickListener(v -> {
            int adIndex = Integer.parseInt(currentMarker.getSnippet());
            getFavoriteAdId(adIndex);
        });
    }

    private void listenAds() {
        database.collection(Constants.KEY_COLLECTION_ADS).addSnapshotListener((value, error) -> {
            if (error != null) {
                return;
            }
            if (value != null) {
                for (DocumentChange documentChange : value.getDocumentChanges()) {
                    if (documentChange.getType() == DocumentChange.Type.ADDED) {
                        String title = documentChange.getDocument().getString(Constants.KEY_TITLE);
                        String author = documentChange.getDocument().getString(Constants.KEY_AUTHOR);
                        String genre = documentChange.getDocument().getString(Constants.KEY_GENRE);
                        String condition = documentChange.getDocument().getString(Constants.KEY_CONDITION);
                        String description = documentChange.getDocument().getString(Constants.KEY_DESCRIPTION);
                        String city = documentChange.getDocument().getString(Constants.KEY_CITY);
                        String address = documentChange.getDocument().getString(Constants.KEY_ADDRESS);
                        ArrayList<String> images = new ArrayList<>();
                        for (String image : (ArrayList<String>) documentChange.getDocument().get(Constants.KEY_IMAGE))
                        {
                            images.add(image);
                        }
//                                documentChange.getDocument().get (Constants.KEY_IMAGE);
                        String dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                        Date dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                        String userId = documentChange.getDocument().getString(Constants.KEY_USER_ID);
                        String userName = documentChange.getDocument().getString(Constants.KEY_NAME);
                        String userImage = documentChange.getDocument().getString(Constants.KEY_USER_IMAGE);
                        String userEmail = documentChange.getDocument().getString(Constants.KEY_EMAIL);

                        if (!userId.equals(preferenceManager.getString(Constants.KEY_USER_ID))) {
                            Ad ad = new Ad();
                            ad.title = title;
                            ad.author = author;
                            ad.genre = genre;
                            ad.condition = condition;
                            ad.description = description;
                            ad.city = city;
                            ad.address = address;
                            ad.images = images;
                            ad.dateTime = dateTime;
                            ad.dateObject = dateObject;
                            ad.userName = userName;
                            ad.userId = userId;
                            ad.userImage = userImage;
                            ad.userEmail = userEmail;

                            ads.add(ad);
                        }
                    }
                    else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                        for (int i = 0; i < ads.size(); i++) { //TODO - probable bug

                            String title = documentChange.getDocument().getString(Constants.KEY_TITLE);
                            String author = documentChange.getDocument().getString(Constants.KEY_AUTHOR);
                            String genre = documentChange.getDocument().getString(Constants.KEY_GENRE);
                            String condition = documentChange.getDocument().getString(Constants.KEY_CONDITION);
                            String description = documentChange.getDocument().getString(Constants.KEY_DESCRIPTION);
                            String city = documentChange.getDocument().getString(Constants.KEY_CITY);
                            String address = documentChange.getDocument().getString(Constants.KEY_ADDRESS);
                            ArrayList<String> images = new ArrayList<>();
                            for (String image : (ArrayList<String>) documentChange.getDocument().get(Constants.KEY_IMAGE))
                            {
                                images.add(image);
                            }
//                            String image = documentChange.getDocument().getString(Constants.KEY_IMAGE);
                            String dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                            Date dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                            String userId = documentChange.getDocument().getString(Constants.KEY_USER_ID);
                            String userName = documentChange.getDocument().getString(Constants.KEY_NAME);
                            String userImage = documentChange.getDocument().getString(Constants.KEY_USER_IMAGE);
                            String userEmail = documentChange.getDocument().getString(Constants.KEY_EMAIL);

                            if (!userId.equals(preferenceManager.getString(Constants.KEY_USER_ID))) {
                                ads.get(i).title = title;
                                ads.get(i).author = author;
                                ads.get(i).genre = genre;
                                ads.get(i).condition = condition;
                                ads.get(i).description = description;
                                ads.get(i).city = city;
                                ads.get(i).address = address;
                                ads.get(i).images = images;
                                ads.get(i).dateTime = dateTime;
                                ads.get(i).dateObject = dateObject;
                                ads.get(i).userName = userName;
                                ads.get(i).userId = userId;
                                ads.get(i).userImage = userImage;
                                ads.get(i).userEmail = userEmail;
                            }
                        }
                    }
                }
//            Collections.sort(ads, (obj1, obj2) -> obj2.dateObject.compareTo(obj1.dateObject));
//            conversationsAdapter.notifyDataSetChanged();
//            binding.conversationsRecyclerView.smoothScrollToPosition(0);
//            binding.conversationsRecyclerView.setVisibility(View.VISIBLE);
//            binding.progressBar.setVisibility(View.GONE);
            }
            showAdsOnMap();
        });
    }

    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("dd MMMM, yyyy - kk:mm", Locale.getDefault()).format(date);
    }

    private void showAdsOnMap() {
        Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());

        for (int i = 0; i < ads.size(); i++) {
            Ad ad = ads.get(i);
            String fullAddress = ad.city + " " + ad.address;
            try {
                List<Address> addressList = geocoder.getFromLocationName(fullAddress, 1);
                if (addressList.size() > 0) {
                    LatLng latLng = new LatLng(addressList.get(0).getLatitude(), addressList.get(0).getLongitude());

                    Bitmap markerBitmap = getBitmapFromDrawable(getApplicationContext(), R.drawable.ic_book);

                    Marker marker = mMap.addMarker(
                            new MarkerOptions()
                                    .position(latLng)
                                    .snippet(Integer.toString(i))
                                    .icon(BitmapDescriptorFactory.fromBitmap(markerBitmap)));
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

    }

    private void addToFavorites(String adId) {
        DocumentReference usersDocRef = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));

        if (usersDocRef != null) {
            usersDocRef.update(Constants.KEY_FAVORITES, FieldValue.arrayUnion(adId))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(MapsActivity.this, "Added to favorites", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(MapsActivity.this, "Error while adding to favorites", Toast.LENGTH_SHORT).show();
                    }
                });
        }
    }
    
    private void getFavoriteAdId(int adIndex) {
        Ad ad = ads.get(adIndex);
        database.collection(Constants.KEY_COLLECTION_ADS)
                .whereEqualTo(Constants.KEY_TIMESTAMP, ad.dateObject)
                .whereEqualTo(Constants.KEY_USER_ID, ad.userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        QuerySnapshot query = task.getResult();
                        if (query.size() > 0) {
                            String documentId = query.getDocuments().get(0).getId();
                            addToFavorites(documentId);
                        }
                    };
                });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        
        LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        mMap.addMarker(new MarkerOptions().position(currentLatLng).title("My current location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14.0f));
        try {
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.mapstyle));
            if (!success) {
                Log.e(this.getLocalClassName(), "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(this.getLocalClassName(), "Can't find style. Error: ", e);
        }


        mMap.setInfoWindowAdapter(new AdInfoWindowAdapter(MapsActivity.this, ads));
        setMapListeners();
        listenAds();
    }

    private void setMapListeners() {
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapClickListener(this);
        mMap.setOnInfoWindowClickListener( marker -> {
            int adIndex = Integer.parseInt(marker.getSnippet());
            Ad ad = ads.get(adIndex);

            Intent intent = new Intent(MapsActivity.this, FullAdInfoActivity.class);
            intent.putExtra(Constants.KEY_AD, ad);
            startActivity(intent);
        });
    }

    public static Bitmap getBitmapFromDrawable(Context context, @DrawableRes int drawableId) {
        Drawable drawable = AppCompatResources.getDrawable(context, drawableId);

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        else if (drawable instanceof VectorDrawableCompat || drawable instanceof VectorDrawable) {
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);

            return bitmap;
        }
        else {
            throw new IllegalArgumentException("unsupported drawable type");
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE);
            return;
        }

        @SuppressLint("MissingPermission") Task<Location> task = fusedLocationClient.getLastLocation();

        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    SupportMapFragment supportMapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
                    assert supportMapFragment != null;
                    supportMapFragment.getMapAsync(MapsActivity.this);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation();
                }
                break;
        }
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        binding.fabAddToFav.setVisibility(View.VISIBLE);
        binding.fabStartChat.setVisibility(View.VISIBLE);
        currentMarker = marker;
        return false;
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        binding.fabAddToFav.setVisibility(View.GONE);
        binding.fabStartChat.setVisibility(View.GONE);
        currentMarker = null;
    }
}