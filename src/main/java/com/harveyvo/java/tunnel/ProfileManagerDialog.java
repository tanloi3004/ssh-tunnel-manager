package com.harveyvo.java.tunnel;

import com.harveyvo.java.tunnel.SSHProfile.AuthMethod;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class ProfileManagerDialog {

    private ProfileManager profileManager;
    private ComboBox<String> profileComboBox;
    private ListView<String> profileListView;
    private TextField profileNameField;
    private TextField sshHostField;
    private TextField sshPortField;
    private TextField usernameField;
    private PasswordField passwordField;
    private TextArea sshKeyTextArea;
    private PasswordField sshKeyPassphraseField;
    private ComboBox<SSHProfile.AuthMethod> authMethodComboBox;
    private Runnable onProfileChangeCallback;  // Callback for triggering ComboBox reload
    private String sshKeyContent;
    private TextArea consoleLogTextArea; //
    private SSHProfile currentProfile;
    private Runnable refreshProfilesCallback;

    public ProfileManagerDialog(Runnable refreshProfilesCallback) {
        this.refreshProfilesCallback = refreshProfilesCallback;
        this.profileManager = new ProfileManager();
    }

    public ProfileManagerDialog(ComboBox<String> profileComboBox, Runnable onProfileChangeCallback) {
        this.profileComboBox = profileComboBox;
        this.onProfileChangeCallback = onProfileChangeCallback;
    }

    public void showProfileManager(Stage parentStage, SSHProfile profileToEdit) {
        Stage dialog = new Stage();
        dialog.setTitle("Profile Manager");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(parentStage);
        this.profileListView = new ListView<>();
        this.refreshProfileList();
        this.profileListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.loadProfileForEditing(newValue);
                this.toggleSSHKeyFields();
            }

        });
        this.profileNameField = new TextField();
        this.profileNameField.setPromptText("Profile Name");
        this.sshHostField = new TextField();
        this.sshHostField.setPromptText("SSH Host");
        this.sshPortField = new TextField("22");
        this.usernameField = new TextField();
        this.usernameField.setPromptText("Username");
        this.passwordField = new PasswordField();
        this.passwordField.setPrefWidth(200);
        this.passwordField.setPromptText("Password");
        this.sshKeyTextArea = new TextArea(); // Use TextArea for editable SSH key content
        this.sshKeyTextArea.setPrefRowCount(10);
        this.sshKeyTextArea.setPromptText("Paste or load your SSH Key content here...");
        this.sshKeyPassphraseField = new PasswordField();
        this.sshKeyPassphraseField.setPromptText("SSH Key Passphrase (Optional)");
        this.authMethodComboBox = new ComboBox<>();
        this.authMethodComboBox.getItems().addAll(AuthMethod.PASSWORD, AuthMethod.SSH_KEY);
        this.authMethodComboBox.setValue(AuthMethod.PASSWORD);
        this.authMethodComboBox.setOnAction((event) -> {
            this.toggleSSHKeyFields();
        });
        Button selectKeyButton = new Button("Select SSH Key File");
        selectKeyButton.setOnAction((event) -> {
            this.selectSSHKeyFile();
        });
        Button testConnectionButton = new Button("Test Connection");
        testConnectionButton.setOnAction((event) -> {
            this.testConnection();
        });
        Button saveButton = new Button("Save Profile");
        saveButton.setOnAction(event -> {
            saveProfile(dialog);
//            refreshProfileList();
//            refreshProfileComboBox();  // Refresh ComboBox in main UI
//            if (onProfileChangeCallback != null) {
//                onProfileChangeCallback.run();  // Trigger the callback to refresh profiles in main UI
//            }
            dialog.close();
        });

        Button deleteButton = new Button("Delete Profile");
        deleteButton.setOnAction(event -> {
            deleteProfile();
//            refreshProfileList();
//            refreshProfileComboBox();  // Refresh ComboBox in main UI
            if (onProfileChangeCallback != null) {
                onProfileChangeCallback.run();  // Trigger the callback to refresh profiles in main UI
            }
        });

        // Create a TextArea for console logs
        consoleLogTextArea = new TextArea();
        consoleLogTextArea.setEditable(false);
        consoleLogTextArea.setPromptText("Console logs will be shown here...");
        consoleLogTextArea.setWrapText(true);
        consoleLogTextArea.setPrefColumnCount(4);
