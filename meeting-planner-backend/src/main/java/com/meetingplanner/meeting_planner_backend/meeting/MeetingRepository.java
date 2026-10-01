package com.meetingplanner.meeting_planner_backend.meeting;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    List<Meeting> findByOrganizerId(Long userId);
}