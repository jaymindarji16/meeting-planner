package com.meetingplanner.meeting_planner_backend.user;

import com.meetingplanner.meeting_planner_backend.user.dto.LoginRequest;
import com.meetingplanner.meeting_planner_backend.user.dto.SignupRequest;
import com.meetingplanner.meeting_planner_backend.user.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public UserResponse signup(@Valid @RequestBody SignupRequest req) {
        return UserResponse.from(userService.signup(req));
    }

    @PostMapping("/login")
    public UserResponse login(@Valid @RequestBody LoginRequest req) {
        return UserResponse.from(userService.login(req));
    }
}