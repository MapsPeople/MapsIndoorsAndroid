package com.mapsindoors.stdapp.helpers;

import android.app.Activity;
import android.app.Dialog;
import android.app.usage.NetworkStatsManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AlertDialog;
import android.text.Html;
import android.text.Spanned;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.mapsindoors.stdapp.R;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

/**
 * GoogleAnalyticsManager
 * MapsIndoorsDemo
 * <p>
 * Created by Jose J Varó on 20/01/2017.
 * Copyright © 2017 MapsPeople A/S. All rights reserved.
 */
public class MapsIndoorsUtils {


    @SuppressWarnings( "deprecation" )
    @NonNull
    public static Spanned fromHtml(@NonNull String html) {
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N )
        {
            return Html.fromHtml( html, Html.FROM_HTML_MODE_LEGACY );
        } else
        {
            return Html.fromHtml( html );
        }
    }


    public static boolean isNullOrEmpty( @Nullable final Collection<?> c ) {
        return (c == null) || c.isEmpty();
    }

    public static boolean isNullOrEmpty( @Nullable final Map<?,?> map ) {
        return (map == null) || map.isEmpty();
    }

    /**
     * Shows the default Google Play Services dialog if needed
     *
     * @param activity
     * @return
     */
    public static boolean CheckGooglePlayServices(@Nullable final Activity activity) {

        if( activity == null )
        {
            return false;
        }

        GoogleApiAvailability gap = GoogleApiAvailability.getInstance();

        final int googlePlayServicesCheck = gap.isGooglePlayServicesAvailable(activity);

        switch (googlePlayServicesCheck) {
            case ConnectionResult.SUCCESS:
                return true;
            case ConnectionResult.SERVICE_DISABLED:
            case ConnectionResult.SERVICE_INVALID:
            case ConnectionResult.SERVICE_MISSING:
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED: {
                Dialog dialog = gap.getErrorDialog(activity, googlePlayServicesCheck, 0);
                dialog.setOnCancelListener( dialogInterface -> activity.finish() );
                dialog.show();
            }
        }
        return false;
    }


    public static boolean checkIfBitmapIsEmpty( @NonNull Bitmap bmp )
    {
        Bitmap emptyBitmap = Bitmap.createBitmap( bmp.getWidth(), bmp.getHeight(), bmp.getConfig() );
        return bmp.sameAs( emptyBitmap );
    }

    public static boolean isNetworkReachable( @Nullable Context context )
    {
        boolean online = false;

        if( context != null )
        {
            Context appCtx = context.getApplicationContext();
            if( appCtx != null )
            {
                ConnectivityManager cm = (ConnectivityManager) appCtx.getSystemService( Context.CONNECTIVITY_SERVICE );

                if( cm != null )
                {
                    NetworkInfo netInfo = cm.getActiveNetworkInfo();
                    online = netInfo != null && netInfo.isConnectedOrConnecting();
                }
            }
        }
        return online;
    }

    public static boolean isBluetoothEnabled()
    {
        final BluetoothAdapter defaultBLEAdapter = BluetoothAdapter.getDefaultAdapter();

        return (defaultBLEAdapter != null) && defaultBLEAdapter.isEnabled();
    }

    /**
     * @param url The Url to get its domain name from
     * @return The given Url's domain name if possible or itself if something went wrong
     */
    @NonNull
    public static String getDomainName( @NonNull String url )
    {
        String domain;
        try
        {
            URI uri = new URI( url );
            domain = uri.getHost();
        } catch( URISyntaxException e )
        {
            domain = url;
        }

        if( domain.startsWith( "www." ) )
        {
            domain = domain.substring( 4 );
        }

        return domain;
    }

    public static void showLeavingAlertDialogue( @Nullable final Context context )
    {
        if( context == null )
        {
            return;
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder( context );

        alertDialogBuilder.setTitle( context.getResources().getString( R.string.leaving_app_dialogue_title ) );
        alertDialogBuilder.setMessage( context.getResources().getString( R.string.leaving_app_dialogue_text ) );

        alertDialogBuilder.setPositiveButton( context.getResources().getString( R.string.yes ), ( dialogInterface, i ) -> ((Activity) context).finish() );
        alertDialogBuilder.setNegativeButton( context.getResources().getString( R.string.cancel ), ( dialogInterface, i ) -> dialogInterface.cancel() );

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    //region ANDROID SDK VERSION AWARE WRAPPERS
    @NonNull
    public static Locale getDeviceDefaultLocale( @NonNull Resources res )
    {
        Locale locale;
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N )
        {
            locale = res.getConfiguration().getLocales().get( 0 );
        } else
        {
            locale = res.getConfiguration().locale;
        }

        return locale;
    }

    public static void setTextAppearance( @Nullable Context context, @Nullable TextView textView, @StyleRes int resId )
    {
        if( (context == null) || (textView == null) )
        {
            return;
        }

        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M )
        {
            textView.setTextAppearance( resId );
        }
        else
        {
            textView.setTextAppearance( context, resId );
        }
    }
    //endregion



    public static void showInvalidAPIKeyDialogue( @Nullable final Context context )
    {
        if( context != null )
        {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder( context, R.style.AlertDialogCustom );

            // set title
            alertDialogBuilder.setTitle(context.getResources().getString(R.string.invalid_api_key_dialogue_title));

            // set dialog message
            alertDialogBuilder.setMessage(R.string.invalid_api_key_dialogue_text).setCancelable(false);

            SharedPrefsHelper.setApiKeyValidity(context, false);

            new Handler( context.getMainLooper() ).post( () -> {

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();
                // show it
                alertDialog.show();
            });
        }
    }
}
