package com.mapsindoors.stdapp.ui.routeoptions.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mapsindoors.mapssdk.MapsIndoors;
import com.mapsindoors.mapssdk.dbglog;
import com.mapsindoors.stdapp.BuildConfig;
import com.mapsindoors.stdapp.R;
import com.mapsindoors.stdapp.managers.UserRolesManager;
import com.mapsindoors.stdapp.ui.routeoptions.models.UserRoleItem;

import java.util.ArrayList;
import java.util.List;

/**
 * [description]
 *
 * @author Jose J Varó - Copyright © 2020 MapsPeople A/S. All rights reserved.
 * @since 2.1.0
 */
public class UserRolesListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    static final String TAG = UserRolesListAdapter.class.getSimpleName();

    static final int TYPE_USER_ROLE_ITEM = 0;

    private UserRolesManager mUserRolesManager;
    private List<UserRoleItem> mItemList;


    public UserRolesListAdapter(@NonNull UserRolesManager userRolesManager, @NonNull List<UserRoleItem> itemList) {
        mUserRolesManager = userRolesManager;
        mItemList = new ArrayList<>(itemList);

        notifyDataSetChanged();
    }


    //region IMPLEMENTS RecyclerView.Adapter
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        if (BuildConfig.DEBUG) {
            if (viewType != TYPE_USER_ROLE_ITEM) {
                dbglog.Assert(false, "viewType must be of type " + TYPE_USER_ROLE_ITEM);
            }
        }

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.control_route_options_user_roles_item, parent, false);
        return new UserRolesListAdapter.UserRolesRoleViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        final UserRoleItem item = mItemList.get(position);

        ((UserRolesRoleViewHolder) holder).setUserRole(item);
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_USER_ROLE_ITEM;
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }
    //endregion


    //region View Holders
    class UserRolesRoleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView userRoleName;
        private ImageButton userRoleSelectButton;


        UserRolesRoleViewHolder(@NonNull View itemView) {
            super(itemView);

            userRoleName = itemView.findViewById(R.id.route_options_user_roles_item_name);
            userRoleSelectButton = itemView.findViewById(R.id.route_options_user_roles_item_button);

            // Set the click listener to the whole item
            itemView.setOnClickListener(this);
        }

        public void setUserRole(@NonNull UserRoleItem userRoleItem) {
            // Set the User Role's localized name
            userRoleName.setText(userRoleItem.roleName);

            // Show/hide the tick icon
            userRoleSelectButton.setVisibility(userRoleItem.isSelected ? View.VISIBLE : View.INVISIBLE);
        }

        @Override
        public void onClick(View v) {
            final UserRoleItem clickedItem = mItemList.get(getLayoutPosition());
            if (clickedItem != null) {
                // Flip the "checkbox" state
                final boolean flippedSelection = clickedItem.isSelected = !clickedItem.isSelected;
                userRoleSelectButton.setVisibility(flippedSelection ? View.VISIBLE : View.INVISIBLE);

                // Remember the change
                mUserRolesManager.saveUserRole(clickedItem.userRole, flippedSelection);

                MapsIndoors.applyUserRoles(mUserRolesManager.getSavedUserRoles());
            }
        }
    }
    //endregion
}
