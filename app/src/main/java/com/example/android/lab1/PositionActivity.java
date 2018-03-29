package com.example.android.lab1;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Locale;

public class PositionActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks, LocationListener {

    private Toolbar mToolbar;
    private GoogleMap mMap;
    private TextView mCurrentPositionTextView;
    private ImageView mConfirmPosition;

    protected Location mCurrentLocationSelected;
    protected Location mLastLocation;
    private AddressResultReceiver mResultReceiver;
    private GoogleApiClient mGoogleApiClient;
    private LocationManager manager;
    private Object synchronizedObject = new Object();

    private LocationRequest mLocationRequest;

    private String mAddressOutput;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 10;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_position);

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

        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if(manager != null && !manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                enableLoc();
            }else{
                connectToGoogleMapApi();
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        mConfirmPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferencesManager sharedPreferencesManager = SharedPreferencesManager.getInstance(getApplicationContext());
                User user = sharedPreferencesManager.getUser();
                if(user != null){
                    user.setTempAddress(mAddressOutput);
                    sharedPreferencesManager.putUser(user);
                }else{
                    User newUser = User.getInstance();
                    newUser.setTempAddress(mAddressOutput);
                    sharedPreferencesManager.putUser(newUser);
                }
                finish();
            }
        });
    }

    private void setMapClick(final GoogleMap map) {
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    if(manager != null && !manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        enableLoc();
                    }else{
                        connectToGoogleMapApi();
                    }
                } else {
                    ActivityCompat.requestPermissions(getParent(), new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                }
                map.setMyLocationEnabled(true);
                if(mLastLocation != null) {
                    mLastLocation.setLatitude(latLng.latitude);
                    mLastLocation.setLongitude(latLng.longitude);
                    startIntentService();
                    String snippet = String.format(Locale.getDefault(), "Lat: %1$.5f, Long: %2$.5f",
                            latLng.latitude, latLng.longitude);
                    map.addMarker(new MarkerOptions().position(latLng).title("Location").snippet(snippet));
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!(requestCode == PLAY_SERVICES_RESOLUTION_REQUEST && resultCode == RESULT_OK)) {
            finish();
        }else {
            mGoogleApiClient.disconnect();
            mGoogleApiClient = null;
            finish();
        }
    }

    private void enableLoc() {
        if (mGoogleApiClient == null) {
            connectToGoogleMapApi();
            synchronized (synchronizedObject) {
                if(mLocationRequest == null) {
                    mLocationRequest = LocationRequest.create();
                    mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    mLocationRequest.setInterval(30 * 1000);
                    mLocationRequest.setFastestInterval(5 * 1000);
                }
            }
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(mLocationRequest);
            builder.setAlwaysShow(true);

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                status.startResolutionForResult(PositionActivity.this, PLAY_SERVICES_RESOLUTION_REQUEST);
                            } catch (IntentSender.SendIntentException e) {}
                            break;
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(manager != null && !manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        enableLoc();
                    }else{
                        connectToGoogleMapApi();
                    }
                } else {
                    finish();
                }
                break;
        }
    }

    protected void startIntentService() {
        mResultReceiver = new AddressResultReceiver(new Handler());
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(FetchAddressIntentService.Constants.RECEIVER, mResultReceiver);
        intent.putExtra(FetchAddressIntentService.Constants.LOCATION_DATA_EXTRA, mLastLocation);
        startService(intent);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                mMap.clear();
                getLocationData();
                return false;
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        synchronized (synchronizedObject) {
            if(mLocationRequest == null){
                mLocationRequest = LocationRequest.create();
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                mLocationRequest.setInterval(30 * 1000);   //update every amount*1000 millisecs by default
                mLocationRequest.setFastestInterval(5 * 1000);     //update every amount*1000 millisecs if the system updates the value on its own
            }
        }
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            if (mLastLocation == null)
                getLocationData();
        }catch(SecurityException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        connectToGoogleMapApi();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mGoogleApiClient != null && mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        finish();
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocationSelected = location;
    }

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultData == null) {
                return;
            }
            mAddressOutput = resultData.getString(FetchAddressIntentService.Constants.RESULT_DATA_KEY);
            if (mAddressOutput == null) {
                mAddressOutput = "";
            }
            mCurrentPositionTextView.setText(mAddressOutput);
            if(mLastLocation != null) {
                LatLng currentPosition = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(currentPosition).title("MARKER IN CURENT POSITION"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 12.0f));
                setMapClick(mMap);
            }
        }
    }

    private void getLocationData() {

        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                startIntentService();
            }
        }catch(SecurityException e){

        }
    }

    private void connectToGoogleMapApi(){
        if(mGoogleApiClient == null)
        {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();
    }

}
