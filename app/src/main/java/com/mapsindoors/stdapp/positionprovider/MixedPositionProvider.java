package com.mapsindoors.stdapp.positionprovider;


/**
 * MixedPositionProviderV1
 * MapsIndoorsDemo
 * <p>
 * @author Martin Hansen
 * Modified by Jose J Varó on 11/3/2017.
 * Copyright © 2017 MapsPeople A/S. All rights reserved.
 */
//public class MixedPositionProvider implements PositionProvider
//{
//
//
//	public static final String TAG = MixedPositionProvider.class.getSimpleName();
//
//
//	private static final boolean DBG_SHOW_UPDATE_DEBUG_INFO = false;
//
//	/** Max time between update calls to consider that a provider is "alive" */
//	private static final long MAX_TIME_BETWEEN_UPDATES = 5000;
//
//	private static final double MIN_ACCURACY_OUTDOORS = 100;
//
//	private static final double MIN_ACCURACY_INDOORS = 50;
//
//	/** */
//	private InternalPosProvider[] mInternalPositionProviders;
//
//	List<OnPositionUpdateListener> mPositionUpdateListeners;
//
//	/** */
//	private InternalPosProvider mBestProvider;
//
//	/** */
//	private InternalPosProvider mPrevBestProvider;
//
//	private PositionResult mLatestPosition;
//
//	private Context mContext;
//	private List<OnStateChangedListener> mStateChangedlistenersList;
//
//
//	/**
//	 *
//	 * @param context An optional(?) context
//	 * @param positionProviders
//	 */
//	public MixedPositionProvider(@NonNull Context context, @Nullable List<PositionProvider> positionProviders )
//	{
//		mContext = context;
//
//		if( (positionProviders != null) && !positionProviders.isEmpty() ) {
//
//			if( BuildConfig.DEBUG )
//			{
//				if( positionProviders.size() > 1 )
//				{
//					Toast.makeText( context, "The aggregator ListenersList contains more than one listener without implementing the selection method", Toast.LENGTH_LONG ).show();
//				}
//			}
//
//			int ppCount = positionProviders.size();
//			mInternalPositionProviders = new InternalPosProvider[ ppCount ];
//
//			long timeNow = System.currentTimeMillis();
//
//			for( int i = 0; i < ppCount; i++ ) {
//				PositionProvider pp = positionProviders.get(i);
//
//				final InternalPosProvider ipp = new InternalPosProvider();
//				ipp.positionProvider = pp;
//				ipp.type = pp.getClass();
//				ipp.lastUpdateTimestamp = timeNow;
//
//				mInternalPositionProviders[ i ] = ipp;
//			}
//
//			mBestProvider = mInternalPositionProviders[ 0 ];
//
//		} else {
//			mInternalPositionProviders = new InternalPosProvider[0];
//		}
//
//
//	}
//
//	void subscribeToInternalPositionProvider()
//	{
//
//		// subscribing to the state changed listener
//		if( mStateChangedlistenersList != null )
//		{
//			for( OnStateChangedListener stateChangedListener : mStateChangedlistenersList )
//			{
//
//				if( mPrevBestProvider != null )
//				{
//					mBestProvider.positionProvider.removeOnstateChangedListener( stateChangedListener );
//				}
//
//				if( mBestProvider != null )
//				{
//					mBestProvider.positionProvider.addOnstateChangedListener( stateChangedListener );
//				}
//
//			}
//		}
//
//		// subscribing to the position changed listener
//		if( mPositionUpdateListeners != null )
//		{
//			for( OnPositionUpdateListener positionUpdateListener : mPositionUpdateListeners )
//			{
//
//				if( mPrevBestProvider != null )
//				{
//					mBestProvider.positionProvider.removeOnPositionUpdateListener( positionUpdateListener );
//				}
//
//				if( mBestProvider != null )
//				{
//					mBestProvider.positionProvider.addOnPositionUpdateListener( positionUpdateListener );
//				}
//			}
//		}
//	}
//
//
//	//region IMPLEMENTS PositionProvider
//
//	@NonNull
//	@Override
//	public String[] getRequiredPermissions()
//	{
//		HashSet<String> perms = new HashSet<>();
//
//		if( mInternalPositionProviders != null ) {
//			for( InternalPosProvider ipp : mInternalPositionProviders) {
//				perms.addAll( Arrays.asList( ipp.positionProvider.getRequiredPermissions() ) );
//			}
//		}
//
//		return perms.toArray( new String[0] );
//	}
//
//	@Override
//	public void startPositioning( String arg ) {
//		if( dbglog.isDebugMode() ) {
//			dbglog.Log( TAG, "startPositioning( " + arg + " ) - Start" );
//		}
//
//
//		subscribeToInternalPositionProvider();
//
//		mBestProvider.positionProvider.startPositioning(arg);
//
//		if( dbglog.isDebugMode() ) {
//			dbglog.Log( TAG, "startPositioning( " + arg + " ) - End" );
//		}
//	}
//
//	@Override
//	public void stopPositioning( String arg ) {
//		if( dbglog.isDebugMode() ) {
//			dbglog.Log( TAG, "stopPositioning( " + arg + " ) - Start" );
//		}
//
//
//		mBestProvider.positionProvider.stopPositioning(arg);
//
//
//		if( dbglog.isDebugMode() ) {
//			dbglog.Log( TAG, "stopPositioning( " + arg + " ) - End" );
//		}
//
//	}
//
//
//	@Override
//	public boolean isRunning() {
//
//		return mBestProvider.positionProvider.isRunning();
//	}
//
//	@Override
//	public void addOnPositionUpdateListener( @Nullable OnPositionUpdateListener listener ) {
//		if( listener != null ) {
//			if( mPositionUpdateListeners == null ) {
//				mPositionUpdateListeners = new ArrayList<>();
//			}
//
//			mPositionUpdateListeners.remove( listener );
//			mPositionUpdateListeners.add( listener );
//
//			mBestProvider.positionProvider.addOnPositionUpdateListener(listener);
//		}
//	}
//
//	@Override
//	public void removeOnPositionUpdateListener( @Nullable OnPositionUpdateListener listener )
//	{
//		if( listener != null )
//		{
//			if( mPositionUpdateListeners != null )
//			{
//				mPositionUpdateListeners.remove( listener );
//				if( mPositionUpdateListeners.isEmpty() )
//				{
//					mPositionUpdateListeners = null;
//				}
//			}
//		}
//	}
//
//	@Override
//	public void setProviderId( String id ) {}
//
//	public void addOnstateChangedListener( @Nullable OnStateChangedListener onStateChangedListener )
//	{
//		if( onStateChangedListener == null )
//		{
//			return;
//		}
//
//		if( mStateChangedlistenersList == null )
//		{
//			mStateChangedlistenersList = new ArrayList<>();
//		}
//
//		// adding it to the current list to keep track on all the listeners that we have
//		mStateChangedlistenersList.add( onStateChangedListener );
//
//		//subscribing it to the current working position provider
//		mBestProvider.positionProvider.addOnstateChangedListener( onStateChangedListener );
//	}
//
//	@Override
//	public void removeOnstateChangedListener( @Nullable OnStateChangedListener onStateChangedListener )
//	{
//		if( onStateChangedListener == null )
//		{
//			return;
//		}
//
//		mStateChangedlistenersList.remove( onStateChangedListener );
//
//		mBestProvider.positionProvider.removeOnstateChangedListener( onStateChangedListener );
//	}
//
//	@Override
//	public void checkPermissionsAndPSEnabled( PermissionsAndPSListener permissionAPSlistener )
//	{
//		mBestProvider.positionProvider.checkPermissionsAndPSEnabled( permissionAPSlistener );
//	}
//
//	@Override
//	@Nullable
//	public String getProviderId() {
//		return null;
//	}
//
//	@Nullable
//	@Override
//	public PositionResult getLatestPosition() {
//		return mBestProvider.positionProvider.getLatestPosition();
//	}
//
//	@Override
//	public void startPositioningAfter( @IntRange(from = 0, to = Integer.MAX_VALUE) int millis, @Nullable String arg ) {
//		new Timer().schedule( new TimerTask() {
//			@Override
//			public void run() {
//				startPositioning( arg );
//			}
//		}, millis );
//	}
//
//	@Override
//	public void terminate()
//	{
//		if( mInternalPositionProviders != null ) {
//
//			for( InternalPosProvider ipp : mInternalPositionProviders) {
//				if( ipp != null ) {
//					if( ipp.positionProvider.isRunning() ) {
//						ipp.positionProvider.stopPositioning( null );
//					}
//					ipp.positionProvider.terminate();
//				}
//			}
//
//			mInternalPositionProviders = null;
//			mBestProvider = null;
//			mPrevBestProvider = null;
//		}
//
//		if( mPositionUpdateListeners != null ) {
//			mPositionUpdateListeners.clear();
//			mPositionUpdateListeners = null;
//		}
//	}
//	//endregion
//
//
//	synchronized void onPositioningStarted( PositionProvider provider, int ppIndex ) {
//		// Report
//
//		if( mBestProvider !=null)
//		{
//		}
//
//		//if(false)
//		{
//			if( mPositionUpdateListeners != null ) {
//				for( OnPositionUpdateListener ls : mPositionUpdateListeners ) {
//					if( ls != null ) {
//						ls.onPositioningStarted( provider );
//					}
//				}
//			}
//		}
//	}
//
//	synchronized void onPositionUpdate( @NonNull PositionResult positionResult, @NonNull PositionProvider provider, @IntRange(from = 0, to = Integer.MAX_VALUE) int ppIndex ) {
//
//		if( DBG_SHOW_UPDATE_DEBUG_INFO && dbglog.isDebugMode() ) {
//			dbglog.Log( TAG, "sync onPositionUpdate()" );
//		}
//
//		//
//		long timeNow = System.currentTimeMillis();
//
//		//
//		Point newPos = positionResult.getPoint();
//		double newLat = newPos.getLat();
//		double newLng = newPos.getLng();
//
//
//		int mostUpdatedIPPIndex = -1;
//		int smallestUpdTimeDt = Integer.MAX_VALUE;
//
//		int bestAccuracyIPPIndex = Integer.MAX_VALUE;
//		double bestAccuracy = Double.MAX_VALUE;
//
//
//		int ppCount = mInternalPositionProviders.length;
//
//		for( int i = 0; i < ppCount; i++ )
//		{
//			boolean isCurrent = i == ppIndex;
//
//			// Gather some data
//			InternalPosProvider ipp = mInternalPositionProviders[i];
//			long dt = timeNow - ipp.lastUpdateTimestamp;
//			PositionResult ippPr = ipp.positionProvider.getLatestPosition();
//
//			if( ippPr == null ) {
//				continue;
//			}
//
//			double provAccuracy = ippPr.getProbability();
//			Point lastPos = ippPr.getPoint();
//
//			double dLat, dLng;
//			if( lastPos != null )
//			{
//				dLat = newLat - lastPos.getLat();
//				dLng = newLng - lastPos.getLng();
//			} else
//			{
//				dLat = dLng = 0.0;
//			}
//
//
//			// =====================================================
//			// Apply filters common to all providers
//			// =====================================================
//
//			if( dt > MAX_TIME_BETWEEN_UPDATES )
//			{
//				if( BuildConfig.DEBUG ){}
//				//return;
//			}
//
//			// =====================================================
//			// Apply filters depending on the provider's type
//			// =====================================================
//
//			//             OUTDOORS
//			if( provider instanceof GoogleAPIPositionProvider )
//			{
//				if( DBG_SHOW_UPDATE_DEBUG_INFO && dbglog.isDebugMode() )
//				{
//					dbglog.Log( TAG, "GoogleAPIPositionProvider\n");
//				}
//				// =====================================================
//				// Filter for when the provider is of THIS TYPE ONLY
//				// =====================================================
//
//				if( provAccuracy > MIN_ACCURACY_OUTDOORS )
//				{
//					if( BuildConfig.DEBUG ){}
//					//return;
//				}
//			}
//			else
//			{
//				//             INDOORS
//
//				// =====================================================
//				// Apply filters common for indoor providers only
//				// =====================================================
//
//				if( provAccuracy > MIN_ACCURACY_INDOORS )
//				{
//					if( BuildConfig.DEBUG ){}
//					//return;
//				}
//
//				// =====================================================
//				// Apply filters depending on the provider's type
//				// =====================================================
//				if( provider instanceof BeaconPositionProvider )
//				{
//					if( DBG_SHOW_UPDATE_DEBUG_INFO && dbglog.isDebugMode() )
//					{
//						dbglog.Log( TAG, "BeaconPositionProvider\n" );
//					}
//					// =====================================================
//					// Filter for when the provider is of THIS TYPE ONLY
//					// =====================================================
//
//
//				} else if( provider instanceof CiscoPositionProvider )
//				{
//					if( DBG_SHOW_UPDATE_DEBUG_INFO && dbglog.isDebugMode() )
//					{
//						dbglog.Log( TAG, "CiscoPositionProvider\n" );
//					}
//					// =====================================================
//					// Filter for when the provider is of THIS TYPE ONLY
//					// =====================================================
//
//
//				} else
//				{
//					if( DBG_SHOW_UPDATE_DEBUG_INFO && dbglog.isDebugMode() )
//					{
//						dbglog.Assert( false, "Unknown Positioning provider" );
//					}
//				}
//			}
//
//			// Find the most updated one
//			if( dt < smallestUpdTimeDt )
//			{
//				smallestUpdTimeDt = (int)dt;
//				mostUpdatedIPPIndex = i;
//			}
//
//			// Find the one with the best (raw) accuracy
//			if( provAccuracy < bestAccuracy )
//			{
//				bestAccuracy = provAccuracy;
//				bestAccuracyIPPIndex = i;
//			}
//
//			if( DBG_SHOW_UPDATE_DEBUG_INFO && dbglog.isDebugMode() )
//			{
//				dbglog.Log( TAG, " - acc: " + provAccuracy + "\n - dt: " + dt + "\n - dLat: " + dLat + "\n - dLng: " + dLng );
//			}
//		}
//
//		// Update the current provider's update time
//		InternalPosProvider ipp = mInternalPositionProviders[ ppIndex ];
//		ipp.lastUpdateTimestamp = timeNow;
//
//		// If none pass the checks, don't update!
//		if( mostUpdatedIPPIndex < 0 )
//		{
//			if( BuildConfig.DEBUG){}
//			return;
//		}
//
//		mPrevBestProvider = mBestProvider;
//
//		if( mostUpdatedIPPIndex == bestAccuracyIPPIndex )
//		{
//			mBestProvider = mInternalPositionProviders[ bestAccuracyIPPIndex ];
//			mLatestPosition = mBestProvider.positionProvider.getLatestPosition();
//
//		}else
//		{
//			// Short update time over accuracy?
//			// ???
//			mBestProvider = mInternalPositionProviders[ bestAccuracyIPPIndex ];
//			mLatestPosition = mBestProvider.positionProvider.getLatestPosition();
//
//			if( mBestProvider != null && mPrevBestProvider != null ) {
//				boolean isSameProvider = mBestProvider.getClass().equals( mPrevBestProvider.getClass() );
//				if( BuildConfig.DEBUG){}
//			}
//		}
//
//		// Report
//		if( mPositionUpdateListeners != null ) {
//			for( OnPositionUpdateListener ls : mPositionUpdateListeners ) {
//				if( ls != null ) {
//					ls.onPositionUpdate( mLatestPosition );
//				}
//			}
//		}
//	}
//
//	synchronized void onPositionFailed( PositionProvider provider, int ppIndex ) {
//
//			if( mPositionUpdateListeners != null ) {
//				for( OnPositionUpdateListener ls : mPositionUpdateListeners ) {
//					if( ls != null ) {
//						ls.onPositionFailed( provider );
//					}
//				}
//			}
//
//	}
//
//
//	class InternalPosProvider
//	{
//		PositionProvider positionProvider;
//		Type type;
//		long lastUpdateTimestamp;
//	}
//
//	public boolean isPSEnabled() {
//		return mBestProvider.positionProvider.isPSEnabled();
//	}
//}
