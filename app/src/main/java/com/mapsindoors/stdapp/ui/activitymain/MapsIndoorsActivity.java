package com.mapsindoors.stdapp.ui.activitymain;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.AnyThread;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
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
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.mapsindoors.livesdk.LiveDataDomainTypes;
import com.mapsindoors.livesdk.LiveDataManager;
import com.mapsindoors.mapssdk.AppConfig;
import com.mapsindoors.mapssdk.BuildingCollection;
import com.mapsindoors.mapssdk.CategoryCollection;
import com.mapsindoors.mapssdk.Floor;
import com.mapsindoors.mapssdk.FloorSelectorInterface;
import com.mapsindoors.mapssdk.MPFilter;
import com.mapsindoors.mapssdk.MPLocation;
import com.mapsindoors.mapssdk.MPLocationSourceOnStatusChangedListener;
import com.mapsindoors.mapssdk.MPLocationSourceStatus;
import com.mapsindoors.mapssdk.MPQuery;
import com.mapsindoors.mapssdk.MapControl;
import com.mapsindoors.mapssdk.MapExtend;
import com.mapsindoors.mapssdk.MapsIndoors;
import com.mapsindoors.mapssdk.OnLocationsReadyListener;
import com.mapsindoors.mapssdk.OnMapClickListener;
import com.mapsindoors.mapssdk.OnMapLongClickListener;
import com.mapsindoors.mapssdk.Point;
import com.mapsindoors.mapssdk.PositionProvider;
import com.mapsindoors.mapssdk.PositionResult;
import com.mapsindoors.mapssdk.Route;
import com.mapsindoors.mapssdk.Solution;
import com.mapsindoors.mapssdk.SolutionInfo;
import com.mapsindoors.mapssdk.SphericalUtil;
import com.mapsindoors.mapssdk.Venue;
import com.mapsindoors.mapssdk.VenueCollection;
import com.mapsindoors.mapssdk.errors.MIError;
import com.mapsindoors.stdapp.BuildConfig;
import com.mapsindoors.stdapp.R;
import com.mapsindoors.stdapp.broadcastReceivers.NetworkStateChangeReceiver;
import com.mapsindoors.stdapp.condeco.CondecoBookingProvider;
import com.mapsindoors.stdapp.condeco.CondecoMapHandler;
import com.mapsindoors.stdapp.helpers.MapsIndoorsSettings;
import com.mapsindoors.stdapp.helpers.MapsIndoorsUrlSchemeHelper;
import com.mapsindoors.stdapp.helpers.MapsIndoorsUtils;
import com.mapsindoors.stdapp.helpers.SharedPrefsHelper;
import com.mapsindoors.stdapp.managers.AppConfigManager;
import com.mapsindoors.stdapp.managers.GoogleAnalyticsManager;
import com.mapsindoors.stdapp.managers.SelectionManager;
import com.mapsindoors.stdapp.managers.UserRolesManager;
import com.mapsindoors.stdapp.models.LastRouteInfo;
import com.mapsindoors.stdapp.models.SchemeModel;
import com.mapsindoors.stdapp.positionprovider.PositionManager;
import com.mapsindoors.stdapp.positionprovider.helpers.PSUtils;
import com.mapsindoors.stdapp.ui.activitymain.adapters.POIMarkerInfoWindowAdapter;
import com.mapsindoors.stdapp.ui.appInfo.AppInfoFragment;
import com.mapsindoors.stdapp.ui.common.enums.DrawerState;
import com.mapsindoors.stdapp.ui.common.enums.MenuCreatedFrom;
import com.mapsindoors.stdapp.ui.common.enums.MenuFrame;
import com.mapsindoors.stdapp.ui.booking.BookingServiceFragment;
import com.mapsindoors.stdapp.ui.common.fragments.BaseFragment;
import com.mapsindoors.stdapp.ui.common.fragments.OverlayFragment;
import com.mapsindoors.stdapp.ui.common.listeners.FloatingActionListener;
import com.mapsindoors.stdapp.ui.common.listeners.MenuListener;
import com.mapsindoors.stdapp.ui.components.mapcompass.MapCompass;
import com.mapsindoors.stdapp.ui.debug.DebugVisualizer;
import com.mapsindoors.stdapp.ui.direction.DirectionsHorizontalFragment;
import com.mapsindoors.stdapp.ui.direction.DirectionsVerticalFragment;
import com.mapsindoors.stdapp.ui.fab.FloatingAction;
import com.mapsindoors.stdapp.ui.locationmenu.LocationMenuFragment;
import com.mapsindoors.stdapp.ui.menumain.MenuFragment;
import com.mapsindoors.stdapp.ui.routeoptions.RouteOptionsFragment;
import com.mapsindoors.stdapp.ui.search.SearchFragment;
import com.mapsindoors.stdapp.ui.tracking.FollowMeButton;
import com.mapsindoors.stdapp.ui.tracking.UserPositionTrackingViewModel;
import com.mapsindoors.stdapp.ui.transportagencies.TransportAgenciesFragment;
import com.mapsindoors.stdapp.ui.venueselector.VenueSelectorFragment;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;


