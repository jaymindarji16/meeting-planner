package com.meetingplanner.meeting_planner_backend.user.dto;

import com.meetingplanner.meeting_planner_backend.user.User;

public record UserResponse(Long id, String name, String email, boolean hasAvatar) {

    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getAvatarFilename() != null);
    }
}