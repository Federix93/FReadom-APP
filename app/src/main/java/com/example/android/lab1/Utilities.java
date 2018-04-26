package com.example.android.lab1;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

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

}


//    if (ContextCompat.checkSelfPermission(getApplicationContext(),
//    Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
//        startBarCodeScanner();
//    } else {
//        ActivityCompat.requestPermissions(thisActivity,
//                new String[]{Manifest.permission.CAMERA},
//                SCAN_REQUEST_TAG);
//    }
