package com.harveyvo.java.tunnel;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.StringConverter;

public class MainUI {

    private Label sshHostLabel, sshPortLabel, usernameLabel;
    private TextField connectionNameField;
    private ComboBox<SSHProfile> profileComboBox;
    private ObservableList<SSHProfile> profiles;
    private SSHProfile selectedProfile;
    private TextField localHostField, localPortField, remoteHostField, remotePortField;
    private RadioButton localForwardingButton, remoteForwardingButton;
    private Label flowDescriptionLabel, listeningSideLabel, localPortGuideline, remotePortGuideline;
    private final GridPane grid;
    private final Consumer<String> loadProfileCallback;
    private final Runnable addNewSessionCallback;
    private final LogDialog logDialog;

    // Callback for refreshing the profile list
    private final Runnable refreshProfilesCallback;

    // Instance of HelpDialog
    private final HelpDialog helpDialog = new HelpDialog();

    public MainUI(List<SSHProfile> profiles, Consumer<String> loadProfileCallback, Runnable addNewSessionCallback, LogDialog logDialog) {
        this.profiles = FXCollections.observableArrayList(profiles);
        this.loadProfileCallback = loadProfileCallback;
        this.addNewSessionCallback = addNewSessionCallback;
        this.logDialog = logDialog;

        sshHostLabel = new Label("...");
        sshPortLabel = new Label("...");
        usernameLabel = new Label("...");

        // Initialize UI components
        connectionNameField = new TextField();
        connectionNameField.setPromptText("Connection Name");

        localHostField = new TextField("127.0.0.1");
        localPortField = new TextField("8080");
        remoteHostField = new TextField("0.0.0.0");
        remotePortField = new TextField();

        localPortGuideline = new Label("Valid range: 1-65535");
        remotePortGuideline = new Label("Valid range: 1-65535");

        localForwardingButton = new RadioButton("Local Forwarding (-L)");
        remoteForwardingButton = new RadioButton("Reverse Tunneling (-R)");
        ToggleGroup tunnelModeGroup = new ToggleGroup();
        localForwardingButton.setToggleGroup(tunnelModeGroup);
        remoteForwardingButton.setToggleGroup(tunnelModeGroup);
        localForwardingButton.setSelected(true);

        flowDescriptionLabel = new Label();
        listeningSideLabel = new Label();
        updateFlowDescription();
        addFieldListeners();

        profileComboBox = new ComboBox<SSHProfile>();
        profileComboBox.setItems(this.profiles);

        profileComboBox.setConverter(new StringConverter<SSHProfile>() {
            @Override
            public String toString(SSHProfile profile) {
                return profile != null ? profile.getProfileName() : "";
            }

            @Override
            public SSHProfile fromString(String string) {
                if (string == null || string.isEmpty()) {
                    return null;
                }
                return profiles.stream()
                        .filter(p -> p.getProfileName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        profileComboBox.setPrefWidth(150);
        profileComboBox.setOnAction(event -> {
            selectedProfile = profileComboBox.getValue();
            if (selectedProfile != null) {
                updateProfileInfo(selectedProfile);
            } else {
                updateProfileInfo(null);
            }
        });

        refreshProfilesCallback = () -> {
            String selectedProfileName = profileComboBox.getValue() != null ? profileComboBox.getValue().getProfileName() : null;

            // Reload profiles from the ProfileManager
            List<SSHProfile> updatedProfiles = new ProfileManager().loadProfiles();

            this.profiles.setAll(updatedProfiles);

            // Re-select the previously selected profile if it still exists
            if (selectedProfileName != null) {
                SSHProfile previouslySelected = profiles.stream()
                        .filter(p -> p.getProfileName().equals(selectedProfileName))
                        .findFirst()
                        .orElse(null);

                if (previouslySelected != null) {
                    profileComboBox.setValue(previouslySelected);
                    updateProfileInfo(previouslySelected);
                } else {
                    // If the selected profile was deleted, clear the selection
                    profileComboBox.setValue(null);
                    updateProfileInfo(null);
                }
            }
        };


        Button manageProfilesButton = new Button("Manage Profiles");
        manageProfilesButton.setOnAction(event -> {
            ProfileManagerDialog profileManagerDialog = new ProfileManagerDialog(refreshProfilesCallback);
            profileManagerDialog.showProfileManager(null, null); // Passing null as owner and profile
        });


        Button addSessionButton = new Button("Add New Session");
        addSessionButton.setOnAction(event -> addNewSessionCallback.run());

        // Help button to open the HelpDialog
        Button helpButton = new Button("Help");
        helpButton.setOnAction(event -> helpDialog.showHelpDialog(null));

        grid = new GridPane();
        grid.setPadding(new Insets(15));
        setupGridLayout(addSessionButton, manageProfilesButton, helpButton); // Now including the Help button
    }

    public void updateProfileInfo(SSHProfile profile) {
        if (profile != null) {
            sshHostLabel.setText(profile.getSshHost());
            sshPortLabel.setText(String.valueOf(profile.getSshPort()));
            usernameLabel.setText(profile.getUsername());
        } else {
            sshHostLabel.setText("...");
            sshPortLabel.setText("...");
            usernameLabel.setText("...");
        }
    }

    public GridPane getGridPane() {
        return grid;
    }
    public String getLocalHost() {
        return localHostField.getText();
    }

    public String getLocalPort() {
        return localPortField.getText();
    }

    public String getRemoteHost() {
        return remoteHostField.getText();
    }

    public String getRemotePort() {
        return remotePortField.getText();
    }

    public boolean isLocalForwardingSelected() {
        return localForwardingButton.isSelected();
    }
    public String getConnectionName() {
        return connectionNameField.getText();
    }

    public SSHProfile getSelectedProfile() {
        return selectedProfile;
    }
    public boolean validatePorts() {
        if (!validatePort(localPortField.getText())) {
            showAlert("Invalid Local Port", "Please enter a valid local port number (1-65535).");
            return false;
        }

        if (!validatePort(remotePortField.getText())) {
            showAlert("Invalid Remote Port", "Please enter a valid remote port number (1-65535).");
            return false;
        }

        return true;
    }

    public void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void addFieldListeners() {
        // Listener for text fields and buttons
        localHostField.textProperty().addListener((observable, oldValue, newValue) -> updateFlowDescription());
        localPortField.textProperty().addListener((observable, oldValue, newValue) -> updateFlowDescription());
        remoteHostField.textProperty().addListener((observable, oldValue, newValue) -> updateFlowDescription());
        remotePortField.textProperty().addListener((observable, oldValue, newValue) -> updateFlowDescription());

        localForwardingButton.setOnAction(event -> updateFlowDescription());
        remoteForwardingButton.setOnAction(event -> updateFlowDescription());
    }

    private void setupGridLayout(Button addSessionButton, Button manageProfilesButton, Button helpButton) {
        grid.setVgap(10);
        grid.setHgap(10);

        grid.add(new Label("Profile:"), 0, 0);
        grid.add(profileComboBox, 1, 0, 2, 1);
        grid.add(manageProfilesButton, 3, 0);  // Add "Manage Profiles" button
        // Modify the grid to include the connection name field
        grid.add(new Label("Connection Name:"), 0, 4);
        grid.add(connectionNameField, 1, 4);

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
        grid.add(localPortGuideline, 2, 6, 2, 1);

        grid.add(new Label("Remote Host:"), 0, 7);
        grid.add(remoteHostField, 1, 7);

        grid.add(new Label("Remote Port:"), 0, 8);
        grid.add(remotePortField, 1, 8);
        grid.add(remotePortGuideline, 2, 8, 2, 1);

        grid.add(new Label("Tunneling Mode:"), 0, 9);
        grid.add(new VBox(5, localForwardingButton, remoteForwardingButton), 1, 9);

        grid.add(new Label("Flow Description:"), 0, 10);
        grid.add(flowDescriptionLabel, 1, 10, 4, 1);

        grid.add(new Label(""), 0, 11); // New label for listening side
        grid.add(listeningSideLabel, 1, 11, 4, 1);

        grid.add(addSessionButton, 0, 12);

        Button viewLogsButton = new Button("View Logs");
        viewLogsButton.setOnAction(event -> logDialog.showLogDialog(null));
        grid.add(viewLogsButton, 1, 12);

        // Add the Help button to the grid
        grid.add(helpButton, 2, 9);  // Help button added in row 9
    }

    private boolean validatePort(String portStr) {
        try {
            int port = Integer.parseInt(portStr);
            return port >= 1 && port <= 65535;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void updateFlowDescription() {
        String localHost = localHostField.getText();
        String localPort = localPortField.getText();
        String remoteHost = remoteHostField.getText();
        String remotePort = remotePortField.getText();

        if (localForwardingButton.isSelected()) {
            flowDescriptionLabel.setText("Traffic flows from localhost:" + localPort + " -> " +
                    remoteHost + ":" + remotePort + " through the SSH server.");
            listeningSideLabel.setText("Listening on LOCAL (" + localHost + ":" + localPort + ")");
        } else if (remoteForwardingButton.isSelected()) {
            flowDescriptionLabel.setText("Traffic flows from " + remoteHost + ":" + remotePort + " -> " +
                    localHost + ":" + localPort + " through the SSH server.");
            listeningSideLabel.setText("Listening on REMOTE (" + remoteHost + ":" + remotePort + ")");
        }
    }
}
