package com.mapsindoors.stdapp.ui.activitymain;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.mapsindoors.mapssdk.FloorSelectorType;
import com.mapsindoors.mapssdk.Location;
import com.mapsindoors.mapssdk.LocationQuery;
import com.mapsindoors.mapssdk.MPLocationsProvider;
import com.mapsindoors.mapssdk.MapControl;
import com.mapsindoors.mapssdk.MapsIndoors;
import com.mapsindoors.mapssdk.OnLocationsReadyListener;
import com.mapsindoors.mapssdk.PermissionsAndPSListener;
import com.mapsindoors.mapssdk.PositionProvider;
import com.mapsindoors.mapssdk.SolutionInfo;
import com.mapsindoors.mapssdk.dbglog;
import com.mapsindoors.mapssdk.errors.MIError;
import com.mapsindoors.mapssdk.models.AppConfig;
import com.mapsindoors.mapssdk.models.BuildingCollection;
import com.mapsindoors.mapssdk.models.MenuInfo;
import com.mapsindoors.mapssdk.models.Point;
import com.mapsindoors.mapssdk.models.Route;
import com.mapsindoors.mapssdk.models.Solution;
import com.mapsindoors.mapssdk.models.Venue;
import com.mapsindoors.mapssdk.models.VenueCollection;
import com.mapsindoors.stdapp.BuildConfig;
import com.mapsindoors.stdapp.R;
import com.mapsindoors.stdapp.broadcastReceivers.NetworkStateChangeReceiver;
import com.mapsindoors.stdapp.helpers.MapsIndoorsHelper;
import com.mapsindoors.stdapp.helpers.MapsIndoorsSettings;
import com.mapsindoors.stdapp.helpers.MapsIndoorsUtils;
import com.mapsindoors.stdapp.helpers.SharedPrefsHelper;
import com.mapsindoors.stdapp.managers.AppConfigManager;
import com.mapsindoors.stdapp.managers.GoogleAnalyticsManager;
import com.mapsindoors.stdapp.managers.SelectionManager;
import com.mapsindoors.stdapp.models.LastRouteInfo;
import com.mapsindoors.stdapp.positionprovider.PositionProviderAggregator;
import com.mapsindoors.stdapp.positionprovider.helpers.PSUtils;
import com.mapsindoors.stdapp.ui.activitymain.adapters.POIMarkerInfoWindowAdapter;
import com.mapsindoors.stdapp.ui.appInfo.AppInfoFragment;
import com.mapsindoors.stdapp.ui.common.adapters.GenericRecyclerViewAdapter;
import com.mapsindoors.stdapp.ui.common.fragments.BaseFragment;
import com.mapsindoors.stdapp.ui.common.fragments.OverlayFragment;
import com.mapsindoors.stdapp.ui.common.listeners.FloatingActionListener;
import com.mapsindoors.stdapp.ui.common.listeners.MenuListener;
import com.mapsindoors.stdapp.ui.common.models.GenericRecyclerViewListItem;
import com.mapsindoors.stdapp.ui.components.mapcompass.MapCompass;
import com.mapsindoors.stdapp.ui.components.mapfloorselector.MapFloorSelector;
import com.mapsindoors.stdapp.ui.direction.DirectionsHorizontalFragment;
import com.mapsindoors.stdapp.ui.direction.DirectionsVerticalFragment;
import com.mapsindoors.stdapp.ui.fab.FloatingAction;
import com.mapsindoors.stdapp.ui.locationmenu.LocationMenuFragment;
import com.mapsindoors.stdapp.ui.menumain.MenuFragment;
import com.mapsindoors.stdapp.ui.search.SearchFragment;
import com.mapsindoors.stdapp.ui.transportagencies.TransportAgenciesFragment;
import com.mapsindoors.stdapp.ui.venueselector.VenueSelectorFragment;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;



