package com.harveyvo.java.tunnel;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class LogDialog {

    private StringBuilder logMessages;

    public LogDialog(StringBuilder logMessages) {
        this.logMessages = logMessages;
    }

    // Get the log messages
    public StringBuilder getLogMessages() {
        return logMessages;
    }

    // Add a new log message
    public void log(String message) {
        logMessages.append(message).append("\n");
        System.out.println(message);  // Also print to console for debugging
    }

    // Show the log dialog
    public void showLogDialog(Stage parentStage) {
        Stage dialog = new Stage();
        dialog.setTitle("Log Dialog");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(parentStage);

        TextArea logTextArea = new TextArea(logMessages.toString());
        logTextArea.setWrapText(true);
        logTextArea.setEditable(false);  // Make it read-only

        VBox dialogVbox = new VBox(10);
        dialogVbox.setPadding(new Insets(10));
        dialogVbox.getChildren().add(logTextArea);

        Scene dialogScene = new Scene(dialogVbox, 500, 400);
        dialog.setScene(dialogScene);
        dialog.show();
    }
}
