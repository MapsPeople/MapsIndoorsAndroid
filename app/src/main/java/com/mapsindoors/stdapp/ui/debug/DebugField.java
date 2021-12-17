package com.mapsindoors.stdapp.ui.debug;

import static android.content.Context.CLIPBOARD_SERVICE;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mapsindoors.stdapp.R;

import java.util.Arrays;
import java.util.List;

/**
 * A debug field consists of a title text and body text, both of which may be stylized.
 * Each field has a linear layout, and is responsible for updating and maintaining it.
 * Tags are used to name a given DebugField object, so it can be retrieved from its DebugVizualizer
 * parent {@link DebugVisualizer}.
 */
public class DebugField {

    private String mTitle;
    private String mText;

    private int mTitleColor;
    private int mTextColor;

    private int mTitleFontSize;
    private int mTextFontSize;

    private Context mContext;
    private LinearLayout mLinearLayoutView;
    private String mTag;
    private boolean mShow;

    private DebugField() {
    }

    public String getTag() {
        return mTag.toLowerCase();
    }

    public String getTitle() {
        return mTitle;
    }

    public void setShow(boolean show) {
        mShow = show;
        updateViewInternally();
    }

    public boolean isShown() {
        return mShow;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
        updateViewInternally();
    }

    public int getTitleColor() {
        return mTitleColor;
    }

    public void setTitleColor(int titleColor) {
        mTitleColor = titleColor;
    }

    public int getTextColor() {
        return mTextColor;
    }

    public void setTextColor(int textColor) {
        mTextColor = textColor;
    }

    public int getTitleFontSize() {
        return mTitleFontSize;
    }

    public void setTitleFontSize(int titleFontSize) {
        mTitleFontSize = titleFontSize;
    }

    public int getTextFontSize() {
        return mTextFontSize;
    }

    public void setTextFontSize(int textFontSize) {
        mTextFontSize = textFontSize;
    }


    public LinearLayout getView(Context context) {
        updateView(context);
        return mLinearLayoutView;
    }

