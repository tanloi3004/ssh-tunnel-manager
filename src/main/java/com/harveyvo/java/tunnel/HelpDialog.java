package com.harveyvo.java.tunnel;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class HelpDialog {

    public void showHelpDialog(Stage parentStage) {
        Stage helpDialog = new Stage();
        helpDialog.setTitle("Tunnel Use Cases Help");
        helpDialog.initModality(Modality.APPLICATION_MODAL);
        helpDialog.initOwner(parentStage);

        // Local Forwarding Section with ASCII diagram
        Text localForwardingTitle = new Text("Local Forwarding (-L):\n");
        localForwardingTitle.setStyle("-fx-font-weight: bold;");
        Text localForwardingDiagram = new Text(
                "\n" +
                        "  [Your Local Machine]                    [Remote Server]\n" +
                        "  localhost:8080  ------------------->  remotehost.com:80\n" +
                        "                |\n" +
                        "                V\n" +
                        "          [SSH Server]\n\n"
        );
        Text localForwardingUseCase = new Text(
                "**Use Case:**\n" +
                        "You want to access a database running on a remote server (port 3306) but can only connect through SSH. \n" +
                        "You set up a local forwarding tunnel to forward traffic from your local port 3306 to the remote server.\n\n"
        );

        // Remote Forwarding Section with ASCII diagram
        Text remoteForwardingTitle = new Text("Remote Forwarding (-R):\n");
        remoteForwardingTitle.setStyle("-fx-font-weight: bold;");
        Text remoteForwardingDiagram = new Text(
                "\n" +
                        "  [Remote Server]                        [Your Local Machine]\n" +
                        "  remotehost.com:9000  ----------------->  localhost:8080\n" +
                        "                     |\n" +
                        "                     V\n" +
                        "                [SSH Server]\n\n"
        );
        Text remoteForwardingUseCase = new Text(
                "**Use Case:**\n" +
                        "You are running a web application on your local machine (port 8080) and need to expose it to a remote server. \n" +
                        "You set up a remote forwarding tunnel to allow remote users to access your local app via the remote server.\n\n"
        );

        // Create TextFlow for formatted display
        TextFlow localFlow = new TextFlow(localForwardingTitle, localForwardingDiagram, localForwardingUseCase);
        TextFlow remoteFlow = new TextFlow(remoteForwardingTitle, remoteForwardingDiagram, remoteForwardingUseCase);

        // Add both TextFlow elements to a VBox
        VBox vbox = new VBox(10, localFlow, remoteFlow);
        vbox.setPadding(new Insets(10));

        // Use a ScrollPane in case the text grows large
        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setFitToWidth(true);

        Scene scene = new Scene(scrollPane, 500, 400);
        helpDialog.setScene(scene);
        helpDialog.show();
    }
}
