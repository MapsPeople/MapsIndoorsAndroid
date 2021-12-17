package com.mapsindoors.stdapp.positionprovider;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.mapsindoors.mapssdk.MapsIndoors;
import com.mapsindoors.stdapp.positionprovider.ciscoDna.CiscoDnaPositionProvider;
import com.mapsindoors.stdapp.positionprovider.gps.GoogleAPIPositionProvider;
import com.mapsindoors.stdapp.positionprovider.helpers.PSUtils;
import com.mapsindoors.stdapp.positionprovider.indooratlas.IndoorAtlasPositionProvider;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

/**
 * The PositionProviderManager is responsible for selection and life-cycle management of
 * position providers.
 */
public class PositionProviderManager {

    final String[] REQUIRED_PERMISSIONS;

    // Priority list, smaller index = higher priority
    private final LinkedList<AppPositionProvider> mProviderList;
    private AppPositionProvider mCurrentProvider;

    private final OnPositionProviderChangedListener mOnPositionProviderChangedListener;

    /**
     * Constructor, initializes a list containing position providers
     * ordered according to priority. Smaller index = higher priority.
     * @param context
     */
    public PositionProviderManager(@NonNull Context context, OnPositionProviderChangedListener onPositionProviderChangedListener){
        mProviderList = new LinkedList<>();
        if (Build.VERSION.SDK_INT >= 31) {
            REQUIRED_PERMISSIONS = new String[]{
                    "android.permission.ACCESS_FINE_LOCATION",
                    "android.permission.ACCESS_COARSE_LOCATION",
                    "android.permission.BLUETOOTH_SCAN"
            };
        }else {
            REQUIRED_PERMISSIONS = new String[]{
                    "android.permission.ACCESS_FINE_LOCATION",
                    "android.permission.ACCESS_COARSE_LOCATION",
                    "android.permission.BLUETOOTH_ADMIN",
                    "android.permission.BLUETOOTH"
            };
        }
        mOnPositionProviderChangedListener = onPositionProviderChangedListener;
        PSUtils.runLocationPermissionsCheck(REQUIRED_PERMISSIONS, context, () -> {
            addProviders(mProviderList, context);
            getProvider();
        });
    }

    public void onPause(){
        for(AppPositionProvider provider : mProviderList){
            provider.onPause();
        }
    }

    public void onResume(){
        for(AppPositionProvider provider : mProviderList){
            provider.onResume();
        }
    }

    /**
     * Adds position providers, upon location permissions granted.
     * If permission is not granted, no position provider objects will be instantiated
     */
    private void addProviders(LinkedList<AppPositionProvider> providerList, Context context) {
        Map<String, Map<String, Object>> configs = MapsIndoors.getSolution().getPositionProviderConfig();
        if(configs != null){
            ArrayList<String> providerNamesByPriority = getPositionProviderNamesByPriority(configs);
            for(String providerName : providerNamesByPriority){
                switch (providerName.toLowerCase()) {
                    case "indooratlas3":
                        if(!providerListContains(providerList, IndoorAtlasPositionProvider.class)){
                            providerList.addLast(new IndoorAtlasPositionProvider(context, configs.get("indooratlas3")));
                        }
                        break;
                    case "ciscodna":
                        if(!providerListContains(providerList, CiscoDnaPositionProvider.class)){
                            providerList.addLast(new CiscoDnaPositionProvider(context, configs.get("ciscodna")));
                        }
                        break;
                }
            }
        }

        // Append a GPS provider to the back of the list, for fallback purposes and for solutions without any positionProviderConfig
        providerList.addLast(new GoogleAPIPositionProvider(context));
    }

    /**
     * Compute a sorted array of strings (position provider names), based on their priority value
     * @param configs
     * @return
     */
    @NotNull
    private ArrayList<String> getPositionProviderNamesByPriority(@NotNull Map<String, Map<String, Object>> configs){
        ArrayList<Pair<String, Double>> priorityList = new ArrayList<>();

        // Construct pairs
        for(Map.Entry<String, Map<String, Object>> config : configs.entrySet()){
            String providerName = config.getKey();
            Double priority = (Double) config.getValue().get("priority");

            if(providerName == null || TextUtils.isEmpty(providerName)){
                continue;
            }

            // If priority value is null, use max double as priority
            if(priority == null){
                priority = Double.MAX_VALUE;
            }

            priorityList.add(new Pair<>(providerName, priority));
        }

        // Sort pairs
        Collections.sort(priorityList, (o1, o2) -> o1.second.compareTo(o2.second));

        // Put into an array
        ArrayList<String> sortedArrayList = new ArrayList<>(priorityList.size());
        for(Pair<String, Double> pair : priorityList){
            sortedArrayList.add(pair.first);
        }

        return sortedArrayList;
    }

    /**
     * Checks if a the providerList contains any objects of a given class
     * @param providerList
     * @param clazz
     * @return
     */
    private boolean providerListContains(@NotNull LinkedList<AppPositionProvider> providerList, Class clazz){
        for(AppPositionProvider provider : providerList){
            if(provider.getClass().equals(clazz)){
                return true;
            }
        }
        return false;
    }

    /**
     * Steps through the provider list, and tests whether a given provider
     * can deliver positioning. The first capable position provider is returned.
     * @return AppPositionProvider object
     */
    public AppPositionProvider getProvider(){
        if(mCurrentProvider != null && getPositionProvidersListSize() == 1) {
            return mCurrentProvider;
        }

        for(AppPositionProvider provider : mProviderList) {
            provider.checkIfCanDeliver(() -> {
                if(provider.getCanDeliver()){
                    if(mCurrentProvider == null || !mCurrentProvider.getCanDeliver()){
                        // if we don't have one, just grab gps
                        mCurrentProvider = mProviderList.getLast();
                        mOnPositionProviderChangedListener.onPositionProviderChanged(mCurrentProvider);
                    } else {
                        // if this one can deliver, and is higher on the list than the current one, switch!
                        if(mProviderList.indexOf(mCurrentProvider) > mProviderList.indexOf(provider)){
                            mCurrentProvider = provider;
                            mOnPositionProviderChangedListener.onPositionProviderChanged(mCurrentProvider);
                        }
                    }
                }
            });
        }
        return mCurrentProvider;
    }

    /**
     * Checks if the provider list contains any elements
     * @return boolean
     */
    public boolean hasPositionProviders() {
        return !mProviderList.isEmpty();
    }

    /**
     * Returns the size of the position provider list
     * (the number of providers available to choose from)
     * @return
     */
    public int getPositionProvidersListSize(){
        return mProviderList.size();
    }

    public void onDestroy() {
        mCurrentProvider = null;
        for (AppPositionProvider item: mProviderList) {
            item.terminate();
        }
        mProviderList.clear();
    }

}
