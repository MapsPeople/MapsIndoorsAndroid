package com.mapsindoors.stdapp.ui.debug;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mapsindoors.stdapp.R;

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
    private boolean mShow = true;

    private DebugField(){}

    public String getTag(){
        return mTag.toLowerCase();
    }

    public String getTitle() {
        return mTitle;
    }

    public void setShow(boolean show){
        mShow = show;
        updateViewInternally();
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

    public LinearLayout getView(Context context){
        updateView(context);
        return mLinearLayoutView;
    }

    public void updateView(Context context){
        mContext = context;
        updateViewInternally();
    }

    private void updateViewInternally(){
        if(mContext == null){
            return;
        }

        if(!mShow){
            mLinearLayoutView.removeAllViews();
            mLinearLayoutView.invalidate();
            return;
        }

        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayoutParams.setMargins(0, 10, 0, 10);

        // make a linear layout for each field
        if(mLinearLayoutView == null){
             mLinearLayoutView = new LinearLayout(mContext);
        }
        mLinearLayoutView.setOrientation(LinearLayout.VERTICAL);

        mLinearLayoutView.removeAllViews();

        // set title view
        if(getTitle() != null){
            TextView titleView = new TextView(mContext);
            titleView.setText(getTitle());
            titleView.setTypeface(Typeface.DEFAULT_BOLD);
            titleView.setTextColor(getTitleColor());
            titleView.setTextSize(getTitleFontSize());
            mLinearLayoutView.addView(titleView, linearLayoutParams);
        }

        // set text view
        if(getText() != null){
            TextView textView = new TextView(mContext);
            textView.setText(getText());
            textView.setTextColor(getTextColor());
            textView.setTextSize(getTextFontSize());
            mLinearLayoutView.addView(textView, linearLayoutParams);
        }

        // set a divider, if the debug field actually has any content
        if(getTitle() != null || getText() != null){
            View divider = new View(mContext);
            divider.setMinimumHeight(1);
            divider.setBackground(mContext.getDrawable(R.drawable.white_rectangle_background));
            divider.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
            mLinearLayoutView.addView(divider);
        }

        mLinearLayoutView.invalidate();
    }

    public static class Builder{
        private String mTitle = null;
        private String mText = null;
        private String mTag = null;

        // Sensible defaults
        private int mTitleColor = Color.WHITE;
        private int mTextColor = Color.WHITE;
        private int mTitleFontSize = 12;
        private int mTextFontSize = 9;
        private boolean mShow = true;

        public Builder setTitle(String title){
            mTitle = title;
            return this;
        }

        public Builder setTag(String tag){
            mTag = tag.toLowerCase();
            return this;
        }

        public Builder setText(String text){
            mText = text;
            return this;
        }

        public Builder setTitleColor(int color){
            mTitleColor = color;
            return this;
        }

        public Builder setTitleSize(int fontSize){
            mTitleFontSize = fontSize;
            return this;
        }

        public Builder setTextColor(int color){
            mTextColor = color;
            return this;
        }

        public Builder setTextSize(int fontSize){
            mTextFontSize = fontSize;
            return this;
        }

        public Builder setShow(boolean show){
            mShow = show;
            return this;
        }

        public DebugField build(){
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

}
