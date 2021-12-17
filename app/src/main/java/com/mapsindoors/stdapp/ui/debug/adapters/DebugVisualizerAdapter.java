package com.mapsindoors.stdapp.ui.debug.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mapsindoors.stdapp.R;
import com.mapsindoors.stdapp.ui.debug.DebugField;
import com.mapsindoors.stdapp.ui.debug.DebugVisualizer;

import java.util.ArrayList;
import java.util.List;

public class DebugVisualizerAdapter extends RecyclerView.Adapter<DebugVisualizerAdapter.DebugViewHolder> {


    Context mContext;
    List<DebugField> mFields = new ArrayList<>();
    DebugVisualizer mVisualizer;

    public DebugVisualizerAdapter(Context context) {
        mContext = context;
    }

    public void setItems(List<DebugField> items) {
        mFields.addAll(items);
        notifyDataSetChanged();
    }

    public void setVisualizer(DebugVisualizer visualizer) {
        mVisualizer = visualizer;
    }


    @NonNull
    @Override
    public DebugViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.debug_field_item, parent, false);
        return new DebugViewHolder(view);
    }

    public void onBindViewHolder(@NonNull DebugViewHolder debugViewHolder, int position) {
        DebugField field = mFields.get(position);
        if (field != null) {
            debugViewHolder.bindView(field, mVisualizer);
        }
    }

    @Override
    public int getItemCount() {
        return mFields.size();
    }

    class DebugViewHolder extends RecyclerView.ViewHolder {

        private final TextView mTextView;
        private final Switch mSwitch;

        DebugViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.debug_field_item_text_view);
            mSwitch = itemView.findViewById(R.id.debug_field_item_switch);
        }

        void bindView(@NonNull DebugField debugField, DebugVisualizer debugVisualizer) {

            mTextView.setText(debugField.getTitle());

            mSwitch.setChecked(debugField.isShown());

            mSwitch.setOnCheckedChangeListener((v, b) -> {
                debugVisualizer.setShowDebugField(debugField.getTag(), b);
            });
        }
    }
}
