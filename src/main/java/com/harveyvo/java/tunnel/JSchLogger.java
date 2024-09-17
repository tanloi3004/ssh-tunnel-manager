package com.harveyvo.java.tunnel;

import com.jcraft.jsch.Logger;

import java.util.function.Consumer;

public class JSchLogger implements Logger {

    private final Consumer<String> logConsumer;

    public JSchLogger(Consumer<String> logConsumer) {
        this.logConsumer = logConsumer;
    }

    @Override
    public boolean isEnabled(int level) {
        return true;
    }

    @Override
    public void log(int level, String message) {
        String levelName = getLevelName(level);
        String logMessage = "[JSch] " + levelName + ": " + message;
        logConsumer.accept(logMessage);
    }

    private String getLevelName(int level) {
        switch (level) {
            case DEBUG: return "DEBUG";
            case INFO: return "INFO";
            case WARN: return "WARN";
            case ERROR: return "ERROR";
            case FATAL: return "FATAL";
            default: return "UNKNOWN";
        }
    }
}
