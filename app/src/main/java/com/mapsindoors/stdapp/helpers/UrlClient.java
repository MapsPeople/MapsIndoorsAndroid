package com.mapsindoors.stdapp.helpers;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.mapsindoors.stdapp.positionprovider.OnResultReadyListener;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class UrlClient<T> extends AsyncTask<Void, Void, Boolean> {

    private OnResultReadyListener<T> mListener;
    private Request mRequest;
    private Class<T> mClazz;
    private T mResult;

    public UrlClient(Request request, Class<T> clazz, OnResultReadyListener<T> onResultReadyListener){
        mListener = onResultReadyListener;
        mRequest = request;
        mClazz = clazz;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            OkHttpClient okHttpClient = new OkHttpClient();
            Response response = okHttpClient.newCall(mRequest).execute();

            if(response.isSuccessful()){
                ResponseBody responseBody = response.body();

                if(responseBody != null){
                    String responseString = responseBody.string();

                    Log.d("Url client response:",responseString);

                    mResult = new Gson().fromJson(responseString,mClazz);
                    return true;
                }
            }
            response.close();
            return false;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if(result){
            mListener.onResultReady(mResult);
        }
        mListener.onResultReady(null);
    }
}
