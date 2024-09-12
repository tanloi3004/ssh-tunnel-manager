package com.harveyvo.java.tunnel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

public class SessionTable {
    private final TableView<SessionStatus> sessionTable;
    private final ObservableList<SessionStatus> sessionList = FXCollections.observableArrayList();

    public SessionTable(SessionManager sessionManager) {
        sessionTable = new TableView<>();

        // Number order column
        TableColumn<SessionStatus, String> numberCol = new TableColumn<>("No.");
        numberCol.setCellValueFactory(new PropertyValueFactory<>("sessionNumber"));

        // SSH Host column
        TableColumn<SessionStatus, String> sshHostCol = new TableColumn<>("SSH Host");
        sshHostCol.setCellValueFactory(new PropertyValueFactory<>("sshHost"));

        // Local host/port columns
        TableColumn<SessionStatus, String> localHostCol = new TableColumn<>("Local Host");
        localHostCol.setCellValueFactory(new PropertyValueFactory<>("localHost"));

        TableColumn<SessionStatus, String> localPortCol = new TableColumn<>("Local Port");
        localPortCol.setCellValueFactory(new PropertyValueFactory<>("localPort"));

        // Remote host/port columns
        TableColumn<SessionStatus, String> remoteHostCol = new TableColumn<>("Remote Host");
        remoteHostCol.setCellValueFactory(new PropertyValueFactory<>("remoteHost"));

        TableColumn<SessionStatus, String> remotePortCol = new TableColumn<>("Remote Port");
        remotePortCol.setCellValueFactory(new PropertyValueFactory<>("remotePort"));

        // Status and timer columns
        TableColumn<SessionStatus, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<SessionStatus, String> timerCol = new TableColumn<>("Time");
        timerCol.setCellValueFactory(cellData -> cellData.getValue().timerTextProperty());

        // Data usage columns (Bytes Sent/Received)
        TableColumn<SessionStatus, String> bytesSentCol = new TableColumn<>("Bytes Sent");
        bytesSentCol.setCellValueFactory(cellData -> cellData.getValue().bytesSentProperty());

        TableColumn<SessionStatus, String> bytesReceivedCol = new TableColumn<>("Bytes Received");
        bytesReceivedCol.setCellValueFactory(cellData -> cellData.getValue().bytesReceivedProperty());

        // Action column for stopping sessions
        TableColumn<SessionStatus, Void> actionCol = new TableColumn<>("Action");
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button stopButton = new Button("Stop");

            {
                stopButton.setOnAction(event -> {
                    SessionStatus session = getTableView().getItems().get(getIndex());
                    sessionManager.stopSession(session); // Stop the session
                    sessionList.remove(session); // Remove it from the table
                });
            }

            @Override
            public void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(stopButton);
                }
            }
        });

        // Add all columns to the table
        sessionTable.getColumns().addAll(numberCol, sshHostCol, localHostCol, localPortCol, remoteHostCol, remotePortCol, statusCol, timerCol, bytesSentCol, bytesReceivedCol, actionCol);
        sessionTable.setItems(sessionList);
    }

    // Return the session table view
    public VBox getSessionTable() {
        return new VBox(sessionTable);
    }

    // Add a session to the table
    public void addSession(SessionStatus session) {
        sessionList.add(session);
    }
}
