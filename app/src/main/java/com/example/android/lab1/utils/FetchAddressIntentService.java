package com.example.android.lab1.utils;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.example.android.lab1.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Federico on 23/03/2018.
 */

public class FetchAddressIntentService extends IntentService {

    protected ResultReceiver mReceiver;
    private Geocoder mGeoCoder;
    private String ID;

    public FetchAddressIntentService() {
        super(Constants.SERVICE_NAME);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        List<Address> addresses = null;

        if (ID == null)
            ID = UUID.randomUUID().toString();

        if (intent == null
                || ! intent.hasExtra(Constants.LOCATION_DATA_EXTRA)
                || ! intent.hasExtra(Constants.RECEIVER))
            return;

        if (mGeoCoder == null)
            mGeoCoder = new Geocoder(this, Locale.getDefault());

        Location location = intent.getParcelableExtra(Constants.LOCATION_DATA_EXTRA);
        mReceiver = intent.getParcelableExtra(Constants.RECEIVER);
        boolean resolveCity = intent.getBooleanExtra(Constants.RESOLVE_CITY, false);
        int position = intent.getIntExtra(Constants.ADAPTER_POSITION, -1);
        int resultCode = Activity.RESULT_CANCELED;
        String result = null;

        try{
            addresses = mGeoCoder.getFromLocation(location.getLatitude(),
                    location.getLongitude(),
                    1);
        }catch(IOException e){
            result = getString(R.string.service_not_available);
        }catch(IllegalArgumentException e) {
            result = getString(R.string.invalid_lat_long_used);
        }
        if (addresses == null || addresses.size()  == 0) {
            if (result == null || result.isEmpty()) {
                result = getString(R.string.no_address_found);
            }
        }else {
            resultCode = RESULT_OK;
            if (! resolveCity) {
                Address address = addresses.get(0);
                ArrayList<String> addressFragments = new ArrayList<String>();

                // Fetch the address lines using getAddressLine,
                // join them, and send them to the thread.
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    addressFragments.add(address.getAddressLine(i));
                }
                result = TextUtils.join(System.getProperty("line.separator"),
                        addressFragments);
            }
            else
                result = addresses.get(0).getLocality();

        }

        deliverResultToReceiver(resultCode,
                result,
                resolveCity,
                position);
    }

    public final class Constants {
        public static final String PACKAGE_NAME = "com.example.android.lab1";
        public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
        public static final String RESULT_DATA_KEY = PACKAGE_NAME +
                ".RESULT_DATA_KEY";
        public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME +
                ".LOCATION_DATA_EXTRA";
        public static final String SERVICE_NAME = "FetchAddressIntentService";
        public static final String RESOLVE_CITY = "RESOLVE_CITY";
        public static final String ADAPTER_POSITION = "ADAPTER_POSITION"; // if service invoked in adapter resend him back the position
        public static final String RESULT = "RESULT";
    }

    private void deliverResultToReceiver(int resultCode,
                                         String resolved,
                                         boolean isCity,
                                         int position) {
        Bundle bundle = new Bundle();
        if (resultCode == RESULT_OK && resolved != null)
        {
            bundle.putString(Constants.RESULT, resolved);
            bundle.putBoolean(Constants.RESOLVE_CITY, isCity);
            if (position > -1)
                bundle.putInt(Constants.ADAPTER_POSITION, position);
        }
        mReceiver.send(resultCode, bundle);
    }
}
