package com.meetingplanner.meeting_planner_backend.user.dto;

import com.meetingplanner.meeting_planner_backend.user.User;

public record UserResponse(Long id, String name, String email, boolean hasAvatar) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getAvatarFilename() != null);
    }
}