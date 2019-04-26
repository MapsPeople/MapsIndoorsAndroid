package com.mapsindoors.stdapp.ui.menumain;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import com.mapsindoors.mapssdk.Location;
import com.mapsindoors.mapssdk.LocationPropertyNames;
import com.mapsindoors.mapssdk.MapControl;
import com.mapsindoors.mapssdk.dbglog;
import com.mapsindoors.mapssdk.MenuInfo;
import com.mapsindoors.mapssdk.Solution;
import com.mapsindoors.mapssdk.VenueCollection;
import com.mapsindoors.stdapp.BuildConfig;
import com.mapsindoors.stdapp.R;
import com.mapsindoors.stdapp.helpers.MapsIndoorsUtils;
import com.mapsindoors.stdapp.listeners.IGenericRecyclerViewItemClickedListener;
import com.mapsindoors.stdapp.managers.AppConfigManager;
import com.mapsindoors.stdapp.ui.activitymain.MapsIndoorsActivity;
import com.mapsindoors.stdapp.ui.common.adapters.GenericRecyclerViewAdapter;
import com.mapsindoors.stdapp.ui.common.fragments.BaseFragment;
import com.mapsindoors.stdapp.ui.common.listeners.MenuListener;
import com.mapsindoors.stdapp.ui.common.models.GenericRecyclerViewListItem;
import com.mapsindoors.stdapp.ui.locationmenu.LocationMenuFragment;
import com.mapsindoors.stdapp.ui.search.SearchFragment;

import java.util.ArrayList;
import java.util.List;

public class MenuFragment extends BaseFragment
{
    private static final String TAG = MenuFragment.class.getSimpleName();


    public static final int FLIPPER_LIST_ITEMS = 0;
    public static final int FLIPPER_LIST_PROGRESS = 1;
    public static final int FLIPPER_LIST_NO_RESULTS = 2;


    Context mContext;
    MapsIndoorsActivity mActivity;
    MapControl mMapControl;

    LocationMenuFragment mLocationMenuFragment;

    protected MenuListener mMenuListener;

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

    private RelativeLayout mRootLayout;
    List<GenericRecyclerViewListItem> mPrevLocationsItems;

    private ActionBarDrawerToggle mDrawerToggle;


    boolean mIsFirstLocationsSearch;
    boolean mIsMenuCategoryMode;
    public boolean mIsOpenedFromBackpress;
    List<GenericRecyclerViewListItem> mLocationItemsList;
    SearchFragment mSearchFragment;
    DrawerLayout mDrawerLayout;


    MenuInfo selectedCateg;

    public MenuFragment() {
        super();
    }


