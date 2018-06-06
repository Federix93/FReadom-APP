package com.example.android.lab1.ui.homepage;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.android.lab1.R;
import com.example.android.lab1.adapter.RecyclerFragmentLoansAdapter;
import com.example.android.lab1.model.Book;
import com.example.android.lab1.model.chatmodels.User;
import com.example.android.lab1.viewmodel.BorrowedBooksViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;

public class BorrowedFragmentItem extends Fragment {

    private RecyclerView mRecyclerView;
    private RecyclerFragmentLoansAdapter mAdapter;
    private List<User> mUserOwners;
    LinearLayout mNoLoansLayout;
    LinearLayout mNoRequestRecLayout;
    LinearLayout mNoRequestDoneLayout;

    public BorrowedFragmentItem() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recycler_fragments_content, container, false);

        mRecyclerView = view.findViewById(R.id.recycler_fragment_content);
        mNoLoansLayout = view.findViewById(R.id.no_loans);
        mNoRequestRecLayout = view.findViewById(R.id.no_request_rec);
        mNoRequestDoneLayout = view.findViewById(R.id.no_request_done);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setNestedScrollingEnabled(true);

        mUserOwners = new ArrayList<>();

        mAdapter = new RecyclerFragmentLoansAdapter(new ArrayList<Book>(), mUserOwners);
        mRecyclerView.setAdapter(mAdapter);

        if (FirebaseAuth.getInstance().getUid() != null) {
            if (getActivity() != null) {
                BorrowedBooksViewModel borrowedBooksViewModel = ViewModelProviders.of(getActivity()).get(BorrowedBooksViewModel.class);
                borrowedBooksViewModel.getSnapshotLiveData().observe(getActivity(), new Observer<List<Book>>() {
                    @Override
                    public void onChanged(@android.support.annotation.Nullable List<Book> books) {
                        if(books != null) {
                            if (books.size() > 0) {
                                mNoLoansLayout.setVisibility(GONE);
                                mNoRequestRecLayout.setVisibility(View.GONE);
                                mNoRequestDoneLayout.setVisibility(GONE);
                            }
                            final List<Book> listBooks = new ArrayList<>();
                            for (final Book b : books) {
                                FirebaseDatabase.getInstance().getReference("users").child(b.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        listBooks.add(b);
                                        mUserOwners.add(dataSnapshot.getValue(User.class));
                                        mAdapter.setItems(listBooks, mUserOwners);
                                        mAdapter.notifyDataSetChanged();                            }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        }
                    }
                });
            }
        } else {
            mNoLoansLayout.setVisibility(View.VISIBLE);
            mNoRequestRecLayout.setVisibility(View.GONE);
            mNoRequestDoneLayout.setVisibility(GONE);
        }

        return view;
    }
}