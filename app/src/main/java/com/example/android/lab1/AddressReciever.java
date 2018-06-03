package com.example.android.lab1;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import com.example.android.lab1.adapter.AddressResultReciever;
import com.example.android.lab1.utils.FetchAddressIntentService;

public class AddressReciever extends ResultReceiver {

    private AddressResultReciever callBack;


    /**
     * Create a new ResultReceive to receive results.  Your
     * {@link #onReceiveResult} method will be called from the thread running
     * <var>handler</var> if given, or from an arbitrary thread if null.
     *
     * @param handler
     * @param callBack
     */
    public AddressReciever(Handler handler, AddressResultReciever callBack) {
        super(handler);
        this.callBack = callBack;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        super.onReceiveResult(resultCode, resultData);
        if (resultData == null)
            return;

        if (resultCode == Activity.RESULT_OK)
        {
            String address = resultData.getString(FetchAddressIntentService.Constants.RESULT);
            boolean isCity = resultData.getBoolean(FetchAddressIntentService.Constants.RESOLVE_CITY, false);
            int position = resultData.getInt(FetchAddressIntentService.Constants.ADAPTER_POSITION, -1);
            callBack.onPositionResolved(address, isCity, position);
        }
    }
}
