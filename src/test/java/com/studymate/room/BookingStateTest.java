package com.studymate.room;

import com.studymate.room.entity.Booking;
import com.studymate.room.entity.BookingStatus;
import com.studymate.room.state.BookingStateFactory;
import com.studymate.room.state.InvalidBookingStateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookingStateTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 12, 1, 8, 0);

    @Test
    @DisplayName("PENDING: cho phép approve -> APPROVED, reject -> REJECTED, cancel -> CANCELLED")
    void pending_allowsApproveRejectAndCancel() {
        Booking b1 = bookingWithStatus(BookingStatus.PENDING);
        BookingStateFactory.from(b1.getStatus()).approve(b1, 99L, NOW);
        assertThat(b1.getStatus()).isEqualTo(BookingStatus.APPROVED);
        assertThat(b1.getReviewedByUserId()).isEqualTo(99L);
        assertThat(b1.getReviewedAt()).isEqualTo(NOW);

        Booking b2 = bookingWithStatus(BookingStatus.PENDING);
        BookingStateFactory.from(b2.getStatus()).reject(b2, 99L, "  Phòng bảo trì đột xuất  ", NOW);
        assertThat(b2.getStatus()).isEqualTo(BookingStatus.REJECTED);
        assertThat(b2.getRejectionReason()).isEqualTo("Phòng bảo trì đột xuất");

        Booking b3 = bookingWithStatus(BookingStatus.PENDING);
        BookingStateFactory.from(b3.getStatus()).cancel(b3, NOW);
        assertThat(b3.getStatus()).isEqualTo(BookingStatus.CANCELLED);

        Booking b4 = bookingWithStatus(BookingStatus.PENDING);
        assertThatThrownBy(() -> BookingStateFactory.from(b4.getStatus()).reject(b4, 99L, "   ", NOW))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("APPROVED: cho phép cancel trước giờ học; từ chối approve, reject hoặc cancel khi đã qua giờ bắt đầu")
    void approved_allowsCancelBeforeStartAndRejectsOthers() {
        Booking futureApproved = bookingWithStatus(BookingStatus.APPROVED);
        futureApproved.setStartTime(NOW.plusHours(1));
        BookingStateFactory.from(futureApproved.getStatus()).cancel(futureApproved, NOW);
        assertThat(futureApproved.getStatus()).isEqualTo(BookingStatus.CANCELLED);

        Booking startedApproved = bookingWithStatus(BookingStatus.APPROVED);
        startedApproved.setStartTime(NOW);
        assertThatThrownBy(() -> BookingStateFactory.from(startedApproved.getStatus()).cancel(startedApproved, NOW))
                .isInstanceOf(InvalidBookingStateException.class);

        Booking approved = bookingWithStatus(BookingStatus.APPROVED);
        assertThatThrownBy(() -> BookingStateFactory.from(approved.getStatus()).approve(approved, 99L, NOW))
                .isInstanceOf(InvalidBookingStateException.class);
        assertThatThrownBy(() -> BookingStateFactory.from(approved.getStatus()).reject(approved, 99L, "Lý do", NOW))
                .isInstanceOf(InvalidBookingStateException.class);
    }

    @Test
    @DisplayName("REJECTED và CANCELLED: là trạng thái kết thúc, mọi chuyển đổi đều bị từ chối")
    void terminalStates_rejectAllTransitions() {
        for (BookingStatus terminal : new BookingStatus[]{BookingStatus.REJECTED, BookingStatus.CANCELLED}) {
            Booking booking = bookingWithStatus(terminal);
            assertThatThrownBy(() -> BookingStateFactory.from(booking.getStatus()).approve(booking, 99L, NOW))
                    .isInstanceOf(InvalidBookingStateException.class);
            assertThatThrownBy(() -> BookingStateFactory.from(booking.getStatus()).reject(booking, 99L, "Lý do", NOW))
                    .isInstanceOf(InvalidBookingStateException.class);
            assertThatThrownBy(() -> BookingStateFactory.from(booking.getStatus()).cancel(booking, NOW))
                    .isInstanceOf(InvalidBookingStateException.class);
        }
    }

    private Booking bookingWithStatus(BookingStatus status) {
        Booking b = new Booking();
        b.setStatus(status);
        b.setStartTime(NOW.plusHours(2));
        b.setEndTime(NOW.plusHours(4));
        return b;
    }
}
