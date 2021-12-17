package com.mapsindoors.stdapp.ui.tracking;

import android.content.Context;
import android.util.AttributeSet;

import com.mapsindoors.stdapp.R;

import static com.mapsindoors.stdapp.ui.tracking.UserPositionTrackingViewModel.*;

public class FollowMeButton extends androidx.appcompat.widget.AppCompatImageButton {



    int mState = STATE_NO_LOCATION;

    public FollowMeButton(Context context) {
        super(context);
    }

    public FollowMeButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FollowMeButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    //    public FollowMeButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        super(context, attrs, defStyleAttr, defStyleRes);
//        init();
//    }


    public void setState(int state){

        if(state == mState){
            return;
        }

        changeButtonIcon(state);

        mState = state;

    }

    void init(){
        changeButtonIcon(mState);

    }


    void changeButtonIcon(int state){

        switch (state) {
            case STATE_NO_LOCATION:

                setImageResource(R.drawable.folow_me_btn_no_location);

                break;
            case STATE_LOCATION_TRACKING_ENABLED:
                setImageResource(R.drawable.folow_me_btn_location_tracking_enabled);


                break;
            case STATE_LOCATION_TRACKING_DISABLED:
            {


                setImageResource(R.drawable.folow_me_btn_location_tracking_disabled);
                break;
            }
            case STATE_COMPASS_TRACKING_ENABLED:
            {

                setImageResource( R.drawable.folow_me_btn_compass_tracking_enabled);
                break;
            }


            case STATE_COMPASS_TRACKING_DISABLED:
            {

                setImageResource( R.drawable.folow_me_btn_compass_tracking_disabled);
                break;
            }
        }

    }
}

