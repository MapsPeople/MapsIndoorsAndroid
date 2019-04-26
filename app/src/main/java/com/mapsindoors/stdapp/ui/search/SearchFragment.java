package com.mapsindoors.stdapp.ui.search;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import com.mapsindoors.mapssdk.Location;
import com.mapsindoors.mapssdk.LocationQuery;
import com.mapsindoors.mapssdk.MPLocationsProvider;
import com.mapsindoors.mapssdk.MapControl;
import com.mapsindoors.mapssdk.MapsIndoors;
import com.mapsindoors.mapssdk.OnLocationsReadyListener;
import com.mapsindoors.mapssdk.PositionProvider;
import com.mapsindoors.mapssdk.dbglog;
import com.mapsindoors.mapssdk.errors.MIError;
import com.mapsindoors.mapssdk.BuildingCollection;
import com.mapsindoors.mapssdk.MPLocation;
import com.mapsindoors.mapssdk.Point;
import com.mapsindoors.mapssdk.Venue;
import com.mapsindoors.mapssdk.VenueCollection;
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
import com.mapsindoors.stdapp.ui.common.models.IconTextElement;
import com.mapsindoors.stdapp.ui.common.models.SearchResultItem;
import com.mapsindoors.stdapp.ui.components.noInternetBar.NoInternetBar;
import com.mapsindoors.stdapp.ui.direction.models.RoutingEndPoint;
import com.mapsindoors.stdapp.ui.menumain.MenuFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class SearchFragment extends BaseFragment
{
    //
    final public static int DIRECTION_SEARCH_TYPE = 0;
    final public static int CATEG_MENU_SEARCH_TYPE = 1;
    final public static int POI_MENU_SEARCH_TYPE = 2;

    //
    final public static int DIRECTION_ORIGIN_SEARCH = 0;
    final public static int DIRECTION_DESTINATION_SEARCH = 1;


    private static final String TAG = SearchFragment.class.getSimpleName();


    private static final int FLIPPER_LIST_ITEMS = 0;
    private static final int FLIPPER_LIST_PROGRESS = 1;
    private static final int FLIPPER_LIST_NO_RESULTS =3 ;
    private static final int FLIPPER_LIST_CATEG_SEARCH_MESS = 2;

    int mCurrentSearchType;

    int mCurrentDirectionSearchType = -1;

    String categStartPoint = "startpoint";

    Context mContext;
    MapsIndoorsActivity mActivity;
    MapControl mMapControl;

    private Handler searchHandler;

    SearchResultSelectedListener mLocationFoundListener;
    LocationQuery mSearchQuery;
    LocationQuery mShortcutQuery;
    LocationQuery.Builder iLocsQueryBuilder;

    EditText mSearchEditTextView;
    ImageButton mSearchClearBtn;
    IconTextListAdapter myAdapter;
    ViewFlipper mViewFlipper;
    TextView mNoResultsTextView;
    TextView mSearchTextLabel;

    ListView mSearchMenuList;
    ImageButton mBackButton;


    GooglePlacesClient mGooglePlacesClient;
    AutoCompleteField mAutoCField;

    boolean mIsMenuCleared = false;
    View mPoweredByGoogleImageView;
    int mLastSearchResultCount;
    TextView messageTextView;

    String mCategFilter;
    String mSearchHint;

    MenuFragment mMenuFragment;

    public boolean mOpenedFromBackPress = false;

    String mLastSearchText;


    NoInternetBar noInternetLayout;


    //region Fragment lifecycle events
    @Nullable
    @Override
    public View onCreateView( @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mMainView == null) {
            mMainView = inflater.inflate(R.layout.fragment_search, container);
        }
        return mMainView;
    }

    @Override
    public void onViewCreated( @NonNull View view, @Nullable Bundle savedInstanceState )
    {
        super.onViewCreated(view, savedInstanceState);

        mViewFlipper = view.findViewById( R.id.directionsfullmenu_itemlist_viewflipper );
        mSearchMenuList = view.findViewById( R.id.directionsfullmenu_itemlist );
        mNoResultsTextView = view.findViewById( R.id.control_noresults_text );
        messageTextView = view.findViewById( R.id.control_message_text );
        mSearchTextLabel = view.findViewById( R.id.search_text_label );

        // Search box text
        mSearchEditTextView = view.findViewById( R.id.search_fragment_edittext_search );

        // Clear search button
        mSearchClearBtn = view.findViewById( R.id.directionsfullmenu_search_clear_btn );

        mPoweredByGoogleImageView = view.findViewById( R.id.powered_by_image_view );

        mBackButton = view.findViewById( R.id.directionsfullmenusearch_back_button );

        noInternetLayout = view.findViewById( R.id.search_frag_no_connection_layout );

        noInternetLayout.setOnClickListener( v -> {
            noInternetLayout.setState( NoInternetBar.REFRESHING_STATE );
            searchRunner.run();
        });

        //
        final String messageText = String.format(
                getResources().getString(R.string.search_explaining_text),
                getResources().getString(R.string.app_provider_name_in_search)
        );
        messageTextView.setText( messageText );
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if( !mIsMenuCleared )
        {
            clearSearchResultList();
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
    }
    //endregion


    public void init( final Context context, final MapControl mapControl )
    {
        mContext = context;
        mActivity = (MapsIndoorsActivity) context;
        mMapControl = mapControl;

        mMainView.setVisibility( View.GONE );

        mSearchMenuList.setAdapter( myAdapter );
        mSearchMenuList.setClickable( true );
        mSearchMenuList.setOnItemClickListener( mAdapterViewOnItemClickListener );
        mSearchMenuList.invalidate();


        setupListView( context, mMainView );

        //Note: Creating a textwatcher as it's needed for software keyboard support.
        mSearchEditTextView.addTextChangedListener( mEditTextViewTextWatcher );
        mSearchEditTextView.setOnFocusChangeListener( mEditTextViewOnFocusChangeListener );

        //Close keyboard and search when user presses search on the keyboard:
        mSearchEditTextView.setOnEditorActionListener( mEditTextViewOnEditorActionListener );

        //Close keyboard and search when user presses enter:
        mSearchEditTextView.setOnKeyListener( mEditTextOnKeyListener );

        // Clear search button
        mSearchClearBtn.setOnClickListener( mClearSearchButtonClickListener );
        mSearchClearBtn.setOnFocusChangeListener( mClearSearchButtonFocusChangeListener );

        mBackButton.setOnClickListener( view -> close() );

        //Set up a location locationFoundListener and make a location search call
        mCSearchLocationsProvider = new MPLocationsProvider();

        // Setup the query; the search string will be set where needed
        iLocsQueryBuilder = new LocationQuery.Builder();

        // Prepare the query to get the requested location


        // Setup the Google Map APIs used
        mGooglePlacesClient = new GooglePlacesClient();

        // Register the GPS broadcast receiver
        mIsMenuCleared = true;

        PositionProvider positionProvider = mActivity.getCurrentPositionProvider();

        positionProvider.addOnstateChangedListener( isEnabled -> {
            if( mIsMenuCleared )
            {
                if( BuildConfig.DEBUG )
                {
                    dbglog.Log( TAG, "onStateChanged:  " + (isEnabled ? "connected" : "not connected") );
                }

                // To avoid UI changes when the fragment is no longer attached to the activity which may cause the app to crash
                if( isAdded() )
                {
                    clearSearchResultList();
                }
            }
        });
    }



    //region Edit Text field

    /**
     *
     */
    TextWatcher mEditTextViewTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged( CharSequence s, int start, int count, int after ) {}

        @Override
        public void onTextChanged( CharSequence s, int start, int before, int count )
        {
            String text = mSearchEditTextView.getText().toString();
            if( !text.isEmpty() )
            {
                if( text.startsWith( " " ) )
                {
                    mSearchEditTextView.setText( text.trim() );
                }
            }
            else
            {
                runClearSearchButtonClickAction();
            }
        }

        @Override
        public void afterTextChanged( Editable s )
        {
            // Only start searching if the user wrote something to look for
            if( !TextUtils.isEmpty( s ) )
            {
                mIsMenuCleared = false;

                startSearchTimer();
            }
        }
    };

    /**
     *
     */
    View.OnFocusChangeListener mEditTextViewOnFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange( View view, boolean hasFocus )
        {
            if( hasFocus )
            {
                mSearchEditTextView.getText().clear();
                setSearchClearBtnActive( true );
                openKeyboard();
            }
        }
    };

    /**
     * Close keyboard and search when user presses search on the keyboard
     */
    TextView.OnEditorActionListener mEditTextViewOnEditorActionListener = ( view, actionId, event ) -> {
        boolean handled = false;
        if( actionId == EditorInfo.IME_ACTION_SEARCH )
        {
            closeKeyboard();
            handled = true;
        }
        return handled;
    };

    /**
     * Close keyboard and search when user presses enter
     */
    View.OnKeyListener mEditTextOnKeyListener = ( view, keyCode, keyEvent ) -> {

        if( keyEvent.getAction() == KeyEvent.ACTION_DOWN )
        {
            switch( keyCode )
            {
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_ENTER:
                {
                    closeKeyboard();
                    return true;
                }
                case KeyEvent.KEYCODE_BACK:
                {
                    break;
                }
                default:
                    startSearchTimer();
                    break;
            }
        }
        return false;
    };

    private void setFocusOnSearchBox()
    {
        mSearchEditTextView.post( () -> {

            Activity activity = getActivity();
            if( activity != null )
            {
                mSearchEditTextView.requestFocusFromTouch();

                InputMethodManager lManager = (InputMethodManager) activity.getSystemService( Context.INPUT_METHOD_SERVICE );
                if( lManager != null )
                {
                    lManager.showSoftInput( mSearchEditTextView, 0 );
                }
            }
        } );
    }
    //endregion


    public void setSearchClearBtnActive( boolean exitActive )
    {
        mSearchClearBtn.setVisibility( exitActive ? View.VISIBLE : View.INVISIBLE );
    }


    //region Clear Search button
    /**
     *
     */
    View.OnClickListener mClearSearchButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick( View view )
        {
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
        }
    };

    /**
     *
     */
    View.OnFocusChangeListener mClearSearchButtonFocusChangeListener = ( view, hasFocus ) -> {
        if( hasFocus )
        {
            //Exit button pressed. Close the keyboard and go back to default - (viewing types)
            runClearSearchButtonClickAction();
        }
    };

    /**
     * Exit button pressed. Close the keyboard and go back to default - (viewing types)
     */
    void runClearSearchButtonClickAction()
    {
        resetSearchBox();
        setFocusOnSearchBox();
        //clearSearchResultList();
        startClearListTimer();
    }

    private void resetSearchBox()
    {
        mSearchEditTextView.getText().clear();
        setSearchClearBtnActive( false );
    }
    //endregion


    void closeKeyboard()
    {
        if( mActivity != null )
        {
            InputMethodManager imm = (InputMethodManager) mActivity.getSystemService( Context.INPUT_METHOD_SERVICE );
            if( imm != null )
            {
                imm.hideSoftInputFromWindow( mMainView.getWindowToken(), 0 );
            }
        }
    }

    void openKeyboard()
    {
        if( mActivity != null )
        {
            InputMethodManager imm = (InputMethodManager) mActivity.getSystemService( Context.INPUT_METHOD_SERVICE );
            if( imm != null )
            {
                imm.toggleSoftInput( InputMethodManager.SHOW_IMPLICIT, 0 );
            }
        }
    }

    // Only search after a second of delay. Any search requests before one sec should replace the seach and restart the timer.
    void startSearchTimer()
    {
        if( searchHandler != null )
        {
            searchHandler.removeCallbacks( searchRunner );
            searchHandler.removeCallbacks( clearRunner );
        }

        searchHandler = new Handler();
        searchHandler.postDelayed( searchRunner, 1000 );
    }

    void startClearListTimer()
    {
        if( searchHandler != null )
        {
            searchHandler.removeCallbacks( searchRunner );
            searchHandler.removeCallbacks( clearRunner );

        }
        changeWaitStatus( FLIPPER_LIST_PROGRESS, true );
        searchHandler = new Handler();
        searchHandler.postDelayed( clearRunner, 1000 );
    }

    /**
     * Non-empty search string
     */
    String mCSearchString;
    MPLocationsProvider mCSearchLocationsProvider;

    private Runnable searchRunner = new Runnable()
    {
        @Override
        public void run()
        {
            String searchString = mSearchEditTextView.getText().toString();

            if( !TextUtils.isEmpty( searchString ) )
            {
                mCSearchString = searchString.trim();
                mLastSearchText = mCSearchString;

                if( !mCSearchString.isEmpty() )
                {
                    // Show as busy
                    changeWaitStatus( FLIPPER_LIST_PROGRESS, true );

                    if( dbglog.isDebugMode() )
                    {
                        dbglog.Log( TAG, "Search for: " + mCSearchString );
                    }

                    //noResultsFoundFeedback( -1 );
                    Venue currentVenue = mActivity.getVenueCollection().getCurrentVenue();
                    switch( mCurrentSearchType )
                    {
                        case POI_MENU_SEARCH_TYPE:
                        {
                            iLocsQueryBuilder.setCategories( Collections.singletonList( mCategFilter ) );
                            iLocsQueryBuilder.
                                setOrderBy( LocationQuery.NO_ORDER ).
                                setQueryMode( LocationQuery.MODE_PREFER_ONLINE ).
                                setMaxResults( MapsIndoorsSettings.INDOOR_LOCATIONS_QUERY_RESULT_MAX_LENGTH ).
                                setNear( (currentVenue != null) ? currentVenue.getPosition() : null );
                            break;
                        }
                        default:
                        {
                            iLocsQueryBuilder.
                                setCategories( null ).
                                setOrderBy( LocationQuery.RELEVANCE ).
                                setQueryMode( LocationQuery.MODE_PREFER_ONLINE ).
                                setMaxResults( MapsIndoorsSettings.FULL_SEARCH_INDOORS_QUERY_RESULT_MAX_LENGTH ).
                                setNear( (currentVenue != null) ? currentVenue.getPosition() : null );
                        }
                    }

                    mSearchQuery = iLocsQueryBuilder.build();

                    // Indoor locations - setup the search query
                    mSearchQuery.setQuery( mCSearchString );

                    mCSearchLocationsProvider.getLocationsAsync( mSearchQuery, mSearchLocationsReadyListener );
                }
            }
            else
            {
                // changeWaitStatus(FLIPPER_LIST_ITEMS , true );
                clearRunner.run();
            }

            if( mCurrentSearchType == DIRECTION_SEARCH_TYPE )
            {
                reportDirectionSearchToAnalytics();
            }
        }
    };

    Runnable clearRunner = this::clearSearchResultList;

    OnLocationsReadyListener mShortcutLocationsReadyListener = new OnLocationsReadyListener() {

        @Override
        public void onLocationsReady( @Nullable final List< Location > indoorLocations, @Nullable MIError error )
        {
            List< SearchResultItem > searchResultItemList = new ArrayList<>();

            if( error == null )
            {
                AppConfigManager appConfigManager = mActivity.getAppConfigManager();

                VenueCollection venueCollection = MapsIndoors.getVenues();
                BuildingCollection buildingCollection = MapsIndoors.getBuildings();

                // Always add the indoor results first
                searchResultItemList = MapsIndoorsRouteHelper.indoorLocationToSearchResultItemList(
                        indoorLocations,
                        appConfigManager,
                        buildingCollection,
                        venueCollection
                );

                // Store the result count
                mLastSearchResultCount = searchResultItemList.size();
            }
            else
            {
                if( error.code == MIError.INVALID_API_KEY )
                {
                    MapsIndoorsUtils.showInvalidAPIKeyDialogue( getContext() );
                }
            }

            ArrayList<IconTextElement> initList = null;

            if( mActivity.getCurrentPositionProvider().isPSEnabled() )
            {
                initList = new ArrayList<>();
                String estimated_position_near = mContext.getString( R.string.my_position );

                // when the object that is carried is null then we know it's my position
                initList.add( new IconTextElement( estimated_position_near, R.drawable.ic_my_location_active, null, IconTextListAdapter.Objtype.LOCATION ) );
            }

            boolean labelVisibility;

            if( searchResultItemList.size() == 0 && MapsIndoorsUtils.isNetworkReachable( getContext() ) )
            {
                labelVisibility = true;
            }
            else
            {
                labelVisibility = false;
                if( !MapsIndoorsUtils.isNetworkReachable( getContext() ) )
                {
                    //
                }
            }

            populateMenu(
                    labelVisibility,
                    initList,
                    searchResultItemList,
                    MapsIndoorsUtils.isNetworkReachable( getContext() ),
                    false
            );

            // Change the wait status
            new Handler( mContext.getMainLooper() ).post( () -> changeWaitStatus( FLIPPER_LIST_ITEMS, true ) );
        }
    };


    OnLocationsReadyListener mSearchLocationsReadyListener = new OnLocationsReadyListener() {

        @Override
        public void onLocationsReady( @Nullable final List<Location> locations, @Nullable MIError error )
        {
            if( error != null )
            {
                if( error.code == MIError.INVALID_API_KEY )
                {
                    MapsIndoorsUtils.showInvalidAPIKeyDialogue( getContext() );
                }
            }

            AppConfigManager appConfigManager = mActivity.getAppConfigManager();

            VenueCollection    venueCollection    = MapsIndoors.getVenues();
            BuildingCollection buildingCollection = MapsIndoors.getBuildings();
            String             currentLanguage    = MapsIndoors.getLanguage();

            // Always add the indoor results first
            final List<SearchResultItem> searchResultItemList = MapsIndoorsRouteHelper.indoorLocationToSearchResultItemList(
                    locations,
                    appConfigManager,
                    buildingCollection,
                    venueCollection
            );

            // Store the result count
            mLastSearchResultCount = searchResultItemList.size();

            //
            populateMenu(
                    false,
                    null,
                    searchResultItemList,
                    MapsIndoorsUtils.isNetworkReachable( getContext() ),
                    false
            );


            // Setup the external location search (Google AutoComplete)
            GooglePlacesClient.AutoCompleteParamsBuilder paramsBuilder =
                    new GooglePlacesClient.AutoCompleteParamsBuilder(
                            mCSearchString,
                            getString(R.string.google_maps_key)
                    );

            paramsBuilder.setComponents( MapsIndoorsSettings.SEARCH_COUNTRIES_LIST );

            //
            paramsBuilder.
                    setLocation( mMapControl.getCurrentPosition().getPoint() ).
                    setRadius( MapsIndoorsSettings.GOOGLE_PLACES_API_AUTOCOMPLETE_QUERY_RADIUS ).
                    setStrictbounds( false ).
                    setLanguage( currentLanguage );


            switch( mCurrentSearchType )
            {
                case CATEG_MENU_SEARCH_TYPE :
                case POI_MENU_SEARCH_TYPE :
                    updateFlipperFromListenerResults( searchResultItemList.size() > 0 );
                    break;

                case DIRECTION_SEARCH_TYPE :
                {
                    mGooglePlacesClient.getAutoCompleteSuggestions( paramsBuilder.build(), new AutoCompleteSuggestionListener()
                    {
                        @Override
                        public void onResult( @NonNull List<AutoCompleteField> autoCompleteFieldList, @NonNull String status ) {

                            switch( status )
                            {
                                case PlacesServiceStatus.OVER_QUERY_LIMIT:
                                {
                                    {
                                        final Bundle eventParams = new Bundle();
                                        eventParams.putString( "Error", getString( R.string.fir_event_val_over_qry_limit ) );

                                        GoogleAnalyticsManager.reportEvent( getString( R.string.fir_event_places_api ), eventParams );
                                    }
                                    break;
                                }
                                case PlacesServiceStatus.REQUEST_DENIED:
                                {
                                    {
                                        final Bundle eventParams = new Bundle();
                                        eventParams.putString( "Error", getString( R.string.fir_event_val_request_denied ) );

                                        GoogleAnalyticsManager.reportEvent( getString( R.string.fir_event_places_api ), eventParams );
                                    }
                                    break;
                                }
                            }

                            searchResultItemList.addAll( MapsIndoorsRouteHelper.googlePlacesAutocompleteFieldToSearchResultItemList( autoCompleteFieldList ) );

                            // Store the result count
                            mLastSearchResultCount = searchResultItemList.size();

                            populateMenu(
                                    false,
                                    null,
                                    searchResultItemList,
                                    MapsIndoorsUtils.isNetworkReachable( getContext() ),
                                    autoCompleteFieldList.size() > 0
                            );

                            updateFlipperFromListenerResults( searchResultItemList.size() > 0 );
                        }
                    });
                    break;
                }
            }
        }
    };


    public void close()
    {
        closeKeyboard();
        setActive( false );
    }

    /**
     * A waiting spinner will appear is set to true and be removed again on false.
     */
    void changeWaitStatus( final int flipperListState, final boolean animate )
    {
        if( animate )
        {
            mViewFlipper.setInAnimation( mContext, R.anim.menu_flipper_fade_in );
            mViewFlipper.setOutAnimation( mContext, R.anim.menu_flipper_fade_out );
        }

        mViewFlipper.setDisplayedChild( flipperListState );
    }

    void updateFlipperFromListenerResults( final boolean anyResults )
    {
        Activity activity = getActivity();
        if( activity == null )
        {
            return;
        }

        activity.runOnUiThread( () -> {
            if( MapsIndoorsUtils.isNetworkReachable( getContext() ) )
            {
                noInternetLayout.setVisibility( View.GONE );
            }

            if( anyResults )
            {
                changeWaitStatus( FLIPPER_LIST_ITEMS, true );
            }
            else
            {
                if( !MapsIndoorsUtils.isNetworkReachable( getContext() ) )
                {
                    noInternetLayout.setState( NoInternetBar.MESSAGE_STATE );
                    noInternetLayout.setVisibility( View.VISIBLE );
                }

                setNoResultsViewText( String.format( getString( R.string.search_no_matches_for ), mCSearchString ) );
                changeWaitStatus( FLIPPER_LIST_NO_RESULTS, true );
            }
        } );
    }


    //region ListView
    private void setupListView( Context context, View view )
    {

        myAdapter = new IconTextListAdapter( context );
        mSearchMenuList.setAdapter( myAdapter );
    }
    //endregion


    void setNoResultsViewText( String string )
    {
        mNoResultsTextView.setText( string );
    }


    public void clearSearchResultList()
    {
        Activity activity = getActivity();
        if( activity == null )
        {
            return;
        }

        if( isFragmentSafe() )
        {
            mIsMenuCleared = true;

            activity.runOnUiThread( () -> {

                switch( mCurrentSearchType )
                {
                    case DIRECTION_SEARCH_TYPE:
                    {
                        mSearchEditTextView.setHint( getString( R.string.search_for ) );
                        iLocsQueryBuilder.
                                setOrderBy( LocationQuery.RELEVANCE ).
                                setQueryMode( LocationQuery.MODE_PREFER_ONLINE ).
                                setMaxResults( MapsIndoorsSettings.FULL_SEARCH_INDOORS_QUERY_RESULT_MAX_LENGTH );

                        // Building the shortcut starting point query
                        String         currentVenueID = mActivity.getCurrentVenueName();
                        List<String>   categList      = new ArrayList<>();
                        categList.add( categStartPoint );

                        iLocsQueryBuilder.setVenue( currentVenueID );
                        iLocsQueryBuilder.setCategories( categList );

                        mShortcutQuery = iLocsQueryBuilder.build();
                        mCSearchLocationsProvider.getLocationsAsync( mShortcutQuery, mShortcutLocationsReadyListener );
                        break;
                    }

                    case CATEG_MENU_SEARCH_TYPE:
                    {
                        mSearchEditTextView.setHint( getString( R.string.search_for ) );
                        mViewFlipper.setDisplayedChild( FLIPPER_LIST_CATEG_SEARCH_MESS );
                        break;
                    }

                    case POI_MENU_SEARCH_TYPE:
                    {
                        mSearchEditTextView.setHint( String.format( getString( R.string.search_param ), mSearchHint ) );
                        mViewFlipper.setDisplayedChild( FLIPPER_LIST_CATEG_SEARCH_MESS );
                        break;
                    }
                }

                noInternetLayout.setVisibility( View.GONE );
                noInternetLayout.setState( NoInternetBar.MESSAGE_STATE );

                mPoweredByGoogleImageView.setVisibility( View.INVISIBLE );
            } );
        }
    }

    // Populate a (reset) menu with the categories and types defined in the mainmenuEntryList
    void populateMenu( boolean searchLabelVisibility, List< IconTextElement > initList, final List< SearchResultItem > searchResultItemList, final boolean internetConnection, final boolean gotAnyGooglePlacesResult )
    {
        Activity activity = getActivity();
        if( activity == null )
        {
            return;
        }

        activity.runOnUiThread( () -> {

            ArrayList< IconTextElement > elements = new ArrayList<>();

            // init with the initList from the call
            if( initList != null )
            {
                elements.addAll( initList );
            }

            if( !searchResultItemList.isEmpty() )
            {
                for( SearchResultItem searchResultItem : searchResultItemList )
                {
                    Bitmap bmp = searchResultItem.getBmp();

                    if( bmp != null )
                    {
                        elements.add( new IconTextElement( searchResultItem.getName(), searchResultItem.getSubtext(), 0, bmp, searchResultItem.getObj(), IconTextListAdapter.Objtype.LOCATION ) );
                    }
                    else
                    {
                        if( searchResultItem.getType() == IconTextListAdapter.Objtype.LOCATION )
                        {
                            // Check if we have an icon resource, if not, default to the sdk one (step)
                            int iconId = (searchResultItem.getImgId() < 0) ? com.mapsindoors.mapssdk.R.drawable.misdk_step : searchResultItem.getImgId();

                            elements.add( new IconTextElement( searchResultItem.getName(), searchResultItem.getSubtext(), 0, iconId, searchResultItem.getObj(), IconTextListAdapter.Objtype.LOCATION ) );
                        }
                        else
                        {
                            elements.add( new IconTextElement( searchResultItem.getName(), searchResultItem.getSubtext(), 0, com.mapsindoors.mapssdk.R.drawable.misdk_step, searchResultItem.getObj(), IconTextListAdapter.Objtype.PLACE ) );
                        }
                    }
                }
            }

            if( internetConnection )
            {
                if( gotAnyGooglePlacesResult )
                {
                    mPoweredByGoogleImageView.setVisibility( View.VISIBLE );
                }
                else
                {
                    mPoweredByGoogleImageView.setVisibility( View.INVISIBLE );
                }

                // Report to our Analytics guy
                GoogleAnalyticsManager.reportSearch( mCSearchString, searchResultItemList.size() );
            }
            else
            {
                if( mCurrentSearchType == DIRECTION_SEARCH_TYPE )
                {
                    String title   = getResources().getString( R.string.search_frag_no_con_title );
                    String details = getResources().getString( R.string.search_frag_no_con_details );

                    elements.add( new IconTextElement( title, details, 0, R.drawable.ic_cloud_off_black_24dp, null, IconTextListAdapter.Objtype.MESSAGE ) );
                }
            }

            //All data loaded now. Show the list.
            myAdapter.setList( elements );
            myAdapter.notifyDataSetChanged();

            if( searchLabelVisibility )
            {
                mSearchTextLabel.setVisibility( View.VISIBLE );
            }
            else
            {
                mSearchTextLabel.setVisibility( View.GONE );
            }

        } );
    }

    AdapterView.OnItemClickListener mAdapterViewOnItemClickListener = new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick( AdapterView<?> parent, View view, int position, long id )
        {
            // Item on the viewlist selected. Inform the locationFoundListener.
            IconTextListAdapter adapter = (IconTextListAdapter) parent.getAdapter();

            IconTextListAdapter.Objtype itemObjType = adapter.getObjType(position);

            if( itemObjType == IconTextListAdapter.Objtype.LOCATION )
            {
                Object itemObj = adapter.getItemObj( position );

                if( itemObj == null )
                {
                    // My position
                    RoutingEndPoint endPoint = new RoutingEndPoint( null, "", -1);
                    mLocationFoundListener.onSearchResultSelected( endPoint );
                }
                else if( itemObj instanceof Location )
                {
                    // POI Location
                    Location loc = (Location) itemObj;
                    if( mLocationFoundListener != null )
                    {
                        if( BuildConfig.DEBUG )
                        {
                            dbglog.Log( TAG, "item selected: " + loc.getName() );
                        }

                        RoutingEndPoint endPoint = new RoutingEndPoint( loc, "", RoutingEndPoint.ENDPOINT_TYPE_POI );

                        switch( mCurrentSearchType )
                        {
                            case DIRECTION_SEARCH_TYPE:
                            {
                                mLocationFoundListener.onSearchResultSelected( endPoint );
                                break;
                            }
                            case CATEG_MENU_SEARCH_TYPE:
                            case POI_MENU_SEARCH_TYPE:
                            {
                                mLocationFoundListener.onSearchResultSelected( loc );
                                break;
                            }
                        }
                    }
                }
                else if( itemObj instanceof AutoCompleteField )
                {
                    // AutoComplete
                    if( BuildConfig.DEBUG )
                    {
                        AutoCompleteField af = (AutoCompleteField) itemObj;
                        dbglog.Log( TAG, "item selected: " + af.mainText );
                    }

                    mAutoCField = (AutoCompleteField) itemObj;

                    getLocationFromGooglePlaceClient( mAutoCField.placeId, resLocation -> mLocationFoundListener.onSearchResultSelected( resLocation ) );
                }

                if( mCurrentSearchType == DIRECTION_SEARCH_TYPE )
                {
                    reportDirectionSearchToAnalytics();
                }
            }
        }
    };

    void reportDirectionSearchToAnalytics()
    {
        String eventString = (mCurrentDirectionSearchType == DIRECTION_DESTINATION_SEARCH) ? getString( R.string.fir_event_Directions_Destination_Search ) : getString( R.string.fir_event_Directions_Origin_Search );
        Bundle eventB      = new Bundle();
        eventB.putString( getString( R.string.fir_param_Query ), mCSearchString );
        GoogleAnalyticsManager.reportEvent( eventString, eventB );
    }

    void getLocationFromGooglePlaceClient( String placeId, GenericObjectResultCallback< RoutingEndPoint > locationReadyCallback )
    {
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

                        Location placesLocation = null;
                        RoutingEndPoint routingEndPoint = null;

                        if( geoCodeResult != null )
                        {
                            //
                            placesLocation = new MPLocation(
                                    new Point(geoCodeResult.geometry.location.lat, geoCodeResult.geometry.location.lng),
                                    mAutoCField.mainText
                            );

                            String desc;
                            if( !TextUtils.isEmpty( mAutoCField.secondaryText ) )
                            {
                                desc = mAutoCField.secondaryText;
                            }
                            else if( !TextUtils.isEmpty( geoCodeResult.formatted_address ) )
                            {
                                desc = geoCodeResult.formatted_address;
                            }
                            else
                            {
                                desc = null;
                            }

                            routingEndPoint = new RoutingEndPoint(placesLocation, desc, RoutingEndPoint.ENDPOINT_TYPE_AUTOCOMPLETE);
                        }

                        final RoutingEndPoint resEndpoint = routingEndPoint;

                        Activity act = getActivity();
                        if( act != null )
                        {
                            act.runOnUiThread( () -> locationReadyCallback.onResultReady( resEndpoint ) );
                        }
                    }
                }
        );
    }


    public void setSearchType( int searchType )
    {
        mCurrentSearchType = searchType;
    }

    void setDirectionSearchType( int directionSearchType )
    {
        mCurrentDirectionSearchType = directionSearchType;
    }

    public void setCategFilter( String categFilter )
    {
        mCategFilter = categFilter;
    }

    public void setSearchHint( String hint )
    {
        mSearchHint = hint;
    }

    /**
     * Hides or shows the search view
     *
     * @param active T/F
     */
    public void setActive( boolean active )
    {
        mMenuFragment = mActivity.getMenuFragment();

        if( active )
        {
            if( mOpenedFromBackPress )
            {
                if( mMainView != null )
                {
                    mActivity.showFragment( MapsIndoorsActivity.MENU_FRAME_SEARCH );
                }

                mSearchEditTextView.setText( mLastSearchText );

                startSearchTimer();

                mOpenedFromBackPress = false;
            }
            else
            {
                if( mMainView != null )
                {
                    mActivity.menuGoTo( MapsIndoorsActivity.MENU_FRAME_SEARCH, true );
                }

                clearSearchResultList();

                mSearchEditTextView.getText().clear();
                setSearchClearBtnActive( false );
            }
        }
        else
        {
            closeKeyboard();
            mActivity.menuGoBack();
        }
    }

    public void setOnLocationFoundHandler( SearchResultSelectedListener listener )
    {
        mLocationFoundListener = listener;
    }

    @Override
    public void connectivityStateChanged( boolean isConnected )
    {
        if( mIsMenuCleared )
        {
            clearSearchResultList();
        }
    }

    //region Implements IActivityEvents
    @Override
    public boolean onBackPressed()
    {
        final boolean imActive = (mActivity != null) && (mActivity.getCurrentMenuShown() == MapsIndoorsActivity.MENU_FRAME_SEARCH);
        if( imActive )
        {
            close();

            return false;
        }

        return true;
    }

    @Override
    public void onDrawerEvent( int newState, int prevState ) {}
    //endregion
}
