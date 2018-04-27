package com.example.android.lab1.ui;

import android.app.Activity;
import android.os.Bundle;

import com.example.android.lab1.R;

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
