package com.mapsindoors.stdapp.positionprovider.gpsPositionProvider;


public class GPSPositionProvider //extends Activity implements LocationListener, LocationSource, PositionProvider
{
//	private LocationManager locationManager;
//	private List<OnPositionUpdateListener > listeners;
//	private String providerId;
//	private PositionResult latestPosition;
//	private boolean isRunning;
//	private Context context;
//	private OnLocationChangedListener googleListener;
//
//
//	public GPSPositionProvider(Context context) {
//		super();
//		this.context = context;
//		// Acquire a reference to the system Location Manager
//		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
//
//		// Define a listener that responds to location updates
//
//		isRunning = false;
//	}
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		//locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER,3000, 10, this );
//	}
//
//	@SuppressLint("MissingPermission")
//	@Override
//	public void startPositioning(String arg) {
//		if (!isRunning) {
//			isRunning = true;
//
//			// TODO: 23-04-2017 LINT ERROR HERE FIX IT!!!
//			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 7, this);
//            onLocationChanged( locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) );
//			if (listeners != null) {
//				for (OnPositionUpdateListener listener : listeners) {
//					if( listener != null) {
//						listener.onPositioningStarted( this );
//					}
//				}
//			}
//		}
//	}
//
//	@Override
//	public void stopPositioning(String arg) {
//		if (isRunning) {
//			isRunning = false;
//			locationManager.removeUpdates(this);
//		}
//	}
//
//	@Override
//	public void addOnPositionUpdateListener(OnPositionUpdateListener listener) {
//		if (this.listeners == null) {
//			this.listeners = new ArrayList<>();
//		}
//		this.listeners.add(listener);
//	}
//
//	@Override
//	public void removeOnPositionUpdateListener( OnPositionUpdateListener listener ) {
//		if( this.listeners != null ) {
//			this.listeners.remove( listener );
//		}
//	}
//
//	@Override
//	public void setProviderId(String id) {
//		providerId = id;
//	}
//
//	@Override
//	public PositionResult getLatestPosition() {
//		return latestPosition;
//	}
//
//	@Override
//	public boolean isRunning() {
//		return isRunning;
//	}
//
//	@Override
//	public String getProviderId() {
//		return providerId;
//	}
//
//
//
//	@Override
//	public void onStatusChanged(String provider, int status, Bundle extras) {
//
//	}
//	@Override
//    public void onProviderEnabled(String provider) {
//
//	}
//
//    @Override
//    public void onProviderDisabled(String provider) {
//
//    }
//
//	@Override
//	public void onLocationChanged(Location location) {
//		if( location != null && isRunning() ) {
//			latestPosition = new MPPositionResult( new Point( location.getLatitude(), location.getLongitude() ), 0, location.getBearing(), location.getTime() );
//			latestPosition.setProvider( this );
//
//			for( OnPositionUpdateListener listener : listeners ) {
//				if( listener != null) {
//					listener.onPositionUpdate( latestPosition );
//				}
//			}
//
//			if( googleListener != null ) {
//				googleListener.onLocationChanged( location );
//			}
//		}
//	}
//
//	@Override
//	public void startPositioningAfter(int millis, final String arg) {
//		Timer restartTimer = new Timer();
//		restartTimer.schedule(new TimerTask() {
//			@Override
//			public void run() {
//				startPositioning(arg);
//			}
//		}, millis);
//	}
//
//	@Override
//	public void activate(OnLocationChangedListener arg0) {
//		this.googleListener = arg0;
//		startPositioning(null);
//	}
//
//	@Override
//	public void deactivate() {
//		//We don't want deactivation by google maps
//	}
//
}
