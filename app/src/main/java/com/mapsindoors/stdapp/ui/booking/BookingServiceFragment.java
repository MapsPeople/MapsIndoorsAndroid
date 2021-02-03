package com.mapsindoors.stdapp.ui.booking;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mapsindoors.mapssdk.MPBooking;
import com.mapsindoors.mapssdk.MPBookingService;
import com.mapsindoors.mapssdk.MPBookingsQuery;
import com.mapsindoors.mapssdk.MPLocation;
import com.mapsindoors.mapssdk.OnLoadingDataReadyListener;
import com.mapsindoors.stdapp.R;
import com.mapsindoors.stdapp.ui.booking.adapters.BookingServiceListAdapter;
import com.mapsindoors.stdapp.ui.common.fragments.BaseFragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BookingServiceFragment extends BaseFragment {

    private MPLocation mLocation;

    private BookingServiceListAdapter mAdapter;

    // False = 30m, true = 60m
    private boolean mTimespanSwitchState;
    private Switch mTimespanSelectorSwitch;

    private List<BookingSlot> mTimeSlots;

    public BookingServiceFragment(){
        mTimeSlots = new ArrayList<>();
    }

    @Override
    public void onCreate( @Nullable Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState ) {
        View view = inflater.inflate( R.layout.fragment_booking, container);
        // Setting up recycler view
        mAdapter = new BookingServiceListAdapter(mTimeSlots);
        RecyclerView mRecyclerView = view.findViewById(R.id.booking_slots_recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setAdapter(mAdapter);

        mTimespanSelectorSwitch = view.findViewById(R.id.booking_timespan_switch);
        mTimespanSelectorSwitch.setOnClickListener(view1 -> {
            mTimespanSwitchState = mTimespanSelectorSwitch.isChecked();
            mTimeSlots.clear();
            updateUI();
            mTimeSlots = computeTimeSlots(mTimeSlots, mTimespanSwitchState);
            syncBookingState(mLocation);
        });

        return view;
    }

    @Override
    public void onViewCreated( @NonNull View view, @Nullable Bundle savedInstanceState ) {
        super.onViewCreated(view, savedInstanceState);
        mMainView = view;

        mTimespanSwitchState = mTimespanSelectorSwitch.isChecked();
        mTimeSlots = computeTimeSlots(mTimeSlots, mTimespanSwitchState);
        syncBookingState(mLocation);
    }

    public void setLocation(MPLocation location){
        mLocation = location;
        if(mLocation.getBookable()){
            this.getView().setVisibility(View.VISIBLE);
        } else {
            this.getView().setVisibility(View.GONE);
        }

        mTimeSlots = computeTimeSlots(mTimeSlots, mTimespanSwitchState);

        syncBookingState(mLocation);
    }

    private List<BookingSlot> computeTimeSlots(List<BookingSlot> timeSlots, boolean timespanSwitchState){
        timeSlots.clear();

        int intervalInMinutes = timespanSwitchState ? 60 : 30;
        final int dayStartHour = 8;
        final int dayEndHour = 18;

        Date now = new Date(System.currentTimeMillis());

        // Determine where our time cursor should start from (last half or whole hour - or tomorrow morning)
        if(now.getHours() <= dayEndHour){
            // Today
            if(now.getHours() < dayStartHour){
                // If before 8, fast forward and start from 8
                now.setHours(dayStartHour);
                now.setMinutes(0);
                now.setSeconds(0);
            } else {
                // set to last half or whole hour (depending on the state of the toggle switch)
                if(intervalInMinutes == 30){
                    if(now.getMinutes() < 30){
                        now.setMinutes(0);
                    } else {
                        now.setMinutes(30);
                    }
                } else {
                    now.setMinutes(0);
                }
                now.setSeconds(0);
            }
        } else {
            // Tomorrow, starting from 8
            now.setDate(now.getDate() + 1);
            now.setHours(dayStartHour);
            now.setMinutes(0);
            now.setSeconds(0);
        }

        long timeCursor = now.getTime();

        Date breakCondition = new Date(timeCursor);
        breakCondition.setHours(dayEndHour);
        breakCondition.setMinutes(0);
        breakCondition.setSeconds(0);

        while(timeCursor < breakCondition.getTime()){
            Date start = new Date(timeCursor);
            timeCursor = timeCursor + ((1000*60)*intervalInMinutes);
            Date end = new Date(timeCursor);
            timeSlots.add(new BookingSlot(start, end, this::onBookButtonClicked));
        }

        return timeSlots;
    }

    private void onBookButtonClicked(BookingSlot bookingSlot){
        if(bookingSlot.isBooked() && bookingSlot.isManaged()){
            // If the slot is booked, and the booking is local, we can unbook it
            unbook(bookingSlot);
        } else if (!bookingSlot.isBooked()){
            // If no booking (local and remote) exists for this slot, we can commit a local booking
            book(bookingSlot);
        }
    }

    private void book(BookingSlot bookingSlot){
        MPBooking booking = new MPBooking.Builder()
                .setTimespan(bookingSlot.getStart(), bookingSlot.getEnd())
                .setLocation(mLocation)
                .setLocationID(mLocation.getId())
                .setTitle("Demo Booking - Android")
                .setDescription("This booking was made with the Booking Service Demo on Android")
                .build();

        MPBookingService.getInstance().performBooking(booking, (mpBooking, miError) -> {
            if(miError == null){
                bookingSlot.setLocalBooking(mpBooking);
                bookingSlot.setRemoteBooking(mpBooking);
            } else {
                // Handle error...
            }
            updateUI();
        });
    }

    private void unbook(@NonNull BookingSlot bookingSlot){
        MPBooking localBooking = bookingSlot.getLocalBooking();
        MPBooking remoteBooking = bookingSlot.getRemoteBooking();
        MPBookingService.getInstance().cancelBooking(localBooking != null ? localBooking : remoteBooking, (mpBooking, miError) -> {
            if(miError == null){
                bookingSlot.setLocalBooking(mpBooking);
                bookingSlot.setRemoteBooking(mpBooking);
            }
            updateUI();
        });
    }

    private void fetchBookings(@NonNull MPLocation location, @NonNull Date from, @NonNull Date to, OnLoadingDataReadyListener listener){
        MPBookingsQuery query = new MPBookingsQuery.Builder()
                .setLocation(location)
                .setTimespan(from, to)
                .build();

        MPBookingService.getInstance().getBookingsUsingQuery(query, (bookingList, miError) -> {
            if(miError == null){
                for(BookingSlot slot : mTimeSlots){
                    slot.setRemoteBooking(null);
                }
                mTimeSlots = computeTimeOverlaps(mTimeSlots, bookingList);
            } else {
                // Handle error...
            }

            listener.onLoadingDataReady(miError);
        });
    }

    private synchronized List<BookingSlot> computeTimeOverlaps(List<BookingSlot> localBookingModel, List<MPBooking> remoteBookings){
        for(BookingSlot slot : localBookingModel){
            for(MPBooking remoteBooking : remoteBookings){
                // if a remote booking overlaps with a given slot, then assign the remote booking to that slot
                // (making the slot unbookable, which can then be reflected in the UI)
                if(detectTimeOverlap(remoteBooking.getStartTime(), remoteBooking.getEndTime(), slot.getStart(), slot.getEnd())){
                    slot.setRemoteBooking(remoteBooking);
                }
            }
        }
        return localBookingModel;
    }

    private void syncBookingState(MPLocation location){
        Date now = new Date(System.currentTimeMillis());
        Date tomorrow = new Date(System.currentTimeMillis() + 1000*60*60*24);
        fetchBookings(location, now, tomorrow, error -> updateUI());
    }

    private synchronized void updateUI(){
        getActivity().runOnUiThread(() -> mAdapter.notifyDataSetChanged());
    }

    private boolean detectTimeOverlap(Date aStart, Date aEnd, Date bStart, Date bEnd){
        // Note, we trim intervals with one second, to avoid an "overlap" if aEnd = bStart
        // Eg. booking 16:30-17:00 should not overlap with booking 17:00-17:30
        final long trimMillis = 1000;
        return aStart.getTime()+trimMillis < bEnd.getTime()-trimMillis && bStart.getTime()+trimMillis < aEnd.getTime()-trimMillis;
    }

}
