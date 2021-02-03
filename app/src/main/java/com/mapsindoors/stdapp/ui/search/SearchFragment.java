package com.mapsindoors.stdapp.ui.search;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.google.android.gms.maps.model.LatLng;
import com.mapsindoors.mapssdk.BuildingCollection;
import com.mapsindoors.mapssdk.MPFilter;
import com.mapsindoors.mapssdk.MPLocation;
import com.mapsindoors.mapssdk.MPQuery;
import com.mapsindoors.mapssdk.MapControl;
import com.mapsindoors.mapssdk.MapsIndoors;
import com.mapsindoors.mapssdk.OnStateChangedListener;
import com.mapsindoors.mapssdk.Point;
import com.mapsindoors.mapssdk.PositionProvider;
import com.mapsindoors.mapssdk.PositionResult;
import com.mapsindoors.mapssdk.Venue;
import com.mapsindoors.mapssdk.VenueCollection;
import com.mapsindoors.mapssdk.dbglog;
import com.mapsindoors.mapssdk.errors.MIError;
import com.mapsindoors.stdapp.BuildConfig;
import com.mapsindoors.stdapp.R;
import com.mapsindoors.stdapp.apis.googleplaces.GooglePlacesClient;
import com.mapsindoors.stdapp.apis.googleplaces.PlacesServiceStatus;
import com.mapsindoors.stdapp.apis.googleplaces.listeners.AutoCompleteSuggestionListener;
import com.mapsindoors.stdapp.apis.googleplaces.listeners.GeoCodeResultListener;
import com.mapsindoors.stdapp.apis.googleplaces.models.AutoCompleteField;
import com.mapsindoors.stdapp.apis.googleplaces.models.GeoCodeResult;
import com.mapsindoors.stdapp.apis.googleplaces.models.GeocodeResults;
import com.mapsindoors.stdapp.helpers.MapsIndoorsRouteHelper;
import com.mapsindoors.stdapp.helpers.MapsIndoorsSettings;
import com.mapsindoors.stdapp.helpers.MapsIndoorsUtils;
import com.mapsindoors.stdapp.listeners.GenericObjectResultCallback;
import com.mapsindoors.stdapp.listeners.SearchResultSelectedListener;
import com.mapsindoors.stdapp.managers.AppConfigManager;
import com.mapsindoors.stdapp.managers.GoogleAnalyticsManager;
import com.mapsindoors.stdapp.ui.activitymain.MapsIndoorsActivity;
import com.mapsindoors.stdapp.ui.common.adapters.IconTextListAdapter;
import com.mapsindoors.stdapp.ui.common.fragments.BaseFragment;
import com.mapsindoors.stdapp.ui.common.enums.MenuFrame;
import com.mapsindoors.stdapp.ui.common.models.IconTextElement;
import com.mapsindoors.stdapp.ui.common.models.SearchResultItem;
import com.mapsindoors.stdapp.ui.components.noInternetBar.NoInternetBar;
import com.mapsindoors.stdapp.ui.direction.models.RoutingEndPoint;
import com.mapsindoors.stdapp.ui.menumain.MenuFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class SearchFragment extends BaseFragment {
    static final String TAG = SearchFragment.class.getSimpleName();

    //
    public static final int CATEG_MENU_SEARCH_TYPE = 0;
    public static final int POI_MENU_SEARCH_TYPE = 1;
    public static final int DIRECTION_SEARCH_TYPE = 2; // Origin + destination
    public static final int SEARCH_TYPE_COUNT = 4;

    //
    public static final int DIRECTION_ORIGIN_SEARCH = 0;
    public static final int DIRECTION_DESTINATION_SEARCH = 1;


    private static final int FLIPPER_LIST_ITEMS = 0;
    private static final int FLIPPER_LIST_PROGRESS = 1;
    private static final int FLIPPER_LIST_NO_RESULTS = 3;
    private static final int FLIPPER_LIST_CATEG_SEARCH_MESS = 2;

    static final int SEARCH_INPUT_MIN_STRING_LENGTH_TO_TRIGGER_QUERY = 2;

    private int mCurrentSearchType;
    private int mCurrentDirectionSearchType;

    private String categStartPoint = "startpoint";

    private MapsIndoorsActivity mActivity;
    private MapControl mMapControl;

    private Handler searchHandler;

    private SearchResultSelectedListener mLocationFoundListener;


    private EditText mSearchEditTextView;
    private ImageButton mSearchClearBtn;
    private IconTextListAdapter myAdapter;
    private ViewFlipper mViewFlipper;
    private TextView mNoResultsTextView;
    private TextView mSearchTextLabel;

    private ListView mSearchMenuList;
    private ImageButton mBackButton;


    private GooglePlacesClient mGooglePlacesClient;
    private AutoCompleteField mAutoCField;

    private boolean mIsMenuCleared = false;
    private View mPoweredByGoogleImageView;
    private int mLastSearchResultCount;
    private TextView messageTextView;

    private String mCategFilter;
    private String mSearchHint;

    public boolean mOpenedFromBackPress = false;

    private String[] mLastSearchText;
    private List<IconTextElement>[] mLastSearchResults;

    private NoInternetBar noInternetLayout;


    //region Fragment lifecycle events
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mMainView == null) {
            mMainView = inflater.inflate(R.layout.fragment_search, container);
        }
        return mMainView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewFlipper = view.findViewById(R.id.directionsfullmenu_itemlist_viewflipper);
        mSearchMenuList = view.findViewById(R.id.directionsfullmenu_itemlist);
        mNoResultsTextView = view.findViewById(R.id.control_noresults_text);
        messageTextView = view.findViewById(R.id.control_message_text);
        mSearchTextLabel = view.findViewById(R.id.search_text_label);

        // Search box text
        mSearchEditTextView = view.findViewById(R.id.search_fragment_edittext_search);

        // Clear search button
        mSearchClearBtn = view.findViewById(R.id.directionsfullmenu_search_clear_btn);

        mPoweredByGoogleImageView = view.findViewById(R.id.powered_by_image_view);

        mBackButton = view.findViewById(R.id.directionsfullmenusearch_back_button);

        noInternetLayout = view.findViewById(R.id.search_frag_no_connection_layout);

        noInternetLayout.setOnClickListener(v -> {
            noInternetLayout.setState(NoInternetBar.REFRESHING_STATE);
            searchRunner.run();
        });

        //
        final String messageText = String.format(
                getResources().getString(R.string.search_explaining_text),
                getResources().getString(R.string.app_provider_name_in_search)
        );
        messageTextView.setText(messageText);
        mFragment = MenuFrame.MENU_FRAME_SEARCH;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!mIsMenuCleared) {
            clearSearchResultList();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mActivity != null) {
            final PositionProvider positionProvider = mActivity.getCurrentPositionProvider();
            if (positionProvider != null) {
                positionProvider.removeOnStateChangedListener(onPositionProviderStateChangedListener);
            }
        }

        mActivity = null;
        mMapControl = null;
    }
    //endregion


    public void init(final Context context, final MapControl mapControl) {
        mActivity = (MapsIndoorsActivity) context;
        mMapControl = mapControl;

        mMainView.setVisibility(View.GONE);

        mSearchMenuList.setAdapter(myAdapter);
        mSearchMenuList.setClickable(true);
        mSearchMenuList.setOnItemClickListener(mAdapterViewOnItemClickListener);
        mSearchMenuList.invalidate();

        setupListView(context);

        //Note: Creating a textwatcher as it's needed for software keyboard support.
        mSearchEditTextView.addTextChangedListener(mEditTextViewTextWatcher);
        mSearchEditTextView.setOnFocusChangeListener(mEditTextViewOnFocusChangeListener);

        //Close keyboard and search when user presses search on the keyboard:
        mSearchEditTextView.setOnEditorActionListener(mEditTextViewOnEditorActionListener);

        //Close keyboard and search when user presses enter:
        mSearchEditTextView.setOnKeyListener(mEditTextOnKeyListener);

        // Clear search button
        mSearchClearBtn.setOnClickListener(mClearSearchButtonClickListener);
        mSearchClearBtn.setOnFocusChangeListener(mClearSearchButtonFocusChangeListener);

        mBackButton.setOnClickListener(mBackButtonOnClickListener);


        // Prepare the query to get the requested location

        // Setup the Google Map APIs used
        mGooglePlacesClient = new GooglePlacesClient();

        // Register the GPS broadcast receiver
        mIsMenuCleared = true;

        final PositionProvider positionProvider = mActivity.getCurrentPositionProvider();

        if (positionProvider != null) {
            positionProvider.addOnStateChangedListener(onPositionProviderStateChangedListener);
        }

        mCurrentSearchType = CATEG_MENU_SEARCH_TYPE;
        mCurrentDirectionSearchType = DIRECTION_ORIGIN_SEARCH;
        mLastSearchText = new String[SEARCH_TYPE_COUNT];
        mLastSearchResults = (ArrayList<IconTextElement>[]) new ArrayList[SEARCH_TYPE_COUNT];
    }


    //region Edit Text field

    /**
     *
     */
    TextWatcher mEditTextViewTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String text = mSearchEditTextView.getText().toString();

            if (!text.isEmpty()) {
                setSearchClearBtnActive(true);

                if (text.charAt(0) == ' ') {
                    mSearchEditTextView.setText(text.trim());
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            // Only start searching if the user wrote something to look for
            if (!TextUtils.isEmpty(s)) {
                mIsMenuCleared = false;

                startSearchTimer();
            } else {
                // No relevant search is being conducted, so clear selection in map
                mActivity.runOnUiThread(() -> {
                    if (mActivity.getSelectionManager() != null) {
                        mActivity.getSelectionManager().clearSelection();
                    }
                });
            }
        }
    };

    /**
     *
     */
    private View.OnFocusChangeListener mEditTextViewOnFocusChangeListener = (view, hasFocus) -> {
        if (hasFocus) {
            //mSearchEditTextView.getText().clear();
            final Editable text = mSearchEditTextView.getText();
            if (!TextUtils.isEmpty(text)) {
                mSearchEditTextView.setSelection(text.length());
            }

            openKeyboard();
        }
    };

    private final View.OnClickListener mBackButtonOnClickListener = view -> {
        close(mActivity);
        closeKeyboard();
    };

    /**
     * Close keyboard and search when user presses search on the keyboard
     */
    private TextView.OnEditorActionListener mEditTextViewOnEditorActionListener = (view, actionId, event) -> {
        boolean handled = false;
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            closeKeyboard();
            handled = true;
        }
        return handled;
    };

    /**
     * Close keyboard and search when user presses enter
     */
    private View.OnKeyListener mEditTextOnKeyListener = (view, keyCode, keyEvent) -> {

        if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_ENTER: {
                    closeKeyboard();
                    return true;
                }
                case KeyEvent.KEYCODE_BACK: {
                    break;
                }
                default:
                    startSearchTimer();
                    break;
            }
        }
        return false;
    };

    private void setFocusOnSearchBox() {
        mSearchEditTextView.post(() -> {
            final Activity activity = getActivity();
            if (activity != null) {
                mSearchEditTextView.requestFocusFromTouch();

                final InputMethodManager lManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (lManager != null) {
                    lManager.showSoftInput(mSearchEditTextView, 0);
                }
            }
        });
    }
    //endregion


    public void setSearchClearBtnActive(boolean exitActive) {
        mSearchClearBtn.setVisibility(exitActive ? View.VISIBLE : View.INVISIBLE);
    }


    //region Clear Search button
    final View.OnClickListener mClearSearchButtonClickListener = view -> {
        String textB4Clear = mSearchEditTextView.getText().toString();

        runClearSearchButtonClickAction();

        // Report this event
        {
            final Bundle eventParams = new Bundle();
            eventParams.putString("Query", textB4Clear);
            eventParams.putLong("Result_Count", mLastSearchResultCount);

            GoogleAnalyticsManager.reportEvent(getString(R.string.fir_event_search_dismissed), eventParams);
        }

        // ...
        mLastSearchResultCount = 0;
    };

    final View.OnFocusChangeListener mClearSearchButtonFocusChangeListener = (view, hasFocus) -> {
        if (hasFocus) {
            //Exit button pressed. Close the keyboard and go back to default - (viewing types)
            runClearSearchButtonClickAction();
        }
    };

    /**
     * Exit button pressed. Close the keyboard and go back to default - (viewing types)
     */
    void runClearSearchButtonClickAction() {
        resetSearchBox();
        setFocusOnSearchBox();
        //clearSearchResultList();
        startClearListTimer();
    }

    public void resetSearchBox() {
        clearSearchEditTextBox();
        setSearchClearBtnActive(false);
    }
    //endregion


    void closeKeyboard() {
        if (mActivity != null) {
            InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(mMainView.getWindowToken(), 0);
            }
        }
    }

    void openKeyboard() {
        if (mActivity != null) {
            InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
            }
        }
    }

    // Only search after a second of delay. Any search requests before one sec should replace the seach and restart the timer.
    void startSearchTimer() {
        if (searchHandler != null) {
            searchHandler.removeCallbacks(searchRunner);
            searchHandler.removeCallbacks(clearRunner);
        }

        searchHandler = new Handler();
        searchHandler.postDelayed(searchRunner, 1000);
    }

    void startClearListTimer() {
        if (searchHandler != null) {
            searchHandler.removeCallbacks(searchRunner);
            searchHandler.removeCallbacks(clearRunner);

        }
        changeWaitStatus(FLIPPER_LIST_PROGRESS, true);
        searchHandler = new Handler();
        searchHandler.postDelayed(clearRunner, 1000);
    }

    /**
     * Non-empty search string
     */
    String mCSearchString;


    boolean canStringTriggerSearchQuery(@NonNull String queryString) {
        final String qString = queryString.trim();
        return !qString.isEmpty() && qString.length() >= SEARCH_INPUT_MIN_STRING_LENGTH_TO_TRIGGER_QUERY;
    }

    final Runnable searchRunner = new Runnable() {
        @Override
        public void run() {
            final String qString = mSearchEditTextView.getText().toString();

            if (canStringTriggerSearchQuery(qString)) {
                mCSearchString = qString.trim();
                setLastSearchText(mCSearchString);

                // Show as busy
                changeWaitStatus(FLIPPER_LIST_PROGRESS, true);

                if (dbglog.isDeveloperMode()) {
                    dbglog.Log(TAG, "Search for: " + mCSearchString);
                }

                final Venue currentVenue = MapsIndoors.getVenues().getCurrentVenue();
                final Point venuePos = (currentVenue != null) ? currentVenue.getPosition() : null;
                final Point userPos = mActivity.getCurrentUserPos();

                final MPQuery.Builder queryBuilder = new MPQuery.Builder();

                queryBuilder.setNear((userPos != null) ? userPos.getLatLng() : ((venuePos != null) ? venuePos.getLatLng() : null));

                final MPFilter.Builder filterBuilder = new MPFilter.Builder();

                if (mCurrentSearchType == POI_MENU_SEARCH_TYPE) {
                    filterBuilder.setCategories(Collections.singletonList(mCategFilter));
                } else {
                    filterBuilder.setCategories(null);
                }

                // Indoor locations - setup the search query
                queryBuilder.setQuery(mCSearchString);

                final MPQuery q = queryBuilder.build();
                final MPFilter f = filterBuilder.build();

                MapsIndoors.getLocationsAsync(q, f, (locs, err) -> onSearchLocationsReady(locs, err));
            } else {
                clearRunner.run();
            }

            if (mCurrentSearchType == DIRECTION_SEARCH_TYPE) {
                reportDirectionSearchToAnalytics();
            }
        }
    };

    final Runnable clearRunner = this::clearSearchResultList;


    public void onShortcutLocationsReady(@Nullable final List<MPLocation> indoorLocations, @Nullable MIError error) {
        List<SearchResultItem> searchResultItemList = new ArrayList<>();

        if (error == null) {
            AppConfigManager appConfigManager = mActivity.getAppConfigManager();

            VenueCollection venueCollection = MapsIndoors.getVenues();
            BuildingCollection buildingCollection = MapsIndoors.getBuildings();

            LatLng userLatLng = null;

            if (mActivity.getCurrentUserPos() != null) {
                userLatLng = mActivity.getCurrentUserPos().getLatLng();
            }

            // Always add the indoor results first
            searchResultItemList = MapsIndoorsRouteHelper.indoorLocationToSearchResultItemList(
                    indoorLocations,
                    appConfigManager,
                    buildingCollection,
                    venueCollection,
                    userLatLng
            );

            // Store the result count
            mLastSearchResultCount = searchResultItemList.size();
        } else {
            if (error.code == MIError.INVALID_API_KEY) {
                MapsIndoorsUtils.showInvalidAPIKeyDialogue(getContext());
            }
        }

        ArrayList<IconTextElement> initList = null;

        final PositionProvider positionProvider = mActivity.getCurrentPositionProvider();
        if ((positionProvider != null) && positionProvider.isPSEnabled()) {
            initList = new ArrayList<>();
            String estimated_position_near = mActivity.getString(R.string.my_position);

            // when the object that is carried is null then we know it's my position
            initList.add(new IconTextElement(estimated_position_near, R.drawable.ic_button_my_location_active, null, IconTextListAdapter.Objtype.LOCATION));
        }

        boolean labelVisibility;

        if (searchResultItemList.size() == 0 && MapsIndoorsUtils.isNetworkReachable(getContext())) {
            labelVisibility = true;
        } else {
            labelVisibility = false;
        }

        populateMenu(
                labelVisibility,
                initList,
                searchResultItemList,
                MapsIndoorsUtils.isNetworkReachable(getContext()),
                false
        );

        // Change the wait status
        new Handler(mActivity.getMainLooper()).post(() -> changeWaitStatus(FLIPPER_LIST_ITEMS, true));
    }

    public void onSearchLocationsReady(@Nullable final List<MPLocation> locations, @Nullable MIError error) {
        if (error != null) {
            if (error.code == MIError.INVALID_API_KEY) {
                MapsIndoorsUtils.showInvalidAPIKeyDialogue(getContext());
            }
        }

        final AppConfigManager appConfigManager = mActivity.getAppConfigManager();

        final VenueCollection venueCollection = MapsIndoors.getVenues();
        final BuildingCollection buildingCollection = MapsIndoors.getBuildings();
        final String currentLanguage = MapsIndoors.getLanguage();

        LatLng userLatLng = null;
        if (mActivity.getCurrentUserPos() != null) {
            userLatLng = mActivity.getCurrentUserPos().getLatLng();
        }

        // Always add the indoor results first
        final List<SearchResultItem> searchResultItemList = MapsIndoorsRouteHelper.indoorLocationToSearchResultItemList(
                locations,
                appConfigManager,
                buildingCollection,
                venueCollection,
                userLatLng
        );

        // Store the result count
        mLastSearchResultCount = searchResultItemList.size();

        //
        populateMenu(
                false,
                null,
                searchResultItemList,
                MapsIndoorsUtils.isNetworkReachable(getContext()),
                false
        );

        // Setup the external location search (Google AutoComplete)
        GooglePlacesClient.AutoCompleteParamsBuilder paramsBuilder =
                new GooglePlacesClient.AutoCompleteParamsBuilder(
                        mCSearchString,
                        getString(R.string.google_maps_key)
                );

        paramsBuilder.setComponents(MapsIndoorsSettings.SEARCH_COUNTRIES_LIST);


        Point googlePlacesBiaisLocation = null;

        if (mMapControl.getCurrentPosition() != null) {
            googlePlacesBiaisLocation = mMapControl.getCurrentPosition().getPoint();
        } else {
            if (MapsIndoors.getVenues().getCurrentVenue() != null) {
                googlePlacesBiaisLocation = MapsIndoors.getVenues().getCurrentVenue().getAnchor();
            }
        }

        if (googlePlacesBiaisLocation != null) {
            paramsBuilder.setLocation(googlePlacesBiaisLocation).
                    setRadius(MapsIndoorsSettings.GOOGLE_PLACES_API_AUTOCOMPLETE_QUERY_RADIUS).
                    setStrictbounds(false);
        }

        //
        paramsBuilder.
                setStrictbounds(false).
                setLanguage(currentLanguage);


        final PositionResult userPos = mMapControl.getCurrentPosition();
        if ((userPos != null) && (userPos.getPoint() != null)) {
            paramsBuilder.
                    setLocation((userPos.getPoint())).
                    setRadius(MapsIndoorsSettings.GOOGLE_PLACES_API_AUTOCOMPLETE_QUERY_RADIUS);
        }

        switch (mCurrentSearchType) {
            case CATEG_MENU_SEARCH_TYPE:
            case POI_MENU_SEARCH_TYPE:
                updateFlipperFromListenerResults(searchResultItemList.size() > 0);

                // Show search results on map
                mActivity.runOnUiThread(() -> {
                    if (locations != null && mActivity.getSelectionManager() != null) {
                        if (locations.isEmpty()) {
                            mActivity.getSelectionManager().clearSelection();
                        } else {
                            mActivity.getSelectionManager().selectSearchResult(locations);
                        }
                    }
                });

                break;

            case DIRECTION_SEARCH_TYPE: {
                mGooglePlacesClient.getAutoCompleteSuggestions(paramsBuilder.build(), new AutoCompleteSuggestionListener() {
                    @Override
                    public void onResult(@NonNull List<AutoCompleteField> autoCompleteFieldList, @NonNull String status) {

                        switch (status) {
                            case PlacesServiceStatus.OVER_QUERY_LIMIT: {
                                {
                                    final Bundle eventParams = new Bundle();
                                    eventParams.putString("Error", getString(R.string.fir_event_val_over_qry_limit));

                                    GoogleAnalyticsManager.reportEvent(getString(R.string.fir_event_places_api), eventParams);
                                }
                                break;
                            }
                            case PlacesServiceStatus.REQUEST_DENIED: {
                                {
                                    final Bundle eventParams = new Bundle();
                                    eventParams.putString("Error", getString(R.string.fir_event_val_request_denied));

                                    GoogleAnalyticsManager.reportEvent(getString(R.string.fir_event_places_api), eventParams);
                                }
                                break;
                            }
                        }

                        searchResultItemList.addAll(MapsIndoorsRouteHelper.googlePlacesAutocompleteFieldToSearchResultItemList(autoCompleteFieldList));

                        // Store the result count
                        mLastSearchResultCount = searchResultItemList.size();

                        populateMenu(
                                false,
                                null,
                                searchResultItemList,
                                MapsIndoorsUtils.isNetworkReachable(getContext()),
                                autoCompleteFieldList.size() > 0
                        );

                        updateFlipperFromListenerResults(searchResultItemList.size() > 0);
                    }
                });
                break;
            }
        }
    }

    public void close() {
        closeKeyboard();
        setActive(false);
    }

    /**
     * A waiting spinner will appear is set to true and be removed again on false.
     */
    void changeWaitStatus(final int flipperListState, final boolean animate) {
        if (animate) {
            mViewFlipper.setInAnimation(mActivity, R.anim.menu_flipper_fade_in);
            mViewFlipper.setOutAnimation(mActivity, R.anim.menu_flipper_fade_out);
        }

        mViewFlipper.setDisplayedChild(flipperListState);
    }

    void updateFlipperFromListenerResults(final boolean anyResults) {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        activity.runOnUiThread(() -> {
            if (MapsIndoorsUtils.isNetworkReachable(getContext())) {
                noInternetLayout.setVisibility(View.GONE);
            }

            if (anyResults) {
                changeWaitStatus(FLIPPER_LIST_ITEMS, true);
            } else {
                if (!MapsIndoorsUtils.isNetworkReachable(getContext())) {
                    noInternetLayout.setState(NoInternetBar.MESSAGE_STATE);
                    noInternetLayout.setVisibility(View.VISIBLE);
                }

                setNoResultsViewText(String.format(getString(R.string.search_no_matches_for), mCSearchString));
                changeWaitStatus(FLIPPER_LIST_NO_RESULTS, true);
            }
        });
    }


    //region ListView
    private void setupListView(Context context) {

        myAdapter = new IconTextListAdapter(context);
        mSearchMenuList.setAdapter(myAdapter);
    }
    //endregion


    void setNoResultsViewText(String string) {
        mNoResultsTextView.setText(string);
    }


    public void clearSearchResultList() {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        if (isFragmentSafe()) {
            mIsMenuCleared = true;

            activity.runOnUiThread(() -> {

                switch (mCurrentSearchType) {
                    case DIRECTION_SEARCH_TYPE: {
                        mSearchEditTextView.setHint(getString(R.string.search_for));

                        String currentVenueID = mActivity.getCurrentVenueId();

                        MPQuery q = new MPQuery.Builder().
                                build();

                        MPFilter f = new MPFilter.Builder().
                                setTake(MapsIndoorsSettings.FULL_SEARCH_INDOORS_QUERY_RESULT_MAX_LENGTH).
                                setCategories(Collections.singletonList(categStartPoint)).
                                setParents(Collections.singletonList(currentVenueID)).
                                build();

                        MapsIndoors.getLocationsAsync(q, f, this::onShortcutLocationsReady);
                        break;
                    }

                    case CATEG_MENU_SEARCH_TYPE: {
                        mSearchEditTextView.setHint(getString(R.string.search_for));
                        mViewFlipper.setDisplayedChild(FLIPPER_LIST_CATEG_SEARCH_MESS);
                        break;
                    }

                    case POI_MENU_SEARCH_TYPE: {
                        mSearchEditTextView.setHint(String.format(getString(R.string.search_param), mSearchHint));
                        mViewFlipper.setDisplayedChild(FLIPPER_LIST_CATEG_SEARCH_MESS);
                        break;
                    }
                }

                noInternetLayout.setVisibility(View.GONE);
                noInternetLayout.setState(NoInternetBar.MESSAGE_STATE);

                mPoweredByGoogleImageView.setVisibility(View.INVISIBLE);
            });
        }
    }

    // Populate a (reset) menu with the categories and types defined in the mainmenuEntryList
    void populateMenu(boolean searchLabelVisibility, List<IconTextElement> initList, final List<SearchResultItem> searchResultItemList, final boolean internetConnection, final boolean gotAnyGooglePlacesResult) {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        activity.runOnUiThread(() -> {

            List<IconTextElement> elements = new ArrayList<>();

            // init with the initList from the call
            if (initList != null) {
                elements.addAll(initList);
            }

            if (!searchResultItemList.isEmpty()) {
                for (SearchResultItem searchResultItem : searchResultItemList) {
                    final Bitmap bmp = searchResultItem.getBmp();

                    int distance = 0;

                    if (searchResultItem.getDist() != 0) {
                        distance = searchResultItem.getDist();
                    }

                    if (bmp != null) {
                        elements.add(new IconTextElement(searchResultItem.getName(), searchResultItem.getSubtext(), distance, bmp, searchResultItem.getObj(), IconTextListAdapter.Objtype.LOCATION));
                    } else {
                        if (searchResultItem.getType() == IconTextListAdapter.Objtype.LOCATION) {
                            // Check if we have an icon resource, if not, default to the sdk one (step)
                            final int iconId = (searchResultItem.getImgId() < 0) ? R.drawable.ic_generic_item_icon : searchResultItem.getImgId();

                            elements.add(new IconTextElement(searchResultItem.getName(), searchResultItem.getSubtext(), distance, iconId, searchResultItem.getObj(), IconTextListAdapter.Objtype.LOCATION));
                        } else {
                            elements.add(new IconTextElement(searchResultItem.getName(), searchResultItem.getSubtext(), distance, R.drawable.ic_generic_item_icon, searchResultItem.getObj(), IconTextListAdapter.Objtype.PLACE));
                        }
                    }
                }
            }

            boolean saveListItems = true;

            if (internetConnection) {
                if (gotAnyGooglePlacesResult) {
                    mPoweredByGoogleImageView.setVisibility(View.VISIBLE);
                } else {
                    mPoweredByGoogleImageView.setVisibility(View.INVISIBLE);
                }

                // Report to our Analytics guy
                GoogleAnalyticsManager.reportSearch(mCSearchString, searchResultItemList.size());
            } else {
                if (mCurrentSearchType == DIRECTION_SEARCH_TYPE) {
                    String title = getResources().getString(R.string.search_frag_no_con_title);
                    String details = getResources().getString(R.string.search_frag_no_con_details);

                    elements.add(new IconTextElement(title, details, 0, R.drawable.ic_cloud_off_black_24dp, null, IconTextListAdapter.Objtype.MESSAGE));

                    saveListItems = false;
                }
            }

            if (saveListItems) {
                // Save the resulting list...
                setLastSearchResults(elements);
            }

            //All data loaded now. Show the list.
            myAdapter.setList(elements);
            myAdapter.notifyDataSetChanged();

            if (searchLabelVisibility) {
                mSearchTextLabel.setVisibility(View.VISIBLE);
            } else {
                mSearchTextLabel.setVisibility(View.GONE);
            }

        });
    }

    AdapterView.OnItemClickListener mAdapterViewOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // Item on the viewlist selected. Inform the locationFoundListener.
            IconTextListAdapter adapter = (IconTextListAdapter) parent.getAdapter();

            IconTextListAdapter.Objtype itemObjType = adapter.getObjType(position);

            if (itemObjType == IconTextListAdapter.Objtype.LOCATION) {
                Object itemObj = adapter.getItemObj(position);

                if (itemObj == null) {
                    // My position
                    RoutingEndPoint endPoint = new RoutingEndPoint(null, "", -1);
                    mLocationFoundListener.onSearchResultSelected(null, endPoint);
                } else if (itemObj instanceof MPLocation) {
                    // POI Location
                    MPLocation loc = (MPLocation) itemObj;
                    if (mLocationFoundListener != null) {
                        if (BuildConfig.DEBUG) {
                            dbglog.Log(TAG, "item selected: " + loc.getName());
                        }

                        switch (mCurrentSearchType) {
                            case DIRECTION_SEARCH_TYPE: {
                                final RoutingEndPoint endPoint = new RoutingEndPoint(loc, "", RoutingEndPoint.ENDPOINT_TYPE_POI);
                                mLocationFoundListener.onSearchResultSelected(getLastSearchText(), endPoint);
                                break;
                            }
                            case CATEG_MENU_SEARCH_TYPE:
                            case POI_MENU_SEARCH_TYPE: {
                                mLocationFoundListener.onSearchResultSelected(getLastSearchText(), loc);
                                break;
                            }
                        }
                    }
                } else if (itemObj instanceof AutoCompleteField) {
                    // AutoComplete
                    if (BuildConfig.DEBUG) {
                        AutoCompleteField af = (AutoCompleteField) itemObj;
                        dbglog.Log(TAG, "item selected: " + af.mainText);
                    }

                    mAutoCField = (AutoCompleteField) itemObj;

                    getLocationFromGooglePlaceClient(mAutoCField.placeId, resLocation -> mLocationFoundListener.onSearchResultSelected(getLastSearchText(), resLocation));
                }

                if (mCurrentSearchType == DIRECTION_SEARCH_TYPE) {
                    reportDirectionSearchToAnalytics();
                }
            }
        }
    };

    void reportDirectionSearchToAnalytics() {
        String eventString = (mCurrentDirectionSearchType == DIRECTION_DESTINATION_SEARCH) ? getString(R.string.fir_event_Directions_Destination_Search) : getString(R.string.fir_event_Directions_Origin_Search);
        Bundle eventB = new Bundle();
        eventB.putString(getString(R.string.fir_param_Query), mCSearchString);
        GoogleAnalyticsManager.reportEvent(eventString, eventB);
    }

    void getLocationFromGooglePlaceClient(String placeId, GenericObjectResultCallback<RoutingEndPoint> locationReadyCallback) {
        mGooglePlacesClient.getPlaceDetails(
                placeId, getString(R.string.google_maps_key),
                new GeoCodeResultListener() {
                    @Override
                    public void onResult(GeocodeResults geoResults) {

                        GeoCodeResult geoCodeResult = null;

                        // If there is more than one result, just pick the first one with a location...
                        for (GeoCodeResult geoResult : geoResults.results) {
                            if (geoResult.geometry != null && geoResult.geometry.location != null) {
                                geoCodeResult = geoResult;
                                break;
                            }
                        }

                        MPLocation placesLocation;
                        RoutingEndPoint routingEndPoint = null;

                        if (geoCodeResult != null) {
                            placesLocation = new MPLocation.Builder("UserLocation").
                                    setPosition(geoCodeResult.geometry.location.lat, geoCodeResult.geometry.location.lng).
                                    setName(mAutoCField.mainText).
                                    build();

                            String desc;
                            if (!TextUtils.isEmpty(mAutoCField.secondaryText)) {
                                desc = mAutoCField.secondaryText;
                            } else if (!TextUtils.isEmpty(geoCodeResult.formatted_address)) {
                                desc = geoCodeResult.formatted_address;
                            } else {
                                desc = null;
                            }

                            routingEndPoint = new RoutingEndPoint(placesLocation, desc, RoutingEndPoint.ENDPOINT_TYPE_AUTOCOMPLETE);
                        }

                        final RoutingEndPoint resEndpoint = routingEndPoint;

                        Activity act = getActivity();
                        if (act != null) {
                            act.runOnUiThread(() -> locationReadyCallback.onResultReady(resEndpoint));
                        }
                    }
                }
        );
    }

    public void setSearchType(int searchType, int directionSearchType) {
        mCurrentSearchType = searchType;

        if (directionSearchType >= 0) {
            mCurrentDirectionSearchType = directionSearchType;
        }
    }

    public void setCategFilter(String categFilter) {
        mCategFilter = categFilter;
    }

    public void setSearchHint(String hint) {
        mSearchHint = hint;
    }

    /**
     * Hides or shows the search view
     *
     * @param active T/F
     */
    public void setActive(boolean active) {
        if (active) {
            if (mOpenedFromBackPress) {
                if (mMainView != null) {
                    mActivity.showFragment(MenuFrame.MENU_FRAME_SEARCH);
                }

                mSearchEditTextView.setText(getLastSearchText());

                final List<IconTextElement> lastSearchResults = getLastSearchResults();
                if (lastSearchResults != null) {
                    myAdapter.setList(lastSearchResults);
                }

                startSearchTimer();

                mOpenedFromBackPress = false;
            } else {
                if (mMainView != null) {
                    mActivity.menuGoTo(MenuFrame.MENU_FRAME_SEARCH, true);
                }

                final boolean searchIsDirectionType = mCurrentSearchType == SearchFragment.DIRECTION_SEARCH_TYPE;

                if (searchIsDirectionType) {
                    mSearchEditTextView.setText(getLastSearchText());

                    final List<IconTextElement> lastSearchResults = getLastSearchResults();
                    if (lastSearchResults != null) {
                        myAdapter.setList(lastSearchResults);
                    }

                    startSearchTimer();
                } else {
                    clearSearchResultList();

                    clearSearchEditTextBox();
                    setSearchClearBtnActive(false);
                    setFocusOnSearchBox();
                }
            }
        } else {
            closeKeyboard();
            mActivity.menuGoBack();
        }
    }

    public void setOnLocationFoundHandler(SearchResultSelectedListener listener) {
        mLocationFoundListener = listener;
    }


    //region BASEFRAGMENT OVERRIDES
    @Override
    public void connectivityStateChanged(boolean isConnected) {
        if (mIsMenuCleared) {
            clearSearchResultList();
        }
    }

    //endregion


    //region IMPLEMENTS OnStateChangedListener
    final OnStateChangedListener onPositionProviderStateChangedListener = isEnabled -> {
        if (mIsMenuCleared) {
            if (BuildConfig.DEBUG) {
                dbglog.Log(TAG, "onStateChanged:  " + (isEnabled ? "connected" : "not connected"));
            }

            // To avoid UI changes when the fragment is no longer attached to the activity which may cause the app to crash
            if (isAdded()) {
                clearSearchResultList();
            }
        }
    };
    //endregion


    //region UTILS
    public void setLastSearchText(@Nullable String lastSearchText) {
        mLastSearchText[resolveSearchTypeArraysIndex(mCurrentSearchType)] = lastSearchText;
    }

    @Nullable
    public String getLastSearchText() {
        return mLastSearchText[resolveSearchTypeArraysIndex(mCurrentSearchType)];
    }

    public void clearLastSearchText() {
        mLastSearchText[resolveSearchTypeArraysIndex(mCurrentSearchType)] = "";
    }

    public void setLastSearchResults(@Nullable List<IconTextElement> itemList) {
        mLastSearchResults[resolveSearchTypeArraysIndex(mCurrentSearchType)] = itemList;
    }

    public void copySearchData(int searchTypeSrc, int searchTypeDst, int directionSearchType) {
        final int srcIndex = resolveSearchTypeArraysIndex(searchTypeSrc);
        final int dstIndex = resolveSearchTypeArraysIndex(searchTypeDst, directionSearchType);

        // Query string
        final String srcString = mLastSearchText[srcIndex];
        mLastSearchText[dstIndex] = (srcString != null) ? "" + srcString : "";

        // Search results
        List<IconTextElement> srcItemList = mLastSearchResults[srcIndex];
        if (srcItemList != null) {
            final List<IconTextElement> dstItemList = new ArrayList<>(srcItemList.size());
            dstItemList.addAll(srcItemList);
            mLastSearchResults[dstIndex] = dstItemList;
        } else {
            mLastSearchResults[dstIndex] = null;
        }
    }

    public void swapDirectionSearchData() {
        final int dirOriginIndex = DIRECTION_SEARCH_TYPE + DIRECTION_ORIGIN_SEARCH;
        final int dirDestinationIndex = DIRECTION_SEARCH_TYPE + DIRECTION_DESTINATION_SEARCH;

        final String swapSearchString = mLastSearchText[dirOriginIndex];
        mLastSearchText[dirOriginIndex] = mLastSearchText[dirDestinationIndex];
        mLastSearchText[dirDestinationIndex] = swapSearchString;

        final List<IconTextElement> swapSearchItemList = mLastSearchResults[dirOriginIndex];
        mLastSearchResults[dirOriginIndex] = mLastSearchResults[dirDestinationIndex];
        mLastSearchResults[dirDestinationIndex] = swapSearchItemList;
    }

    @Nullable
    public List<IconTextElement> getLastSearchResults() {
        return mLastSearchResults[resolveSearchTypeArraysIndex(mCurrentSearchType)];
    }

    public void clearLastSearchResults() {
        mLastSearchResults[resolveSearchTypeArraysIndex(mCurrentSearchType)] = null;
    }

    public void clearDirectionSearchData() {
        final int dirOriginIndex = DIRECTION_SEARCH_TYPE + DIRECTION_ORIGIN_SEARCH;
        final int dirDestinationIndex = DIRECTION_SEARCH_TYPE + DIRECTION_DESTINATION_SEARCH;

        mLastSearchText[dirOriginIndex] = "";
        mLastSearchText[dirDestinationIndex] = "";
        mLastSearchResults[dirOriginIndex] = null;
        mLastSearchResults[dirDestinationIndex] = null;
    }

    private int resolveSearchTypeArraysIndex(int searchType) {
        return (searchType == DIRECTION_SEARCH_TYPE)
                ? (DIRECTION_SEARCH_TYPE + mCurrentDirectionSearchType)
                : searchType;
    }

    private int resolveSearchTypeArraysIndex(int searchType, int directionSearchType) {
        return (searchType == DIRECTION_SEARCH_TYPE)
                ? (DIRECTION_SEARCH_TYPE + directionSearchType)
                : searchType;
    }

    void clearSearchEditTextBox() {
        if (mSearchEditTextView != null) {
            mSearchEditTextView.getText().clear();
            clearLastSearchText();
            clearLastSearchResults();
        }
    }
}
