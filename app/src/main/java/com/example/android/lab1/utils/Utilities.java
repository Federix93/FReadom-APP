package com.example.android.lab1.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.android.lab1.R;
import com.example.android.lab1.model.Address;
import com.example.android.lab1.ui.PositionActivity;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;

public abstract class Utilities {

    public static boolean checkPermissionActivity(Activity activity, String permission){

        return ContextCompat.checkSelfPermission(activity.getApplicationContext(), permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static void askPermissionActivity(Activity activity, String permission, final int callbackRequest)
    {
        ActivityCompat.requestPermissions(activity, new String[] {permission}, callbackRequest);
    }

    public static Intent getSearchBarIntent(Activity activity, LatLng center, Double radius) throws GooglePlayServicesNotAvailableException, GooglePlayServicesRepairableException {
        return new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                .setFilter(new AutocompleteFilter.Builder().setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS).build())
                .setBoundsBias(Utilities.toBounds(center, radius))
                .build(activity);
    }

    public static LatLngBounds toBounds(LatLng center, double radiusInMeters) {
        double distanceFromCenterToCorner = radiusInMeters * Math.sqrt(2.0);
        LatLng southwestCorner =
                SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 225.0);
        LatLng northeastCorner =
                SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 45.0);
        return new LatLngBounds(southwestCorner, northeastCorner);
    }

    public static Address readResultOfPositionActivity(Intent i) {
        if (i.hasExtra(PositionActivity.ADDRESS_KEY) && i.hasExtra(PositionActivity.LAT_KEY) && i.hasExtra(PositionActivity.LON_KEY)) {
            return new Address(i.getStringExtra(PositionActivity.ADDRESS_KEY),
                    i.getDoubleExtra(PositionActivity.LAT_KEY, 0),
                    i.getDoubleExtra(PositionActivity.LON_KEY, 0));
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static  void setupStatusBarColor(Activity activity)
    {
        Window window = activity.getWindow();

// clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

// finally change the color
        window.setStatusBarColor(ContextCompat.getColor(activity.getApplicationContext(), R.color.background_app));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = activity.getWindow().getDecorView();

            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

}


//    if (ContextCompat.checkSelfPermission(getApplicationContext(),
//    Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
//        startBarCodeScanner();
//    } else {
//        ActivityCompat.requestPermissions(thisActivity,
//                new String[]{Manifest.permission.CAMERA},
//                SCAN_REQUEST_TAG);
//    }
