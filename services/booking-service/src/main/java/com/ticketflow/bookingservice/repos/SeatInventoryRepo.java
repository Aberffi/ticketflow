package com.ticketflow.bookingservice.repos;

import com.ticketflow.bookingservice.entities.SeatInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Check-then-act ("count the seats, then insert") is a race: two requests can both see one free seat.
 * Here the check and the change are the SAME statement, which the database runs atomically on the row.
 */
public interface SeatInventoryRepo extends JpaRepository<SeatInventory, Long> {

    /** Takes {@code quantity} seats if enough are left. Returns 1 if taken, 0 if sold out. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update SeatInventory s set s.held = s.held + :quantity, s.capacity = :capacity "
            + "where s.eventId = :eventId and s.held + :quantity <= :capacity")
    int reserve(@Param("eventId") Long eventId, @Param("quantity") int quantity, @Param("capacity") int capacity);

    /** Gives seats back. Returns 0 (and changes nothing) if that would make the count negative. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update SeatInventory s set s.held = s.held - :quantity "
            + "where s.eventId = :eventId and s.held >= :quantity")
    int release(@Param("eventId") Long eventId, @Param("quantity") int quantity);
}
