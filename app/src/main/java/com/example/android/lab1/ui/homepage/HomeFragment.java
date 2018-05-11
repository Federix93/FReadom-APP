package com.example.android.lab1.ui.homepage;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.lab1.R;
import com.example.android.lab1.adapter.RecyclerBookAdapter;
import com.example.android.lab1.model.Book;
import com.example.android.lab1.model.BookFilter;
import com.example.android.lab1.ui.searchbooks.SearchBookActivity;
import com.example.android.lab1.utils.SharedPreferencesManager;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {

    private static final int ADD_BOOK_REQUEST = 1;
    private static final String SHOWCASE_ID = "HomeFragment";

    View mRootView;
    Toolbar mToolbar;
    AppCompatButton mGenreFilterButton;
    AppCompatButton mPositionFilterButton;
    TextView mFirstOtherTextView;
    TextView mSecondOtherTextView;
    ImageView mSearchImageView;
    Query mQuery;
    FirebaseFirestore mFirebaseFirestore;
    private RecyclerView mFirstRecyclerView;
    private RecyclerView mSecondRecyclerView;
    private RecyclerBookAdapter mAdapter;

    public HomeFragment() {

    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.fragment_home, container, false);

        mToolbar = getActivity().findViewById(R.id.toolbar_home_page_activity);

        mFirstRecyclerView = mRootView.findViewById(R.id.first_recycler_books);
        mSecondRecyclerView = mRootView.findViewById(R.id.second_recycler_books);
        mGenreFilterButton = mRootView.findViewById(R.id.genre_filter_button);
        mPositionFilterButton = mRootView.findViewById(R.id.position_filter_button);
        mFirstOtherTextView = mRootView.findViewById(R.id.button_first_recycler_view);
        mSecondOtherTextView = mRootView.findViewById(R.id.button_second_recycler_view);

        mToolbar.setTitle(getString(R.string.toolbar_title_home));
        mToolbar.getMenu().clear();
        mToolbar.inflateMenu(R.menu.fragment_home);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int clickedId = item.getItemId();
                switch (clickedId) {
                    case R.id.action_search:
                        Intent intent = new Intent(getActivity(), SearchBookActivity.class);
                        startActivity(intent);
                        break;
                }
                return true;
            }
        });

        mGenreFilterButton.setOnClickListener(new View.OnClickListener() {
                                                  @Override
                                                  public void onClick(View v) {
                                                      AlertDialog dialog;
                                                      // arraylist to keep the selected items
                                                      final ArrayList selectedItems = new ArrayList();
                                                      AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                      builder.setTitle(R.string.select_genres);
                                                      builder.setMultiChoiceItems(R.array.genre,
                                                              null,
                                                              new DialogInterface.OnMultiChoiceClickListener() {
                                                                  //                 indexSelected contains the index of item (of which checkbox checked)
                                                                  @Override
                                                                  public void onClick(DialogInterface dialog, int indexSelected,
                                                                                      boolean isChecked) {
                                                                      if (isChecked) {
                                                                          // If the user checked the item, add it to the selected items
                                                                          // write your code when user checked the checkbox
                                                                          selectedItems.add(indexSelected);
                                                                      } else if (selectedItems.contains(indexSelected)) {
                                                                          // Else, if the item is already in the array, remove it
                                                                          //  write your code when user Uchecked the checkbox
                                                                          selectedItems.remove(Integer.valueOf(indexSelected));
                                                                      }
                                                                  }
                                                              })
                                                              .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                                                  @Override
                                                                  public void onClick(DialogInterface dialog, int id) {
                                                                      // Your code when user clicked on OK
                                                                      BookFilter bookFilter = BookFilter.buildGenderFilter(selectedItems.isEmpty() ? null : selectedItems);
                                                                      RecyclerBookAdapter firstRecyclerViewAdapter, secondRecyclerViewAdapter;
                                                                      if (mFirstRecyclerView != null && mFirstRecyclerView.getAdapter() != null) {
                                                                          firstRecyclerViewAdapter = (RecyclerBookAdapter) mFirstRecyclerView.getAdapter();
                                                                          firstRecyclerViewAdapter.setFilter(bookFilter);
                                                                      }

                                                                      if (mSecondRecyclerView != null && mSecondRecyclerView.getAdapter() != null) {
                                                                          secondRecyclerViewAdapter = (RecyclerBookAdapter) mSecondRecyclerView.getAdapter();
                                                                          secondRecyclerViewAdapter.setFilter(bookFilter);
                                                                      }
                                                                  }
                                                              })
                                                              .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                                                  @Override
                                                                  public void onClick(DialogInterface dialog, int id) {
                                                                      //  Your code when user clicked on Cancel

                                                                  }
                                                              });
                                                      dialog = builder.create();//AlertDialog dialog; create like this outside onClick
                                                      dialog.show();
                                                  }
                                              }
        );

        mPositionFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Function not implemented", Toast.LENGTH_SHORT).show();
            }
        });

        mFirstOtherTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Function not implemented", Toast.LENGTH_SHORT).show();
            }
        });

        mSecondOtherTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Function not implemented", Toast.LENGTH_SHORT).show();
            }
        });


        mFirstRecyclerView.setLayoutManager(
                new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        SnapHelper firstSnapHelperStart = new GravitySnapHelper(Gravity.START);
        mFirstRecyclerView.setNestedScrollingEnabled(true);
        firstSnapHelperStart.attachToRecyclerView(mFirstRecyclerView);

        mSecondRecyclerView.setLayoutManager(
                new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        SnapHelper secondSnapHelperStart = new GravitySnapHelper(Gravity.START);
        mSecondRecyclerView.setNestedScrollingEnabled(true);
        secondSnapHelperStart.attachToRecyclerView(mFirstRecyclerView);

        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mQuery = mFirebaseFirestore.collection("books");
        mQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }
                List<String> IDs = new ArrayList<>();
                for (DocumentSnapshot d : queryDocumentSnapshots.getDocuments()) {
                    IDs.add(d.getId());
                }
                List<Book> books = queryDocumentSnapshots.toObjects(Book.class);
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    for (int i = 0; i < books.size(); i++) {
                        if (books.get(i).getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            books.remove(i);
                            IDs.remove(i);
                            i--;
                        }
                    }
                }
                mAdapter = new RecyclerBookAdapter(books, IDs);
                mFirstRecyclerView.setAdapter(mAdapter);
                // order by time stamp
                List<Book> orderedByTime = new ArrayList<>(books);
                List<String> orderedByTimeIds = new ArrayList<>(IDs);
                /*Object temp[] = new Object[2];
                for (int i = 0; i < orderedByTime.size() - 1; i++) {
                    for (int i1 = 0; i1 < orderedByTime.size(); i1++) {
                        if (orderedByTime.get(i).getTimeInserted() > orderedByTime.get(i1).getTimeInserted())
                        {
                            temp[0] = orderedByTime.get(i);
                            temp[1] = orderedByTimeIds.get(i);

                            orderedByTime.set(i, orderedByTime.get(i1));
                            orderedByTimeIds.set(i, orderedByTimeIds.get(i1));

                            orderedByTime.set(i1, (Book) temp[0]);
                            orderedByTimeIds.set(i1, (String) temp[1]);
                        }
                    }
                }*/

                mSecondRecyclerView.setAdapter(new RecyclerBookAdapter(orderedByTime, orderedByTimeIds));
            }
        });
        if (SharedPreferencesManager.getInstance(getActivity()).isFirstRun()) {
            Resources res = getResources();
            SharedPreferencesManager.getInstance(getActivity()).putFirstRun(false);
            TapTargetSequence tapTargetSequence = new TapTargetSequence(getActivity());
            tapTargetSequence.continueOnCancel(true);
            tapTargetSequence.targets(
                    TapTarget.forToolbarMenuItem(mToolbar, R.id.action_search,
                            res.getString(R.string.tutorial_title_search))
                            .outerCircleColor(R.color.colorAccent)
                            .targetCircleColor(R.color.background_app)
                            .textColor(R.color.white),
                    TapTarget.forView(mGenreFilterButton,
                            res.getString(R.string.tutorial_title_genre))
                            .outerCircleColor(R.color.colorAccent)
                            .targetCircleColor(R.color.background_app)
                            .transparentTarget(true)
                            .textColor(R.color.white),
                    TapTarget.forView(mPositionFilterButton,
                            res.getString(R.string.tutorial_title_position))
                            .outerCircleColor(R.color.colorAccent)
                            .targetCircleColor(R.color.background_app)
                            .transparentTarget(true)
                            .textColor(R.color.white));
            tapTargetSequence.start();
        }

        return mRootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.fragment_home, menu);
        mSearchImageView = (ImageView) menu.findItem(R.id.action_search).getActionView();
    }
}
