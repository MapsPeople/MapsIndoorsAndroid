package com.mapsindoors.stdapp.ui.venueselector.adapters;

import android.graphics.Bitmap;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mapsindoors.stdapp.R;
import com.mapsindoors.stdapp.ui.venueselector.listeners.IVenueClickedListener;
import com.mapsindoors.stdapp.ui.venueselector.models.VenueSelectorItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jose J Varó (jjv@mapspeople.com) on 13-04-2017.
 */

public class VenueSelectorAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = VenueSelectorAdapter.class.getSimpleName();

    /* ======================================================================================
        List item types (in this case, one). Will be used on the vertical direction panel
     */
    private static final int TYPE_VENUE_ITEM = 0;


    private IVenueClickedListener mClickListener;
    private List<VenueSelectorItem> mItemList;


    public VenueSelectorAdapter(IVenueClickedListener listener) {
        mClickListener = listener;
        mItemList = new ArrayList<>();

        setItems(null);
    }


    //region RecyclerView.Adapter
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == TYPE_VENUE_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.control_venue_selector_item, parent, false);
            return new VenueViewHolder(v);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        onBindVenueViewHolder((VenueViewHolder) holder, position);
    }

    @Override
    public int getItemViewType(int position) {

        // return a venue type for any position
        return TYPE_VENUE_ITEM;
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }
    //endregion


    public void setItems(List<VenueSelectorItem> items) {
        mItemList.clear();

        if (items != null) {

            mItemList.addAll(items);
        }

        notifyDataSetChanged();
    }

    private void onBindVenueViewHolder(VenueViewHolder holder, int position) {
        VenueSelectorItem item = mItemList.get(position);

        holder.mVenueName.setText(item.getRenderName());

        Bitmap bmp = item.getImageBmp();
        holder.mVenueImage.setImageBitmap(bmp);
    }


    //region View Holders
    private class VenueViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView mVenueImage;
        TextView mVenueName;

        VenueViewHolder(View itemView) {
            super(itemView);

            mVenueImage = itemView.findViewById(R.id.venue_selector_item_img);
            mVenueName = itemView.findViewById(R.id.venue_selector_item_text);

            // set the click listener to the above item's parent
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mClickListener != null) {
                VenueSelectorItem clickedItem = mItemList.get(getLayoutPosition());

                mClickListener.OnVenueClicked(clickedItem.getId());
            }
        }
    }
    //endregion
}
