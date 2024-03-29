package com.example.android.lab1.ui.homepage;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.android.lab1.R;
import com.example.android.lab1.adapter.RecyclerFragmentRequestsDoneAdapter;
import com.example.android.lab1.model.Book;
import com.example.android.lab1.model.chatmodels.User;
import com.example.android.lab1.viewmodel.RequestedDoneBooksViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;

public class RequestsFragmentDoneItem extends Fragment {

    private RecyclerView mRecyclerView;
    RecyclerFragmentRequestsDoneAdapter mAdapter;

    LinearLayout mNoLoansLayout;
    LinearLayout mNoRequestRecLayout;
    LinearLayout mNoRequestDoneLayout;

    public void RequestsFragmentDoneItem() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recycler_fragments_content, container, false);

        mRecyclerView = view.findViewById(R.id.recycler_fragment_content);
        mNoLoansLayout = view.findViewById(R.id.no_loans);
        mNoRequestRecLayout = view.findViewById(R.id.no_request_rec);
        mNoRequestDoneLayout = view.findViewById(R.id.no_request_done);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setNestedScrollingEnabled(true);

        mAdapter = new RecyclerFragmentRequestsDoneAdapter(getActivity(), new ArrayList<Book>(), new ArrayList<User>(),
                new RecyclerFragmentRequestsDoneAdapter.OnRemovedCallBack() {
                    @Override
                    public void positionRemoved(int position) {
                        if (mAdapter.getItemCount() == 0) {
                            mNoRequestDoneLayout.setVisibility(View.VISIBLE);
                        }
                    }
                });

        mRecyclerView.setAdapter(mAdapter);

        if (FirebaseAuth.getInstance().getUid() != null) {
            RequestedDoneBooksViewModel requestedDoneBooksViewModel = ViewModelProviders.of(getActivity()).get(RequestedDoneBooksViewModel.class);
            requestedDoneBooksViewModel.getSnapshotLiveData().observe(getActivity(), new Observer<List<Book>>() {
                @Override
                public void onChanged(@Nullable List<Book> books) {
                    if (books != null && !books.isEmpty()) {
                        mNoLoansLayout.setVisibility(GONE);
                        mNoRequestRecLayout.setVisibility(View.GONE);
                        mNoRequestDoneLayout.setVisibility(GONE);
                        mRecyclerView.setVisibility(View.VISIBLE);

                        final List<Book> listBooks = new ArrayList<>();
                        final List<User> usersOwner = new ArrayList<>();
                        for (final Book b : books) {
                            FirebaseDatabase.getInstance().getReference("users").child(b.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    listBooks.add(b);
                                    usersOwner.add(dataSnapshot.getValue(User.class));
                                    mAdapter.setItems(listBooks, usersOwner);
                                    mAdapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    } else {
                        mAdapter.setItems(new ArrayList<Book>(), new ArrayList<User>());
                        mAdapter.notifyItemRemoved(0);
                        mNoLoansLayout.setVisibility(View.GONE);
                        mNoRequestRecLayout.setVisibility(GONE);
                        mNoRequestDoneLayout.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
        return view;
    }
}
