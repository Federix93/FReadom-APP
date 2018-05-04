package com.example.android.lab1.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.widget.ArrayAdapter;

import com.example.android.lab1.model.Address;
import com.example.android.lab1.ui.PositionActivity;
import com.example.android.lab1.ui.ScanBarCodeActivity;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.example.android.lab1.utils.Constants.SCAN_REQUEST_TAG;

public abstract class Utilities {

    public static boolean checkPermissionActivity(Activity activity, String permission){

        return ContextCompat.checkSelfPermission(activity.getApplicationContext(), permission) == PackageManager.PERMISSION_GRANTED;

    }

    public static void askPermissionActivity(Activity activity, String permission, final int callbackRequest)
    {
        ActivityCompat.requestPermissions(activity, new String[] {permission}, callbackRequest);
    }

    public static Intent getSearchBarIntent(Activity activity, LatLng center, Double radius, int selectedFilter) throws GooglePlayServicesNotAvailableException, GooglePlayServicesRepairableException {
        return new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                .setFilter(new AutocompleteFilter.Builder().setTypeFilter(selectedFilter).build())
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

    public static String Isbn10ToIsbn13(String isbn10) {
        // https://stackoverflow.com/questions/17108621/converting-isbn10-to-isbn13
        int d, sum = 0;
        String isbn13 = isbn10;
        isbn13 = "978" + isbn13.substring(0, 9);
        //if (LOG_D) Log.d(TAG, "ISBN13 without sum" + ISBN13);
        for (int i = 0; i < isbn13.length(); i++) {
            d = ((i % 2 == 0) ? 1 : 3);
            sum += ((((int) isbn13.charAt(i)) - 48) * d);
            //if (LOG_D) Log.d(TAG, "adding " + ISBN13.charAt(i) + "x" + d + "=" + ((((int) ISBN13.charAt(i)) - 48) * d));
        }
        sum = 10 - (sum % 10);
        isbn13 += sum;

        return isbn13;
    }

    public static Intent getPositionActivityIntent(Activity currentActivity, boolean searchCities) {
        Intent intent = new Intent(currentActivity, PositionActivity.class);
        if (searchCities)
            intent.putExtra(PositionActivity.SEARCH_CITY_EXTRA, true);
        return intent;
    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode;
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

    public static ArrayAdapter<String> makeDropDownAdapter(Activity containerActivity, String[] items) {
        List<String> temp;
        temp = new ArrayList<>(Arrays.asList(items));

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(containerActivity,
                android.R.layout.simple_spinner_item,
                temp);

        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return arrayAdapter;
    }

    public static void startBarcodeScanner(Activity activity) {
        Intent i = new Intent(activity, ScanBarCodeActivity.class);
        activity.startActivityForResult(i, SCAN_REQUEST_TAG);
    }

    public static File saveThumbnail(Context c) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = c.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
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
