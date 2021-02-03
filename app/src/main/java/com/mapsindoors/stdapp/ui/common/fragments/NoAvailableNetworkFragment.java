package com.mapsindoors.stdapp.ui.common.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mapsindoors.stdapp.R;


public class NoAvailableNetworkFragment extends Fragment {


    //UI component
    private View mRootView;
    private TextView mSettingsLayout;

    public NoAvailableNetworkFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_no_available_network, container, false);

        setupUI();

        return mRootView;
    }


    void setupUI() {
        mSettingsLayout = mRootView.findViewById(R.id.no_network_setting_text);

        mSettingsLayout.setOnClickListener(view -> {
            final Context ctx = getContext();
            if (ctx != null) {
                ctx.startActivity(
                        isAirplaneModeOn(ctx)
                                ? new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS)
                                : new Intent(Settings.ACTION_WIRELESS_SETTINGS)
                );
            }
        });
    }

    private static boolean isAirplaneModeOn(@NonNull Context context) {
        return Settings.System.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }
}
