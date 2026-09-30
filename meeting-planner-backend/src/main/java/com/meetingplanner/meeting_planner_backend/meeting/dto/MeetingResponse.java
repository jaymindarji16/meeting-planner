package com.meetingplanner.meeting_planner_backend.meeting.dto;

import com.meetingplanner.meeting_planner_backend.meeting.Meeting;
import com.meetingplanner.meeting_planner_backend.meeting.MeetingParticipant;

import java.time.LocalDateTime;
import java.util.List;

public record MeetingResponse(
    Long id,
    String title,
    String description,
    LocalDateTime scheduledAt,
    Integer durationMinutes,
    String location,
    Long organizerId,
    String organizerName,
    List<ParticipantView> participants
) {
    public record ParticipantView(Long id, String name, String email, boolean hasAvatar, String status) {}

    public static MeetingResponse from(Meeting m) {
        List<ParticipantView> parts = m.getParticipants().stream()
            .map(MeetingResponse::toParticipantView)
            .toList();
        return new MeetingResponse(
            m.getId(),
            m.getTitle(),
            m.getDescription(),
            m.getScheduledAt(),
            m.getDurationMinutes(),
            m.getLocation(),
            m.getOrganizer().getId(),
            m.getOrganizer().getName(),
            parts
        );
    }

    private static ParticipantView toParticipantView(MeetingParticipant p) {
        return new ParticipantView(
            p.getUser().getId(),
            p.getUser().getName(),
            p.getUser().getEmail(),
            p.getUser().getAvatarFilename() != null,
            p.getStatus().name()
        );
    }
}