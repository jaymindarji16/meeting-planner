package com.meetingplanner.meeting_planner_backend.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.*;
import java.util.Set;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_EXT = Set.of("png", "jpg", "jpeg");

    private final Path uploadDir;

    public FileStorageService(@Value("${app.upload.dir}") String dir) {
        this.uploadDir = Paths.get(dir, "avatars").toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Cannot create upload directory", e);
        }
    }

    public String saveAvatar(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Empty file");
        }
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String ext = original.contains(".")
            ? original.substring(original.lastIndexOf('.') + 1).toLowerCase()
            : "";
        if (!ALLOWED_EXT.contains(ext)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only png/jpg/jpeg allowed");
        }

        String filename = "user-" + userId + "." + ext;
        Path target = uploadDir.resolve(filename);

        // remove older avatar files for this user with different extensions
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(uploadDir, "user-" + userId + ".*")) {
            for (Path old : stream) Files.deleteIfExists(old);
        } catch (IOException ignored) { }

        try {
            file.transferTo(target);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store file");
        }
        return filename;
    }

    public Path resolve(String filename) {
        Path p = uploadDir.resolve(filename).normalize();
        if (!p.startsWith(uploadDir)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid path");
        }
        return p;
    }
}