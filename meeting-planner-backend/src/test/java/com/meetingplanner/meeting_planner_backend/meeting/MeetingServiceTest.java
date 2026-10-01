package com.meetingplanner.meeting_planner_backend.meeting;

import com.meetingplanner.meeting_planner_backend.meeting.dto.CreateMeetingRequest;
import com.meetingplanner.meeting_planner_backend.user.User;
import com.meetingplanner.meeting_planner_backend.user.UserService;
import com.meetingplanner.meeting_planner_backend.user.dto.SignupRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class MeetingServiceTest {

    @Autowired MeetingService meetingService;
    @Autowired UserService userService;

    private User newUser(String tag) {
        return userService.signup(new SignupRequest(
            tag, tag + System.nanoTime() + "@example.com", "secret123"));
    }

    @Test
    void organizerCanSeeOwnMeeting() {
        User organizer = newUser("org");
        Meeting m = meetingService.create(organizer, new CreateMeetingRequest(
            "Standup", null, LocalDateTime.now().plusHours(1), 30, "Room A", List.of()
        ));
        assertThat(meetingService.findAllForUser(organizer.getId()))
            .extracting(Meeting::getId).contains(m.getId());
    }

    @Test
    void participantCanSeeMeetingButOutsiderCannot() {
        User organizer = newUser("org2");
        User participant = newUser("part2");
        User outsider = newUser("out2");

        Meeting m = meetingService.create(organizer, new CreateMeetingRequest(
            "1:1", null, LocalDateTime.now().plusHours(2), 30, null,
            List.of(participant.getId())
        ));

        assertThat(meetingService.findAllForUser(participant.getId()))
            .extracting(Meeting::getId).contains(m.getId());

        assertThatThrownBy(() -> meetingService.getVisible(m.getId(), outsider.getId()))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("not part");
    }

    @Test
    void newUserSeesNoMeetings() {
        User lonely = newUser("lonely");
        assertThat(meetingService.findAllForUser(lonely.getId())).isEmpty();
    }

    @Test
    void organizerIsNotAddedAsExplicitParticipant() {
        User organizer = newUser("org3");
        // Even if organizer passes their own id, it should be ignored
        Meeting m = meetingService.create(organizer, new CreateMeetingRequest(
            "Self", null, LocalDateTime.now().plusHours(1), 30, null,
            List.of(organizer.getId())
        ));
        assertThat(m.getParticipants()).isEmpty();
    }

    @Test
    void duplicateParticipantIdsAreNotCreatedTwice() {
        User organizer = newUser("org4");
        User participant = newUser("part4");

        Meeting m = meetingService.create(organizer, new CreateMeetingRequest(
            "Dupes", null, LocalDateTime.now().plusHours(1), 30, null,
            List.of(participant.getId(), participant.getId(), participant.getId())
        ));

        assertThat(m.getParticipants()).hasSize(1);
    }

    @Test
    void unknownParticipantIdIsRejected() {
        User organizer = newUser("org5");
        assertThatThrownBy(() -> meetingService.create(organizer, new CreateMeetingRequest(
            "Bad", null, LocalDateTime.now().plusHours(1), 30, null,
            List.of(999_999_999L)
        )))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Unknown user id");
    }

    @Test
    void nonOrganizerCannotDelete() {
        User organizer = newUser("org6");
        User other = newUser("other6");
        Meeting m = meetingService.create(organizer, new CreateMeetingRequest(
            "Planning", null, LocalDateTime.now().plusMinutes(10), 60, null, List.of(other.getId())
        ));

        assertThatThrownBy(() -> meetingService.delete(m.getId(), other.getId()))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("organizer");
    }

    @Test
    void pastMeetingIsRejected() {
        User organizer = newUser("org7");
        assertThatThrownBy(() -> meetingService.create(organizer, new CreateMeetingRequest(
            "Yesterday", null, LocalDateTime.now().minusHours(1), 30, null, List.of()
        )))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("future");
    }
}