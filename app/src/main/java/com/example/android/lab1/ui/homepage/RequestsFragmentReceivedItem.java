package com.example.android.lab1.ui.homepage;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.android.lab1.R;
import com.example.android.lab1.adapter.RecyclerReqReceivedAdapter;
import com.example.android.lab1.model.Book;
import com.example.android.lab1.viewmodel.RequestsReceivedBooksViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;

public class RequestsFragmentReceivedItem extends Fragment {

    RecyclerView mRecyclerView;
    List<Book> listBooks;
    RecyclerReqReceivedAdapter mAdapter;
    LinearLayout mNoLoansLayout;
    LinearLayout mNoRequestRecLayout;
    LinearLayout mNoRequestDoneLayout;

    public void RequestsFragmentReceivedItem() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recycler_fragments_content, container, false);

        mRecyclerView = view.findViewById(R.id.recycler_fragment_content);
        mNoLoansLayout = view.findViewById(R.id.no_loans);
        mNoRequestRecLayout = view.findViewById(R.id.no_request_rec);
        mNoRequestDoneLayout = view.findViewById(R.id.no_request_done);

        if (getActivity() != null) {
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
            listBooks = new ArrayList<>();
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setLayoutManager(layoutManager);
            mRecyclerView.setNestedScrollingEnabled(true);

            mAdapter = new RecyclerReqReceivedAdapter(listBooks);
            mRecyclerView.setAdapter(mAdapter);

            if (FirebaseAuth.getInstance().getUid() != null) {
                RequestsReceivedBooksViewModel reqRecBooksViewModel = ViewModelProviders.of(getActivity()).get(RequestsReceivedBooksViewModel.class);
                reqRecBooksViewModel.getSnapshotLiveData().observe(getActivity(), new Observer<List<Book>>() {
                    @Override
                    public void onChanged(@Nullable List<Book> books) {
                        if (books != null && !books.isEmpty()) {
                            mNoRequestRecLayout.setVisibility(View.GONE);
                            mNoLoansLayout.setVisibility(View.GONE);
                            mNoRequestDoneLayout.setVisibility(GONE);

                            mAdapter = new RecyclerReqReceivedAdapter(books);
                            mRecyclerView.setAdapter(mAdapter);
                        } else {
                            mAdapter.setItems(new ArrayList<Book>());
                            mAdapter.notifyItemRemoved(0);
                            mNoRequestRecLayout.setVisibility(View.VISIBLE);
                            mNoLoansLayout.setVisibility(View.GONE);
                            mNoRequestDoneLayout.setVisibility(GONE);
                        }
                    }

                });
            }
        }

        return view;
    }

}
