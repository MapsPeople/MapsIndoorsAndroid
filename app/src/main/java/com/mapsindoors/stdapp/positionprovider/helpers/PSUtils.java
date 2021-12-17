package com.mapsindoors.stdapp.positionprovider.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.mapsindoors.mapssdk.OnResultReadyListener;
import com.mapsindoors.mapssdk.PermissionsAndPSListener;
import com.mapsindoors.mapssdk.dbglog;
import com.mapsindoors.mapssdk.errors.MIError;
import com.mapsindoors.stdapp.R;
import com.mapsindoors.stdapp.managers.GoogleAnalyticsManager;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * PSUtils
 * MapsIndoorsDemo
 * <p>
 * Created by Amine on 02/08/2017.
 * Copyright © 2017 MapsPeople A/S. All rights reserved.
 */
@SuppressWarnings({"WeakerAccess"})
public class PSUtils {
    static final String TAG = PSUtils.class.getSimpleName();

    static boolean mIsNoshowNOGPSPermissionAlertDialogueShown = false;


    public static boolean isLocationServiceEnabled(Context context) {


        if (isActivityFinishing(context)) {
            return false;
        }

        final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (manager != null) {
            return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }

        return false;
    }

    public static boolean isLocationPermissionGranted(Context context) {

        if (isActivityFinishing(context)) {
            return false;
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            return (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
        } else {
            return true;
        }
    }

    public static boolean arePermissionsGranted(Context context, String[] permissions) {

        if (isActivityFinishing(context)) {
            return false;
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

            for (String permission : permissions) {

                if ((context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)) {
                    return false;
                }
            }
        }
        return true;

    }

    static void startGPSSettingsActivity(Context context) {

        if (isActivityFinishing(context)) {
            return;
        }
        Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        context.startActivity(gpsOptionsIntent);
    }

    static void startAppSettingsActivity(Context context) {

        if (isActivityFinishing(context)) {
            return;
        }

        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        context.startActivity(intent);
    }

    public static void showNOGPSAlertDialogue(final Context context) {

        if (isActivityFinishing(context)) {
            return;
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        alertDialogBuilder.setTitle(context.getResources().getString(R.string.no_gps_dialogue_title));
        alertDialogBuilder.setMessage(context.getResources().getString(R.string.no_gps_dialogue_text));

        alertDialogBuilder.setPositiveButton(context.getResources().getString(R.string.action_settings), (dialogInterface, i) -> startGPSSettingsActivity(context));

        alertDialogBuilder.setNegativeButton(context.getResources().getString(R.string.cancel), (dialogInterface, i) -> {
            if (!isActivityFinishing(context)) {
                dialogInterface.cancel();
            }
        });


        final AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
    }

    static void showNOGPSPermissionAlertDialogue(final Context context) {

        if (isActivityFinishing(context)) {
            return;
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        alertDialogBuilder.setTitle(context.getResources().getString(R.string.no_gps_permission_dialogue_title));
        alertDialogBuilder.setMessage(context.getResources().getString(R.string.no_gps_permission_dialogue_text));

        alertDialogBuilder.setPositiveButton(context.getResources().getString(R.string.action_settings), (dialogInterface, i) -> {
            startAppSettingsActivity(context);
            mIsNoshowNOGPSPermissionAlertDialogueShown = false;
        });

        alertDialogBuilder.setNegativeButton(context.getResources().getString(R.string.cancel), (dialogInterface, i) -> {
            dialogInterface.cancel();
            mIsNoshowNOGPSPermissionAlertDialogueShown = false;
        });

        final AlertDialog alertDialog = alertDialogBuilder.create();

        if (!mIsNoshowNOGPSPermissionAlertDialogueShown) {
            alertDialog.show();
            mIsNoshowNOGPSPermissionAlertDialogueShown = true;
        }
    }

	// A simpler way to check location permissions, and only react on location permission granted
	public static void runLocationPermissionsCheck(@Nullable String[] requiredPermissions, @NonNull final Context context, @Nullable Runnable onPermissionGranted){
		checkLocationPermissionAndServicesEnabled(requiredPermissions, context, new PermissionsAndPSListener() {
			@Override
			public void onPermissionDenied() { }

			@Override
			public void onPermissionGranted() {
				onPermissionGranted.run();
			}

			@Override
			public void onGPSPermissionAndServiceEnabled() { }

			@Override
			public void onPermissionRequestError() { }
		});
	}

	public static void checkLocationPermissionAndServicesEnabled( @Nullable String[] requiredPermissions, @NonNull final Context context, @Nullable  final PermissionsAndPSListener gpsPermissionAndServiceListener )
	{
		//region DEXTER HANDLERS
		final MultiplePermissionsListener dexterMultiplePermissionsListener = new MultiplePermissionsListener()
		{
			@Override
			public void onPermissionsChecked( MultiplePermissionsReport report ) {


				if(!report.getDeniedPermissionResponses().isEmpty()){
					for(PermissionDeniedResponse response : report.getDeniedPermissionResponses()){
						Log.d("Denied:",response.getPermissionName());
					}
				}
				Log.d("Report: ", Arrays.toString(report.getDeniedPermissionResponses().toArray()));
				if( report.areAllPermissionsGranted() ) {
					// permission granted
					if( gpsPermissionAndServiceListener != null ) {
						gpsPermissionAndServiceListener.onPermissionGranted();
					}

                    if (!PSUtils.isLocationServiceEnabled(context)) {
                        PSUtils.showNOGPSAlertDialogue(context);

                        GoogleAnalyticsManager.reportEvent(context.getString(R.string.fir_event_No_Location_Service), null);

                    } else {
                        if (gpsPermissionAndServiceListener != null) {
                            gpsPermissionAndServiceListener.onGPSPermissionAndServiceEnabled();
                        }
                    }

				} else {
					//permission denied
					for(PermissionDeniedResponse response : report.getDeniedPermissionResponses()){
						Log.d("frha", response.getPermissionName());
					}
					if( gpsPermissionAndServiceListener != null ) {
						gpsPermissionAndServiceListener.onPermissionDenied();
					}

                    boolean permanentlyDenied = report.isAnyPermissionPermanentlyDenied();
                    if (permanentlyDenied) {
                        showNOGPSPermissionAlertDialogue(context);
                    }

                    if (dbglog.isDeveloperMode()) {
                        dbglog.Log(TAG, "Dexter.onPermissionDenied: User has denied permissions and selected 'Never ask again'");
                    }
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                token.continuePermissionRequest();
            }
        };

        final PermissionRequestErrorListener dexterPermissionRequestErrorListener = dexterError -> {
            if (gpsPermissionAndServiceListener != null) {
                gpsPermissionAndServiceListener.onPermissionRequestError();
            }

            if (dbglog.isDeveloperMode()) {
                dbglog.Log(TAG, String.format(Locale.US, "Dexter.onError: %s", dexterError));
            }
        };
        //endregion

        Dexter.withActivity((Activity) context).
                withPermissions(requiredPermissions).
                withListener(dexterMultiplePermissionsListener).
                withErrorListener(dexterPermissionRequestErrorListener).
                check();
    }


    static boolean isActivityFinishing(Context context) {

        if (context == null) {
            return true;
        }

        if ((context instanceof Activity)) {
            Activity asActivity = (Activity) context;
            return asActivity.isFinishing();
        }

        return false;
    }
}