    //region Fragment lifecycle events
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mMainView == null) {
            mMainView = inflater.inflate(R.layout.fragment_mainmenu, container);
        }

        return mMainView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupView(view);
    }
    //endregion


    public void setCategLocationItems(List<GenericRecyclerViewListItem> items) {
        mLocationItemsList.clear();
        mLocationItemsList.addAll(items);
    }

    private void setupView( View view )
    {
        // Increase the drawer's width to 90% of the total
        mRootLayout = mMainView.findViewById(R.id.mainmenu);
        int width = (getResources().getDisplayMetrics().widthPixels / 100) * 90;
        DrawerLayout.LayoutParams params = new DrawerLayout.LayoutParams(width, ActionBar.LayoutParams.MATCH_PARENT);
        mRootLayout.setLayoutParams(params);


        mViewFlipper = view.findViewById( R.id.mainmenu_itemlist_viewflipper );
        mMainMenuListView = view.findViewById( R.id.mainmenu_itemlist );

        mSearchEditTextView = view.findViewById( R.id.mainmenu_edittext_search );

        mSearchIcon = view.findViewById( R.id.mainmenu_searchicon );
        mVenueSelectorBtn = view.findViewById( R.id.mainmenu_venue_ic );
        mVenueNameTextView = view.findViewById( R.id.mainmenu_venue_text );
        mCategSearchTextView = view.findViewById( R.id.mainmenu_text_categ_search );

        mBackButton = view.findViewById( R.id.mainmenu_back_ic );
        mInfoButton = view.findViewById( R.id.mainmenu_info_ic );
        mTopImage = view.findViewById( R.id.mainmmenu_topimage );

        setupSearchBox(view);

        setupListView(view);
    }

    public void init( Context context, MenuListener menuListener, MapControl mapControl )
    {
        mContext = (mContext != null) ? mContext : getContext();
        mActivity = (mActivity != null) ? mActivity : (MapsIndoorsActivity) mContext;
        mDrawerLayout = ((MapsIndoorsActivity) context).findViewById(R.id.main_drawer);


        mMenuListener = menuListener;
        mMapControl = mapControl;
        mLocationMenuFragment = mActivity.getLocationMenuFragment();
        mLocationMenuFragment.init( context, menuListener, mapControl );

        setupViewFlipper(mMainView);
        setupListView(mMainView);

        mPrevLocationsItems = new ArrayList<>();


        // Set the focus on the search box with clicking on the search icon
        mSearchIcon.setOnClickListener( view -> {

            if(mIsMenuCategoryMode){
                openSearchFragment(SearchFragment.CATEG_MENU_SEARCH_TYPE);

            } else{
                openSearchFragment(SearchFragment.POI_MENU_SEARCH_TYPE);
            }

        } );

        // Venue selector button
        mVenueSelectorBtn.setOnClickListener( v -> {

            // Open the fragment
            mActivity.menuGoTo(MapsIndoorsActivity.MENU_FRAME_VENUE_SELECTOR, true);
        } );

        mBackButton.setOnClickListener( v -> {

            // Populate the menu with the categories
            populateMenu();
            // Show the venue seklector button
            setToolbarBackAndVenueButtonVisibility(false);
        } );


        mInfoButton.setOnClickListener( v -> {
           mActivity.menuGoTo(MapsIndoorsActivity.MENU_FRAME_APP_INFO, true);

        } );
    }

    public void openLocationMenu( final Location location )
    {
        AppConfigManager acm = mActivity.getAppConfigManager();

        Bitmap bitmap, logo;
        bitmap = logo = null;

        if( (location.getCategories() != null) && (location.getCategories().length > 0) )
        {
            final String locationImageUrl = (String)location.getProperty( LocationPropertyNames.IMAGE_URL );

            if( TextUtils.isEmpty( locationImageUrl ) )
            {
                List<MenuInfo> mainmenuEntryList = (acm != null)
                        ? acm.getMainMenuEntries()
                        : null;

                if( mainmenuEntryList != null )
                {
                    outerLoop:
                    for( MenuInfo element : mainmenuEntryList ) {

                        String[] cats = location.getCategories();

                        if( (cats != null) && (cats.length > 0) ) {
                            for( String category : cats ) {
                                if( element.getCategoryKey().equalsIgnoreCase( category ) ) {

                                    bitmap = acm.getMainMenuImage( category );

                                    String typeName = location.getType();
                                    logo = acm.getPOITypeIcon( typeName );

                                    if( BuildConfig.DEBUG ) {
                                        if( (logo != null) && MapsIndoorsUtils.checkIfBitmapIsEmpty( logo ) ) {
                                            dbglog.Log( TAG, "MenuFragment.openLocationMenu: EMPTY icon for " + category );
                                        }
                                    }

                                    break outerLoop;
                                }
                            }
                        }
                    }
                }
            }
        }

        if (bitmap == null) {
            //No category specific image found. Use the current venue image instead.
            if( mTopImage.getDrawable() != null )
            {
                bitmap = ((BitmapDrawable) mTopImage.getDrawable()).getBitmap();
            }
        }

        if (logo == null) {
            String typeName = location.getType();
            if( acm != null ) {
                logo = acm.getPOITypeIcon( typeName );
            }
        }

        mActivity.menuGoTo(MapsIndoorsActivity.MENU_FRAME_LOCATION_MENU, true);

        if( bitmap != null )
        {
            mLocationMenuFragment.setLocation( location, bitmap, logo );
        }
    }

    public void initMenu( final Solution solution, AppConfigManager settings, VenueCollection venues, boolean isFirstRun) {

        if (isFirstRun) {
            mSolution = solution;
            mLocationMenuFragment.initMenu();
        }

        mIsFirstLocationsSearch = true;

        // Venue stuff
        String selectedVenueName = venues.getCurrentVenue().getName();

        // Set/change the venue's image
        Bitmap topImageBitmap = settings.getVenueImage(selectedVenueName);

        if (topImageBitmap != null) {
            mTopImage.setImageBitmap(topImageBitmap);
        }


        //
        setVenueName( venues.getCurrentVenue().getVenueInfo().getName() );

        setSearchBoxHint(null);


        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.removeDrawerListener(mDrawerToggle);
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        //Finally populate the menu list
        if(!mIsOpenedFromBackpress){
            populateMenu();
            setToolbarBackAndVenueButtonVisibility(false);

        }
        else
        {
            mIsOpenedFromBackpress = false;
            changeWaitStatus( MenuFragment.FLIPPER_LIST_ITEMS, true );
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
    public void populateMenu() {

        setMenuModeToCategory( true );

        mListItems.clear();
        mLayoutManager.scrollToPositionWithOffset( 0, 0 );

        AppConfigManager acm = mActivity.getAppConfigManager();
        List<MenuInfo> mainmenuEntryList = (acm != null)
                ? acm.getMainMenuEntries()
                : null;

        if( (mSolution != null) && (mainmenuEntryList != null) ) {

            for( MenuInfo menuItem : mainmenuEntryList ) {

                Bitmap bm = acm.getMainMenuIcon( menuItem.getCategoryKey() );

                if( BuildConfig.DEBUG ) {
                    if( (bm != null) && MapsIndoorsUtils.checkIfBitmapIsEmpty( bm ) ) {
                        dbglog.Log( TAG, "MenuFragment.openLocationMenu: EMPTY icon for " + menuItem.getName() );
                    }
                }

                if( bm != null )
                {
                    mListItems.add( new GenericRecyclerViewListItem( menuItem.getName(), bm, menuItem, GenericRecyclerViewAdapter.VIEWTYPE_CATEGORY ) );
                }
                else {
                    if( BuildConfig.DEBUG)
                    {
                        dbglog.Log(TAG, "");
                    }
                }
            }
        }

        {
            // All data loaded now. Show the list.
            mMainMenuListViewAdapter.setItems(mListItems);

            changeWaitStatus(MenuFragment.FLIPPER_LIST_ITEMS, true);
        }

        mMainMenuListView.invalidate();

        mActivity.resetLocationsSearchCategoryFilter();

    }

    @NonNull
    public List<GenericRecyclerViewListItem> getCleanItemList() {
        mListItems.clear();
        return mListItems;
    }

    public void resetScroll() {
        mLayoutManager.scrollToPositionWithOffset(0, 0);
    }

    public void populateItemList(@NonNull List<GenericRecyclerViewListItem> items) {

        changeWaitStatus(FLIPPER_LIST_ITEMS, true);
        setToolbarBackAndVenueButtonVisibility( true );
        mIsMenuCategoryMode = false;

        mMainMenuListViewAdapter.setItems(items);
    }

    private boolean isBackBtnActive() {
        return mBackButton.getVisibility() == View.VISIBLE;
    }

    private void setupSearchBox( View view )
    {
        mCategSearchTextView.setOnClickListener( view1 -> {
            mSearchEditTextView.getText().clear();

            //
            if (mIsMenuCategoryMode) {
                openSearchFragment(SearchFragment.CATEG_MENU_SEARCH_TYPE);
            }
            else
            {
                openSearchFragment(SearchFragment.POI_MENU_SEARCH_TYPE);
            }
        } );
    }

    void openSearchFragment( int searchType )
    {
        mSearchEditTextView.clearFocus();

        mSearchFragment = mActivity.getDirectionsFullMenuSearchFragment();
        mSearchFragment.init( getContext(), mMapControl );
        mSearchFragment.setOnLocationFoundHandler( location -> {
            //User selected a location.
            openLocationMenu( (Location) location );
            closeKeyboard();
        } );

        switch( searchType )
        {
            case SearchFragment.POI_MENU_SEARCH_TYPE:
            {
                mSearchFragment.setCategFilter( selectedCateg.getCategoryKey() );
                mSearchFragment.setSearchHint( selectedCateg.getName() );
                break;
            }
        }

        mSearchFragment.setSearchType( searchType );

        mSearchFragment.setActive( true );
    }


    //region ViewFlipper
    private void setupViewFlipper( View view )
    {
        mViewFlipper.setDisplayedChild( FLIPPER_LIST_PROGRESS );
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
    private void setupListView( View view )
    {
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
            if( appConfigManager != null ) {

                final String feedbackURL = appConfigManager.getFeedbackUrl();

                // Validate the url string
                if( !TextUtils.isEmpty( feedbackURL ) && Patterns.WEB_URL.matcher( feedbackURL ).matches() ) {
                    mMainMenuListViewAdapter.setFeedbackItem( feedbackURL );
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
    IGenericRecyclerViewItemClickedListener mGenericRecyclerViewItemClickedListener = new IGenericRecyclerViewItemClickedListener() {
        @Override
        public void OnGenericRVItemClicked( GenericRecyclerViewListItem item )
        {
            if( item.mViewType == GenericRecyclerViewAdapter.VIEWTYPE_CATEGORY )
            {
                closeKeyboard();
                //
                changeWaitStatus( MenuFragment.FLIPPER_LIST_PROGRESS, true );
            }

            if( mMenuListener != null )
            {
                if( mIsMenuCategoryMode )
                {
                    selectedCateg = (MenuInfo) item.mObj;
                }

                mMenuListener.onMenuSelect( item.mObj, item.mViewType );
            }
        }
    };
    //endregion


    @Override
    public void connectivityStateChanged( boolean state ) {}

    @Override
    public boolean onBackPressed() {
        if(isActive()){

            if (!mActivity.isDrawerOpen() ) {
                mActivity.openDrawer(true);
            }
            else {

                if (isBackBtnActive()) {
                    setToolbarBackAndVenueButtonVisibility(false);// show the venue seklector button
                    // populate the menu with the categories
                    populateMenu();
                }
                else {
                    closeKeyboard();
                    return true;
                }
            }

            return false;
        }

        return true;
    }

    @Override
    public void onDrawerEvent( int newState, int prevState ) {}
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


    boolean mSelectorButtonshouldBeShown = true ;
    public void setVenueSelectorButtonshouldBeShown(boolean state) {

        mSelectorButtonshouldBeShown = state;

        // set the visiblity of the icon to gone
        if(!state){
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
}