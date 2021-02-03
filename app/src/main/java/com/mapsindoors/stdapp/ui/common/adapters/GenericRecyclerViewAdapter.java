package com.mapsindoors.stdapp.ui.common.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mapsindoors.stdapp.R;
import com.mapsindoors.stdapp.listeners.IGenericRecyclerViewItemClickedListener;
import com.mapsindoors.stdapp.ui.common.models.GenericRecyclerViewListItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jose J Varó (jjv@mapspeople.com) on 20/Apr/2017.
 */

public class GenericRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = GenericRecyclerViewAdapter.class.getSimpleName();


    public static final int VIEWTYPE_LOCATION = 0;
    public static final int VIEWTYPE_TYPE = 1;
    public static final int VIEWTYPE_CATEGORY = 2;
    public static final int VIEWTYPE_ROUTE = 3;
    public static final int VIEWTYPE_OPENING_HOURS = 4;
    public static final int VIEWTYPE_PHONE = 5;
    public static final int VIEWTYPE_URL = 6;
    public static final int VIEWTYPE_LANGUAGE = 7;
    public static final int VIEWTYPE_VENUE = 8;
    public static final int VIEWTYPE_PLACE = 9;
    public static final int VIEWTYPE_NOT_FOUND = 10;


    private Context mContext;
    private IGenericRecyclerViewItemClickedListener mClickListener;
    private List<GenericRecyclerViewListItem> mItemList;
    private int mItemsViewType;
    private boolean mHasMixedViewTypes;
    private String mTintColor;
    private String mLanguage;
    private String mFeedbackURL;


    public GenericRecyclerViewAdapter(Context context) {
        mContext = context;
        mClickListener = null;
        mItemList = new ArrayList<>();
        mItemsViewType = VIEWTYPE_NOT_FOUND;
        mHasMixedViewTypes = false;
        mTintColor =
                mLanguage =
                        mFeedbackURL = null;
    }


    public void setItems(List<GenericRecyclerViewListItem> items) {
        mItemList.clear();

        int itemsViewType = mItemsViewType = VIEWTYPE_NOT_FOUND;
        boolean hasMixedViewTypes = mHasMixedViewTypes = false;

        if (items != null) {

            boolean gotItems = items.size() > 0;

            if (gotItems) {
                int lastViewType = items.get(0).mViewType;

                for (GenericRecyclerViewListItem item : items) {
                    itemsViewType = item.mViewType;

                    hasMixedViewTypes = itemsViewType != lastViewType;
                    if (hasMixedViewTypes) {
                        break;
                    }
                }

                mItemsViewType = itemsViewType;
                mHasMixedViewTypes = hasMixedViewTypes;

                mItemList.addAll(items);
            }
        }

        notifyDataSetChanged();
    }

    public void setItemClickListener(IGenericRecyclerViewItemClickedListener itemClickedListener) {
        mClickListener = itemClickedListener;
    }

    public void setFeedbackItem(@Nullable String feedbackUrl) {
        if ((mFeedbackURL == null) && (mContext != null)) {

            if (!TextUtils.isEmpty(feedbackUrl)) {
                mFeedbackURL = feedbackUrl;
            }
        }
    }


    //region RecyclerView.Adapter

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (!mHasMixedViewTypes) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());

            switch (viewType) {
                case VIEWTYPE_LOCATION: {
                    View v = inflater.inflate(R.layout.control_mainmenu_twolineitem, parent, false);
                    return new LocationViewHolder(v);
                }

                case VIEWTYPE_TYPE:
                    break;

                case VIEWTYPE_CATEGORY: {
                    View v = inflater.inflate(R.layout.control_mainmenu_category_item, parent, false);
                    return new CategoryViewHolder(v);
                }


                case VIEWTYPE_ROUTE: {
                    View v = inflater.inflate(R.layout.control_mainmenu_item, parent, false);
                    return new RouteViewHolder(v);
                }

                case VIEWTYPE_OPENING_HOURS:
                case VIEWTYPE_PHONE:
                case VIEWTYPE_URL:
                    break;

                case VIEWTYPE_VENUE:
                case VIEWTYPE_PLACE:
                case VIEWTYPE_NOT_FOUND:
                    break;
            }
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (!mHasMixedViewTypes) {
            switch (mItemsViewType) {
                case VIEWTYPE_LOCATION:
                    onBindLocationViewHolder((LocationViewHolder) holder, position);
                    break;
                case VIEWTYPE_TYPE:
                    break;
                case VIEWTYPE_CATEGORY:
                    if (position < mItemList.size())
                        onBindCategoryViewHolder((CategoryViewHolder) holder, position);
                    break;
                case VIEWTYPE_ROUTE:
                    onBindRouteViewHolder((RouteViewHolder) holder, position);
                    break;
                case VIEWTYPE_OPENING_HOURS:
                    break;
                case VIEWTYPE_PHONE:
                    break;
                case VIEWTYPE_URL:
                    break;
                case VIEWTYPE_LANGUAGE:
                    onBindLanguageViewHolder((LanguageViewHolder) holder, position);
                    break;
                case VIEWTYPE_VENUE:
                    break;
                case VIEWTYPE_PLACE:
                    break;
                case VIEWTYPE_NOT_FOUND:
                    break;
            }
        }

    }

    @Override
    public int getItemViewType(int position) {

        return mItemsViewType;

    }

    @Override
    public int getItemCount() {

        if (mItemsViewType == VIEWTYPE_CATEGORY) {
            return mItemList.size() + 1;
        }

        return mItemList.size();


    }
    //endregion


    //region View Holder binders
    private void onBindCategoryViewHolder(CategoryViewHolder holder, int position) {
        GenericRecyclerViewListItem item = mItemList.get(position);

        holder.mTxtTitle.setText(item.mName);

        if (mTintColor == null) {
            setImage(holder.mImageView, holder.mImageViewTint, item.mImg, item.mImgId);
        } else {
            setImage(holder.mImageViewTint, holder.mImageView, item.mImg, item.mImgId);
        }
    }

    private void onBindLocationViewHolder(LocationViewHolder holder, int position) {
        GenericRecyclerViewListItem item = mItemList.get(position);

        holder.mTxtTitle.setText(item.mName);
        holder.mTxtSubTitle.setText(item.mSubText);

        if (mTintColor == null) {
            setImage(holder.mImageView, holder.mImageViewTint, item.mImg, item.mImgId);
        } else {
            setImage(holder.mImageViewTint, holder.mImageView, item.mImg, item.mImgId);
        }
    }

    private void onBindLanguageViewHolder(LanguageViewHolder holder, int position) {
        GenericRecyclerViewListItem item = mItemList.get(position);

        holder.mTxtTitle.setText(item.mName);

        @IdRes int radioImage = mLanguage.contentEquals((String) item.mObj)
                ? android.R.drawable.radiobutton_on_background
                : android.R.drawable.radiobutton_off_background;

        setImage(holder.mImageView, holder.mImageView, item.mImg, item.mImgId);
        setImage(holder.mSelectorView, holder.mSelectorView, null, radioImage);
    }

    private void onBindRouteViewHolder(RouteViewHolder holder, int position) {
        GenericRecyclerViewListItem item = mItemList.get(position);

        holder.mTxtTitle.setText(item.mName);

        if (mTintColor == null) {
            setImage(holder.mImageView, holder.mImageViewTint, item.mImg, item.mImgId);
        } else {
            setImage(holder.mImageViewTint, holder.mImageView, item.mImg, item.mImgId);
        }
    }
    //endregion

    private void setImage(ImageView visible, ImageView invisible, Bitmap img, @DrawableRes int imgId) {
        invisible.setVisibility(View.INVISIBLE);
        visible.setVisibility(View.VISIBLE);

        if (img != null) {
            visible.setImageBitmap(img);
        } else {
            visible.setImageResource(imgId);
        }
    }

    //region View Holders
    private class CategoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView mTxtTitle;
        ImageView mImageView, mImageViewTint; //, mStrokeImageView;

        CategoryViewHolder(View itemView) {
            super(itemView);

            mTxtTitle = itemView.findViewById(R.id.cat_textitem);
            mImageView = itemView.findViewById(R.id.cat_iconitem);
            mImageViewTint = itemView.findViewById(R.id.cat_iconitem_tint);

            // set the click listener to the above item's parent
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if ((mClickListener != null) && (mItemList != null)) {
                int lPos = getLayoutPosition();
                if (mItemList.size() > lPos) {
                    mClickListener.OnGenericRVItemClicked(mItemList.get(lPos));
                }
            }
        }
    }

    private class LocationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView mTxtTitle, mTxtSubTitle;
        ImageView mImageView, mImageViewTint;


        LocationViewHolder(View itemView) {
            super(itemView);

            mTxtTitle = itemView.findViewById(R.id.ctrl_mainmenu_textitem_main);
            mTxtSubTitle = itemView.findViewById(R.id.ctrl_mainmenu_textitem_sub);
            mImageView = itemView.findViewById(R.id.ctrl_mainmenu_iconitem);
            mImageViewTint = itemView.findViewById(R.id.ctrl_mainmenu_iconitem_tint);

            // set the click listener to the above item's parent
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if ((mClickListener != null) && (mItemList != null)) {
                int lPos = getLayoutPosition();
                if (mItemList.size() > lPos) {
                    mClickListener.OnGenericRVItemClicked(mItemList.get(lPos));
                }
            }
        }
    }

    private class LanguageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView mTxtTitle;
        ImageView mImageView;
        ImageView mSelectorView;

        LanguageViewHolder(View itemView) {
            super(itemView);

            mTxtTitle = itemView.findViewById(R.id.ctrl_mainmenu_textitem);
            mImageView = itemView.findViewById(R.id.flagimage);
            mSelectorView = itemView.findViewById(R.id.selector);

            // set the click listener to the above item's parent
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if ((mClickListener != null) && (mItemList != null)) {
                int lPos = getLayoutPosition();
                if (mItemList.size() > lPos) {
                    mClickListener.OnGenericRVItemClicked(mItemList.get(lPos));
                }
            }
        }
    }

    private class RouteViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView mTxtTitle;
        ImageView mImageView, mImageViewTint;

        RouteViewHolder(View itemView) {
            super(itemView);

            mTxtTitle = itemView.findViewById(R.id.ctrl_mainmenu_textitem);
            mImageView = itemView.findViewById(R.id.ctrl_mainmenu_iconitem);
            mImageViewTint = itemView.findViewById(R.id.ctrl_mainmenu_iconitem_tint);

            // set the click listener to the above item's parent
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if ((mClickListener != null) && (mItemList != null)) {
                int lPos = getLayoutPosition();
                if (mItemList.size() > lPos) {
                    mClickListener.OnGenericRVItemClicked(mItemList.get(lPos));
                }
            }
        }
    }
    //endregion
}
