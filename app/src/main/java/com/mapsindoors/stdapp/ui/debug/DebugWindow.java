package com.mapsindoors.stdapp.ui.debug;

import android.content.Context;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.Shape;

import com.mapsindoors.stdapp.R;
import com.mapsindoors.stdapp.ui.activitymain.MapsIndoorsActivity;

import java.util.List;

public class DebugWindow {
    private int padding;
    private int paddingTop;
    private int paddingBottom;
    private int paddingLeft;
    private int paddingRight;

    private int cornerRadius;
    private int cornerRadiusTopLeft;
    private int cornerRadiusTopRight;
    private int cornerRadiusBottomLeft;
    private int cornerRadiusBottomRight;

    private String color;
    private ShapeDrawable drawable;

    private List<DebugField> fields;

    public DebugWindow(MapsIndoorsActivity context){
        //this.drawable = (ShapeDrawable) context.findViewById(R.drawable.debug_window);
    }

    public void setFields(List<DebugField> fields) {
        this.fields = fields;
    }
}
