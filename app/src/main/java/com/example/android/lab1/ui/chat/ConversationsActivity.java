package com.example.android.lab1.ui.chat;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.example.android.lab1.R;
import com.example.android.lab1.adapter.RecyclerConversationAdapter;
import com.example.android.lab1.model.chatmodels.Chat;
import com.example.android.lab1.model.chatmodels.Conversation;
import com.example.android.lab1.model.chatmodels.User;
import com.example.android.lab1.utils.NotificationUtilities;
import com.example.android.lab1.utils.Utilities;
import com.example.android.lab1.viewmodel.ConversationsViewModel;
import com.example.android.lab1.viewmodel.ViewModelFactory;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConversationsActivity extends AppCompatActivity {

    Toolbar mToolbar;
    String mBookID;
    RecyclerView mRecyclerView;
    RecyclerView.OnItemTouchListener listener;
    private RecyclerConversationAdapter mAdapter;
    private ShimmerFrameLayout mShimmerViewContainer;
    private ArrayList<Conversation> mConversations;
    private ChildEventListener mChildEventListener;
    private ConversationsViewModel mConversationsViewModel;
    private Observer<DataSnapshot> mObserver;

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
                onBackPressed();
            }
        });
        mToolbar.inflateMenu(R.menu.conversations_menu);
        mToolbar.getMenu().findItem(R.id.delete_chat).setVisible(false);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (mAdapter != null) {
                    if (listener != null)
                        mAdapter.deleteChat(listener);
                }
                return true;
            }
        });
        mShimmerViewContainer = findViewById(R.id.shimmer_view_container);

        mRecyclerView = findViewById(R.id.conversation_recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setNestedScrollingEnabled(true);
        listener = new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent motionEvent) {
                if (motionEvent.getAction() != MotionEvent.ACTION_UP) {
                    return false;
                }
                View child = mRecyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
                if (child == null && mAdapter != null) {
                    mAdapter.deselectItem();
                    return true;
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        };

        mRecyclerView.addOnItemTouchListener(listener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth != null) {
            mConversationsViewModel = ViewModelProviders.of(this, new ViewModelFactory(mBookID)).get(ConversationsViewModel.class);
            // add new conversation
            mObserver = new Observer<DataSnapshot>() {
                @Override
                public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                    final ArrayList<String> userIds = new ArrayList<>();
                    final ArrayList<String> chatIds = new ArrayList<>();
                    final Map<String, Chat> chatsObj = new HashMap<>();
                    mConversations = new ArrayList<>();
                    for (final DataSnapshot d : dataSnapshot.getChildren()) {
                        userIds.add(d.getValue().toString());
                        chatIds.add(d.getKey());
                    }
                    FirebaseDatabase.getInstance()
                            .getReference("users")
                            .addChildEventListener(new ChildEventListener() {
                                @Override
                                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                    if (userIds.contains(dataSnapshot.getKey())) {
                                        int i = userIds.indexOf(dataSnapshot.getKey());
                                        Conversation conversation = new Conversation(
                                                dataSnapshot.getValue(User.class),
                                                chatIds.get(i));
                                        conversation.setUserId(userIds.get(i));
                                        mConversations.add(conversation);
                                        if (mShimmerViewContainer.isAnimationStarted()) {
                                            mShimmerViewContainer.stopShimmerAnimation();
                                            mShimmerViewContainer.setVisibility(View.GONE);
                                        }
                                    }
                                    if (mConversations.size() >= userIds.size()) {
                                        mChildEventListener = FirebaseDatabase.getInstance()
                                                .getReference("chats")
                                                .orderByChild("timestamp")
                                                .addChildEventListener(new ChildEventListener() {
                                                    @Override
                                                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                                        if (!chatIds.contains(dataSnapshot.getKey()))
                                                            return;
                                                        if (chatsObj.size() < chatIds.size()) // avoid replication
                                                            chatsObj.put(dataSnapshot.getKey(), dataSnapshot.getValue(Chat.class));
                                                        if (chatsObj.size() >= chatIds.size()) {
                                                            for (int i = 0; i < mConversations.size(); i++) {
                                                                mConversations.get(i).setChat(
                                                                        chatsObj.get(mConversations.get(i).getChatId())
                                                                );
                                                            }

                                                            Conversation temp;
                                                            for (int i = 0; i < mConversations.size() - 1; i++) {
                                                                for (int i1 = i + 1; i1 < mConversations.size(); i1++) {
                                                                    if (mConversations.get(i).getTimestamp() <
                                                                            mConversations.get(i1).getTimestamp()) {
                                                                        temp = mConversations.get(i);
                                                                        mConversations.set(i, mConversations.get(i1));
                                                                        mConversations.set(i1, temp);
                                                                    }
                                                                }
                                                            }
                                                            mAdapter = new RecyclerConversationAdapter(mConversations,
                                                                    mBookID,
                                                                    mToolbar,
                                                                    new RecyclerConversationAdapter.ActivityCallBack() {
                                                                        @Override
                                                                        public void doClose() {
                                                                            finish();
                                                                        }

                                                                        @Override
                                                                        public void removeWithContext(String chatId) {
                                                                            if (NotificationUtilities.notificationExist(chatId))
                                                                                NotificationUtilities.removeNotification(chatId,
                                                                                        ConversationsActivity.this,
                                                                                        true);
                                                                        }
                                                                    });
                                                            mRecyclerView.setAdapter(mAdapter);
                                                        }

                                                    }

                                                    @Override
                                                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                                                        if (!chatIds.contains(dataSnapshot.getKey()))
                                                            return;
                                                        Chat chat = dataSnapshot.getValue(Chat.class);
                                                        Conversation updated = null;
                                                        for (int i = 0; i < mConversations.size(); i++) {
                                                            if (mConversations.get(i).getChatId().equals(dataSnapshot.getKey())) {
                                                                updated = mConversations.remove(i);
                                                                updated.setChat(chat);
                                                                break;
                                                            }
                                                        }
                                                        if (updated != null) {
                                                            // are sorted so
                                                            mConversations.add(0, updated);
                                                            mAdapter.notifyDataSetChanged();
                                                        }
                                                    }

                                                    @Override
                                                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                                                    }

                                                    @Override
                                                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });
                                    }
                                }

                                @Override
                                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                                }

                                @Override
                                public void onChildRemoved(DataSnapshot dataSnapshot) {

                                }

                                @Override
                                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                }
            };
            mConversationsViewModel.getSnapshotLiveData().observe(this, mObserver);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mChildEventListener != null)
            FirebaseDatabase.getInstance().getReference("chats").removeEventListener(mChildEventListener);
        if (mObserver != null)
            mConversationsViewModel.getSnapshotLiveData().removeObserver(mObserver);
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

    @Override
    public void onBackPressed() {
        if (mAdapter != null) {
            if (mAdapter.isSomeItemSelected()) {
                mAdapter.deselectItem();
                return;
            }
        }
        super.onBackPressed();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.conversations_menu, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == R.id.delete_chat) {
//            Log.d("LULLO", "Clicked");
//        }
//        return super.onOptionsItemSelected(item);
//    }

    private class DiffUtilCallback extends DiffUtil.Callback {

        private List<Conversation> oldList;
        private List<Conversation> newList;

        public DiffUtilCallback(List<Conversation> oldList, List<Conversation> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition) == newList.get(newItemPosition);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Conversation oldConversation = oldList.get(oldItemPosition);
            Conversation newConversation = newList.get(newItemPosition);
            return oldConversation.getTimestamp().equals(newConversation.getTimestamp()) &&
                    oldConversation.getChatId().equals(newConversation.getChatId());
        }
    }
}
