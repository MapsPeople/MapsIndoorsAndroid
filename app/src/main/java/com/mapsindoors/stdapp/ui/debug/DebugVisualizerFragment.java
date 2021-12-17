package com.mapsindoors.stdapp.ui.debug;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mapsindoors.stdapp.R;
import com.mapsindoors.stdapp.ui.activitymain.MapsIndoorsActivity;
import com.mapsindoors.stdapp.ui.common.fragments.BaseFragment;
import com.mapsindoors.stdapp.ui.debug.adapters.DebugVisualizerAdapter;


public class DebugVisualizerFragment extends BaseFragment {

    private MapsIndoorsActivity mActivity;
    private FrameLayout mParent;
    private final View.OnClickListener mBackBtnClickListener = view -> mParent.setVisibility(View.GONE);
    private Context mContext;
    private ImageButton mBackBtn;
    private DebugVisualizerAdapter mGeneralAdapter;
    private DebugVisualizer mGeneralVisualizer;
    private DebugVisualizerAdapter mPositionAdapter;
    private DebugVisualizer mPositionVisualizer;
    private RecyclerView mRvGeneral;
    private RecyclerView mRvPosition;
    private Switch mGeneralDebugSwitch;
    private Switch mPositionDebugSwitch;

    public DebugVisualizerFragment() {
        //required empty public constructor
    }

    public static DebugVisualizerFragment newInstance() {
        return new DebugVisualizerFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mParent = (FrameLayout) container;
        return inflater.inflate(R.layout.fragment_debug_visualizer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mContext = getContext();
        mActivity = (MapsIndoorsActivity) getActivity();
        mBackBtn = view.findViewById(R.id.debug_fragment_back_button);

        mBackBtn.setOnClickListener(mBackBtnClickListener);

        mGeneralVisualizer = mActivity.getGeneralDebugVisualizer();
        mRvGeneral = view.findViewById(R.id.general_debug_list);
        ConstraintLayout generalLayout = view.findViewById(R.id.debug_fragment_general_frame);
        generalLayout.setVisibility(View.GONE);
        mGeneralAdapter = new DebugVisualizerAdapter(mActivity);
        mRvGeneral.setAdapter(mGeneralAdapter);
        LinearLayoutManager generalLinearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mRvGeneral.setLayoutManager(generalLinearLayoutManager);
        mGeneralAdapter.setVisualizer(mActivity.getGeneralDebugVisualizer());
        mGeneralAdapter.setItems(DebugField.getGeneralFieldsAsList());
        mGeneralDebugSwitch = view.findViewById(R.id.debug_fragment_general_switch);
        mGeneralDebugSwitch.setOnCheckedChangeListener((v, b) -> {
            mGeneralVisualizer.show(b);
            if (b) {
                generalLayout.setVisibility(View.VISIBLE);
            } else {
                generalLayout.setVisibility(View.GONE);
            }

        });

        mPositionVisualizer = mActivity.getPositionProviderDebugVisualizer();
        mRvPosition = view.findViewById(R.id.position_debug_list);
        ConstraintLayout positionLayout = view.findViewById(R.id.debug_fragment_position_frame);
        positionLayout.setVisibility(View.GONE);
        mPositionAdapter = new DebugVisualizerAdapter(mActivity);
        mRvPosition.setAdapter(mPositionAdapter);
        LinearLayoutManager positionLinearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mRvPosition.setLayoutManager(positionLinearLayoutManager);
        mPositionAdapter.setVisualizer(mActivity.getPositionProviderDebugVisualizer());
        mPositionAdapter.setItems(DebugField.getPositionProviderFieldsAsList());
        mPositionDebugSwitch = view.findViewById(R.id.debug_fragment_position_switch);
        mPositionDebugSwitch.setOnCheckedChangeListener((v, b) -> {
            mPositionVisualizer.show(b);
            if (b) {
                positionLayout.setVisibility(View.VISIBLE);
            } else {
                positionLayout.setVisibility(View.GONE);
            }
        });


    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onDestroyView() {
        mBackBtn = null;
        mGeneralDebugSwitch = null;
        mPositionDebugSwitch = null;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mParent.setVisibility(View.GONE);
        super.onDestroy();
    }

}
