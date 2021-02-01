package com.mapsindoors.stdapp.ui.booking;

import android.content.res.Resources;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mapsindoors.stdapp.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class BookingSlotViewHolder extends RecyclerView.ViewHolder {

    private final TextView mTextView;
    private final Button mButton;
    private final ProgressBar mLoadingWheel;

    private BookingSlot mBookingSlot;

    private final int mBookingAvailableColor;
    private final int mBookingUnavailableColor;
    private final int mBookingCancelColor;

    private final String mStrBooking;
    private final String mStrBooked;
    private final String mStrBook;
    private final String mStrCancel;
    private final String mStrCancelling;

    public BookingSlotViewHolder(@NonNull View itemView) {
        super(itemView);

        Resources res = itemView.getContext().getResources();

        mStrBooking = res.getString(R.string.booking_booking);
        mStrBooked = res.getString(R.string.booking_booked);
        mStrBook = res.getString(R.string.booking_book);
        mStrCancel = res.getString(R.string.booking_cancel);
        mStrCancelling = res.getString(R.string.booking_cancelling);

        mBookingAvailableColor = res.getColor(R.color.misdk_colorPrimary);
        mBookingUnavailableColor = res.getColor(R.color.misdk_grey);
        mBookingCancelColor = Color.RED;

        mTextView = itemView.findViewById(R.id.bookingRow_timespan);
        mButton = itemView.findViewById(R.id.bookingRow_button);
        mLoadingWheel = itemView.findViewById(R.id.booking_row_loading_wheel);

        mButton.setOnClickListener(view -> {
                mBookingSlot.getOnBookClickedListener().clicked(mBookingSlot);
                mLoadingWheel.setVisibility(View.VISIBLE);

                if (mBookingSlot.isBooked()){
                    mButton.setText(mStrCancelling);
                    mButton.setTextColor(mBookingUnavailableColor);
                    mButton.setClickable(true);
                } else {
                    mButton.setText(mStrBooking);
                    mButton.setTextColor(mBookingUnavailableColor);
                    mButton.setClickable(true);
                }

        });
    }

    public void bindData(final BookingSlot bookingSlot){
        mBookingSlot = bookingSlot;

        DateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.ROOT);
        String start = dateFormat.format(mBookingSlot.getStart());
        String end = dateFormat.format(mBookingSlot.getEnd());

        String timeSlot = start + " - " + end;
        mTextView.setText(timeSlot);

        mLoadingWheel.setVisibility(View.GONE);

        if(mBookingSlot.isOnlyBookedOnRemote() && !mBookingSlot.isManaged()){
            mButton.setText(mStrBooked);
            mButton.setTextColor(mBookingUnavailableColor);
            mButton.setClickable(false);
        } else if (mBookingSlot.isBooked()){
            mButton.setText(mStrCancel);
            mButton.setTextColor(mBookingCancelColor);
            mButton.setClickable(true);
        } else {
            mButton.setText(mStrBook);
            mButton.setTextColor(mBookingAvailableColor);
            mButton.setClickable(true);
        }
    }

}
