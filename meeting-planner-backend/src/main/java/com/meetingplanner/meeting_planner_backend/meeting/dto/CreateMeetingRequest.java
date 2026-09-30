package com.meetingplanner.meeting_planner_backend.meeting.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

public record CreateMeetingRequest(
    @NotBlank @Size(max = 200) String title,
    @Size(max = 5000) String description,
    @NotNull LocalDateTime scheduledAt,
    @Min(1) @Max(1440) Integer durationMinutes,
    @Size(max = 200) String location,
    List<Long> participantIds
) {}