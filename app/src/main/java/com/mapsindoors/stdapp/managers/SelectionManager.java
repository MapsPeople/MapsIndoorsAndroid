package com.mapsindoors.stdapp.managers;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLngBounds;
import com.mapsindoors.mapssdk.Category;
import com.mapsindoors.mapssdk.Geometry;
import com.mapsindoors.mapssdk.MPLocation;
import com.mapsindoors.mapssdk.MapControl;
import com.mapsindoors.mapssdk.MapsIndoors;
import com.mapsindoors.mapssdk.MultiPolygonGeometry;
import com.mapsindoors.mapssdk.PolygonGeometry;
import com.mapsindoors.mapssdk.ReadyListener;
import com.mapsindoors.mapssdk.Venue;
import com.mapsindoors.stdapp.helpers.MapsIndoorsSettings;
import com.mapsindoors.stdapp.ui.activitymain.MapsIndoorsActivity;
import com.mapsindoors.stdapp.ui.activitymain.TopSearchField;

import java.util.List;


/**
 * Created by jose on 07-05-2017.
 * <p>
 * Temp class for handling location selection
 */
public class SelectionManager {
    private static final int LAST_SELECTION_TYPE_VENUE = 0;
    private static final int LAST_SELECTION_TYPE_SINGLE_LOCATION = 1;
    private static final int LAST_SELECTION_TYPE_NEAREST_POSITION = 2;
    private static final int LAST_SELECTION_TYPE_SEARCH_RESULT = 3;


    private MapsIndoorsActivity mActivity;
    private TopSearchField mTopSearchField;

    private Venue mCurrentVenue;

    /**
     * Category name (facilities, etc.), Type (POI
     */
    private MapControl mMapControl;


    public SelectionManager(MapsIndoorsActivity activity, MapControl mapControl, TopSearchField topSearchField) {
        mActivity = activity;
        mTopSearchField = topSearchField;
        mMapControl = mapControl;
    }

    public void clearSelection() {
        mTopSearchField.setToolbarText(null, false);

        if (mMapControl != null) {
            mMapControl.clearMap();
        }
    }

    public void setCurrentVenue(@Nullable Venue venue) {
        if (venue != null) {
            mCurrentVenue = venue;

            clearSelection();

            mMapControl.selectFloor(mCurrentVenue.getDefaultFloor());

            focusOnLocationBoundaries(
                    venue.getGeometry(),
                    MapsIndoorsSettings.MAPSINDOORS_TILES_AVAILABLE_ZOOM_LEVEL,
                    false
            );

            mTopSearchField.setToolbarText(venue.getVenueInfo().getName(), false);

        }
    }

    @Nullable
    public Venue getCurrentVenue() {
        return mCurrentVenue;
    }

    public void setCurrentCategory(@Nullable String categoryKey) {
        if (categoryKey != null) {
            Category category = MapsIndoors.getCategories().getCategory(categoryKey);
            setCurrentCategory(category);
        }
    }

    public void setCurrentCategory(@Nullable Category category) {
        if (category != null) {
            mTopSearchField.setToolbarText(category.getValue(), true);
        }
    }

    @MainThread
    public void selectLocation(MPLocation location, boolean changeZoomLevel, boolean showNameInToolbar, boolean clearMapButtonVisible, boolean clickedOnMarker) {
        if (showNameInToolbar) {
            mTopSearchField.setToolbarText(location.getName(), clearMapButtonVisible);
        }

        if (mMapControl != null) {
            // Check the location type:
            final boolean locationIsVenue = location.isLocationOfTypeVenue();
            final boolean locationIsBuilding = location.isLocationOfTypeBuilding();
            final boolean locationIsFloor = location.isLocationOfTypeFloor();

            final boolean animateToLocationBoundaries = locationIsVenue || locationIsBuilding || locationIsFloor;

            if (!animateToLocationBoundaries) {
                if (clickedOnMarker) {
                    mMapControl.selectLocation(location, true, changeZoomLevel, true, true);
                } else {
                    mMapControl.selectLocation(location, changeZoomLevel);
                }
            } else {
                focusOnLocationBoundaries(
                        location.getGeometry(),
                        MapsIndoorsSettings.MAPSINDOORS_TILES_AVAILABLE_ZOOM_LEVEL,
                        true
                );
            }
        }
    }

