package com.harveyvo.java.tunnel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileManager {

    private static final String APP_DIRECTORY_NAME = ".ssh_tunnel_manager"; // Folder name in user home
    private Path appDirectoryPath;

    public FileManager() {
        // Get the user’s home directory and construct the path to the app directory
        String userHome = System.getProperty("user.home");
        appDirectoryPath = Paths.get(userHome, APP_DIRECTORY_NAME);

        // Ensure the directory exists, create if not
        createAppDirectoryIfNotExists();
    }

    // Create the app directory if it doesn't exist
    private void createAppDirectoryIfNotExists() {
        if (!Files.exists(appDirectoryPath)) {
            try {
                Files.createDirectories(appDirectoryPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Get the full path to a file (log or profile) in the app directory
    public Path getFilePath(String fileName) {
        return appDirectoryPath.resolve(fileName);
    }

    // Ensure a specific file exists, create if not
    public void createFileIfNotExists(String fileName) {
        Path filePath = getFilePath(fileName);
        try {
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Path getAppDirectoryPath() {
        return appDirectoryPath;
    }
}
