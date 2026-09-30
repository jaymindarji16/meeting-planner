package com.meetingplanner.meeting_planner_backend.meeting;

import com.meetingplanner.meeting_planner_backend.user.User;
import org.springframework.stereotype.Component;

@Component
public class MeetingAccessPolicy {

    public boolean canView(Meeting meeting, User user) {
        return isOrganizer(meeting, user) || isParticipant(meeting, user);
    }

    public boolean canEdit(Meeting meeting, User user) {
        // Only the organizer can edit for now.
        return isOrganizer(meeting, user);
    }

    public boolean canDelete(Meeting meeting, User user) {
        return isOrganizer(meeting, user);
    }

    private boolean isOrganizer(Meeting meeting, User user) {
        return meeting.getOrganizer().getId().equals(user.getId());
    }

    private boolean isParticipant(Meeting meeting, User user) {
        return meeting.getParticipants().stream()
            .anyMatch(p -> p.getUser().getId().equals(user.getId()));
    }
}