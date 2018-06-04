package com.example.android.lab1.ui.homepage;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.algolia.search.saas.Client;
import com.algolia.search.saas.Index;
import com.example.android.lab1.R;
import com.example.android.lab1.adapter.RecyclerBookAdapter;
import com.example.android.lab1.adapter.RecyclerYourLibraryAdapter;
import com.example.android.lab1.adapter.ViewPagerAdapter;
import com.example.android.lab1.model.Book;
import com.example.android.lab1.ui.GenreBooksActivity;
import com.example.android.lab1.utils.Constants;
import com.example.android.lab1.utils.CustomSwipeRefresh;
import com.example.android.lab1.utils.SharedPreferencesManager;
import com.example.android.lab1.utils.Utilities;
import com.example.android.lab1.viewmodel.BooksViewModel;
import com.example.android.lab1.viewmodel.ViewModelFactory;
import com.firebase.ui.auth.ui.ProgressDialogHolder;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class TabFragment extends Fragment {

    // HomeFragment variables
    private final static String ALGOLIA_APP_ID = "2TZTD61TRP";
    private final static String ALGOLIA_SEARCH_API_KEY = "e78db865fd37a6880ec1c3f6ccef046a";
    private final static String ALGOLIA_BOOKS_INDEX_NAME = "books";
    private final static String ALGOLIA_USERS_INDEX_NAME = "users";

    private static final String LAT = "LAT";
    private static final String LON = "LAT";
    private static final String SELECTED_GENRE = "SG";
    private static final String CITY = "CITY";
    private static final String GENRES = "GENRES";
    private static final String GENRE_PLACE_HOLDER = "GENRE_PLACE_HOLDER";
    private static final int HITS = 30;

    CustomSwipeRefresh mRefreshLayout;
    AppCompatButton mGenreFilterButton;
    AppCompatButton mPositionFilterButton;
    TextView mYourLibraryAdvice;
    TextView mFirstOtherTextView;
    TextView mSecondOtherTextView;
    ImageView mSearchImageView;
    FirebaseFirestore mFirebaseFirestore;
    RecyclerYourLibraryAdapter mAdapter;
    RecyclerBookAdapter mFirstRecyclerBookAdapter;
    RecyclerBookAdapter mSecondRecyclerBookAdapter;
    List<Book> mBookIds;
    List<Book> mBookListHomeFragment = new ArrayList<>();
    //General variables
    FragmentManager mFt = null;
    View fragmentContainer;
    int mFragmentType;
    private Index books;
    private Index users;
    private Bundle savedState = null;
    private RecyclerView mFirstRecyclerView;
    private RecyclerView mSecondRecyclerView;
    private LiveData<List<Book>> firstLiveData;
    BooksViewModel booksViewModel;
    private ListenerRegistration firstListener;
    private ListenerRegistration secondListener;

    //LoanFragment variables

    //RequestsFragment variables
    private Integer mSelectedGenre;
    private GeoPoint mCurrentPosition;
    //YourLibraryFragment variables
    private RecyclerView mYourLibraryRecyclerView;

    public static TabFragment newInstance(int fragmentTab) {

        Bundle args = new Bundle();
        args.putInt("fragmentType", fragmentTab);
        TabFragment fragment = new TabFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        int fragmentType = getArguments().getInt("fragmentType");
        View view;
        mFragmentType = fragmentType;

        switch (fragmentType) {
            case 0:
                view = inflater.inflate(R.layout.fragment_home, container, false);
                initHomeFragment(view);
                return view;

            case 1:
                view = inflater.inflate(R.layout.fragment_your_library, container, false);
                initYourLibraryFragment(view);
                return view;

            case 2:
                view = inflater.inflate(R.layout.fragment_loans_and_requests, container, false);
                initLoanFragment(view);
                return view;

            case 3:
                view = inflater.inflate(R.layout.fragment_loans_and_requests, container, false);
                initRequestsFragment(view);
                return view;

            default:
                view = inflater.inflate(R.layout.fragment_home, container, false);
                initHomeFragment(view);
                return view;
        }

    }

    private void initHomeFragment(View view) {

        mRefreshLayout = view.findViewById(R.id.homepage_refresh_layout);
        mFirstRecyclerView = view.findViewById(R.id.first_recycler_books);
        mSecondRecyclerView = view.findViewById(R.id.second_recycler_books);
        mGenreFilterButton = view.findViewById(R.id.genre_filter_button);
        mPositionFilterButton = view.findViewById(R.id.position_filter_button);
        mFirstOtherTextView = view.findViewById(R.id.button_first_recycler_view);
        mSecondOtherTextView = view.findViewById(R.id.button_second_recycler_view);

        Client client = new Client(ALGOLIA_APP_ID, ALGOLIA_SEARCH_API_KEY);
        books = client.getIndex(ALGOLIA_BOOKS_INDEX_NAME);
        users = client.getIndex(ALGOLIA_USERS_INDEX_NAME);

        mGenreFilterButton.setOnClickListener(new View.OnClickListener() {
                                                  @Override
                                                  public void onClick(View v) {
                                                      Intent i = new Intent(getActivity(), GenreBooksActivity.class);
                                                      i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                                      getActivity().startActivityForResult(i, Constants.PICK_GENRE);
                                                  }
                                              }
        );

        mPositionFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Utilities.getGoogleAddressSearchBar(getActivity(),
                            mCurrentPosition == null ? null : new LatLng(mCurrentPosition.getLatitude(),
                                    mCurrentPosition.getLongitude()),
                            true,
                            Constants.ADDRESS_SEARCH_BAR);
                } catch (GooglePlayServicesNotAvailableException | GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                }
            }
        });

        mFirstOtherTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent moreIntent = new Intent(getActivity(), MoreActivity.class);
                moreIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                moreIntent.putExtra("latitude", mCurrentPosition.getLatitude());
                moreIntent.putExtra("longitude", mCurrentPosition.getLongitude());
                startActivity(moreIntent);
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

        getUserPosition();

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        //booksViewModel.refreshLayout(mCurrentPosition);
                        new RefreshAsyncTask().execute();
                    }
                });
            }
        });

        if (SharedPreferencesManager.getInstance(getActivity()).isFirstRun()) {
            Resources res = getResources();
            SharedPreferencesManager.getInstance(getActivity()).putFirstRun(false);
            TapTargetSequence tapTargetSequence = new TapTargetSequence(getActivity());
            tapTargetSequence.continueOnCancel(true);
            tapTargetSequence.targets(
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

    }

    private void getUserPosition() {
        if (Utilities.checkPermissionActivity(getActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            makePositionRequest();
        } else {
            Utilities.askPermissionActivity(getActivity(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    Constants.POSITION_PERMISSION);
        }
    }

    public GeoPoint getPosition() {
        return mCurrentPosition;
    }

    public void setPosition(GeoPoint value) {
        mCurrentPosition = value;
    }

    @SuppressLint("MissingPermission")
    private void makePositionRequest() {
        if (!Utilities.isLocationEnabled(getContext()))
            Utilities.enableLoc(getActivity());
        else {
            LocationServices.getFusedLocationProviderClient(getActivity())
                    .requestLocationUpdates(LocationRequest.create(),
                            new LocationCallback() {
                                @Override
                                public void onLocationResult(LocationResult locationResult) {
                                    if (locationResult == null || locationResult.getLocations().size() == 0) {
                                        return;
                                    }
                                    final Location location = locationResult.getLocations().get(0);
                                    mCurrentPosition = new GeoPoint(location.getLatitude(),
                                            location.getLongitude());
                                    mPositionFilterButton.setText(R.string.current_position);
                                    queryDatabaseWithViewModel();
                                    //resolveLocation(location);
                                }
                            }, null);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makePositionRequest();
            } else {
                try {
                    Utilities.getGoogleAddressSearchBar(getActivity(),
                            mCurrentPosition != null ? new LatLng(mCurrentPosition.getLatitude(),
                                    mCurrentPosition.getLongitude()) : null,
                            true,
                            Constants.ADDRESS_SEARCH_BAR);
                } catch (GooglePlayServicesNotAvailableException | GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void initYourLibraryFragment(final View view) {

        fragmentContainer = view.findViewById(R.id.fragment_container);
        mYourLibraryRecyclerView = view.findViewById(R.id.rv_fragment_books_library);
        mYourLibraryAdvice = view.findViewById(R.id.your_library_advice);

        final FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();

        if (mFirebaseAuth.getUid() != null) {
            BooksViewModel booksViewModel = ViewModelProviders.of(this, new ViewModelFactory(mFirebaseAuth.getUid())).get(BooksViewModel.class);
            booksViewModel.getSnapshotLiveData().observe(this, new Observer<List<Book>>() {
                @Override
                public void onChanged(@Nullable List<Book> books) {
                    if (books != null) {
                        updateListOfBooks(books);
                        if (mYourLibraryAdvice.getVisibility() == View.VISIBLE)
                            mYourLibraryAdvice.setVisibility(View.GONE);
                    } else {
                        mYourLibraryAdvice.setVisibility(View.VISIBLE);
                    }
                }
            });
        } else {
            mYourLibraryAdvice.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.PLAY_SERVICES_RESOLUTION_REQUEST:
                if (resultCode == RESULT_OK)
                    makePositionRequest();
                break;
            case Constants.ADDRESS_SEARCH_BAR:
                if (resultCode == RESULT_OK) {
                    Place place = PlaceAutocomplete.getPlace(getContext(), data);
                    mCurrentPosition = new GeoPoint(place.getLatLng().latitude, place.getLatLng().longitude);
                    mPositionFilterButton.setText(place.getName());
                    new RefreshAsyncTask().execute();
                } else {
                    // keep asking position
                    if (mCurrentPosition == null) {
                        try {
                            Utilities.getGoogleAddressSearchBar(getActivity(),
                                    mCurrentPosition != null ? new LatLng(mCurrentPosition.getLatitude(),
                                            mCurrentPosition.getLongitude()) : null,
                                    true,
                                    Constants.ADDRESS_SEARCH_BAR);
                        } catch (GooglePlayServicesNotAvailableException | GooglePlayServicesRepairableException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
        }
    }

    private void updateListOfBooks(List<Book> books) {
        RecyclerView.LayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);

        mYourLibraryRecyclerView.setHasFixedSize(true);
        mYourLibraryRecyclerView.setLayoutManager(layoutManager);
        mYourLibraryRecyclerView.setNestedScrollingEnabled(true);

        mAdapter = new RecyclerYourLibraryAdapter(books);
        mYourLibraryRecyclerView.setAdapter(mAdapter);
    }

    private void initLoanFragment(View view) {

        fragmentContainer = view.findViewById(R.id.main_content);
        mFt = getChildFragmentManager();

        ViewPager viewPager = view.findViewById(R.id.viewpager);
        setupLoanViewPager(viewPager);

        // Give the TabLayout the ViewPager
        TabLayout tabs = view.findViewById(R.id.tabs);
        tabs.addTab(tabs.newTab());
        tabs.addTab(tabs.newTab());

        tabs.setupWithViewPager(viewPager);
    }

    private void setupLoanViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(mFt);
        adapter.addFragment(new LentFragmentItem(), getResources().getString(R.string.loan_your_item));
        adapter.addFragment(new BorrowedFragmentItem(), getResources().getString(R.string.loan_other_book_item));
        viewPager.setAdapter(adapter);
    }

    private void initRequestsFragment(View view) {

        fragmentContainer = view.findViewById(R.id.main_content);
        mFt = getChildFragmentManager();

        ViewPager viewPager = view.findViewById(R.id.viewpager);
        setupRequestsViewPager(viewPager);

        // Give the TabLayout the ViewPager
        TabLayout tabs = view.findViewById(R.id.tabs);
        tabs.addTab(tabs.newTab());
        tabs.addTab(tabs.newTab());

        tabs.setupWithViewPager(viewPager);

    }

    private void setupRequestsViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(mFt);
        adapter.addFragment(new RequestsFragmentDoneItem(), getResources().getString(R.string.request_done_item));
        adapter.addFragment(new RequestsFragmentReceivedItem(), getResources().getString(R.string.request_received_item));
        viewPager.setAdapter(adapter);
    }

    public void refresh() {
        if (mFirstRecyclerView != null) {
            mFirstRecyclerView.smoothScrollToPosition(0);
            mSecondRecyclerView.smoothScrollToPosition(0);
        }
        if (mYourLibraryRecyclerView != null) {
            mYourLibraryRecyclerView.smoothScrollToPosition(0);
        }
    }

    public void willBeDisplayed() {
        // Do what you want here, for example animate the content
        if (fragmentContainer != null) {
            Animation fadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
            fragmentContainer.startAnimation(fadeIn);
        }
    }

    public void willBeHidden() {
        if (fragmentContainer != null) {
            Animation fadeOut = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);
            fragmentContainer.startAnimation(fadeOut);
        }
    }

    public int getFragmentType() {
        return mFragmentType;
    }

    private void queryDatabaseWithViewModel() {
        if (getActivity() != null) {
            booksViewModel = ViewModelProviders.of(getActivity(), new ViewModelFactory(mCurrentPosition)).get(BooksViewModel.class);
            firstLiveData = booksViewModel.getBooksFirstRecycler();

            firstLiveData.observe(getActivity(), new Observer<List<Book>>() {
                @Override
                public void onChanged(@Nullable List<Book> books) {
                    if (books != null)
                        updateLayoutFirstRecyclerView(books);
                    firstLiveData.removeObserver(this);
                }
            });

            final LiveData<List<Book>> secondLiveData = booksViewModel.getBooksSecondRecycler();

            secondLiveData.observe(getActivity(), new Observer<List<Book>>() {
                @Override
                public void onChanged(@Nullable List<Book> books) {
                    if (books != null) {
                        updateLayoutSecondRecyclerView(books);
                    }
                    secondLiveData.removeObserver(this);
                }
            });
        }
    }

    private class RefreshAsyncTask extends AsyncTask<Void, Void, Void> {


        @Override
        protected Void doInBackground(Void... params) {

            GeoPoint[] firstGeoPoints = Utilities.buildBoundingBox(mCurrentPosition.getLatitude(),
                    mCurrentPosition.getLongitude(),
                    (double) 15000);
            Query query1 = FirebaseFirestore.getInstance().collection("books").whereGreaterThan("geoPoint", firstGeoPoints[0])
                    .whereLessThan("geoPoint", firstGeoPoints[1]).limit(30);
            //liveFirstRecyclerView = new FirebaseQueryLiveDataFirestore(BOOK_REF_1);
            query1.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    if (queryDocumentSnapshots != null)
                        updateLayoutFirstRecyclerView(queryDocumentSnapshots.toObjects(Book.class));
                }
            });
            GeoPoint[] secondGeoPoints = Utilities.buildBoundingBox(mCurrentPosition.getLatitude(),
                    mCurrentPosition.getLongitude(),
                    (double) 60000);
            Query query2 = FirebaseFirestore.getInstance().collection("books").whereGreaterThan("geoPoint", secondGeoPoints[0])
                    .whereLessThan("geoPoint", secondGeoPoints[1]).limit(30);
            query2.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    if (queryDocumentSnapshots != null) {
                        updateLayoutSecondRecyclerView(queryDocumentSnapshots.toObjects(Book.class));
                    }
                }
            });
            return null;
        }

    }

    public void updateLayoutSecondRecyclerView(List<Book> books){

        if (mSecondRecyclerBookAdapter == null) {
            mSecondRecyclerBookAdapter = new RecyclerBookAdapter(books);
            mSecondRecyclerView.setAdapter(mSecondRecyclerBookAdapter);
        } else {
            mSecondRecyclerBookAdapter.updateItems(books);
            mSecondRecyclerBookAdapter.notifyDataSetChanged();
            if(mRefreshLayout.isRefreshing())
                mRefreshLayout.setRefreshing(false);
            mSecondRecyclerView.smoothScrollToPosition(0);
        }
    }
    public void updateLayoutFirstRecyclerView(List<Book> books){
        if (mFirstRecyclerBookAdapter == null) {
            mFirstRecyclerBookAdapter = new RecyclerBookAdapter(books);
            mFirstRecyclerView.setAdapter(mFirstRecyclerBookAdapter);
            mFirstRecyclerView.smoothScrollToPosition(0);

        } else {
            mFirstRecyclerBookAdapter.updateItems(books);
            mFirstRecyclerBookAdapter.notifyDataSetChanged();
        }
    }
}





