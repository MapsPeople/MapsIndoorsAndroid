package com.mapsindoors.stdapp.ui.components.mapfloorselector;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mapsindoors.mapssdk.Floor;
import com.mapsindoors.mapssdk.MapsIndoors;
import com.mapsindoors.stdapp.R;

import java.util.ArrayList;
import java.util.List;

class MapFloorSelectorAdapter extends ArrayAdapter<Floor> {

    private MapFloorSelectorAdapterListener mMapFloorSelectorAdapterListener;
    private List<Floor> mItems;
    private int mSelectedPosition;
    private int mUserLocationListPosition;

    /**
     * Default constructor
     * @param context Context for inflating layouts
     * @param resource Default item layout
     */
    MapFloorSelectorAdapter(@NonNull Context context, @LayoutRes int resource) {
        super(context, resource);
        mSelectedPosition = 0;
        mUserLocationListPosition = Floor.NO_FLOOR_INDEX;
        mItems = new ArrayList<>();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;

        if(convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.control_mapsindoors_floor_selector_button,parent,false);
        } else {
            view = convertView;
        }

        TextView textView = view.findViewById(R.id.mapspeople_floor_selector_btn);

        int selectedColor;
        int unselectedColor;
        int selectedText;
        int unselectedTextColor;

        if(MapsIndoors.getApplicationContext() != null) {
            Resources res = getContext().getResources();
            Resources.Theme appTheme = MapsIndoors.getApplicationContext().getTheme();

            selectedColor = ResourcesCompat.getColor(res, R.color.floorselector_background_btn_selected,appTheme);
            unselectedColor = ResourcesCompat.getColor(res, R.color.floorselector_background_btn_default,appTheme);
            selectedText = ResourcesCompat.getColor(res, R.color.blueGray,appTheme);
            unselectedTextColor = ResourcesCompat.getColor(res, R.color.grey, appTheme);
        } else {
            selectedColor = Color.WHITE;
            unselectedColor = Color.parseColor("#d1d1d1");
            selectedText = Color.parseColor("#43aaa0");
            unselectedTextColor = Color.parseColor("#89000000");
        }

        textView.setTextColor(unselectedTextColor);

        if(position == mSelectedPosition){
            textView.setBackgroundColor(selectedColor);
            textView.setTypeface(null, Typeface.BOLD);
        } else {
            textView.setBackgroundColor(unselectedColor);
            textView.setTypeface(null, Typeface.NORMAL);
        }

        if((mItems.get(position).getZIndex() == mUserLocationListPosition) && (mUserLocationListPosition != Floor.NO_FLOOR_INDEX)){
            textView.setTextColor(selectedText);
        }

        textView.setText(mItems.get(position).getDisplayName());

        return view;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Nullable
    @Override
    public Floor getItem(int position) {
        return mItems.get(position);
    }

    /**
     * Sets the list of {@link Floor}s to show in the ListView
     * @param floors Floors.
     */
    void setFloors(@NonNull List<Floor> floors){
        mItems.clear();
        mItems.addAll(floors);
    }

    /**
     * The ONLY actual selection in this adapter.
     * Sets the internal selection, notifies both internal and if a {@link MapFloorSelectorAdapterListener}
     * is set, alerts that too.
     *
     * @param position The LIST position to select,
     */
    void setSelectedListPosition(int position){
        mSelectedPosition = position;
        notifyDataSetChanged();
        if(mMapFloorSelectorAdapterListener != null){
            mMapFloorSelectorAdapterListener.onFloorSelectionChanged(mItems.get(position));
        }
    }

    /**
     * Selects the floor by the Z index of the Floor
     * @param value Z index to select
     */
    private void selectFloorByZIndex(int value){
        for(int i = 0, length = mItems.size(); i <length; ++i){
            if(mItems.get(i).getZIndex() == value) {
                setSelectedListPosition(i);
                return;
            }
        }
    }

    /**
     * Sets the selection, based on a {@link Floor}
     * @param selectedFloor Floor to select.
     */
    void setSelectedFloor(@NonNull Floor selectedFloor){
        selectFloorByZIndex(selectedFloor.getZIndex());
    }

    /**
     * Sets a {@link MapFloorSelectorAdapterListener} for receiving callbacks for ALL selections
     * in stead of standard onItemClick.
     * @param callback Callback if necessary
     */
    void setCallback(@Nullable MapFloorSelectorAdapterListener callback){
        mMapFloorSelectorAdapterListener = callback;
    }


    /**
     * Sets the current floor for the user's position
     * @param zIndex Z-index of the floor.
     */
    void setUserPositionFloor(int zIndex) {
        mUserLocationListPosition = zIndex;
    }
}
