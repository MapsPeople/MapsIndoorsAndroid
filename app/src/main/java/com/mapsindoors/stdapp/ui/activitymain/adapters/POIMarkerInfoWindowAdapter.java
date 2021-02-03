package com.mapsindoors.stdapp.ui.activitymain.adapters;

/**
 * Created by amine on 25/01/2018.
 */

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.mapsindoors.mapssdk.MPLocation;
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
    private MapControl mMapControl;
    private TextView mTitleTextView;


    public POIMarkerInfoWindowAdapter(Context ctx, MapControl mapControl) {
        mCtx = ctx;

        View v = mInfoWindowView = LayoutInflater.from(mCtx).inflate(R.layout.control_poi_infowindow, null);
        int ScreenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;

        mTitleTextView = v.findViewById(R.id.poi_info_title);
        mTitleTextView.setMaxWidth((ScreenWidth / 100) * 60);
        mMapControl = mapControl;
    }


    //region IMPLEMENTS GoogleMap.InfoWindowAdapter
    @Override
    public View getInfoWindow(Marker marker) {
        View view = mInfoWindowView;

        if (view != null) {
            renderView(marker);
        }
        return view;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
    //endregion


    void renderView(Marker marker) {
        MPLocation location = mMapControl.getLocation(marker);

        if (location != null) {
            final String locationName = location.getName();


            mTitleTextView.setText(locationName);


        } else {
            if (BuildConfig.DEBUG) {
                dbglog.LogW(TAG, "renderView: got no location (null) for the given marker (" + marker.getId() + marker.getTitle() + ")");
            }
        }
    }

    public void setInfoWindowType(int type) {
        currentType = type;

        switch (currentType) {
            case INFOWINDOW_TYPE_LINK:
                mTitleTextView.setTextColor(ContextCompat.getColor(mCtx, R.color.info_window_text_color));
                break;

            case INFOWINDOW_TYPE_NORMAL:
                mTitleTextView.setTextColor(Color.BLACK);
                break;
        }
    }
}
