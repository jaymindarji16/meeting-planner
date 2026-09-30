package com.meetingplanner.meeting_planner_backend.meeting;

import com.meetingplanner.meeting_planner_backend.common.CurrentUser;
import com.meetingplanner.meeting_planner_backend.meeting.dto.CreateMeetingRequest;
import com.meetingplanner.meeting_planner_backend.meeting.dto.MeetingResponse;
import com.meetingplanner.meeting_planner_backend.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meetings")
public class MeetingController {

    private final MeetingService meetingService;
    private final CurrentUser currentUser;

    public MeetingController(MeetingService meetingService, CurrentUser currentUser) {
        this.meetingService = meetingService;
        this.currentUser = currentUser;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MeetingResponse create(@RequestHeader("X-User-Id") Long userId,
                                  @Valid @RequestBody CreateMeetingRequest req) {
        User me = currentUser.require(userId);
        return MeetingResponse.from(meetingService.create(me, req));
    }

    @GetMapping
    public List<MeetingResponse> myMeetings(@RequestHeader("X-User-Id") Long userId) {
        currentUser.require(userId);
        return meetingService.findAllForUser(userId).stream().map(MeetingResponse::from).toList();
    }

    @GetMapping("/{id}")
    public MeetingResponse detail(@RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        currentUser.require(userId);
        return MeetingResponse.from(meetingService.getVisible(id, userId));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        currentUser.require(userId);
        meetingService.delete(id, userId);
    }
}