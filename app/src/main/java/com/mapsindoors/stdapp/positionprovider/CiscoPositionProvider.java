package com.mapsindoors.stdapp.positionprovider;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mapsindoors.mapssdk.MPPositionResult;
import com.mapsindoors.mapssdk.OnPositionUpdateListener;
import com.mapsindoors.mapssdk.OnStateChangedListener;
import com.mapsindoors.mapssdk.PermissionsAndPSListener;
import com.mapsindoors.mapssdk.PositionProvider;
import com.mapsindoors.mapssdk.PositionResult;
import com.mapsindoors.mapssdk.UrlLoader;
import com.mapsindoors.mapssdk.dbglog;
import com.mapsindoors.stdapp.BuildConfig;
import com.mapsindoors.stdapp.models.CiscoPositionResponse;

import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * CiscoPositionProvider
 * MapsIndoorsDemo
 * <p>
 * Created by Jose J Varó on 11/2/2017.
 * Copyright © 2017 MapsPeople A/S. All rights reserved.
 */
public class CiscoPositionProvider implements PositionProvider
{
	public static final String TAG = CiscoPositionProvider.class.getSimpleName();


	/** Interval for the poll timer */
	private static final long POLL_INTERVAL = 2000;

	/** How much the user needs to move before reporting a position update, in meters */
	private static final double UPDATE_DISTANCE_THRESHOLD = 1;

	/** */
	private static final String[] REQUIRED_PERMISSIONS = {"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"};


	private Context mContext;
	final String mCiscoProxyUrl;
	final CiscoPositionProvider mThisPositionProvider;

	List<OnPositionUpdateListener> mPositionUpdateListeners;
	boolean mIsRunning;
	boolean mIsWaitingForDelayedPositioningStart;
	private String mProviderId;
	PositionResult mLatestPosition;

	private GsonBuilder mGsonBuilder;
	private Type mModelType;

	private Timer mPollTimer;
	WifiManager mMainWifi;
	WifiReceiver mWifiReceiver;



	public CiscoPositionProvider( @NonNull Context context, @NonNull String ciscoProxyUrl ) {
		mContext = context;
		mCiscoProxyUrl = ciscoProxyUrl;
		mThisPositionProvider = this;

		mPositionUpdateListeners = null;
		mLatestPosition = null;
		mIsRunning = false;
		mIsWaitingForDelayedPositioningStart = true;
	}


	//region IMPLEMENTS PositionProvider
	@NonNull
	@Override
	public String[] getRequiredPermissions() {
		return REQUIRED_PERMISSIONS;
	}

