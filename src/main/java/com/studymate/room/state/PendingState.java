package com.studymate.room.state;

import com.studymate.room.entity.Booking;
import com.studymate.room.entity.BookingStatus;

import java.time.LocalDateTime;

public class PendingState implements BookingState {

    @Override
    public BookingStatus getStatus() {
        return BookingStatus.PENDING;
    }

    @Override
    public void approve(Booking booking, Long reviewerUserId, LocalDateTime now) {
        if (reviewerUserId == null) {
            throw new IllegalArgumentException("Thiếu thông tin người duyệt");
        }
        booking.setStatus(BookingStatus.APPROVED);
        booking.setReviewedByUserId(reviewerUserId);
        booking.setReviewedAt(now);
        booking.setRejectionReason(null);
        booking.setUpdatedAt(now);
    }

    @Override
    public void reject(Booking booking, Long reviewerUserId, String reason, LocalDateTime now) {
        if (reviewerUserId == null) {
            throw new IllegalArgumentException("Thiếu thông tin người từ chối");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập lý do từ chối");
        }
        booking.setStatus(BookingStatus.REJECTED);
        booking.setReviewedByUserId(reviewerUserId);
        booking.setReviewedAt(now);
        booking.setRejectionReason(reason.trim());
        booking.setUpdatedAt(now);
    }

    @Override
    public void cancel(Booking booking, LocalDateTime now) {
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setUpdatedAt(now);
    }
}
