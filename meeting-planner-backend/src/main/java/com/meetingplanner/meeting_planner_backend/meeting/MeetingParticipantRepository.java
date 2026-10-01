package com.meetingplanner.meeting_planner_backend.meeting;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface MeetingParticipantRepository extends JpaRepository<MeetingParticipant, Long> {

    List<MeetingParticipant> findByUserId(Long userId);
}