package com.ticketflow.bookingservice.repository;

import com.ticketflow.bookingservice.model.Booking;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("select coalesce(sum(b.quantity), 0) from Booking b where b.eventId = :eventId "
            + "and (b.status = com.ticketflow.bookingservice.model.BookingStatus.CONFIRMED "
            + "or (b.status = com.ticketflow.bookingservice.model.BookingStatus.PENDING_PAYMENT "
            + "and b.expiresAt > :now))")
    long reservedQuantity(@Param("eventId") Long eventId, @Param("now") Instant now);

    List<Booking> findByCustomerIdOrderByCreatedAtDesc(String customerId);
}
