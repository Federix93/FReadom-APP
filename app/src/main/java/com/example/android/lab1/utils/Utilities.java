package com.example.android.lab1.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;

import com.example.android.lab1.R;
import com.example.android.lab1.model.Address;
import com.example.android.lab1.ui.PositionActivity;
import com.example.android.lab1.ui.ReviewActivity;
import com.example.android.lab1.ui.ScanBarCodeActivity;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.GeoPoint;
import com.google.maps.android.SphericalUtil;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.android.lab1.utils.Constants.SCAN_REQUEST_TAG;

public abstract class Utilities {

    public static boolean checkPermissionActivity(Activity activity, String permission) {

        return ContextCompat.checkSelfPermission(activity.getApplicationContext(), permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static void askPermissionActivity(Activity activity, String permission, final int callbackRequest) {
        ActivityCompat.requestPermissions(activity, new String[]{permission}, callbackRequest);
    }

    public static Intent getSearchBarIntent(Activity activity,
                                            LatLng center,
                                            Double radius,
                                            int selectedFilter) throws GooglePlayServicesNotAvailableException, GooglePlayServicesRepairableException {
        if (center != null && radius != null) {
            return new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                    .setFilter(new AutocompleteFilter.Builder().setTypeFilter(selectedFilter).build())
                    .setBoundsBias(Utilities.toBounds(center, radius))
                    .build(activity);
        } else
            return new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                    .setFilter(new AutocompleteFilter.Builder().setTypeFilter(selectedFilter).build())
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
    public static void setupStatusBarColor(Activity activity) {
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

    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
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
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
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

    public static String getSha1Hex(String clearString) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            messageDigest.update(clearString.getBytes("UTF-8"));
            byte[] bytes = messageDigest.digest();
            StringBuilder buffer = new StringBuilder();
            for (byte b : bytes) {
                buffer.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            return buffer.toString();
        } catch (Exception ignored) {
            ignored.printStackTrace();
            return null;
        }
    }

    private static String getRealPathFromURI(Uri contentUri, Context context) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(context, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

    public static Intent getRatingIntent(Activity currentActivity,
                                         String reviewerId,
                                         String reviewedId,
                                         String bookId) {
        if (currentActivity == null || reviewedId == null || reviewerId == null)
            return null;
        Intent i = new Intent(currentActivity, ReviewActivity.class);
        i.putExtra(ReviewActivity.Keys.REVIEWER_ID, reviewerId);
        i.putExtra(ReviewActivity.Keys.REVIEWED_ID, reviewedId);
        i.putExtra(ReviewActivity.Keys.BOOK_ID, bookId);
        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return i;
    }

    public static void showSoftKeyboard(Context context, View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager)
                    context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null)
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public static void hideKeyboard(Context context, Activity activity) {
        InputMethodManager inputManager =
                (InputMethodManager) context.
                        getSystemService(Context.INPUT_METHOD_SERVICE);
        if (activity.getCurrentFocus() != null &&
                activity.getCurrentFocus().getWindowToken() != null &&
                inputManager != null)
            inputManager.hideSoftInputFromWindow(
                    activity.getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static byte[] compressPhoto(String filePath, ContentResolver contentResolver, Context context) {
        int rotationAngle = 0;
        ExifInterface ei = null;
        if (filePath.contains("content"))
            filePath = Utilities.getRealPathFromURI(Uri.parse(filePath), context);
        try {
            ei = new ExifInterface(filePath);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);


            switch (orientation) {

                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotationAngle = 90;
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotationAngle = 180;
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotationAngle = 270;
                    break;

                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    rotationAngle = 0;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        Uri filePathUri = Uri.parse(filePath);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        ByteArrayOutputStream out;
        Bitmap bitmap = null;
        if (filePathUri.getScheme() != null && filePathUri.getScheme().equals("content")) {
            try {
                bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(filePathUri));
            } catch (FileNotFoundException e) {
                return null;
            }
        } else
            bitmap = BitmapFactory.decodeFile(filePath, options);
        // rotate bitmap
        Bitmap compressedRotated;
        out = new ByteArrayOutputStream();
        if (rotationAngle > 0) {
            Matrix m = new Matrix();
            m.postRotate(rotationAngle);
            compressedRotated = Bitmap.createBitmap(bitmap,
                    0,
                    0,
                    bitmap.getWidth(),
                    bitmap.getHeight(),
                    m,
                    true);
            compressedRotated.compress(Bitmap.CompressFormat.JPEG, 60, out);
        } else
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, out);

        return out.toByteArray();
    }

    public static double distanceBetweenGeoPoints(double lat1, double lon1, double lat2, double lon2, char unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == 'K') {
            dist = dist * 1.609344;
        } else if (unit == 'N') {
            dist = dist * 0.8684;
        }
        return (dist);
    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private static double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    public static void enableLoc(final Activity activity) {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(LocationRequest.create());
        SettingsClient client = LocationServices.getSettingsClient(activity);
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
                                        .startResolutionForResult(activity,
                                                Constants.PLAY_SERVICES_RESOLUTION_REQUEST);
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

    public static void getGoogleAddressSearchBar(Activity container,
                                                 LatLng location,
                                                 boolean searchCities,
                                                 int requestCode) throws GooglePlayServicesNotAvailableException, GooglePlayServicesRepairableException {
        try {
            Intent googleAddressSearchBar = Utilities.getSearchBarIntent(container,
                    location,
                    Double.valueOf(Integer.toString(container.getResources().getInteger(R.integer.position_radius_address))),
                    searchCities ? AutocompleteFilter.TYPE_FILTER_CITIES : AutocompleteFilter.TYPE_FILTER_ADDRESS);
            container.startActivityForResult(googleAddressSearchBar,
                    requestCode);
        } catch (GooglePlayServicesNotAvailableException | GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        }
    }

    public static GeoPoint[] buildBoundingBox(Double latitude, Double longitude, Double distanceInKm) {

        // ~1 mile of lat and lon in degrees
        //Double lat = 0.0144927536231884;
        //Double lon = 0.0181818181818182;

//        Double lowerLat = latitude - (lat * distanceInKm);
//        Double lowerLon = longitude - (lon * distanceInKm);
//
//        Double greaterLat = latitude + (lat * distanceInKm);
//        Double greaterLon = longitude + (lon * distanceInKm);

        //GeoPoint lesserGeoPoint = new GeoPoint(lowerLat, lowerLon);
        //GeoPoint greaterGeoPoint = new GeoPoint(greaterLat, greaterLon);

        double lat = latitude;
        double longi = longitude;

// 6378000 Size of the Earth (in meters)
        double longitudeD = (Math.asin(distanceInKm / (6378000 * Math.cos(Math.PI*lat/180))))*180/Math.PI;
        double latitudeD = (Math.asin(distanceInKm / (double)6378000))*180/Math.PI;

        double latitudeMax = lat+(latitudeD);
        double latitudeMin = lat-(latitudeD);
        double longitudeMax = longi+(longitudeD);
        double longitudeMin = longi-(longitudeD);
        GeoPoint lesserGeoPoint = new GeoPoint(latitudeMin, longitudeMin);
        GeoPoint greaterGeoPoint = new GeoPoint(latitudeMax, longitudeMax);
        return new GeoPoint[]{lesserGeoPoint, greaterGeoPoint};
    }


    public static void resolveSingleLocation(@NonNull WeakReference<Activity> activityWeakReference,
                                             @NonNull GeoPoint location,
                                             boolean getCity,
                                             @NonNull ResultReceiver resultReceiver) {
        Activity activity = activityWeakReference.get();
        if (activity != null) {
            Intent intent = new Intent(activity, FetchAddressIntentService.class);
            intent.putExtra(FetchAddressIntentService.Constants.RECEIVER, resultReceiver);
            Location location1 = new Location("default");
            location1.setLatitude(location.getLatitude());
            location1.setLongitude(location.getLongitude());
            intent.putExtra(FetchAddressIntentService.Constants.LOCATION_DATA_EXTRA, location1);
            intent.putExtra(FetchAddressIntentService.Constants.RESOLVE_CITY, getCity);
            activity.startService(intent);
        }
    }
}
