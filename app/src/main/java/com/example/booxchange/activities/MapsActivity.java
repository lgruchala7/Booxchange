package com.example.booxchange.activities;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.booxchange.R;
import com.example.booxchange.adapters.AdInfoWindowAdapter;
import com.example.booxchange.databinding.ActivityMapsBinding;
import com.example.booxchange.models.Ad;
import com.example.booxchange.models.PolylineData;
import com.example.booxchange.models.User;
import com.example.booxchange.utilities.Constants;
import com.example.booxchange.utilities.PreferenceManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;

import org.checkerframework.checker.units.qual.A;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener, GoogleMap.OnPolylineClickListener {

    private static final int REQUEST_CODE = 101;
    private static final String TAG = MapsActivity.class.getSimpleName();

    private GoogleMap googleMap;
    private ActivityMapsBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Location currentLocation;
    private boolean isPermissionGranted;
    private List<Ad> ads;
    private PreferenceManager preferenceManager;
    private Marker currentMarker;
    private FirebaseFirestore database;
    private GeoApiContext geoApiContext;
    private ArrayList<PolylineData> polylinesData = new ArrayList();
    private boolean isInDirectionMode = false;

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

    private void checkIfAdFavorite(Ad ad) {
        database.collection(Constants.KEY_COLLECTION_ADS)
                .whereEqualTo(Constants.KEY_USER_ID, ad.userId)
                .whereEqualTo(Constants.KEY_TIMESTAMP, ad.dateObject)
                .get()
                .addOnCompleteListener( task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String adId = task.getResult().getDocuments().get(0).getId();
                        database.collection(Constants.KEY_COLLECTION_USERS)
                                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                                .get()
                                .addOnCompleteListener(getDocumentTask -> setImageFavoriteIndicator(getDocumentTask, adId, ad));
                    }
                });
    }

    private void setImageFavoriteIndicator(Task<DocumentSnapshot> task, String adId, Ad ad) {
        if (task.isSuccessful() && task.getResult() != null) {
            ArrayList<String> favoriteAdList = new ArrayList<>();
            try {
                favoriteAdList = (ArrayList<String>) task.getResult().get(Constants.KEY_FAVORITES);
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
            if (favoriteAdList != null && favoriteAdList.contains(adId)) {
                ad.isInFavorites = true;
            } else {
                ad.isInFavorites = false;
            }
        }
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
        binding.fabStartChat.setOnClickListener( v -> {
            if (currentMarker != null) {
                int adIndex = Integer.parseInt(currentMarker.getTag().toString());
                Ad ad = ads.get(adIndex);

                User user = new User(ad.userId, ad.userName, ad.userImage);

                Intent intent = new Intent(MapsActivity.this, ChatActivity.class);
                intent.putExtra(Constants.KEY_USER, user);
                startActivity(intent);
            }
        });
        binding.fabAddToFav.setOnClickListener( v -> {
            if (currentMarker != null) {
                int adIndex = Integer.parseInt(currentMarker.getTag().toString());
                addToFavorites(adIndex);

            }
        });
        binding.fabRemoveFromFav.setOnClickListener( v -> {
            if (currentMarker != null) {
                int adIndex = Integer.parseInt(currentMarker.getTag().toString());
                removeFromFavorites(adIndex);
            }
        });
        binding.fabGetDirections.setOnClickListener( v -> {
            if (currentMarker != null) {
                binding.fabAddToFav.setVisibility(View.GONE);
                binding.fabStartChat.setVisibility(View.GONE);
                binding.fabGetDirections.setVisibility(View.GONE);
                binding.fabCloseDirections.setVisibility(View.VISIBLE);
                currentMarker.hideInfoWindow();
                googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                    @Nullable
                    @Override
                    public View getInfoContents(@NonNull Marker marker) {
                        return null;
                    }

                    @Nullable
                    @Override
                    public View getInfoWindow(@NonNull Marker marker) {
                        return null;
                    }
                });
                calculateDirections(currentMarker);
            }
        });
        binding.fabFocusOnMyLocation.setOnClickListener( v -> centerOnMyLocation() );
        binding.fabCloseDirections.setOnClickListener ( v -> {
            for (PolylineData polylineData : polylinesData) {
                polylineData.getPolyline().remove();
            }
            polylinesData.clear();
            polylinesData = new ArrayList<>();

//            currentMarker.hideInfoWindow();
            googleMap.clear();
            googleMap.setInfoWindowAdapter(new AdInfoWindowAdapter(MapsActivity.this, ads));
            setMapListeners();
            showAdsOnMap();
            centerOnMyLocation();

            currentMarker = null;
            isInDirectionMode = false;

//            binding.fabAddToFav.setVisibility(View.VISIBLE);
//            binding.fabStartChat.setVisibility(View.VISIBLE);
            binding.fabCloseDirections.setVisibility(View.GONE);

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

                        images.addAll((ArrayList<String>) documentChange.getDocument().get(Constants.KEY_IMAGE));

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
            for (Ad ad : ads) {
                checkIfAdFavorite(ad);
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

                    Marker marker = googleMap.addMarker(
                            new MarkerOptions()
                                    .position(latLng)
                                    .icon(BitmapDescriptorFactory.fromBitmap(markerBitmap)));
                    String tag = Integer.toString(i);
                    marker.setTag(tag);
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

    }

    private void addToFavorites(int adIndex) {
        Ad ad = ads.get(adIndex);
        database.collection(Constants.KEY_COLLECTION_ADS)
                .whereEqualTo(Constants.KEY_TIMESTAMP, ad.dateObject)
                .whereEqualTo(Constants.KEY_USER_ID, ad.userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        QuerySnapshot query = task.getResult();
                        if (query.size() > 0) {
                            String adId = query.getDocuments().get(0).getId();
                            DocumentReference usersDocRef = database.collection(Constants.KEY_COLLECTION_USERS)
                                    .document(preferenceManager.getString(Constants.KEY_USER_ID));

                            if (usersDocRef != null) {
                                usersDocRef.update(Constants.KEY_FAVORITES, FieldValue.arrayUnion(adId))
                                        .addOnCompleteListener(updateTask -> {
                                            if (updateTask.isSuccessful()) {
                                                binding.fabAddToFav.setVisibility(View.GONE);
                                                binding.fabRemoveFromFav.setVisibility(View.VISIBLE);
                                                showSnackbar("Added to favorites");
                                            }
                                            else {
                                                showSnackbar("Error while adding to favorites");
                                            }
                                        });
                            }
                        }
                    }
                });
    }

    private void removeFromFavorites(int adIndex) {
        Ad ad = ads.get(adIndex);
        database.collection(Constants.KEY_COLLECTION_ADS)
                .whereEqualTo(Constants.KEY_TIMESTAMP, ad.dateObject)
                .whereEqualTo(Constants.KEY_USER_ID, ad.userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        QuerySnapshot query = task.getResult();
                        if (query.size() > 0) {
                            String adId = query.getDocuments().get(0).getId();
                            DocumentReference usersDocRef = database.collection(Constants.KEY_COLLECTION_USERS)
                                    .document(preferenceManager.getString(Constants.KEY_USER_ID));
                            if (adId != null) {
                                usersDocRef.update(Constants.KEY_FAVORITES, FieldValue.arrayRemove(adId))
                                        .addOnCompleteListener( removeTask -> {
                                            if (removeTask.isSuccessful()) {
                                                binding.fabRemoveFromFav.setVisibility(View.GONE);
                                                binding.fabAddToFav.setVisibility(View.VISIBLE);
                                                showSnackbar("Removed from favorite ads list");
                                            }
                                            else {
                                                showSnackbar("Error while removing from favorite ads list");
                                            }
                                        });
                            }
                            else {
                                showSnackbar("Error while removing from favorite ads list");
                            }
                        }
                    }
                });
    }
    
