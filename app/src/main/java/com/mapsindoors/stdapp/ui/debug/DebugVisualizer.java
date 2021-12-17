package com.mapsindoors.stdapp.ui.debug;

import android.content.res.Resources;
import android.os.Build;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import com.mapsindoors.stdapp.ui.activitymain.MapsIndoorsActivity;

import java.util.Arrays;
import java.util.HashMap;

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

    private final int mScreenBounds;
    private final float mTransparencyHidden = 0.4f;

    private final int mSnapAnimationDuration = 100;
    private final int mScaleAnimationDuration = 30;
    private final float mHoldDownScale = 0.95f;

    private RelativeLayout mWindow;
    private LinearLayout mLinearLayout;

    private final HashMap<String, DebugField> mFields = new HashMap<>();
    private final HashMap<DebugField, View> mFieldViews = new HashMap<>();

    private MapsIndoorsActivity mContext;

    /**
     * Constructor, only accessible through the builder
     *
     * @param context
     */

    private DebugVisualizer(MapsIndoorsActivity context, int overlayRes, int linearLayoutRes, int windowRes, int screenBounds) {
        mContext = context;
        LayoutInflater inflater = context.getLayoutInflater();
        context.getWindow().addContentView(inflater.inflate(overlayRes, null),
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        mLinearLayout = mContext.findViewById(linearLayoutRes);
        mWindow = mContext.findViewById(windowRes);

        mWindow.setOnTouchListener(mOnTouchListener);

        mScreenBounds = screenBounds;


        mScreenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        mScreenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;

        // Place the box in a sensible place, lower right
        int startPosX = (mScreenWidth / 2) - mWindow.getWidth();
        int startPosY = (mScreenHeight / 2) - mWindow.getHeight();
        mWindow.setX(startPosX);
        mWindow.setY(startPosY);

        show(false);

        // Hide it away
        snapToRight();
    }

    /**
     * Set the window size (dp).
     * If a null value is given, the dimension behavior is "wrap_content".
     *
     * @param width
     * @param height
     */
    public void resize(@Nullable Integer width, @Nullable Integer height) {
        Resources r = mContext.getResources();
        ViewGroup.LayoutParams params = mLinearLayout.getLayoutParams();

        if (width != null) {
            params.width = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, width, r.getDisplayMetrics()));
        }

        if (height != null) {
            params.height = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height, r.getDisplayMetrics()));
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
     *
     * @param fields a number of fields to be added
     */
    public void addFields(DebugField... fields) {
        for (DebugField field : fields) {
            mFields.put(field.getTag(), field);
            add(field);
        }

    }

    /**
     * Add a debug field {@link DebugField} to the debug window
     *
     * @param field the field to be added
     */
    public void addField(DebugField field) {
        mFields.put(field.getTag(), field);
        add(field);
    }

    /**
     * Removes a field from the debug visualizer
     *
     * @param debugField the key for the field
     * @return true if the field was found and removed, otherwise false
     */
    public boolean removeField(DebugField debugField) {
        if (mFields.containsKey(debugField.getTag())) {
            mFields.remove(debugField.getTag());
            remove(debugField);
            return true;
        }
        return false;
    }


    /**
     * Retrieve a debug field {@link DebugField} from the debug window, based
     * on a string tag
     *
     * @param tag
     * @return
     */
    public DebugField getDebugField(String tag) {
        return mFields.get(tag);
    }

    public void updateDebugField(String tag, String... value) {
        DebugField debugField = mFields.get(tag);
        if (debugField != null) {
            if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                String[] strings = value.clone();
                StringBuilder stringBuilder = new StringBuilder();
                for (String s : strings) {
                    stringBuilder.append(s).append("\n");
                }
                debugField.setText(stringBuilder.toString());
            } else {
                debugField.setText(Arrays.stream(value).map(x -> x + "\n").reduce("", String::concat));
            }
        }
    }

    public boolean hasDebugField(String tag) {
        return mFields.get(tag) != null;
    }

    public void setShowDebugField(String tag, Boolean isShown) {
        DebugField debugField = mFields.get(tag);
        if (debugField != null) {
            debugField.setShow(isShown);
        }
    }

    /**
     * Toggle whether to show the window
     * {@link #hide()} the window if show is false
     *
     * @param show
     */
    public void show(boolean show) {
        mWindow.setVisibility(show ? View.VISIBLE : View.GONE);
        if (!show) {
            hide();
        }
    }

    /**
     * Hides the window by shoving it to the right edge of the screen
     */
    public void hide() {
        snapToRight();
    }

    /**
     * Checks whether the window is currently shown
     *
     * @return
     */
    public boolean isShown() {
        return mWindow.getVisibility() == View.VISIBLE;
    }

    /**
     * Trigger haptic feedback
     *
     * @param feedback
     */
    public void performHapticFeedback(int feedback) {
        mWindow.performHapticFeedback(feedback);
    }

    /**
     * Render the contents (debug fields) of the debug window
     */
    private void add(DebugField field) {
        if (!mFieldViews.containsKey(field)) {
            LinearLayout view = field.getView(mContext);
            mLinearLayout.addView(view);
            mFieldViews.put(field, view);
        }
    }

    private void remove(DebugField field) {
        if (mFieldViews.containsKey(field)) {
            LinearLayout view = field.getView(mContext);
            mLinearLayout.removeView(view);
            mFieldViews.remove(field, view);
        }
    }

    /**
     * Computes whether x is closet to a or b (euclidean distance), and returns either a or b
     *
     * @param x
     * @param a
     * @param b
     * @return
     */
    private float isClosestTo(float x, float a, float b) {
        float diffToA = Math.abs(a - x);
        float diffToB = Math.abs(b - x);
        return diffToA < diffToB ? a : b;
    }

    /**
     * Snaps the debug window to the right edge of the screen and makes it more translucent
     */
    private void snapToRight() {
        int newPos = mScreenWidth - mScreenBounds;
        mWindow.animate().translationX(newPos).alpha(mTransparencyHidden).setDuration(mSnapAnimationDuration).start();
    }

    private float dX = 0f;
    private float dY = 0f;


    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    hidden = false;
                    dX = v.getX() - event.getRawX();
                    dY = v.getY() - event.getRawY();
                    v.animate().scaleY(mHoldDownScale).scaleX(mHoldDownScale).alpha(1f).setDuration(mScaleAnimationDuration).start();
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (!hidden) {
                        v.setY(event.getRawY() + dY);
                        v.setX(event.getRawX() + dX);
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    v.animate().scaleY(1f).scaleX(1f).setDuration(mScaleAnimationDuration).start();
                    float x = v.getX();
                    float y = v.getY();
                    if (x + mWindow.getWidth() < mScreenBounds) {
                        v.setX(mScreenBounds - mWindow.getWidth());
                    } else if (x > mScreenWidth - mScreenBounds) {
                        v.setX(mScreenWidth - mScreenBounds);
                    }
                    if (y + mWindow.getHeight() < mScreenBounds) {
                        v.setY(mScreenBounds - mWindow.getHeight());
                    } else if (y > mScreenHeight - mScreenBounds) {
                        v.setY(mScreenHeight - mScreenBounds);
                    }
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
        private int mScreenBounds = 50;

        public Builder() {
        }

        public Builder transparency(int transparency) {
            mTransparency = transparency;
            return this;
        }

        public Builder animationSpeed(int speed) {
            mAnimationSpeed = speed;
            return this;
        }

        public Builder setWindowSize(Integer width, Integer height) {
            mWidth = width;
            mHeight = height;
            return this;
        }

        public Builder setScreenBounds(int bounds) {
            mScreenBounds = bounds;
            return this;
        }

        public DebugVisualizer build(MapsIndoorsActivity context, int overlayRes, int linearLayoutRes, int windowRes) {
            DebugVisualizer debugVisualizer = new DebugVisualizer(context, overlayRes, linearLayoutRes, windowRes, mScreenBounds);
            debugVisualizer.mAnimationSpeed = mAnimationSpeed;
            debugVisualizer.mTransparency = mTransparency;
            debugVisualizer.resize(mWidth, mHeight);
            return debugVisualizer;
        }
    }

}
