package com.meetingplanner.meeting_planner_backend.user;

import com.meetingplanner.meeting_planner_backend.common.CurrentUser;
import com.meetingplanner.meeting_planner_backend.common.FileStorageService;
import com.meetingplanner.meeting_planner_backend.user.dto.UserResponse;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final CurrentUser currentUser;
    private final FileStorageService fileStorage;

    public UserController(UserService userService,
                          CurrentUser currentUser,
                          FileStorageService fileStorage) {
        this.userService = userService;
        this.currentUser = currentUser;
        this.fileStorage = fileStorage;
    }

    @GetMapping
    public List<UserResponse> list(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        currentUser.require(userId);
        return userService.findAll().stream().map(UserResponse::from).toList();
    }

    @GetMapping("/me")
    public UserResponse me(@RequestHeader("X-User-Id") Long userId) {
        return UserResponse.from(currentUser.require(userId));
    }

    @PostMapping("/me/avatar")
    public UserResponse uploadAvatar(@RequestHeader("X-User-Id") Long userId,
                                     @RequestParam("file") MultipartFile file) {
        User user = currentUser.require(userId);
        String filename = fileStorage.saveAvatar(user.getId(), file);
        user.setAvatarFilename(filename);
        userService.save(user);
        return UserResponse.from(user);
    }

    @GetMapping("/{id}/avatar")
    public ResponseEntity<Resource> getAvatar(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user.getAvatarFilename() == null) {
            return ResponseEntity.notFound().build();
        }
        Path file = fileStorage.resolve(user.getAvatarFilename());
        if (!Files.exists(file)) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new FileSystemResource(file);
        String contentType = user.getAvatarFilename().endsWith(".png") ? "image/png" : "image/jpeg";
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .body(resource);
    }
}