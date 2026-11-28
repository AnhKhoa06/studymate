package com.studymate.room.state;

import com.studymate.room.entity.BookingStatus;

public class RejectedState implements BookingState {

    @Override
    public BookingStatus getStatus() {
        return BookingStatus.REJECTED;
    }
}
