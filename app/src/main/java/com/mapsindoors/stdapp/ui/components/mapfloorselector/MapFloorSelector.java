package com.mapsindoors.stdapp.ui.components.mapfloorselector;

import android.animation.Animator;
import android.content.Context;
import android.os.Build;
import androidx.annotation.AttrRes;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;

import com.mapsindoors.mapssdk.Floor;
import com.mapsindoors.mapssdk.FloorSelectorInterface;
import com.mapsindoors.mapssdk.OnFloorSelectionChangedListener;
import com.mapsindoors.stdapp.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MapFloorSelector extends FrameLayout implements FloorSelectorInterface {
    static final String TAG = MapFloorSelector.class.getSimpleName();

    boolean mWillShowView;

    private static final int FADE_IN_TIME_MS = 500;
    private static final int FADE_OUT_TIME_MS = 500;

    public static final float SHOW_ON_ZOOM_LEVEL = 17;

    private OnFloorSelectionChangedListener mOnFloorSelectionChangedListener;
    private ListView mLvFloorSelector;
    private ImageView mIvBottomGradient;
    private ImageView mIvTopGradient;

    private List<Floor> mFloors;
    private MapFloorSelectorAdapter mFloorSelectorAdapter;
    private float currentMapZoomLevel;


    //region CTOR

    /**
     * Required default constructor - just forwarding to private {@link #init()}
     *
     * @param context Default Context.
     */
    public MapFloorSelector(@NonNull Context context) {
        super(context);
        init();
    }

    public MapFloorSelector(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MapFloorSelector(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public MapFloorSelector(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    /**
     * Inflates the layout and does view finding as well as setting the adapter for the ListView
     */
    private void init() {
        inflate(getContext(), R.layout.control_mapsindoors_floor_selector2, this);

        mFloors = new ArrayList<>();

        mLvFloorSelector = findViewById(R.id.mapspeople_floor_selector_list);
        mIvBottomGradient = findViewById(R.id.bottom_gradient);
        mIvTopGradient = findViewById(R.id.top_gradient);

        mFloorSelectorAdapter = new MapFloorSelectorAdapter(getContext(), R.layout.control_mapsindoors_floor_selector_button);
        mFloorSelectorAdapter.setCallback(mMapFloorSelectorAdapterListener);
        mLvFloorSelector.setAdapter(mFloorSelectorAdapter);
        mLvFloorSelector.getViewTreeObserver().addOnGlobalLayoutListener(mGlobalLayoutListener);
        mLvFloorSelector.setOnItemClickListener(mFloorSelectorOnItemClickListener);

        this.setVisibility(INVISIBLE);
        this.setAlpha(0.0f);

        currentMapZoomLevel = 0.0f;
    }
    //endregion


    //region IMPLEMENTS FloorSelectorInterface
    @Nullable
    @Override
    public View getView() {
        return null;
    }

    /**
     * Sets the {@link OnFloorSelectionChangedListener} to invoke when changes occur in this view.
     * I.e. a new floor has been selected.
     *
     * @param onFloorSelectionChangedListener Listener to be invoked
     */
    @Override
    public void setOnFloorSelectionChangedListener(@Nullable OnFloorSelectionChangedListener onFloorSelectionChangedListener) {
        mOnFloorSelectionChangedListener = onFloorSelectionChangedListener;
    }

    /**
     * Sets the list of {@link Floor} to show in the FloorSelector
     *
     * @param floors List of Floors to show.
     */
    @Override
    public void setList(@Nullable List<Floor> floors) {
        mFloors.clear();
        if (floors == null) {
            return;
        }
        mFloors.addAll(floors);
        // the floors should be ordered in a descendant way
        Collections.reverse(mFloors);

        mFloorSelectorAdapter.setFloors(mFloors);
        mFloorSelectorAdapter.notifyDataSetChanged();
    }

    /**
     * Shows or hides the view, with/without animation, based on the implementation
     *
     * @param show     Should the view be shown?
     * @param animated Should the transition be animated?
     */
    @Override
    public void show(boolean show, boolean animated) {
        mWillShowView = show && canTheFloorSelectorBeShown();

        final float currentAlpha = getAlpha();
        if (mWillShowView && (currentAlpha < 1.0f)){
            if (animated) {
                animate().alpha(1.0f).setDuration(FADE_IN_TIME_MS).setListener(mAnimationListener).start();
            } else {
                setVisibility(VISIBLE);
                setAlpha(1.0f);
            }
            mLvFloorSelector.setOnItemClickListener(mFloorSelectorOnItemClickListener);
        } else if (!mWillShowView){
            if (animated && (currentAlpha > 0.1f)) {
                animate().alpha(0.0f).setDuration(FADE_OUT_TIME_MS).setListener(mAnimationListener).start();
            } else {
                setVisibility(INVISIBLE);
                setAlpha(0.0f);
            }
            mLvFloorSelector.setOnItemClickListener((parent, view, position, id) -> {});
        }
    }

    /**
     * Sets the floor selected and forwards it to the {@link MapFloorSelectorAdapter}
     *
     * @param floor Floor selected
     */
    @Override
    public void setSelectedFloor(@NonNull Floor floor) {
        mFloorSelectorAdapter.setSelectedFloor(floor);
        scrollToFloor(floor);

    }

    /**
     * Sets the selected floor based on a Z-index
     *
     * @param zIndex Z index of the new Floor to be selected
     */
    @Override
    public void setSelectedFloorByZIndex(int zIndex) {
        if (!hasFloors()) {
            return;
        }
        for (Floor floor : mFloors) {
            if (floor.getZIndex() == zIndex) {
                setSelectedFloor(floor);
                return;
            }
        }
    }

    /**
     * Invoked when the Zoom level changes - Checks if there is a reason to show the FloorSelector
     *
     * @param newZoomLevel The current Map camera zoom value
     */
    @Override
    public void zoomLevelChanged(@FloatRange(from = 0, to = 22) float newZoomLevel) {
        currentMapZoomLevel = newZoomLevel;

        // Internal notification about the event
        onMapZoomLevelChanged();
    }

    /**
     * Should the floor selection change automatically
     *
     * @return - true if the floor should change automatically, false if not.
     */
    @Override
    public boolean isAutoFloorChangeEnabled() {
        return true;
    }

    @Override
    public void setUserPositionFloor(int zIndex) {
        mFloorSelectorAdapter.setUserPositionFloor(zIndex);
        mFloorSelectorAdapter.notifyDataSetChanged();
    }
    //endregion

    /**
     * What to do if the Map zoom level changes ...
     */
    void onMapZoomLevelChanged() {
        show(true, true);
    }

    boolean canTheFloorSelectorBeShown() {
        final boolean isZoomLevelWithinVisibleRange = currentMapZoomLevel >= SHOW_ON_ZOOM_LEVEL;

        return isZoomLevelWithinVisibleRange && hasFloors();
    }

    /**
     * Measures the total height of children in the List
     *
     * @return true if the combined height exceeds the height of the view itself, false if not
     */
    boolean isListScrollable() {
        View listViewChild = mLvFloorSelector.getChildAt(0);
        if (listViewChild != null) {
            int totalHeightOfChildren = listViewChild.getHeight() * mFloors.size();
            int heightOfView = mLvFloorSelector.getHeight();

            return totalHeightOfChildren > heightOfView;
        }
        return false;
    }

    /**
     * Checks if there are any floors available
     *
     * @return {@code true} if the current building has any floors, {@code false} otherwise
     */
    boolean hasFloors() {
        return (mFloors != null) && !mFloors.isEmpty();
    }

    /**
     * Private Adapter.Callback - Basically just forwarding messages from the {@link MapFloorSelectorAdapter}
     * to any {@link OnFloorSelectionChangedListener} registered
     */
    private MapFloorSelectorAdapterListener mMapFloorSelectorAdapterListener = new MapFloorSelectorAdapterListener() {
        @Override

        public void onFloorSelectionChanged(@NonNull Floor newFloor) {
            if (mOnFloorSelectionChangedListener != null) {
                mOnFloorSelectionChangedListener.onFloorSelectionChanged(newFloor);
            }
        }
    };

    /**
     * GlobalLayoutListener used to check if the ListView in the FloorSelector is scrollable
     * and thereby should have gradient scroll indicators
     */
    private ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            mIvTopGradient.setVisibility(isListScrollable() ? VISIBLE : INVISIBLE);
            mIvBottomGradient.setVisibility(isListScrollable() ? VISIBLE : INVISIBLE);
        }
    };

    /**
     * AnimationListener used to set properties of the view before/after the animation.
     */
    private Animator.AnimatorListener mAnimationListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {
            if (getVisibility() != View.VISIBLE) {
                setVisibility(VISIBLE);
            }
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (!mWillShowView && !(getVisibility() == VISIBLE)) {
                setVisibility(INVISIBLE);
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            //Auto generated method stub
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
            //Auto generated method stub
        }
    };

    private void scrollToFloor(@NonNull Floor floor) {
        if (isListScrollable()) {
            int pos = getPositionOfFloor(floor);
            mLvFloorSelector.smoothScrollToPosition(pos);
        }
    }

    private int getPositionOfFloor(@NonNull Floor floor) {
        for (int i = mFloors.size(); --i >= 0; ) {
            final Floor f = mFloors.get(i);
            if (f.getZIndex() == floor.getZIndex()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * For setting up OnItemClickListener for the Floor Selector, as this bit is used multiple times.
    */
    private final AdapterView.OnItemClickListener mFloorSelectorOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            if (mFloors.size() > 0) {
                mOnFloorSelectionChangedListener.onFloorSelectionChanged(mFloors.get(i));
            }
            mFloorSelectorAdapter.setSelectedListPosition(i);
            mFloorSelectorAdapter.notifyDataSetChanged();
        }
    };


}