public class MapsIndoorsActivity extends AppCompatActivity
        implements
            GoogleMap.OnMapLongClickListener,
            GoogleMap.OnMarkerClickListener,
            GoogleMap.OnInfoWindowClickListener,
            GoogleMap.OnMapClickListener,
            FloatingActionListener,
            MenuListener,
            MapCompass.OnMapCompassClickedListener
{
    public static final String TAG = MapsIndoorsActivity.class.getSimpleName();

    public static final int MENU_FRAME_MAIN_MENU = 0;
    public static final int MENU_FRAME_VENUE_SELECTOR = 1;
    public static final int MENU_FRAME_LOCATION_MENU = 2;
    public static final int MENU_FRAME_DIRECTIONS_FULL_MENU = 3;
    public static final int MENU_FRAME_SEARCH = 4;
    public static final int MENU_FRAME_TRANSPORT_AGENCIES = 5;
    public static final int MENU_FRAME_APP_INFO = 6;

    public static final int DRAWER_ISTATE_IS_CLOSED = 0;
    public static final int DRAWER_ISTATE_WILL_OPEN = 1;
    public static final int DRAWER_ISTATE_IS_OPEN = 2;
    public static final int DRAWER_ISTATE_WILL_CLOSE = 3;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({INIT_MENU_FROM_INIT, INIT_MENU_FROM_ON_VENUE_SELECTED, INIT_MENU_FROM_MENU_BTN_CLICKED})
    public @interface InitMenuCallers {}

    public static final int INIT_MENU_FROM_INIT                 = 0;
    public static final int INIT_MENU_FROM_ON_VENUE_SELECTED    = 1;
    public static final int INIT_MENU_FROM_MENU_BTN_CLICKED     = 2;


    MapsIndoorsActivity mActivity;
    Solution            mSolution;
    GoogleMap           mGoogleMap; // Might be null if Google Play services APK is not available.
    MapControl          mMapControl;

    // Fragments
    SupportMapFragment mMapFragment;
    ViewFlipper mMenuFrameViewFlipper;
    MenuFragment mMenuFragment;
    VenueSelectorFragment mVenueSelectorFragment;
    DirectionsHorizontalFragment mHorizontalDirectionsFragment;
    DirectionsVerticalFragment mVerticalDirectionsFragment;
    SearchFragment mSearchFragment;
    LocationMenuFragment mLocationMenuFragment;
    TransportAgenciesFragment mTransportAgenciesFragment;
    AppInfoFragment mAppInfoFragment;

    BaseFragment[] mFragments;
    View mSplashLayout;
    View mSplashLayoutView;
    View mNoAvailableNetworkLayout;

    boolean mLocationPermissionGranted;

    AppConfigManager mAppConfigManager;

    protected boolean mMapsIndoorsDataIsReady;
    protected ListView mMainMenuList;
    protected boolean mIsSearching = false;
    protected String pendingSearch;
    protected FloatingAction mFloatingActionButton;
    boolean mMenuInitialized;

    protected PositionProvider mPositionProvider;
    String mQueryCategoryFilter;
    String mQueryTypeFilter;

    private boolean initializeCalled = false;
    protected TopSearchField mTopSearchField;
    SelectionManager mSelectionManager;


    DrawerLayout mDrawerLayout;

    //receivers
    NetworkStateChangeReceiver mNetworkStateChangeReceiver;

    //
    boolean isNetworkReachable;
    /**
     * MapsIndoors external UI component: Floor selector
     */
    MapFloorSelector mMapFloorSelector;
    /**
     * MapsIndoors external UI component: Map compass
     */
    MapCompass mMapCompass;

    /**
     * UI components
     */
    ImageButton mGoToMyLocationButton;


    protected LocationQuery mCSearchQuery;
    protected MPLocationsProvider mCSearchLocationsProvider;

    protected boolean mHasUserChoosenAVenue;
    LastRouteInfo mLastRouteInfo;

    OverlayFragment mOverlayFragment;

    View mBlurEffectView;




    //region Activity lifecycle

    Button mZoomForDetailButton;
    Button mReturnToVenueButton;


    Stack<Integer> mFragmentStack = new Stack<>();


    //
    private Point mDefaultVenueCameraPosition;

    //
    POIMarkerInfoWindowAdapter mPoiMarkerCustomInfoWindowAdapter;


    View bottomMessageLayout;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = this;


        if (dbglog.isDebugMode()) {
            dbglog.Log(TAG, "onCreate()");
        }

        // network broadcast receivers
        setupConnectivityHandler();

        setContentView(R.layout.activity_mapsindoors);


        setupView();

        showLoadingStatus();

        mMapsIndoorsDataIsReady = false;

        // because the first fragment is shown as default
        mFragmentStack.push(0);


        setupActionBar(this);
        // toggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout , mTopSearchField.getToolbarView() , R.string.desc, R.string.desc);

        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (BuildConfig.DEBUG) {
            if (getString(R.string.google_maps_key).equalsIgnoreCase("InsertYourAndroidKeyHere")) {
                Toast.makeText(getApplicationContext(), "WARNING: Google key not found! The map will not work.", Toast.LENGTH_LONG).show();
            }
        }

        //
        setupPositionProvider();
        //
        syncDataTimestamp = System.currentTimeMillis();

        syncData( true );
        //
        setUpMapIfNeeded();


        mMenuInitialized =  false;

    }

    //
    long syncDataTimestamp=0;

    void syncData( boolean invokedFromActOnCreate )
    {
        boolean internetConnectivity = MapsIndoorsUtils.isNetworkReachable(this);

        isNetworkReachable = internetConnectivity;

        // Check for offline data availability BEFORE the first data sync...
        boolean hasOfflineData = MapsIndoors.checkOfflineDataAvailability();

        boolean apiKeyvalidity = SharedPrefsHelper.getApiKeyValidity(MapsIndoorsActivity.this);

        if( !internetConnectivity && !apiKeyvalidity )
        {
            MapsIndoorsUtils.showInvalidAPIKeyDialogue( this );
        }
        else if( hasOfflineData || internetConnectivity )
        {
            boolean runDataSync = false;

            final long timeNow = System.currentTimeMillis();

            if( Math.abs( timeNow - syncDataTimestamp ) > 15000 )
            {
                syncDataTimestamp = timeNow;
                runDataSync = true;
            }

            if( runDataSync )
            {
                //  Once the initial setup is done, we can either trigger a manual data sync
                MapsIndoors.synchronizeContent( error -> {

                    if( error == null )
                    {
                        SharedPrefsHelper.setApiKeyValidity( MapsIndoorsActivity.this, true );

                        if( !invokedFromActOnCreate )
                        {
                            onMIDataSetupReady( null );
                        }
                    }
                    else
                    {
                        if( dbglog.isDebugMode() )
                        {
                            dbglog.LogI( TAG, "MapsIndoors.synchronizeContent -> error: " + error );
                        }

                        if( error.code == MIError.INVALID_API_KEY )
                        {
                            MapsIndoorsUtils.showInvalidAPIKeyDialogue( this );
                        }
                    }
                });
            }
        } else {

            hideSplashScreen();
            showNoAvailableNetworkFragment();

            if (!mMapsIndoorsDataIsReady) {
                if (mTopSearchField != null) {
                    mTopSearchField.setToolbarText(getResources().getString(R.string.app_name), false);
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mMapControl != null) {
            mMapControl.onStart();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        addListeners();

        //
        GoogleAnalyticsManager.reportScreen( getString( R.string.ga_screen_map ), mActivity );

        if (mMapControl != null) {
            mMapControl.onResume();

            boolean networkState = MapsIndoorsUtils.isNetworkReachable(this);
            respondToInternetConnectivityChanges(networkState);
            if (mPositionProvider.isPSEnabled()) {
                startPositioning();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        removeListeners();

        if (mMapControl != null) {
            stopPositioning();

            mMapControl.onPause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mMapControl != null) {
            mMapControl.onStop();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mMapControl != null) {
            mMapControl.onSaveInstanceState(outState);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (mMapControl != null) {
            mMapControl.onSaveInstanceState(savedInstanceState);
        }
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();

        if (mMapControl != null) {
            mMapControl.onLowMemory();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mFetchImagesThread != null) {
            if (mFetchImagesThread.isAlive()) {
                mFetchImagesThread.interrupt();
                mFetchImagesThread = null;
            }
        }

        if( mMapControl != null )
        {
            mMapControl.onDestroy();
            mMapControl = null;
        }

        // Position provider: unregister, etc...
        MapsIndoors.setPositionProvider( null );

        if( mPositionProvider != null )
        {
            mPositionProvider.terminate();
            mPositionProvider = null;
        }

        // Unregister receivers
        if( mNetworkStateChangeReceiver != null )
        {
            unregisterReceiver(mNetworkStateChangeReceiver);
        }
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Detects if the burger menu button at the top bar has been called
        if (item.getItemId() == android.R.id.home) {
            if (isDrawerOpen()) {
               closeDrawer();
            } else {
                openDrawer(true);
            }
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {

        final BaseFragment[] frags = getMyFragments();

        boolean canCallSuper = true;

        // Check if any of the fragments is handling the back press ...
        for (int i = frags.length; --i >= 0; )
        {
            BaseFragment frag = frags[i];
            if ((frag != null) && frag.isFragmentSafe() && !frag.onBackPressed())
            {
                canCallSuper = false;
                break;
            }
        }

        //
        if( canCallSuper )
        {
            MapsIndoorsUtils.showLeavingAlertDialogue( this );
        }
    }
    //endregion


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void addListeners() {
        // Drawer
        if (mDrawerLayout != null) {
            mDrawerLayout.removeDrawerListener(mDrawerListener);
            mDrawerLayout.addDrawerListener(mDrawerListener);
        }

        // ...
    }

    private void removeListeners() {
        // Drawer
        if (mDrawerLayout != null) {
            mDrawerLayout.removeDrawerListener(mDrawerListener);
        }

        // ...
    }


    private void setupView()
    {
        // Main fragments
        FragmentManager fm = getSupportFragmentManager();

        mMapFragment = (SupportMapFragment) fm.findFragmentById(R.id.mapfragment);
        mVenueSelectorFragment = (VenueSelectorFragment) fm.findFragmentById(R.id.venue_selector_fragment);
        mHorizontalDirectionsFragment = (DirectionsHorizontalFragment) fm.findFragmentById(R.id.directionsmenufragment);
        mLocationMenuFragment = (LocationMenuFragment) fm.findFragmentById(R.id.locationmenufragment);
        mVerticalDirectionsFragment = (DirectionsVerticalFragment) fm.findFragmentById(R.id.directionsfullmenufragment);
        mSearchFragment = (SearchFragment) fm.findFragmentById(R.id.directionsfullmenuSearchfragment);

        mTransportAgenciesFragment = (TransportAgenciesFragment) fm.findFragmentById(R.id.transport_sources_fragment);
        mAppInfoFragment = (AppInfoFragment) fm.findFragmentById(R.id.app_info_fragment);
        mOverlayFragment = (OverlayFragment)  fm.findFragmentById(R.id.overlayfragment);

        mDrawerLayout = findViewById(R.id.main_drawer);

        // Initialize the main view flipper
        mMenuFrameViewFlipper = findViewById(R.id.menuframe_viewflipper);

        mMapFloorSelector = findViewById(R.id.mp_floor_selector);
        mMapCompass = findViewById(R.id.mp_map_compass);

        mNoAvailableNetworkLayout = findViewById(R.id.no_available_network_fragment_layout);
        mGoToMyLocationButton = findViewById(R.id.mp_goto_mylocation_button );

        mBlurEffectView = findViewById(R.id.blur_effect_view);

        bottomMessageLayout = findViewById(R.id.main_activity_boottom_message);

        mBlurEffectView.setOnClickListener( view -> {
            if (mFloatingActionButton != null) {
                mFloatingActionButton.close();
            }
        } );

        //
        mZoomForDetailButton = findViewById(R.id.zoom_for_detail_button);
        mZoomForDetailButton.setOnClickListener( view -> {

            if( (mGoogleMap != null) && (mDefaultVenueCameraPosition != null)  )
            {
                final CameraPosition currentCameraPosition = mGoogleMap.getCameraPosition();

                final LatLng defaultCameraPosition = mDefaultVenueCameraPosition.getLatLng();

                final CameraPosition newCameraPosition = new CameraPosition.Builder().
                        target( defaultCameraPosition ).
                        tilt( currentCameraPosition.tilt ).
                        zoom( MapsIndoorsSettings.VENUE_TILE_LAYER_VISIBLE_START_ZOOM_LEVEL ).
                        bearing( currentCameraPosition.bearing ).
                        build();

                mGoogleMap.animateCamera( CameraUpdateFactory.newCameraPosition( newCameraPosition ) );
            }

            setZoomForDetailInvisible();
        });

        mReturnToVenueButton = findViewById(R.id.return_to_venue_button);
        mReturnToVenueButton.setOnClickListener( view -> {
            mSelectionManager.selectLastSelection();
            setReturnToVenueInvisible();
        });

        if( SharedPrefsHelper.isZoomForDetailButtonToShow( this ) )
        {
            setZoomForDetailVisible();
        }

        //
    /*    if( !PSUtils.isLocationServiceEnabled( this ) || !mLocationPermissionGranted )
        {
          //  mGoToMyLocationButton.setImageResource( R.drawable.ic_my_location_inactive );
        }*/

        mGoToMyLocationButton.setOnClickListener(view -> {

            GoogleAnalyticsManager.reportEvent( getString( R.string.fir_event_Map_Blue_Dot_clicked ), null );

            mPositionProvider.checkPermissionsAndPSEnabled( new PermissionsAndPSListener() {
                @Override
                public void onPermissionDenied() {
                    mLocationPermissionGranted = false;

                    mGoToMyLocationButton.setImageResource(R.drawable.ic_my_location_inactive);


                }

                @Override
                public void onPermissionGranted() {

                }

                @Override
                public void onGPSPermissionAndServiceEnabled() {
                    // could be the gps already running but the permission was not granted
                    mGoToMyLocationButton.setImageResource(R.drawable.ic_my_location_active);
                    mLocationPermissionGranted = true;

                    //
                    startPositioning();
                    mMapControl.showUserPosition(true);

                    //
                    if (mGoogleMap != null) {
                        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(getCurrentPos().getLatLng()));
                    }
                }

                @Override

                public void onPermissionRequestError() {

                }
            });
        });
    }

    public void resetMapToInitialState()
    {
        if( mMapControl != null )
        {
            mMapControl.deSelectLocation();
        }

        mSelectionManager.selectCurrentVenue();
    }

    /**
     * Clears any route/search results being shown and sets the side menu (drawer) to its defaults (category list)
     */
    @SuppressWarnings("WeakerAccess")
    public void OnToolbarCloseButtonClicked()
    {
        setOpenLocationMenuFromInfowindowClick( true );

        if( mVerticalDirectionsFragment != null )
        {
            mVerticalDirectionsFragment.closeRouting();
        }

        invalidateLastRoute();

        resetMapToInitialState();

        // go to main menu fragment and reset it
        if( mMenuFragment != null )
        {
            boolean drawerIsOpen = isDrawerOpen();
            boolean animateMenuChange = !drawerIsOpen;
            mMenuFragment.mIsOpenedFromBackpress = false;
            menuGoTo( MENU_FRAME_MAIN_MENU, animateMenuChange );
        }
    }

    public void horizontalDirectionsPanelWillOpen()
    {
        if( mFloatingActionButton != null )
        {
            mFloatingActionButton.setActive( false );
        }
    }

    public void horizontalDirectionsPanelWillClose()
    {
        if( mFloatingActionButton != null )
        {
            mFloatingActionButton.setActive( true );
        }
    }

    @Nullable
    public VenueCollection getVenueCollection() {
        return MapsIndoors.getVenues();
    }

    @Nullable
    public BuildingCollection getBuildingCollection() {
        return MapsIndoors.getBuildings();
    }

    @Nullable
    public String getCurrentVenueName()
    {
        VenueCollection vc = getVenueCollection();

        if( (vc != null) && (vc.getCurrentVenue() != null) )
        {
            return vc.getCurrentVenue().getVenueInfo().getName();
        }

        return null;
    }

    @Nullable
    public String getCurrentVenueId()
    {
        VenueCollection vc = getVenueCollection();

        if( (vc != null) && (vc.getCurrentVenue() != null) )
        {
            return vc.getCurrentVenue().getVenueId();
        }

        return null;
    }

    protected void initializeMenu( @InitMenuCallers final int callerId )
    {
        if (dbglog.isDebugMode()) {
            dbglog.Log(TAG, "initializeMenu() - mMenuInitialized: " + mMenuInitialized);
        }

        if (mMenuInitialized) {
            return;
        }

        if (dbglog.isDebugMode()) {
            dbglog.Log(TAG, "initializeMenu() - mMenuFragment is initialized: " + ((mMenuFragment != null) ? "YES" : "NO"));
        }

        if (mMenuFragment == null)
        {
            FragmentManager fm = getSupportFragmentManager();

            mMenuFragment = (MenuFragment) fm.findFragmentById(R.id.menufragment);

            if (dbglog.isDebugMode()) {
                dbglog.Log(TAG, "initializeMenu() - mMenuFragment is available: " + ((mMenuFragment != null) ? "YES" : "NO"));
            }

            if (mMenuFragment == null) {
                return;
            }

            mMenuFragment.init(this, this, mMapControl);

            mVenueSelectorFragment.init(this, this, mMapControl, getVenueCollection());
            mTransportAgenciesFragment.init(this, this, mMapControl);
            mHorizontalDirectionsFragment.init(this, mGoogleMap);
            mAppInfoFragment.init(this);

            // Set the first fragment to be shown the first time the drawer is open
            mHasUserChoosenAVenue = SharedPrefsHelper.hasUserChoosenAVenue( this );
            String currAppVersion = SharedPrefsHelper.getAppVersionName(this);

            if (TextUtils.isEmpty(currAppVersion)) {
                if (dbglog.isDebugMode()) {
                    dbglog.Log(TAG, "\n######################\n - Installed a new version (fresh, shared prefs absent)\n    OR\n - App's data has been manually deleted\n######################");
                }
                SharedPrefsHelper.setAppVersionName(this, BuildConfig.VERSION_NAME);
            } else {
                if (!currAppVersion.equalsIgnoreCase(BuildConfig.VERSION_NAME)) {

                    if (dbglog.isDebugMode()) {
                        dbglog.Log(TAG, "\n######################\nUpdated app, shared prefs is present\nIN THIS CASE, WE MUST CLEAN UP ANY APP CACHES\n######################");
                    }

                    {
                        // =================================================================
                        // IN THIS CASE, WE MUST CLEAN UP ANY APP CACHES
                        // THIS SHOULD BE DONE BEFORE INITIALIZING MAP CONTROL!!!
                        // =================================================================
                    }

                    SharedPrefsHelper.setAppVersionName(this, BuildConfig.VERSION_NAME);
                }
            }


            List<Venue> venuesList = getVenueCollection().getVenues();

            if( venuesList.size() == 1 )
            {
                onMenuVenueSelect( venuesList.get( 0 ).getVenueId() );

                mActivity.menuGoTo( MapsIndoorsActivity.MENU_FRAME_MAIN_MENU, true );
                mVenueSelectorFragment.setToolbarBackAndVenueButtonVisibility( true );
                mMenuFragment.setVenueSelectorButtonshouldBeShown( false );

                SharedPrefsHelper.setUserHasChoosenVenue( this, true );
            }
            else
            {
                if( mHasUserChoosenAVenue )
                {
                    menuGoTo( MENU_FRAME_MAIN_MENU, false );
                    mVenueSelectorFragment.setToolbarBackAndVenueButtonVisibility( true );
                }
                else
                {
                    menuGoTo( MENU_FRAME_VENUE_SELECTOR, false );
                    mVenueSelectorFragment.setToolbarBackAndVenueButtonVisibility( false );
                }
            }
        }

        if (dbglog.isDebugMode()) {
            dbglog.Log(TAG, "initializeMenu() - mDataReady: " + mMapsIndoorsDataIsReady);
        }

        if( mMapsIndoorsDataIsReady )
        {
            if( !mAppConfigManager.isFabMenuHidden() )
            {
                mFloatingActionButton = new FloatingAction(
                        this,
                        this,
                        mAppConfigManager,
                        findViewById( R.id.float_button_fragment )
                );
            }

            runOnUiThread( () -> {

                mMenuFragment.initMenu(
                        mSolution,
                        mAppConfigManager,
                        getVenueCollection(),
                        callerId == INIT_MENU_FROM_INIT
                );

                if (dbglog.isDebugMode()) {
                    dbglog.Log(TAG, "initializeMenu() - mMainMenuList got something: " + ((mMainMenuList != null) ? "YES" : "NO"));
                }

                if (callerId != INIT_MENU_FROM_ON_VENUE_SELECTED) {
                    mVenueSelectorFragment.onDataReady();
                }

                mMenuInitialized = true;

                if (callerId != INIT_MENU_FROM_ON_VENUE_SELECTED) {
                    hideSplashScreen();

                    enableMenu();

                    // Open the drawer with the venue selector if this is the first time the app runs after
                    // an install
                    if (!mHasUserChoosenAVenue && isNetworkReachable) {
                        openDrawer(true);
                    }

                    if (mLocationPermissionGranted && PSUtils.isLocationServiceEnabled(mActivity)) {
                        startPositioning();
                    }

                    if(! isNetworkReachable){
                        showMapSnackBar();

                    }
                }
            });
        }
    }

    public void openDrawer( boolean animated )
    {
        if( !isDrawerOpen() )
        {
            runOnUiThread( () -> {
                mDrawerLayout.setClickable( true );
                mDrawerLayout.openDrawer( GravityCompat.START, animated );
            });
        }
    }



    //region "Splash screen"

    protected CountDownTimer mSplashTextCountDownTimer;

    private void showLoadingStatus()
    {
        mSplashLayout = findViewById(R.id.splashscreenfragment);

        mHasUserChoosenAVenue = false;

        if (mSplashLayout != null) {

            mSplashLayout.setVisibility(View.VISIBLE);
            mSplashLayout.setAlpha(1f);

            mSplashLayout.findViewById(R.id.splash_icon).setVisibility(View.VISIBLE);
            mSplashLayout.findViewById(R.id.splash_icon_2).setVisibility(View.GONE);

            mSplashLayoutView = mSplashLayout.findViewById(R.id.splash_main);
            mSplashLayoutView.setAlpha(1f);


            mSplashLayout.findViewById(R.id.splash_progressbar).setVisibility(View.VISIBLE);

            TextView splashTextStatus = mSplashLayout.findViewById(R.id.splash_text_status);
            splashTextStatus.setVisibility(View.VISIBLE);

            splashTextStatus.setText(getString(R.string.splash_screen_status));

            restartLoadingStatus();
        }
    }

    private void restartLoadingStatus()
    {
        // Drawer
        resetDrawer();
    }

    void hideSplashScreen()
    {
        if( dbglog.isDebugMode() )
        {
            dbglog.Log( TAG, "hideSplashScreen()" );
        }

        if( mSplashLayout.getVisibility() != View.GONE )
        {
            ViewCompat.animate( mSplashLayout ).
                    alpha( 0f ).
                    setDuration( 500 ).
                    setListener( mSplashLayoutAlphaAnimationListener ).
                    // Maybe "too much" but we need some delay before starting the animation...
                    // We basically give some time to Mr. GC to do its work, if any
                    setStartDelay( 1000 );
        }
    }

    ViewPropertyAnimatorListener mSplashLayoutAlphaAnimationListener = new ViewPropertyAnimatorListener() {
        @Override
        public void onAnimationStart( View view ) {}

        @Override
        public void onAnimationEnd( View view )
        {
            onSplashScreenAnimationEnded( view );
        }

        @Override
        public void onAnimationCancel( View view ) {}
    };

    void onSplashScreenAnimationEnded( View view )
    {
        // Stop the loading text animation timer and clear it
        if( mSplashTextCountDownTimer != null )
        {
            mSplashTextCountDownTimer.cancel();
            mSplashTextCountDownTimer = null;
        }

        // Disable the splash screen fragment
        view.setVisibility(View.GONE);
    }
    //endregion


    //region FRAGMENTS

    @Nullable
    public TransportAgenciesFragment getTransportAgenciesFragment() {
        return mTransportAgenciesFragment;
    }

    @Nullable
    public LocationMenuFragment getLocationMenuFragment() {
        return mLocationMenuFragment;
    }

    @Nullable
    public DirectionsHorizontalFragment getHorizontalDirectionsFragment() {
        return mHorizontalDirectionsFragment;
    }

    @Nullable
    public DirectionsVerticalFragment getVerticalDirectionsFragment() {
        return mVerticalDirectionsFragment;
    }

    @Nullable
    public MenuFragment getMenuFragment() {
        return mMenuFragment;
    }

    @Nullable
    public SearchFragment getDirectionsFullMenuSearchFragment() {
        return mSearchFragment;
    }

    @Nullable
    public SupportMapFragment getGMapFragment() {
        return mMapFragment;
    }
    //endregion


    //region Menu Frame ViewFlipper
    public void menuGoTo( int index, boolean animate )
    {
        if( mMenuFrameViewFlipper == null )
        {
            return;
        }

        if( animate )
        {
            mMenuFrameViewFlipper.setInAnimation( this, R.anim.menu_flipper_fade_in );
            mMenuFrameViewFlipper.setOutAnimation( this, R.anim.menu_flipper_fade_out );

            //When it still on the menuFragment and you want to go back to the categ menu this call is needed otherwise it will stay in the locations menu
            if( mMenuFragment != null )
            {
                switch( index )
                {
                    case MENU_FRAME_MAIN_MENU:
                        mMenuFragment.initMenu( mSolution, mAppConfigManager, getVenueCollection(), false );
                        break;
                    case MENU_FRAME_LOCATION_MENU:
                        mMenuFragment.closeKeyboard();
                        break;
                    case MENU_FRAME_DIRECTIONS_FULL_MENU:
                        break;
                }
            }
        }

        if( index != getCurrentMenuShown() )
        {
            mFragmentStack.push( index );
        }

        showFragment(index);
    }

    public void showFragment( int index )
    {
        if( mMenuFrameViewFlipper == null )
        {
            return;
        }

        mMenuFrameViewFlipper.setDisplayedChild( index );

        if( isDrawerOpen() )
        {
            reportDrawerMenuToGA( index );
        }
    }

    public int getCurrentMenuShown()
    {
        if( mMenuFrameViewFlipper != null )
        {
            return mMenuFrameViewFlipper.getDisplayedChild();
        }

        return MENU_FRAME_MAIN_MENU;
    }

    public void menuGoBack()
    {
        // to avoid crash when clicking multiple times on the backbutton
        try {
            mFragmentStack.pop();

            int index = mFragmentStack.peek();

            switch (index)
            {
                case MENU_FRAME_MAIN_MENU:
                    mMenuFragment.mIsOpenedFromBackpress = true;
                    showFragment(index);
                    break;
                case MENU_FRAME_SEARCH:
                    mSearchFragment.mOpenedFromBackPress = true;
                    mSearchFragment.setActive(true);

                    break;
                default:
                    showFragment(index);
            }
        }
        catch (Exception ex) {}
    }

    public void reportCurrentMenuToGA() {
        reportDrawerMenuToGA(getCurrentMenuShown());
    }

    private void reportDrawerMenuToGA(int menuIndx) {
        int strId;
        switch (menuIndx) {
            case MapsIndoorsActivity.MENU_FRAME_MAIN_MENU:
                strId = R.string.ga_screen_main_menu;
                break;

            case MapsIndoorsActivity.MENU_FRAME_VENUE_SELECTOR:
                strId = R.string.ga_screen_venues;
                break;
            case MapsIndoorsActivity.MENU_FRAME_LOCATION_MENU:
                strId = R.string.ga_screen_details;
                break;
            case MapsIndoorsActivity.MENU_FRAME_DIRECTIONS_FULL_MENU:
                strId = R.string.ga_screen_directions;
                break;
            case MapsIndoorsActivity.MENU_FRAME_SEARCH:
                strId = R.string.ga_screen_select_place;
                break;
            case MapsIndoorsActivity.MENU_FRAME_TRANSPORT_AGENCIES:
                strId = R.string.ga_screen_transit_sources;
                break;
            default:
                strId = 0;
        }

        if (strId != 0) {
            GoogleAnalyticsManager.reportScreen(getString(strId), mActivity);
        }
    }
    //endregion


    private void setupActionBar(MapsIndoorsActivity activity) {
        ActionBar ac = getActionBar();
        if (ac != null) {
            ac.setHomeButtonEnabled(true);
        }

        mTopSearchField = new TopSearchField(activity);
        mTopSearchField.setCloseButtonClickListener( view -> OnToolbarCloseButtonClicked() );

        Toolbar toolbarView = mTopSearchField.getToolbarView();
        if (toolbarView != null) {
            setSupportActionBar(toolbarView);

            toolbarView.setNavigationOnClickListener( view -> {

                if (isDrawerOpen()) {
                    openDrawer(true);
                } else {
                    initializeMenu(INIT_MENU_FROM_MENU_BTN_CLICKED);
                }
            });
        }
    }

    void setUpMapIfNeeded()
    {
        if( dbglog.isDebugMode() )
        {
            dbglog.Log( TAG, "setUpMapIfNeeded() - initializeCalled: " + initializeCalled );
        }

        if( initializeCalled )
        {
            return;
        }

        boolean doWork = false;

        // Do a null check to confirm that we have not already instantiated the map.
        if( mGoogleMap == null )
        {
            initializeCalled = true;
            doWork = true;
        }

        if( dbglog.isDebugMode() )
        {
            dbglog.Log( TAG, "setUpMapIfNeeded() - doWork: " + doWork );
        }

        if( doWork )
        {

            if( dbglog.isDebugMode() )
            {
                dbglog.Log( TAG, "setUpMapIfNeeded() - TP1" );
            }
            mMapFragment.getMapAsync( mOnMapReadyCallback );
        }
        else
        {
            setupMapControl();
        }
    }

    void setupPositionProvider()
    {
        // Add a position provider to track the user's position.
        if( mPositionProvider == null )
        {
            PositionProviderAggregator posProviderAggregator = new PositionProviderAggregator( mActivity );

            if(posProviderAggregator.getPositionProviders().size() > 0){
                mPositionProvider = posProviderAggregator.getPositionProviders().get(0);
            }

            // Set the position provider
            MapsIndoors.setPositionProvider( mPositionProvider );

            mPositionProvider.addOnstateChangedListener( this::onPositionProviderStateChanged );

            mPositionProvider.checkPermissionsAndPSEnabled( new PermissionsAndPSListener() {
                @Override
                public void onPermissionDenied()
                {
                    mLocationPermissionGranted = false;
                }

                @Override
                public void onPermissionGranted()
                {
                    mLocationPermissionGranted = true;
                }

                @Override
                public void onGPSPermissionAndServiceEnabled()
                {
                    if( mGoToMyLocationButton != null )
                    {
                        mGoToMyLocationButton.setImageResource( R.drawable.ic_my_location_active );
                    }

                    startPositioning();
                }

                @Override
                public void onPermissionRequestError() {}
            });
        }
    }

    //region MAPSINDOORS LIBRARY SETUP + GOOGLE MAPS SETUP
    OnMapReadyCallback mOnMapReadyCallback = new OnMapReadyCallback()
    {
        @Override
        public void onMapReady( GoogleMap googleMap )
        {
            if (dbglog.isDebugMode()) {
                dbglog.Log(TAG, "setUpMapIfNeeded() - TP2 - GMAP: " + googleMap);
            }

            mGoogleMap = googleMap;

            if (mGoogleMap == null) {
                Toast.makeText(getApplicationContext(), "Unable to open Google map. Unable to continue", Toast.LENGTH_LONG).show();
                return;
            }

            try {
                //For customizing styles tweak res/raw/google_maps_styles_style.json  https://mapstyle.withgoogle.com/
                boolean success = mGoogleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(mActivity, R.raw.google_maps_style));
                if (!success) {
                    if (dbglog.isDebugMode()) {
                        dbglog.LogE(TAG, "Style parsing failed.");
                    }
                }
            } catch (Resources.NotFoundException e) {
                if (dbglog.isDebugMode()) {
                    dbglog.LogE(TAG, "Style parsing failed: " + e);
                }
            }

            setupMapControl();
        }
    };

    void setupMapsIndoorsContentLanguage()
    {
        final String deviceLanguage = MapsIndoorsUtils.getDeviceDefaultLocale( this.getResources() ).getLanguage();

        //
        final SolutionInfo solutionInfo = MapsIndoors.getSolutionInfo();
        if( solutionInfo != null )
        {
            // Get the MI MISolution supported languages
            List< String > solutionAvailableLanguages = solutionInfo.getAvailableLanguages();

            // ========================================================================
            // This will work as long as we add string locales matching 1:1 what we have in
            // the solution
            // ========================================================================
            for( String solutionLang : solutionAvailableLanguages )
            {
                if( deviceLanguage.equalsIgnoreCase( solutionLang ) )
                {
                    boolean miSDKNewLanguageHasBeenSetSuccessfully = MapsIndoors.setLanguage( solutionLang );
                    break;
                }
            }
        }
    }

    void setupMapControl()
    {
        if(BuildConfig.DEBUG)
        {
            if(dbglog.isDebugMode())
            {
                dbglog.Assert( mMapControl == null, "MapControl has already been instantiated!!!" );
            }
        }

        // Creates a new MapControl instance
        mMapControl = new MapControl( mActivity, mMapFragment, mGoogleMap );

        // init the selection manager
        mSelectionManager = new SelectionManager(this, mMapControl, mTopSearchField);
        //
        mPoiMarkerCustomInfoWindowAdapter = new POIMarkerInfoWindowAdapter(this, mMapControl);
        mMapControl.setInfoWindowAdapter(mPoiMarkerCustomInfoWindowAdapter);

        finishMapsIndoorsSetup();
    }

    void finishMapsIndoorsSetup()
    {
        mMapControl.showBuildingOutline( true );
        mMapControl.showUserPosition( true );
        mMapControl.setOnMarkerClickListener( mActivity );
        mMapControl.setOnMarkerInfoWindowClickListener( mActivity );

        setupMIUIComponents();

        // ==========================================================
        // Set the listeners you want to use here
        //
        // Note that CAMERA EVENT LISTENERS MUST BE SET ON THE MAP CONTROL OBJECT, NOT DIRECTLY
        // ON GOOGLE MAP.
        // Mapindoors's MapControl forwards them.
        //
        mGoogleMap.setOnMapLongClickListener( mActivity );
        mMapControl.setOnMapClickListener( mActivity );

        mMapControl.addOnCameraIdleListener( () -> {

            mMapCompass.updateFromCameraEvent( MapCompass.EVENT_CAMERA_IDLE, 0 );

            CameraPosition pos = mGoogleMap.getCameraPosition();

            if( SharedPrefsHelper.isZoomForDetailButtonToShow( MapsIndoorsActivity.this ) )
            {
                if( pos.zoom >= (MapsIndoorsSettings.VENUE_TILE_LAYER_VISIBLE_START_ZOOM_LEVEL) )
                {
                    setZoomForDetailInvisible();
                }
            }
        } );

        mMapControl.setOnCurrentBuildingChangedListener( building -> {
            boolean zoomButtonToShow = SharedPrefsHelper.isZoomForDetailButtonToShow(MapsIndoorsActivity.this);
            if( building != null )
            {
                if( zoomButtonToShow )
                {
                    setZoomForDetailVisible();
                }

                setReturnToVenueInvisible();
            }
            else
            {
                if( zoomButtonToShow )
                {
                    setZoomForDetailInvisible();
                }

                setReturnToVenueVisible();
            }
        });

        mMapControl.showUserPosition( true );

        setupMapsIndoorsContentLanguage();

        // Initialize MapControl: among other tasks, it will load/update the solution data, if needed
        mMapControl.init( this::onMIDataSetupReady );
    }

	Venue mDefaultVenue;
	Thread mFetchImagesThread;

	void onMIDataSetupReady( final MIError error )
    {
		if (dbglog.isDebugMode()) {
			dbglog.Log(TAG, "## onLoadingDataReady()");
		}

		if (isActivityFinishing(this)) {
			return;
		}

		MIError iError = error;

        if( error == null )
		{
			// ===============================================================< NON-UI thread
            if( mSolution == null )
            {
                mSolution = MapsIndoors.getSolution();

                // Create the app config manager
                if( mAppConfigManager == null )
                {
                    final AppConfig ac = MapsIndoors.getAppConfig();
                    if( ac != null )
                    {
                        mAppConfigManager = new AppConfigManager( mMapControl );

                    }
                    else
                    {
                        iError = new MIError( MIError.DATALOADER_APPCONFIG_UNKNOWN_ERROR );
                    }

                    if( mAppConfigManager != null )
                    {
                        mFetchImagesThread = mAppConfigManager.getUIAssets( this, this::finishAppInit );

                        if( BuildConfig.DEBUG )
                        {
                            mFetchImagesThread.setName( "MI_Fetch_Images" );
                        }

                        mFetchImagesThread.start();
                    }
                    else
                    {
                        iError = new MIError( MIError.DATALOADER_APPCONFIG_UNKNOWN_ERROR );
                    }
                }
            }
		}

        if( iError != null )
		{
            if( dbglog.isDebugMode() )
            {
                final @MIError.MISDKErrorCode int errorCode = iError.code;
                final String errorMessage = iError.message;

				new Handler(getMainLooper()).post( () -> {

					Toast.makeText(
					        getApplicationContext(),
                            "onLoadingDataReady() - ERROR: code <" + errorCode +">, message <" + errorMessage + ">",
                            Toast.LENGTH_SHORT
                    ).show();
				});
			}

			//
            if( iError.code == MIError.INVALID_API_KEY )
            {
                MapsIndoorsUtils.showInvalidAPIKeyDialogue(this);
            }
		}
	}

    void finishAppInit()
    {
		mCSearchLocationsProvider = new MPLocationsProvider();
		mDefaultVenue = null;

        String savedVenueId = SharedPrefsHelper.getCurrentVenueId( getApplicationContext() );

        VenueCollection vc = getVenueCollection();
        if( vc != null )
        {
            if( !TextUtils.isEmpty( savedVenueId ) )
            {
                mDefaultVenue = vc.getVenueById( savedVenueId );
                if( mDefaultVenue != null )
                {
                    vc.selectVenue( savedVenueId );
                }
            }

            if( mDefaultVenue == null )
            {
                mDefaultVenue = vc.getDefaultVenue();
                if( mDefaultVenue != null )
                {
                    mDefaultVenueCameraPosition = mDefaultVenue.getPosition();
                }

                if( BuildConfig.DEBUG )
                {
                    if( dbglog.isDebugMode() )
                    {
                        dbglog.Assert( mDefaultVenue != null, "Default venue is NULL!" );
                    }
                }

                if( mDefaultVenue != null )
                {
                    SharedPrefsHelper.setCurrentVenueId( getApplicationContext(), mDefaultVenue.getVenueId() );
                }
            }
        }

		// ===============================================================< UI thread
        runOnUiThread( () -> {

			if (mDefaultVenue != null) {
				// Sets the name to the default venue
				mSelectionManager.setCurrentVenue(mDefaultVenue);
			}

			//
			if (dbglog.isDebugMode()) {
				dbglog.Log(TAG, "onDataReady() - will invoke initializeMenu() - TP2");
			}

			mMapsIndoorsDataIsReady = true;

			initializeMenu(INIT_MENU_FROM_INIT);
		} );
	}
	//endregion


    //region Implements MenuListener

    @Override
    public void onMenuVenueSelect(String venueId) {

        VenueCollection vc = getVenueCollection();

        if ((vc != null) && vc.selectVenue(venueId)) {

            // Remember the selected venue
            SharedPrefsHelper.setCurrentVenueId(getApplicationContext(), venueId);

            // Reinitialize the menu with the new venue settings
            mMenuInitialized = false;

            // Retrieve the current venue just set
            Venue cVenue = vc.getCurrentVenue();

            if (cVenue != null) {
                // Sets the menu's title to the current venue
                mSelectionManager.setCurrentVenue(cVenue);
                // save the venue position
                mDefaultVenueCameraPosition = cVenue.getPosition();

                // Get the current venue's default floor
                final int venueDefaultFloor = cVenue.getDefaultFloor();

            } else if (dbglog.isDebugMode()) {
                dbglog.LogE(TAG, "onMenuVenueSelect can not set/get venue with id " + venueId);
            }

        }
    }

    @Override
    public void onMenuShowLocation( Location location )
    {
        GoogleAnalyticsManager.reportEvent( getString( R.string.fir_event_Map_Fab_Opened ), null );

        mSelectionManager.selectLocation( location, true, true );
    }

    //Callback from the menu stating that the user wants to search for locations containing a given name.
    //If no name is given we should list the location types instead
    @Override
    public void onMenuSearch(String searchString, boolean finalSearch) {}

    @Override
    public void onMenuSelect(Object selected, int objViewType)
    {
        switch (objViewType)
        {
            case GenericRecyclerViewAdapter.VIEWTYPE_LOCATION: {
                //A specific location is selected. Get detailed data from it and launch the POI detail menu
                Location location = (Location) selected;

                mMenuFragment.openLocationMenu(location);
                break;
            }

            case GenericRecyclerViewAdapter.VIEWTYPE_CATEGORY: {

                MenuInfo selectedMenuInfo = (MenuInfo) selected;

                final String categoryKey = selectedMenuInfo.getCategoryKey();
                final String categoryName = selectedMenuInfo.getName();

                // A location type is selected. Find all locations with that category.
                setLocationQueryTypeFilter(null);
                setLocationQueryCategoryFilter(categoryKey);

                // Change the toolbar title with the category name
                mTopSearchField.setToolbarText(categoryName, true);

                findLocations("", false, false);

                mMenuFragment.setMenuModeToCategory(false);
                mMenuFragment.setSearchBoxHint(categoryName);

                GoogleAnalyticsManager.reportScreen(categoryName, mActivity);
                break;
            }
        }
    }
    //endregion


    public Point getCurrentPos()
    {
        //If the latest position is at 0,0 it's not updated yet. Using the camera position instead.
        //If you want a default position before we get any signals from our position providers, use setCurrentPosition() on mapscontrol
        return mMapControl.getCurrentPosition().getPoint();
    }


    /**
     * Finds and shows locations based on a search string and optionally with optional type and category filters (can be null)
     *
     * @param searchString      Search string (typed in by the user, etc.)
     * @param preferOnline      TEMPORARY, TO BE REMOVED ONCE THE LOCAL SEARCH IS WORKING AS ITS SERVER VERSION
     * @param searchInAllVenues True if ...
     */
    public void findLocations( String searchString, boolean preferOnline, boolean searchInAllVenues )
    {
        Venue currentVenue = (getVenueCollection() != null) ? getVenueCollection().getCurrentVenue() : null;

        final LocationQuery.Builder queryBuilder = new LocationQuery.Builder();

        // ATM, manual search is performed on the backend only (proper offline search not implemented yet)
        queryBuilder.
                setQueryMode(preferOnline ? LocationQuery.MODE_ONLY_ONLINE : LocationQuery.MODE_ONLY_OFFLINE).
                setOrderBy(LocationQuery.RELEVANCE).
                setMaxResults(MapsIndoorsSettings.INDOOR_LOCATIONS_QUERY_RESULT_MAX_LENGTH).
                setNear((currentVenue != null) ? currentVenue.getPosition() : null);

        if (!TextUtils.isEmpty(searchString)) {
            queryBuilder.setQuery(searchString);
        } else {
            // In our case, we just want them all...
            queryBuilder.setNear(null);
        }

        if (mQueryTypeFilter != null) {
            queryBuilder.setTypes(Collections.singletonList(mQueryTypeFilter));
        }

        if (mQueryCategoryFilter != null) {
            queryBuilder.
                    setCategories(Collections.singletonList(mQueryCategoryFilter)).
                    setVenue( (searchInAllVenues || (currentVenue == null)) ? LocationQuery.VENUE_ALL : currentVenue.getName() );
        }

        if( !mIsSearching )
        {
            mIsSearching = true;

            mCSearchQuery = queryBuilder.build();

            mCSearchLocationsProvider.getLocationsAsync( mCSearchQuery, mSearchLocationsReadyListener );
        } else {
            // Already waiting for a response. Store the query for later use.
            pendingSearch = searchString;
        }
    }

    OnLocationsReadyListener mSearchLocationsReadyListener = new OnLocationsReadyListener() {
        @Override
        public void onLocationsReady(final List<Location> locations, @Nullable MIError error ) {

            if (!mIsSearching) {
                return;
            }

            if( error != null )
            {
                if(error.code == MIError.INVALID_API_KEY){
                    MapsIndoorsUtils.showInvalidAPIKeyDialogue(MapsIndoorsActivity.this);
                }
            }else {

                // Report to our Analytics
                {
                    @LocationQuery.QueryMode int qMode = mCSearchQuery.getQueryMode();

                    if ((qMode == LocationQuery.MODE_ONLY_ONLINE) || (qMode == LocationQuery.MODE_PREFER_ONLINE)) {
                        int locationCount = (locations != null)?locations.size(): 0;
                        // analytics
                        GoogleAnalyticsManager.reportSearch(mCSearchQuery,locationCount );
                    }
                }

                final List<GenericRecyclerViewListItem> elements = mMenuFragment.getCleanItemList();


                //Get the latest position from our position providers made for mapcontrol earlier.
                Point from = mMapControl.getCurrentPosition().getPoint();

                //If the latest position is at 0,0 it's not updated yet. Using the camera position instead.
                //If you want a default position before we get any signals from our position providers, use setCurrentPosition() on MapControl.
                if ((Double.compare(from.getLat(), 0) == 0) && (Double.compare(from.getLng(), 0) == 0)) {
                    LatLng target = mGoogleMap.getCameraPosition().target;
                    from = new Point(target.latitude, target.longitude);
                }

                if( locations != null )
                {
                    //runOnUiThread( () -> {
                        // Select the search result
                        mSelectionManager.selectSearchResult(locations);
                    //});

                    final Context context = getApplicationContext();
                    VenueCollection venueCollection = getVenueCollection();
                    BuildingCollection buildingCollection = getBuildingCollection();
                    AppConfigManager acm = mAppConfigManager;

                    for (Location location : locations) {

                        if (location != null) {

                            final String subText = MapsIndoorsHelper.composeLocationInfoString(context, location, venueCollection, buildingCollection);

                            if (subText != null) {

                                double airDistance = location.getPoint().distanceTo(from);

                                final String typeName = location.getType();
                                Bitmap bm = acm.getPOITypeIcon(typeName);

                                if (BuildConfig.DEBUG) {
                                    if ((bm != null) && MapsIndoorsUtils.checkIfBitmapIsEmpty(bm)) {
                                        dbglog.Log(TAG, "Activity mSearchLocationsReadyListener.onLocationsReady: mLocationTypeImages bm IS EMPTY for " + typeName);
                                    }
                                }

                                if (bm != null) {
                                    elements.add(new GenericRecyclerViewListItem(location.getName(), subText, airDistance, bm, location, GenericRecyclerViewAdapter.VIEWTYPE_LOCATION));
                                } else {
                                    elements.add(new GenericRecyclerViewListItem(location.getName(), subText, airDistance, com.mapsindoors.mapssdk.R.drawable.misdk_step, location, GenericRecyclerViewAdapter.VIEWTYPE_LOCATION));
                                }
                            }
                        }
                    }
                }

                runOnUiThread( () -> {

                    mMenuFragment.resetScroll();
                    mMenuFragment.populateItemList(elements);

                    // know when the search text is empty to save all the locations of a category
                    LocationQuery locQuery = mCSearchQuery;

                    if (locQuery.getQuery() == null) {
                        mMenuFragment.setCategLocationItems(elements);
                    }

                    mIsSearching = false;

                    if (pendingSearch != null) {
                        String searchString = pendingSearch;
                        pendingSearch = null;
                        onMenuSearch(searchString, true);
                    }
                });
            }
        }
    };


    //region Implements FloatingActionListener
    static final float FAB_OVERLAY_MAX_ALPHA = 0.7f;


    /**
     *
     * @param selectedCategory
     */
    @Override
    public void onFABSelect( @Nullable final String selectedCategory )
    {
        final Context context = getApplicationContext();

        final LocationQuery.Builder iLocsQueryBuilder = new LocationQuery.Builder();

        // Build up the query
        iLocsQueryBuilder.
                setVenue( getCurrentVenueName() ).
                setFloor( mMapControl.getCurrentFloorIndex() ).
                setOrderBy( LocationQuery.RELEVANCE ).
                setQueryMode( LocationQuery.MODE_PREFER_OFFLINE ).
                setCategories( Collections.singletonList( selectedCategory ) ).
                setMaxResults( MapsIndoorsSettings.INDOOR_LOCATIONS_QUERY_RESULT_MAX_LENGTH );

        LocationQuery query = iLocsQueryBuilder.build();

        mCSearchLocationsProvider.getLocationsAsync(query, ( locations, error ) -> {
            if( error == null )
            {
                runOnUiThread( () -> {
                    if( !MapsIndoorsUtils.isNullOrEmpty( locations ) )
                    {
                        mSelectionManager.selectLocationsBy( locations, SelectionManager.QUERY_CATEGORY );
                    }
                    else
                    {
                        Toast.makeText( context, String.format( getString( R.string.no_pois_found ), selectedCategory ), Toast.LENGTH_SHORT ).
                                show();
                    }
                } );
            }
            else
            {
                if( error.code == MIError.INVALID_API_KEY )
                {
                    MapsIndoorsUtils.showInvalidAPIKeyDialogue( MapsIndoorsActivity.this );
                }
            }
        } );
    }

    @Override
    public void onFABListOpen() {}

    @Override
    public void onFABListClose() {}

    @Override
    public void onFABAnimationUpdate( float value )
    {
        if( mBlurEffectView != null )
        {
            float alphaValue = FAB_OVERLAY_MAX_ALPHA * value;

            mBlurEffectView.setAlpha( alphaValue );

            if( (mBlurEffectView.getVisibility() != View.VISIBLE) && alphaValue > 0 )
            {
                mBlurEffectView.setVisibility( View.VISIBLE );
            }

            if( alphaValue == 0 )
            {
                mBlurEffectView.setVisibility( View.GONE );
            }
        }
    }
    //endregion


    public MapControl getMapControl() {
        return mMapControl;
    }



    //region Implements OnMapCompassClickedListener
    @Override
    public void onMapCompassClicked()
    {
        GoogleAnalyticsManager.reportEvent( getString( R.string.fir_event_Map_Compass_Clicked ), null );

        CameraPosition camPos = mGoogleMap.getCameraPosition();

        CameraPosition cameraPosition = new CameraPosition.Builder().
                tilt( 0 ).
                bearing( 0 ).
                target( camPos.target ).
                zoom( camPos.zoom ).
                build();

        mGoogleMap.animateCamera( CameraUpdateFactory.newCameraPosition( cameraPosition ) );
    }
    //endregion


    public LastRouteInfo getLastRouteInfo()
    {
        return mLastRouteInfo;
    }

    public void setLastRoute( Route route, Location origin, Location destination )
    {
        BuildingCollection bc = getBuildingCollection();

        final boolean isOriginInsideABuilding, isDestinationInsideABuilding;
        if( bc != null )
        {
            isOriginInsideABuilding = bc.getBuilding( origin.getLatLng() ) != null;
            isDestinationInsideABuilding = bc.getBuilding( destination.getLatLng() ) != null;
        } else
        {
            isOriginInsideABuilding = isDestinationInsideABuilding = false;
        }

        mLastRouteInfo = new LastRouteInfo(
                route,
                origin,
                destination,
                isOriginInsideABuilding,
                isDestinationInsideABuilding,
                System.currentTimeMillis()
        );
    }

    public Route getLastRoute()
    {
        if( mLastRouteInfo == null )
        {
            return null;
        }

        return mLastRouteInfo.getLastRoute();
    }

    public Location getLastRouteOrigin()
    {
        if( mLastRouteInfo == null )
        {
            return null;
        }

        return mLastRouteInfo.getLastRouteOrigin();
    }

    public Location getLastRouteDestination()
    {
        if( mLastRouteInfo == null )
        {
            return null;
        }

        return mLastRouteInfo.getLastRouteDestination();
    }

    public boolean isLastRouteValid()
    {
        if( mLastRouteInfo == null )
        {
            return false;
        }

        final long dt = System.currentTimeMillis() - mLastRouteInfo.getTimeStamp();
        return dt <= (MapsIndoorsSettings.ROUTING_ROUTE_VALIDITY_MAX_TIME_IN_SECS * 1000);
    }

    public void invalidateLastRoute() {
        mLastRouteInfo = null;
    }



    //region MAPSINDOORS UI COMPONENTS

    /**
     * Setup MapsIndoors external UI components: Map compass and floor selector
     */
    void setupMIUIComponents()
    {
        //  Set the floor selector (as an external SDK UI component)
        mMapControl.setFloorSelector( mMapFloorSelector );
        mMapControl.setFloorSelectorType( FloorSelectorType.ONLYCURRENTBUILDING );

        //  Set the map compass (as an external SDK UI component)
        mMapCompass.setGoogleMap( mGoogleMap );

        // --------------------------------------------------------------------------
        // The compass needs to listen for the map's camera events
        // Note that we DO NOT THE USE THE GMAP CAMERA EVENT LISTENERS DIRECTLY,
        // instead, we add our listener to map control, which will forward the events
        // from the gmaps object
        //
        mMapControl.addOnCameraMoveStartedListener( mMapCompass );
        mMapControl.addOnCameraMoveListener( mMapCompass );
        mMapControl.addOnCameraMoveCanceledListener( mMapCompass );

        // Listen for clicks on the compass view
        mMapCompass.setOnCompassClickedListener( mActivity );

        // Disable the (Google Maps) compass
        UiSettings gMapUISettings = mGoogleMap.getUiSettings();
        if( gMapUISettings.isCompassEnabled() )
        {
            gMapUISettings.setCompassEnabled( false );
        }
    }
    //endregion



    @Nullable
    public AppConfigManager getAppConfigManager() {
        return mAppConfigManager;
    }

    public void setToolbarTitle(String title, boolean closeButtonVisibility) {
        mTopSearchField.setToolbarText(title, closeButtonVisibility);
    }


    //region CONNECTIVITY HANDLING
    public void hideNoAvailableNetworkFragment()
    {
        mNoAvailableNetworkLayout.setVisibility(View.GONE);

        if (mMapsIndoorsDataIsReady) {
            enableMenu();
        }
    }

    public void showNoAvailableNetworkFragment()
    {
        disableMenu();

        mNoAvailableNetworkLayout.setVisibility(View.VISIBLE);
    }

    private boolean mIsTheFirstCall = true;

    /**
     *
     */
    private void setupConnectivityHandler()
    {
        // Init the connectivity change broadcastReceiver with the activity context
        mNetworkStateChangeReceiver = new NetworkStateChangeReceiver( this ); //passing context

        mNetworkStateChangeReceiver.addOnStateChangedListener( this::respondToInternetConnectivityChanges );

        registerReceiver(mNetworkStateChangeReceiver, new IntentFilter( ConnectivityManager.CONNECTIVITY_ACTION ) );
    }

    void respondToInternetConnectivityChanges(boolean isConnected) {

        // inform all the fragments of the connectivity state change
        for( BaseFragment fragment : getMyFragments() )
        {
            if( fragment != null )
            {
                fragment.connectivityStateChanged( isConnected );
            }
        }

        if( isConnected )
        {
            hideNoAvailableNetworkFragment();

            boolean isSynchronizingContent = MapsIndoors.isSynchronizingContent();

            if( !isSynchronizingContent  )
            {
                syncData( false );
            }

            if( !mMapsIndoorsDataIsReady && !mIsTheFirstCall )
            {
                showLoadingStatus();
            }

            hideMapSnackBar();

            isNetworkReachable = true;

        }
        else
        {
            if(mDrawerIStatePrev == DRAWER_ISTATE_IS_CLOSED){
                showMapSnackBar();
            }

            isNetworkReachable = false;
        }

        mIsTheFirstCall = false;
    }

    void onPositionProviderStateChanged( boolean enabled )
    {
        if( mMapControl != null )
        {
            if( enabled )
            {
                mMapControl.showUserPosition( true );
                startPositioning();
            }
            else
            {
                mMapControl.showUserPosition( false );
                stopPositioning();
            }
        }

        // Update the "Go to my location" UI Button
        if( mGoToMyLocationButton != null )
        {
            mGoToMyLocationButton.setImageResource( enabled ? R.drawable.ic_my_location_active : R.drawable.ic_my_location_inactive );
        }
    }
    //endregion


    //
    public void getNearestLocationToTheUser( boolean preferOnline, @NonNull OnLocationsReadyListener onLocationsReadyListener )
    {
        if( getCurrentPos() == null )
        {
            onLocationsReadyListener.onLocationsReady( null, new MIError( MIError.UNKNOWN_ERROR ) );
            return;
        }

        final LocationQuery.Builder iLocsQueryBuilder = new LocationQuery.Builder();

        // Prepare the query to get the requested location
        iLocsQueryBuilder.
                setRadius( 15 ).
                setMaxResults( 1 ).
                setNear(getCurrentPos()).
                setOrderBy( LocationQuery.RELEVANCE ).
                // ATM, manual search is performed on the backend only (proper offline search not implemented yet)
                setQueryMode( preferOnline ? LocationQuery.MODE_ONLY_ONLINE : LocationQuery.MODE_ONLY_OFFLINE );

        mCSearchQuery = iLocsQueryBuilder.build();
        mCSearchLocationsProvider.getLocationsAsync( mCSearchQuery, onLocationsReadyListener );
    }

    public void resetLocationsSearchCategoryFilter() {
        setLocationQueryTypeFilter(null);
        setLocationQueryCategoryFilter(null);
    }

    public void setLocationQueryTypeFilter(@Nullable String typeFilter) {
        mQueryTypeFilter = typeFilter;
    }

    public void setLocationQueryCategoryFilter(@Nullable String categoryFilter) {
        mQueryCategoryFilter = categoryFilter;
    }


    int mDrawerFlags = 0, mDrawerIStatePrev = DRAWER_ISTATE_IS_CLOSED;
    float mDrawerSOffsPrev = 0;

    private DrawerLayout.DrawerListener mDrawerListener = new DrawerLayout.DrawerListener() {
        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {

            if ((slideOffset > mDrawerSOffsPrev) && ((mDrawerFlags & (1 << 0)) == 0)) {
                // opening

                mDrawerFlags |= (1 << 0);
                drawerEvent(DRAWER_ISTATE_WILL_OPEN);
            } else if ((slideOffset < mDrawerSOffsPrev) && ((mDrawerFlags & (1 << 0)) != 0)) {
                // closing
                mDrawerFlags &= ~(1 << 0);
                drawerEvent( DRAWER_ISTATE_WILL_CLOSE );
            }

            mDrawerSOffsPrev = slideOffset;

            // Setting the visibility of the map components
            // The the visibility of the components and the visibility of the drawer should go in the opposite way
            float alphaValue = 1 - slideOffset;

            if (mFloatingActionButton != null) {
                View fabLayout = mFloatingActionButton.getView();
                if (fabLayout != null) {
                    fabLayout.setAlpha(alphaValue);
                    mOverlayFragment.setAlpha(alphaValue);
                }
            }
        }

        @Override
        public void onDrawerOpened( @NonNull View drawerView )
        {
            drawerEvent( DRAWER_ISTATE_IS_OPEN );

            if( mFloatingActionButton != null )
            {
                mFloatingActionButton.close();
            }

            if( !isNetworkReachable )
            {
                hideMapSnackBar();
            }
        }

        @Override
        public void onDrawerClosed( @NonNull View drawerView )
        {
            GoogleAnalyticsManager.reportEvent(getString(R.string.fir_event_Map_Tap_Drawer_Close), null);

            drawerEvent( DRAWER_ISTATE_IS_CLOSED );

            if( !isNetworkReachable )
            {
                showMapSnackBar();
            }
        }

        @Override
        public void onDrawerStateChanged( int newState ) {}
    };

    public void hideMapSnackBar()
    {
        bottomMessageLayout.setVisibility( View.GONE );
    }

    public void showMapSnackBar()
    {
        bottomMessageLayout.setVisibility( View.VISIBLE );
    }

    void drawerEvent( int state )
    {
        final BaseFragment[] frags = getMyFragments();

        switch( state )
        {
            case DRAWER_ISTATE_IS_CLOSED:

                // Analytics reporting
                if (mMapControl != null) {
                    final int resultCount = mMapControl.getSearchResultCount();
                    final boolean isShowingSearch = resultCount >= 1;
                    final boolean isMainMenuCurrent = getCurrentMenuShown() == MENU_FRAME_MAIN_MENU;

                    if (isMainMenuCurrent) {
                        if (isShowingSearch) {
                            GoogleAnalyticsManager.reportScreen(getString(R.string.ga_screen_show_search_on_map), mActivity);
                        }
                    }
                    // Report a screen view when the drawer hides while a route (being rendered) is active
                    if ((mHorizontalDirectionsFragment != null) && mHorizontalDirectionsFragment.isFragmentSafe()) {
                        if (mHorizontalDirectionsFragment.isActive()) {
                            GoogleAnalyticsManager.reportScreen(getString(R.string.ga_screen_show_route_on_map), mActivity);
                        }
                    }
                }
                break;

            case DRAWER_ISTATE_WILL_OPEN:
                break;

            // Analytics reporting
            case DRAWER_ISTATE_IS_OPEN:
                reportCurrentMenuToGA();
                break;

            case DRAWER_ISTATE_WILL_CLOSE:
                break;
        }

        // Check if any of the fragments is handling the back press ...
        for (int i = frags.length; --i >= 0; ) {
            BaseFragment frag = frags[i];
            if ((frag != null) && frag.isFragmentSafe()) {
                frag.onDrawerEvent(state, mDrawerIStatePrev);
            }
        }

        mDrawerIStatePrev = state;
    }

    BaseFragment[] getMyFragments() {
            // build a list of the current fragments
            mFragments = new BaseFragment[]{
                    mMenuFragment,
                    mVenueSelectorFragment,
                    mVerticalDirectionsFragment,
                    mSearchFragment,
                    mLocationMenuFragment,
                    mHorizontalDirectionsFragment,
                    mTransportAgenciesFragment,
                    mAppInfoFragment
            };

        return mFragments;
    }


    //region Positioning
    void startPositioning()
    {
        if( mLocationPermissionGranted && PSUtils.isLocationServiceEnabled( this ) )
        {
            MapsIndoors.startPositioning();
        }
    }

    private void stopPositioning()
    {
        MapsIndoors.stopPositioning();
    }
    //endregion


    static boolean isActivityFinishing(Context context) {

        if (context == null) {
            return true;
        }

        if ((context instanceof Activity)) {
            final Activity asActivity = (Activity) context;

            return (asActivity.isFinishing() || asActivity.isDestroyed());
        }

        return false;
    }

    // zoom for detail visibility
    private void setZoomForDetailInvisible()
    {
        SharedPrefsHelper.setZoomForDetailButtonToShow(this,false);

        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mZoomForDetailButton, "alpha", 1f, 0f);
        objectAnimator.setDuration(500L);

        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mZoomForDetailButton.setVisibility(View.GONE);
            }
        });
        objectAnimator.start();
    }

    private void setZoomForDetailVisible() {

        mZoomForDetailButton.setVisibility(View.VISIBLE);

        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mZoomForDetailButton, "alpha", 0f, 1f);
        objectAnimator.setDuration(500L);

        objectAnimator.start();
    }

    // return to venue visibility
    private void setReturnToVenueInvisible() {

        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mReturnToVenueButton, "alpha", 1f, 0f);
        objectAnimator.setDuration(500L);

        objectAnimator.addListener( new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mReturnToVenueButton.setVisibility(View.GONE);
            }
        });
        objectAnimator.start();
    }

    private void setReturnToVenueVisible()
    {
        String target = mSelectionManager.getSelectionLabelForReturnToVenue();

        mReturnToVenueButton.setVisibility( View.VISIBLE );

        mReturnToVenueButton.setText( String.format( getResources().getString( R.string.return_to_venue_text ), target ) );

        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat( mReturnToVenueButton, "alpha", 0f, 1f );
        objectAnimator.setDuration( 500L );

        objectAnimator.start();
    }

    public PositionProvider getCurrentPositionProvider()
    {
        return mPositionProvider;
    }

    public boolean isDrawerOpen()
    {
        return (mDrawerLayout != null && mDrawerLayout.isDrawerOpen( GravityCompat.START ));
    }

    public void closeDrawer()
    {
        runOnUiThread( () -> {
            mDrawerLayout.closeDrawer( GravityCompat.START );
            mDrawerLayout.setClickable( false );
        } );
    }

    public void disableMenu()
    {
        if( mDrawerLayout != null )
        {
            mDrawerLayout.setDrawerLockMode( DrawerLayout.LOCK_MODE_LOCKED_CLOSED );
        }

        android.support.v7.app.ActionBar ab = getSupportActionBar();

        if( ab != null )
        {
            ab.setDisplayHomeAsUpEnabled( false );
        }
    }

    public void enableMenu()
    {
        if( mDrawerLayout != null )
        {
            mDrawerLayout.setDrawerLockMode( DrawerLayout.LOCK_MODE_UNLOCKED );
        }

        android.support.v7.app.ActionBar ab = getSupportActionBar();

        if( ab != null )
        {
            ab.setDisplayHomeAsUpEnabled( true );
        }
        else
        {
            if( dbglog.isDebugMode() )
            {
                dbglog.Assert( false, "Missing action bar?!" );
            }
        }
    }

    private void resetDrawer()
    {
        if (mDrawerLayout != null)
        {
            mDrawerLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    //Remove the listener before proceeding
                    mDrawerLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    mDrawerLayout.closeDrawer(GravityCompat.START, false);
                    disableMenu();
                }
            });
        }
    }

    //region Implements GoogleMap.OnMapLongClickListener
    @Override
    public void onMapLongClick( LatLng latLng ) {}
    //endregion


    //region Implements GoogleMap.OnMapClickListener
    //Called when the user makes a tap gesture on the map, but only if none of the overlays of the map handled the gesture.
    @Override
    public void onMapClick( LatLng latLng )
    {
        if( mFloatingActionButton != null )
        {
            mFloatingActionButton.close();
        }
    }
    //endregion


    //region Implements GoogleMap.OnMarkerClickListener
    @Override
    public boolean onMarkerClick( Marker marker )
    {
        String title = marker.getTitle();

        if( title != null )
        {
            Location location = mMapControl.getLocation( marker );

            if( location != null )
            {
                // Report the click event to Analytics...
                {
                    final Bundle eventParams = new Bundle();
                    eventParams.putString("Location", location.getName());

                    GoogleAnalyticsManager.reportEvent(
                            getString(R.string.fir_event_tapped_location_on_map),
                            eventParams
                    );
                }

                boolean showLocationNameInToolbar = true;
                if(mHorizontalDirectionsFragment.isActive()){
                    showLocationNameInToolbar = false;
                }

                mSelectionManager.selectLocation(location, false,showLocationNameInToolbar);

            } else {
                if (dbglog.isDebugMode()) {
                    final Context context = getApplicationContext();
                    Toast.makeText(context, "onMarkerClick() Error: no locations found", Toast.LENGTH_SHORT).show();
                }
            }
        }

        return true;
    }
    //endregion


    boolean openLocationMenuFromInfowindowClick = true;

    public void setOpenLocationMenuFromInfowindowClick( boolean state )
    {
        openLocationMenuFromInfowindowClick = state;

        if( openLocationMenuFromInfowindowClick )
        {
            mPoiMarkerCustomInfoWindowAdapter.setInfoWindowType( POIMarkerInfoWindowAdapter.INFOWINDOW_TYPE_LINK );
        }
        else
        {
            mPoiMarkerCustomInfoWindowAdapter.setInfoWindowType( POIMarkerInfoWindowAdapter.INFOWINDOW_TYPE_NORMAL );
        }
    }


    //region Implements GoogleMap.OnInfoWindowClickListener
    @Override
    public void onInfoWindowClick( Marker marker )
    {
        if( dbglog.isDebugMode() )
        {
            dbglog.Log( TAG, String.format( Locale.US, "Clicked on marker's title '%s' info window", marker.getTitle() ) );
        }

        if( openLocationMenuFromInfowindowClick )
        {
            Location loc = mMapControl.getLocation( marker );
            if( loc != null )
            {
                onMenuSelect( loc, GenericRecyclerViewAdapter.VIEWTYPE_LOCATION);
                openDrawer( true );
            }
        }
    }
    //endregion

    @Nullable
    public GoogleMap getGoogleMap()
    {
        return mGoogleMap;
    }
}
