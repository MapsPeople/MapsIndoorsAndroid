package com.mapsindoors.stdapp.ui.booking.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mapsindoors.stdapp.R;
import com.mapsindoors.stdapp.ui.booking.BookingSlot;
import com.mapsindoors.stdapp.ui.booking.BookingSlotViewHolder;

import java.util.List;

public class BookingServiceListAdapter extends RecyclerView.Adapter {

    private List<BookingSlot> mBookingSlots;

    public BookingServiceListAdapter(List<BookingSlot> bookingSlots){
        mBookingSlots = bookingSlots;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return new BookingSlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((BookingSlotViewHolder) holder).bindData(mBookingSlots.get(position));
    }

    @Override
    public int getItemViewType(final int position) {
        return R.layout.booking_row_view;
    }

    @Override
    public int getItemCount() {
        return mBookingSlots.size();
    }

}
