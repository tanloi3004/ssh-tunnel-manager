package com.harveyvo.java.tunnel;

import com.jcraft.jsch.Logger;
import javafx.application.Platform;
import javafx.scene.control.TextArea;

public class JSchConsoleLogger implements Logger {

    private final TextArea consoleLogTextArea;

    public JSchConsoleLogger(TextArea consoleLogTextArea) {
        this.consoleLogTextArea = consoleLogTextArea;
    }

    private static final int[] LEVELS = {
            DEBUG, INFO, WARN, ERROR, FATAL
    };

    @Override
    public boolean isEnabled(int level) {
        // Enable all levels for detailed logs
        return true;
    }

    @Override
    public void log(int level, String message) {
        Platform.runLater(() -> {
            consoleLogTextArea.appendText("[JSch] " + getLevelName(level) + ": " + message + "\n");
        });
    }

    private String getLevelName(int level) {
        switch (level) {
            case DEBUG:
                return "DEBUG";
            case INFO:
                return "INFO";
            case WARN:
                return "WARN";
            case ERROR:
                return "ERROR";
            case FATAL:
                return "FATAL";
            default:
                return "UNKNOWN";
        }
    }
}
