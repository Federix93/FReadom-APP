package com.example.android.lab1.ui.profile;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.lab1.R;
import com.example.android.lab1.model.User;

public class ProfileInfoFragment extends Fragment {

    TextView mEmailText;
    TextView mPhoneText;
    TextView mShortBioText;

    User mUser;

    public void ProfileInfoFragment () {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_info, container, false);

        mPhoneText = view.findViewById(R.id.phone_text);
        mEmailText = view.findViewById(R.id.email_text);
        mShortBioText = view.findViewById(R.id.bio_text_edit);

        mUser = ProfileActivity.getUser();
        updateUI();

        return view;
    }

    private void updateUI() {
        if (mUser.getEmail() != null)
            mEmailText.setText(mUser.getEmail());

        if (mUser.getPhone() != null)
            mPhoneText.setText(mUser.getPhone());

        if (mUser.getShortBio() != null)
            mShortBioText.setText(mUser.getShortBio());

    }
}
