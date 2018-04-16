package com.example.android.lab1;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class ProfileFragment extends Fragment {

    TextInputLayout mUsernameTextInputLayout;
    TextInputLayout mEmailTextInputLayout;
    TextInputLayout mPhoneTextInputLayout;
    TextInputLayout mAddressTextInputLayout;
    TextInputLayout mShortBioTextInputLayout;
    ImageView mCircleImageView;
    private Toolbar mToolbar;

    public static final String ADDRESS_KEY = "ADDRESS";

    public ProfileFragment()
    {

    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        mUsernameTextInputLayout = rootView.findViewById(R.id.username_text);

        mPhoneTextInputLayout = rootView.findViewById(R.id.phone_text);

        mEmailTextInputLayout = rootView.findViewById(R.id.email_text);

        mAddressTextInputLayout = rootView.findViewById(R.id.address_text_view);

        mCircleImageView = rootView.findViewById(R.id.profile_image);

        mShortBioTextInputLayout = rootView.findViewById(R.id.bio_text_edit);

        final SharedPreferencesManager sharedPreferencesManager = SharedPreferencesManager.getInstance(getContext());
        User user = sharedPreferencesManager.getUser();

        mToolbar = getActivity().findViewById(R.id.toolbar_main_activity);
        mToolbar.setTitle(R.string.title_profile);
        mToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        mToolbar.getMenu().clear();
        mToolbar.inflateMenu(R.menu.fragment_profile);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int clickedId = item.getItemId();
                if (clickedId == R.id.action_edit) {
                    Intent intent = new Intent(rootView.getContext(), EditProfileActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    User u = sharedPreferencesManager.getUser();
                    if (u != null)
                        intent.putExtra(ADDRESS_KEY, u.getAddress());
                    startActivity(intent);
                }
                return true;
            }
        });

        if (mUsernameTextInputLayout.getEditText() != null) {
            if(user != null)
                mUsernameTextInputLayout.getEditText().setText(user.getUsername());
            mUsernameTextInputLayout.getEditText().setKeyListener(null);
            mUsernameTextInputLayout.getEditText().setEnabled(false);
        }
        if (mEmailTextInputLayout.getEditText() != null) {
            if(user != null)
                mEmailTextInputLayout.getEditText().setText(user.getEmail());
            mEmailTextInputLayout.getEditText().setKeyListener(null);
            mEmailTextInputLayout.getEditText().setEnabled(false);
        }
        if (mPhoneTextInputLayout.getEditText() != null) {
            if(user != null)
                mPhoneTextInputLayout.getEditText().setText(user.getPhone());
            mPhoneTextInputLayout.getEditText().setKeyListener(null);
            mPhoneTextInputLayout.getEditText().setEnabled(false);
        }

        if(mShortBioTextInputLayout.getEditText() != null){
            if(user != null)
                mShortBioTextInputLayout.getEditText().setText(user.getShortBio());
            mShortBioTextInputLayout.getEditText().setKeyListener(null);
            mShortBioTextInputLayout.getEditText().setEnabled(false);
        }

        if(user != null && user.getImage() != null) {
            Glide.with(this).load(user.getImage())
                    .apply(bitmapTransform(new CircleCrop()))
                    .into(mCircleImageView);
        }else{
            Glide.with(this).load(getResources().getDrawable(R.drawable.ic_account_circle_black_24dp))
                    .apply(bitmapTransform(new CircleCrop()))
                    .into(mCircleImageView);
        }

        if(mAddressTextInputLayout.getEditText() != null){
            if(user != null)
                mAddressTextInputLayout.getEditText().setText(user.getAddress());
            mAddressTextInputLayout.getEditText().setKeyListener(null);
            mAddressTextInputLayout.getEditText().setEnabled(false);
        }
/*
        mToolbarIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), EditProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                User u = sharedPreferencesManager.getUser();
                if (u != null)
                    intent.putExtra(ADDRESS_KEY, u.getAddress());
                startActivity(intent);
            }
        });
*/

        /*
        // If a list of image ids exists, set the image resource to the correct item in that list
        // Otherwise, create a Log statement that indicates that the list was not found
        if(mImageIds != null){
            // Set the image resource to the list item at the stored index
            imageView.setImageResource(mImageIds.get(mListIndex));

            // Set a click listener on the image view
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Increment position as long as the index remains <= the size of the image ids list
                    if(mListIndex < mImageIds.size()-1) {
                        mListIndex++;
                    } else {
                        // The end of list has been reached, so return to beginning index
                        mListIndex = 0;
                    }
                    // Set the image resource to the new list item
                    imageView.setImageResource(mImageIds.get(mListIndex));
                }
            });

        } else {
            Log.v(TAG, "This fragment has a null list of image id's");
        }
        */

        // Return the rootView
        return rootView;
    }
}
