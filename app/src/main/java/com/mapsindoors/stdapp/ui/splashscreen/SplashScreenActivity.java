package com.mapsindoors.stdapp.ui.splashscreen;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewPropertyAnimatorListener;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.mapsindoors.mapssdk.MapsIndoors;
import com.mapsindoors.stdapp.R;
import com.mapsindoors.stdapp.helpers.MapsIndoorsUtils;
import com.mapsindoors.stdapp.ui.activitymain.MapsIndoorsActivity;

import static com.mapsindoors.stdapp.helpers.MapsIndoorsUtils.isNetworkReachable;


/**
 * SplashScreenActivity
 * MapsIndoorsDemo
 * <p>
 * Created by Jose J Varó on 03/19/2017.
 * Copyright © 2017 MapsPeople A/S. All rights reserved.
 */
public class SplashScreenActivity extends AppCompatActivity {
    private static final String TAG = SplashScreenActivity.class.getSimpleName();

    private ViewGroup mSplashMainLayout;
    private ViewGroup mSplashMainView;
    private ImageView mIconStatic, mIconDynamic;

    private boolean mAnimIconDone, mAnimMainViewDone;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mAnimIconDone = mAnimMainViewDone = false;

        setContentView(R.layout.fragment_splashscreen);
        mSplashMainLayout = findViewById(R.id.splash_layout);
        mSplashMainView = findViewById(R.id.splash_main);

        mIconStatic = mSplashMainLayout.findViewById(R.id.splash_icon);
        mIconDynamic = mSplashMainLayout.findViewById(R.id.splash_icon_2);

        if (!isNetworkReachable(this) && MapsIndoors.checkOfflineDataAvailability()) {
            Snackbar.make(getWindow().getDecorView().getRootView()
                    , getResources().getString(R.string.no_internet_snackbar_message),
                    Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }


        mSplashMainLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //Remove the listener before proceeding
                mSplashMainLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                int dstTop = mIconStatic.getTop();
                int srcTop = mIconDynamic.getTop();

                animateIcon(dstTop - srcTop);
            }
        });
    }


    void animateIcon(int toTopPos) {
        ViewCompat.animate(mIconDynamic)
                .translationY(toTopPos)
                .setDuration(500)
                //.setInterpolator( new DecelerateInterpolator() )
                .setListener(new ViewPropertyAnimatorListener() {
                    @Override
                    public void onAnimationStart(View view) {
                        mAnimIconDone = false;

                        animateMainView(0);
                    }

                    @Override
                    public void onAnimationEnd(View view) {
                        mAnimIconDone = true;
                        view.setAlpha(1f);
                        continueToGooglePlayServicesCheck();
                    }

                    @Override
                    public void onAnimationCancel(View view) {
                    }
                })
                .setStartDelay(125);
    }

    void animateMainView(int initDelay) {
        ViewCompat.animate(mSplashMainView)
                .alpha(1f)
                .setDuration(250)
                .setListener(new ViewPropertyAnimatorListener() {
                    @Override
                    public void onAnimationStart(View view) {
                        mAnimMainViewDone = false;
                    }

                    @Override
                    public void onAnimationEnd(View view) {
                        mAnimMainViewDone = true;
                        view.setAlpha(1f);
                        continueToGooglePlayServicesCheck();
                    }

                    @Override
                    public void onAnimationCancel(View view) {
                    }
                })
                .setStartDelay(initDelay);
    }

    void continueToGooglePlayServicesCheck() {
        if (mAnimIconDone && mAnimMainViewDone) {
            if (MapsIndoorsUtils.CheckGooglePlayServices(this)) {
                prepareNextActivity();
            }
        }
    }

    private void prepareNextActivity() {
        //	final long timeToWait = getSplashScreenDelay();


        final long timeToWait = 0;
        final Class clazz = MapsIndoorsActivity.class;

        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> startNextActivity(clazz), timeToWait);
    }

    void startNextActivity(Class clazz) {
        Intent intent = new Intent(SplashScreenActivity.this, clazz);

        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }
}
