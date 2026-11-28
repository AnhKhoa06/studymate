package com.studymate.room.state;

import com.studymate.room.entity.Booking;
import com.studymate.room.entity.BookingStatus;

import java.time.LocalDateTime;

public class ApprovedState implements BookingState {

    @Override
    public BookingStatus getStatus() {
        return BookingStatus.APPROVED;
    }

    @Override
    public void cancel(Booking booking, LocalDateTime now) {
        if (!booking.getStartTime().isAfter(now)) {
            throw new InvalidBookingStateException("Không thể hủy đặt phòng khi đã đến hoặc qua giờ bắt đầu");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setUpdatedAt(now);
    }
}
