package com.mapsindoors.stdapp.ui.locationmenu;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.mapsindoors.mapssdk.Building;
import com.mapsindoors.mapssdk.DataField;
import com.mapsindoors.mapssdk.Highway;
import com.mapsindoors.mapssdk.ImageProvider;
import com.mapsindoors.mapssdk.LocationPropertyNames;
import com.mapsindoors.mapssdk.MPLocation;
import com.mapsindoors.mapssdk.MPRoutingProvider;
import com.mapsindoors.mapssdk.MapControl;
import com.mapsindoors.mapssdk.MapsIndoors;
import com.mapsindoors.mapssdk.Point;
import com.mapsindoors.mapssdk.PositionResult;
import com.mapsindoors.mapssdk.RouteLeg;
import com.mapsindoors.mapssdk.RoutingProvider;
import com.mapsindoors.mapssdk.Solution;
import com.mapsindoors.mapssdk.UserRole;
import com.mapsindoors.mapssdk.dbglog;
import com.mapsindoors.mapssdk.errors.MIError;
import com.mapsindoors.stdapp.BuildConfig;
import com.mapsindoors.stdapp.R;
import com.mapsindoors.stdapp.apis.googleplaces.GooglePlacesClient;
import com.mapsindoors.stdapp.apis.googleplaces.models.AddressComponent;
import com.mapsindoors.stdapp.apis.googleplaces.models.ReverseGeocodeResult;
import com.mapsindoors.stdapp.helpers.MapsIndoorsHelper;
import com.mapsindoors.stdapp.helpers.MapsIndoorsRouteHelper;
import com.mapsindoors.stdapp.helpers.MapsIndoorsSettings;
import com.mapsindoors.stdapp.helpers.MapsIndoorsUtils;
import com.mapsindoors.stdapp.helpers.SharedPrefsHelper;
import com.mapsindoors.stdapp.listeners.GenericObjectResultCallback;
import com.mapsindoors.stdapp.managers.GoogleAnalyticsManager;
import com.mapsindoors.stdapp.managers.SelectionManager;
import com.mapsindoors.stdapp.managers.UserRolesManager;
import com.mapsindoors.stdapp.ui.activitymain.MapsIndoorsActivity;
import com.mapsindoors.stdapp.ui.booking.BookingServiceFragment;
import com.mapsindoors.stdapp.ui.common.adapters.IconTextListAdapter;
import com.mapsindoors.stdapp.ui.common.enums.MenuFrame;
import com.mapsindoors.stdapp.ui.common.fragments.BaseFragment;
import com.mapsindoors.stdapp.ui.common.models.IconTextElement;
import com.mapsindoors.stdapp.ui.components.noInternetBar.NoInternetBar;
import com.mapsindoors.stdapp.ui.direction.DirectionsVerticalFragment;

import java.util.ArrayList;
import java.util.List;

public class LocationMenuFragment extends BaseFragment {
    private static final String TAG = LocationMenuFragment.class.getSimpleName();


    public static final int FLIPPER_ROUTE_ESTIMATION = 1;
    public static final int FLIPPER_NO_INTERNET = 0;

    private boolean mClickedOnMarker;

    private Context mContext;
    private MapsIndoorsActivity mActivity;
    private ListView mMainMenuList;
    private MPLocation mLocation;

    private TextView mTitleTextView;
    private IconTextListAdapter myAdapter;
    private MapControl mMapControl;
    private DirectionsVerticalFragment mDirectionsFullMenuFragment;

    private ImageView mTopImage;

    private ImageButton mBackButton;
    private ImageButton mShareButton;

    private Button mShowOnMapButton;
    private Button mShowRouteButton;

    private MPLocation mLastRouteOriginLocation;
    private MPLocation mLastRouteDestinationLocation;
    private GooglePlacesClient mGooglePlacesClient;

    //Attributes for the async request
    private boolean mRouteEstimationDone = false;
    private boolean mOriginPositionTextDone = false;
    private String mRouteEstimation;
    private String mOriginPositionText;

    //  private View mRouteInfoView;
    private TextView mRouteInfoMainTextView, mRouteInfoSubTextView;
    private ImageView mRouteInfoIconView;
    private ViewFlipper routeEstimationViewFlipper;