    public void selectLocations(@NonNull List<MPLocation> locations) {
        if (mMapControl != null) {
            mMapControl.selectFloor(locations.get(0).getFloor());   // .getFloorIndex() );

            mMapControl.displaySearchResults(
                    locations,
                    true,
                    MapsIndoorsSettings.DISPLAY_SEARCH_RESULTS_CAMERA_PADDING_IN_DP
            );
        }
    }

    public void selectSearchResult(@NonNull List<MPLocation> locations) {
        mMapControl.displaySearchResults(
                locations,
                true,
                MapsIndoorsSettings.DISPLAY_SEARCH_RESULTS_CAMERA_PADDING_IN_DP, () -> {
                    if (!locations.isEmpty())
                        mMapControl.selectFloor(locations.get(0).getFloor());
                });

    }

    // this function is linked to the returnToVenueButton logic, if you need the selection label for other purpose please implement your own method
    @NonNull
    public String getSelectionLabelForReturnToVenue() {

        if (mCurrentVenue != null) {
            return mCurrentVenue.getVenueInfo().getName();
        }

        return "";
    }

    public void selectCurrentVenue() {
        setCurrentVenue(mCurrentVenue);
    }

    boolean focusOnLocationBoundaries(@NonNull Geometry locationGeometry, float minZoomLevel, boolean animateCamera) {
        final LatLngBounds latLngBounds;
        @Geometry.GeometryType final int geometryType = locationGeometry.getIType();
        switch (geometryType) {

            case Geometry.TYPE_GEOMETRYCOLLECTION:
            case Geometry.TYPE_LINESTRING:
            case Geometry.TYPE_MULTILINESTRING:
            case Geometry.TYPE_MULTIPOINT:
            case Geometry.TYPE_POINT:
            default:
                latLngBounds = null;
                break;
            case Geometry.TYPE_MULTIPOLYGON: {
                final MultiPolygonGeometry geometry = (MultiPolygonGeometry) locationGeometry;
                latLngBounds = geometry.getLatLngBounds();
                break;
            }
            case Geometry.TYPE_POLYGON: {
                final PolygonGeometry geometry = (PolygonGeometry) locationGeometry;
                latLngBounds = geometry.getLatLngBounds();
                break;
            }
        }

        if (latLngBounds != null) {
            final GoogleMap googleMap = mActivity.getGoogleMap();
            if (googleMap != null) {
                // Save current camera values
                final CameraPosition srcCameraPosition = googleMap.getCameraPosition();

                // Move to the given bounds
                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 10));

                // Check the resulting zoom level
                final CameraPosition dstCameraPosition = googleMap.getCameraPosition();
                final float resZoomLevel = dstCameraPosition.zoom;
                final boolean animateCameraToCappedZoomLevel = resZoomLevel < minZoomLevel;

                // Move the camera back to the src position
                googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(srcCameraPosition));

                if (animateCameraToCappedZoomLevel) {
                    // Build a new camera position reusing all but zoom values
                    final CameraPosition newCameraPosition = new CameraPosition.Builder().
                            target(dstCameraPosition.target).
                            tilt(dstCameraPosition.tilt).
                            bearing(dstCameraPosition.bearing).
                            zoom(minZoomLevel).
                            build();

                    if (animateCamera) {
                        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition));
                    } else {
                        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition));
                    }
                } else {
                    // Reuse the camera position object we've got from the first camera move
                    if (animateCamera) {
                        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(dstCameraPosition));
                    } else {
                        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(dstCameraPosition));
                    }
                }
            }
        }

        return latLngBounds != null;
    }

    public void moveCameraToCurrentVenue() {
        if (mCurrentVenue != null) {
            //Disable any tracking state, the use may be in, when clicking "return to venue"
            mActivity.getUserPositionTrackingViewModel().stopTracking();

            // Set camera position to venue
            focusOnLocationBoundaries(
                    mCurrentVenue.getGeometry(),
                    MapsIndoorsSettings.MAPSINDOORS_TILES_AVAILABLE_ZOOM_LEVEL,
                    false
            );
        }
    }
}
