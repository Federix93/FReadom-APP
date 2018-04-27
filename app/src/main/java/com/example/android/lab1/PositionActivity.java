package com.example.android.lab1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.lab1.model.Address;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PositionActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String ADDRESS_KEY = "ADDRESS_KEY";
    public static final String LAT_KEY = "LATITUDE";
    public static final String LON_KEY = "LONGITUDE";

    public static final String INTERNET_AVAILABLE_BROAD_KEY = "BROADCAST_INTERNET";

    private static final int ADDRESS_SEARCH_BAR_REQUEST = 11;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 10;
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 11;

    private Toolbar mToolbar;
    private GoogleMap mMap;
    private TextView mCurrentPositionTextView;
    private ImageView mConfirmPosition;

    private Float mInitialZoom;
    private ImageView mSearchAddressImageView;
    private Activity mSelf;
    private Address mResultAddress;
    private LocationManager manager;
    private boolean mLocationSettingWasEnabled;

    private BroadcastReceiver mNetworkAvaiable;
    private NetworkReceiver mNetworkReceiver;
    private GeoCodingTask mCurrentlyExecuting;
    private Location mCurrentlyResolving;
    private Location mResolveLater;

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_position);

        // init toolbar
        mSearchAddressImageView = findViewById(R.id.position_search_address);
        mToolbar = findViewById(R.id.toolbar_position);
        mToolbar.setTitle(R.string.text_position_toolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_close_black_24dp);
        mToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(mToolbar);

        mCurrentPositionTextView = findViewById(R.id.current_position_text_view);
        mConfirmPosition = findViewById(R.id.confirm_position_image_view);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mConfirmPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                if (mResultAddress != null) {
                    i.putExtra(ADDRESS_KEY, mResultAddress.getAddress());
                    i.putExtra(LAT_KEY, mResultAddress.getLat());
                    i.putExtra(LON_KEY, mResultAddress.getLon());
                }
                setResult(RESULT_OK, i);
                finish();
            }
        });

        mInitialZoom = new Float(12.0);
        mSelf = this;

        // network listener setup
        mNetworkAvaiable = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // called on network updates
                if (intent.hasExtra(NetworkReceiver.NETWORK_AVAILABILITY_RESULT) &&
                        intent.getBooleanExtra(NetworkReceiver.NETWORK_AVAILABILITY_RESULT, false) == true) {
                    // theres connection
                    if (mResolveLater != null)
                        resolveLocation(mResolveLater);
                } else {
                    if (mCurrentlyExecuting != null) {
                        mCurrentlyExecuting.cancel(true);
                        mResolveLater = mCurrentlyResolving;
                    }
                }
            }
        };

        mNetworkReceiver = new NetworkReceiver();

        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (manager != null && !manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                mLocationSettingWasEnabled = true;
                enableLoc();
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mNetworkAvaiable, new IntentFilter(INTERNET_AVAILABLE_BROAD_KEY));
        registerReceiver(mNetworkReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mNetworkAvaiable);
        unregisterReceiver(mNetworkReceiver);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mResultAddress != null) {
            outState.putString(ADDRESS_KEY, mResultAddress.getAddress());
            outState.putDouble(LAT_KEY, mResultAddress.getLat());
            outState.putDouble(LON_KEY, mResultAddress.getLon());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey(ADDRESS_KEY) &&
                savedInstanceState.containsKey(LAT_KEY) &&
                savedInstanceState.containsKey(LON_KEY))
            mResultAddress = new Address(savedInstanceState.getString(ADDRESS_KEY),
                    savedInstanceState.getDouble(LAT_KEY),
                    savedInstanceState.getDouble(LON_KEY));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLAY_SERVICES_RESOLUTION_REQUEST) {
            if (resultCode == RESULT_OK)
                getCurrentLocation();
        } else if (requestCode == ADDRESS_SEARCH_BAR_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                setMarker(place.getLatLng());
                mResultAddress = new Address(place.getAddress().toString(),
                        place.getLatLng().latitude,
                        place.getLatLng().longitude);
                mCurrentPositionTextView.setText(place.getAddress());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    private void enableLoc() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(LocationRequest.create());
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response =
                            task.getResult(ApiException.class);
                } catch (ApiException ex) {
                    switch (ex.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                ResolvableApiException resolvableApiException =
                                        (ResolvableApiException) ex;
                                resolvableApiException
                                        .startResolutionForResult(mSelf,
                                                PLAY_SERVICES_RESOLUTION_REQUEST);
                            } catch (IntentSender.SendIntentException e) {

                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:

                            break;
                    }
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (manager != null && !manager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                        enableLoc();
                } else {
                    finish();
                }
                break;
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                return true;
            }
        });
        if (Utilities.checkPermissionActivity(this, Manifest.permission.ACCESS_FINE_LOCATION))
            mMap.setMyLocationEnabled(true);
        getCurrentLocation();
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                getCurrentLocation();
                return true;
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        if (Utilities.checkPermissionActivity(mSelf, Manifest.permission.ACCESS_FINE_LOCATION)
                && isLocationEnabled(getApplicationContext())) {
            enableLoc();
            // make one time position request
            LocationServices
                    .getFusedLocationProviderClient(mSelf)
                    .requestLocationUpdates(LocationRequest.create(),
                            new LocationCallback() {
                                @Override
                                public void onLocationResult(LocationResult locationResult) {
                                    if (locationResult == null || locationResult.getLocations().size() == 0) {
                                        return;
                                    }
                                    final Location location = locationResult.getLocations().get(0);
                                    mSearchAddressImageView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            try {
                                                // load address bar
                                                Intent intent = Utilities.getSearchBarIntent(mSelf,
                                                        new LatLng(location.getLatitude(), location.getLongitude()),
                                                        (double) getResources().getInteger(R.integer.position_radius_address));
                                                startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                                            } catch (GooglePlayServicesRepairableException e) {
                                                // TODO: Handle the error.
                                                e.printStackTrace();
                                            } catch (GooglePlayServicesNotAvailableException e) {
                                                // TODO: Handle the error.
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                    // translate latlng to address
                                    resolveLocation(location);
                                }
                            }, null);
        } else {
            enableLoc();
        }
    }

    private void setMarker(LatLng position) {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(position));
        if (mInitialZoom == null)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, mMap.getCameraPosition().zoom));
        else {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, mInitialZoom));
        }
    }

    private void resolveLocation(Location loc) {
        // This method will change address mResultAddress var
        mCurrentlyResolving = loc;
        mCurrentlyExecuting = new GeoCodingTask(mCurrentPositionTextView);
        mCurrentlyExecuting.execute(new LatLng(loc.getLatitude(),
                loc.getLongitude()));
    }

    private class GeoCodingTask extends AsyncTask<LatLng, String, String> {

        private TextView mTarget;
        private LatLng mParam;

        public GeoCodingTask(TextView target) {
            this.mTarget = target;
        }

        @Override
        protected String doInBackground(LatLng... latLngs) {
            if (latLngs.length > 0) {
                mParam = latLngs[0];
                Geocoder geocoder = new Geocoder(mSelf, Locale.getDefault());
                List<android.location.Address> fromLocation = null;
                try {
                    if (!isCancelled())
                        fromLocation = geocoder.getFromLocation(latLngs[0].latitude,
                                latLngs[0].longitude,
                                1);
                    else
                        return null;

                } catch (IOException e) {
                    return null;
                }
                if (fromLocation == null || fromLocation.size() == 0)
                    return null;
                else {
                    android.location.Address address = fromLocation.get(0);
                    ArrayList<String> addressFragments = new ArrayList<String>();

                    // Fetch the address lines using getAddressLine,
                    // join them, and send them to the thread.
                    for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                        addressFragments.add(address.getAddressLine(i));
                    }
                    return TextUtils.join(System.getProperty("line.separator"),
                            addressFragments);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (mTarget != null && !this.isCancelled()) {
                if (s == null) {
                    mTarget.setText(R.string.no_address_found);
                } else {
                    mTarget.setText(s);
                    mCurrentlyExecuting = null;
                    mCurrentlyResolving = null;
                    mResolveLater = null;
                    mResultAddress = new Address(s,
                            mParam.latitude,
                            mParam.longitude);
                    setMarker(mParam);
                }
            }

        }
    }

}
