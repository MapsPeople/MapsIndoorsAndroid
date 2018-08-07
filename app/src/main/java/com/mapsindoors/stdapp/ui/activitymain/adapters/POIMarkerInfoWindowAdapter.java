package com.mapsindoors.stdapp.ui.activitymain.adapters;

/**
 * Created by amine on 25/01/2018.
 */

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.mapsindoors.mapssdk.Location;
import com.mapsindoors.mapssdk.MapControl;
import com.mapsindoors.mapssdk.dbglog;
import com.mapsindoors.stdapp.BuildConfig;
import com.mapsindoors.stdapp.R;


/**
 * Custom Info Window used in MI markers
 */
public class POIMarkerInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    public static final String TAG = POIMarkerInfoWindowAdapter.class.getSimpleName();

    public static final int INFOWINDOW_TYPE_LINK = 0;

    public static final int INFOWINDOW_TYPE_NORMAL = 1;

    private int currentType;
    private Context mCtx;
    private View mInfoWindowView;
    MapControl mMapControl;
    private TextView mTitleTextView;
    private String mTitleTextStrFormat;


    public POIMarkerInfoWindowAdapter(Context ctx, MapControl mapControl) {
        mCtx = ctx;

        View v = mInfoWindowView = LayoutInflater.from(mCtx).inflate(R.layout.control_poi_infowindow, null);
        int ScreenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;

        mTitleTextView = v.findViewById(R.id.poi_info_title);
        mTitleTextView.setMaxWidth((ScreenWidth/100) * 60);
	    mTitleTextStrFormat = ctx.getString( R.string.poi_info_window_title_text_format );
        mMapControl = mapControl;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        View view = mInfoWindowView;

        if (view != null) {
            renderView(marker, view);
        }
        return view;
    }


	void renderView( Marker marker, View view )
	{
		Location location = mMapControl.getLocation( marker );

		if( location != null )
		{
			final String locationName = location.getName();
			final String locationType = location.getType();

			final boolean gotName = !TextUtils.isEmpty( locationName );
			final boolean gotType = !TextUtils.isEmpty( locationType );

			if( gotName && gotType )
			{
				mTitleTextView.setText( String.format( mTitleTextStrFormat, locationName, locationType ) );
			}
			else
			{
				mTitleTextView.setText( locationType );
			}
		}
		else
		{
			if( BuildConfig.DEBUG )
			{
				dbglog.LogW( TAG, "renderView: got no location (null) for the given marker (" + marker.getId() + marker.getTitle() + ")" );
			}
		}
	}

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }


    public void setInfoWindowType(int type) {
        currentType = type;

	    switch( currentType )
	    {
		    case INFOWINDOW_TYPE_LINK:
			    mTitleTextView.setTextColor( ContextCompat.getColor( mCtx, R.color.blueGray ) );
			    break;

		    case INFOWINDOW_TYPE_NORMAL:
			    mTitleTextView.setTextColor( Color.BLACK );
			    break;
	    }
    }
}
