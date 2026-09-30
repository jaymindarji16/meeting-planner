package com.meetingplanner.meeting_planner_backend.meeting;

import com.meetingplanner.meeting_planner_backend.meeting.dto.CreateMeetingRequest;
import com.meetingplanner.meeting_planner_backend.user.User;
import com.meetingplanner.meeting_planner_backend.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final MeetingAccessPolicy accessPolicy;

    public MeetingService(MeetingRepository meetingRepository,
                          UserRepository userRepository,
                          MeetingAccessPolicy accessPolicy) {
        this.meetingRepository = meetingRepository;
        this.userRepository = userRepository;
        this.accessPolicy = accessPolicy;
    }

    @Transactional
    public Meeting create(User organizer, CreateMeetingRequest req) {
        Meeting m = new Meeting();
        m.setTitle(req.title().trim());
        m.setDescription(req.description());
        m.setScheduledAt(req.scheduledAt());
        m.setDurationMinutes(req.durationMinutes());
        m.setLocation(req.location());
        m.setOrganizer(organizer);

        if (req.participantIds() != null) {
            for (Long pid : req.participantIds()) {
                if (pid.equals(organizer.getId())) continue; // organizer is implicit
                User u = userRepository.findById(pid)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown user id: " + pid));
                m.getParticipants().add(new MeetingParticipant(m, u));
            }
        }

        return meetingRepository.save(m);
    }

    public List<Meeting> findAllForUser(Long userId) {
        return meetingRepository.findAllForUser(userId);
    }

    public Meeting getVisible(Long meetingId, Long userId) {
        Meeting m = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meeting not found"));
        User u = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unknown user"));
        if (!accessPolicy.canView(m, u)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not part of this meeting");
        }
        return m;
    }

    @Transactional
    public void delete(Long meetingId, Long userId) {
        Meeting m = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meeting not found"));
        User u = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unknown user"));
        if (!accessPolicy.canDelete(m, u)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the organizer can delete this meeting");
        }
        meetingRepository.delete(m);
    }
}