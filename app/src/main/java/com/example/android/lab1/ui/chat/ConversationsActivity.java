package com.example.android.lab1.ui.chat;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.example.android.lab1.R;
import com.example.android.lab1.adapter.RecyclerConversationAdapter;
import com.example.android.lab1.model.Book;
import com.example.android.lab1.model.chatmodels.Chat;
import com.example.android.lab1.model.chatmodels.User;
import com.example.android.lab1.utils.Utilities;
import com.example.android.lab1.viewmodel.ConversationsViewModel;
import com.example.android.lab1.viewmodel.UserRealtimeDBViewModel;
import com.example.android.lab1.viewmodel.UserViewModel;
import com.example.android.lab1.viewmodel.ViewModelFactory;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class ConversationsActivity extends AppCompatActivity {

    Toolbar mToolbar;
    String mBookID;
    RecyclerView mRecyclerView;
    private RecyclerConversationAdapter mAdapter;
    private ShimmerFrameLayout mShimmerViewContainer;
    ChildEventListener childEventListener;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversations);

        mBookID = getIntent().getStringExtra("BOOK_SELECTED");
        Utilities.setupStatusBarColor(this);
        mToolbar = findViewById(R.id.toolbar_conversations_activity);
        mToolbar.setTitle(R.string.conversations_title);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mShimmerViewContainer = findViewById(R.id.shimmer_view_container);

        mRecyclerView = findViewById(R.id.conversation_recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setNestedScrollingEnabled(true);
        mAdapter = new RecyclerConversationAdapter(mBookID);
        mRecyclerView.setAdapter(mAdapter);

        final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth != null) {

            ConversationsViewModel conversationsViewModel = ViewModelProviders.of(this, new ViewModelFactory(mBookID)).get(ConversationsViewModel.class);
            conversationsViewModel.getSnapshotLiveData().observe(this, new Observer<DataSnapshot>() {
                        @Override
                        public void onChanged(@android.support.annotation.Nullable DataSnapshot dataSnapshot) {
                            final List<User> userList = new ArrayList<>();
                            final List<DataSnapshot> snapshotList = new ArrayList<>();
                            for (final DataSnapshot d : dataSnapshot.getChildren()) {
                                snapshotList.add(d);
                                FirebaseDatabase.getInstance().getReference("users").child((String)d.getValue()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        User user = dataSnapshot.getValue(User.class);
                                        userList.add(user);
                                        mAdapter.setItems(snapshotList.get(userList.size() - 1).getKey(), user);
                                        mAdapter.notifyDataSetChanged();
                                        if(mShimmerViewContainer.isAnimationStarted()) {
                                            mShimmerViewContainer.stopShimmerAnimation();
                                            mShimmerViewContainer.setVisibility(View.GONE);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        }
                    });
        }
    }

    @Override
    protected void onPause() {
        mShimmerViewContainer.stopShimmerAnimation();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mShimmerViewContainer.startShimmerAnimation();
    }
}
