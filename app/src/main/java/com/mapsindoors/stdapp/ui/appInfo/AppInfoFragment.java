package com.mapsindoors.stdapp.ui.appInfo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mapsindoors.mapssdk.MapsIndoors;
import com.mapsindoors.mapssdk.dbglog;
import com.mapsindoors.stdapp.BuildConfig;
import com.mapsindoors.stdapp.R;
import com.mapsindoors.stdapp.managers.AppConfigManager;
import com.mapsindoors.stdapp.positionprovider.AppPositionProvider;
import com.mapsindoors.stdapp.ui.activitymain.MapsIndoorsActivity;
import com.mapsindoors.stdapp.ui.appInfo.adapters.AppInfoAdapter;
import com.mapsindoors.stdapp.ui.appInfo.models.CreditItem;
import com.mapsindoors.stdapp.ui.common.enums.MenuFrame;
import com.mapsindoors.stdapp.ui.common.fragments.BaseFragment;
import com.mapsindoors.stdapp.ui.debug.DebugVisualizer;
import com.mapsindoors.stdapp.ui.debug.DebugVisualizerFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * AppInfoFragment
 * MapsIndoorsDemo
 * <p>
 * Created by Amine on 15/11/2017.
 * Copyright © 2017 MapsPeople A/S. All rights reserved.
 */
public class AppInfoFragment extends BaseFragment {
    private static final String TAG = AppInfoFragment.class.getSimpleName();

    private static final int FLIPPER_LIST_ITEMS = 0;

    private List<CreditItem> mCreditList;
    private Context mContext;
    private MapsIndoorsActivity mActivity;
    private RecyclerView mCreditsListRecyclerView;
    private ViewFlipper mViewFlipper;
    private AppInfoAdapter mRecyclerViewAdapter;
    private DebugVisualizerFragment mDebugVisualizerFragment;

    TextView providerTextView;
    TextView appVersiontextView;
    TextView sdkVersionTextview;
    TextView positionProviderNameTextView;
    TextView positionProviderVersionTextView;
    View mapsPeopleASLayout;
    View mFeedbackLayout;
    ImageButton debugVisualizerMenu;


    //region Fragment lifecycle events


    private AppConfigManager appConfigManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_app_info, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMainView = view;
        mFragment = MenuFrame.MENU_FRAME_APP_INFO;

