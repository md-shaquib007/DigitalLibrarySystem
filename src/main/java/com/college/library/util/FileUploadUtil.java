package com.college.library.util;

import com.college.library.config.AppConstants;
import com.college.library.exception.LibraryException;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public final class FileUploadUtil {

    private FileUploadUtil() {}

    public static String saveBookFile(byte[] content, String originalFilename, String uploadBasePath) {
        if (content == null || content.length == 0) {
            throw new LibraryException("File is empty");
        }
        if (content.length > AppConstants.MAX_FILE_SIZE) {
            throw new LibraryException("File size exceeds 50MB limit");
        }

        String extension = FilenameUtils.getExtension(originalFilename);
        if (!"pdf".equalsIgnoreCase(extension)) {
            throw new LibraryException("Only PDF files are allowed");
        }

        try {
            Path uploadDir = Paths.get(uploadBasePath, AppConstants.UPLOAD_DIR);
            Files.createDirectories(uploadDir);

            String storedName = UUID.randomUUID() + "." + extension.toLowerCase();
            Path target = uploadDir.resolve(storedName);
            Files.write(target, content);
            return AppConstants.UPLOAD_DIR + "/" + storedName;
        } catch (IOException e) {
            throw new LibraryException("Failed to save file", e);
        }
    }

    public static void deleteFile(String relativePath, String uploadBasePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return;
        }
        try {
            Path path = Paths.get(uploadBasePath, relativePath);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new LibraryException("Failed to delete file", e);
        }
    }

    public static Path resolveFilePath(String relativePath, String uploadBasePath) {
        return Paths.get(uploadBasePath, relativePath);
    }
}
