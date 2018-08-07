package com.mapsindoors.stdapp.ui.activitymain;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mapsindoors.stdapp.R;

/**
 * @author Martin Hansen
 */
public class TopSearchField
{
	Activity mContext;
	private boolean  mIsActive;
	private Button   mClearMapButton;
	private Toolbar  mToolbar;
	TextView mTitle;

	TopSearchField( @NonNull Activity context ) {
		mContext = context;

		mToolbar = context.findViewById( R.id.toolbar );
		mTitle = context.findViewById( R.id.toolbar_title );
		mToolbar.setTitle( "" );

		mTitle.setText( R.string.app_name );

		mToolbar.setNavigationIcon( R.drawable.ic_menu_white );
		mClearMapButton = context.findViewById( R.id.clear_map_button );

		setToolbarText( null, false );
	}

	void setCloseButtonClickListener( @NonNull View.OnClickListener listener )
	{
		mClearMapButton.setOnClickListener( listener );
	}

	//Sets a search text and activates the close button.
	//Once exit is pressed onClosePressed will be called.
	public void setToolbarText( @Nullable String newText, boolean closeButtonVisibility )
	{
		mTitle.setText( (newText != null) ? newText : "" );
		mClearMapButton.setVisibility( closeButtonVisibility ? View.VISIBLE : View.INVISIBLE );
		mIsActive = closeButtonVisibility;

		setcloseButtonVisibility(mIsActive);
	}

	Toolbar getToolbarView() {
		return mToolbar;
	}

	public void setEnabled( boolean enabled )
	{
		mToolbar.setEnabled( enabled );
	}

	private void setcloseButtonVisibility( boolean visibility )
	{
		mClearMapButton.setVisibility( visibility ? View.VISIBLE : View.INVISIBLE );
	}
}
