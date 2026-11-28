package com.studymate.room.state;

import com.studymate.room.entity.BookingStatus;

import java.util.EnumMap;
import java.util.Map;

public final class BookingStateFactory {

    private static final Map<BookingStatus, BookingState> STATES = buildStates();

    private BookingStateFactory() {
    }

    private static Map<BookingStatus, BookingState> buildStates() {
        EnumMap<BookingStatus, BookingState> map = new EnumMap<>(BookingStatus.class);
        map.put(BookingStatus.PENDING, new PendingState());
        map.put(BookingStatus.APPROVED, new ApprovedState());
        map.put(BookingStatus.REJECTED, new RejectedState());
        map.put(BookingStatus.CANCELLED, new CancelledState());
        return map;
    }

    public static BookingState from(BookingStatus status) {
        if (status == null || !STATES.containsKey(status)) {
            throw new IllegalArgumentException("Trạng thái đặt phòng không hợp lệ");
        }
        return STATES.get(status);
    }
}
