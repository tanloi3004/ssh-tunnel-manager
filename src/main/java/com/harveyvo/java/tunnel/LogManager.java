package com.harveyvo.java.tunnel;

import java.io.*;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Deque;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class LogManager {

    private static final String LOG_FILE_NAME = "ssh_tunnel_manager.log"; // Log file name
    private FileManager fileManager;  // FileManager to handle file operations
    private SimpleDateFormat dateFormatter;

    // Enum for log levels
    public enum LogLevel {
        INFO, WARNING, ERROR
    }

    // Private constructor for Singleton
    private LogManager() {
        this.fileManager = new FileManager(); // Initialize FileManager
        dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // Date format for log timestamps

        // Ensure the log file exists
        this.fileManager.createFileIfNotExists(LOG_FILE_NAME);
    }

    // Static inner class responsible for holding the Singleton instance
    private static class LogManagerHolder {
        private static final LogManager INSTANCE = new LogManager();
    }

    // Public method to provide access to the singleton instance
    public static LogManager getInstance() {
        return LogManagerHolder.INSTANCE;
    }

    public void log(String message) {
        this.log(message, LogLevel.INFO);
    }

    // Write log message to the log file with log level and timestamp
    public void log(String message, LogLevel level) {
        String timestamp = dateFormatter.format(new Date()); // Get the current timestamp
        String formattedMessage = String.format("[%s] [%s] %s", timestamp, level, message); // Format the log message
        Path logFilePath = fileManager.getFilePath(LOG_FILE_NAME); // Get the log file path

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFilePath.toFile(), true))) {
            writer.write(formattedMessage + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Read the last 100 lines from the log file
    public String getLast100Lines() {
        Deque<String> lines = new LinkedList<>();
        Path logFilePath = fileManager.getFilePath(LOG_FILE_NAME); // Get the log file path
        try (BufferedReader reader = new BufferedReader(new FileReader(logFilePath.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
                if (lines.size() > 100) {
                    lines.poll(); // Keep only the last 100 lines
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines.stream().collect(Collectors.joining("\n"));
    }
}
