package com.mapsindoors.stdapp.condeco;

import com.mapsindoors.mapssdk.LocationDisplayRule;
import com.mapsindoors.mapssdk.MPLocation;
import com.mapsindoors.mapssdk.MapControl;
import com.mapsindoors.mapssdk.MapsIndoors;
import com.mapsindoors.stdapp.R;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CondecoMapHandler
{
    static final int MI_ICON_SIZE_IN_DP = (int)(LocationDisplayRule.DEFAULT_ICON_UNIFORM_SIZE_IN_DP * 1f);

    static final String GROUP_ROOM_AVAILABLE_NAME = "GROUP_ROOM_AVAILABLE";
    static final LocationDisplayRule GROUP_ROOM_AVAILABLE = new LocationDisplayRule.Builder( GROUP_ROOM_AVAILABLE_NAME ).
            setVectorDrawableIcon(R.drawable.ic_group_room_green, MI_ICON_SIZE_IN_DP, MI_ICON_SIZE_IN_DP ).
            setZoomLevelOn( 15 ).
            setZoomLevelOff( 21 ).
            setLabelZoomLevelOn(15).
            setLabelZoomLevelOff(21).
            setVisible( true ).
            setLabel( null ).
            setShowLabel( false ).
            build();

    static final String GROUP_ROOM_BOOKED_NAME = "GROUP_ROOM_BOOKED";
    static final LocationDisplayRule GROUP_ROOM_BOOKED = new LocationDisplayRule.Builder( GROUP_ROOM_BOOKED_NAME ).
            setVectorDrawableIcon(R.drawable.ic_group_room_red, MI_ICON_SIZE_IN_DP, MI_ICON_SIZE_IN_DP ).
            setZoomLevelOn( 15 ).
            setZoomLevelOff( 21 ).
            setLabelZoomLevelOn(15).
            setLabelZoomLevelOff(21).
            setVisible( true ).
            setLabel( null ).
            setShowLabel( false ).
            build();

    static final String OFFICE_AVAILABLE_NAME = "OFFICE_AVAILABLE";
    static final LocationDisplayRule OFFICE_AVAILABLE = new LocationDisplayRule.Builder( OFFICE_AVAILABLE_NAME ).
            setVectorDrawableIcon(R.drawable.ic_office_green, MI_ICON_SIZE_IN_DP, MI_ICON_SIZE_IN_DP ).
            setZoomLevelOn( 15 ).
            setZoomLevelOff( 21 ).
            setLabelZoomLevelOn(15).
            setLabelZoomLevelOff(21).
            setVisible( true ).
            setLabel( null ).
            setShowLabel( false ).
            build();

    static final String OFFICE_BOOKED_NAME = "OFFICE_BOOKED";
    static final LocationDisplayRule OFFICE_BOOKED = new LocationDisplayRule.Builder( OFFICE_BOOKED_NAME ).
            setVectorDrawableIcon(R.drawable.ic_office_red, MI_ICON_SIZE_IN_DP, MI_ICON_SIZE_IN_DP ).
            setZoomLevelOn( 15 ).
            setZoomLevelOff( 21 ).
            setLabelZoomLevelOn(15).
            setLabelZoomLevelOff(21).
            setVisible( true ).
            setLabel( null ).
            setShowLabel( false ).
            build();

    static final String BENCH_AVAILABLE_NAME = "BENCH_AVAILABLE";
    static final LocationDisplayRule BENCH_AVAILABLE = new LocationDisplayRule.Builder( BENCH_AVAILABLE_NAME ).
            setVectorDrawableIcon(R.drawable.ic_office_var_green, MI_ICON_SIZE_IN_DP, MI_ICON_SIZE_IN_DP ).
            setZoomLevelOn( 15 ).
            setZoomLevelOff( 21 ).
            setLabelZoomLevelOn(15).
            setLabelZoomLevelOff(21).
            setVisible( true ).
            setLabel( null ).
            setShowLabel( false ).
            build();

    static final String BENCH_BOOKED_NAME = "BENCH_BOOKED";
    static final LocationDisplayRule BENCH_BOOKED = new LocationDisplayRule.Builder( BENCH_BOOKED_NAME ).
            setVectorDrawableIcon(R.drawable.ic_office_var_red, MI_ICON_SIZE_IN_DP, MI_ICON_SIZE_IN_DP ).
            setZoomLevelOn( 15 ).
            setZoomLevelOff( 21 ).
            setLabelZoomLevelOn(15).
            setLabelZoomLevelOff(21).
            setVisible( true ).
            setLabel( null ).
            setShowLabel( false ).
            build();

    private MapControl mMapControl;

    private CondecoBookingProvider mBookingProvider;

    private Map<String, MPLocation> mExternalIdIndex;

    public CondecoMapHandler(MapControl mapControl) {
        this.mMapControl = mapControl;
        this.mExternalIdIndex = new HashMap<>();
    }

    public void setupBookingProvider(CondecoBookingProvider bookingProvider)
    {
        if (mBookingProvider != null)
            return;

        mBookingProvider = bookingProvider;

        List<MPLocation> locations = MapsIndoors.getLocations();

        for (MPLocation loc: locations) {

            if (loc.getExternalId() != null)
            {
                mExternalIdIndex.put(loc.getExternalId(), loc);
            }
        }

        bookingProvider.setExternalIds(mExternalIdIndex.keySet());
        bookingProvider.addOnPositionUpdateListener(this::onBookingProviderUpdate);
        bookingProvider.startPolling();
    }

    private void onBookingProviderUpdate(CondecoBookingResult result) {
        if (mMapControl != null) {

            MPLocation loc = mExternalIdIndex.get(result.getExternalId());

            if (loc != null) {
                boolean booked = determineBookingState(result);
                LocationDisplayRule displayRule = determineDisplayRule(loc, booked);
                mMapControl.setDisplayRule(displayRule, loc);
            }
        }
    }

    private boolean determineBookingState(CondecoBookingResult result) {

        Date currentDate = new Date();
        boolean booked = false;
        int i = 0;
        while (!booked && i < result.getBookings().size()) {
            CondecoBooking booking = result.getBookings().get(i);

            boolean startsBefore = booking.getTimeFromUTCDate().before(currentDate);
            boolean endsAfter = booking.getTimeToUTCDate().after(currentDate);
            booked = startsBefore && endsAfter;

            i++;
        }

        return booked;
    }

    private LocationDisplayRule determineDisplayRule(MPLocation loc, boolean booked)
    {
        switch (loc.getType())
        {
            case "Workstation":
                if (booked) {
                    return BENCH_BOOKED;
                }

                return BENCH_AVAILABLE;
            case "MeetingRoom":
                if (booked) {
                    return GROUP_ROOM_BOOKED;
                }

                return GROUP_ROOM_AVAILABLE;
            case "Office":
                if (booked) {
                    return OFFICE_BOOKED;
                }

                return OFFICE_AVAILABLE;
            default:
                return null;
        }
    }
}
