package com.mapsindoors.stdapp.positionprovider.gps;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mapsindoors.mapssdk.MPPositionResult;
import com.mapsindoors.mapssdk.OnPositionUpdateListener;
import com.mapsindoors.mapssdk.OnStateChangedListener;
import com.mapsindoors.mapssdk.PermissionsAndPSListener;
import com.mapsindoors.mapssdk.Point;
import com.mapsindoors.mapssdk.PositionProvider;
import com.mapsindoors.mapssdk.PositionResult;
import com.mapsindoors.mapssdk.Route;
import com.mapsindoors.mapssdk.RouteLeg;
import com.mapsindoors.mapssdk.RouteStep;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class RouteTrackingDebugPositionProvider implements PositionProvider {


    private List<OnPositionUpdateListener> mPositionUpdateListeners;

    int currentLegIndex;
    int currentStepIndex;
    Timer t = new Timer();

    Route route;

    public RouteTrackingDebugPositionProvider(Route route) {

        this.route = route;


    }


    void reportNewLocationToListeners(PositionResult newPosition) {
        if (mPositionUpdateListeners != null) {
            for (OnPositionUpdateListener listener : mPositionUpdateListeners) {
                if (listener != null) {
                    listener.onPositionUpdate(newPosition);
                }
            }
        }
    }


    @NonNull
    @Override
    public String[] getRequiredPermissions() {
        return new String[0];
    }

    @Override
    public boolean isPSEnabled() {
        return false;
    }

    @Override
    public void startPositioning(@Nullable String s) {

        currentLegIndex = 0;
        currentStepIndex = 0;


        //Set the schedule function and rate
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                RouteLeg currentLeg = route.getLegs().get(currentLegIndex);
                RouteStep currentStep = currentLeg.getSteps().get(currentStepIndex);

                int geometryIndex = currentStep.getGeometry().size() / 2;
                MPPositionResult newLocation = new MPPositionResult(
                        new Point(currentStep.getGeometry().get(geometryIndex).getLatLng()),
                        100);

                newLocation.setFloor((int) currentStep.getEndLocation().getZIndex());

                reportNewLocationToListeners(newLocation);

                currentStepIndex++;
                currentStepIndex %= (currentLeg.getSteps().size());

                if (currentStepIndex == 0) {
                    currentLegIndex++;
                    currentLegIndex %= (route.getLegs().size());
                }
            }
        }, 0, 10000);
    }

    @Override
    public void stopPositioning(@Nullable String s) {
        t.cancel();
    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public void addOnPositionUpdateListener(@Nullable OnPositionUpdateListener listener) {
        if (listener != null) {
            if (mPositionUpdateListeners == null) {
                mPositionUpdateListeners = new ArrayList<>();
            }

            mPositionUpdateListeners.remove(listener);
            mPositionUpdateListeners.add(listener);
        }
    }

    @Override
    public void removeOnPositionUpdateListener(@Nullable OnPositionUpdateListener listener) {
        if (listener != null) {
            if (mPositionUpdateListeners != null) {
                mPositionUpdateListeners.remove(listener);
                if (mPositionUpdateListeners.isEmpty()) {
                    mPositionUpdateListeners = null;
                }
            }
        }
    }

    @Override
    public void setProviderId(@Nullable String s) {

    }

    @Override
    public void addOnStateChangedListener(@Nullable OnStateChangedListener onStateChangedListener) {

    }

    @Override
    public void removeOnStateChangedListener(@Nullable OnStateChangedListener onStateChangedListener) {

    }

    @Override
    public void checkPermissionsAndPSEnabled(@Nullable PermissionsAndPSListener permissionAndPSlistener) {
        permissionAndPSlistener.onGPSPermissionAndServiceEnabled();
    }

    @Nullable
    @Override
    public String getProviderId() {
        return null;
    }

    @Nullable
    @Override
    public PositionResult getLatestPosition() {
        return null;
    }

    @Override
    public void startPositioningAfter(int i, @Nullable String s) {

    }

    @Override
    public void terminate() {

    }
}
