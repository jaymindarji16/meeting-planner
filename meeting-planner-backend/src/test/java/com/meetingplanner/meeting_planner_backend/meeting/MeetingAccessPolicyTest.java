package com.meetingplanner.meeting_planner_backend.meeting;

import com.meetingplanner.meeting_planner_backend.meeting.dto.CreateMeetingRequest;
import com.meetingplanner.meeting_planner_backend.user.User;
import com.meetingplanner.meeting_planner_backend.user.UserService;
import com.meetingplanner.meeting_planner_backend.user.dto.SignupRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MeetingAccessPolicyTest {

    @Autowired MeetingAccessPolicy policy;
    @Autowired UserService userService;
    @Autowired MeetingService meetingService;

    private User newUser(String tag) {
        return userService.signup(new SignupRequest(
            tag, tag + System.nanoTime() + "@example.com", "secret123"));
    }

    @Test
    void organizerCanViewEditAndDelete() {
        User org = newUser("aorg");
        Meeting m = meetingService.create(org, new CreateMeetingRequest(
            "T", null, LocalDateTime.now().plusHours(1), 30, null, List.of()
        ));
        assertThat(policy.canView(m, org)).isTrue();
        assertThat(policy.canEdit(m, org)).isTrue();
        assertThat(policy.canDelete(m, org)).isTrue();
    }

    @Test
    void participantCanViewButNotDeleteOrEdit() {
        User org = newUser("borg");
        User p = newUser("bpart");
        Meeting m = meetingService.create(org, new CreateMeetingRequest(
            "T", null, LocalDateTime.now().plusHours(1), 30, null, List.of(p.getId())
        ));
        assertThat(policy.canView(m, p)).isTrue();
        assertThat(policy.canEdit(m, p)).isFalse();
        assertThat(policy.canDelete(m, p)).isFalse();
    }

    @Test
    void outsiderCannotViewEditOrDelete() {
        User org = newUser("corg");
        User outsider = newUser("cout");
        Meeting m = meetingService.create(org, new CreateMeetingRequest(
            "T", null, LocalDateTime.now().plusHours(1), 30, null, List.of()
        ));
        assertThat(policy.canView(m, outsider)).isFalse();
        assertThat(policy.canEdit(m, outsider)).isFalse();
        assertThat(policy.canDelete(m, outsider)).isFalse();
    }
}