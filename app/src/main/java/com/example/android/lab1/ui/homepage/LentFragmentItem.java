package com.example.android.lab1.ui.homepage;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.lab1.R;
import com.example.android.lab1.adapter.RecyclerFragmentBooksAdapter;
import com.example.android.lab1.model.Book;
import com.example.android.lab1.model.chatmodels.User;
import com.example.android.lab1.viewmodel.LoansViewModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class LentFragmentItem extends Fragment {

    private RecyclerView mRecyclerView;
    private RecyclerFragmentBooksAdapter mAdapter;

    public LentFragmentItem() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recycler_fragments_content, container, false);

        mRecyclerView = view.findViewById(R.id.recycler_fragment_content);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setNestedScrollingEnabled(true);

        mAdapter = new RecyclerFragmentBooksAdapter(getActivity(), new ArrayList<Book>(), new ArrayList<User>(), true);
        mRecyclerView.setAdapter(mAdapter);
        LoansViewModel loansViewModel = ViewModelProviders.of(getActivity()).get(LoansViewModel.class);
        loansViewModel.getSnapshotLiveData().observe(getActivity(), new Observer<List<Book>>() {
            @Override
            public void onChanged(@Nullable List<Book> books) {
                if(books != null) {
                    final List<Book> listBooks = new ArrayList<>();
                    final List<User> otherUsers = new ArrayList<>();
                    for (final Book b : books) {
                        FirebaseDatabase.getInstance().getReference("users").child(b.getLentTo()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                listBooks.add(b);
                                otherUsers.add(dataSnapshot.getValue(User.class));
                                mAdapter.setItems(listBooks, otherUsers);
                                mAdapter.notifyDataSetChanged();                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }
        });
        return view;
    }

}