    private NoInternetBar noInternetBar;

    private Bitmap mVenueImageBitmap;
    private String mLocationImageUrl;

    private BookingServiceFragment mBookingServiceFragment;

    public LocationMenuFragment() {
        super();
    }


    //region Fragment lifecycle events
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mMainView == null) {
            mMainView = inflater.inflate(R.layout.fragment_locationmenu, container);
        }
        return mMainView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupView(view);
    }
    //endregion


    private void setupView(View view) {

        mBackButton = view.findViewById(R.id.locations_back_button);
        mShareButton = view.findViewById(R.id.locations_share_button);
        routeEstimationViewFlipper = view.findViewById(R.id.route_estimation_flipper);
        mTopImage = view.findViewById(R.id.locationmenu_topimage);

        mTitleTextView = view.findViewById(R.id.locationTitleId);

        // Route Info
        {
            mRouteInfoMainTextView = view.findViewById(R.id.ctrl_mainmenu_textitem_main);
            mRouteInfoSubTextView = view.findViewById(R.id.ctrl_mainmenu_textitem_sub);
            mRouteInfoSubTextView.setVisibility(View.GONE);

            View iconItem = view.findViewById(R.id.ctrl_mainmenu_iconitem);
            iconItem.setVisibility(View.INVISIBLE);
            mRouteInfoIconView = view.findViewById(R.id.ctrl_mainmenu_iconitem_tint);
            mRouteInfoIconView.setVisibility(View.VISIBLE);
        }

        mMainMenuList = view.findViewById(R.id.locationmenu_itemlist);
        mShowOnMapButton = view.findViewById(R.id.locations_gotobutton);
        mShowRouteButton = view.findViewById(R.id.locations_routebutton);

        noInternetBar = view.findViewById(R.id.location_frag_no_internet_bar);

        noInternetBar.setOnClickListener(v -> {
            noInternetBar.setState(NoInternetBar.REFRESHING_STATE);
            loadTopImage(mLocationImageUrl, mVenueImageBitmap);
            distanceEstimation();
        });

        mFragment = MenuFrame.MENU_FRAME_LOCATION_MENU;

        mBookingServiceFragment = (BookingServiceFragment) getChildFragmentManager().findFragmentById(R.id.booking_fragment);
    }


    public void init(Context context, MapControl mapControl) {
        mContext = context;
        mActivity = (MapsIndoorsActivity) mContext;
        mMapControl = mapControl;

        mLastRouteOriginLocation = mLastRouteDestinationLocation = null;

        mGooglePlacesClient = new GooglePlacesClient();

        mDirectionsFullMenuFragment = ((MapsIndoorsActivity) context).getVerticalDirectionsFragment();

        mDirectionsFullMenuFragment.init(context, mapControl);

        mBackButton.setOnClickListener(view -> closeMenu());

        mShowOnMapButton.setOnClickListener(view -> {
            mActivity.closeDrawer();

            GoogleAnalyticsManager.reportScreen(context.getString(R.string.fir_screen_Show_Location_On_Map), mActivity);

            final SelectionManager selectionManager = mActivity.getSelectionManager();
            if (selectionManager != null) {
                selectionManager.selectLocation(mLocation, true, true, true, false);
            }
        });


        mShowRouteButton.setOnClickListener(v -> mDirectionsFullMenuFragment.open(mLocation));

        shareButtonSetup();
    }

    public void closeMenu() {
        mActivity.menuGoBack();
        resetRouteInfoView();
        // invalidate the estimation route calculated in this view
        mActivity.invalidateLastRoute();
    }

    private void shareButtonSetup() {
        boolean addShareButton = false;
        Solution solution = MapsIndoors.getSolution();
        if (solution != null) {
            String mapClientUrl = solution.getMapClientUrl();
            if (!TextUtils.isEmpty(mapClientUrl)) {
                addShareButton = true;
            }
        }

        if (addShareButton) {
            mShareButton.setVisibility(View.VISIBLE);
            mShareButton.setOnClickListener(v -> {
                GoogleAnalyticsManager.reportEvent(getString(R.string.fir_event_Location_Share), null);

                final String locationId = mLocation.getId();
                final String venueId = ((MapsIndoorsActivity) mContext).getCurrentVenueId();

                final Solution miSolution = MapsIndoors.getSolution();
                final String shareBody = miSolution.parseMapClientUrl(venueId, locationId);

                if (shareBody != null) {
                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                    startActivity(Intent.createChooser(sharingIntent, "share using"));
                } else {
                    if (dbglog.isDeveloperMode()) {
                        Toast.makeText(mContext, "ShareButton clicked: parsing the MapClientUrl failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public Bitmap mPoiPrevLogo;

    public void setLocation(MPLocation location, Bitmap bitmap, Bitmap logo, boolean clickedOnMarker) {
        mPoiPrevLogo = logo;

        if (BuildConfig.DEBUG) {
            if ((logo != null) && MapsIndoorsUtils.checkIfBitmapIsEmpty(logo)) {
                dbglog.Log(TAG, "");
            }
        }

        mClickedOnMarker = clickedOnMarker;

        setLocation(location, bitmap);

        final SelectionManager selectionManager = mActivity.getSelectionManager();
        if (selectionManager != null) {
            selectionManager.selectLocation(mLocation, true, true, true, clickedOnMarker);
        }
    }

    void setLocation(@NonNull final MPLocation location, final Bitmap topImageBitmap) {
        // Reset the Info view before anything else
        resetRouteInfoView();

        mLocation = location;
        mVenueImageBitmap = topImageBitmap;
        final String locationName = mLocation.getName();

        mTitleTextView.setText(locationName);
        mShowRouteButton.setText(mContext.getString(R.string.toolbar_label_directions));

        final String imageUrl = (String) location.getProperty(LocationPropertyNames.IMAGE_URL);

        mLocationImageUrl = imageUrl;

        loadTopImage(imageUrl, topImageBitmap);

        //
        mLastRouteDestinationLocation = mLocation;
        PositionResult userPosition = mMapControl.getCurrentPosition();

        if(userPosition != null) {
            Building building = MapsIndoors.getBuildings().getBuilding(mActivity.getCurrentUserPos().getLatLng());
            if (building != null) {
                mLastRouteOriginLocation = new MPLocation.Builder("UserLocation")
                        .setPosition(userPosition.getPoint())
                        .setFloor(userPosition.getFloor())
                        .setFloorName(building.getFloorByZIndex(userPosition.getFloor()).getDisplayName())
                        .setBuilding(building.getAdministrativeId())
                        .setName(getString(R.string.my_position))
                        .build();
            }else {
                mLastRouteOriginLocation = new MPLocation.Builder("UserLocation")
                        .setPosition(userPosition.getPoint())
                        .setName(getString(R.string.my_position))
                        .build();
            }
        }

        final ArrayList<IconTextElement> elements = new ArrayList<>();

        includeMiscInfoFields(elements);
        includeInformationField(elements);
        includeBuildingInformationField(elements);
        includeDescriptionField(elements);

        if(mBookingServiceFragment != null){
            mBookingServiceFragment.setLocation(mLocation);
        }

        // calculate the distance to the destination
        distanceEstimation();

        myAdapter = new IconTextListAdapter(mContext);
        myAdapter.setTint("@color/primary");
        myAdapter.setList(elements);
        mMainMenuList.setAdapter(myAdapter);
    }

    void includeMiscInfoFields(@NonNull List<IconTextElement> infoItemList) {
        final String openingHours = getFieldValue("openinghours", mLocation);
        final String phone = getFieldValue("phone", mLocation);
        final String website = getFieldValue("website", mLocation);

        addElement(infoItemList, openingHours, "", R.drawable.ic_access_time_white_24dp, IconTextListAdapter.Objtype.OPENINGHOURS);
        addElement(infoItemList, phone, "", R.drawable.ic_phone_white_24dp, IconTextListAdapter.Objtype.PHONE);
        addElement(infoItemList, website, "", R.drawable.ic_web, IconTextListAdapter.Objtype.URL);
    }

    void includeInformationField(@NonNull List<IconTextElement> infoItemList) {
        final String[] catNames = MapsIndoorsHelper.getLocationCategoryNames(mLocation);
        final String externalId = mLocation.getExternalId();

        final boolean gotCategories = catNames.length > 0;
        final boolean gotExternalId = !TextUtils.isEmpty(externalId);

        if (gotCategories || gotExternalId) {
            final StringBuilder sb = new StringBuilder();

            if (gotCategories) {
                final int catCount = catNames.length;

                // Add the first category
                sb.append(catNames[0].trim());

                // Add any others, comma separated
                for (int i = 1; i < catCount; i++) {
                    sb.append(", ").append(catNames[i].trim());
                }
            }

            if (gotExternalId) {
                // Add a line break if categories were previously added
                if (gotCategories) {
                    sb.append('\n');
                }

                sb.append(externalId.trim());
            }

            addElement(infoItemList, sb.toString(), "", R.drawable.ic_info_poi_type_24dp, IconTextListAdapter.Objtype.PLACE);
        }
    }

    void includeDescriptionField(@NonNull List<IconTextElement> infoItemList) {
        final String locationDescription = mLocation.getDescription();
        addElement(infoItemList, locationDescription, "", R.drawable.ic_description_white_24px, IconTextListAdapter.Objtype.PLACE);
    }

    void includeBuildingInformationField(@NonNull List<IconTextElement> infoItemList) {
        final String locationPlace = MapsIndoorsHelper.composeLocationInfoString(
                mLocation,
                mActivity.getVenueCollection(),
                mActivity.getBuildingCollection(),
                MapsIndoorsHelper.FORMAT_LOCATION_INFO_STRING_USE_LINE_BREAKS,
                false,
                mContext
        );

        addElement(infoItemList, locationPlace, "", R.drawable.ic_location_city_white_24dp, IconTextListAdapter.Objtype.PLACE);
    }

    void distanceEstimation() {
        if (mLastRouteOriginLocation == null || mLastRouteDestinationLocation == null) {
            return;
        }

        Context context = getContext();
        if (context == null) {
            if (BuildConfig.DEBUG) {
                dbglog.LogW(TAG, "distanceEstimation() - Context is null");
            }
            return;
        }

        Point origin = mLastRouteOriginLocation.getPoint();
        Point destination = mLastRouteDestinationLocation.getPoint();
        int floorZIndex = 0;

        //If the latest position is at 0,0 it's not updated yet. Using the camera position instead.
        //If you want a default position before we get any signals from our position providers, use setCurrentPosition() on mapscontrol.
        if (origin.getLat() == 0 && origin.getLng() == 0) {
            if ((mActivity != null) && (mActivity.getGoogleMap() != null)) {
                LatLng target = mActivity.getGoogleMap().getCameraPosition().target;
                origin = new Point(target.latitude, target.longitude, floorZIndex);
            } else {
                if (BuildConfig.DEBUG) {
                    dbglog.LogW(TAG, "distanceEstimation() - Either the activity or the Google Map Object were null");
                }
                return;
            }
        }

        int travelMode = SharedPrefsHelper.getUserTravelingMode(context);
        if (travelMode == MapsIndoorsHelper.VEHICLE_NONE) {
            double distance = destination.distanceTo(origin);
            travelMode = (distance < MapsIndoorsSettings.ROUTING_MAX_WALKING_DISTANCE_IN_METERS) ? MapsIndoorsHelper.VEHICLE_WALKING : MapsIndoorsHelper.VEHICLE_DRIVING;
        }

        final int vehicleIcon = MapsIndoorsHelper.getTravelModeIcon(travelMode);

        Point userCPos = mActivity.getCurrentUserPos();
        if (userCPos != null) {
            if (MapsIndoorsUtils.isNetworkReachable(context)) {
                getEstimationInsideBuilding(userCPos, travelMode, vehicleIcon);
            } else {
                showNoInternetMessage();
            }
        } else {
            if (BuildConfig.DEBUG) {
                Toast.makeText(mContext, "The position provider is not returning a position", Toast.LENGTH_SHORT).show();
            }
        }
    }

    void showNoInternetMessage() {
        noInternetBar.setState(NoInternetBar.MESSAGE_STATE);
        routeEstimationViewFlipper.setVisibility(View.VISIBLE);
        routeEstimationViewFlipper.setDisplayedChild(FLIPPER_NO_INTERNET);
    }

    public MPLocation getLocation() {
        return mLocation;
    }

    /**
     * The given element will only be added if its text field is not empty
     *
     * @param elements
     * @param text
     * @param imgId
     * @param type
     */
    private void addElement(List<IconTextElement> elements, String text, String subText, Integer imgId, IconTextListAdapter.Objtype type) {
        if (!TextUtils.isEmpty(text)) {
            if (type == IconTextListAdapter.Objtype.ROUTE) {
                elements.add(new IconTextElement(text, subText, imgId, text, type));
            } else {
                elements.add(new IconTextElement(text, imgId, text, type));
            }
        }
    }

    private String getFieldValue(@Nullable String fieldName, @NonNull MPLocation location) {
        final DataField data = location.getField(fieldName);
        return data == null ? "" : data.getValue();
    }

    //Gets an image using an imageURL and sets the icon to the resulting image.
    public void loadImage(final String imageURL) {
        //ImageUrl seems to contain a link. Load it in a new thread and change the image when loaded.
        ImageProvider imageProvider = MapsIndoors.getImageProvider();

        imageProvider.loadImageAsync(imageURL, ((result, error) -> {
            if (result != null) {
                //Runs on UI thread to avoid a exception
                getActivity().runOnUiThread(() -> mTopImage.setImageBitmap(result));
            }
        }));
    }

    void loadTopImage(String imageUrl, Bitmap venueBitmap) {
        if (TextUtils.isEmpty(imageUrl)) {
            mTopImage.setImageBitmap(venueBitmap);
        } else {
            loadImage(imageUrl);
        }
    }


    @Override
    public boolean onBackPressed() {
        if (isActive()) {
            if (!mActivity.isDrawerOpen()) {
                mActivity.openDrawer(true);
            } else {
                if (mClickedOnMarker) {
                    mClickedOnMarker = false;
                    mActivity.closeDrawer();
                    return true;
                } else {
                    closeMenu();
                }
            }
        }

        return true;
    }
    //endregion


    /**
     * @param origin
     * @param destination
     * @param travelMode
     * @param genericObjectResultCallback
     */
    void getRouteEstimation(MPLocation origin, MPLocation destination, final int travelMode, final GenericObjectResultCallback<String> genericObjectResultCallback) {
        //Location data acquired. Find the route from current position to that location.
        RoutingProvider routingProvider = new MPRoutingProvider();

        // Localize the textual content of the directions service
        routingProvider.setLanguage(MapsIndoors.getLanguage());

        // Add some variables to the route query
        routingProvider.setTravelMode(MapsIndoorsHelper.getTravelModeFromInt(travelMode));

        // Get the default value for the avoid stairs switch and use it for this first query
        routingProvider.clearRouteRestrictions();

        final boolean avoidStairsSwitchSet = SharedPrefsHelper.getAvoidStairs(mContext);
        if (avoidStairsSwitchSet) {
            routingProvider.addRouteRestriction(Highway.STEPS);
        }

        routingProvider.setOnRouteResultListener((route, error) -> {

            if (MapsIndoorsActivity.isActivityFinishing(mActivity)) {
                return;
            }

            final String routeText;

            if (route != null) {
                if (BuildConfig.DEBUG && dbglog.isDeveloperMode()) {
                    List<RouteLeg> legs = route.getLegs();
                    for (RouteLeg leg : legs) {
                        Point legStart = leg.getStartPoint();
                        Point legEnd = leg.getEndPoint();
                    }
                }

                mActivity.setLastRoute(route, mLastRouteOriginLocation, mLastRouteDestinationLocation);

                @StringRes int meansStringId = MapsIndoorsHelper.getTravelMeanFromIntTravelMode(travelMode);

                routeText = String.format(getString(meansStringId), MapsIndoorsRouteHelper.getFormattedDuration(route.getDuration()));
            } else {
                if (error != null) {
                    if (error.code == MIError.INVALID_API_KEY) {
                        MapsIndoorsUtils.showInvalidAPIKeyDialogue(getContext());
                    }
                }

                //an error while getting the route so the internet bar should show
                noInternetBar.setState(NoInternetBar.MESSAGE_STATE);
                routeText = null;
            }

            if (BuildConfig.DEBUG) {
                dbglog.Log(TAG, "LocationMenuFragment.setLocation -> onRouteResult");
            }

            new Handler(mContext.getMainLooper()).post(() -> genericObjectResultCallback.onResultReady(routeText));
        });

        if (BuildConfig.DEBUG && dbglog.isDeveloperMode()) {
            Point op = origin.getPoint();

            dbglog.LogI(TAG, "Origin: LAT=" + op.getLat() + " / LNG=" + op.getLng());
        }

        final Point originPoint = origin.getPoint();
        final Point destinationPoint = destination.getPoint();

        String logMsg = "Origin: "+originPoint.getCoordinatesAsString() + "/Destination: " + destinationPoint.getCoordinatesAsString()+ "/"+ "TravelMode: " +MapsIndoorsHelper.getTravelModeFromInt( travelMode ) ;
        FirebaseCrashlytics.getInstance().log(logMsg);

        // Set route restrictions based on the current user's available/preferred user roles
        {
            final List<UserRole> savedUserRoles;

            final UserRolesManager userRolesManager = mActivity.getUserRolesManager();
            if (userRolesManager != null) {
                savedUserRoles = userRolesManager.getSavedUserRoles();
            } else {
                savedUserRoles = null;
            }

            // IMPORTANT: ALWAYS SET THE USER ROLES / CLEAR PREVIOUS SET
            routingProvider.setUserRoles(savedUserRoles);
        }

        routingProvider.query(
                new Point(originPoint.getLat(), originPoint.getLng(), origin.getFloor()),
                new Point(destinationPoint.getLat(), destinationPoint.getLng(), destination.getFloor())
        );
    }

    /**
     * @param userCPos
     * @param genericObjectResultCallback
     * @see <a href="https://developers.google.com/maps/documentation/geocoding/intro#Types">Geocoding types</a>
     */
    void getOutsideOriginPositionText(@Nullable Point userCPos, @Nullable final GenericObjectResultCallback<String> genericObjectResultCallback) {
        if (userCPos == null || genericObjectResultCallback == null) {
            if (genericObjectResultCallback != null) {
                genericObjectResultCallback.onResultReady(null);
            }
            return;
        }

        LatLng userPos = userCPos.getLatLng();

        // outside a building
        mGooglePlacesClient.getLatLngAddress(userPos, getString(R.string.google_maps_key), reverseGeocodeResults -> new Handler(mContext.getMainLooper()).post(() -> {
            if (reverseGeocodeResults == null) {
                genericObjectResultCallback.onResultReady(null);
                return;
            }

            String address, streetNumber, streetName;
            address = streetNumber = streetName = null;

            outerLoop:
            for (ReverseGeocodeResult geoResult : reverseGeocodeResults.results) {
                if (geoResult.address_components != null) {

                    // look for the street number and route
                    for (AddressComponent adCpt : geoResult.address_components) {
                        if (adCpt.types[0].equals("street_number")) {
                            streetNumber = adCpt.long_name;
                        }

                        if (adCpt.types[0].equals("route")) {
                            streetName = adCpt.long_name;
                        }

                        if (streetName != null) {
                            if (streetNumber != null) {
                                address = String.format(getString(R.string.location_address_street_name_number), streetName, streetNumber);
                            } else {
                                address = String.format(getString(R.string.location_address_street_name), streetName);
                            }

                            break outerLoop;
                        }
                    }
                }
            }

            genericObjectResultCallback.onResultReady(address);
        }));
    }

    void getEstimationOutsideBuilding(@Nullable Point userCPos, int travelMode, final int vehicleIcon) {

        mRouteEstimationDone = mOriginPositionTextDone = false;

        // Get the estimation
        getRouteEstimation(mLastRouteOriginLocation, mLastRouteDestinationLocation, travelMode, routeEstimationText ->
        {
            mRouteEstimationDone = true;

            if (routeEstimationText != null) {
                mRouteEstimation = routeEstimationText.toString();
            } else {
                // No route found
                mRouteEstimation = null;
                mOriginPositionText = null;
            }

            if (mRouteEstimationDone && mOriginPositionTextDone) {

                if (mRouteEstimation != null) {
                    updateRouteInfoView(mRouteEstimation, mOriginPositionText, vehicleIcon);
                }

                mRouteEstimationDone = mOriginPositionTextDone = false;
            }
        });

        // Get the origin text from google places
        getOutsideOriginPositionText(userCPos, revGeolocationText ->
        {
            mOriginPositionTextDone = true;

            // Check if we've got an address
            if (revGeolocationText != null) {
                mOriginPositionText = String.format(getString(R.string.from_param), revGeolocationText.toString());
            } else {
                // ... if not, set this guy to null so the route estimation string will fall and align
                // with the walk/drive icon
                mOriginPositionText = null;
            }

            if (mRouteEstimationDone && mOriginPositionTextDone) {

                if (mRouteEstimation != null) {
                    updateRouteInfoView(mRouteEstimation, mOriginPositionText, vehicleIcon);
                }

                mRouteEstimationDone = mOriginPositionTextDone = false;
            }
        });
    }

    void getEstimationInsideBuilding(final Point userCPos, int travelMode, final int vehicleIcon) {
        // get the nearest POI first
        mActivity.getNearestLocationToTheUser((locations, error) -> {

            if (error != null) {
                if (error.code == MIError.INVALID_API_KEY) {
                    MapsIndoorsUtils.showInvalidAPIKeyDialogue(getContext());
                }

                mRouteEstimation = mOriginPositionText = null;
                resetRouteInfoView();

                new Handler(mContext.getMainLooper()).post(() -> noInternetBar.setState(NoInternetBar.MESSAGE_STATE));
            } else {
                new Handler(mContext.getMainLooper()).post(() -> {
                    if ((locations != null) && (locations.size() > 0)) {
                        mLastRouteOriginLocation = locations.get(0);

                        // get the route estimation after getting the closest POI
                        getRouteEstimation(mLastRouteOriginLocation, mLastRouteDestinationLocation, travelMode, routeEstimationText -> {
                            if (routeEstimationText != null) {
                                mRouteEstimation = routeEstimationText.toString();
                            } else {
                                // ... if not, set this guy to null so the route estimation string will fall and align
                                // with the walk/drive icon
                                mRouteEstimation = null;
                                mOriginPositionText = null;
                            }

                            mOriginPositionText = String.format(getString(R.string.from_param), mLastRouteOriginLocation.getName());

                            if (mRouteEstimation != null) {
                                updateRouteInfoView(mRouteEstimation, mOriginPositionText, vehicleIcon);
                            }
                        });
                    } else {
                        getEstimationOutsideBuilding(userCPos, travelMode, vehicleIcon);
                    }
                });
            }
        });
    }

    void resetRouteInfoView() {
        if (routeEstimationViewFlipper != null) {
            if (routeEstimationViewFlipper.getVisibility() != View.GONE) {
                routeEstimationViewFlipper.setVisibility(View.GONE);
            }
            noInternetBar.setState(NoInternetBar.MESSAGE_STATE);
        }

    }

    void updateRouteInfoView(String text, String subText, @DrawableRes Integer vehicleIcon) {

        if (routeEstimationViewFlipper != null) {
            if (routeEstimationViewFlipper.getVisibility() != View.VISIBLE) {
                routeEstimationViewFlipper.setVisibility(View.VISIBLE);
            }

            if (!TextUtils.isEmpty(text)) {
                mRouteInfoMainTextView.setText(text);
            }

            if (!TextUtils.isEmpty(subText)) {
                mRouteInfoSubTextView.setText(subText);
                mRouteInfoSubTextView.setVisibility(View.VISIBLE);
            } else {
                mRouteInfoSubTextView.setVisibility(View.GONE);
            }

            mRouteInfoIconView.setImageResource(vehicleIcon);
            routeEstimationViewFlipper.setDisplayedChild(FLIPPER_ROUTE_ESTIMATION);

            noInternetBar.setState(NoInternetBar.MESSAGE_STATE);

        }
    }

}