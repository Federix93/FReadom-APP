package com.example.android.lab1.ui.homepage;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.lab1.R;
import com.example.android.lab1.adapter.RecyclerBookAdapter;
import com.example.android.lab1.adapter.RecyclerYourLibraryAdapter;
import com.example.android.lab1.adapter.ViewPagerAdapter;
import com.example.android.lab1.model.Book;
import com.example.android.lab1.ui.GenreBooksActivity;
import com.example.android.lab1.utils.Constants;
import com.example.android.lab1.utils.CustomSwipeRefresh;
import com.example.android.lab1.utils.FetchAddressIntentService;
import com.example.android.lab1.utils.SharedPreferencesManager;
import com.example.android.lab1.utils.Utilities;
import com.example.android.lab1.viewmodel.BooksViewModel;
import com.example.android.lab1.viewmodel.ViewModelFactory;
import com.example.android.lab1.viewmodel.YourLibraryViewModel;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class TabFragment extends Fragment {

    public static final String HOME_POSITION_LAT = "HOME_POSITION_LAT";
    public static final String HOME_POSITION_LON = "HOME_POSITION_LON";
    // HomeFragment variables
    private static final String HOME_FIRST_BOOKS = "HOME_FIRST_BOOKS";
    private static final String HOME_SECOND_BOOKS = "HOME_SECOND_BOOKS";
    private static final String HOME_CURRENT_CITY = "HOME_CURRENT_CITY";
    private static final String HOME_SELECTED_GENRE = "HOME_SELECTED_GENRE";
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
    BooksViewModel booksViewModel;
    //General variables
    FragmentManager mFt = null;
    View fragmentContainer;
    int mFragmentType;
    private TextView mTitleFirstRecyclerView;
    private RecyclerView mFirstRecyclerView;
    private RecyclerView mSecondRecyclerView;
    private LiveData<List<Book>> mFirstLiveData;
    private int mBookLimitHomepage;
    //LoanFragment variables
    //RequestsFragment variables
    private Integer mSelectedGenre;
    private GeoPoint mCurrentPosition;
    //YourLibraryFragment variables
    private RecyclerView mYourLibraryRecyclerView;
    private int mFirstRecyclerDistance;
    private int mSecondRecyclerDistance;
    private View mTitleSecondRecyclerView;
    private TextView mNoInfoPlaceholder;
    private View mNoInfoLayout;
    private LiveData<List<Book>> mSecondLiveData;

    public static TabFragment newInstance(int fragmentTab) {

        Bundle args = new Bundle();
        args.putInt("fragmentType", fragmentTab);
        TabFragment fragment = new TabFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public int getFirstRecyclerDistance() {
        return mFirstRecyclerDistance;
    }

    public int getBookLimitHomepage() {
        return mBookLimitHomepage;
    }

    public int getSecondRecyclerDistance() {
        return mSecondRecyclerDistance;
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
                initHomeFragment(view, savedInstanceState);
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
                initHomeFragment(view, savedInstanceState);
                return view;
        }

    }

    private void resolveCity() {
        Utilities.resolveSingleLocation(new WeakReference<Activity>(getActivity()),
                mCurrentPosition,
                true,
                new ResultReceiver(new Handler()) {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        super.onReceiveResult(resultCode, resultData);
                        if (resultCode == RESULT_OK) {
                            mPositionFilterButton.setText(resultData.getString(
                                    FetchAddressIntentService.Constants.RESULT
                            ));
                        }
                    }
                }
        );
    }

    private void initHomeFragment(View view, final Bundle savedInstanceState) {
        mRefreshLayout = view.findViewById(R.id.homepage_refresh_layout);
        mTitleFirstRecyclerView = view.findViewById(R.id.title_first_recycler_view);
        mFirstRecyclerView = view.findViewById(R.id.first_recycler_books);
        mTitleSecondRecyclerView = view.findViewById(R.id.title_second_recycler_view);
        mSecondRecyclerView = view.findViewById(R.id.second_recycler_books);
        mGenreFilterButton = view.findViewById(R.id.genre_filter_button);
        mPositionFilterButton = view.findViewById(R.id.position_filter_button);
        mFirstOtherTextView = view.findViewById(R.id.button_first_recycler_view);
        mSecondOtherTextView = view.findViewById(R.id.button_second_recycler_view);
        mNoInfoPlaceholder = view.findViewById(R.id.homepage_no_info_text_view);
        mBookLimitHomepage = getResources().getInteger(R.integer.homepage_book_number);
        mFirstRecyclerDistance = getResources().getInteger(R.integer.position_radius_address);
        mSecondRecyclerDistance = getResources().getInteger(R.integer.position_radius_address);
        mNoInfoLayout = view.findViewById(R.id.no_info_layout);

        mGenreFilterButton.setOnClickListener(new View.OnClickListener() {
                                                  @Override
                                                  public void onClick(View v) {
                                                      if (getActivity() != null) {
                                                          Intent i = new Intent(getActivity(), GenreBooksActivity.class);
                                                          i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                                          getActivity().startActivityForResult(i, Constants.PICK_GENRE);
                                                      }
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

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        //booksViewModel.refreshLayout(mCurrentPosition);
                        new RefreshAsyncTask(TabFragment.this).execute();
                    }
                });
            }
        });

        if (getActivity() != null &&
                SharedPreferencesManager.getInstance(getActivity()).isFirstRun()) {
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
            tapTargetSequence.listener(new TapTargetSequence.Listener() {
                @Override
                public void onSequenceFinish() {
                    updateUI(savedInstanceState);
                }

                @Override
                public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {

                }

                @Override
                public void onSequenceCanceled(TapTarget lastTarget) {

                }
            });
            tapTargetSequence.start();
        } else
            updateUI(savedInstanceState);

    }

    private void updateUI(Bundle savedInstanceState) {
        // method used to avoid redundancy

        if (savedInstanceState == null) {
            // fragment just created
            mCurrentPosition = SharedPreferencesManager.getInstance(getContext()).getPosition();
            if (mCurrentPosition == null)
                getUserPosition(); // try to solve it
            else if (getActivity() != null) {
                // resolve city name
                resolveCity();
                queryDatabaseWithViewModel();
            }
        } else {
            // config change restore previous state
            if (savedInstanceState.containsKey(HOME_POSITION_LON)
                    && savedInstanceState.containsKey(HOME_POSITION_LON))
                mCurrentPosition = new GeoPoint(
                        savedInstanceState.getDouble(HOME_POSITION_LAT),
                        savedInstanceState.getDouble(HOME_POSITION_LON));
            mSelectedGenre = savedInstanceState.getInt(HOME_SELECTED_GENRE);
            if (mSelectedGenre >= 0) {
                mGenreFilterButton.setText(getResources().getStringArray(R.array.genre)[mSelectedGenre]);
            } else
                mGenreFilterButton.setText(R.string.genre_filter_home_page);

            if (savedInstanceState.containsKey(HOME_CURRENT_CITY))
                mPositionFilterButton.setText(savedInstanceState.getString(HOME_CURRENT_CITY));


            // if position never has been resolved no books will be present
            if (SharedPreferencesManager.getInstance(getContext()).getPosition() != null) {
                if (savedInstanceState.containsKey(HOME_FIRST_BOOKS))
                    updateLayoutFirstRecyclerView(savedInstanceState.<Book>getParcelableArrayList(HOME_FIRST_BOOKS));
                else
                    updateLayoutFirstRecyclerView(null);

                if (savedInstanceState.containsKey(HOME_SECOND_BOOKS))
                    updateLayoutSecondRecyclerView(savedInstanceState.<Book>getParcelableArrayList(HOME_SECOND_BOOKS));
                else
                    updateLayoutSecondRecyclerView(null);
            } else {
                mGenreFilterButton.setVisibility(View.GONE);
                mTitleFirstRecyclerView.setVisibility(View.GONE);
                mFirstRecyclerView.setVisibility(View.GONE);
                mFirstOtherTextView.setVisibility(View.GONE);
                mSecondOtherTextView.setVisibility(View.GONE);
                mSecondRecyclerView.setVisibility(View.GONE);
                mTitleSecondRecyclerView.setVisibility(View.GONE);

                mNoInfoLayout.setVisibility(View.VISIBLE);
                mNoInfoPlaceholder.setText(R.string.homepage_select_position);
            }

        }
    }

    private void getUserPosition() {
        if (getActivity() != null &&
                Utilities.checkPermissionActivity(getActivity(),
                        android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            makePositionRequest();
        } else if (getActivity() != null) {
            Utilities.askPermissionActivity(getActivity(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    Constants.POSITION_PERMISSION);
        }
    }

    public GeoPoint getPosition() {
        return mCurrentPosition;
    }

    public void setPosition(GeoPoint value) {
        GeoPoint oldValue = mCurrentPosition;
        mCurrentPosition = value;

        // check if there's a variation in values and update homepage

        Double lat1, lat2, lon1, lon2;

        if (oldValue != null) {
            lat1 = oldValue.getLatitude();
            lon1 = oldValue.getLongitude();
        } else
            lat1 = lon1 = null;

        if (mCurrentPosition != null) {
            lat2 = mCurrentPosition.getLatitude();
            lon2 = mCurrentPosition.getLongitude();
        } else
            lat2 = lon2 = null;


        if (!Objects.equals(lat1, lat2) ||
                !Objects.equals(lon1, lon2))
            new RefreshAsyncTask(TabFragment.this).execute();
    }

    @SuppressLint("MissingPermission")
    private void makePositionRequest() {
        if (getActivity() != null) {
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
                                        // its called only first time
                                        final Location location = locationResult.getLocations().get(0);
                                        mCurrentPosition = new GeoPoint(location.getLatitude(),
                                                location.getLongitude());
                                        SharedPreferencesManager.getInstance(getActivity()).setPosition(
                                                mCurrentPosition);
                                        resolveCity();
                                        queryDatabaseWithViewModel();
                                        //resolveLocation(location);
                                    }
                                }, null);
            }
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
            YourLibraryViewModel yourLibraryViewModel = ViewModelProviders.of(this,
                    new ViewModelFactory(mFirebaseAuth.getUid())).get(YourLibraryViewModel.class);
            yourLibraryViewModel.getSnapshotLiveData()
                    .observe(this, new Observer<List<Book>>() {
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
            case Constants.PICK_GENRE:
                if (data != null
                        && data.hasExtra(GenreBooksActivity.SELECTED_GENRE)) {
                    int genre = data.getIntExtra(GenreBooksActivity.SELECTED_GENRE, -1);
                    setSelectedGenre(genre);
                }
                break;
            case Constants.PLAY_SERVICES_RESOLUTION_REQUEST:
                if (resultCode == RESULT_OK)
                    makePositionRequest();
                else if (resultCode == RESULT_CANCELED)
                    Utilities.enableLoc(getActivity());
                break;
            case Constants.ADDRESS_SEARCH_BAR:
                if (resultCode == RESULT_OK && getContext() != null) {
                    Place place = PlaceAutocomplete.getPlace(getContext(), data);
                    SharedPreferencesManager.getInstance(getContext()).setPosition(
                            new GeoPoint(place.getLatLng().latitude,
                                    place.getLatLng().longitude));
                    setPosition(new GeoPoint(place.getLatLng().latitude,
                            place.getLatLng().longitude));
                    mPositionFilterButton.setText(place.getName());
                } else {
                    // keep asking position
                    if (mCurrentPosition == null) {

                        mTitleFirstRecyclerView.setVisibility(View.GONE);
                        mFirstRecyclerView.setVisibility(View.GONE);
                        mTitleSecondRecyclerView.setVisibility(View.GONE);
                        mSecondRecyclerView.setVisibility(View.GONE);
                        mGenreFilterButton.setVisibility(View.GONE);
                        mFirstOtherTextView.setVisibility(View.GONE);
                        mSecondOtherTextView.setVisibility(View.GONE);

                        // show message
                        mNoInfoLayout.setVisibility(View.VISIBLE);
                        mNoInfoPlaceholder.setText(R.string.homepage_select_position);
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
            booksViewModel = ViewModelProviders.of(getActivity(), new ViewModelFactory(mCurrentPosition,
                    mFirstRecyclerDistance,
                    mSecondRecyclerDistance,
                    mBookLimitHomepage)).get(BooksViewModel.class);
            mFirstLiveData = booksViewModel.getBooksFirstRecycler();

            mFirstLiveData.observe(this, new Observer<List<Book>>() {
                @Override
                public void onChanged(@Nullable List<Book> books) {
                    updateLayoutFirstRecyclerView(books);
                    mFirstLiveData.removeObserver(this);
                }
            });

            mSecondLiveData = booksViewModel.getBooksSecondRecycler();

            mSecondLiveData.observe(this, new Observer<List<Book>>() {
                @Override
                public void onChanged(@Nullable List<Book> books) {
                    updateLayoutSecondRecyclerView(books);
                    mSecondLiveData.removeObserver(this);
                }
            });
        }
    }

    public void updateLayoutSecondRecyclerView(List<Book> books) {
        if (books != null && !books.isEmpty()) {
            mGenreFilterButton.setVisibility(View.VISIBLE);
            mSecondOtherTextView.setVisibility(View.VISIBLE);
            mTitleSecondRecyclerView.setVisibility(View.VISIBLE);
            mSecondRecyclerView.setVisibility(View.VISIBLE);
            mNoInfoLayout.setVisibility(View.GONE);
        } else {
            mSecondRecyclerView.setVisibility(View.GONE);
            mTitleSecondRecyclerView.setVisibility(View.GONE);
            mSecondOtherTextView.setVisibility(View.GONE);
            if (mFirstRecyclerView.getVisibility() == View.GONE) {
                // both recycler are empty
                // show placeholder
                mNoInfoLayout.setVisibility(View.VISIBLE);
                mNoInfoPlaceholder.setText(R.string.homepage_no_results);
                if (mSelectedGenre == null || mSelectedGenre < 0)
                    mGenreFilterButton.setVisibility(View.GONE);
            }

            books = new ArrayList<>();
        }

        if (mSecondRecyclerBookAdapter == null) {
            mSecondRecyclerBookAdapter = new RecyclerBookAdapter(books);
            mSecondRecyclerView.setAdapter(mSecondRecyclerBookAdapter);
        } else {
            final List<Book> old = mSecondRecyclerBookAdapter.getBooks();
            final List<Book> finalBooks = books;
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCallback(old, finalBooks));
            mSecondRecyclerBookAdapter.updateItems(books);
            diffResult.dispatchUpdatesTo(mSecondRecyclerBookAdapter);

            mSecondRecyclerView.smoothScrollToPosition(0);
        }

        if (mRefreshLayout.isRefreshing())
            mRefreshLayout.setRefreshing(false);
    }

    public void updateLayoutFirstRecyclerView(List<Book> books) {
        if (books != null && !books.isEmpty()) {
            mFirstOtherTextView.setVisibility(View.VISIBLE);
            mGenreFilterButton.setVisibility(View.VISIBLE);
            mFirstRecyclerView.setVisibility(View.VISIBLE);
            mTitleFirstRecyclerView.setVisibility(View.VISIBLE);
            mNoInfoLayout.setVisibility(View.GONE);
        } else {
            mFirstOtherTextView.setVisibility(View.GONE);
            mFirstRecyclerView.setVisibility(View.GONE);
            mTitleFirstRecyclerView.setVisibility(View.GONE);

            if (mSecondRecyclerView.getVisibility() == View.GONE) {
                mNoInfoLayout.setVisibility(View.VISIBLE);
                mNoInfoPlaceholder.setText(R.string.homepage_no_results);
                if (mSelectedGenre == null || mSelectedGenre < 0)
                    mGenreFilterButton.setVisibility(View.GONE);
            }
            books = new ArrayList<>(); // to avoid having dirty recyclers view
        }

        if (mFirstRecyclerBookAdapter == null) {
            mFirstRecyclerBookAdapter = new RecyclerBookAdapter(books);
            mFirstRecyclerView.setAdapter(mFirstRecyclerBookAdapter);
            mFirstRecyclerView.smoothScrollToPosition(0);

        } else {
            mFirstRecyclerBookAdapter.updateItems(books);
            mFirstRecyclerBookAdapter.notifyDataSetChanged();
        }
    }

    public Integer getSelectedGenre() {
        return mSelectedGenre;
    }

    public void setSelectedGenre(int selectedGenre) {
        Integer oldValue = mSelectedGenre;
        this.mSelectedGenre = selectedGenre;
        if (mSelectedGenre >= 0)
            mGenreFilterButton.setText(getResources().getStringArray(R.array.genre)[mSelectedGenre]);
        else {
            mGenreFilterButton.setText(R.string.genre_filter_home_page);
            mSelectedGenre = null;
        }

        if (!Objects.equals(oldValue, mSelectedGenre))
            new RefreshAsyncTask(TabFragment.this).execute();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mCurrentPosition != null) {
            outState.putDouble(HOME_POSITION_LAT, mCurrentPosition.getLatitude());
            outState.putDouble(HOME_POSITION_LON, mCurrentPosition.getLongitude());
        }
        outState.putInt(HOME_SELECTED_GENRE, mSelectedGenre != null &&
                mSelectedGenre >= 0 ? mSelectedGenre : -1);
        if (mFirstRecyclerBookAdapter != null) {
            List<Book> books = mFirstRecyclerBookAdapter.getBooks();
            if (books != null && !books.isEmpty())
                outState.putParcelableArrayList(HOME_FIRST_BOOKS, (ArrayList<? extends Parcelable>) books);
        }
        if (mSecondRecyclerBookAdapter != null) {
            List<Book> books = mSecondRecyclerBookAdapter.getBooks();
            if (books != null && !books.isEmpty())
                outState.putParcelableArrayList(HOME_SECOND_BOOKS, (ArrayList<? extends Parcelable>) books);
        }
        if (mPositionFilterButton != null)
            outState.putString(HOME_CURRENT_CITY, mPositionFilterButton.getText().toString());
    }

    private static class RefreshAsyncTask extends AsyncTask<Void, Void, Void> {

        private final WeakReference<TabFragment> mContainerRef;
        private final CountDownLatch countDownLatch = new CountDownLatch(2);
        private GeoPoint mCurrentPosition;
        private Integer mSelectedGenre;
        private Integer mFirstDistance;
        private Integer mSecondDistance;
        private Integer mBookLimit;
        private List<Book> mSecondBookList;
        private List<Book> mFirstBookList;


        public RefreshAsyncTask(TabFragment container) {
            mContainerRef = new WeakReference<>(container);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            TabFragment tabFragment = mContainerRef.get();
            if (tabFragment != null) {
                mCurrentPosition = tabFragment.getPosition();
                mSelectedGenre = tabFragment.getSelectedGenre();
                mFirstDistance = tabFragment.getFirstRecyclerDistance();
                mSecondDistance = tabFragment.getSecondRecyclerDistance();
                mBookLimit = tabFragment.getBookLimitHomepage();
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            TabFragment tabFragment = mContainerRef.get();
            if (tabFragment != null) {
                tabFragment.updateLayoutFirstRecyclerView(mFirstBookList);
                tabFragment.updateLayoutSecondRecyclerView(mSecondBookList);
                tabFragment.mRefreshLayout.setRefreshing(false);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (mCurrentPosition != null) {
                GeoPoint[] firstGeoPoints = Utilities.buildBoundingBox(mCurrentPosition.getLatitude(),
                        mCurrentPosition.getLongitude(),
                        (double) mFirstDistance);
                Query query1;
                if (mSelectedGenre == null || mSelectedGenre < 0)
                    query1 = FirebaseFirestore.getInstance()
                            .collection("books")
                            .whereGreaterThan("geoPoint", firstGeoPoints[0])
                            .whereLessThan("geoPoint", firstGeoPoints[1])
                            .whereEqualTo("lentTo", null);
                else
                    query1 = FirebaseFirestore.getInstance()
                            .collection("books")
                            .whereGreaterThan("geoPoint", firstGeoPoints[0])
                            .whereLessThan("geoPoint", firstGeoPoints[1])
                            .whereEqualTo("lentTo", null)
                            .whereEqualTo("genre", mSelectedGenre);

                query1.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots != null
                                && !queryDocumentSnapshots.isEmpty()) {
                            int valid = 0;
                            mFirstBookList = new ArrayList<>();
                            String uid = FirebaseAuth.getInstance().getUid();
                            for (Book book : queryDocumentSnapshots.toObjects(Book.class)) {
                                if (uid == null || !book.getUid().equals(uid)) {
                                    mFirstBookList.add(book);
                                    valid++;
                                }
                                if (valid > mBookLimit)
                                    break;
                            }
                        }
                        countDownLatch.countDown();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        countDownLatch.countDown();
                    }
                });

                GeoPoint[] secondGeoPoints = Utilities.buildBoundingBox(mCurrentPosition.getLatitude(),
                        mCurrentPosition.getLongitude(),
                        (double) mSecondDistance);

                Query query2;
                if (mSelectedGenre == null || mSelectedGenre < 0)
                    query2 = FirebaseFirestore.getInstance()
                            .collection("books")
                            .whereGreaterThan("geoPoint", secondGeoPoints[0])
                            .whereLessThan("geoPoint", secondGeoPoints[1])
                            .whereEqualTo("lentTo", null);
                else
                    query2 = FirebaseFirestore.getInstance()
                            .collection("books")
                            .whereGreaterThan("geoPoint", secondGeoPoints[0])
                            .whereLessThan("geoPoint", secondGeoPoints[1])
                            .whereEqualTo("lentTo", null)
                            .whereEqualTo("genre", mSelectedGenre);

                query2.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots != null
                                && !queryDocumentSnapshots.isEmpty()) {
                            int valid = 0;
                            mSecondBookList = new ArrayList<>();
                            String uid = FirebaseAuth.getInstance().getUid();
                            for (Book book : queryDocumentSnapshots.toObjects(Book.class)) {
                                if (uid == null || !book.getUid().equals(uid)) {
                                    mSecondBookList.add(book);
                                    valid++;
                                }
                                if (valid > mBookLimit)
                                    break;
                            }
                            Book temp;
                            for (int i = 0; i < mSecondBookList.size() - 1; i++) {
                                for (int i1 = i + 1; i1 < mSecondBookList.size(); i1++) {
                                    if (mSecondBookList.get(i).getTimeInserted().getTime() <
                                            mSecondBookList.get(i1).getTimeInserted().getTime()) {
                                        temp = mSecondBookList.get(i);
                                        mSecondBookList.set(i, mSecondBookList.get(i1));
                                        mSecondBookList.set(i1, temp);
                                    }
                                }
                            }
                        }
                        countDownLatch.countDown();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        countDownLatch.countDown();
                    }
                });
                try {
                    countDownLatch.await();
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

    }

    private class DiffCallback extends DiffUtil.Callback {
        private List<Book> old;
        private List<Book> finalBooks;

        public DiffCallback(List<Book> old, List<Book> finalBooks) {
            this.old = old;
            this.finalBooks = finalBooks;
        }

        @Override
        public int getOldListSize() {
            return old.size();
        }

        @Override
        public int getNewListSize() {
            return finalBooks.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return old.get(oldItemPosition) == finalBooks.get(newItemPosition);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Book oldBook = old.get(oldItemPosition);
            Book newBook = finalBooks.get(newItemPosition);
            return oldBook.getTitle().equals(newBook.getTitle()) &&
                    oldBook.getAuthors().equals(newBook.getAuthors()) &&
                    oldBook.getUid().equals(newBook.getUid());
        }
    }
}





