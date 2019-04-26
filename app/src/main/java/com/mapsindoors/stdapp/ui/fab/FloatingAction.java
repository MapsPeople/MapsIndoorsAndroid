package com.mapsindoors.stdapp.ui.fab;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mapsindoors.mapssdk.Convert;
import com.mapsindoors.mapssdk.MenuInfo;
import com.mapsindoors.stdapp.BuildConfig;
import com.mapsindoors.stdapp.R;
import com.mapsindoors.stdapp.helpers.MapsIndoorsUtils;
import com.mapsindoors.stdapp.managers.AppConfigManager;
import com.mapsindoors.stdapp.managers.GoogleAnalyticsManager;
import com.mapsindoors.stdapp.ui.common.listeners.FloatingActionListener;

import java.util.List;

/**
 * @author Martin Hansen
 */
public class FloatingAction {
    public static final String TAG = FloatingAction.class.getSimpleName();


    private static final int LAYOUT_FAB_BUTTONS_COUNT = 3;

    private int mFABButtonHeight;
    boolean mIsFABOpen;
    View mView;
    FloatingActionListener mFabListener;
    private boolean mIsActive;

    Activity mActivity;
    FloatingActionButton mFabSearch;



    @IdRes
    int[] mFabCategoryButtonIds = {R.id.fab_button1, R.id.fab_button2, R.id.fab_button3};
    FabButtonWithLabel [] mFabButtons;

    private TextView mFabTextView;




    public FloatingAction(final Activity activity, FloatingActionListener fabListener, AppConfigManager appConfigManager, View view) {
        mFabListener = fabListener;
        mView = view;
        mActivity = activity;

        mIsActive = true;

        mFABButtonHeight = Convert.getPixels(50, activity);

        mFabSearch = view.findViewById(R.id.fabSearch);
        mFabSearch.setAlpha(1f);

        mFabTextView = view.findViewById(R.id.TextAct1);
        mFabTextView.setAlpha(1f);


        mFabSearch.setOnClickListener( v -> onFABClick() );

        final List< MenuInfo > fabmenuEntries = appConfigManager.getFabMenuEntries();

        if( MapsIndoorsUtils.isNullOrEmpty( fabmenuEntries ) )
        {
            view.setVisibility( View.GONE );
        } else
        {
            view.setVisibility(View.VISIBLE);

            if( BuildConfig.DEBUG )
            {
                // We've hardcoded only x buttons in the layout ATM, check if this solution has more
                if( fabmenuEntries.size() > LAYOUT_FAB_BUTTONS_COUNT )
                {
                    new Handler( activity.getMainLooper() ).post( () -> {
                        Toast.makeText( activity, TAG +
                                ": FloatingAction -> appConfig.fabMenu has more than " + LAYOUT_FAB_BUTTONS_COUNT +
                                " entries!: " + fabmenuEntries.size(),
                                Toast.LENGTH_SHORT ).show();
                    });
                }
            }

            int fabEntriesCount = fabmenuEntries.size();

            // cap
            fabEntriesCount = (fabEntriesCount <= LAYOUT_FAB_BUTTONS_COUNT) ? fabEntriesCount : LAYOUT_FAB_BUTTONS_COUNT;

            mFabButtons = new FabButtonWithLabel[fabEntriesCount];

            for( int i = 0; i < fabEntriesCount; i++ )
            {

                MenuInfo mi = fabmenuEntries.get( i );
                FabButtonWithLabel fabButtonWL = view.findViewById( mFabCategoryButtonIds[i] );
                mFabButtons[i] = fabButtonWL;

                if( (mi != null) && (fabButtonWL != null) )
                {
                    final String catKey = mi.getCategoryKey();
                    fabButtonWL.setFabButtonImageBitmap( appConfigManager.getFabMenuIcon( catKey ) );
                    String label = appConfigManager.getFabMenuItem(catKey).getName();

                    fabButtonWL.setLabelText(label);

                    fabButtonWL.setOnClickListener( v1 ->
                    {
                        //Close the fab if open
                        if( mIsFABOpen )
                        {
                            if( mFabListener != null )
                            {
                                mFabListener.onFABSelect( catKey );
                                mFabListener.onFABListClose();
                            }
                            onFABClick();
                        } else
                        {
                            onFABClick();
                        }
                    } );
                }
            }
        }
    }

