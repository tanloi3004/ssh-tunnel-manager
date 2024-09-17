package com.harveyvo.java.tunnel;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class Main extends Application {
    private SessionManager sessionManager;
    private SessionTable sessionTable;
    private ProfileManager profileManager;
    private final LogDialog logDialog = new LogDialog();
    private final LogManager logManager = LogManager.getInstance();
    private List<SSHProfile> profiles;
    private SSHProfile selectedProfile;
    private MainUI mainUI;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("SSH Tunnel Manager");

        profileManager = new ProfileManager();
        sessionManager = new SessionManager();

        sessionTable = new SessionTable(sessionManager);
        mainUI = new MainUI(profileManager.loadProfiles(), this::loadProfile, this::addNewSession, new LogDialog());

        // Load sessions into the session table
        for (SessionStatus session : sessionManager.getSessions()) {
            sessionTable.addSession(session);
        }

        VBox vbox = new VBox(20, mainUI.getGridPane(), sessionTable.getSessionTable());
        Scene scene = new Scene(vbox, 840, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    private void loadProfile(String profileName) {
        SSHProfile profile = profiles.stream()
                .filter(p -> p.getProfileName().equals(profileName))
                .findFirst()
                .orElse(null);

        if (profile != null) {
            selectedProfile = profile;
            mainUI.updateProfileInfo(profile);
        }
    }
    private void addNewSession() {
        SSHProfile selectedProfile = mainUI.getSelectedProfile();
        if (selectedProfile == null) {
            mainUI.showAlert("No Profile Selected", "Please select an SSH profile before starting a session.");
            return;
        }

        if (!mainUI.validatePorts()) {
            return;
        }

        String connectionName = mainUI.getConnectionName();
        if (connectionName == null || connectionName.isEmpty()) {
            mainUI.showAlert("No Connection Name", "Please enter a connection name.");
            return;
        }

        int sessionCount = sessionManager.getSessions().size() + 1;

        SessionStatus session = new SessionStatus(
                String.valueOf(sessionCount),
                connectionName,
                selectedProfile.getProfileName(),
                selectedProfile.getSshHost(),
                mainUI.getLocalHost(),
                mainUI.getLocalPort(),
                mainUI.getRemoteHost(),
                mainUI.getRemotePort(),
                mainUI.isLocalForwardingSelected(),
                "Connecting..."
        );
        // Create SSHTunnelManager with log consumer
        SSHTunnelManager sshTunnelManager = new SSHTunnelManager(session, message -> {
            logManager.log(message, LogManager.LogLevel.INFO);
        });
        session.setSshTunnelManager(sshTunnelManager);

        sessionTable.addSession(session);

        boolean isLocalForwarding = mainUI.isLocalForwardingSelected();

        Task<Void> sessionTask = new Task<>() {
            @Override
            protected Void call() {
                try {
                    sshTunnelManager.connect(selectedProfile);

                    if (isLocalForwarding) {
                        sshTunnelManager.setUpTunnel(mainUI.getLocalHost(), Integer.parseInt(mainUI.getLocalPort()),
                                mainUI.getRemoteHost(), Integer.parseInt(mainUI.getRemotePort()));
                    } else {
                        sshTunnelManager.setUpRemoteTunnel(mainUI.getRemoteHost(), Integer.parseInt(mainUI.getRemotePort()),
                                mainUI.getLocalHost(), Integer.parseInt(mainUI.getLocalPort()));
                    }

                    Platform.runLater(() -> {
                        session.setStatus("Connected");
                        session.startTimer(() -> {
                            // Update data usage periodically
                            long sent = sshTunnelManager.getBytesSent();
                            long received = sshTunnelManager.getBytesReceived();
                            session.updateDataUsage(sent, received);
                        });
                        sessionManager.saveSessions();
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> session.setStatus("Failed: " + e.getMessage()));
                }
                return null;
            }
        };

        new Thread(sessionTask).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
