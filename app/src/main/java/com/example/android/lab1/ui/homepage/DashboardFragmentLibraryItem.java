package com.example.android.lab1.ui.homepage;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.lab1.R;
import com.example.android.lab1.ui.listeners.AddNewBookListener;

public class DashboardFragmentLibraryItem extends Fragment {

    private FloatingActionButton mAddNewBook;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard_library, container, false);

        mAddNewBook = view.findViewById(R.id.personal_library_new_book_button);
        mAddNewBook.setOnClickListener(new AddNewBookListener(getActivity()));

        return view;
    }


}
