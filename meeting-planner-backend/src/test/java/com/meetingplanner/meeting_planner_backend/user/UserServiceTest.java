package com.meetingplanner.meeting_planner_backend.user;

import com.meetingplanner.meeting_planner_backend.user.dto.LoginRequest;
import com.meetingplanner.meeting_planner_backend.user.dto.SignupRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class UserServiceTest {

    @Autowired UserService userService;

    @Test
    void signupHashesPasswordAndLoginWorks() {
        String email = "alice+" + System.nanoTime() + "@example.com";
        User u = userService.signup(new SignupRequest("Alice", email, "secret123"));

        assertThat(u.getId()).isNotNull();
        assertThat(u.getPasswordHash()).isNotEqualTo("secret123");
        assertThat(u.getPasswordHash()).startsWith("$2");

        User loggedIn = userService.login(new LoginRequest(email, "secret123"));
        assertThat(loggedIn.getId()).isEqualTo(u.getId());
    }

    @Test
    void signupRejectsDuplicateEmail() {
        String email = "bob+" + System.nanoTime() + "@example.com";
        userService.signup(new SignupRequest("Bob", email, "secret123"));

        assertThatThrownBy(() -> userService.signup(new SignupRequest("Bob2", email, "other123")))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("already registered");
    }

    @Test
    void signupNormalisesEmailToLowercase() {
        String email = "MixedCase+" + System.nanoTime() + "@Example.COM";
        User u = userService.signup(new SignupRequest("Case", email, "secret123"));
        assertThat(u.getEmail()).isLowerCase();
    }

    @Test
    void loginRejectsWrongPassword() {
        String email = "carol+" + System.nanoTime() + "@example.com";
        userService.signup(new SignupRequest("Carol", email, "secret123"));

        assertThatThrownBy(() -> userService.login(new LoginRequest(email, "wrong")))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Invalid credentials");
    }

    @Test
    void loginRejectsUnknownEmail() {
        assertThatThrownBy(() -> userService.login(
            new LoginRequest("ghost+" + System.nanoTime() + "@example.com", "anything")))
            .isInstanceOf(ResponseStatusException.class);
    }
}