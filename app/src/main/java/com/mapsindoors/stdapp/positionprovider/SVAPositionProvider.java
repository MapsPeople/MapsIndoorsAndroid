package com.mapsindoors.stdapp.positionprovider;


public class SVAPositionProvider //implements PositionProvider
{
//	private static final String TAG = SVAPositionProvider.class.getSimpleName();
//
//
//	private List<OnPositionUpdateListener > listeners;
//	private String providerId;
//	private PositionResult latestPosition;
//	private boolean isRunning;
//	private Context context;
//	private Thread loginProcess;
//	private String token;
//	private static String SVA_SERVICE_LOGIN_URL = "https://182.138.104.35:8001/login?userid={userid}&passwd={passwd}";
//
//	public SVAPositionProvider(Context context) {
//		super();
//		this.context = context;
//		// Define a listener that responds to location updates
//		isRunning = false;
//
//		//First mail:
//		//IP address: 182.138.104.35
//		//Port: 9001 used to get token
//		//4703 used to get data
//
//		//Second mail:
//		//Getting Token:  182.138.104.35:8001
//		//Getting Data:  182.138.104.35:8703
//
//		//From Bruce:
//		//App id/password: app5/User@123456
//		//The test secenario is our cafeteria, the attachment is the map of the cafeteria, you can see the local coordinates of some point,
//		//The integration test engineer is my colleague YanWeizi(yanweizi@huawei.com), he will help you to complete the test, if you have any question you can send mail to him,
//		login("app5", "User@123456");
//	}
//
//	private void login(String userId, String password )
//	{
//		//Calling the SVA Login endpoint to get a token.
//		URITemplate t = new URITemplate(SVA_SERVICE_LOGIN_URL);
//		HashMap<String, String> param = new HashMap<>();
//		param.put("userid", userId);
//		param.put("passwd", password);
//		final String url = t.generate(param);
//		loginProcess = new Thread(new Runnable() {
//			@Override
//			public void run() {
//				String res = readUrl(url);
//				if ( res != null && res.length() > 0 )
//				{
//					//TODO: Read token from the response if possible.
//					//token = ...
//				}
//			}
//		});
//		loginProcess.start();
//	}
//
//	@Override
//	public void startPositioning(String arg) {
//		if (!isRunning) {
//			isRunning = true;
//			//TODO: Here we need to start getting position updates from SVA.
//			if (listeners != null) {
//				for (OnPositionUpdateListener listener : listeners) {
//					if( listener != null) {
//						listener.onPositioningStarted( this );
//					}
//				}
//			}
//
//		}
//	}
//
//	//Example method to be called whenever there is a new position available
//	private void updatePosition(Point newLatLng, double probability, double heading )
//	{
//		MPPositionResult newPosition = new MPPositionResult(newLatLng, probability, heading, 0);
//		for (OnPositionUpdateListener listener : listeners) {
//			if( listener != null) {
//				listener.onPositionUpdate( newPosition );
//			}
//		}
//	}
//
//
//	@Override
//	public void stopPositioning(String arg) {
//		if (isRunning) {
//			isRunning = false;
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
//	private String readUrl( String string ) {
//		HttpURLConnection urlConnection = null;
//		String result = "";
//		int retryCount = 3;
//		while( retryCount > 0 ) {
//			try {
//				URL url = new URL( string );
//				urlConnection = (HttpURLConnection) url.openConnection();
//				urlConnection.setReadTimeout( 10000 );
//				urlConnection.setConnectTimeout( 15000 );
//				urlConnection.setRequestMethod( "POST" );
//				urlConnection.setDoOutput( true );
//				result = readStream( urlConnection.getInputStream() );
//				retryCount = 0;
//			} catch( Exception e ) {
//				dbglog.Log( TAG, "Failed to read from: " + string );
//				dbglog.Log( TAG, "read: " + result.length() + " bytes: " + result );
//				try {
//					synchronized(this) {
//						this.wait( 300 );
//					}
//				} catch( InterruptedException ie ) {
//				}
//				retryCount--;
//			} finally {
//				if( urlConnection != null ) {
//					urlConnection.disconnect();
//				}
//			}
//		}
//		return result;
//	}
//
//	private String readStream(InputStream is) throws IOException
//	{
//		StringBuilder sb = new StringBuilder();
//		BufferedReader r = new BufferedReader(new InputStreamReader(is), 1024);
//		for (String line = r.readLine(); line != null; line = r.readLine()) {
//			sb.append(line);
//		}
//		is.close();
//		return sb.toString();
//	}
//
}
