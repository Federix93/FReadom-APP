package com.example.android.lab1.ui;

import android.os.Bundle;
import android.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.lab1.R;


public class InformationFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";


    private String mParam1;
    private TextView mContentFragmentTextView;

    public InformationFragment() {
        // Required empty public constructor
    }


    public static android.app.Fragment newInstance(String param1) {
        InformationFragment fragment = new InformationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View rootView = inflater.inflate(R.layout.fragment_information, container, false);
        mContentFragmentTextView = rootView.findViewById(R.id.fragment_text_view);
        Log.d("LULLO", mParam1);
        if(mParam1.equals("TERMS"))
            mContentFragmentTextView.setText(Html.fromHtml(getResources().getString(R.string.terms_content)));

        return rootView;
    }
}
