package com.example.android.lab1;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScanBarCodeActivity extends Activity{

    private ZXingScannerView mScannerView;
    public static final String BARCODE_KEY = "BARCODE_READ";

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = findViewById(R.id.scanner_fragment);
        setContentView(R.layout.barcode_scanner_layout);
    }


}
