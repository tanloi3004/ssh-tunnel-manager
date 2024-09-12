package com.harveyvo.java.tunnel;

import javafx.application.Platform;
import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SessionManager {
    private final List<SessionStatus> sessions = new ArrayList<>();
    private final SSHTunnelManager sshTunnelManager = new SSHTunnelManager();

    public List<SessionStatus> getSessions() {
        return sessions;
    }

    // Add new session
    public void addSession(SessionStatus session, Runnable onSessionUpdate) {
        sessions.add(session);
        session.startTimer(onSessionUpdate); // Start the session timer

        // Periodically update data usage
        TimerTask updateDataUsageTask = new TimerTask() {
            @Override
            public void run() {
                long sent = sshTunnelManager.getBytesSent();
                long received = sshTunnelManager.getBytesReceived();
                Platform.runLater(() -> session.updateDataUsage(sent, received));
            }
        };
        new Timer(true).scheduleAtFixedRate(updateDataUsageTask, 1000, 5000); // Update every 5 seconds
    }

    // Stop a session
    public void stopSession(SessionStatus session) {
        session.stopTimer(); // Stop the timer
        sshTunnelManager.disconnect(); // Disconnect the SSH session
        session.setStatus("Stopped");
    }

    // Create a task to run an SSH session in the background using SSHProfile
    public Task<Void> createSessionTask(SSHProfile profile,
                                        String localHost, int localPort,
                                        String remoteHost, int remotePort,
                                        boolean isLocalForwarding, SessionStatus session) {
        return new Task<>() {
            @Override
            protected Void call() {
                try {
                    // Connect using the SSHProfile (either password or SSH key)
                    sshTunnelManager.connect(profile);

                    // Set up the tunnel (local or remote forwarding)
                    if (isLocalForwarding) {
                        sshTunnelManager.setUpTunnel(localPort, remoteHost, remotePort);
                    } else {
                        sshTunnelManager.setUpRemoteTunnel(localHost, localPort, remoteHost, remotePort);
                    }

                    // Session successfully connected: Update status and start the timer
                    Platform.runLater(() -> {
                        session.setStatus("Connected");
                        session.startTimer(() -> {});
                        addSession(session, () -> {});  // Add to session manager after connection
                    });

                } catch (Exception e) {
                    // Handle failure and update session status
                    Platform.runLater(() -> session.setStatus("Failed: " + e.getMessage()));
                }
                return null;
            }
        };
    }
}
