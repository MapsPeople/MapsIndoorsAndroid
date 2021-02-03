package com.mapsindoors.stdapp.ui.routeoptions;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mapsindoors.mapssdk.UserRole;
import com.mapsindoors.stdapp.R;
import com.mapsindoors.stdapp.managers.UserRolesManager;
import com.mapsindoors.stdapp.ui.activitymain.MapsIndoorsActivity;
import com.mapsindoors.stdapp.ui.common.fragments.BaseFragment;
import com.mapsindoors.stdapp.ui.common.enums.MenuFrame;
import com.mapsindoors.stdapp.ui.routeoptions.adapters.UserRolesListAdapter;
import com.mapsindoors.stdapp.ui.routeoptions.models.UserRoleItem;

import java.util.ArrayList;
import java.util.List;

/**
 * [description]
 *
 * @author Jose J Varó - Copyright © 2020 MapsPeople A/S. All rights reserved.
 * @since 2.1.0
 */
public class RouteOptionsFragment extends BaseFragment {
    static final String TAG = RouteOptionsFragment.class.getSimpleName();


    MapsIndoorsActivity mActivity;

    View mBackButton;
    RecyclerView mUserRolesRecyclerView;
    UserRolesListAdapter mRecyclerViewAdapter;


    //region Fragment lifecycle events
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_route_options, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mMainView = view;

        mUserRolesRecyclerView = view.findViewById(R.id.route_options_user_roles_group_users);
        mBackButton = view.findViewById(R.id.route_options_back_button);

        if (mBackButton != null) {
            mBackButton.setOnClickListener(v -> close());
        }
    }
    //endregion


    //region BASEFRAGMENT OVERRIDES
    @Override
    public boolean onBackPressed() {
        close();
        return true;
    }

    @Override
    public void willOpen(final MenuFrame fromFrame) {
        updateUserRoleList();
    }

    @Override
    public void onDataUpdated() {
        updateUserRoleList();
    }
    //endregion


    public void init(@Nullable Context context) {
        mActivity = (MapsIndoorsActivity) context;

        setupLists();
    }

    void setupLists() {
        setupUserRolesList();
    }

    void setupUserRolesList() {
        mUserRolesRecyclerView.setHasFixedSize(true);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(mActivity);
        layoutManager.setSmoothScrollbarEnabled(false);
        if (!layoutManager.isAutoMeasureEnabled()) {
            layoutManager.setAutoMeasureEnabled(true);
        }

        mUserRolesRecyclerView.setLayoutManager(layoutManager);

        updateUserRoleList();
    }

    void updateUserRoleList() {
        if (mActivity == null) {
            return;
        }

        final UserRolesManager userRolesManager = mActivity.getUserRolesManager();

        if (userRolesManager != null) {
            // Available user roles for the current user-solution
            final List<UserRole> availableUserRoles = userRolesManager.getAvailableUserRoles();
            if (availableUserRoles != null) {

                // UI list items
                final List<UserRoleItem> userRoleItems = new ArrayList<>(availableUserRoles.size());

                for (final UserRole availableUserRole : availableUserRoles) {
                    if (availableUserRole != null) {

                        final boolean isSaved = userRolesManager.isUserRoleSaved(availableUserRole);

                        userRoleItems.add(new UserRoleItem(availableUserRole.getValue(), availableUserRole, isSaved));
                    }
                }

                mRecyclerViewAdapter = new UserRolesListAdapter(userRolesManager, userRoleItems);
                mUserRolesRecyclerView.setAdapter(mRecyclerViewAdapter);
            }
        }
    }

    public void close() {
        mActivity.menuGoBack();
    }
}
