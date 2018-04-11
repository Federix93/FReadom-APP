package com.example.android.lab1;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScanBarCodeActivity extends Activity implements ZXingScannerView.ResultHandler{

    private ZXingScannerView mScannerView;
    public static final String BARCODE_KEY = "BARCODE_READ";

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        Log.d("SCANBAR", "onCreate: Created");
        setContentView(mScannerView);                // Set the scanner view as the content view
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result result)
    {
        Intent res = new Intent();
        Log.d("SCANBAR", "handleResult: " + result.getText());
        res.putExtra(BARCODE_KEY, result.getText());
        setResult(RESULT_OK, res);
        finish();
    }
}
