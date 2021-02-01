package com.mapsindoors.stdapp.ui.menumain;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mapsindoors.mapssdk.BuildingCollection;
import com.mapsindoors.mapssdk.LocationPropertyNames;
import com.mapsindoors.mapssdk.MPFilter;
import com.mapsindoors.mapssdk.MPLocation;
import com.mapsindoors.mapssdk.MPQuery;
import com.mapsindoors.mapssdk.MapControl;
import com.mapsindoors.mapssdk.MapsIndoors;
import com.mapsindoors.mapssdk.MenuInfo;
import com.mapsindoors.mapssdk.OnLocationsReadyListener;
import com.mapsindoors.mapssdk.Solution;
import com.mapsindoors.mapssdk.VenueCollection;
import com.mapsindoors.mapssdk.dbglog;
import com.mapsindoors.mapssdk.errors.MIError;
import com.mapsindoors.stdapp.BuildConfig;
import com.mapsindoors.stdapp.R;
import com.mapsindoors.stdapp.helpers.MapsIndoorsHelper;
import com.mapsindoors.stdapp.helpers.MapsIndoorsUtils;
import com.mapsindoors.stdapp.listeners.IGenericRecyclerViewItemClickedListener;
import com.mapsindoors.stdapp.managers.AppConfigManager;
import com.mapsindoors.stdapp.managers.GoogleAnalyticsManager;
import com.mapsindoors.stdapp.managers.UserRolesManager;
import com.mapsindoors.stdapp.ui.activitymain.MapsIndoorsActivity;
import com.mapsindoors.stdapp.ui.common.adapters.GenericRecyclerViewAdapter;
import com.mapsindoors.stdapp.ui.common.enums.MenuFrame;
import com.mapsindoors.stdapp.ui.common.fragments.BaseFragment;
import com.mapsindoors.stdapp.ui.common.models.GenericRecyclerViewListItem;
import com.mapsindoors.stdapp.ui.locationmenu.LocationMenuFragment;
import com.mapsindoors.stdapp.ui.search.SearchFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MenuFragment extends BaseFragment {
    static final String TAG = MenuFragment.class.getSimpleName();


    public static final int FLIPPER_LIST_ITEMS = 0;
    public static final int FLIPPER_LIST_PROGRESS = 1;
    public static final int FLIPPER_LIST_NO_RESULTS = 2;


    Context mContext;
    MapsIndoorsActivity mActivity;
    MapControl mMapControl;

    LocationMenuFragment mLocationMenuFragment;


    private Solution mSolution;

    EditText mSearchEditTextView;

    private TextView mVenueNameTextView;

    private ImageView mTopImage;

    private ViewFlipper mViewFlipper;

    private RecyclerView mMainMenuListView;
    private LinearLayoutManager mLayoutManager;
    private GenericRecyclerViewAdapter mMainMenuListViewAdapter;
    private List<GenericRecyclerViewListItem> mListItems;


    private TextView mCategSearchTextView;

    private View mSearchIcon, mVenueSelectorBtn;
    ImageView mBackButton;
    ImageView mInfoButton;
    ImageView mBookingButton;
    ImageView mSettingsButton;

    private RelativeLayout mRootLayout;


    private boolean mIsMenuCategoryMode;
    public boolean mIsOpenedFromBackpress;
    private List<GenericRecyclerViewListItem> mLocationItemsList;
    private SearchFragment mSearchFragment;

    private MenuInfo selectedCateg;

    private String mQueryCategoryFilter;

    protected boolean mIsSearching = false;


    public MenuFragment() {
        super();
    }


    //region Fragment lifecycle events
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mMainView == null) {
            mMainView = inflater.inflate(R.layout.fragment_mainmenu, container);
        }

        return mMainView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupView(view);
    }
    //endregion


    public void setCategLocationItems(List<GenericRecyclerViewListItem> items) {
        mLocationItemsList.clear();
        mLocationItemsList.addAll(items);
    }

    private void setupView(View view) {
        // Increase the drawer's width to 90% of the total
        mRootLayout = mMainView.findViewById(R.id.mainmenu);
        int width = (getResources().getDisplayMetrics().widthPixels / 100) * 90;
        DrawerLayout.LayoutParams params = new DrawerLayout.LayoutParams(width, ActionBar.LayoutParams.MATCH_PARENT);
        mRootLayout.setLayoutParams(params);


        mViewFlipper = view.findViewById(R.id.mainmenu_itemlist_viewflipper);
        mMainMenuListView = view.findViewById(R.id.mainmenu_itemlist);

        mSearchEditTextView = view.findViewById(R.id.mainmenu_edittext_search);

        mSearchIcon = view.findViewById(R.id.mainmenu_searchicon);
        mVenueSelectorBtn = view.findViewById(R.id.mainmenu_venue_ic);
        mVenueNameTextView = view.findViewById(R.id.mainmenu_venue_text);
        mCategSearchTextView = view.findViewById(R.id.mainmenu_text_categ_search);

        mBackButton = view.findViewById(R.id.mainmenu_back_ic);
        mInfoButton = view.findViewById(R.id.mainmenu_info_ic);
        mTopImage = view.findViewById(R.id.mainmmenu_topimage);
        mSettingsButton = view.findViewById(R.id.mainmenu_settings_ic);

        mBookingButton = view.findViewById(R.id.mainmenu_booking_ic);

        setupSearchBox();

        setupListView();

        mFragment = MenuFrame.MENU_FRAME_MAIN_MENU;
    }

    public boolean hasBeenInitialized() {
        // Using a ref here
        return mLocationMenuFragment != null;
    }

    public void init(Context context, MapControl mapControl) {
        mContext = (mContext != null) ? mContext : getContext();
        mActivity = (mActivity != null) ? mActivity : (MapsIndoorsActivity) mContext;

        mMapControl = mapControl;
        mLocationMenuFragment = mActivity.getLocationMenuFragment();
        mLocationMenuFragment.init(context, mapControl);

        setupViewFlipper();
        setupListView();

        // Set the focus on the search box with clicking on the search icon
        mSearchIcon.setOnClickListener(view -> {

            if (mIsMenuCategoryMode) {
                openSearchFragment(SearchFragment.CATEG_MENU_SEARCH_TYPE);

            } else {
                openSearchFragment(SearchFragment.POI_MENU_SEARCH_TYPE);
            }

        });

        // Venue selector button
        mVenueSelectorBtn.setOnClickListener(v -> {

            // Open the fragment
            mActivity.menuGoTo(MenuFrame.MENU_FRAME_VENUE_SELECTOR, true);
        });

        mBackButton.setOnClickListener(v -> {

            // Populate the menu with the categories
            populateCategoriesMenu();
            // Show the venue seklector button
            setToolbarBackAndVenueButtonVisibility(false);
        });

        mInfoButton.setOnClickListener( v -> {
            mActivity.menuGoTo( MenuFrame.MENU_FRAME_APP_INFO, true );
        } );

        mBookingButton.setOnClickListener(v -> {
            mActivity.menuGoTo( MenuFrame.MENU_FRAME_BOOKING, true);
        });

        mSettingsButton.setOnClickListener(v -> {
            mActivity.menuGoTo(MenuFrame.MENU_FRAME_ROUTE_OPTIONS, true);
        });
    }

    public void openLocationMenu(final MPLocation location) {
        openLocationMenu(location, false);
    }

    public void openLocationMenu(final MPLocation location, boolean infoWindowClick) {
        setLocationMenu(location, infoWindowClick);
        mActivity.menuGoTo(MenuFrame.MENU_FRAME_LOCATION_MENU, true);
    }

    public void setLocationMenu(final MPLocation location, boolean clickedOnMarker) {
        AppConfigManager acm = mActivity.getAppConfigManager();

        Bitmap bitmap = null;
        Bitmap logo = null;

        final String[] locCategories = location.getCategories();
        final String locationImageUrl = (String) location.getProperty(LocationPropertyNames.IMAGE_URL);

        if (locCategories != null
                && locCategories.length > 0
                && acm != null
                && TextUtils.isEmpty(locationImageUrl)) {
            for (final MenuInfo element : acm.getMainMenuEntries()) {
                for (String category : locCategories) {
                    if (element.getCategoryKey().equalsIgnoreCase(category)) {
                        bitmap = acm.getMainMenuImage(category);
                        String typeName = location.getType();
                        logo = acm.getPOITypeIcon(typeName);
                        break;
                    }
                }
                break;
            }
        }

        if (bitmap == null && mTopImage.getDrawable() != null) {
            //No category specific image found. Use the current venue image instead.
            bitmap = ((BitmapDrawable) mTopImage.getDrawable()).getBitmap();
        }

        if (logo == null && acm != null) {
            String typeName = location.getType();
            logo = acm.getPOITypeIcon(typeName);
        }

        mLocationMenuFragment.setLocation(location, bitmap, logo, clickedOnMarker);

    }

    public void initMenu(final Solution solution, AppConfigManager settings, VenueCollection venues, boolean isFirstRun) {

        if (isFirstRun) {
            mSolution = solution;
        }


        // Venue stuff
        String selectedVenueName = venues.getCurrentVenue().getName();

        // Set/change the venue's image
        Bitmap topImageBitmap = settings.getVenueImage(selectedVenueName);

        if (topImageBitmap != null) {
            mTopImage.setImageBitmap(topImageBitmap);
        }


        //
        setVenueName(venues.getCurrentVenue().getVenueInfo().getName());

        setSearchBoxHint(null);


        //Finally populate the menu list
        if (!mIsOpenedFromBackpress) {
            populateCategoriesMenu();
            setToolbarBackAndVenueButtonVisibility(false);

        } else {
            mIsOpenedFromBackpress = false;
            changeWaitStatus(MenuFragment.FLIPPER_LIST_ITEMS, true);
        }

        final UserRolesManager userRolesManager = mActivity.getUserRolesManager();
        if (userRolesManager != null) {
            final boolean showUserRolesButton = UserRolesManager.hasUserRoles();
            mSettingsButton.setVisibility(showUserRolesButton ? View.VISIBLE : View.GONE);
        }

    }

    public void setVenueName(String venueName) {
        if (mVenueNameTextView != null) {
            mVenueNameTextView.setText(venueName);
        }
    }

    /**
     * Populate a (reset) menu with the categories and types defined in the mainmenuEntryList
     *
     * @return
     */
    public void populateCategoriesMenu() {

        setMenuModeToCategory(true);
        setSearchBoxHint(null);

        mListItems.clear();
        mLayoutManager.scrollToPositionWithOffset(0, 0);

        final AppConfigManager acm = mActivity.getAppConfigManager();
        final List<MenuInfo> mainMenuEntryList = (acm != null)
                ? acm.getMainMenuEntries()
                : null;

        if ((mSolution != null) && (mainMenuEntryList != null)) {

            for (MenuInfo menuItem : mainMenuEntryList) {

                final Bitmap bm = acm.getMainMenuIcon(menuItem.getCategoryKey());

                if (BuildConfig.DEBUG) {
                    if ((bm != null) && MapsIndoorsUtils.checkIfBitmapIsEmpty(bm)) {
                        dbglog.Log(TAG, "MenuFragment.openLocationMenu: EMPTY icon for " + menuItem.getName());
                    }
                }

                if (bm != null) {
                    mListItems.add(new GenericRecyclerViewListItem(menuItem.getName(), bm, menuItem, GenericRecyclerViewAdapter.VIEWTYPE_CATEGORY));
                } else {
                    mListItems.add(new GenericRecyclerViewListItem(menuItem.getName(), R.drawable.ic_generic_item_icon, menuItem, GenericRecyclerViewAdapter.VIEWTYPE_CATEGORY));
                }
            }
        }

        {
            // All data loaded now. Show the list.
            mMainMenuListViewAdapter.setItems(mListItems);

            changeWaitStatus(MenuFragment.FLIPPER_LIST_ITEMS, true);
        }

        mMainMenuListView.invalidate();

        setLocationQueryCategoryFilter(null);

    }

    @NonNull
    public List<GenericRecyclerViewListItem> getCleanItemList() {
        mListItems.clear();
        return mListItems;
    }

    public void resetScroll() {
        mLayoutManager.scrollToPositionWithOffset(0, 0);
    }

    public void populatePOIsMenu(@NonNull List<GenericRecyclerViewListItem> items) {

        setToolbarBackAndVenueButtonVisibility(true);
        mIsMenuCategoryMode = false;

        if (items.size() > 0) {
            mMainMenuListViewAdapter.setItems(items);
            setCategLocationItems(items);

            changeWaitStatus(FLIPPER_LIST_ITEMS, true);
        } else {
            changeWaitStatus(FLIPPER_LIST_NO_RESULTS, true);
        }


    }

    private boolean isBackBtnActive() {
        return mBackButton.getVisibility() == View.VISIBLE;
    }

    private void setupSearchBox() {
        mCategSearchTextView.setOnClickListener(view1 -> {
            mSearchEditTextView.getText().clear();

            //
            if (mIsMenuCategoryMode) {
                openSearchFragment(SearchFragment.CATEG_MENU_SEARCH_TYPE);
            } else {
                openSearchFragment(SearchFragment.POI_MENU_SEARCH_TYPE);
            }
        });
    }

    String mLastSearchText;

    void openSearchFragment(int searchType) {
        mSearchEditTextView.clearFocus();

        mSearchFragment = mActivity.getDirectionsFullMenuSearchFragment();
        mSearchFragment.init(getContext(), mMapControl);
        mSearchFragment.setOnLocationFoundHandler((queryString, searchResult) -> {
            //User selected a location.
            if (searchResult != null) {
                mLastSearchText = queryString;
                openLocationMenu((MPLocation) searchResult);
                closeKeyboard();
            }
        });

        if (searchType == SearchFragment.POI_MENU_SEARCH_TYPE) {
            mSearchFragment.setCategFilter(selectedCateg.getCategoryKey());
            mSearchFragment.setSearchHint(selectedCateg.getName());
        }

        mSearchFragment.setSearchType(searchType, -1);
        mSearchFragment.setActive(true);
    }


    //region ViewFlipper
    private void setupViewFlipper() {
        mViewFlipper.setDisplayedChild(FLIPPER_LIST_PROGRESS);
    }

    /**
     * A waiting spinner will appear is set to true and be removed again on false.
     */
    public void changeWaitStatus(int flipperListState, boolean animate) {
        if (animate) {
            mViewFlipper.setInAnimation(mContext, R.anim.menu_flipper_fade_in);
            mViewFlipper.setOutAnimation(mContext, R.anim.menu_flipper_fade_out);
        }

        mViewFlipper.setDisplayedChild(flipperListState);
    }
    //endregion


    //region RecyclerView
    private void setupListView() {
        mContext = (mContext != null) ? mContext : getContext();
        mActivity = (mActivity != null) ? mActivity : (MapsIndoorsActivity) mContext;

        mMainMenuListView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(mContext);
        mLayoutManager.setSmoothScrollbarEnabled(false);
        mLayoutManager.setAutoMeasureEnabled(true);

        mMainMenuListView.setLayoutManager(mLayoutManager);

        mMainMenuListViewAdapter = new GenericRecyclerViewAdapter(mContext);
        mMainMenuListViewAdapter.setItemClickListener(mGenericRecyclerViewItemClickedListener);

        // Add the feedback guy, if any
        {
            AppConfigManager appConfigManager = mActivity.getAppConfigManager();
            if (appConfigManager != null) {

                final String feedbackURL = appConfigManager.getFeedbackUrl();

                // Validate the url string
                if (!TextUtils.isEmpty(feedbackURL) && Patterns.WEB_URL.matcher(feedbackURL).matches()) {
                    mMainMenuListViewAdapter.setFeedbackItem(feedbackURL);
                }
            }
        }

        mMainMenuListView.setAdapter(mMainMenuListViewAdapter);
        mListItems = new ArrayList<>();
        mLocationItemsList = new ArrayList<>();
    }

    /**
     *
     */
    final IGenericRecyclerViewItemClickedListener mGenericRecyclerViewItemClickedListener = new IGenericRecyclerViewItemClickedListener() {
        @Override
        public void OnGenericRVItemClicked(GenericRecyclerViewListItem item) {
            switch (item.mViewType) {
                case GenericRecyclerViewAdapter.VIEWTYPE_LOCATION: {
                    //A specific location is selected. Get detailed data from it and launch the POI detail menu
                    MPLocation location = (MPLocation) item.mObj;

                    openLocationMenu(location);
                    break;
                }

                case GenericRecyclerViewAdapter.VIEWTYPE_CATEGORY: {
                    closeKeyboard();

                    selectedCateg = (MenuInfo) item.mObj;

                    final String categoryKey = selectedCateg.getCategoryKey();
                    final String categoryName = selectedCateg.getName();

                    // A location type is selected. Find all locations with that category.
                    setLocationQueryCategoryFilter(categoryKey);

                    // Change the toolbar title with the category name
                    mActivity.getSelectionManager().setCurrentCategory(categoryKey);
                    changeWaitStatus(MenuFragment.FLIPPER_LIST_PROGRESS, true);

                    findLocations();

                    setMenuModeToCategory(false);
                    setSearchBoxHint(categoryName);

                    GoogleAnalyticsManager.reportScreen(categoryName, mActivity);
                    break;
                }
            }


        }
    };
    //endregion


    @Override
    public boolean onBackPressed() {
        if (isActive()) {
            if (!mActivity.isDrawerOpen()) {
                return false;
            } else {
                if (isBackBtnActive()) {
                    setToolbarBackAndVenueButtonVisibility(false);
                    // populate the menu with the categories
                    populateCategoriesMenu();
                } else {
                    mActivity.closeDrawer();
                }
            }
        }
        return true;
    }
    //endregion


    public void setToolbarBackAndVenueButtonVisibility(boolean backButtonState) {

        if (backButtonState) {
            // only make changments when the button need to be showed
            if (mSelectorButtonshouldBeShown) {
                mVenueSelectorBtn.setVisibility(View.GONE);
            }
            mBackButton.setVisibility(View.VISIBLE);

        } else {
            // only make changments when the button need to be showed
            mBackButton.setVisibility(View.GONE);

            if (mSelectorButtonshouldBeShown) {
                mVenueSelectorBtn.setVisibility(View.VISIBLE);
            }
        }
    }


    boolean mSelectorButtonshouldBeShown = true;

    public void setVenueSelectorButtonshouldBeShown(boolean state) {

        mSelectorButtonshouldBeShown = state;

        // set the visiblity of the icon to gone
        if (!state) {
            mVenueSelectorBtn.setVisibility(View.GONE);
        }
    }


    public void setMenuModeToCategory(boolean state) {
        mIsMenuCategoryMode = state;
    }

    private String mCurrentCategoryHintReplName;

    public void setSearchBoxHint(String catName) {
        if (catName != null) {
            mCurrentCategoryHintReplName = catName;
            mCategSearchTextView.setHint(String.format(getString(R.string.search_param), catName));

        } else {

            // Need to know which hint to show when the search box is cleared (while listing categories or showing the results of one)
            if (mIsMenuCategoryMode || TextUtils.isEmpty(mCurrentCategoryHintReplName)) {
                mCategSearchTextView.setHint(R.string.search_places);
            } else {
                mCategSearchTextView.setHint(String.format(getString(R.string.search_param), mCurrentCategoryHintReplName));
            }
        }

        mCategSearchTextView.setText("");
    }


    public void closeKeyboard() {
        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mMainView.getWindowToken(), 0);
    }


    //// search region

    /**
     * Finds and shows locations based on a search string and optionally with optional type and category filters (can be null)
     */
    public void findLocations() {

        String currentVenueID = mActivity.getCurrentVenueId();


        MPQuery.Builder queryBuilder = new MPQuery.Builder();
        MPFilter.Builder filterBuilder = new MPFilter.Builder();


        if (mQueryCategoryFilter != null) {
            filterBuilder.setCategories(Collections.singletonList(mQueryCategoryFilter));
            if (currentVenueID != null) {
                filterBuilder.setParents(Collections.singletonList(currentVenueID));
                filterBuilder.setDepth(4);
            }


        }

        if (!mIsSearching) {
            mIsSearching = true;


            MPQuery q = queryBuilder.build();
            MPFilter f = filterBuilder.build();


            MapsIndoors.getLocationsAsync(q, f, mSearchLocationsReadyListener);

        }
    }

    private OnLocationsReadyListener mSearchLocationsReadyListener = new OnLocationsReadyListener() {
        @Override
        public void onLocationsReady(final List<MPLocation> locations, @Nullable MIError error) {

            if (!mIsSearching) {
                return;
            }

            if (error != null) {
                if (error.code == MIError.INVALID_API_KEY) {
                    MapsIndoorsUtils.showInvalidAPIKeyDialogue(getContext());
                }
            } else {

                final List<GenericRecyclerViewListItem> elements = getCleanItemList();

                if (locations != null) {
                    VenueCollection venueCollection = mActivity.getVenueCollection();
                    BuildingCollection buildingCollection = mActivity.getBuildingCollection();
                    AppConfigManager acm = mActivity.getAppConfigManager();

                    for (MPLocation location : locations) {

                        if (location != null) {

                            final String subText = MapsIndoorsHelper.composeLocationInfoString(
                                    location,
                                    venueCollection,
                                    buildingCollection,
                                    MapsIndoorsHelper.FORMAT_LOCATION_INFO_STRING_USE_COMMAS,
                                    true,
                                    mContext
                            );

                            double airDistance = 0;
                            final String typeName = location.getType();
                            final Bitmap bm = acm.getPOITypeIcon(typeName);

                            if (BuildConfig.DEBUG) {
                                if ((bm != null) && MapsIndoorsUtils.checkIfBitmapIsEmpty(bm)) {
                                    dbglog.Log(TAG, "Main Menu mSearchLocationsReadyListener.onLocationsReady: mLocationTypeImages bm IS EMPTY for " + typeName);
                                }
                            }

                            if (bm != null) {
                                elements.add(new GenericRecyclerViewListItem(location.getName(), subText, airDistance, bm, location, GenericRecyclerViewAdapter.VIEWTYPE_LOCATION));
                            } else {
                                elements.add(new GenericRecyclerViewListItem(location.getName(), subText, airDistance, R.drawable.ic_generic_item_icon, location, GenericRecyclerViewAdapter.VIEWTYPE_LOCATION));
                            }
                        }
                    }
                }

                mActivity.runOnUiThread(() -> {
                    if (locations != null) {

                        // Select the search result on the map
                        mActivity.getSelectionManager().selectSearchResult(locations);
                        resetScroll();
                        populatePOIsMenu(elements);
                    }
                });

                // Report to our Analytics
                int locationCount = (locations != null) ? locations.size() : 0;
                GoogleAnalyticsManager.reportSearch("", Collections.singletonList(mQueryCategoryFilter), locationCount);

                mIsSearching = false;
            }
        }
    };


    private void setLocationQueryCategoryFilter(@Nullable String categoryFilter) {
        mQueryCategoryFilter = categoryFilter;
    }

}