package com.meetingplanner.meeting_planner_backend.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    private FileStorageService newService() {
        return new FileStorageService(tempDir.toString());
    }

    @Test
    void savesPngWithExpectedFilename() {
        FileStorageService svc = newService();
        var file = new MockMultipartFile(
            "file", "me.png", "image/png", "fake-png".getBytes()
        );

        String filename = svc.saveAvatar(42L, file);

        assertThat(filename).isEqualTo("user-42.png");
        assertThat(svc.resolve(filename)).exists();
    }

    @Test
    void rejectsNonImageExtension() {
        FileStorageService svc = newService();
        var file = new MockMultipartFile(
            "file", "virus.exe", "application/octet-stream", "boom".getBytes()
        );

        assertThatThrownBy(() -> svc.saveAvatar(1L, file))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("png/jpg/jpeg");
    }

    @Test
    void rejectsEmptyFile() {
        FileStorageService svc = newService();
        var file = new MockMultipartFile(
            "file", "empty.png", "image/png", new byte[0]
        );

        assertThatThrownBy(() -> svc.saveAvatar(1L, file))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Empty");
    }

    @Test
    void replacingAvatarDeletesOldFile() {
        FileStorageService svc = newService();

        var pngFile = new MockMultipartFile(
            "file", "me.png", "image/png", "png-bytes".getBytes()
        );
        String first = svc.saveAvatar(7L, pngFile);
        Path firstPath = svc.resolve(first);

        var jpgFile = new MockMultipartFile(
            "file", "me.jpg", "image/jpeg", "jpg-bytes".getBytes()
        );
        String second = svc.saveAvatar(7L, jpgFile);

        assertThat(second).isEqualTo("user-7.jpg");
        assertThat(firstPath).doesNotExist(); // old .png was cleaned up
    }

    @Test
    void resolveRejectsPathTraversal() {
        FileStorageService svc = newService();
        assertThatThrownBy(() -> svc.resolve("../../../etc/passwd"))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Invalid path");
    }
}