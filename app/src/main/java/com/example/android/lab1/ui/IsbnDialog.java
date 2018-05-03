package com.example.android.lab1.ui;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.android.lab1.R;

public class IsbnDialog extends DialogFragment {

    private IsbnDialogListener mIsbnDialogListener;
    private EditText mIsbnEditText;
    private Button mSearchInternetButton;
    private Button mScanBarCodeButton;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View rootView = inflater.inflate(R.layout.dialog_isbn, null);

        mIsbnEditText = rootView.findViewById(R.id.dialog_isbn_isbn);
        mSearchInternetButton = rootView.findViewById(R.id.dialog_isbn_search_internet);
        mScanBarCodeButton = rootView.findViewById(R.id.dialog_isbn_scan_bar_code);

        mSearchInternetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsbnEditText != null &&
                        mSearchInternetButton != null &&
                        mIsbnDialogListener != null)
                    mIsbnDialogListener.onSearchOnInternetButtonClick(mIsbnEditText.getText().toString());

            }
        });

        mScanBarCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsbnDialogListener != null) {
                    mIsbnDialogListener.onScanBarCodeButtonClick();
                }
            }
        });


        builder.setView(rootView);

        return builder.create();
    }

    public void setmIsbnDialogListener(IsbnDialogListener mIsbnDialogListener) {
        this.mIsbnDialogListener = mIsbnDialogListener;
    }

    public interface IsbnDialogListener {
        void onSearchOnInternetButtonClick(String isbn);

        void onScanBarCodeButtonClick();
    }
}
