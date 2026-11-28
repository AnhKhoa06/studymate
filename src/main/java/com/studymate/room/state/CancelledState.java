package com.studymate.room.state;

import com.studymate.room.entity.BookingStatus;

public class CancelledState implements BookingState {

    @Override
    public BookingStatus getStatus() {
        return BookingStatus.CANCELLED;
    }
}
