package com.mapsindoors.stdapp.ui.activitymain;

import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mapsindoors.stdapp.R;

/**
 * @author Martin Hansen
 */
public class TopSearchField {
    private ImageButton mClearMapButton;
    private Toolbar mToolbar;
    private TextView mTitle;

    TopSearchField(@NonNull Activity context) {

        mToolbar = context.findViewById(R.id.toolbar);
        mTitle = context.findViewById(R.id.toolbar_title);
        mToolbar.setTitle("");

        mTitle.setText(R.string.app_name);

        mToolbar.setNavigationIcon(R.drawable.ic_menu_white);
        mClearMapButton = context.findViewById(R.id.clear_map_button);

        setToolbarText(null, false);
    }

    void setClearMapButtonClickListener(@NonNull View.OnClickListener listener) {
        mClearMapButton.setOnClickListener(listener);
    }

    //Sets a search text and activates the clear map button.
    //Once exit is pressed onClosePressed will be called.
    public void setToolbarText(@Nullable String newText, boolean clearMapButtonVisibility) {
        mTitle.setText((newText != null) ? newText : "");
        mClearMapButton.setVisibility(clearMapButtonVisibility ? View.VISIBLE : View.INVISIBLE);

        setClearMapButtonVisibility(clearMapButtonVisibility);
    }

    Toolbar getToolbarView() {
        return mToolbar;
    }

    public void setEnabled(boolean enabled) {
        mToolbar.setEnabled(enabled);
    }

    private void setClearMapButtonVisibility(boolean visibility) {
        mClearMapButton.setVisibility(visibility ? View.VISIBLE : View.INVISIBLE);
    }
}