	@Override
	public void startPositioning( @Nullable String arg ) {
		if( !mIsRunning )
		{
			if( mPollTimer != null ) {
				mPollTimer.cancel();
				mPollTimer.purge();
				mPollTimer = null;
			}

			Context ctx = mContext.getApplicationContext();
			if( ctx == null ) {
				sendFailure("startPositioning -> Context is null...");
				return;
			}

			mMainWifi = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);

			if( mMainWifi == null )
			{
				if( dbglog.isDebugMode() ) {
					dbglog.LogW( TAG, "startPositioning( " + arg + " ) -> WifiManager was null" );
				}
				return;
			}

			boolean wifiEnabled = mMainWifi.isWifiEnabled();

			mWifiReceiver = new WifiReceiver();
			mContext.registerReceiver( mWifiReceiver, new IntentFilter( WifiManager.SCAN_RESULTS_AVAILABLE_ACTION ) );

			mIsRunning = true;
			mIsWaitingForDelayedPositioningStart = false;

			mPollTimer = new Timer( "MICiscoPosTimer", false );
			mPollTimer.schedule(
					getTimerTask(),
					POLL_INTERVAL,
					POLL_INTERVAL
			);

			if ( mPositionUpdateListeners != null) {

				if( dbglog.isDebugMode() ) {
					dbglog.Log( TAG, "startPositioning( " + arg + " ) - Start" );
				}

				for (OnPositionUpdateListener listener : mPositionUpdateListeners ) {
					if( listener != null) {
						listener.onPositioningStarted( this );
					}
				}
			}
		}
	}

	@Override
	public void stopPositioning( @Nullable String arg ) {
		if( dbglog.isDebugMode() ) {
			dbglog.Log( TAG, "stopPositioning( " + arg + " ) - Start" );
		}

		if( mIsRunning ) {
			mIsRunning = false;
			mIsWaitingForDelayedPositioningStart = false;

			if( mPollTimer != null ) {
				mPollTimer.cancel();
				mPollTimer.purge();
				mPollTimer = null;
			}
			if( mQueryPositionTask != null ) {
				mQueryPositionTask.cancel();
				mQueryPositionTask = null;
			}

			if( mWifiReceiver != null ) {
				try {
					mContext.unregisterReceiver( mWifiReceiver );
				} catch( Exception e ) {
					if( dbglog.isDebugMode() ) {
						dbglog.Log( TAG, "stopPositioning( " + arg + " ) - Exception: " + e );
					}
				}
			}
		}
	}

	@Override
	public boolean isRunning() {
		return mIsRunning;
	}

	@Override
	public void addOnPositionUpdateListener( @Nullable OnPositionUpdateListener listener ) {
		if( listener != null ) {
			if( mPositionUpdateListeners == null ) {
				mPositionUpdateListeners = new ArrayList<>();
			}

			mPositionUpdateListeners.remove( listener );
			mPositionUpdateListeners.add( listener );
		}
	}

	@Override
	public void removeOnPositionUpdateListener( @Nullable OnPositionUpdateListener listener ) {
		if( listener != null ) {
			if( mPositionUpdateListeners != null ) {
				mPositionUpdateListeners.remove( listener );
				if( mPositionUpdateListeners.isEmpty() ) {
					mPositionUpdateListeners = null;
				}
			}
		}
	}

	@Override
	public void setProviderId( @Nullable String id ) {
		mProviderId = id;
	}

	@Override
	@Nullable
	public String getProviderId() {
		return mProviderId;
	}

	@Nullable
	@Override
	public PositionResult getLatestPosition() {
		return null;
	}

	@Override
	public void startPositioningAfter( @IntRange(from = 0, to = Integer.MAX_VALUE) int delayInMs, @Nullable String arg ) {
		if( !mIsWaitingForDelayedPositioningStart )
		{
			mIsWaitingForDelayedPositioningStart = true;

			Timer restartTimer = new Timer();
			restartTimer.schedule( new TimerTask() {
				@Override
				public void run()
				{
					startPositioning( arg );
				}
			}, delayInMs );
		}
	}

	@Override
	public void terminate()
	{
		if( mPositionUpdateListeners != null ) {
			mPositionUpdateListeners.clear();
			mPositionUpdateListeners = null;
		}

		mContext = null;
	}
	//endregion


	void sendFailure( @Nullable String reason )
	{
		if( reason != null ) {
			if( dbglog.isDebugMode() ) {
				dbglog.LogW( TAG, reason );
			}
		}

		if( mPositionUpdateListeners != null ) {
			for( OnPositionUpdateListener ls : mPositionUpdateListeners ) {
				if( ls != null ) {
					ls.onPositionFailed( mThisPositionProvider );
				}
			}
		}
	}

	/**
	 *
	 * @return
	 */
	String getMacAddress() {

		String macAddress = "02:00:00:00:00:00";

		if( android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M )
		{
			try {
				// ================================================================================
				// How to retrieve the device's mac address in Android 6+
				// Credits: http://robinhenniges.com/en/android6-get-mac-address-programmatically
				// ================================================================================
				List< NetworkInterface > all = Collections.list( NetworkInterface.getNetworkInterfaces() );
				for( NetworkInterface nif : all ) {
					if( !nif.getName().equalsIgnoreCase( "wlan0" ) ) {
						continue;
					}

					Enumeration<InetAddress > addresses = nif.getInetAddresses();

					byte[] macBytes = nif.getHardwareAddress();
					if( macBytes == null ) {
						if( dbglog.isDebugMode() ) {
							dbglog.Log( TAG, "getMacAddress - macBytes == null" );
						}
						break;
					}

					StringBuilder sb = new StringBuilder();
					for( byte b : macBytes ) {
						sb.append( Integer.toHexString( b & 0xFF ) ).append( ':' );
					}

					if( sb.length() > 0 ) {
						sb.deleteCharAt( sb.length() - 1 );
					}

					macAddress = sb.toString();
					break;
				}
			} catch (Exception e) {
				if( dbglog.isDebugMode() ) {
					dbglog.Log( TAG, "getMacAddress - Exception: " + e );
				}
			}
		}
		else
		{
			WifiManager wifiManager = (WifiManager) mContext.getApplicationContext().getSystemService( Context.WIFI_SERVICE );
			macAddress =  (wifiManager != null) ? wifiManager.getConnectionInfo().getMacAddress() : macAddress;
		}

		return macAddress;
	}


	int getIPV4Address()
	{
		int ipV4Address = 0;

		try {
			// ================================================================================
			// How to retrieve the device's mac address in Android 6+
			// Credits: http://robinhenniges.com/en/android6-get-mac-address-programmatically
			// ================================================================================
			List< NetworkInterface > all = Collections.list( NetworkInterface.getNetworkInterfaces() );
			for( NetworkInterface nif : all ) {

				// At least, Genimotion will only expose "eth0", so it will not work here ...
				if( !nif.getName().equalsIgnoreCase( "wlan0" ) ) {
					continue;
				}

				Enumeration<InetAddress> addresses = nif.getInetAddresses();
				while(addresses.hasMoreElements()) {
					InetAddress address = addresses.nextElement();

					if( address instanceof Inet6Address ) {
						continue;
					}

					byte[] ipBytes = address.getAddress();

					ipV4Address = 0;
					ipV4Address |= ((ipBytes[0] & 0xFF) << 24);
					ipV4Address |= ((ipBytes[1] & 0xFF) << 16);
					ipV4Address |= ((ipBytes[2] & 0xFF) << 8);
					ipV4Address |= ((ipBytes[3] & 0xFF) << 0);

					if( dbglog.isDebugMode() ) {
						String strAddress = address.getHostAddress();
						dbglog.Log( TAG, String.format("getIPV4Address -> %s / %08X", strAddress, ipV4Address ));
					}
				}
				break;
			}
		} catch (Exception e) {
			if( dbglog.isDebugMode() ) {
				dbglog.Log( TAG, "getIPV4Address - Exception: " + e );
			}
		}

		return ipV4Address;
	}

	@Nullable
	CiscoPositionResponse parseResult( @Nullable String data ) {
		if( TextUtils.isEmpty( data ) )
		{
			return null;
		}

		if( mGsonBuilder == null ) {
			mModelType = new TypeToken< CiscoPositionResponse >(){}.getType();
			mGsonBuilder = new GsonBuilder();
		}

		return mGsonBuilder.create().fromJson( data, mModelType );
	}

	TimerTask mQueryPositionTask;

	TimerTask getTimerTask() {

		boolean recreateTask = true;
		if( mQueryPositionTask != null ) {
			recreateTask = !mQueryPositionTask.cancel();
		}

		if( recreateTask )
		{
			mQueryPositionTask = new TimerTask() {
				@Override
				public void run() {
					if( mIsRunning ) {
						try {

							// Probe network
							if( mMainWifi.isWifiEnabled() ) {
								mMainWifi.startScan();
							} else {
								if( dbglog.isDebugMode() ) {
									dbglog.Log( TAG, "QueryPositionTask -> Wifi not enabled - TP2" );
								}
								return;
							}

							int ipV4Address = getIPV4Address();

							if( ipV4Address != 0 ) {

								// We could check if the previous call is done and skip the call otherwise (?)
								mUrlLoader.readUrl( mCiscoProxyUrl + ipV4Address );

							} else {
								if( dbglog.isDebugMode() ) {
									dbglog.LogW( TAG, "QueryPositionTask -> No IPV4 address" );
								}
							}
						} catch( Exception e ) {
							//e.printStackTrace();
							//sendFailure( "startPositioning -> Exception:\n" + e );
							if( dbglog.isDebugMode() ) {
								dbglog.Log( TAG, "QueryPositionTask -> Exception:\n" + e );
							}
						}
					}
				}
			};
		}

		return mQueryPositionTask;
	}

	UrlLoader mUrlLoader = new UrlLoader() {
		@Override
		public void onResult( @Nullable String result, int status ) {

			CiscoPositionResponse resp = parseResult( result );

			if( (resp !=null) && (resp.getStatus() == HttpURLConnection.HTTP_OK) ) {
				MPPositionResult newLocation = resp.getResult();
				newLocation.getPoint().setZ( newLocation.getFloor() );

				// Report an update if the user has moved
				if( mLatestPosition != null )
				{
					final double dist = mLatestPosition.getPoint().distanceTo( newLocation.getPoint() );
					if( dist <= UPDATE_DISTANCE_THRESHOLD )
					{
						// Get the altitude too. Just imagine the lady/guy is using a lift/elevator/"spiral staircase"...
						final double altDiff = Math.abs( newLocation.getPoint().getZ() - mLatestPosition.getPoint().getZ() );
						if( altDiff <= UPDATE_DISTANCE_THRESHOLD )
						{
							if( BuildConfig.DEBUG ){}
							return;
						}
					}
				}

				mLatestPosition = newLocation;
				mLatestPosition.setProvider( mThisPositionProvider );

				if( mPositionUpdateListeners != null ) {
					for( OnPositionUpdateListener listener : mPositionUpdateListeners ) {
						if( listener != null ) {
							listener.onPositionUpdate( mLatestPosition );
						}
					}
				}
			} else {
				if( dbglog.isDebugMode() ) {
					dbglog.Log( TAG, "QueryPositionTask -> Server returned with status: " + ((resp != null) ? resp.getStatus() : status) );
				}
			}
		}
	};

	class WifiReceiver extends BroadcastReceiver
	{
		public void onReceive( Context c, Intent intent ) {

			String intentAction = intent.getAction();
			if( intentAction != null ) {
				switch( intentAction )
				{
					case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
						if( BuildConfig.DEBUG ) {
						}
						break;
					case ConnectivityManager.CONNECTIVITY_ACTION:
						if( BuildConfig.DEBUG ) {
						}
						break;
				}
			}
		}
	}



	@Override
	public void addOnstateChangedListener(OnStateChangedListener onStateChangedListener) {
	}

	@Override
	public void removeOnstateChangedListener(OnStateChangedListener onStateChangedListener) {
	}

	@Override
	public void checkPermissionsAndPSEnabled(PermissionsAndPSListener permissionAPSlist) {

	}

	public boolean isPSEnabled() {
		return false;
	}
}