public class MapsIndoorsActivity extends AppCompatActivity
        implements
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener,
        OnMapClickListener,
        OnMapLongClickListener,
        FloatingActionListener,
        MenuListener,
        MapCompass.OnMapCompassClickedListener {

    public static final String TAG = MapsIndoorsActivity.class.getSimpleName();

    public static final int DRAWER_ISTATE_IS_CLOSED         = 0;
    public static final int DRAWER_ISTATE_WILL_OPEN         = 1;
    public static final int DRAWER_ISTATE_IS_OPEN           = 2;
    public static final int DRAWER_ISTATE_WILL_CLOSE        = 3;

    @Retention(RetentionPolicy.SOURCE)
    public @interface InitMenuCallers {
    }

    private MapsIndoorsActivity mActivity;
    private Solution mSolution;

    // Might be null if Google Play services APK is not available.
    private GoogleMap mGoogleMap;

    private MapControl mMapControl;
    private UserPositionTrackingViewModel mUserPositionTrackingViewModel;

    // Fragments
    private SupportMapFragment mMapFragment;
    private ViewFlipper mMenuFrameViewFlipper;
    private MenuFragment mMenuFragment;
    private VenueSelectorFragment mVenueSelectorFragment;
    private DirectionsHorizontalFragment mHorizontalDirectionsFragment;
    private DirectionsVerticalFragment mVerticalDirectionsFragment;
    private SearchFragment mSearchFragment;
    private LocationMenuFragment mLocationMenuFragment;
    private TransportAgenciesFragment mTransportAgenciesFragment;
    private AppInfoFragment mAppInfoFragment;
    private RouteOptionsFragment mRouteOptionsFragment;
    private BookingServiceFragment mBookingServiceFragment;

    private BaseFragment[] mFragments;
    private View mSplashLayout;
    private View mSplashLayoutView;
    private View mNoAvailableNetworkLayout;

    private AppConfigManager mAppConfigManager;

    protected ListView mMainMenuList;
    protected FloatingAction mFloatingActionButton;

    private TopSearchField mTopSearchField;
    private SelectionManager mSelectionManager;
    private UserRolesManager mUserRolesManager;

    private DrawerLayout mDrawerLayout;

    //receivers
    private NetworkStateChangeReceiver mNetworkStateChangeReceiver;

    /**
     * MapsIndoors external UI component: Floor selector
     */
    private FloorSelectorInterface mMapFloorSelector;
    /**
     * MapsIndoors external UI component: Map compass
     */
    private MapCompass mMapCompass;

    /**
     * UI components
     */
    private FollowMeButton followMeButton;
    private LastRouteInfo mLastRouteInfo;
    private OverlayFragment mOverlayFragment;
    private View mBlurEffectView;
    private Button mZoomForDetailButton;
    private Button mReturnToVenueButton;
    private Stack<MenuFrame> mFragmentStack = new Stack<>();
    private Point mDefaultVenueCameraPosition;
    private POIMarkerInfoWindowAdapter mPoiMarkerCustomInfoWindowAdapter;
    private View bottomMessageLayout;

    private SchemeModel urlScheme;

    private int mDrawerFlags;
    private DrawerState mDrawerIStatePrev = DrawerState.DRAWER_ISTATE_IS_CLOSED;

    private Venue mDefaultVenue;
    private Thread mFetchImagesThread;

    private float mDrawerSOffsPrev;
    private int defaultFollowMeButtonBottomMarginDP = 35;
    private long syncDataTimestamp;

    private boolean mIsTheFirstCall = true;
    private boolean mMapsIndoorsDataIsReady;
    private boolean mMenuInitialized;

    private boolean mLocationPermissionGranted;
    private boolean initializeCalled;
    /**
     * Hide the floor selector until the user first interacts with the map
     * - On application start
     * - On Venue change
     */
    private boolean mFloorSelectorHiddingAtStart;
    private boolean isNetworkReachable;
    private boolean mHasUserChosenAVenue;
    private boolean mSideMenuVisibility = true;
    private boolean openLocationMenuFromInfowindowClick = true;
    private boolean mNoAvailableNetworkFragmentVisibility;
    private boolean mLocationSourceStatusChangedListenerInvoked;
    private boolean mZoomButtonToShow;

    private CondecoBookingProvider mCondecoBookingProvider;
    private CondecoMapHandler mCondecoMapHandler;

    @NonNull
    private final AtomicBoolean configManagerUIAssetsIsReady = new AtomicBoolean();
    @NonNull
    private final AtomicBoolean locationsAreAvailable = new AtomicBoolean();

    private boolean finishAppInitHasBeenInvoked;

    private static final float FAB_OVERLAY_MAX_ALPHA = 0.7f;

    private CountDownTimer mSplashTextCountDownTimer;

    private PositionManager mPositionManager;
    //region Activity lifecycle

    private DebugVisualizer mPositionProviderDebugVisializer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;

        // report the solution id to crashlytics to help debug
        FirebaseCrashlytics.getInstance().setCustomKey("Solution key", MapsIndoors.getAPIKey());
        SharedPrefsHelper.setMapsIndoorsAPIKey(MapsIndoors.getAPIKey());

        setContentView(R.layout.activity_mapsindoors);

        // Create the debug window for position providers
        mPositionProviderDebugVisializer = new DebugVisualizer.Builder().
                setWindowSize(200, null).
                build(this);

        setupView();
        showLoadingStatus();
        mMapsIndoorsDataIsReady = false;

        // The first fragment is shown as default
        mFragmentStack.push(MenuFrame.MENU_FRAME_MAIN_MENU);

        setupActionBar(this);

        // toggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mTopSearchField.getToolbarView(), R.string.desc, R.string.desc);

        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // We also add this same listener in the onResume event call; we delete & add it to the internal list so no problem
        MapsIndoors.addLocationSourceOnStatusChangedListener(onLocationSourceOnStatusChangedListener);

        syncDataTimestamp = System.currentTimeMillis();
        syncData(true);

        Intent intent = getIntent();
        Uri data = intent.getData();

        urlScheme = MapsIndoorsUrlSchemeHelper.urlSchemeParser(data);
        setUpMapIfNeeded();
        mMenuInitialized = false;
        mFloorSelectorHiddingAtStart = true;

    }

    public DebugVisualizer getPositionProviderDebugVisializer(){
        return mPositionProviderDebugVisializer;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mMapControl != null) {
            mMapControl.onStart();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Uri data = intent.getData();
        if (intent.getData() != null) {
            setIntent(intent);
            urlScheme = MapsIndoorsUrlSchemeHelper.urlSchemeParser(data);
            handleUrlSchemeActions();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        addListeners();
        GoogleAnalyticsManager.reportScreen(getString(R.string.ga_screen_map), mActivity);
        if (mMapControl != null) {
            mMapControl.onResume();
            final boolean networkState = MapsIndoorsUtils.isNetworkReachable(this);
            respondToInternetConnectivityChanges(networkState);
            startPositioning();
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
    protected void onDestroy() {
        super.onDestroy();
        if (MapsIndoors.isInitialized()) {
            MapsIndoors.clearCachedData(false);
        }

        if (!BuildConfig.FLAVOR.contains("poc_app")) {
            MapsIndoors.removeLocationSourceOnStatusChangedListener(onLocationSourceOnStatusChangedListener);
        }

        if (mFetchImagesThread != null && mFetchImagesThread.isAlive()) {
            mFetchImagesThread.interrupt();
            mFetchImagesThread = null;
        }

        if (mMapControl != null) {
            mMapControl.onDestroy();
            mMapControl = null;
        }

        // Position provider: unregister, etc...
        MapsIndoors.setPositionProvider(null);

        if (mCondecoBookingProvider != null) {
            mCondecoBookingProvider.terminate();
            mCondecoBookingProvider = null;
        }

        mCondecoMapHandler = null;

        // Unregister receivers
        unregisterConnectivityBroadcastReveiver();

        mGoogleMap = null;
        mActivity = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
        if (isSplashScreenVisible()) {
            return;
        }
        if (canExitOnBackPressed()) {
            MapsIndoorsUtils.showLeavingAlertDialogue(this);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }
    //endregion

    private void addListeners() {
        // Drawer
        if (mDrawerLayout != null) {
            mDrawerLayout.removeDrawerListener(mDrawerListener);
            mDrawerLayout.addDrawerListener(mDrawerListener);
        }
    }

    public void removeListeners() {
        // Drawer
        if (mDrawerLayout != null) {
            mDrawerLayout.removeDrawerListener(mDrawerListener);
        }
    }

    public boolean isLocationPermissionGranted() {
        return mLocationPermissionGranted;
    }

    public void setLocationPermissionGranted(boolean mLocationPermissionGranted) {
        this.mLocationPermissionGranted = mLocationPermissionGranted;
    }

    private void setupView() {
        // Main fragments
        final FragmentManager fm = getSupportFragmentManager();

        mMapFragment = (SupportMapFragment) fm.findFragmentById(R.id.mapfragment);
        mMenuFragment = (MenuFragment) fm.findFragmentById(R.id.menufragment);

        mVenueSelectorFragment = (VenueSelectorFragment) fm.findFragmentById(R.id.venue_selector_fragment);
        mHorizontalDirectionsFragment = (DirectionsHorizontalFragment) fm.findFragmentById(R.id.directionsmenufragment);
        mLocationMenuFragment = (LocationMenuFragment) fm.findFragmentById(R.id.locationmenufragment);
        mVerticalDirectionsFragment = (DirectionsVerticalFragment) fm.findFragmentById(R.id.directionsfullmenufragment);
        mSearchFragment = (SearchFragment) fm.findFragmentById(R.id.directionsfullmenuSearchfragment);

        mTransportAgenciesFragment = (TransportAgenciesFragment) fm.findFragmentById(R.id.transport_sources_fragment);
        mAppInfoFragment = (AppInfoFragment) fm.findFragmentById(R.id.app_info_fragment);
        mRouteOptionsFragment = (RouteOptionsFragment) fm.findFragmentById(R.id.route_options_fragment);
        mOverlayFragment = (OverlayFragment) fm.findFragmentById(R.id.overlayfragment);

        mDrawerLayout = findViewById(R.id.main_drawer);

        // Initialize the main view flipper
        mMenuFrameViewFlipper = findViewById(R.id.menuframe_viewflipper);

        mMapFloorSelector = findViewById(R.id.mp_floor_selector);
        mMapCompass = findViewById(R.id.mp_map_compass);

        mNoAvailableNetworkLayout = findViewById(R.id.no_available_network_fragment_layout);
        followMeButton = findViewById(R.id.follow_me_button);

        mBlurEffectView = findViewById(R.id.blur_effect_view);

        bottomMessageLayout = findViewById(R.id.main_activity_bottom_message);

        mBlurEffectView.setOnTouchListener((v, event) -> {
            if (mFloatingActionButton != null) {
                mFloatingActionButton.close();
                v.performClick();
            }
            return false;
        });

        mZoomForDetailButton = findViewById(R.id.zoom_for_detail_button);
        mZoomForDetailButton.setOnClickListener(view -> {

            if (mGoogleMap != null && mDefaultVenueCameraPosition != null) {
                final CameraPosition currentCameraPosition = mGoogleMap.getCameraPosition();
                final LatLng defaultCameraPosition = mDefaultVenueCameraPosition.getLatLng();
                final CameraPosition newCameraPosition = new CameraPosition.Builder().
                        target(defaultCameraPosition).
                        tilt(currentCameraPosition.tilt).
                        zoom(MapsIndoorsSettings.VENUE_TILE_LAYER_VISIBLE_START_ZOOM_LEVEL).
                        bearing(currentCameraPosition.bearing).
                        build();
                mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition));
            }
            setZoomForDetailInvisible();
        });

        mReturnToVenueButton = findViewById(R.id.return_to_venue_button);
        mReturnToVenueButton.setOnClickListener(view -> {
            mSelectionManager.moveCameraToCurrentVenue();
            setReturnToVenueVisible(false);
        });

        mZoomButtonToShow = SharedPrefsHelper.isZoomForDetailButtonToShow(this);
        if (mZoomButtonToShow) {
            setZoomForDetailVisible();
        }

        followMeButton.setOnClickListener(view -> mPositionManager.onFollowMeButtonClickListener(view));
    }

    public void resetMapToInitialState() {
        if (mMapControl != null) {
            mMapControl.deSelectLocation();
        }
        mSelectionManager.selectCurrentVenue();
    }

    public void onRouteRendered() {
    }

    /**
     * Clears any route/search results being shown and sets the side menu (drawer) to its defaults (category list)
     */
    @SuppressWarnings("WeakerAccess")
    public void OnToolbarClearMapButtonClicked() {
        setOpenLocationMenuFromInfowindowClick(true);
        if (mVerticalDirectionsFragment != null) {
            mVerticalDirectionsFragment.closeRouting();
        }

        invalidateLastRoute();
        mSelectionManager.clearSelection();

        // go to main menu fragment and reset it
        if (mMenuFragment != null) {
            boolean animateMenuChange = !isDrawerOpen();
            mMenuFragment.mIsOpenedFromBackpress = false;
            menuGoTo(MenuFrame.MENU_FRAME_MAIN_MENU, animateMenuChange);
        }
    }

    public void horizontalDirectionsPanelWillOpen() {
        if (mFloatingActionButton != null) {
            mFloatingActionButton.setActive(false);
        }
    }

    public void horizontalDirectionsPanelWillClose() {
        if (mFloatingActionButton != null) {
            mFloatingActionButton.setActive(true);
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
    public String getCurrentVenueId() {
        VenueCollection vc = getVenueCollection();
        if ((vc != null) && (vc.getCurrentVenue() != null)) {
            return vc.getCurrentVenue().getId();
        } else {
            return null;
        }
    }

    private void initializeMenu(final MenuCreatedFrom callerId) {
        if (mMenuInitialized) {
            return;
        }

        if (mMenuFragment != null && !mMenuFragment.hasBeenInitialized()) {
            mMenuFragment.init(this, mMapControl);
            mVenueSelectorFragment.init(this, this);
            mTransportAgenciesFragment.init(this);
            mHorizontalDirectionsFragment.init(this, mGoogleMap);
            mAppInfoFragment.init( this );
            mRouteOptionsFragment.init( this );

            // Set the first fragment to be shown the first time the drawer is open
            mHasUserChosenAVenue = SharedPrefsHelper.hasUserChosenAVenue(this);
            String currAppVersion = SharedPrefsHelper.getAppVersionName(this);

            if (TextUtils.isEmpty(currAppVersion)) {
                SharedPrefsHelper.setAppVersionName(this, BuildConfig.VERSION_NAME);
            } else {
                if (!currAppVersion.equalsIgnoreCase(BuildConfig.VERSION_NAME)) {
                    SharedPrefsHelper.setAppVersionName(this, BuildConfig.VERSION_NAME);
                }
            }

            List<Venue> venuesList = getVenueCollection().getVenues();

            if (venuesList.size() == 1) {
                onMenuVenueSelect(venuesList.get(0).getId());

                mActivity.menuGoTo(MenuFrame.MENU_FRAME_MAIN_MENU, true);
                mVenueSelectorFragment.setToolbarBackAndVenueButtonVisibility(true);
                mMenuFragment.setVenueSelectorButtonshouldBeShown(false);

                SharedPrefsHelper.setUserHasChosenVenue(this, true);
            } else {
                if (mHasUserChosenAVenue) {
                    menuGoTo(MenuFrame.MENU_FRAME_MAIN_MENU, false);
                    mVenueSelectorFragment.setToolbarBackAndVenueButtonVisibility(true);
                } else {
                    menuGoTo(MenuFrame.MENU_FRAME_VENUE_SELECTOR, false);
                    mVenueSelectorFragment.setToolbarBackAndVenueButtonVisibility(false);
                }
            }
        }

        if (mMapsIndoorsDataIsReady) {
            if (!mAppConfigManager.isFabMenuHidden()) {
                mFloatingActionButton = new FloatingAction(
                        this,
                        this,
                        mAppConfigManager,
                        findViewById(R.id.float_button_fragment)
                );
            }

            runOnUiThread(() -> {
                mMenuFragment.initMenu(
                        mSolution,
                        mAppConfigManager,
                        getVenueCollection(),
                        callerId == MenuCreatedFrom.INIT
                );

                if (callerId != MenuCreatedFrom.ON_VENUE_SELECTED) {
                    mVenueSelectorFragment.onDataReady();
                }

                mMenuInitialized = true;

                if (callerId != MenuCreatedFrom.ON_VENUE_SELECTED) {
                    dataLoadingFinished();
                    setSideMenuEnabled(true);

                    // Open the drawer with the venue selector if this is the first time the app runs after an install
                    if (!mHasUserChosenAVenue && isNetworkReachable) {
                        openDrawer(true);
                    }

                    if (mLocationPermissionGranted && PSUtils.isLocationServiceEnabled(mActivity)) {
                        startPositioning();
                    }

                    if (!isNetworkReachable) {
                        setMapSnackBarVisibility(true);
                    }
                }
            });
        }
    }

    public void openDrawer(boolean animated) {
        if (!isDrawerOpen()) {
            runOnUiThread(() -> {
                mDrawerLayout.setClickable(true);
                mDrawerLayout.openDrawer(GravityCompat.START, animated);
            });
        }
    }

    //region "Splash screen"
    private void showLoadingStatus() {
        mSplashLayout = findViewById(R.id.splashscreenfragment);
        mHasUserChosenAVenue = false;

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

    private void restartLoadingStatus() {
        resetDrawer();
    }

    private void dataLoadingFinished() {
        // Network broadcast receivers
        setupConnectivityHandler();

        if (mSplashLayout.getVisibility() != View.GONE) {
            ViewCompat.animate(mSplashLayout)
                    .alpha(0f)
                    .setDuration(500)
                    .setStartDelay(1000)
                    .withEndAction(()-> {
                        if (mSplashTextCountDownTimer != null) {
                            mSplashTextCountDownTimer.cancel();
                            mSplashTextCountDownTimer = null;
                        }
                        mSplashLayout.setVisibility(View.GONE);
                    });
        }

        if (mCondecoMapHandler != null && mCondecoBookingProvider != null) {
            mCondecoMapHandler.setupBookingProvider(mCondecoBookingProvider);
        }
    }

    private boolean isSplashScreenVisible() {
        return (mSplashLayout != null) && (mSplashLayout.getVisibility() != View.GONE);
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

    @Nullable
    public SchemeModel getUrlScheme() {
        return urlScheme;
    }

    //region Menu Frame ViewFlipper

    public void menuGoTo(MenuFrame frame, boolean animate ) {
        menuGoTo( frame, false, animate );
    }

    public void menuGoTo(MenuFrame frame, boolean asPopUp, boolean animate) {
        if (mMenuFrameViewFlipper == null) {
            return;
        }

        if (animate) {
            mMenuFrameViewFlipper.setInAnimation(this, R.anim.menu_flipper_fade_in);
            mMenuFrameViewFlipper.setOutAnimation(this, R.anim.menu_flipper_fade_out);

            // When it still on the menuFragment and you want to go back to the categ menu this call is needed otherwise it will stay in the locations menu
            if (mMenuFragment != null) {
                switch (frame) {
                    case MENU_FRAME_MAIN_MENU:
                        mMenuFragment.initMenu(mSolution, mAppConfigManager, getVenueCollection(), false);
                        break;
                    case MENU_FRAME_LOCATION_MENU:
                        mMenuFragment.closeKeyboard();
                        break;
                    case MENU_FRAME_DIRECTIONS_FULL_MENU:
                        break;
                }
            }
        }

        if (!asPopUp && (frame != getCurrentMenuShown())) {
            mFragmentStack.push(frame);
        }

        showFragment(frame);
    }

    public void showFragment(MenuFrame frame) {
        if (mMenuFrameViewFlipper == null) {
            return;
        } else {
            mMenuFrameViewFlipper.setDisplayedChild(frame.ordinal());
        }

        if (isDrawerOpen()) {
            reportDrawerMenuToGA(frame);
        }
    }

    public MenuFrame getCurrentMenuShown() {
        if (mMenuFrameViewFlipper != null) {
            return MenuFrame.values()[mMenuFrameViewFlipper.getDisplayedChild()];
        } else {
            return MenuFrame.MENU_FRAME_MAIN_MENU;
        }
    }

    public void menuGoBack() {
        try {
            mFragmentStack.pop();
            MenuFrame frame = mFragmentStack.peek();
            switch (frame) {
                case MENU_FRAME_MAIN_MENU:
                case MENU_FRAME_DIRECTIONS_FULL_MENU:
                    mMenuFragment.mIsOpenedFromBackpress = true;
                    showFragment(frame);
                    break;
                case MENU_FRAME_SEARCH:
                    mSearchFragment.mOpenedFromBackPress = true;
                    mSearchFragment.setActive(true);
                    break;
                default:
                    showFragment(frame);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void reportCurrentMenuToGA() {
        reportDrawerMenuToGA(getCurrentMenuShown());
    }

    private void reportDrawerMenuToGA(MenuFrame frame) {
        int strId;
        switch (frame) {
            case MENU_FRAME_MAIN_MENU:
                strId = R.string.ga_screen_main_menu;
                break;
            case MENU_FRAME_VENUE_SELECTOR:
                strId = R.string.ga_screen_venues;
                break;
            case MENU_FRAME_LOCATION_MENU:
                strId = R.string.ga_screen_details;
                break;
            case MENU_FRAME_DIRECTIONS_FULL_MENU:
                strId = R.string.ga_screen_directions;
                break;
            case MENU_FRAME_SEARCH:
                strId = R.string.ga_screen_select_place;
                break;
            case MENU_FRAME_TRANSPORT_AGENCIES:
                strId = R.string.ga_screen_transit_sources;
                break;
            case MENU_FRAME_APP_INFO:
                strId = 0; //R.string.ga_screen_transit_sources;
                break;
            case MENU_FRAME_ROUTE_OPTIONS:
                strId = 0; //R.string.ga_screen_transit_sources;
                break;
            default:
                strId = 0;
        }

        if (strId != 0) {
            GoogleAnalyticsManager.reportScreen(getString(strId), mActivity);
        }
    }


    private void setupActionBar(MapsIndoorsActivity activity) {
        ActionBar ac = getActionBar();
        if (ac != null) {
            ac.setHomeButtonEnabled(true);
        }

        mTopSearchField = new TopSearchField(activity);
        mTopSearchField.setClearMapButtonClickListener(view -> OnToolbarClearMapButtonClicked());

        Toolbar toolbarView = mTopSearchField.getToolbarView();
        if (toolbarView != null) {
            setSupportActionBar(toolbarView);
            toolbarView.setNavigationOnClickListener(view -> {
                if (isDrawerOpen()) {
                    openDrawer(true);
                } else {
                    initializeMenu(MenuCreatedFrom.MENU_BTN_CLICKED);
                }
            });
        }
    }

    private void setUpMapIfNeeded() {
        if (initializeCalled) {
            return;
        }

        final boolean doWork;

        // Do a null check to confirm that we have not already instantiated the map.
        if (mGoogleMap == null) {
            initializeCalled = true;
            doWork = true;
        } else {
            doWork = false;
        }

        if (doWork) {
            mMapFragment.getMapAsync(mOnMapReadyCallback);
        } else {
            setupMapControl();
        }
    }

    private void setupCondecoBookingProvider() {
        if (BuildConfig.FLAVOR.contains("condeco")) {
            try {
                mCondecoBookingProvider = new CondecoBookingProvider();
                mCondecoMapHandler = new CondecoMapHandler(mMapControl);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //region MAPSINDOORS LIBRARY SETUP + GOOGLE MAPS SETUP
    private OnMapReadyCallback mOnMapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mGoogleMap = googleMap;
            if (mGoogleMap == null) {
                Toast.makeText(getApplicationContext(), "Unable to open Google map. Unable to continue", Toast.LENGTH_LONG).show();
                return;
            }
            setupMapControl();
        }
    };

    private void customizeMap() {
        try {
            //For customizing styles tweak res/raw/google_maps_styles_style.json  https://mapstyle.withgoogle.com/
            mGoogleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(mActivity, R.raw.google_maps_style));
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    private void setupMapsIndoorsContentLanguage() {
        final String deviceLanguage = MapsIndoorsUtils.getDeviceDefaultLocale(this.getResources()).getLanguage();
        final SolutionInfo solutionInfo = MapsIndoors.getSolutionInfo();
        if (solutionInfo != null) {
            // Get the MI MISolution supported languages
            List<String> solutionAvailableLanguages = solutionInfo.getAvailableLanguages();
            // This will work as long as we add string locales matching 1:1 what we have in the solution
            for (String solutionLang : solutionAvailableLanguages) {
                if (deviceLanguage.equalsIgnoreCase(solutionLang)) {
                    MapsIndoors.setLanguage(solutionLang);
                    break;
                }
            }
        }
    }

    private void setupMapControl() {
        // Creates a new MapControl instance
        mMapControl = new MapControl(mActivity);
        mMapControl.setGoogleMap(mGoogleMap, mMapFragment.getView());
        mUserPositionTrackingViewModel = new UserPositionTrackingViewModel(mMapControl, mGoogleMap, this, state -> {
            if (followMeButton != null) {
                followMeButton.setState(state);
            }
        });

        // Condeco flavor only
        setupCondecoBookingProvider();

        // init the selection manager
        mSelectionManager = new SelectionManager(this, mMapControl, mTopSearchField);
        mPoiMarkerCustomInfoWindowAdapter = new POIMarkerInfoWindowAdapter(this, mMapControl);
        mMapControl.setBuildingOutlineStrokeColor(ContextCompat.getColor(mActivity, R.color.building_outline_color));
        mMapControl.setInfoWindowAdapter(mPoiMarkerCustomInfoWindowAdapter);
        mMapControl.showBuildingOutline(true);

        customizeMap();
        finishMapsIndoorsSetup();
    }

    private void finishMapsIndoorsSetup() {
        mMapControl.showBuildingOutline(true);
        mMapControl.setOnMarkerClickListener(mActivity);
        mMapControl.setOnMarkerInfoWindowClickListener(mActivity);

        setupMIUIComponents();

        /*
        Set the listeners you want to use here.
        Note that camera event listeners must be set on the map control object,
        not directly on Google Map. Mapindoors' MapControl forwards them.
        */
        mMapControl.setOnMapLongClickListener(mActivity);
        mMapControl.setOnMapClickListener(mActivity);

        mMapControl.addOnCameraMoveStartedListener(reason -> {
            switch (reason) {
                // Camera motion initiated in response to user gestures on the map
                case GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE: {
                    if (mFloorSelectorHiddingAtStart) {
                        mFloorSelectorHiddingAtStart = false;
                        if (mMapFloorSelector != null) {
                            final Floor currentBuildingFloor = mMapControl.getCurrentBuildingFloor();
                            if (currentBuildingFloor != null) {
                                mMapFloorSelector.setSelectedFloor(currentBuildingFloor);
                            }
                            mMapControl.enableFloorSelector(true);
                        }
                    }
                }
                // Non-gesture animation initiated in response to user actions
                case GoogleMap.OnCameraMoveStartedListener.REASON_API_ANIMATION:
                    break;
                // Developer initiated animation
                case GoogleMap.OnCameraMoveStartedListener.REASON_DEVELOPER_ANIMATION:
                    // Disable return-to-venue visibility, when either directions fragments are active
                    if (mVerticalDirectionsFragment.isActive() || mHorizontalDirectionsFragment.isActive()) {
                        setReturnToVenueVisible(false);
                    }
                    break;
            }
        });

        mMapControl.addOnCameraIdleListener(() -> {
            mMapCompass.updateFromCameraEvent();
            CameraPosition pos = mGoogleMap.getCameraPosition();
            if (mZoomButtonToShow) {
                if (pos.zoom >= (MapsIndoorsSettings.VENUE_TILE_LAYER_VISIBLE_START_ZOOM_LEVEL)) {
                    setZoomForDetailInvisible();
                }
            }
        });

        mMapControl.setOnCurrentVenueChangedListener(venue -> {
            final boolean isOverAVenue = (venue != null);

            if (mZoomButtonToShow) {
                if (isOverAVenue) {
                    setZoomForDetailVisible();
                } else {
                    setZoomForDetailInvisible();
                }
            }

            boolean isHoveringSelectedVenue = false;
            final Venue currentVenue = mSelectionManager.getCurrentVenue();
            if (currentVenue != null && isOverAVenue) {
                final String hoveringVenueId = venue.getId();
                isHoveringSelectedVenue = hoveringVenueId.contentEquals(currentVenue.getId());
            }

            if (isHoveringSelectedVenue) {
                setReturnToVenueVisible(false);
            } else {
                if (!mVerticalDirectionsFragment.isActive() && !mHorizontalDirectionsFragment.isActive()) {
                    runOnUiThread(()->{
                        setReturnToVenueVisible(true);
                    });
                }
            }
        });

        setupMapsIndoorsContentLanguage();

        mMapControl.setLocationHideOnIconOverlapEnabled(true);
        // Initialize MapControl: among other tasks, it will load/update the solution data, if needed
        mMapControl.init(this::onMapControlInitReady);


    }


    @MainThread
    void onMapControlInitReady( @Nullable final MIError error ) {
        onMapsIndoorsDataUpdates( error );
        mPositionManager = new PositionManager(mActivity, mMapControl, positionProvider -> {
            runOnUiThread(() -> mAppInfoFragment.setPositionProvider(positionProvider));
        });
    }

    /**
     * Invoked once per activity session
     */
    void onLocationSourceStatusChangedToAvailable() {
        locationsAreAvailable.set(true);
        invokeFinishAppInit();
    }

    /**
     * Invoked once per activity session
     */
    void onConfigManagerUIAssetsReady() {
        configManagerUIAssetsIsReady.set(true);
        invokeFinishAppInit();
    }

    synchronized void invokeFinishAppInit() {
        if (configManagerUIAssetsIsReady.get() && locationsAreAvailable.get()
                && !finishAppInitHasBeenInvoked) {
            finishAppInitHasBeenInvoked = true;
            finishAppInit();
        }
    }

    void onMapsIndoorsDataUpdates(@Nullable final MIError error) {
        if (isActivityFinishing(mActivity)) {
            return;
        }

        MIError err = error;

        if (err == null) {
            if (mSolution == null) {
                mSolution = MapsIndoors.getSolution();

                // Create the app's User Role manager
                if (mUserRolesManager == null) {
                    mUserRolesManager = new UserRolesManager(mActivity);
                }

                // Create the app config manager
                if (mAppConfigManager == null) {
                    final AppConfig ac = MapsIndoors.getAppConfig();
                    if (ac != null) {
                        mAppConfigManager = new AppConfigManager(mMapControl);
                    } else {
                        err = new MIError(MIError.DATALOADER_APPCONFIG_UNKNOWN_ERROR);
                    }

                    if (mAppConfigManager != null) {
                        mFetchImagesThread = mAppConfigManager.getUIAssets(this, this::onConfigManagerUIAssetsReady);
                        mFetchImagesThread.start();
                    } else {
                        err = new MIError(MIError.DATALOADER_APPCONFIG_UNKNOWN_ERROR);
                    }
                }
            }

            // Refresh the user roles manager
            if (mUserRolesManager != null) {
                mUserRolesManager.updateFromDataUpdate();
            }
        }

        if (err != null && err.code == MIError.INVALID_API_KEY) {
            MapsIndoorsUtils.showInvalidAPIKeyDialogue(this);
        }

        reportOnDataUpdatedToFragments();
    }

    void finishAppInit() {
        mDefaultVenue = null;
        final String savedVenueId = SharedPrefsHelper.getCurrentVenueId(getApplicationContext());
        final VenueCollection vc = getVenueCollection();

        if (vc != null) {
            if (!TextUtils.isEmpty(savedVenueId)) {
                mDefaultVenue = vc.getVenueById(savedVenueId);
                if (mDefaultVenue != null) {
                    vc.selectVenue(savedVenueId);
                }
            }

            // if no venue was chosen before then put the camera on the default venue and set as a default venue
            if (mDefaultVenue == null) {
                mDefaultVenue = vc.getDefaultVenue();
            }

            // if no venue was chosen before and no default venue then just choose the first one in the venues list
            if ((mDefaultVenue == null) && !vc.getVenues().isEmpty()) {
                mDefaultVenue = vc.getVenues().get(0);
            }

            if (mDefaultVenue != null) {
                mDefaultVenueCameraPosition = mDefaultVenue.getPosition();
                SharedPrefsHelper.setCurrentVenueId(getApplicationContext(), mDefaultVenue.getId());
            }
        }

        runOnUiThread(() -> {
            if (mDefaultVenue != null) {
                // Sets the name to the default venue
                mSelectionManager.setCurrentVenue(mDefaultVenue);
            }

            mMapsIndoorsDataIsReady = true;
            initializeMenu(MenuCreatedFrom.INIT);
        });

        enableLiveData();

        handleUrlSchemeActions();
    }
    //endregion

    public void handleUrlSchemeActions() {
        SchemeModel urlScheme = getUrlScheme();
        if (urlScheme != null) {
            if (urlScheme.getType().equalsIgnoreCase("directions")) {
                MPLocation destination = MapsIndoorsUrlSchemeHelper.createLocationForDirectionsDestination(urlScheme);
                MPLocation origin = MapsIndoorsUrlSchemeHelper.createLocationForDirectionsOrigin(urlScheme);
                String travelMode = urlScheme.getParameter(SchemeModel.SCHEME_PROPERTY_DIRECTIONS_TRAVEL_MODEL);
                String avoid = urlScheme.getParameter(SchemeModel.SCHEME_PROPERTY_DIRECTIONS_AVOID);

                if (destination != null) {
                    runOnUiThread(() -> {
                        mVerticalDirectionsFragment.open(destination, origin, travelMode, avoid);
                        openDrawer(false);
                    });
                }
            } else if (urlScheme.getType().equalsIgnoreCase("details")) {
                MPLocation location = MapsIndoorsUrlSchemeHelper.createLocationForDetailsLocation(urlScheme);
                if (location != null) {
                    runOnUiThread(() -> {
                        mMenuFragment.openLocationMenu(location);
                        openDrawer(false);
                    });
                }
            }
        }
    }

    //region Implements MenuListener
    @Override
    public void onMenuVenueSelect(String venueId) {
        VenueCollection vc = getVenueCollection();

        if ((vc != null) && vc.selectVenue(venueId)) {
            final String currentVenueIdStored = SharedPrefsHelper.getCurrentVenueId(getApplicationContext());
            if ((venueId != null) && !venueId.equalsIgnoreCase(currentVenueIdStored)) {
                userHasChosenNewVenue();
            }

            // Remember the selected venue
            SharedPrefsHelper.setCurrentVenueId(getApplicationContext(), venueId);

            // Reinitialize the menu with the new venue settings
            mMenuInitialized = false;

            // Retrieve the current venue just set
            Venue cVenue = vc.getCurrentVenue();

            if (cVenue != null) {
                // Sets the menu's title to the current venue
                mSelectionManager.setCurrentVenue(cVenue);

                // Hide the return to venue button
                setReturnToVenueVisible(false);

                // save the venue position
                mDefaultVenueCameraPosition = cVenue.getPosition();
            }
        }
    }
    //endregion

    @Nullable
    public Point getCurrentUserPos() {
        final PositionResult userPos = mMapControl.getCurrentPosition();
        if (userPos != null) {
            return userPos.getPoint();
        }
        return null;
    }

    //region Implements FloatingActionListener

    /**
     * @param selectedCategory
     */
    @Override
    public void onFABSelect(@Nullable final String selectedCategory) {
        final Context context = getApplicationContext();
        mSelectionManager.clearSelection();
        final MPQuery.Builder queryBuilder = new MPQuery.Builder();
        final MPFilter.Builder filterBuilder = new MPFilter.Builder();

        // Set the selected category
        filterBuilder.setCategories(Collections.singletonList(selectedCategory));

        // Make the search venue-bound (?)
        final String currentVenueID = mActivity.getCurrentVenueId();
        if (currentVenueID != null) {
            filterBuilder.setParents(Collections.singletonList(currentVenueID));
            filterBuilder.setDepth(4);
        }

        MapsIndoors.getLocationsAsync(queryBuilder.build(), filterBuilder.build(), (locations, qError) -> {
            if (qError == null) {
                runOnUiThread(() -> {
                    if (!MapsIndoorsUtils.isNullOrEmpty(locations)) {
                        mSelectionManager.setCurrentCategory(selectedCategory);
                        mSelectionManager.selectLocations(locations);
                    } else {
                        final CategoryCollection cc = MapsIndoors.getCategories();
                        if (cc != null) {
                            final String categoryName = cc.getValue(selectedCategory);
                            Toast.makeText(context, String.format(getString(R.string.no_pois_found), categoryName), Toast.LENGTH_SHORT).
                                    show();
                        } else {
                            Toast.makeText(context, String.format(getString(R.string.no_pois_found), selectedCategory), Toast.LENGTH_SHORT).
                                    show();
                        }
                    }
                });
            } else if (qError.code == MIError.INVALID_API_KEY) {
                MapsIndoorsUtils.showInvalidAPIKeyDialogue(MapsIndoorsActivity.this);
            }
        });
    }

    @Override
    public void onFABListOpen() {
    }

    @Override
    public void onFABListClose() {
    }

    @Override
    public void onFABAnimationUpdate(float value) {
        if (mBlurEffectView != null) {
            float alphaValue = FAB_OVERLAY_MAX_ALPHA * value;
            mBlurEffectView.setAlpha(alphaValue);
        }
    }
    //endregion

    public MapControl getMapControl() {
        return mMapControl;
    }

    //region Implements OnMapCompassClickedListener
    @Override
    public void onMapCompassClicked() {
        GoogleAnalyticsManager.reportEvent(getString(R.string.fir_event_Map_Compass_Clicked), null);
        CameraPosition camPos = mGoogleMap.getCameraPosition();
        CameraPosition cameraPosition = new CameraPosition.Builder().
                tilt(0).
                bearing(0).
                target(camPos.target).
                zoom(camPos.zoom).
                build();

        mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
    //endregion

    public LastRouteInfo getLastRouteInfo() {
        return mLastRouteInfo;
    }

    public void setLastRoute(Route route, MPLocation origin, MPLocation destination) {
        BuildingCollection bc = getBuildingCollection();

        final boolean isOriginInsideABuilding;
        final boolean isDestinationInsideABuilding;

        if (bc != null) {
            isOriginInsideABuilding = (bc.getBuilding(origin.getLatLng()) != null);
            isDestinationInsideABuilding = (bc.getBuilding(destination.getLatLng()) != null);
        } else {
            isOriginInsideABuilding = false;
            isDestinationInsideABuilding = false;
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

    public Route getLastRoute() {
        if (mLastRouteInfo == null) {
            return null;
        } else {
            return mLastRouteInfo.getLastRoute();
        }
    }

    public MPLocation getLastRouteOrigin() {
        if (mLastRouteInfo == null) {
            return null;
        } else {
            return mLastRouteInfo.getLastRouteOrigin();
        }
    }


    public void invalidateLastRoute() {
        mLastRouteInfo = null;
    }

    //region MAPSINDOORS UI COMPONENTS

    /**
     * Setup MapsIndoors external UI components: Map compass and floor selector
     */
    private void setupMIUIComponents() {
        //  Set the floor selector (as an external SDK UI component)
        mMapControl.setFloorSelector(mMapFloorSelector);
        mMapControl.enableFloorSelector(false);

        //  Set the map compass (as an external SDK UI component)
        mMapCompass.setGoogleMap(mGoogleMap);

        /*
        The compass needs to listen for the map's camera events
        Note that we DO NOT THE USE THE GMAP CAMERA EVENT LISTENERS DIRECTLY,
        instead, we add our listener to map control, which will forward the events from the gmaps object
        */

        mMapControl.addOnCameraMoveStartedListener(mMapCompass);
        mMapControl.addOnCameraMoveListener(mMapCompass);
        mMapControl.addOnCameraMoveCanceledListener(mMapCompass);

        // Listen for clicks on the compass view
        mMapCompass.setOnCompassClickedListener(mActivity);

        // Disable the (Google Maps) compass
        UiSettings gMapUISettings = mGoogleMap.getUiSettings();
        if (gMapUISettings.isCompassEnabled()) {
            gMapUISettings.setCompassEnabled(false);
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

    public void setNoAvailableNetworkFragmentVisibility(boolean isVisible) {
        if (mNoAvailableNetworkFragmentVisibility == isVisible) {
            return;
        }
        setSideMenuEnabled(!isVisible);
        mNoAvailableNetworkLayout.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        mNoAvailableNetworkFragmentVisibility = isVisible;
    }

    private void setupConnectivityHandler() {
        // Init the connectivity change broadcastReceiver with the activity context
        mNetworkStateChangeReceiver = new NetworkStateChangeReceiver(this); //passing context
        mNetworkStateChangeReceiver.addOnStateChangedListener(this::respondToInternetConnectivityChanges);
        mNetworkStateChangeReceiver.addOnStateChangedListener(isEnabled -> {
            if (isEnabled) {
                onOnlineFetchUIAssets();
            }
        });
        registerReceiver(mNetworkStateChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    /**
     * Starts fetching remote image resources (same is performed on startup).
     * This is called when online access becomes available.
     */
    void onOnlineFetchUIAssets() {
        mFetchImagesThread = mAppConfigManager.getUIAssets(this, this::onOnlineFetchUIAssetsCallback);
        mFetchImagesThread.start();
    }

    /**
     * Callback for when {@link #onOnlineFetchUIAssets()} has finished.
     * Responsible for informing fragments that new data (might) be avaiable, as we (might) have
     * just downloaded image resources.
     */
    void onOnlineFetchUIAssetsCallback() {
        configManagerUIAssetsIsReady.set(true);
        runOnUiThread(() -> {
            for (BaseFragment frag : mFragments) {
                if ((frag != null) && !frag.isDetached() && !frag.isRemoving() && frag.isAvailable()) {
                    frag.onDataUpdated();
                }
            }
        });
    }


    void unregisterConnectivityBroadcastReveiver() {
        if (mNetworkStateChangeReceiver != null) {
            try {
                unregisterReceiver(mNetworkStateChangeReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void respondToInternetConnectivityChanges(boolean isConnected) {
        // inform all the fragments of the connectivity state change
        for (BaseFragment fragment : getMyFragments()) {
            if (fragment != null) {
                fragment.connectivityStateChanged(isConnected);
            }
        }

        if (isConnected) {
            setNoAvailableNetworkFragmentVisibility(false);
            final boolean isSynchronizingContent = MapsIndoors.isSynchronizingContent();

            if (!isSynchronizingContent) {
                syncData(false);
            }

            if (!mMapsIndoorsDataIsReady && !mIsTheFirstCall) {
                showLoadingStatus();
            }

            setMapSnackBarVisibility(false);
            isNetworkReachable = true;
        } else {
            if (mDrawerIStatePrev == DrawerState.DRAWER_ISTATE_IS_CLOSED) {
                setMapSnackBarVisibility(true);
            }
            isNetworkReachable = false;
        }
        mIsTheFirstCall = false;
    }

    //endregion

    //
    public void getNearestLocationToTheUser(@NonNull OnLocationsReadyListener onLocationsReadyListener) {
        if (getCurrentUserPos() == null) {
            onLocationsReadyListener.onLocationsReady(null, new MIError(MIError.UNKNOWN_ERROR));
            return;
        }

        Point userPos = getCurrentUserPos();
        if (userPos != null) {
            LatLng northEast = SphericalUtil.computeOffset(userPos.getLatLng(), 15, 22.5);
            LatLng southWest = SphericalUtil.computeOffset(userPos.getLatLng(), 15, 225);
            MapExtend mapExtend = new MapExtend(southWest, northEast);

            MPQuery q = new MPQuery.Builder().
                    setNear(userPos).
                    build();
            MPFilter f = new MPFilter.Builder().
                    setMapExtend(mapExtend).
                    setFloorIndex(userPos.getZIndex()).
                    setTake(1).build();

            MapsIndoors.getLocationsAsync(q, f, onLocationsReadyListener);
        }
    }

    private DrawerLayout.DrawerListener mDrawerListener = new DrawerLayout.DrawerListener() {
        @Override
        public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
            if ((slideOffset > mDrawerSOffsPrev) && ((mDrawerFlags & (1 << 0)) == 0)) {
                // opening
                mDrawerFlags |= (1 << 0);
                drawerEvent(DrawerState.DRAWER_ISTATE_WILL_OPEN);
            } else if ((slideOffset < mDrawerSOffsPrev) && ((mDrawerFlags & (1 << 0)) != 0)) {
                // closing
                mDrawerFlags &= ~(1 << 0);
                drawerEvent(DrawerState.DRAWER_ISTATE_WILL_CLOSE);
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
        public void onDrawerOpened(@NonNull View drawerView) {
            drawerEvent(DrawerState.DRAWER_ISTATE_IS_OPEN);
            if (mFloatingActionButton != null) {
                mFloatingActionButton.close();
            }

            if (!isNetworkReachable) {
                setMapSnackBarVisibility(false);
            }
        }

        @Override
        public void onDrawerClosed(@NonNull View drawerView) {
            GoogleAnalyticsManager.reportEvent(getString(R.string.fir_event_Map_Tap_Drawer_Close), null);
            drawerEvent(DrawerState.DRAWER_ISTATE_IS_CLOSED);
            if (!isNetworkReachable) {
                setMapSnackBarVisibility(true);
            }
        }

        @Override
        public void onDrawerStateChanged(int newState) {
        }
    };

    public void setMapSnackBarVisibility(boolean isVisible) {
        bottomMessageLayout.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    void drawerEvent(DrawerState state) {
        switch (state) {
            case DRAWER_ISTATE_IS_CLOSED:
                // Analytics reporting
                if (mMapControl != null) {
                    final int resultCount = mMapControl.getSearchResultCount();
                    final boolean isShowingSearch = (resultCount >= 1);
                    final boolean isMainMenuCurrent = (getCurrentMenuShown() == MenuFrame.MENU_FRAME_MAIN_MENU);

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
                mMenuFragment.closeKeyboard();
                break;
        }

        // Check if any of the fragments is handling the back press ...
        reportOnDrawerEventToFragments(state, mDrawerIStatePrev);
        mDrawerIStatePrev = state;
    }

    private BaseFragment[] getMyFragments() {
        // build a list of the current fragments
        // IMPORTANT: KEEP THE ORDER OF THE MENU_FRAME_XX CONSTANTS
        if (mFragments == null) {
            mFragments = new BaseFragment[]{
                    mMenuFragment,
                    mVenueSelectorFragment,
                    mLocationMenuFragment,
                    mVerticalDirectionsFragment,
                    mSearchFragment,
                    mTransportAgenciesFragment,
                    mAppInfoFragment,
                    mRouteOptionsFragment,
                    mHorizontalDirectionsFragment,
            };
        }
        return mFragments;
    }

    //region Positioning
    private void startPositioning() {
        if (mMapControl != null) {
            MapsIndoors.startPositioning();
            mMapControl.showUserPosition(true);
        }
    }

    private void stopPositioning() {
        MapsIndoors.stopPositioning();
    }

    //endregion

    public static boolean isActivityFinishing(Context context) {
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
    private void setZoomForDetailInvisible() {
        mZoomButtonToShow = false;
        SharedPrefsHelper.setZoomForDetailButtonToShow(this, false);
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

    private void setReturnToVenueVisible(boolean show) {
        final float buttonAlpha = mReturnToVenueButton.getAlpha();
        if (show && buttonAlpha < 0.1f) {
            mReturnToVenueButton.setVisibility(View.VISIBLE);
            final String target = mSelectionManager.getSelectionLabelForReturnToVenue();
            mReturnToVenueButton.setText(String.format(getResources().getString(R.string.return_to_venue_text), target));
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mReturnToVenueButton, "alpha", 0f, 1f);
            objectAnimator.setDuration(500L);
            objectAnimator.start();
        } else if (!show && (buttonAlpha > 0.1)) {
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mReturnToVenueButton, "alpha", 1f, 0f);
            objectAnimator.setDuration(500L);
            objectAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mReturnToVenueButton.setVisibility(View.GONE);
                }
            });
            objectAnimator.start();
        }
    }

    @Nullable
    public PositionProvider getCurrentPositionProvider() {
        return mPositionManager.getCurrentProvider();
    }

    public boolean isDrawerOpen() {
        return (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START));
    }

    public void closeDrawer() {
        runOnUiThread(() -> {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            mDrawerLayout.setClickable(false);
        });
    }

    public void setSideMenuEnabled(boolean enabled) {
        if (mSideMenuVisibility == enabled) {
            return;
        }

        if (mDrawerLayout != null) {
            mDrawerLayout.setDrawerLockMode(enabled ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }

        androidx.appcompat.app.ActionBar ab = getSupportActionBar();

        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(enabled);
        }

        mSideMenuVisibility = enabled;
    }

    private void resetDrawer() {
        if (mDrawerLayout != null) {
            mDrawerLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    //Remove the listener before proceeding
                    mDrawerLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mDrawerLayout.closeDrawer(GravityCompat.START, false);
                    setSideMenuEnabled(false);
                }
            });
        }
    }

    //region Implements OnMapClickListener
    @Override
    public boolean onMapClick(@NonNull LatLng point, List<MPLocation> locations) {
        if (locations == null || locations.get(0) == null) {
            return false;
        } else {
            MPLocation location = locations.get(0);
            mActivity.runOnUiThread(() -> {
                mSelectionManager.selectLocation(location, false, true, true, true);
                mMenuFragment.openLocationMenu(location);
            });
            return true;
        }
    }
    //endregion

    //region Implements OnMapLongClickListener
    @Override
    public boolean onMapLongClick(@NonNull LatLng point) {
        return false;
    }
    //endregion

    //region Implements GoogleMap.OnMarkerClickListener
    @Override
    public boolean onMarkerClick(@Nullable Marker marker) {
        if (marker == null) {
            return false;
        }

        mUserPositionTrackingViewModel.stopTracking();
        boolean clickWasHandled = false;
        String title = marker.getTitle();

        if (title != null) {
            MPLocation location = mMapControl.getLocation(marker);
            if (location != null) {
                // Report the click event to Analytics...
                final Bundle eventParams = new Bundle();
                eventParams.putString("Location", location.getName());

                GoogleAnalyticsManager.reportEvent(
                        getString(R.string.fir_event_tapped_location_on_map),
                        eventParams
                );

                mMenuFragment.openLocationMenu(location);
                clickWasHandled = true;
            }
        }
        return clickWasHandled;
    }
    //endregion


    public void setOpenLocationMenuFromInfowindowClick(boolean state) {
        openLocationMenuFromInfowindowClick = state;
        if (openLocationMenuFromInfowindowClick) {
            mPoiMarkerCustomInfoWindowAdapter.setInfoWindowType(POIMarkerInfoWindowAdapter.INFOWINDOW_TYPE_LINK);
        } else {
            mPoiMarkerCustomInfoWindowAdapter.setInfoWindowType(POIMarkerInfoWindowAdapter.INFOWINDOW_TYPE_NORMAL);
        }
    }

    //region Implements GoogleMap.OnInfoWindowClickListener
    @Override
    public void onInfoWindowClick(Marker marker) {
        MPLocation loc = mMapControl.getLocation(marker);
        if (openLocationMenuFromInfowindowClick && loc != null) {
            mMenuFragment.openLocationMenu(loc, true);
            openDrawer(true);
        }
    }
    //endregion

    @Nullable
    public GoogleMap getGoogleMap() {
        return mGoogleMap;
    }

    @Nullable
    public SelectionManager getSelectionManager() {
        return mSelectionManager;
    }

    @Nullable
    public UserRolesManager getUserRolesManager() {
        return mUserRolesManager;
    }

    public void setPositioningBtnBottomPadding(int margin) {
        ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) followMeButton.getLayoutParams();
        int defaultBottomPadding = (int) (defaultFollowMeButtonBottomMarginDP * getResources().getDisplayMetrics().density);
        marginParams.setMargins(marginParams.leftMargin, marginParams.topMargin, marginParams.rightMargin, defaultBottomPadding + margin);
    }

    public void setFollowMeBtnBottomMarginToDefault() {
        int bottomPadding = (int) (defaultFollowMeButtonBottomMarginDP * getResources().getDisplayMetrics().density);
        ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) followMeButton.getLayoutParams();
        marginParams.setMargins(marginParams.leftMargin, marginParams.topMargin, marginParams.rightMargin, bottomPadding);
    }

    public UserPositionTrackingViewModel getUserPositionTrackingViewModel() {
        return mUserPositionTrackingViewModel;
    }

    @Nullable
    public BaseFragment getFragment(MenuFrame frag) {
        final BaseFragment[] myFragments = getMyFragments();
        if (frag.ordinal() < myFragments.length) {
            return myFragments[frag.ordinal()];
        } else {
            return null;
        }
    }

    @AnyThread
    public void reportOnDataUpdatedToFragments() {
        runOnUiThread(() -> {
            for (BaseFragment frag : getMyFragments()) {
                if (frag != null && !frag.isDetached() && !frag.isRemoving() && frag.isAvailable()) {
                    frag.onDataUpdated();

                }
            }
        });
    }

    public void reportOnDrawerEventToFragments(DrawerState newState, DrawerState prevState) {
        for (BaseFragment frag : getMyFragments()) {
            if (frag != null && frag.isFragmentSafe()) {
                frag.onDrawerEvent(newState, prevState);
            }
        }
    }

    /**
     * Checks if any of the fragments is handling the back press ...
     *
     * @return {@code true} if none of the available fragments did handle the backpress {@code false} otherwise
     */
    private boolean canExitOnBackPressed() {
        boolean canCallSuper = true;
        for (BaseFragment frag : getMyFragments()) {
            if (frag != null && frag.isFragmentSafe() && frag.onBackPressed()) {
                canCallSuper = false;
                break;
            }
        }
        return canCallSuper;
    }

    /**
     * syncData synchronize content callback. It is invoked only once
     */
    private void onSyncDataReady() {
        if (isActivityFinishing(mActivity)) {
            return;
        }
        onMapsIndoorsDataUpdates(null);
    }

    private void syncData(boolean invokedFromActOnCreate) {
        // to avoid interruptions then it will not start another sync again
        unregisterConnectivityBroadcastReveiver();
        final boolean internetConnectivity = MapsIndoorsUtils.isNetworkReachable(this);
        isNetworkReachable = internetConnectivity;

        // Check for offline data availability BEFORE the first data sync...
        final boolean hasOfflineData = MapsIndoors.checkOfflineDataAvailability();
        final boolean apiKeyValidity = SharedPrefsHelper.getApiKeyValidity(MapsIndoorsActivity.this);

        if (!internetConnectivity && !apiKeyValidity) {
            MapsIndoorsUtils.showInvalidAPIKeyDialogue(this);
        } else if (hasOfflineData || internetConnectivity) {
            final long timeNow = System.currentTimeMillis();
            final long dt = Math.abs(timeNow - syncDataTimestamp);
            final boolean runDataSync;
            if (dt > 15000) {
                syncDataTimestamp = timeNow;
                final boolean isSplashScreenVisible = isSplashScreenVisible();
                runDataSync = invokedFromActOnCreate || !isSplashScreenVisible;
            } else {
                runDataSync = false;
            }

            if (runDataSync) {
                //  Once the initial setup is done, we can either trigger a manual data sync
                MapsIndoors.synchronizeContent(error -> {
                    if (error == null) {
                        SharedPrefsHelper.setApiKeyValidity(MapsIndoorsActivity.this, true);
                        if (!invokedFromActOnCreate) {
                            onSyncDataReady();
                        }
                    } else {
                        if (error.code == MIError.INVALID_API_KEY) {
                            MapsIndoorsUtils.showInvalidAPIKeyDialogue(this);
                        }
                    }
                });
            }
        } else {
            dataLoadingFinished();
            setNoAvailableNetworkFragmentVisibility(true);
            if (!mMapsIndoorsDataIsReady) {
                if (mTopSearchField != null) {
                    mTopSearchField.setToolbarText(getResources().getString(R.string.app_name), false);
                }
            }
        }
    }

    @NonNull
    private MPLocationSourceOnStatusChangedListener onLocationSourceOnStatusChangedListener = (status, sourceId) -> {
        if (status == MPLocationSourceStatus.AVAILABLE) {
            if (!mLocationSourceStatusChangedListenerInvoked) {
                mLocationSourceStatusChangedListenerInvoked = true;
                onLocationSourceStatusChangedToAvailable();
            }
        }
    };

    /**
     * Invoked upon a venue change
     */
    public void userHasChosenNewVenue() {
        mFloorSelectorHiddingAtStart = true;
        if (mMapFloorSelector != null) {
            mMapControl.enableFloorSelector(false);
        }
    }

    //region POC APP ONLY
    void prepareNextActivity() {
        if (BuildConfig.FLAVOR.contains("poc_app")) {
            try {
                final Class<?> innerClass = Class.forName("com.mapsindoors.stdapp.ui.pocactivitymain.POCMainActivity");
                if (mActivity != null) {
                    mActivity.removeListeners();
                }
                // Location data
                MapsIndoors.removeLocationSourceOnStatusChangedListener(onLocationSourceOnStatusChangedListener);
                MapsIndoors.onApplicationTerminate();

                final long timeToWait = 0;

                final Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(() -> startNextActivity(innerClass), timeToWait);

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        MPQuery query = new MPQuery.Builder().setQuery("testQuery").build();
    }

    void startNextActivity(@NonNull Class clazz) {
        if (BuildConfig.FLAVOR.contains("poc_app")) {
            final Intent intent = new Intent(mActivity, clazz);
            startActivity(intent);

            @Nullable final FragmentActivity activity = mActivity;
            if (activity != null) {
                activity.overridePendingTransition(0, 0);
                activity.finish();
            }
        }
    }
    //endregion

    //region LIVE DATA
    void enableLiveData() {
        LiveDataManager.getInstance().getActiveLiveData(activeLiveDataModel -> {
            if (activeLiveDataModel != null && activeLiveDataModel.getDomainTypes() != null) {
                for (String domainType : activeLiveDataModel.getDomainTypes()) {
                    switch (domainType) {
                        case LiveDataDomainTypes.AVAILABILITY_DOMAIN:
                        case LiveDataDomainTypes.OCCUPANCY_DOMAIN:
                        case LiveDataDomainTypes.POSITION_DOMAIN:
                            mMapControl.enableLiveData(domainType);
                            break;
                        default:
                            break;
                    }
                }
            }
        });
    }
}