    public View getView() {
        return mView;
    }

    public void close() {
        if (mIsFABOpen) {
            onFABClick();
        }
    }

    public void setActive(boolean isActive) {

        if (isActive != mIsActive) {
            ValueAnimator va = isActive ? ValueAnimator.ofFloat(0f, 1f) : ValueAnimator.ofFloat(1f, 0f);
            va.addUpdateListener( animation -> {
                float animVal = ((Float) animation.getAnimatedValue());
                mFabSearch.setAlpha(animVal);
            } );

            va.setDuration(500);
            va.start();

            if (!isActive) {
                close();
            }

            mIsActive = isActive;
        }
    }

    void onFABClick() {
        if (!mIsActive) {
            return;
        }

        mIsFABOpen = !mIsFABOpen;

        ValueAnimator va = mIsFABOpen ? ValueAnimator.ofFloat(0f, 1f) : ValueAnimator.ofFloat(1f, 0f);
        va.addUpdateListener( animation -> {

            final float animVal = ((Float) animation.getAnimatedValue());

            // Rotation angle goes from 0 to 90
            mFabSearch.setRotation( animVal * 90 );

            if( mFabListener != null )
            {
                mFabListener.onFABAnimationUpdate( animVal );
            }
        });

        if (mIsFABOpen) {
            GoogleAnalyticsManager.reportEvent( mActivity.getString( R.string.fir_event_Map_Fab_Opened ), null );

            mFabSearch.setImageResource(R.drawable.ic_clear_white_24dp);
            mFabListener.onFABListOpen();
        }
        else {
            mFabListener.onFABListClose();
        }

        va.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!mIsFABOpen) {
                    mFabSearch.setImageResource(R.drawable.ic_map_marker_radius);
                }
            }

            @Override public void onAnimationStart(Animator animation) {}
            @Override public void onAnimationCancel(Animator animation) {}
            @Override public void onAnimationRepeat(Animator animation) {}
        });

        va.start();

        for (int i = 0, j = 1, aLen = mFabButtons.length; i < aLen; i++, j++) {
            toggle(i, mFABButtonHeight * j, mIsFABOpen);
        }
    }

    private void toggle(final int buttonIndex, final int distancePx, boolean open) {
        mFabButtons[buttonIndex].setY(mFabSearch.getY());
        mFabButtons[buttonIndex].setVisibility(View.VISIBLE);
        mFabButtons[buttonIndex].setClickable(open);
        final float startY = mFabSearch.getY();
        ValueAnimator va = open ? ValueAnimator.ofFloat(0f, 1f) : ValueAnimator.ofFloat(1f, 0f);
        va.addUpdateListener( animation -> {
            float animVal = ((Float) animation.getAnimatedValue());
            mFabButtons[buttonIndex].setAlpha(animVal);
            mFabButtons[buttonIndex].setY(startY - (animVal * distancePx));
        } );
        va.start();
    }

    public boolean isActive() {
        return mIsActive;
    }

    public boolean isOpen() {
        return mIsActive && mIsFABOpen;
    }

    public void setVisible(boolean visibility) {
        if (visibility){
            mFabSearch.setVisibility(View.VISIBLE);
            mFabTextView.setVisibility(View.VISIBLE);
        }
        else {
            mFabSearch.setVisibility(View.INVISIBLE);
            mFabTextView.setVisibility(View.INVISIBLE);
        }
    }

    public void setAlpha(float alpha) {
        mFabSearch.setAlpha(alpha);
        mFabTextView.setAlpha(alpha);
    }
}
