package com.mapsindoors.stdapp.positionprovider;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.RemoteException;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.mapsindoors.mapssdk.BeaconProvider;
import com.mapsindoors.mapssdk.BeaconRSSICache;
import com.mapsindoors.mapssdk.BeaconRSSICacheMgr;
import com.mapsindoors.mapssdk.MPBeaconProvider;
import com.mapsindoors.mapssdk.MPPositionResult;
import com.mapsindoors.mapssdk.OnBeaconServiceConnectListener;
import com.mapsindoors.mapssdk.OnPositionUpdateListener;
import com.mapsindoors.mapssdk.OnStateChangedListener;
import com.mapsindoors.mapssdk.PermissionsAndPSListener;
import com.mapsindoors.mapssdk.PointXY;
import com.mapsindoors.mapssdk.PositionCalculator;
import com.mapsindoors.mapssdk.PositionProvider;
import com.mapsindoors.mapssdk.PositionResult;
import com.mapsindoors.mapssdk.dbglog;
import com.mapsindoors.mapssdk.models.MPBeacon;
import com.mapsindoors.mapssdk.models.Point;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BuildConfig;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Created by JSM on 26/02/15.
 *
 * @see <a href="http://altbeacon.org/examples>Alt beacon examples</a>
 */
public class BeaconPositionProvider
		implements
			PositionProvider,
			OnBeaconServiceConnectListener
{
	public static final String TAG = BeaconPositionProvider.class.getSimpleName();


	/** */
	private static final String[] REQUIRED_PERMISSIONS = {};//{"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"};


	private boolean started = false;
	private String         mUUID;
	private BeaconProvider beaconProvider;
	List<Point>  posList;
	PositionResult mLatestPosition;
	private String providerId;
	List< OnPositionUpdateListener > mPositionUpdateListeners;

	private BeaconManager        beaconManager;
	private String               solutionId;
	private BeaconRSSICacheMgr   beaconRSSICacheMgr;
	private Collection< Beacon > beaconCollection;
	private GoogleMap            mMap;
	private boolean              isRSSICircleVisible;
	private boolean isBeaconContextBound = false;
	PositionCalculator positionCalculator;
	boolean isUsingTrilateration = false;


	Context mContext = null;
	final BeaconPositionProvider mThisPositionProvider;

	private static List< String > layoutList = new ArrayList<>();

	public BeaconPositionProvider( Context ctx, String UUID )
	{
		if( mContext != null )
		{
			this.beaconManager = BeaconManager.getInstanceForApplication( mContext );
			try
			{
				beaconManager.unbind( (BeaconConsumer) mContext );
				isBeaconContextBound = false;
			} catch( Exception e )
			{
				dbglog.Log( TAG, "Failed to unbind the beacon manager" );
			}
		}

		//BeaconManager.setUseTrackingCache(true);
		BeaconManager.setAndroidLScanningDisabled( true );

		mContext = ctx;
		mUUID = UUID;

		mThisPositionProvider = this;
		mPositionUpdateListeners = null;

		this.beaconProvider = new MPBeaconProvider();
		this.positionCalculator = new PositionCalculator();

		setBeaconParserLayout( "m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24" );

		beaconRSSICacheMgr = new BeaconRSSICacheMgr();
	}


	//region IMPLEMENTS PositionProvider
	@NonNull
	@Override
	public String[] getRequiredPermissions()
	{
		return REQUIRED_PERMISSIONS;
	}

	@Override
	public void startPositioning( String arg )
	{
		this.solutionId = arg;
	}

	@Override
	public void stopPositioning( String arg )
	{
	}

	@Override
	public boolean isRunning()
	{
		return false;
	}

	@Override
	public void addOnPositionUpdateListener( OnPositionUpdateListener listener )
	{
		if( listener != null )
		{
			if( mPositionUpdateListeners == null )
			{
				mPositionUpdateListeners = new ArrayList<>();
			}

			mPositionUpdateListeners.remove( listener );
			mPositionUpdateListeners.add( listener );
		}
	}

	@Override
	public void removeOnPositionUpdateListener( OnPositionUpdateListener listener )
	{
		if( listener != null )
		{
			if( mPositionUpdateListeners != null )
			{
				mPositionUpdateListeners.remove( listener );
				if( mPositionUpdateListeners.isEmpty() )
				{
					mPositionUpdateListeners = null;
				}
			}
		}
	}

	@Override
	public void setProviderId( String id )
	{
		providerId = id;
	}

	@Override
	public String getProviderId()
	{
		return providerId;
	}

	@Override
	public PositionResult getLatestPosition()
	{
		return mLatestPosition;
	}

	@Override
	public void startPositioningAfter( @IntRange(from = 0, to = Integer.MAX_VALUE) int millis, String arg )
	{
	}

	@Override
	public void terminate()
	{
		if( mPositionUpdateListeners != null )
		{
			mPositionUpdateListeners.clear();
			mPositionUpdateListeners = null;
		}
	}
	//endregion


	//region IMPLEMENTS OnBeaconServiceConnectListener
	@Override
	public void onBeaconServiceConnect()
	{
		if( mRangeNotifier != null )
		{
			beaconManager.removeRangeNotifier( mRangeNotifier );
			beaconManager.addRangeNotifier( mRangeNotifier );
		}

		try
		{
			if( !started && beaconManager.checkAvailability() )
			{
				started = true;
				final String UUID = this.mUUID;

//				beaconManager.startRangingBeaconsInRegion(new Region(UUID, null, null, null));
//				beaconManager.startMonitoringBeaconsInRegion(new Region(UUID, null, null, null));
//				beaconManager.setForegroundScanPeriod(1000);
//				beaconManager.setForegroundBetweenScanPeriod(0);
//				beaconManager.setBackgroundMode(false);
//				beaconManager.updateScanPeriods();

				new Thread( () ->
				{
					//Set the beacon manager to update faster after a short pause (to allow the app to start up properly)
					sleep( 5000 );

					try
					{
						beaconManager.startRangingBeaconsInRegion( new Region( UUID, null, null, null ) );
						beaconManager.setForegroundScanPeriod( 500 );
						beaconManager.setForegroundBetweenScanPeriod( 0 );
						beaconManager.setBackgroundMode( false );
						beaconManager.updateScanPeriods();
					} catch( RemoteException e )
					{
						dbglog.Log( TAG, e.getMessage() );
					}
				} ).start();
			}
		} catch( Exception e )
		{
			dbglog.Log( TAG, e.getMessage() );
		}
	}
	//endregion


	public void unbindBeaconManager()
	{
		if( beaconManager.isBound( (BeaconConsumer) mContext ) )
		{
			beaconManager.unbind( (BeaconConsumer) mContext );
		}
	}

	public void setBeaconParserLayout( String layout )
	{
		beaconManager = BeaconManager.getInstanceForApplication( mContext );
		if( !layoutList.contains( layout ) )
		{
			layoutList.add( layout );
			beaconManager.getBeaconParsers().add( new BeaconParser().setBeaconLayout( layout ) );
		}
		if( !isBeaconContextBound )
		{
			try
			{
				if( !beaconManager.isBound( (BeaconConsumer) mContext ) )
				{
					beaconManager.bind( (BeaconConsumer) mContext );
				}
				isBeaconContextBound = true;
			} catch( Exception e )
			{
				dbglog.Log( TAG, "Failed to bind the beacon manager" );
			}
		}
	}

	public void useTrilateration( boolean isActive )
	{
		isUsingTrilateration = isActive;
	}


	private RangeNotifier mRangeNotifier = new RangeNotifier()
	{
		@Override
		public void didRangeBeaconsInRegion( Collection< Beacon > beacons, Region region )
		{
			if( beacons.size() > 0 )
			{
				beaconCollection = new ArrayList<>( beacons );
				collectBeaconsFromAPI( new ArrayList<>( beaconCollection ) );
			}
		}
	};

	void collectBeaconsFromAPI( final List< Beacon > beacons )
	{
		List< String > beaconIds = new ArrayList<>();
		for( Beacon b : beacons )
		{
			beaconIds.add( b.getId1() + "-" + b.getId2() + "-" + b.getId3() );
		}

		beaconProvider.setOnBeaconsReadyListener( mpBeacons ->
		{
			boolean isChanged = false;
			List< PointXY > measurements = new ArrayList<>();
			for( MPBeacon mpBeacon : mpBeacons )
			{
				for( Beacon beacon : beacons )
				{
					if( beaconEquals( beacon, mpBeacon ) )
					{
						isChanged = true;
						beaconRSSICacheMgr.add( mpBeacon, beacon.getRssi() );
					}
				}
			}
			if( !isChanged )
			{
				return;
			}
			//Get a list of beacons we have heard from within 5 seconds
			ArrayList< BeaconRSSICache > beaconList = beaconRSSICacheMgr.getBeaconList( 5000 );
			if( beaconList.size() == 0 )
			{
				return;
			}
			for( BeaconRSSICache c : beaconList )
			{
				double myDist = positionCalculator.convertRSSItoMeter( c.getAvgVal(), c.beacon.getMaxTxPower() );
				measurements.add( new PointXY( c.beacon.getPoint(), myDist ) );
			}

/*
measurements.add( new PointXY(new Point( 57.085958, 9.9569259), 3.9));
measurements.add( new PointXY(new Point(57.086000, 9.956929), 0.8));
measurements.add( new PointXY(new Point(57.086006, 9.957032), 1.23));
measurements.add( new PointXY(new Point(57.085955, 9.957026), 1.0));
*/
			latestMeasurements = measurements;

			updateRSSICircles();

			//Got a number of measurements. Calculate where the origin should be.
			Point newPos = positionCalculator.calcLatLngPos( measurements, isUsingTrilateration );
			if( Double.isNaN( newPos.getLat() ) || Double.isNaN( newPos.getLat() ) )
			{
				//Unable to calculate a sane position from the given data
				return;
			}
			if( posList == null || !isUsingTrilateration )
			{
				MPPositionResult posResult = new MPPositionResult( newPos, 0, Double.MIN_VALUE );
				posList = new ArrayList<>();
				posList.add( posResult.getPoint() );
				mLatestPosition = posResult;

			}
			else
			{
				if( !isUsingTrilateration )
				{
					//Reuse the poslist, but only keep one entry
					posList.clear();

					MPPositionResult posResult = new MPPositionResult( newPos, 0, Double.MIN_VALUE );
					posList.add( posResult.getPoint() );
					mLatestPosition = posResult;

				}
				else
				{

					double distanceFromPrevious = newPos.distanceTo( mLatestPosition.getPoint() );

					if( distanceFromPrevious > 50 && posList.size() > 1 )
					{
						if( dbglog.isDebugMode() )
						{
							dbglog.Log( TAG, "Reading not used: Last measured position is far away from previous pos: " + distanceFromPrevious + " meters" );
						}
					}
					else if( newPos.getZ() > 50 )
					{
						if( dbglog.isDebugMode() )
						{
							dbglog.Log( TAG, "Reading not used: High error distance: " + newPos.getZ() + " meters" );
						}
					}
					else
					{
						posList.add( newPos );
					}
				}
			}

			//
			// TODO: 11/13/2017 CHECK THE CODE ABOVE AND BELOW, AS THE mLatestPosition REPORTED TO THE LISTENERS COULD BE WRONG
			//

			if( mPositionUpdateListeners != null )
			{
				if( posList.size() > 1 )
				{
					posList.remove( 0 );
				}

				double avgLat, avgLon, avgErr;
				avgLat = avgLon = avgErr = 0;

				for( Point p : posList )
				{
					avgLat += p.getLat();
					avgLon += p.getLng();
					avgErr += p.getZ();
				}

				double posListSizeInv = 1.0 / posList.size();
				avgLat *= posListSizeInv;
				avgLon *= posListSizeInv;
				avgErr *= posListSizeInv;


				mLatestPosition = new MPPositionResult( new Point( avgLat, avgLon, 0 ), avgErr, Double.MIN_VALUE );

				// Report an update if the user has moved?
//				if( mLatestPosition != null ) {
//					double dist = mLatestPosition.getPoint().distanceTo( newLocation.getPoint() );
//					if( dist <= 1 ) {
//						//	return;
//					}
//				}

//				mLatestPosition = newLocation;
				mLatestPosition.setProvider( mThisPositionProvider );

				if( mPositionUpdateListeners != null )
				{
					for( OnPositionUpdateListener listener : mPositionUpdateListeners )
					{
						if( listener != null )
						{
							listener.onPositionUpdate( mLatestPosition );
						}
					}
				}


			}
		} );

		beaconProvider.queryBeacons( solutionId, beaconIds.toArray( new String[beaconIds.size()] ) );
	}

	public void addBeaconsToMap( GoogleMap map, boolean showRSSICircle )
	{
		if( BuildConfig.DEBUG )
		{
			mMap = map;
			isRSSICircleVisible = showRSSICircle;
		}
	}

	public List< MPBeacon > getBeaconCollection()
	{
		List< BeaconRSSICache > cache = beaconRSSICacheMgr.getBeaconList( 5000 );
		List< MPBeacon > result = new ArrayList<>( cache.size() );
		for( BeaconRSSICache bc : cache )
		{
			result.add( bc.beacon );
		}
		return result;
	}

	List< PointXY > latestMeasurements = new ArrayList<>();
	private List< Circle > circles = new ArrayList<>();


	// TODO: 11/13/2017 CHECK WHAT THIS IS AND WHY THIS IS HERE (DEVELOPER CODE?)
	void updateRSSICircles()
	{
		if( BuildConfig.DEBUG )
		{
			return;
		}

		if( isRSSICircleVisible && mMap != null )
		{
			Handler mainHandler = new Handler( mContext.getMainLooper() );
			final Runnable myRunnable = () ->
			{
				if( circles == null )
				{
					circles = new ArrayList<>();
					for( int i = 0; i < 16; i++ )
					{
						CircleOptions options = new CircleOptions().center( new LatLng( 0, 0 ) ).radius( 1 ).fillColor( Color.argb( 128, 128, 128, 255 ) ).zIndex( 99 ).strokeWidth( 0 );
						Circle newCircle = mMap.addCircle( options );
						circles.add( newCircle );
					}
				}

				for( int i = 0, circlesCount = circles.size(); i < circlesCount; i++ )
				{
					Circle c = circles.get( i );
					if( i < latestMeasurements.size() )
					{
						PointXY pos = latestMeasurements.get( i );
						c.setCenter( pos.latlng.getLatLng() );
						c.setRadius( pos.distance );
						c.setVisible( true );
					}
					else
					{
						if( c.isVisible() )
						{
							c.setVisible( false );
						}
					}
				}
			};
			mainHandler.post( myRunnable );
		}
	}

	public boolean beaconEquals( Beacon beacon, MPBeacon mpBeacon )
	{
		return (beacon.getId1() + "-" + beacon.getId2() + "-" + beacon.getId3()).toLowerCase( Locale.ROOT ).equals( mpBeacon.getId().toLowerCase( Locale.ROOT ) );
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


	public void sleep( int milliseconds )
	{
		try
		{
			synchronized (this)
			{
				this.wait(milliseconds);
			}
		}
		catch ( InterruptedException ie )
		{
			if( dbglog.isDebugMode() ) {
				dbglog.Log( TAG, "current thread has been interrupted." );
			}
		}
	}
}
