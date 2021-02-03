package com.mapsindoors.stdapp.ui.transportagencies.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mapsindoors.stdapp.R;
import com.mapsindoors.stdapp.helpers.MapsIndoorsUtils;
import com.mapsindoors.stdapp.ui.transportagencies.models.TransportAgencyItem;

import java.util.ArrayList;
import java.util.List;


/**
 * TransportAgenciesAdapter
 * MapsIndoorsDemo
 * <p>
 * Created by Jose J Varó on 18/08/2017.
 * Copyright © 2017 MapsPeople A/S. All rights reserved.
 */
public class TransportAgenciesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = TransportAgenciesAdapter.class.getSimpleName();

    /* ======================================================================================
        List item types (in this case, one). Will be used on the vertical direction panel
     */
    private static final int TYPE_AGENCY_ITEM = 0;

    private Context mContext;
    private List<TransportAgencyItem> mItemList;


    public TransportAgenciesAdapter(Context context) {
        mContext = context;
        mItemList = new ArrayList<>();

        notifyDataSetChanged();
    }


    //region RecyclerView.Adapter
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == TYPE_AGENCY_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.control_transport_sources_item, parent, false);
            return new TransportAgencyViewHolder(v);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        onBindTransportAgencyViewHolder((TransportAgencyViewHolder) holder, position);
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_AGENCY_ITEM;
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }
    //endregion


    public void setItems(@NonNull List<TransportAgencyItem> items) {
        mItemList.clear();
        mItemList.addAll(items);

        notifyDataSetChanged();
    }

    private void onBindTransportAgencyViewHolder(TransportAgencyViewHolder holder, int position) {
        TransportAgencyItem item = mItemList.get(position);

        // Display only the domain name
        String rootUrl = MapsIndoorsUtils.getDomainName(item.agencyInfoUrl);

        holder.agencyNameView.setText(item.agencyInfoName);
        holder.agencyUrlView.setText(rootUrl);
    }


    //region View Holders
    private class TransportAgencyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView agencyNameView;
        TextView agencyUrlView;

        TransportAgencyViewHolder(View itemView) {
            super(itemView);

            agencyNameView = itemView.findViewById(R.id.transp_src_agency_info_name);
            agencyUrlView = itemView.findViewById(R.id.transp_src_agency_info_url);

            // set the click listener to the above item's parent
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            TransportAgencyItem clickedItem = mItemList.get(getLayoutPosition());


            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(clickedItem.agencyInfoUrl));
            mContext.startActivity(browserIntent);
        }
    }
    //endregion
}
