package com.harveyvo.java.tunnel;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class Main extends Application {

    private SessionManager sessionManager = new SessionManager();
    private SessionTable sessionTable;
    private ProfileManager profileManager = new ProfileManager();
    private LogDialog logDialog = new LogDialog(new StringBuilder());
    private List<SSHProfile> profiles;

    // Fields for showing SSH connection info (read-only)
    private Label sshHostLabel, sshPortLabel, usernameLabel;

    // Fields for tunnel info (entered per session)
    private TextField localHostField, localPortField, remoteHostField, remotePortField;

    // Radio buttons for tunneling mode
    private RadioButton localForwardingButton, remoteForwardingButton;

    private SSHProfile selectedProfile;

    // Labels for showing flow description, listening side, and guidelines
    private Label flowDescriptionLabel, listeningSideLabel;
    private Label localPortGuideline, remotePortGuideline;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("SSH Tunnel Manager");

        profiles = profileManager.loadProfiles();
        sessionTable = new SessionTable(sessionManager);

        sshHostLabel = new Label("...");
        sshPortLabel = new Label("...");
        usernameLabel = new Label("...");

        // Tunnel fields (specific to each session)
        localHostField = new TextField("127.0.0.1");
        localPortField = new TextField("8080");
        remoteHostField = new TextField("0.0.0.0");
        remotePortField = new TextField();

        // Guidelines for port numbers
        localPortGuideline = new Label("Valid range: 1-65535");
        remotePortGuideline = new Label("Valid range: 1-65535");

        // RadioButtons for tunneling mode
        localForwardingButton = new RadioButton("Local Forwarding (-L)");
        remoteForwardingButton = new RadioButton("Reverse Tunneling (-R)");
        ToggleGroup tunnelModeGroup = new ToggleGroup();
        localForwardingButton.setToggleGroup(tunnelModeGroup);
        remoteForwardingButton.setToggleGroup(tunnelModeGroup);
        localForwardingButton.setSelected(true);

        // Create labels to show the dynamic flow description and the listening side
        flowDescriptionLabel = new Label();
        listeningSideLabel = new Label();
        updateFlowDescription(); // Initialize the flow description
        updateListeningSide(); // Initialize the listening side

        // Add listeners to input fields and buttons to update flow description and listening side dynamically
        addFieldListeners();

        ComboBox<String> profileComboBox = new ComboBox<>();
        profileComboBox.setPrefWidth(150);
        profileComboBox.getItems().addAll(profiles.stream().map(SSHProfile::getProfileName).toList());
        profileComboBox.setOnAction(event -> loadProfile(profileComboBox.getValue()));

        Button openProfileManagerButton = new Button("Manage Profiles");
        openProfileManagerButton.setOnAction(event -> new ProfileManagerDialog(logDialog.getLogMessages(), profileComboBox).showProfileManager(primaryStage, null));

        Button addSessionButton = new Button("Add New Session");
        addSessionButton.setOnAction(event -> addNewSession());

        Button viewLogsButton = new Button("View Logs");
        viewLogsButton.setOnAction(event -> logDialog.showLogDialog(primaryStage));

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(10);
        grid.setHgap(10);

        grid.add(new Label("Profile:"), 0, 0);
        grid.add(profileComboBox, 1, 0, 2, 1);
        grid.add(openProfileManagerButton, 3, 0);

        grid.add(new Label("SSH Host:"), 0, 1);
        grid.add(sshHostLabel, 1, 1);

        grid.add(new Label("SSH Port:"), 0, 2);
        grid.add(sshPortLabel, 1, 2);

        grid.add(new Label("Username:"), 0, 3);
        grid.add(usernameLabel, 1, 3);

        grid.add(new Label("Local Host:"), 0, 5);
        grid.add(localHostField, 1, 5);

        grid.add(new Label("Local Port:"), 0, 6);
        grid.add(localPortField, 1, 6);
        grid.add(localPortGuideline, 2, 6,2,1); // Add guideline for local port

        grid.add(new Label("Remote Host:"), 0, 7);
        grid.add(remoteHostField, 1, 7);

        grid.add(new Label("Remote Port:"), 0, 8);
        grid.add(remotePortField, 1, 8);
        grid.add(remotePortGuideline, 2, 8,2,1); // Add guideline for remote port

        grid.add(new Label("Tunneling Mode:"), 0, 9);
        grid.add(new VBox(5, localForwardingButton, remoteForwardingButton), 1, 9);

        // Add the dynamic flow description and listening side labels below the tunnel fields
        grid.add(new Label("Flow Description:"), 0, 10);
        grid.add(flowDescriptionLabel, 1, 10, 4, 1);

        grid.add(new Label(""), 0, 11); // New label for listening side
        grid.add(listeningSideLabel, 1, 11, 4, 1); // Display the listening side

        grid.add(addSessionButton, 0, 12);
        grid.add(viewLogsButton, 1, 12);

        VBox vbox = new VBox(20, grid, sessionTable.getSessionTable());
        Scene scene = new Scene(vbox, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void addFieldListeners() {
        // Listeners for text fields and radio buttons to update the flow description and listening side dynamically
        ChangeListener<String> textChangeListener = (observable, oldValue, newValue) -> {
            updateFlowDescription();
            updateListeningSide();
        };
        localHostField.textProperty().addListener(textChangeListener);
        localPortField.textProperty().addListener(textChangeListener);
        remoteHostField.textProperty().addListener(textChangeListener);
        remotePortField.textProperty().addListener(textChangeListener);

        // Listener for tunneling mode change
        localForwardingButton.setOnAction(event -> {
            updateFlowDescription();
            updateListeningSide();
        });
        remoteForwardingButton.setOnAction(event -> {
            updateFlowDescription();
            updateListeningSide();
        });
    }

    // Method to update the flow description based on the current mode and tunnel details
    private void updateFlowDescription() {
        String localHost = localHostField.getText();
        String localPort = localPortField.getText();
        String remoteHost = remoteHostField.getText();
        String remotePort = remotePortField.getText();

        if (localForwardingButton.isSelected()) {
            // Local forwarding flow
            flowDescriptionLabel.setText("Traffic flows from localhost:" + localPort + " -> " +
                    remoteHost + ":" + remotePort + " through the SSH server.");
        } else if (remoteForwardingButton.isSelected()) {
            // Remote forwarding flow
            flowDescriptionLabel.setText("Traffic flows from " + remoteHost + ":" + remotePort + " -> " +
                    localHost + ":" + localPort + " through the SSH server.");
        }
    }

    // Method to update which side is listening for traffic
    private void updateListeningSide() {
        String localHost = localHostField.getText();
        String localPort = localPortField.getText();
        String remoteHost = remoteHostField.getText();
        String remotePort = remotePortField.getText();

        if (localForwardingButton.isSelected()) {
            // Local forwarding: Local side is listening
            listeningSideLabel.setText("Listening on **localhost:" + localPort + "**. Forwarding to " +
                    remoteHost + ":" + remotePort + ".");
        } else if (remoteForwardingButton.isSelected()) {
            // Remote forwarding: Remote side is listening
            listeningSideLabel.setText("Listening on **" + remoteHost + ":" + remotePort + "**. Forwarding to " +
                    "localhost:" + localPort + ".");
        }
    }

    // Method to validate port numbers
    private boolean validatePort(String portStr) {
        try {
            int port = Integer.parseInt(portStr);
            return port >= 1 && port <= 65535;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void loadProfile(String profileName) {
        SSHProfile profile = profiles.stream()
                .filter(p -> p.getProfileName().equals(profileName))
                .findFirst()
                .orElse(null);

        if (profile != null) {
            selectedProfile = profile;
            sshHostLabel.setText(profile.getSshHost());
            sshPortLabel.setText(String.valueOf(profile.getSshPort()));
            usernameLabel.setText(profile.getUsername());
        }
    }

    private void addNewSession() {
        if (selectedProfile == null) {
            showAlert("No Profile Selected", "Please select an SSH profile before starting a session.");
            return;
        }

        // Validate the port numbers
        if (!validatePort(localPortField.getText())) {
            showAlert("Invalid Local Port", "Please enter a valid local port number (1-65535).");
            return;
        }

        if (!validatePort(remotePortField.getText())) {
            showAlert("Invalid Remote Port", "Please enter a valid remote port number (1-65535).");
            return;
        }

        int sessionCount = sessionManager.getSessions().size() + 1;

        SessionStatus session = new SessionStatus(
                String.valueOf(sessionCount),
                selectedProfile.getSshHost(),
                localHostField.getText(),
                localPortField.getText(),
                remoteHostField.getText(),
                remotePortField.getText(),
                "Connecting..."
        );

        sessionTable.addSession(session);

        boolean isLocalForwarding = localForwardingButton.isSelected();

        Task<Void> sessionTask = sessionManager.createSessionTask(
                selectedProfile,
                localHostField.getText(),
                Integer.parseInt(localPortField.getText()),
                remoteHostField.getText(),
                Integer.parseInt(remotePortField.getText()),
                isLocalForwarding,
                session
        );

        new Thread(sessionTask).start();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
