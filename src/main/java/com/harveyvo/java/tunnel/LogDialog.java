package com.harveyvo.java.tunnel;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LogDialog {

    private final LogManager logManager = LogManager.getInstance();
    private TextArea logTextArea;
    private String lastLogContent = ""; // To store the last displayed log

    // Show the log dialog and auto-refresh
    public void showLogDialog(Stage parentStage) {
        Stage dialog = new Stage();
        dialog.setTitle("Log Dialog");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(parentStage);

        logTextArea = new TextArea();
        logTextArea.setWrapText(true);
        logTextArea.setEditable(false);  // Make it read-only
        logTextArea.setPrefHeight(450);  // Increase the height of the text area

        VBox dialogVbox = new VBox(10);
        dialogVbox.setPadding(new Insets(10));
        dialogVbox.getChildren().add(logTextArea);

        Scene dialogScene = new Scene(dialogVbox, 800, 800);  // Adjust window size accordingly
        dialog.setScene(dialogScene);
        dialog.show();

        // Load initial log content (last 100 lines)
        lastLogContent = logManager.getLast100Lines();
        logTextArea.setText(lastLogContent);

        // Auto-refresh every 5 seconds to check for new log content
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> {
            String currentLogContent = logManager.getLast100Lines();

            // Only update the TextArea if there's new content
            if (!currentLogContent.equals(lastLogContent)) {
                // Find the new lines by comparing the old and new log contents
                String newLog = currentLogContent.substring(lastLogContent.length());
                logTextArea.appendText(newLog);  // Append only the new content
                lastLogContent = currentLogContent;  // Update the last log content
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
}
