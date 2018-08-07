package com.mapsindoors.stdapp.ui.appInfo.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mapsindoors.stdapp.R;
import com.mapsindoors.stdapp.ui.appInfo.models.CreditItem;

import java.util.ArrayList;
import java.util.List;


/**
 * TransportAgenciesAdapter
 * MapsIndoorsDemo
 * <p>
 * Created by Jose J Varó on 18/08/2017.
 * Copyright © 2017 MapsPeople A/S. All rights reserved.
 */
public class AppInfoAdapter extends RecyclerView.Adapter< RecyclerView.ViewHolder >
{
	private static final String TAG = AppInfoAdapter.class.getSimpleName();

	/* ======================================================================================
		List item types (in this case, one). Will be used on the vertical direction panel
	 */
	private static final int TYPE_CREDIT_ITEM = 0;

	private Context mContext;
	private List<CreditItem> mItemList;


	public AppInfoAdapter(Context context,  List<CreditItem> itemList) {
		mContext = context;
		mItemList = new ArrayList<>();
		mItemList.addAll(itemList);
		notifyDataSetChanged();
	}


	//region RecyclerView.Adapter
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder( ViewGroup parent, int viewType ) {

		switch( viewType ) {
			case TYPE_CREDIT_ITEM: {
				View v = LayoutInflater.from( parent.getContext() ).inflate( R.layout.control_app_info_credits_item, parent, false );
				return new AppInfoCreditViewHolder( v );
			}
		}

		return null;
	}

	@Override
	public void onBindViewHolder( RecyclerView.ViewHolder holder, int position ) {
		onBindTransportAgencyViewHolder( (AppInfoCreditViewHolder) holder, position );
	}

	@Override
	public int getItemViewType( int position ) {
		return TYPE_CREDIT_ITEM;
	}

	@Override
	public int getItemCount() {
		return mItemList.size();
	}
	//endregion


	public void setItems( @NonNull List<CreditItem> items ) {
		mItemList.clear();
		mItemList.addAll( items );

		notifyDataSetChanged();
	}

	private void onBindTransportAgencyViewHolder(AppInfoCreditViewHolder holder, int position ) {
		CreditItem item = mItemList.get( position );

		holder.titleTextView.setText( item.name );
	}


	//region View Holders
	private class AppInfoCreditViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
	{
		private TextView titleTextView;

		AppInfoCreditViewHolder(View itemView ) {
			super( itemView );

			titleTextView = itemView.findViewById( R.id.item_title );

			// set the click listener to the above item's parent
			itemView.setOnClickListener( this );
		}

		@Override
		public void onClick( View v ) {
			CreditItem clickedItem = mItemList.get( getLayoutPosition() );
			Intent browserIntent = new Intent( Intent.ACTION_VIEW, Uri.parse( clickedItem.url ) );
			mContext.startActivity( browserIntent );
		}
	}
	//endregion
}