//    private void getFavoriteAdId(int adIndex) {
//        Ad ad = ads.get(adIndex);
//        database.collection(Constants.KEY_COLLECTION_ADS)
//                .whereEqualTo(Constants.KEY_TIMESTAMP, ad.dateObject)
//                .whereEqualTo(Constants.KEY_USER_ID, ad.userId)
//                .get()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful() && task.getResult() != null) {
//                        QuerySnapshot query = task.getResult();
//                        if (query.size() > 0) {
//                            String documentId = query.getDocuments().get(0).getId();
//                            addToFavorites(documentId);
//                        }
//                    }
//                });
//    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;

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

        if (geoApiContext == null) {
            geoApiContext = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.google_api_key))
                    .build();
        }

        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.setOnPolylineClickListener(this);
        googleMap.setMyLocationEnabled(true);
        googleMap.setInfoWindowAdapter(new AdInfoWindowAdapter(MapsActivity.this, ads));
        setMapListeners();
        listenAds();
        centerOnMyLocation();
    }

    private void setMapListeners() {
        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnMapClickListener(this);
        googleMap.setOnInfoWindowClickListener( marker -> {
            int adIndex = Integer.parseInt(marker.getTag().toString());
            Ad ad = ads.get(adIndex);

            Intent intent = new Intent(MapsActivity.this, FullAdInfoActivity.class);
            intent.putExtra(Constants.KEY_TIMESTAMP, ad.dateObject);
            intent.putExtra(Constants.KEY_USER_ID, ad.userId);
            intent.putExtra(Constants.KEY_FAVORITES, ad.isInFavorites);
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

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener( location -> {
                if (location != null) {
                    currentLocation = location;
                    SupportMapFragment supportMapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
                    assert supportMapFragment != null;
                    supportMapFragment.getMapAsync(MapsActivity.this);

                    setLocationRequest();
                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                }
            });
    }

    private void calculateDirections(Marker marker){
        isInDirectionMode = true;

        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                marker.getPosition().latitude,
                marker.getPosition().longitude
        );
        new DirectionsApiRequest(geoApiContext)
                .alternatives(true)
                .origin(new com.google.maps.model.LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()))
                .mode(TravelMode.WALKING)
                .destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
                    @Override
                    public void onResult(DirectionsResult result) {
                        addPolylinesToMap(result);
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        Log.e(TAG, "calculateDirections: Failed to get directions: " + e.getMessage() );
                    }
                });
    }

    private void addPolylinesToMap(final DirectionsResult result) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: result routes: " + result.routes.length);

                if (polylinesData.size() > 0) {
                    for (PolylineData polylineData : polylinesData) {
                        polylineData.getPolyline().remove();
                    }
                    polylinesData.clear();
                    polylinesData = new ArrayList<>();
                }

                double duration = Long.MAX_VALUE;
                Polyline polyline = null;

                for (DirectionsRoute route: result.routes) {
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                    List<LatLng> newDecodedPath = new ArrayList<>();

                    // This loops through all the LatLng coordinates of ONE polyline.
                    for (com.google.maps.model.LatLng latLng: decodedPath) {
                        newDecodedPath.add(new LatLng(
                                latLng.lat,
                                latLng.lng
                        ));
                    }
                    polyline = googleMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(ContextCompat.getColor(MapsActivity.this, R.color.secondary_text));
                    polyline.setClickable(true);
                    polyline.setTag(polylinesData.size());
                    polylinesData.add(new PolylineData(polyline, route.legs[0]));

                    long tempDuration = route.legs[0].duration.inSeconds;
                    if (tempDuration < duration) {
                        duration = tempDuration;
                    }
                }

                if (polyline != null) {
                    onPolylineClick(polyline);
                }
            }
        });
    }

    private void setLocationRequest() {
        locationRequest = new com.google.android.gms.location.LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                .setMinUpdateIntervalMillis(5*1000)
                .setWaitForAccurateLocation(true)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        currentLocation.setLatitude(location.getLatitude());
                        currentLocation.setLongitude(location.getLongitude());
//                        LatLng currentLatLng = new LatLng(
//                                currentLocation.getLatitude(),
//                                currentLocation.getLongitude());

//                        List<LatLng> userTrailPoints= userTrail.getPoints();
//                        userTrailPoints.add(currentLatLng);
//                        userTrail.setPoints(userTrailPoints);

//                        googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));
                    }
                }
            }
        };
    }

    private void centerOnMyLocation() {
        LatLng currentLatLng = new LatLng(
                currentLocation.getLatitude(),
                currentLocation.getLongitude());
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14.0f), 600, null);
    }

    public void zoomRoute(List<LatLng> latLngList) {
        if (googleMap == null || latLngList == null || latLngList.isEmpty()) return;

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLng latLngPoint : latLngList)
            boundsBuilder.include(latLngPoint);

        int routePadding = 120;
        LatLngBounds latLngBounds = boundsBuilder.build();

        googleMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding),
                600,
                null
        );
    }

    private void showSnackbar(String text) {
        Snackbar.make(MapsActivity.this, binding.getRoot(), text, Toast.LENGTH_SHORT).show();
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
        if (!isInDirectionMode) {
            int adIndex = Integer.parseInt(marker.getTag().toString());
            currentMarker = marker;
            currentMarker.showInfoWindow();
            if (ads.get(adIndex).isInFavorites) {
                binding.fabRemoveFromFav.setVisibility(View.VISIBLE);
                binding.fabAddToFav.setVisibility(View.GONE);
            }
            else {
                binding.fabAddToFav.setVisibility(View.VISIBLE);
                binding.fabRemoveFromFav.setVisibility(View.GONE);
            }
            binding.fabStartChat.setVisibility(View.VISIBLE);
            binding.fabGetDirections.setVisibility(View.VISIBLE);
        }
        return false;
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        if (!isInDirectionMode) {
            binding.fabRemoveFromFav.setVisibility(View.GONE);
            binding.fabAddToFav.setVisibility(View.GONE);
            binding.fabStartChat.setVisibility(View.GONE);
            binding.fabGetDirections.setVisibility(View.GONE);
            currentMarker = null;
        }
    }

    @Override
    public void onPolylineClick(Polyline polyline) {

        for (PolylineData polylineData: polylinesData) {
            if (polyline.getId().equals(polylineData.getPolyline().getId())) {
                polylineData.getPolyline().setColor(ContextCompat.getColor(getApplicationContext(), R.color.polyline_active));
                polylineData.getPolyline().setZIndex(1);
                zoomRoute(polyline.getPoints());
                currentMarker.setTitle(String.valueOf((int)((polylineData.getLeg().duration.inSeconds / 61.0) + 1)) + " min.");
                currentMarker.showInfoWindow();
            }
            else {
                polylineData.getPolyline().setColor(ContextCompat.getColor(getApplicationContext(), R.color.secondary_text));
                polylineData.getPolyline().setZIndex(0);
            }
        }
    }
}