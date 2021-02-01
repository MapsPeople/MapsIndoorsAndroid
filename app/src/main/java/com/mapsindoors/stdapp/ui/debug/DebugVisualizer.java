package com.mapsindoors.stdapp.ui.debug;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import com.mapsindoors.stdapp.R;
import com.mapsindoors.stdapp.ui.activitymain.MapsIndoorsActivity;

import java.util.HashMap;
import java.util.Map;

/**
 * This class facilitates the instantiation and management of the debugging window.
 * The debug visualizer may contain a number of debug fields {@link DebugField}.
 */
public class DebugVisualizer {

    private boolean hidden;

    private final int mScreenWidth;
    private final int mScreenHeight;

    private int mAnimationSpeed;
    private int mTransparency;

    private final int mOverlapIntoScreenHidden = 50;
    private final float mTransparencyHidden = 0.4f;

    private final int mSnapAnimationDuration = 100;
    private final int mScaleAnimationDuration = 30;
    private final float mHoldDownScale = 0.95f;
    private final int mHoldDownToHideDuration = 2000;

    private final RelativeLayout mWindow;
    private final LinearLayout mLinearLayout;

    private final HashMap<String, DebugField> mFields = new HashMap<>();
    private final HashMap<DebugField, View> mFieldViews = new HashMap<>();

    private Context context;

    /**
     * Constructor, only accessible through the builder
     * @param context
     */
    private DebugVisualizer(MapsIndoorsActivity context){
        this.context = context;
        LayoutInflater inflater = context.getLayoutInflater();
        context.getWindow().addContentView(inflater.inflate(R.layout.debug_overlay, null),
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        mWindow = context.findViewById(R.id.debug_window);
        mWindow.setOnTouchListener(mOnTouchListener);

        this.mLinearLayout = context.findViewById(R.id.debugwindow_linearlayout);

        this.mScreenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        this.mScreenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;

        // Place the box in a sensible place, lower right
        int startPosX = (mScreenWidth / 2) - mWindow.getWidth();
        int startPosY = (mScreenHeight / 2) - mWindow.getHeight();
        mWindow.setX(startPosX);
        mWindow.setY(startPosY);

        show(false);

        // Hide it away
        snapToEdge();
    }

    /**
     * Set the window size (dp).
     * If a null value is given, the dimension behavior is "wrap_content".
     * @param width
     * @param height
     */
    public void resize(@Nullable Integer width, @Nullable Integer height){
        Resources r = context.getResources();
        ViewGroup.LayoutParams params = mLinearLayout.getLayoutParams();

        if(width != null){
            int widthDp = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, width, r.getDisplayMetrics()));
            params.width = widthDp;
        }

        if(height != null){
            int heightDp = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height, r.getDisplayMetrics()));
            params.height = heightDp;
        }

        mLinearLayout.setLayoutParams(params);
        mLinearLayout.refreshDrawableState();
    }

    public int getAnimationSpeed() {
        return mAnimationSpeed;
    }

    public int getTransparency() {
        return mTransparency;
    }

    /**
     * Add a number of debug fields {@link DebugField} to the debug window
     * @param fields
     */
    public void addFields(DebugField... fields){
        for(DebugField field : fields){
            mFields.put(field.getTitle(), field);
        }
        update();
    }

    /**
     * Retrieve a debug field {@link DebugField} from the debug window, based
     * on a string tag
     * @param tag
     * @return
     */
    public DebugField getDebugField(String tag){
        for(Map.Entry<String, DebugField> entry : mFields.entrySet()){
            if(entry.getValue().getTag().equals(tag.toLowerCase())){
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Toggle whether to show the window
     * @param show
     */
    public void show(boolean show){
        if(show){
            mWindow.setVisibility(View.VISIBLE);
        } else {
            mWindow.setVisibility(View.GONE);
        }
    }

    /**
     * Checks whether the window is currently shown
     * @return
     */
    public boolean isShown(){
        if(mWindow.getVisibility() == View.VISIBLE) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Trigger haptic feedback
     * @param feedback
     */
    public void performHapticFeedback(int feedback){
        mWindow.performHapticFeedback(feedback);
    }

    /**
     * Render the contents (debug fields) of the debug window
     */
    public void update(){
        for(Map.Entry<String, DebugField> entry : mFields.entrySet()){
            DebugField debugField = entry.getValue();
            if(!mFieldViews.containsKey(debugField)){
                LinearLayout view = debugField.getView(context);
                mLinearLayout.addView(view);
                mFieldViews.put(debugField, view);
            }
        }
    }

    /**
     * Computes whether x is closet to a or b (euclidean distance), and returns either a or b
     * @param x
     * @param a
     * @param b
     * @return
     */
    private float isClosestTo(float x, float a, float b){
        float diffToA = Math.abs(a - x);
        float diffToB = Math.abs(b - x);
        return diffToA < diffToB ? a : b;
    }

    /**
     * Snaps the debug window to either left or right edge of the screen
     */
    private void snapToEdge() {
        float x = this.mWindow.getX();

        if (isClosestTo(x, 0, mScreenWidth - (mWindow.getWidth())) == 0) {
            // snap to left
            int newPos = (-mWindow.getWidth()) + mOverlapIntoScreenHidden;
            mWindow.animate().translationX(newPos).alpha(mTransparencyHidden).setDuration(mSnapAnimationDuration).start();
        } else {
            // snap to right
            int newPos = mScreenWidth - mOverlapIntoScreenHidden;
            mWindow.animate().translationX(newPos).alpha(mTransparencyHidden).setDuration(mSnapAnimationDuration).start();
        }
    }


    // On long press, hide away the window
    private boolean isPressedDown = false;
    final Handler handler = new Handler();
    Runnable mLongPressed = new Runnable() {
        public void run() {
            if(isPressedDown){
                mWindow.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                hidden = true;
                snapToEdge();
            }
        }
    };


    private float dX = 0f;
    private float dY = 0f;
    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    hidden = false;
                    isPressedDown = true;
                    dX = v.getX() - event.getRawX();
                    dY = v.getY() - event.getRawY();
                    v.animate().scaleY(mHoldDownScale).scaleX(mHoldDownScale).alpha(1f).setDuration(mScaleAnimationDuration).start();
                    handler.postDelayed(mLongPressed, mHoldDownToHideDuration);
                    break;

                case MotionEvent.ACTION_MOVE:
                    if(!hidden){
                        v.setY(event.getRawY() + dY);
                        v.setX(event.getRawX() + dX);
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    v.animate().scaleY(1f).scaleX(1f).setDuration(mScaleAnimationDuration).start();
                    handler.removeCallbacks(mLongPressed);
                    isPressedDown = false;
                    break;

                default:
                    return false;
            }
            mWindow.invalidate();
            v.performClick();
            return true;
        }
    };


    public static class Builder {
        private int mAnimationSpeed = 100;
        private int mTransparency = 50;
        private Integer mHeight;
        private Integer mWidth;

        public Builder(){ }

        public Builder transparency(int transparency){
            mTransparency = transparency;
            return this;
        }

        public Builder animationSpeed(int speed){
            mAnimationSpeed = speed;
            return this;
        }

        public Builder setWindowSize(Integer width, Integer height){
            mWidth = width;
            mHeight = height;
            return this;
        }

        public DebugVisualizer build(MapsIndoorsActivity context){
            DebugVisualizer debugVisualizer = new DebugVisualizer(context);
            debugVisualizer.mAnimationSpeed = mAnimationSpeed;
            debugVisualizer.mTransparency = mTransparency;
            debugVisualizer.resize(mWidth, mHeight);
            return debugVisualizer;
        }
    }

}
