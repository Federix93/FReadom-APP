package com.example.android.lab1;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
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
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Locale;

public class PositionActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks, LocationListener {

    private Toolbar mToolbar;
    private GoogleMap mMap;
    private TextView mCurrentPositionTextView;
    private ImageView mConfirmPosition;
    private static final int ADDRESS_SEARCH_BAR_REQUEST = 11;

    protected Location mCurrentLocationSelected;
    protected Location mLastLocation;
    private AddressResultReceiver mResultReceiver;
    private GoogleApiClient mGoogleApiClient;
    private LocationManager manager;
    private Object synchronizedObject = new Object();

    private LocationRequest mLocationRequest;

    private String mAddressOutput;

    private Float mInitialZoom;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 10;
    private ImageView mSearchAddressImageView;
    public static final String ADDRESS_KEY = "ADDRESS_KEY";
    private Activity mSelf;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_position);

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


        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (manager != null && !manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                enableLoc();
            } else {
                connectToGoogleMapApi();
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        mConfirmPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                if (mAddressOutput != null)
                    i.putExtra(ADDRESS_KEY, mAddressOutput);
                setResult(RESULT_OK, i);
                finish();
            }
        });

        mInitialZoom = new Float(12.0);
    }

    private void setMapClick(final GoogleMap map) {
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    if (manager != null && !manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        enableLoc();
                    } else {
                        connectToGoogleMapApi();
                    }
                } else {
                    ActivityCompat.requestPermissions(getParent(), new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                }
                map.setMyLocationEnabled(true);
                if (mLastLocation != null) {
                    mLastLocation.setLatitude(latLng.latitude);
                    mLastLocation.setLongitude(latLng.longitude);
                    startIntentService();
                    String snippet = String.format(Locale.getDefault(), "Lat: %1$.5f, Long: %2$.5f",
                            latLng.latitude, latLng.longitude);
                    //map.addMarker(new MarkerOptions().position(latLng).title("Location").snippet(snippet));
                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLAY_SERVICES_RESOLUTION_REQUEST && resultCode == RESULT_OK) {
            mGoogleApiClient.disconnect();
            mGoogleApiClient = null;
            finish();
        } else if (requestCode == ADDRESS_SEARCH_BAR_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                setMarker(place.getLatLng());
                // TODO deliver back place object
                mCurrentPositionTextView.setText(place.getAddress());
                Log.i("AUTOCOMPLETE", "Place: " + place.getName());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i("AUTOCOMPLETE", status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    private void enableLoc() {
        if (mGoogleApiClient == null) {
            connectToGoogleMapApi();
            synchronized (synchronizedObject) {
                if (mLocationRequest == null) {
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
                            } catch (IntentSender.SendIntentException e) {
                            }
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
                    if (manager != null && !manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        enableLoc();
                    } else {
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
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                return true;
            }
        });
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                mMap.clear();
                getLocationData();
                return false;
            }
        });
        //setMapClick(mMap);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        synchronized (synchronizedObject) {
            if (mLocationRequest == null) {
                mLocationRequest = LocationRequest.create();
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                mLocationRequest.setInterval(30 * 1000);   //update every amount*1000 millisecs by default
                mLocationRequest.setFastestInterval(5 * 1000);     //update every amount*1000 millisecs if the system updates the value on its own
            }
        }
        try {
            //LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRe);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            if (mLastLocation == null)
                getLocationData();
        } catch (SecurityException e) {
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
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        finish();
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocationSelected = location;
        mSelf = this;
        mSearchAddressImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent i = Utilities.getSearchBarIntent(mSelf,
                            new LatLng(mCurrentLocationSelected.getLatitude(), mCurrentLocationSelected.getLongitude()),
                            (double) getResources().getInteger(R.integer.position_radius_address));
                    startActivityForResult(i, ADDRESS_SEARCH_BAR_REQUEST);
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                }
            }
        });
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
            if (resultCode == FetchAddressIntentService.Constants.SUCCESS_RESULT) {
                mAddressOutput = resultData.getString(FetchAddressIntentService.Constants.RESULT_DATA_KEY);
                if (mAddressOutput == null) {
                    mAddressOutput = "";
                }
                mCurrentPositionTextView.setText(mAddressOutput);
                if (mLastLocation != null) {
                    LatLng currentPosition = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(currentPosition).title("SELECTED POSITION"));
                    setMapClick(mMap);
                }
            } else {
                // no connection to translate position to address when request was made
                //mMap.clear();
                mAddressOutput = resultData.getString(FetchAddressIntentService.Constants.RESULT_DATA_KEY);
                Toast.makeText(getApplicationContext(),
                        R.string.no_internet_connection,
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private void getLocationData() {
        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                startIntentService();
            }
        } catch (SecurityException e) {

        }
    }

    private void connectToGoogleMapApi() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();
    }

    protected boolean phoneIsConnectedToInternet() {
        ConnectivityManager cm =
                (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        ;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private void setMarker(LatLng position) {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(position));
        if (mInitialZoom == null)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, mMap.getCameraPosition().zoom));
        else {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, mInitialZoom));
            mInitialZoom = null;
        }
    }

}
