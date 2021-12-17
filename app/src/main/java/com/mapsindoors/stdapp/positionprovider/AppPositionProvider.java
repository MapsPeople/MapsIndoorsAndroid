package com.mapsindoors.stdapp.positionprovider;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mapsindoors.mapssdk.OnPositionUpdateListener;
import com.mapsindoors.mapssdk.OnStateChangedListener;
import com.mapsindoors.mapssdk.PermissionsAndPSListener;
import com.mapsindoors.mapssdk.PositionProvider;
import com.mapsindoors.mapssdk.PositionResult;
import com.mapsindoors.mapssdk.ReadyListener;
import com.mapsindoors.stdapp.positionprovider.helpers.PSUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * This abstract class extends the PositionProvider interface from the SDK. The reasoning, we require
 * position providers to offer a canDeliver() method, in order to evaluate their current
 * capability. This requirement comes from the PositionProviderManager class.
 * @see PositionProviderManager
 */
public abstract class AppPositionProvider implements PositionProvider {

    protected Context mContext;
    protected PositionResult mLatestPosition;
    protected String[] REQUIRED_PERMISSIONS;
    protected volatile boolean mIsRunning;
    protected boolean mIsIPSEnabled;
    protected String mProviderId;
    protected final List<OnStateChangedListener> onStateChangedListenersList = new ArrayList<>();
    protected final List<OnPositionUpdateListener> onPositionUpdateListeners = new ArrayList<>();

    protected boolean mCanDeliver;

    /**
     * Constructor
     * @param context
     */
    protected AppPositionProvider( @NonNull Context context ){
        mContext = context;
    }

    /**
     * Returns REQUIRED_PERMISSIONS, used for checking location permissions
     * @return
     */
    public String[] getRequiredPermissions() {
        return REQUIRED_PERMISSIONS;
    }

    /**
     * Returns boolean, indicating whether or not positioning is currently enabled
     * @return
     */
    public boolean isPSEnabled() {
        return mIsIPSEnabled;
    }

    /**
     * Returns boolean, indicating whether the position provider is running (doing positioning)
     * @return
     */
    public boolean isRunning() {
        return mIsRunning;
    }

    /**
     * Add position update listener
     * @param onPositionUpdateListener
     */
    public void addOnPositionUpdateListener( @Nullable final OnPositionUpdateListener onPositionUpdateListener ) {
        if( onPositionUpdateListener != null ) {
            onPositionUpdateListeners.remove( onPositionUpdateListener );
            onPositionUpdateListeners.add( onPositionUpdateListener );
        }
    }

    /**
     * Remove position update listener
     * @param onPositionUpdateListener
     */
    public void removeOnPositionUpdateListener( @Nullable final OnPositionUpdateListener onPositionUpdateListener ) {
        if( onPositionUpdateListener != null ) {
            onPositionUpdateListeners.remove( onPositionUpdateListener );
        }
    }

    /**
     * Add state change listener
     * @param onStateChangedListener
     */
    public void addOnStateChangedListener( @Nullable final OnStateChangedListener onStateChangedListener ) {
        if( onStateChangedListener != null ) {
            onStateChangedListenersList.remove( onStateChangedListener );
            onStateChangedListenersList.add( onStateChangedListener );
        }
    }

    /**
     * Remove position update listener
     * @param onStateChangedListener
     */
    public void removeOnStateChangedListener( @Nullable final OnStateChangedListener onStateChangedListener ) {
        if( onStateChangedListener != null ) {
            onStateChangedListenersList.remove( onStateChangedListener );
        }
    }

    /**
     * Reports the state change, to all listeners
     * @param state
     */
    protected void reportStateChanged(boolean state){
        for(OnStateChangedListener listener : onStateChangedListenersList){
            if(listener != null){
                listener.onStateChanged(state);
            }
        }
    }

    /**
     * Reports to listeners, upon new positioning
     */
    public void reportPositionUpdate() {
        if(mIsRunning){
            for(OnPositionUpdateListener listener : onPositionUpdateListeners){
                if(listener != null && mLatestPosition != null){
                    listener.onPositionUpdate(mLatestPosition);
                }
            }
        }
    }

    /**
     * Checks the location permissions, requires REQUIRED_PERMISSIONS to be set
     * @param permissionsAndPSListener
     */
    public void checkPermissionsAndPSEnabled( @Nullable final PermissionsAndPSListener permissionsAndPSListener ) {
        PSUtils.checkLocationPermissionAndServicesEnabled( getRequiredPermissions(), mContext, permissionsAndPSListener );
    }

    /**
     * Returns provider id
     * @return
     */
    @Nullable
    public String getProviderId() {
        return mProviderId;
    }

    /**
     * Sets provider id
     * @param id
     */
    public void setProviderId( @Nullable String id ) {
        mProviderId = id;
    }

    /**
     * Returns latest position result object
     * @return
     */
    public PositionResult getLatestPosition() {
        return mLatestPosition;
    }

    /**
     * Compute whether or not a position provider instance is capable of
     * providing positioning data, under current conditions.
     * @return true or false,
     */
    protected abstract void checkIfCanDeliver(ReadyListener onComplete);

    /**
     * Returns the internal boolean state of the position provider,
     * indicating whether it can currently deliver positioning or not.
     * @return
     */
    protected abstract boolean getCanDeliver();

    /**
     * Returns the name of the {@link PositionProvider}
     * @return Name of the PositionProvider
     */
    public abstract String getName();

    /**
     * Returns the versioning of the position provider
     * @return version name string
     */
    public abstract String getVersion();

    /**
     * Returns any additional relevant data concerning the state
     * of the position provider (Keys, id's etc.). It is up to the
     * individual position provider to utilize this appropriately, if at all.
     * @return meta data string
     */
    public abstract String getAdditionalMetaData();

    /**
     * React to application going in the background - usually disable any positioning
     */
    public void onPause() {
        boolean shouldRestart = false;
        if(isRunning()){
            shouldRestart = true;
        }
        stopPositioning(null);
        if(shouldRestart){
            mIsRunning = true;
        }
    }

    /**
     * React to application coming back into foreground, from the background - usually restart any positioning
     */
    public void onResume() {
        if(isRunning()){
            startPositioning(null);
        }
    }

}
