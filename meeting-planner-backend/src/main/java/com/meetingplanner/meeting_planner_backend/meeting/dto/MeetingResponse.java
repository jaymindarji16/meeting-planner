package com.meetingplanner.meeting_planner_backend.meeting.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.meetingplanner.meeting_planner_backend.meeting.Meeting;
import com.meetingplanner.meeting_planner_backend.meeting.MeetingParticipant;

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

    public static MeetingResponse from(Meeting meeting) {
        List<ParticipantView> parts = meeting.getParticipants().stream()
            .map(MeetingResponse::toParticipantView)
            .toList();
        return new MeetingResponse(
            meeting.getId(),
            meeting.getTitle(),
            meeting.getDescription(),
            meeting.getScheduledAt(),
            meeting.getDurationMinutes(),
            meeting.getLocation(),
            meeting.getOrganizer().getId(),
            meeting.getOrganizer().getName(),
            parts
        );
    }

    private static ParticipantView toParticipantView(MeetingParticipant meetingParticipant) {
        return new ParticipantView(
            meetingParticipant.getUser().getId(),
            meetingParticipant.getUser().getName(),
            meetingParticipant.getUser().getEmail(),
            meetingParticipant.getUser().getAvatarFilename() != null,
            meetingParticipant.getStatus().name()
        );
    }
}