package com.mapsindoors.stdapp.condeco;

import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mapsindoors.mapssdk.dbglog;


import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CondecoBookingProvider {

    public static final String TAG = CondecoBookingProvider.class.getSimpleName();

    /**
     * Interval for the poll timer
     */
    private static final long POLL_INTERVAL = 2000;

    private List<OnCondecoBookingUpdateListener> mBookingUpdateListeners;
    private boolean mIsRunning;

    private Timer mPollTimer;

    private GsonBuilder mGsonBuilder;
    private Type mTokenModelType;
    private Type mBookingModelType;

    private Set<String> mExternalIds = new HashSet<>();

    private final SimpleDateFormat mInputSDF = new SimpleDateFormat("yyyy-MM-dd");
    private final String mOutputDateFormat = "yyyy-MM-dd'T'HH:mm:ss";
    private final SimpleDateFormat mOutputSDF;


    public CondecoBookingProvider() {

        mBookingUpdateListeners = null;
        mIsRunning = false;

        mOutputSDF = new SimpleDateFormat(mOutputDateFormat);
        mOutputSDF.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public void addOnPositionUpdateListener(@Nullable OnCondecoBookingUpdateListener listener) {
        if (listener != null) {
            if (mBookingUpdateListeners == null) {
                mBookingUpdateListeners = new ArrayList<>();
            }

            mBookingUpdateListeners.remove(listener);
            mBookingUpdateListeners.add(listener);
        }
    }

    public void removeOnPositionUpdateListener(@Nullable OnCondecoBookingUpdateListener listener) {
        if (listener != null) {
            if (mBookingUpdateListeners != null) {
                mBookingUpdateListeners.remove(listener);
                if (mBookingUpdateListeners.isEmpty()) {
                    mBookingUpdateListeners = null;
                }
            }
        }
    }

    public void terminate() {
        stopPolling();

        if (mBookingUpdateListeners != null) {
            mBookingUpdateListeners.clear();
            mBookingUpdateListeners = null;
        }
    }

    public void startPolling() {
        if (!mIsRunning) {
            if (mPollTimer != null) {
                mPollTimer.cancel();
                mPollTimer.purge();
                mPollTimer = null;
            }

            mIsRunning = true;

            mPollTimer = new Timer("MICondecoTimer", false);
            mPollTimer.schedule(
                    getTimerTask(),
                    POLL_INTERVAL,
                    POLL_INTERVAL
            );
        }
    }

    public void stopPolling() {
        if (dbglog.isDeveloperMode()) {
            dbglog.Log(TAG, "stopPollingBookings - Start");
        }

        if (mIsRunning) {
            mIsRunning = false;

            if (mPollTimer != null) {
                mPollTimer.cancel();
                mPollTimer.purge();
                mPollTimer = null;
            }
            if (mQueryBookingsTask != null) {
                mQueryBookingsTask.cancel();
                mQueryBookingsTask = null;
            }
        }
    }


    private TimerTask mQueryBookingsTask;

    private TimerTask getTimerTask() {

        boolean recreateTask = true;
        if (mQueryBookingsTask != null) {
            recreateTask = !mQueryBookingsTask.cancel();
        }

        if (recreateTask) {
            mQueryBookingsTask = new TimerTask() {
                @Override
                public void run() {
                    if (mIsRunning) {
                        try {

                            pollBookings();

                        } catch (Exception e) {
                            //e.printStackTrace();
                            //sendFailure( "startPositioning -> Exception:\n" + e );
                            if (dbglog.isDeveloperMode()) {
                                dbglog.Log(TAG, "QueryPositionTask -> Exception:\n" + e);
                            }
                        }
                    }
                }
            };
        }

        return mQueryBookingsTask;
    }

    private void pollBookings() {
        final String tokenUrl = "https://ise-demo.condecosoftware.com/tokenproviderapi/token";

        // We could check if the previous call is done and skip the call otherwise (?)
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

        FormBody tokenRequestData = new FormBody.Builder()
                .add("client_id", "insert client id here")
                .add("password", "instert password here")
                .add("grant_type", "password")
                .build();

        Request request = new Request.Builder().url(tokenUrl).post(tokenRequestData).build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                onResult(response.body().string(),response.code());
            }
        });
    }

    void onResult(String result, int status) {
        if (status == HttpURLConnection.HTTP_OK) {
            //Get the actual data
            CondecoTokenResult resp = parseTokenResult(result);
            getBookings(resp.getAccessToken());
        } else {
            if (dbglog.isDeveloperMode()) {
                dbglog.Log(TAG, "TokenGetTask -> Server returned with status: " + status);
            }
        }
    }

    CondecoTokenResult parseTokenResult(@Nullable String data) {
        if (TextUtils.isEmpty(data)) {
            return null;
        }

        if (mGsonBuilder == null) {
            mGsonBuilder = new GsonBuilder();
        }

        if (mTokenModelType == null) {
            mTokenModelType = new TypeToken<CondecoTokenResult>() {
            }.getType();
        }

        return mGsonBuilder.create().fromJson(data, mTokenModelType);
    }

    private void getBookings(@NotNull String accessToken) {
        final HttpUrl endpointUrl = HttpUrl.parse("https://developer-api.condecosoftware.com/ISE_Event_Demo/api/V1/bookings");

        // We could check if the previous call is done and skip the call otherwise (?)
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

        String dateString = mInputSDF.format(new Date());

        for (String externalId : mExternalIds) {

            HttpUrl url = endpointUrl.newBuilder()
                    .addQueryParameter("roomId", externalId)
                    .addQueryParameter("isUTCDateTime", "false")
                    .addQueryParameter("startDate", dateString)
                    .addQueryParameter("endDate", dateString)
                    .build();

            Request request = new Request.Builder().url(url)
                    .header("Ocp-Apim-Subscription-Key", "insert subscription key here")
                    .header("Authorization", "Bearer " + accessToken)
                    .build();

            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {

                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    onBookingResult(externalId, response.body().string(), response.code());
                }
            });
        }
    }

    private void onBookingResult(String externalId, String result, int status) {
        if (status == HttpURLConnection.HTTP_OK) {
            CondecoBookingResult resp = parseBookingResult(result);
            resp.setExternalId(externalId);

            //API does not provide a timezone indicator in the date format
            //Therefor we must manually parse any times that we know are UTC. Otherwise they will be considered local time
            for (CondecoBooking booking : resp.getBookings()) {
                try {
                    booking.setTimeFromUTCDate(mOutputSDF.parse(booking.getTimeFromUTC()));
                    booking.setTimeToUTCDate(mOutputSDF.parse(booking.getTimeToUTC()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            //publish the data
            publishResult(resp);
        } else {
            if (dbglog.isDeveloperMode()) {
                dbglog.Log(TAG, "BookingGetTask -> Server returned with status: " + status);
            }
        }
    }

    private void publishResult(CondecoBookingResult result) {
        for (OnCondecoBookingUpdateListener listener : mBookingUpdateListeners) {
            listener.BookingsAvailable(result);
        }
    }

    CondecoBookingResult parseBookingResult(@Nullable String data) {
        if (TextUtils.isEmpty(data)) {
            return null;
        }

        if (mGsonBuilder == null) {
            mGsonBuilder = new GsonBuilder();
        }

        if (mBookingModelType == null) {
            mBookingModelType = new TypeToken<CondecoBookingResult>() {
            }.getType();
        }

        return mGsonBuilder.setDateFormat(mOutputDateFormat).create().fromJson(data, mBookingModelType);
    }

    public Set<String> getExternalIds() {
        return mExternalIds;
    }

    public void setExternalIds(Set<String> mExternalIds) {
        this.mExternalIds = mExternalIds;
    }
}
