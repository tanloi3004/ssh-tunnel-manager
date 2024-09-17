package com.harveyvo.java.tunnel;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SessionManager {
    private final List<SessionStatus> sessions = new ArrayList<>();
    private final FileManager fileManager = new FileManager();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String SESSION_FILE_NAME = "ssh_sessions.json";

    public SessionManager() {
        loadSessions();
    }

    public List<SessionStatus> getSessions() {
        return sessions;
    }

    public void loadSessions() {
        File sessionFile = fileManager.getFilePath(SESSION_FILE_NAME).toFile();

        if (!sessionFile.exists() || sessionFile.length() == 0) {
            return;  // Nothing to load
        }

        try {
            List<SessionStatusData> sessionDataList = objectMapper.readValue(
                    sessionFile, new TypeReference<List<SessionStatusData>>() {}
            );
            for (SessionStatusData data : sessionDataList) {
                SessionStatus session = new SessionStatus(
                        data.getSessionNumber(),
                        data.getConnectionName(),
                        data.getProfileId(),
                        data.getSshHost(),
                        data.getLocalHost(),
                        data.getLocalPort(),
                        data.getRemoteHost(),
                        data.getRemotePort(),
                        data.getMode().equals("-L"),
                        "Disconnected"
                );
                sessions.add(session);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveSessions() {
        try {
            List<SessionStatusData> sessionDataList = sessions.stream()
                    .map(session -> new SessionStatusData(
                            session.getSessionNumber(),
                            session.getSessionId(),
                            session.getConnectionName(),
                            session.getProfileId(),
                            session.getSshHost(),
                            session.getLocalHost(),
                            session.getLocalPort(),
                            session.getRemoteHost(),
                            session.getRemotePort(),
                            session.getMode()
                    ))
                    .collect(Collectors.toList());

            File sessionFile = fileManager.getFilePath(SESSION_FILE_NAME).toFile();
            objectMapper.writeValue(sessionFile, sessionDataList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Stop session method
    public void stopSession(SessionStatus session) {
        session.stopTimer();
        if (session.getSshTunnelManager() != null) {
            session.getSshTunnelManager().disconnect();
            session.setSshTunnelManager(null); // Remove the reference to the manager
        }
        session.setStatus("Stopped");
        saveSessions();
    }

    // Modify addSession to save sessions after adding
    public void addSession(SessionStatus session) {
        sessions.add(session);
        saveSessions();
    }

    // Nested class for session data serialization
    public static class SessionStatusData {
        private String sessionNumber;
        private String sessionId;
        private String connectionName;
        private String profileId;
        private String sshHost;
        private String localHost;
        private String localPort;
        private String remoteHost;
        private String remotePort;
        private String mode;

        // Constructors, getters, and setters

        public SessionStatusData() {}

        public SessionStatusData(String sessionNumber, String sessionId, String connectionName, String profileId, String sshHost,
                                 String localHost, String localPort, String remoteHost, String remotePort, String mode) {
            this.sessionNumber = sessionNumber;
            this.sessionId = sessionId;
            this.connectionName = connectionName;
            this.profileId = profileId;
            this.sshHost = sshHost;
            this.localHost = localHost;
            this.localPort = localPort;
            this.remoteHost = remoteHost;
            this.remotePort = remotePort;
            this.mode = mode;
        }

        public String getSessionNumber() {
            return sessionNumber;
        }

        public void setSessionNumber(String sessionNumber) {
            this.sessionNumber = sessionNumber;
        }

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        public String getConnectionName() {
            return connectionName;
        }

        public void setConnectionName(String connectionName) {
            this.connectionName = connectionName;
        }

        public String getProfileId() {
            return profileId;
        }

        public void setProfileId(String profileId) {
            this.profileId = profileId;
        }

        public String getSshHost() {
            return sshHost;
        }

        public void setSshHost(String sshHost) {
            this.sshHost = sshHost;
        }

        public String getLocalHost() {
            return localHost;
        }

        public void setLocalHost(String localHost) {
            this.localHost = localHost;
        }

        public String getLocalPort() {
            return localPort;
        }

        public void setLocalPort(String localPort) {
            this.localPort = localPort;
        }

        public String getRemoteHost() {
            return remoteHost;
        }

        public void setRemoteHost(String remoteHost) {
            this.remoteHost = remoteHost;
        }

        public String getRemotePort() {
            return remotePort;
        }

        public void setRemotePort(String remotePort) {
            this.remotePort = remotePort;
        }

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }
    }
}
