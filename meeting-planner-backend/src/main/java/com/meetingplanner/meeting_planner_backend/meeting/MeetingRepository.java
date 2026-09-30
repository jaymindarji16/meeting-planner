package com.meetingplanner.meeting_planner_backend.meeting;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    @Query("select distinct m from Meeting m left join m.participants p " +
           "where m.organizer.id = :userId or p.user.id = :userId " +
           "order by m.scheduledAt asc")
    List<Meeting> findAllForUser(@Param("userId") Long userId);
}