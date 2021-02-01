package com.mapsindoors.stdapp.ui.routeoptions.models;

import androidx.annotation.NonNull;

import com.mapsindoors.mapssdk.UserRole;

/**
 * [description]
 *
 * @author Jose J Varó - Copyright © 2020 MapsPeople A/S. All rights reserved.
 * @since 2.1.0
 */
public class UserRoleItem {
    // Visible name
    @NonNull
    public String roleName;
    // Payload
    @NonNull
    public UserRole userRole;

    // Selected item
    public boolean isSelected;


    public UserRoleItem(@NonNull String roleName, @NonNull UserRole userRole, boolean isSelected) {
        this.roleName = roleName;
        this.userRole = userRole;
        this.isSelected = isSelected;
    }
}
