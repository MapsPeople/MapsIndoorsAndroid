package com.mapsindoors.stdapp.managers;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mapsindoors.mapssdk.MapsIndoors;
import com.mapsindoors.mapssdk.UserRole;
import com.mapsindoors.stdapp.helpers.SharedPrefsHelper;
import com.mapsindoors.stdapp.ui.activitymain.MapsIndoorsActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Handles
 *
 * @author Jose J Varó - Copyright © 2020 MapsPeople A/S. All rights reserved.
 * @since 2.1.0
 */
public class UserRolesManager {
    static final String TAG = UserRolesManager.class.getSimpleName();


    @NonNull
    private Context mContext;
    @NonNull
    private MapsIndoorsActivity mActivity;
    @NonNull
    private Set<String> mSelectedUserRoles;
    private boolean isQueued = false;


    public UserRolesManager(@NonNull Context context) {
        mContext = context;
        mActivity = (MapsIndoorsActivity) context;
        mSelectedUserRoles = SharedPrefsHelper.getSelectedUserRoles(mContext);

        updateSelectedUserRoles();
    }

    /**
     * Checks that the saved user roles are still available
     */
    private void updateSelectedUserRoles() {
        final List<UserRole> cmsUserRoles = MapsIndoors.getUserRoles();

        // If by any chance, there are no rules, delete all saved...
        if (cmsUserRoles == null) {
            clearSavedUserRoles();
            return;
        }

        final List<String> savedRolesNotFoundInCMSData = new ArrayList<>(mSelectedUserRoles.size());

        for (final String savedUserRoleId : mSelectedUserRoles) {
            boolean delRole = true;
            for (final UserRole cmsUserRole : cmsUserRoles) {
                final String cmsUserRoleId = cmsUserRole.getKey();
                if (savedUserRoleId.contentEquals(cmsUserRoleId)) {
                    delRole = false;
                    break;
                }
            }

            if (delRole) {
                savedRolesNotFoundInCMSData.add(savedUserRoleId);
            }
        }

        // Delete saved ids not found in the cmsUserRoles
        if (!savedRolesNotFoundInCMSData.isEmpty()) {
            for (final String invalidSavedId : savedRolesNotFoundInCMSData) {
                mSelectedUserRoles.remove(invalidSavedId);
            }

            SharedPrefsHelper.setSelectedUserRoles(mContext, mSelectedUserRoles);
        }
        if (MapsIndoors.isReady()) {
            alignUserRolesWithSdk();
        } else {
            addReadyListener();
        }

    }

    private synchronized void addReadyListener() {
        if (!isQueued) {
            isQueued = true;
            MapsIndoors.addOnMapsIndoorsReadyListener(this::alignUserRolesWithSdk);
        }
    }


    private void alignUserRolesWithSdk() {
        List<UserRole> appliedUserRoles = MapsIndoors.getAppliedUserRoles();
        List<UserRole> savedUserRoles = getSavedUserRoles();
        if ((appliedUserRoles == null || appliedUserRoles.isEmpty()) && savedUserRoles != null) {
            MapsIndoors.applyUserRoles(savedUserRoles);
        }else if (appliedUserRoles != null && savedUserRoles != null) {
            if (appliedUserRoles.size() != savedUserRoles.size()) {
                MapsIndoors.applyUserRoles(savedUserRoles);
            }
        }
        isQueued = false;
    }

    /**
     * Checks if there are "user roles" available for the current user in the current solution..
     *
     * @return {@code true} if there are any user roles available, {@code false} otherwise
     */
    public static boolean hasUserRoles() {
        final List<UserRole> userRoles = MapsIndoors.getUserRoles();
        return (userRoles != null) && !userRoles.isEmpty();
    }

    /**
     * Gets the available user roles for the current user + solution
     *
     * @return If present, a list of {@link UserRole} objects. {@code null} otherwise
     */
    @Nullable
    public List<UserRole> getAvailableUserRoles() {
        return MapsIndoors.getUserRoles();
    }

    public void saveUserRole(@Nullable UserRole userRole, boolean save) {
        if (userRole == null) {
            return;
        }

        final String userRoleId = userRole.getKey();

        // Update the manager's ref list
        if (save) {
            mSelectedUserRoles.add(userRoleId);
        } else {
            mSelectedUserRoles.remove(userRoleId);
        }

        SharedPrefsHelper.setSelectedUserRoles(mContext, mSelectedUserRoles);
    }

    /**
     * Gets a list of saved user roles, if any
     *
     * @return
     */
    @Nullable
    public List<UserRole> getSavedUserRoles() {
        final Set<String> selectedUserRoles = SharedPrefsHelper.getSelectedUserRoles(mContext);
        final List<UserRole> cmsUserRoles = MapsIndoors.getUserRoles();
        if (selectedUserRoles.isEmpty() || (cmsUserRoles == null) || cmsUserRoles.isEmpty()) {
            updateDebugVisualizer(null);
            return null;
        }

        final List<UserRole> savedRoles = new ArrayList<>(selectedUserRoles.size());

        for (final String selectedUserRole : selectedUserRoles) {
            for (final UserRole cmsUserRole : cmsUserRoles) {
                if (selectedUserRole.contentEquals(cmsUserRole.getKey())) {
                    savedRoles.add(cmsUserRole);
                }
            }
        }
        updateDebugVisualizer(savedRoles);
        return savedRoles;
    }

    public boolean isUserRoleSaved(@NonNull UserRole userRole) {
        return mSelectedUserRoles.contains(userRole.getKey());
    }

    /**
     * Used to update UserRole Status for the debugVisualizer
     * @param savedUserRoles a list of current userRoles
     */
    public void updateDebugVisualizer(List<UserRole> savedUserRoles) {
        StringBuilder userRoleStringBuilder = new StringBuilder();

        if (savedUserRoles != null) {
            for (UserRole role : savedUserRoles)  {
                userRoleStringBuilder.append(role.getValue()).append("\n");
            }
        } else {
            userRoleStringBuilder.append("-");
        }

        mActivity.getGeneralDebugVisualizer().updateDebugField("userRoles", userRoleStringBuilder.toString());
    }


    /**
     * Clears any saved users, both from the local list and from the shared prefs
     */
    void clearSavedUserRoles() {
        mSelectedUserRoles = new HashSet<>(0);
        SharedPrefsHelper.setSelectedUserRoles(mContext, mSelectedUserRoles);
    }

    public void updateFromDataUpdate() {
        updateSelectedUserRoles();
    }
}