    public void updateView(Context context) {
        mContext = context;
        updateViewInternally();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void updateViewInternally() {
        if (mContext == null) {
            return;
        }

        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayoutParams.setMargins(0, 10, 0, 10);

        // make a linear layout for each field
        if (mLinearLayoutView == null) {
            mLinearLayoutView = new LinearLayout(mContext);
        }
        mLinearLayoutView.setOnTouchListener((view, motionEvent) -> false);
        mLinearLayoutView.setOrientation(LinearLayout.VERTICAL);

        mLinearLayoutView.removeAllViews();

        if (!mShow) {
            mLinearLayoutView.removeAllViews();
            mLinearLayoutView.invalidate();
            return;
        }

        // set title view
        if (getTitle() != null) {
            RelativeLayout x = new RelativeLayout(mContext);


            RelativeLayout.LayoutParams rp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            RelativeLayout.LayoutParams rp2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            rp2.addRule(RelativeLayout.ALIGN_PARENT_END);

            TextView titleView = new TextView(mContext);
            titleView.setText(getTitle());
            titleView.setTypeface(Typeface.DEFAULT_BOLD);
            titleView.setTextColor(getTitleColor());
            titleView.setTextSize(getTitleFontSize());

            x.addView(titleView, rp);


            TextView copyView = new TextView(mContext);
            copyView.setText("COPY");
            Typeface type = Typeface.createFromAsset(mContext.getAssets(), "fonts/roboto_bold.ttf");
            copyView.setTypeface(type);
            copyView.setTextColor(Color.YELLOW);
            copyView.setTextSize(getTitleFontSize());
            copyView.setGravity(Gravity.END);
            copyView.setClickable(true);

            copyView.setOnClickListener(v -> {
                ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(getTitle(), getText());
                clipboard.setPrimaryClip(clip);

                Toast.makeText(mContext, getTitle() + " info has been copied to the clipboard", Toast.LENGTH_SHORT).show();
            });

            x.addView(copyView, rp2);

            mLinearLayoutView.addView(x, linearLayoutParams);

        }

        // set text view
        if (getText() != null) {
            TextView textView = new TextView(mContext);
            textView.setText(getText());
            textView.setTextColor(getTextColor());
            textView.setTextSize(getTextFontSize());
            mLinearLayoutView.addView(textView, linearLayoutParams);
        }

        // set a divider, if the debug field actually has any content
        if (getTitle() != null || getText() != null) {
            View divider = new View(mContext);
            divider.setMinimumHeight(1);
            divider.setBackground(mContext.getDrawable(R.drawable.white_rectangle_background));
            divider.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
            mLinearLayoutView.addView(divider);
        }

    }

    public static class Builder {
        private String mTitle = null;
        private String mText = null;
        private String mTag = null;

        // Sensible defaults
        private int mTitleColor = Color.WHITE;
        private int mTextColor = Color.WHITE;
        private int mTitleFontSize = 12;
        private int mTextFontSize = 9;
        private boolean mShow = false;

        public Builder setTitle(String title) {
            mTitle = title;
            return this;
        }

        public Builder setTag(String tag) {
            mTag = tag.toLowerCase();
            return this;
        }

        public Builder setText(String text) {
            mText = text;
            return this;
        }

        public Builder setTitleColor(int color) {
            mTitleColor = color;
            return this;
        }

        public Builder setTitleSize(int fontSize) {
            mTitleFontSize = fontSize;
            return this;
        }

        public Builder setTextColor(int color) {
            mTextColor = color;
            return this;
        }

        public Builder setTextSize(int fontSize) {
            mTextFontSize = fontSize;
            return this;
        }

        public Builder setShow(boolean show) {
            mShow = show;
            return this;
        }

        public DebugField build() {
            DebugField field = new DebugField();
            field.mTitle = mTitle;
            field.mText = mText;
            field.mTitleColor = mTitleColor;
            field.mTextColor = mTextColor;
            field.mTitleFontSize = mTitleFontSize;
            field.mTextFontSize = mTextFontSize;
            field.mTag = mTag;
            field.mShow = mShow;
            return field;
        }

    }

    public static DebugField[] getGeneralFields() {
        DebugField floor = new DebugField.Builder()
                .setTitle("Floor")
                .setText("-")
                .setTag("floor")
                .build();

        DebugField venue = new DebugField.Builder()
                .setTitle("Venue")
                .setText("-")
                .setTag("venue")
                .build();

        DebugField building = new DebugField.Builder()
                .setTitle("Building")
                .setText("-")
                .setTag("building")
                .build();

        DebugField location = new DebugField.Builder()
                .setTitle("Current Location")
                .setText("-")
                .setTag("location")
                .build();

        DebugField locations = new DebugField.Builder()
                .setTitle("# of Locations")
                .setText("-")
                .setTag("locations")
                .build();

        DebugField map = new DebugField.Builder()
                .setTitle("Map")
                .setText("-")
                .setTag("map")
                .build();

        DebugField current_filter = new DebugField.Builder()
                .setTitle("Current filter")
                .setText("-")
                .setTag("filter")
                .build();

        DebugField active_userRoles = new DebugField.Builder()
                .setTitle("Active App User Roles")
                .setText("-")
                .setTag("userRoles")
                .build();

        return new DebugField[]{floor, venue, building, location, locations, map, current_filter, active_userRoles};
    }

    /**
     * @return
     */
    public static List<DebugField> getGeneralFieldsAsList() {
        return Arrays.asList(getGeneralFields().clone());
    }

    public static DebugField[] getPositionProviderFields() {
        DebugField position = new DebugField.Builder()
                .setTitle("Position")
                .setTitleSize(16)
                .setText("-")
                .setTextSize(12)
                .setTag("position")
                .build();

        DebugField provider = new DebugField.Builder()
                .setTitle("Using")
                .setTitleSize(16)
                .setText("-")
                .setTextSize(12)
                .setTag("provider")
                .build();

        DebugField meta = new DebugField.Builder()
                .setTitle("Meta")
                .setTitleSize(16)
                .setText("-")
                .setTextSize(12)
                .setTag("meta")
                .build();

        return new DebugField[]{position, provider, meta};
    }


    /**
     * @return
     */
    public static List<DebugField> getPositionProviderFieldsAsList() {
        return Arrays.asList(getPositionProviderFields().clone());
    }

}