        providerTextView = view.findViewById(R.id.app_info_provider_name);
        appVersiontextView = view.findViewById(R.id.app_info_app_version);
        sdkVersionTextview = view.findViewById(R.id.app_info_sdk_version);
        positionProviderNameTextView = view.findViewById(R.id.app_info_positioning_provider);
        positionProviderVersionTextView = view.findViewById(R.id.app_info_positioning_version);
        mapsPeopleASLayout = view.findViewById(R.id.app_info_maps_people_as_layout);
        mFeedbackLayout = view.findViewById(R.id.feedback_layout);
        debugVisualizerMenu = view.findViewById(R.id.app_info_debug_visualizer_imageButton);

    }
    //endregion


    public void init(@NonNull Context context) {
        mContext = (mContext != null) ? mContext : context;
        mActivity = (mActivity != null) ? mActivity : (MapsIndoorsActivity) context;

        appConfigManager = mActivity.getAppConfigManager();

        //
        sdkVersionTextview.setText(MapsIndoors.getSDKVersion());
        //
        appVersiontextView.setText(getAppVersion());

        mDebugVisualizerFragment = DebugVisualizerFragment.newInstance();

        //
        View backBtn = mMainView.findViewById(R.id.app_info_back_button);
        backBtn.setOnClickListener(v -> close(mActivity));

        mapsPeopleASLayout.setOnClickListener(view -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.app_supplier_website)));
            mContext.startActivity(browserIntent);
        });

        debugVisualizerMenu.setOnClickListener(v -> {
            FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            FrameLayout frameLayout = mActivity.findViewById(R.id.debug_fragment_container);
            frameLayout.setVisibility(View.VISIBLE);

            fragmentTransaction.replace(frameLayout.getId(), mDebugVisualizerFragment).addToBackStack("DEBUG_FRAGMENT").commit();

        });

        appVersiontextView.setOnLongClickListener(v -> {
            if (debugVisualizerMenu.getVisibility() == View.GONE) {
                debugVisualizerMenu.setVisibility(View.VISIBLE);
            } else {
                debugVisualizerMenu.setVisibility(View.GONE);
            }
            return true;
        });

        if (appConfigManager != null) {
            final String feedbackURL = appConfigManager.getFeedbackUrl();

            if (!TextUtils.isEmpty(feedbackURL)) {
                try {
                    Uri feedbackURI = Uri.parse(feedbackURL);
                    //
                    mFeedbackLayout.setOnClickListener(view -> {

                        Intent intent = new Intent(Intent.ACTION_VIEW, feedbackURI);

                        try {
                            mContext.startActivity(intent);
                        } catch (Exception ex) {
                            if (BuildConfig.DEBUG) {
                                Toast.makeText(mContext, TAG + " - Parsing the feedback URI Error, check the log!!!", Toast.LENGTH_SHORT).show();
                                dbglog.LogE(TAG, "Parsing the feedback URI Error:\n" + ex.toString());
                            }
                        }
                    });
                    //
                    mFeedbackLayout.setVisibility(View.VISIBLE);

                } catch (Exception ex) {

                    if (BuildConfig.DEBUG) {
                        Toast.makeText(mContext, TAG + " - Parsing the feedback URI Error, check the log!!!", Toast.LENGTH_SHORT).show();
                        dbglog.LogE(TAG, "Parsing the feedback URI Error:\n" + ex.toString());
                    }
                }
            }
        }

        //
        providerTextView.setOnClickListener(view -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.app_provider_website)));
            mContext.startActivity(browserIntent);
        });

        //
        setupViewFlipper(mMainView);
        setupListView(mMainView);
    }

    //region List
    private void setupViewFlipper(View view) {
        mViewFlipper = view.findViewById(R.id.transport_sources_viewflipper);
    }

    private void setupListView(View view) {
        mContext = (mContext != null) ? mContext : getContext();
        mActivity = (mActivity != null) ? mActivity : (MapsIndoorsActivity) mContext;
        mCreditsListRecyclerView = view.findViewById(R.id.credit_list);
        mCreditsListRecyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setSmoothScrollbarEnabled(false);
        layoutManager.setAutoMeasureEnabled(true);
        mCreditsListRecyclerView.setLayoutManager(layoutManager);

        initCreditList();
        mRecyclerViewAdapter = new AppInfoAdapter(mContext, mCreditList);
        mCreditsListRecyclerView.setAdapter(mRecyclerViewAdapter);
    }

    /**
     * @param AppInfoCreditList
     */
    public void setList(@NonNull List<CreditItem> AppInfoCreditList) {
        List<CreditItem> taItems = new ArrayList<>(AppInfoCreditList.size());

        for (CreditItem credit : AppInfoCreditList) {
            taItems.add(new CreditItem(credit.name, credit.url));
        }

        mRecyclerViewAdapter.setItems(taItems);

        updateViewFlipper();
    }

    private void updateViewFlipper() {
        if (mViewFlipper.getDisplayedChild() != FLIPPER_LIST_ITEMS) {
            mViewFlipper.setDisplayedChild(FLIPPER_LIST_ITEMS);
        }
    }
    //endregion

    //endregion


    String getAppVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @UiThread
    public void setPositionProvider(AppPositionProvider positionProvider) {
        if (positionProvider != null) {
            positionProviderNameTextView.setText(positionProvider.getName());
            positionProviderVersionTextView.setText(positionProvider.getVersion());
        }
    }

    void initCreditList() {
        String[] libraries = getResources().getStringArray(R.array.credits);
        String[] licences = getResources().getStringArray(R.array.licence);

        int libCount = libraries.length;
        mCreditList = new ArrayList<>(libCount);

        for (int i = 0; i < libCount; i++) {
            mCreditList.add(new CreditItem(libraries[i], licences[i]));
        }
    }
}
