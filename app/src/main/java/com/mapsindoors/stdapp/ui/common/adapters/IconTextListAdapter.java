package com.mapsindoors.stdapp.ui.common.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mapsindoors.mapssdk.dbglog;
import com.mapsindoors.stdapp.BuildConfig;
import com.mapsindoors.stdapp.R;
import com.mapsindoors.stdapp.helpers.MapsIndoorsUtils;
import com.mapsindoors.stdapp.ui.common.models.IconTextElement;

import java.util.ArrayList;
import java.util.List;


public class IconTextListAdapter extends ArrayAdapter<String> {
    private static final String TAG = IconTextListAdapter.class.getSimpleName();

    private List<IconTextElement> itemList;
    private Context context;
    private String tintColor = null;

    public enum Objtype {
        LOCATION, TYPE, CATEGORY, ROUTE, OPENINGHOURS, PHONE, URL, LANGUAGE, VENUE, PLACE, MESSAGE
    }

    public IconTextListAdapter(Context context) {
        super(context, R.layout.control_mainmenu_item);
        this.context = context;
    }

    public void setTint(String tintColor) {
        this.tintColor = tintColor;
    }

    public void setList(List<IconTextElement> itemList) {
        clear();

        this.itemList = itemList;

        ArrayList<String> collectionList = new ArrayList<>(itemList.size());
        for (IconTextElement element : itemList)
            collectionList.add(element.getName());
        addAll(collectionList);
    }

    public void addToList(IconTextElement newElement) {
        itemList.add(newElement);
        add(newElement.toString());
    }


    public Object getItemObj(int index) {
        if (BuildConfig.DEBUG) {
            if (index >= itemList.size()) {
                dbglog.Log(TAG, "oob");
            }
        }

        return itemList.get(index).getObj();
    }

    public Objtype getObjType(int index) {
        return itemList.get(index).getObjType();
    }

    @Override
    public View getView(int index, View view, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        IconTextElement element = itemList.get(index);

        if (element.getObjType() == Objtype.VENUE) {
            TextView single_row = new TextView(context);
            single_row.setText(element.getName());
            single_row.setFocusable(false);
            return single_row;

        } else if (element.getObjType() == Objtype.LOCATION || element.getObjType() == Objtype.ROUTE || element.getObjType() == Objtype.MESSAGE) {

            View dual_row = inflater.inflate(R.layout.control_mainmenu_twolineitem, null, true);

            TextView txtTitleMain = dual_row.findViewById(R.id.ctrl_mainmenu_textitem_main);
            TextView txtTitleSub = dual_row.findViewById(R.id.ctrl_mainmenu_textitem_sub);
            TextView txtDistance = dual_row.findViewById(R.id.distanceTextView);

            txtTitleMain.setText(element.getName());

            boolean showSubText = element.getSubText() != null;

            if (showSubText) {
                txtTitleSub.setText(element.getSubText());
            } else {
                txtTitleSub.setVisibility(View.GONE);
            }

            if (element.getDistText() != null && !element.getDistText().equals("0 m") && !element.getDistText().equals("0 ft")) {
                txtDistance.setVisibility(View.VISIBLE);
                txtDistance.setText(element.getDistText());
            }

            if (element.getObjType() == Objtype.ROUTE) {

                MapsIndoorsUtils.setTextAppearance(getContext(), txtTitleMain, android.R.style.TextAppearance_DeviceDefault_Medium);
                if (showSubText) {
                    MapsIndoorsUtils.setTextAppearance(getContext(), txtTitleSub, android.R.style.TextAppearance_DeviceDefault_Small);
                }

                txtTitleMain.setTextColor(ContextCompat.getColor(context, R.color.black));
            }

            ImageView imageView = dual_row.findViewById(R.id.ctrl_mainmenu_iconitem);
            ImageView imageViewTint = dual_row.findViewById(R.id.ctrl_mainmenu_iconitem_tint);

            if (tintColor == null) {
                setImage(imageView, imageViewTint, element.getImg(), element.getImageId());
            } else {
                setImage(imageViewTint, imageView, element.getImg(), element.getImageId());
            }

            dual_row.setFocusable(false);
            return dual_row;

        } else {
            View single_row = inflater.inflate(R.layout.control_mainmenu_category_item, null, true);

            TextView txtTitle = single_row.findViewById(R.id.cat_textitem);
            ImageView imageView = single_row.findViewById(R.id.cat_iconitem);
            ImageView imageViewTint = single_row.findViewById(R.id.cat_iconitem_tint);

            txtTitle.setText(element.getName());
            if (tintColor == null) {
                setImage(imageView, imageViewTint, element.getImg(), element.getImageId());
            } else {
                setImage(imageViewTint, imageView, element.getImg(), element.getImageId());
            }

            single_row.setFocusable(false);
            return single_row;
        }
    }

    private void setImage(ImageView visible, ImageView invisible, Bitmap img, Integer imgId) {
        invisible.setVisibility(View.INVISIBLE);
        visible.setVisibility(View.VISIBLE);
        if (img != null) {
            visible.setImageBitmap(img);
        } else {
            visible.setImageResource(imgId);
        }
    }
}