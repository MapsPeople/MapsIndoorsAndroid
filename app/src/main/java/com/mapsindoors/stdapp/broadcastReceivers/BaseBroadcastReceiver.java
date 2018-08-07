package com.mapsindoors.stdapp.broadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;

import com.mapsindoors.mapssdk.OnStateChangedListener;
import com.mapsindoors.stdapp.positionprovider.helpers.PSUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseBroadcastReceiver extends BroadcastReceiver {

    public static final String TAG = BaseBroadcastReceiver.class.getSimpleName();

    // list to store all the listeners
    static List<OnStateChangedListener> sStateChangedListenerList;
    boolean mPreviousState;
    boolean mFirstTime;

    Context mContext;



    public BaseBroadcastReceiver(Context activityContext){
        if( sStateChangedListenerList == null )
        {
            sStateChangedListenerList = new ArrayList<>();
        }else
        {
            sStateChangedListenerList.clear();
        }

        mContext = activityContext;

        mFirstTime = true;




    }


    public static void addOnStateChangedListener( OnStateChangedListener onStateChangedListener )
    {
        if( sStateChangedListenerList != null )
        {
            sStateChangedListenerList.remove( onStateChangedListener );
            sStateChangedListenerList.add( onStateChangedListener );
        }
    }

    public static void removeOnStateChangedListener( OnStateChangedListener onStateChangedListener )
    {
        if( sStateChangedListenerList != null )
        {
            sStateChangedListenerList.remove( onStateChangedListener );
        }
    }

    void reportStateToListeners( boolean state )
    {
        if ((sStateChangedListenerList != null) && !sStateChangedListenerList.isEmpty()) {

            // only when the state changes
            if (state != mPreviousState || mFirstTime) {

                if (mFirstTime) {
                    mFirstTime = false;
                }

                for (OnStateChangedListener stChListener : sStateChangedListenerList) {
                    if (stChListener != null) {
                        stChListener.onStateChanged(state);
                    }
                }
                mPreviousState = state;

            }

        }
    }

    public void terminate()
    {
        if( sStateChangedListenerList != null )
        {
            sStateChangedListenerList.clear();
            sStateChangedListenerList = null;
        }

        if( mContext != null )
        {
            mContext = null;
        }
    }
}
