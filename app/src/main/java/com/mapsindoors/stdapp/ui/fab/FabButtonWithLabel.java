package com.mapsindoors.stdapp.ui.fab;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mapsindoors.stdapp.R;

/**
 * Created by amine on 03/01/2018.
 */

public class FabButtonWithLabel extends RelativeLayout
{

	TextView             label;
	FloatingActionButton fabButton;

	public FabButtonWithLabel( Context context, AttributeSet attrs )
	{
		super( context, attrs );
		LayoutInflater inflater = LayoutInflater.from( context );
		inflater.inflate( R.layout.control_custom_floating_button, this );
		//
		fabButton = findViewById( R.id.fab_button );
		label = findViewById( R.id.fab_button_label );
	}

	public void setLabelText( String labelText )
	{
		label.setText( labelText );
	}


	public void setFabButtonImageBitmap( Bitmap bitmap )
	{
		fabButton.setImageBitmap( bitmap );
	}


	public TextView getLabel()
	{
		return label;
	}

	public FloatingActionButton getFabButton()
	{
		return fabButton;
	}
}
