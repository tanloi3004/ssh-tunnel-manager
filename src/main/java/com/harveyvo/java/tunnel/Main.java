package com.harveyvo.java.tunnel;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class Main extends Application {

    private final SessionManager sessionManager = new SessionManager();
    private SessionTable sessionTable;
    private final ProfileManager profileManager = new ProfileManager();
    private final LogDialog logDialog = new LogDialog();
    private List<SSHProfile> profiles;
    private SSHProfile selectedProfile;
    private MainUI mainUI;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("SSH Tunnel Manager");

        profiles = profileManager.loadProfiles();
        sessionTable = new SessionTable(sessionManager);

        mainUI = new MainUI(profiles, this::loadProfile, this::addNewSession, logDialog);

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
        if (selectedProfile == null) {
            mainUI.showAlert("No Profile Selected", "Please select an SSH profile before starting a session.");
            return;
        }

        if (!mainUI.validatePorts()) {
            return;
        }

        int sessionCount = sessionManager.getSessions().size() + 1;

        SessionStatus session = new SessionStatus(
                String.valueOf(sessionCount),
                selectedProfile.getSshHost(),
                mainUI.getLocalHost(),
                mainUI.getLocalPort(),
                mainUI.getRemoteHost(),
                mainUI.getRemotePort(),
                mainUI.isLocalForwardingSelected(),
                "Connecting..."
        );

        sessionTable.addSession(session);

        boolean isLocalForwarding = mainUI.isLocalForwardingSelected();

        Task<Void> sessionTask = sessionManager.createSessionTask(
                selectedProfile,
                mainUI.getLocalHost(),
                Integer.parseInt(mainUI.getLocalPort()),
                mainUI.getRemoteHost(),
                Integer.parseInt(mainUI.getRemotePort()),
                isLocalForwarding,
                session
        );

        new Thread(sessionTask).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
