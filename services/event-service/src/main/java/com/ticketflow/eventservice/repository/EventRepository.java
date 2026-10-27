package com.ticketflow.eventservice.repository;

import com.ticketflow.eventservice.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("select e from Event e where e.status = com.ticketflow.eventservice.model.EventStatus.PUBLISHED "
            + "and (:q is null or lower(e.title) like lower(concat('%', :q, '%'))) "
            + "and (:city is null or lower(e.city) = lower(:city)) "
            + "and (:category is null or lower(e.category) = lower(:category))")
    Page<Event> search(@Param("q") String q,
                       @Param("city") String city,
                       @Param("category") String category,
                       Pageable pageable);
}
