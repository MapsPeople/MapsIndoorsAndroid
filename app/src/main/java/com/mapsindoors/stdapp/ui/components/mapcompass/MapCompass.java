package com.mapsindoors.stdapp.ui.components.mapcompass;

import android.content.Context;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StyleRes;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.mapsindoors.stdapp.R;

/**
 * Created by Jose J Varó (jjv@mapspeople.com) on 03/11/2017.
 */
public class MapCompass extends FrameLayout
		implements
			GoogleMap.OnCameraMoveStartedListener,
			GoogleMap.OnCameraMoveListener,
			GoogleMap.OnCameraMoveCanceledListener

{
	public final String TAG = MapCompass.class.getSimpleName();


	private static final int FADE_IN_ANIM_TIME_IN_MS = 250;
	private static final int FADE_OUT_ANIM_TIME_IN_MS = 500;
	private static final int FADE_OUT_START_DELAY_IN_MS = 1500;

	private static final int EVENT_CAMERA_MOVE_STARTED  = 0;
	private static final int EVENT_CAMERA_MOVED         = 1;
	private static final int EVENT_CAMERA_MOVE_CANCELED = 2;
	public static final int EVENT_CAMERA_IDLE          = 3;


	private GoogleMap mGoogleMap;
	OnMapCompassClickedListener mClickListener;
	private ViewPropertyAnimatorCompat mAnimator;
	private ImageButton mCompassBGView;
	private View mNeedleView;
	boolean mWillShowView, mWillShowViewPrev, mShowViewCancelled, mViewAnimating;
	private float mPrevCamBearing, mPrevCamTilt;


	//region CTOR
	public MapCompass( @NonNull Context context )
	{
		super( context );
		init( context );
	}

	public MapCompass( @NonNull Context context, @Nullable AttributeSet attrs )
	{
		super( context, attrs );
		init( context );
	}

	public MapCompass( @NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr )
	{
		super( context, attrs, defStyleAttr );
		init( context );
	}

	@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
	public MapCompass( @NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes )
	{
		super( context, attrs, defStyleAttr, defStyleRes );
		init( context );
	}
	//endregion


	private void init(Context context)
	{
		inflate( context, R.layout.control_mapsindoors_map_compass, this );

		mAnimator = null;
		mPrevCamBearing = mPrevCamTilt = 0;
		mViewAnimating = false;

		mNeedleView = findViewById( R.id.mapsindoors_map_compass_needle );

		mCompassBGView = findViewById( R.id.mapsindoors_map_compass );
		mCompassBGView.setOnClickListener( v -> {
			if( mClickListener != null ) {
				mClickListener.onMapCompassClicked();
			}
		} );

		show(false, false);
	}

	public void setGoogleMap(GoogleMap googleMap)
	{
		mGoogleMap = googleMap;
	}

	private void show( boolean show, boolean animated )
	{
		float cAlpha = this.getAlpha();
		mWillShowViewPrev = mWillShowView;
		mWillShowView = show;

		if( !animated ) {
			if( show && (cAlpha < 0.1f) ) {
				setAlpha( 1f );
				setVisibility( View.VISIBLE );
			}
			else if( !show && (cAlpha > 0.9f) ) {
				setAlpha( 0f );
				setVisibility( View.INVISIBLE );
			}
		}
		else {
			mAnimator = (mAnimator != null) ? mAnimator : ViewCompat.animate( this );

			// Check if an ongoing anim has to be cancelled
			if( !mViewAnimating )
			{
				if( show && (cAlpha < 0.1f) ) {

					if( getVisibility() != View.VISIBLE ) {
						setVisibility( View.VISIBLE );
					}

					// Fade in anim setup
					mAnimator
							.alpha( 1f )
							.setDuration( FADE_IN_ANIM_TIME_IN_MS )
							.setListener( mVisAnimatorListener )
							.start();
				}
				else if( !show && (cAlpha > 0.9f) ) {
					// Fade out anim setup
					mAnimator
							.alpha( 0f )
							.setDuration( FADE_OUT_ANIM_TIME_IN_MS )
							.setListener( mVisAnimatorListener )
							.setStartDelay( FADE_OUT_START_DELAY_IN_MS );
				}
			}
		}
	}

	public boolean isVisible()
	{
		return !(getAlpha() < 0.1) ;
	}

	private ViewPropertyAnimatorListener mVisAnimatorListener = new ViewPropertyAnimatorListener() {
		@Override
		public void onAnimationStart( View view ) {
			mViewAnimating = true;
		}

		@Override
		public void onAnimationEnd( View view ) {
			if( !mWillShowView && (getVisibility() == View.VISIBLE) ) {
				setVisibility( View.INVISIBLE );
			}
			mViewAnimating = false;
		}

		@Override
		public void onAnimationCancel( View view )
		{
			mShowViewCancelled = true;
			mViewAnimating = false;
		}
	};


	//region Implements Google Maps Camera event listeners

	@Override
	public void onCameraMoveStarted( int reason ) {
		updateFromCameraEvent( EVENT_CAMERA_MOVE_STARTED, reason );
	}

	@Override
	public void onCameraMove() {
		updateFromCameraEvent( EVENT_CAMERA_MOVED, 0 );
	}

	@Override
	public void onCameraMoveCanceled() {
		updateFromCameraEvent( EVENT_CAMERA_MOVE_CANCELED, 0 );
	}

	public void updateFromCameraEvent(int cameraEvent, int moveStartedReason)
	{
		if( mGoogleMap == null ) {
			return;
		}

		CameraPosition pos = mGoogleMap.getCameraPosition();
		float camBearing = pos.bearing;
		float camTilt = pos.tilt;

		// Check if either bearing or tilt have changed since last
		if( (Double.compare( camBearing, mPrevCamBearing ) != 0) || (Double.compare( camTilt, mPrevCamTilt ) != 0) ) {

			mNeedleView.setRotation( -camBearing );
			mNeedleView.setRotationX( camTilt );

			boolean allZero = (Double.compare( pos.tilt, 0 ) == 0) && (Double.compare( pos.bearing, 0 ) == 0);
			{
				if( allZero && isVisible() ) {
					show( false, true );
				} else if( !isVisible() ) {
					show( true, false );
				}
			}
		}

		// save
		mPrevCamBearing = camBearing;
		mPrevCamTilt = camTilt;
	}

	//endregion


	public void setOnCompassClickedListener( OnMapCompassClickedListener listener ) {
		mClickListener = listener;
	}

	public interface OnMapCompassClickedListener {
		void onMapCompassClicked();
	}
}
