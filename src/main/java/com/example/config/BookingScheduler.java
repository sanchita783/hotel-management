package com.example.config;

import com.example.entity.Booking;
import com.example.entity.BookingStatus;
import com.example.entity.RoomStatus;
import com.example.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingScheduler {

    private final BookingRepository bookingRepository;

    /**
     * Runs every day at 02:00 AM.
     * Marks CONFIRMED bookings whose check-in date has passed as NO_SHOW.
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void markNoShows() {
        List<Booking> confirmedPast = bookingRepository.findByBookingStatus(BookingStatus.CONFIRMED)
                .stream()
                .filter(b -> b.getCheckInDate().isBefore(LocalDate.now()))
                .toList();

        if (confirmedPast.isEmpty()) {
            return;
        }

        confirmedPast.forEach(booking -> {
            booking.setBookingStatus(BookingStatus.NO_SHOW);
            booking.setCancellationReason("Auto-marked: Guest did not check in by " +
                    booking.getCheckInDate());
            booking.setCancelledAt(LocalDateTime.now());
            booking.getRoom().setRoomStatus(RoomStatus.AVAILABLE);
        });

        bookingRepository.saveAll(confirmedPast);
        log.info("Scheduler: Marked {} booking(s) as NO_SHOW", confirmedPast.size());
    }

    /**
     * Runs every day at 03:00 AM.
     * Sets rooms in CLEANING status back to AVAILABLE after overnight.
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void resetCleaningRooms() {
        List<Booking> checkedOut = bookingRepository.findByBookingStatus(BookingStatus.CHECKED_OUT)
                .stream()
                .filter(b -> b.getCheckedOutAt() != null &&
                        b.getCheckedOutAt().isBefore(LocalDateTime.now().minusHours(8)))
                .toList();

        checkedOut.forEach(b -> {
            if (b.getRoom().getRoomStatus() == RoomStatus.CLEANING) {
                b.getRoom().setRoomStatus(RoomStatus.AVAILABLE);
            }
        });

        if (!checkedOut.isEmpty()) {
            bookingRepository.saveAll(checkedOut);
            log.info("Scheduler: Reset {} room(s) from CLEANING to AVAILABLE", checkedOut.size());
        }
    }
}
