package com.example.android.lab1.ui.Profile;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.lab1.R;
import com.example.android.lab1.adapter.ProfileBookAdapter;
import com.example.android.lab1.model.Book;
import com.example.android.lab1.model.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class GlobalProfileBooksFragment extends Fragment {

    User mUser;
    TextView mBookNumber;
    TextView mRatingNumber;

    private RecyclerView mRecyclerView;

    public void GlobalProfileBooksFragment (){
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recycler_fragments_content, container, false);

        mUser = GlobalShowProfileActivity.getUser();
        mRecyclerView = view.findViewById(R.id.recycler_fragment_content);

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        firebaseFirestore.setFirestoreSettings(settings);
        firebaseFirestore.collection("books").whereEqualTo("uid", GlobalShowProfileActivity.getUserId())
                .addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                        List<Book> mListBooksOfUser = queryDocumentSnapshots.toObjects(Book.class);
                        List<String> IDs = new ArrayList<>();
                        for(DocumentSnapshot d : queryDocumentSnapshots.getDocuments()){
                            IDs.add(d.getId());
                        }
                        int numOfBooks = mListBooksOfUser.size();
                        updateListOfBooks(mListBooksOfUser, numOfBooks, IDs);
                    }
                });
        return view;
    }

    private void updateListOfBooks(List<Book> mListBooksOfUser, int numOfBooks, List<String> bookIds) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setNestedScrollingEnabled(false);

        ProfileBookAdapter adapter = new ProfileBookAdapter(mListBooksOfUser, bookIds);
        mRecyclerView.setAdapter(adapter);
        GlobalShowProfileActivity.getBookNumber().setText(String.valueOf(numOfBooks));
    }


}
