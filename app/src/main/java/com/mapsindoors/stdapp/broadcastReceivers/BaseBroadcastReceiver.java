package com.mapsindoors.stdapp.broadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mapsindoors.mapssdk.OnStateChangedListener;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseBroadcastReceiver extends BroadcastReceiver {

    public static final String TAG = BaseBroadcastReceiver.class.getSimpleName();

    static boolean mFirstTime = true;

    Context mContext;

    // list to store all the listeners
    List<OnStateChangedListener> stateChangedListeners;
    boolean mPreviousState;


    public BaseBroadcastReceiver(@NonNull Context context) {
        stateChangedListeners = new ArrayList<>();

        mContext = context;
    }


    public void addOnStateChangedListener(@Nullable OnStateChangedListener onStateChangedListener) {
        if (stateChangedListeners != null) {
            stateChangedListeners.remove(onStateChangedListener);
            stateChangedListeners.add(onStateChangedListener);
        }
    }

    public void removeOnStateChangedListener(@Nullable OnStateChangedListener onStateChangedListener) {
        if (stateChangedListeners != null) {
            stateChangedListeners.remove(onStateChangedListener);
        }
    }

    void reportStateToListeners(boolean state) {
        if ((stateChangedListeners != null) && !stateChangedListeners.isEmpty()) {

            // only when the state changes
            if (state != mPreviousState || mFirstTime) {

                if (mFirstTime) {
                    mFirstTime = false;
                }

                for (OnStateChangedListener stChListener : stateChangedListeners) {
                    if (stChListener != null) {
                        stChListener.onStateChanged(state);
                    }
                }

                mPreviousState = state;
            }
        }
    }

    public void terminate() {
        if (stateChangedListeners != null) {
            stateChangedListeners.clear();
            stateChangedListeners = null;
        }

        if (mContext != null) {
            mContext = null;
        }
    }
}
