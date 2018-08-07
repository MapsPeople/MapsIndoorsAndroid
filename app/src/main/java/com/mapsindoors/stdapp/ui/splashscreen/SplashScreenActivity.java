package com.mapsindoors.stdapp.ui.splashscreen;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.mapsindoors.mapssdk.dbglog;
import com.mapsindoors.stdapp.Application;
import com.mapsindoors.stdapp.BuildConfig;
import com.mapsindoors.stdapp.R;
import com.mapsindoors.stdapp.helpers.MapsIndoorsUtils;
import com.mapsindoors.stdapp.ui.activitymain.MapsIndoorsActivity;

import java.util.Locale;

import static com.mapsindoors.stdapp.helpers.MapsIndoorsUtils.isNetworkReachable;


/**
 * SplashScreenActivity
 * MapsIndoorsDemo
 * <p>
 * Created by Jose J Varó on 03/19/2017.
 * Copyright © 2017 MapsPeople A/S. All rights reserved.
 */public class SplashScreenActivity extends AppCompatActivity
{
	private static final String TAG = SplashScreenActivity.class.getSimpleName();

	private static final long MINIMUM_SCREEN_DELAY = 250;

	private long mStartTime;
	private long mOnCreateTime, mAppCreateTDiff;

	ViewGroup mSplashMainLayout;
	ViewGroup mSplashMainView;
	ImageView mIconStatic, mIconDynamic;

	boolean mAnimIconDone, mAnimMainViewDone;


	@Override
	protected void onCreate( @Nullable Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );

		if( BuildConfig.DEBUG )
		{
			mOnCreateTime = System.nanoTime();
			mAppCreateTDiff = mOnCreateTime - Application.getInstance().mAppCreateTime;
		}

		mAnimIconDone = mAnimMainViewDone = false;

		setContentView( R.layout.fragment_splashscreen );
		mSplashMainLayout = findViewById( R.id.splash_layout );
		mSplashMainView = findViewById( R.id.splash_main );

		mIconStatic = mSplashMainLayout.findViewById( R.id.splash_icon);
		mIconDynamic = mSplashMainLayout.findViewById( R.id.splash_icon_2);

		if(! isNetworkReachable(this)){
			Snackbar.make(getWindow().getDecorView().getRootView()
					, getResources().getString(R.string.no_internet_snackbar_message),
					Snackbar.LENGTH_LONG)
					.setAction("Action", null).show();
		}


		mSplashMainLayout.getViewTreeObserver().addOnGlobalLayoutListener( new ViewTreeObserver.OnGlobalLayoutListener() {
			public void onGlobalLayout() {
				//Remove the listener before proceeding
				mSplashMainLayout.getViewTreeObserver().removeOnGlobalLayoutListener( this );

				int dstTop = mIconStatic.getTop();
				int srcTop = mIconDynamic.getTop();

				animateIcon( dstTop - srcTop );
			}
		});
	}


	void animateIcon(int toTopPos)
	{
		ViewCompat.animate( mIconDynamic )
				.translationY( toTopPos )
				.setDuration( 500 )
				//.setInterpolator( new DecelerateInterpolator() )
				.setListener( new ViewPropertyAnimatorListener()
				{
					@Override
					public void onAnimationStart( View view ) {
						mAnimIconDone = false;

						animateMainView( 0 );
					}

					@Override
					public void onAnimationEnd( View view ) {
						mAnimIconDone = true;
						view.setAlpha( 1f );
						continueToGooglePlayServicesCheck();
					}

					@Override
					public void onAnimationCancel( View view ) {}
				} )
				.setStartDelay( 125 );
	}

	void animateMainView(int initDelay )
	{
		ViewCompat.animate( mSplashMainView )
				.alpha( 1f )
				.setDuration( 250 )
				.setListener( new ViewPropertyAnimatorListener() {
					@Override
					public void onAnimationStart( View view ) {
						mAnimMainViewDone = false;
					}

					@Override
					public void onAnimationEnd( View view ) {
						mAnimMainViewDone = true;
						view.setAlpha( 1f );
						continueToGooglePlayServicesCheck();
					}

					@Override
					public void onAnimationCancel( View view ) {}
				})
				.setStartDelay( initDelay );
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		if( BuildConfig.DEBUG )
		{
			long tDiff = (System.nanoTime() - mOnCreateTime);
			float tDiffF = tDiff * (1 / 1000000f);
			float tAppDiffF = mAppCreateTDiff * (1 / 1000000f);
			dbglog.Log( TAG, String.format( Locale.US, "App to Splash: %.2f, Splash create to resume: %.2f", tAppDiffF, tDiffF ) );
		}
	}

	void continueToGooglePlayServicesCheck()
	{
		if( mAnimIconDone && mAnimMainViewDone ) {
			if( MapsIndoorsUtils.CheckGooglePlayServices( this ) ) {
				if( BuildConfig.DEBUG ) {
					mStartTime = System.currentTimeMillis();
				}

				prepareNextActivity();
			}
		}
	}

	private void prepareNextActivity()
	{
		final long timeToWait = getSplashScreenDelay();

		final Class clazz = MapsIndoorsActivity.class;

		Handler handler = new Handler( Looper.getMainLooper() );
		handler.postDelayed( () -> startNextActivity( clazz ), timeToWait );
	}

	void startNextActivity( Class clazz ) {
		Intent intent = new Intent( SplashScreenActivity.this, clazz );

		startActivity( intent );
		overridePendingTransition( 0, 0 );
		finish();
	}

	private long getSplashScreenDelay() {
		long timeToWait = 0;
		long difference = System.currentTimeMillis() - mStartTime;

		if( difference < MINIMUM_SCREEN_DELAY ) {
			timeToWait = MINIMUM_SCREEN_DELAY - difference;
		}

		return timeToWait;
	}
}
