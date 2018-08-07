package com.mapsindoors.stdapp.broadcastReceivers;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Environment;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.mapsindoors.mapssdk.OnStateChangedListener;
import com.mapsindoors.stdapp.helpers.MapsIndoorsUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * NetworkChangeReceiver
 * MapsIndoorsDemo
 * <p>
 * Created by Amine on 17/07/2017.
 * Copyright © 2017 MapsPeople A/S. All rights reserved.
 */
public class NetworkChangeReceiver extends BroadcastReceiver
{

	public static final String TAG = NetworkChangeReceiver.class.getSimpleName();
	static List<OnStateChangedListener> mStateChangedlistenersList;
	boolean mPreviousState;
	boolean mFirstTime;

	Context mActivityContext;

	static String LOG_FILE_NAME = "log";
	static String LOG_DIR_NAME = "MISTDApp";

	public NetworkChangeReceiver( Context activityContext ) {
		mStateChangedlistenersList = new ArrayList<>();

		mActivityContext = activityContext;

		mPreviousState = MapsIndoorsUtils.isNetworkReachable( mActivityContext );

		mFirstTime = true;

	}

	@Override
	public void onReceive( final Context context, final Intent intent ) {


		if( intent.getAction().matches( ConnectivityManager.CONNECTIVITY_ACTION ) ) {
			boolean networkState = MapsIndoorsUtils.isNetworkReachable( mActivityContext );

			// only when the state changes
			if( mFirstTime ) {
				reportStateToListeners( networkState );


				mFirstTime = false;
			} else if( networkState != mPreviousState ) {


				reportStateToListeners( networkState );
				mPreviousState = networkState;
			}
		}

	}

	public void addOnStateChangedListener( OnStateChangedListener onStateChangedListener ) {
		mStateChangedlistenersList.remove( onStateChangedListener );
		mStateChangedlistenersList.add( onStateChangedListener );
	}

	void reportStateToListeners( boolean state ) {
		if( !mStateChangedlistenersList.isEmpty() ) {
			for( OnStateChangedListener stChListener : mStateChangedlistenersList ) {
				if( stChListener != null) {
					stChListener.onStateChanged( state );
				}
			}
		}
	}


	static void writeLogFile( final String content, Context context ) {

		Dexter.withActivity((Activity) context)
				.withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE )
				.withListener(new PermissionListener() {
					@Override public void onPermissionGranted(PermissionGrantedResponse response) {writeInFile(content);}
					@Override public void onPermissionDenied(PermissionDeniedResponse response) {/* ... */}
					@Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {}
				})
				.check();

	}

	static void writeInFile(String content){

		File file = new File (createDir(), LOG_FILE_NAME);
		int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		int minutes = Calendar.getInstance().get(Calendar.MINUTE);
		int seconds = Calendar.getInstance().get(Calendar.SECOND);

		PrintWriter out = null;
		if ( file.exists() && !file.isDirectory() ) {
			try {
				out = new PrintWriter(new FileOutputStream(new File(createDir(), LOG_FILE_NAME), true));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		else {
			try {
				out = new PrintWriter(file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		out.append(hour + ":" + minutes+":"+ seconds + "    "+content);
		out.close();

	}
	static File createDir(){

		String root = Environment.getExternalStorageDirectory().toString();
		File myDir = new File(root +"/" + LOG_DIR_NAME
		);
		myDir.mkdirs();

		return myDir;
	}
}