//        consoleLogTextArea.setPrefHeight(100); // Set a preferred height for the log area

        GridPane formGrid = new GridPane();
        formGrid.setPadding(new Insets(10.0));
        formGrid.setVgap(10.0);
        formGrid.setHgap(10.0);
        formGrid.add(new Label("Profile Name:"), 0, 0);
        formGrid.add(profileNameField, 1, 0);
        formGrid.add(new Label("SSH Host:"), 0, 1);
        formGrid.add(sshHostField, 1, 1);
        formGrid.add(new Label("SSH Port:"), 0, 2);
        formGrid.add(sshPortField, 1, 2);
        formGrid.add(new Label("Username:"), 0, 3);
        formGrid.add(usernameField, 1, 3);
        formGrid.add(new Label("Auth Method:"), 0, 4);
        formGrid.add(authMethodComboBox, 1, 4);
        formGrid.add(new Label("Password:"), 0, 5);
        formGrid.add(this.passwordField, 1, 5, 2, 1);
        formGrid.add(new Label("SSH Key:"), 0, 6);
        formGrid.add(this.sshKeyTextArea, 1, 6, 2, 1); // Adjust TextArea to span across more columns
        formGrid.add(selectKeyButton, 3, 6);
        formGrid.add(new Label("SSH Key Passphrase:"), 0, 7);
        formGrid.add(this.sshKeyPassphraseField, 1, 7, 2, 1);

        HBox buttonBox = new HBox(10.0, saveButton, deleteButton, testConnectionButton);
        VBox vbox = new VBox(10.0, new Label("Profiles:"), this.profileListView, formGrid, new Label("Console Logs:"), consoleLogTextArea, buttonBox);
        vbox.setPadding(new Insets(20.0));
        Scene dialogScene = new Scene(vbox, 600.0, 600.0); // Increased height for the new log section
        dialog.setScene(dialogScene);
        dialog.show();
    }

    private void loadProfileForEditing(String profileName) {
        SSHProfile profile = this.profileManager.getProfileByName(profileName);
        if (profile != null) {
            this.currentProfile = profile;
            this.profileNameField.setText(profile.getProfileName());
            this.sshHostField.setText(profile.getSshHost());
            this.sshPortField.setText(String.valueOf(profile.getSshPort()));
            this.usernameField.setText(profile.getUsername());
            this.passwordField.setText(profile.getPassword());
            this.authMethodComboBox.setValue(profile.getAuthMethod());
            this.sshKeyPassphraseField.setText(profile.getPassphrase());
            if (profile.getAuthMethod() == AuthMethod.SSH_KEY) {
                this.sshKeyContent = profile.getSshKeyContent();
                this.sshKeyTextArea.setText(this.sshKeyContent); // Set the SSH key content in the TextArea
            }
        }
    }

    private void toggleSSHKeyFields() {
        boolean isSSHKey = this.authMethodComboBox.getValue() == AuthMethod.SSH_KEY;
        this.passwordField.setDisable(isSSHKey);
        this.sshKeyTextArea.setDisable(!isSSHKey); // Enable or disable the TextArea instead of TextField
        this.sshKeyPassphraseField.setDisable(!isSSHKey);
    }

    private void selectSSHKeyFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select SSH Private Key File");
        File pemFile = fileChooser.showOpenDialog((Window) null);
        if (pemFile != null) {
            try {
                this.sshKeyContent = new String(Files.readAllBytes(pemFile.toPath()));
                this.sshKeyTextArea.setText(this.sshKeyContent); // Display full SSH key content in TextArea
            } catch (IOException e) {
                this.showAlert("Error", "Failed to load SSH key file: " + e.getMessage());
            }
        }
    }

    private void saveProfile(Stage dialog) {
        SSHProfile profile = new SSHProfile(this.profileNameField.getText(), this.sshHostField.getText(), Integer.parseInt(this.sshPortField.getText()), this.usernameField.getText(), this.passwordField.getText(), this.authMethodComboBox.getValue(), this.sshKeyTextArea.getText(), this.sshKeyPassphraseField.getText());
        this.profileManager.saveOrUpdateProfile(profile);
        if (refreshProfilesCallback != null) {
            refreshProfilesCallback.run();
        }
        dialog.close();
    }

    private void testConnection() {
        if (!validateProfileFields()) {
            return;
        }
        final SSHProfile profile = new SSHProfile(this.profileNameField.getText(), this.sshHostField.getText(), Integer.parseInt(this.sshPortField.getText()), this.usernameField.getText(), this.passwordField.getText(), this.authMethodComboBox.getValue(), this.sshKeyTextArea.getText(), this.sshKeyPassphraseField.getText());
        Task<Void> testTask = new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    SSHTunnelManager.testConnection(profile);
                    Platform.runLater(() -> {
                        consoleLogTextArea.appendText("Connection successful.\n");
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        consoleLogTextArea.appendText(e.getMessage() + "\n");
                    });
                }
                return null;
            }
        };
        new Thread(testTask).start();
    }

    private void deleteProfile() {
        String selectedProfile = this.profileListView.getSelectionModel().getSelectedItem();
        if (selectedProfile != null) {
            this.profileManager.deleteProfile(selectedProfile);
            if (refreshProfilesCallback != null) {
                refreshProfilesCallback.run();
            }
        }
    }

    private void refreshProfileList() {
        this.profileListView.getItems().clear();
        List<SSHProfile> profiles = this.profileManager.loadProfiles();
        this.profileListView.getItems().addAll(profiles.stream().map(SSHProfile::getProfileName).toList());
    }
//
//    private void refreshProfileComboBox() {
//        this.profileComboBox.getItems().clear();
//        List<SSHProfile> updatedProfiles = this.profileManager.loadProfiles();
//        this.profileComboBox.getItems().addAll(updatedProfiles.stream().map(SSHProfile::getProfileName).toList());
//    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean validateProfileFields() {
        if (profileNameField.getText().isEmpty() || sshHostField.getText().isEmpty()
                || sshPortField.getText().isEmpty() || usernameField.getText().isEmpty()) {
            showAlert("Validation Error", "Please fill in all required fields.");
            return false;
        }

        try {
            Integer.parseInt(sshPortField.getText());
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "SSH Port must be a valid number.");
            return false;
        }

        if (authMethodComboBox.getValue() == AuthMethod.PASSWORD && passwordField.getText().isEmpty()) {
            showAlert("Validation Error", "Please enter a password for authentication.");
            return false;
        }

        if (authMethodComboBox.getValue() == AuthMethod.SSH_KEY && sshKeyTextArea.getText().isEmpty()) {
            showAlert("Validation Error", "Please provide your SSH key content.");
            return false;
        }

        return true;
    }

}
