package com.harveyvo.java.tunnel;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class SessionStatus {
    private final StringProperty sessionNumber;
    private final StringProperty sessionId;
    private final StringProperty connectionName;
    private final StringProperty profileId;
    private final StringProperty sshHost;
    private final StringProperty localHost;
    private final StringProperty localPort;
    private final StringProperty remoteHost;
    private final StringProperty remotePort;
    private final StringProperty status;
    private final StringProperty timerText;
    private final StringProperty bytesSent;
    private final StringProperty bytesReceived;
    private final StringProperty mode;

    private Timer timer;
    private int elapsedSeconds = 0;

    private SSHTunnelManager sshTunnelManager;

    public SessionStatus(String sessionNumber, String connectionName, String profileId, String sshHost, String localHost, String localPort,
                         String remoteHost, String remotePort, Boolean isLocal, String status) {
        this.sessionNumber = new SimpleStringProperty(sessionNumber);
        this.sessionId = new SimpleStringProperty(UUID.randomUUID().toString());
        this.connectionName = new SimpleStringProperty(connectionName);
        this.profileId = new SimpleStringProperty(profileId);
        this.sshHost = new SimpleStringProperty(sshHost);
        this.localHost = new SimpleStringProperty(localHost);
        this.localPort = new SimpleStringProperty(localPort);
        this.remoteHost = new SimpleStringProperty(remoteHost);
        this.remotePort = new SimpleStringProperty(remotePort);
        this.status = new SimpleStringProperty(status);
        this.mode = new SimpleStringProperty(isLocal ? "-L" : "-R");
        this.timerText = new SimpleStringProperty("00:00:00");
        this.bytesSent = new SimpleStringProperty("0 MB");
        this.bytesReceived = new SimpleStringProperty("0 MB");
    }

    // Getter and setter for SSHTunnelManager
    public SSHTunnelManager getSshTunnelManager() {
        return sshTunnelManager;
    }

    public void setSshTunnelManager(SSHTunnelManager sshTunnelManager) {
        this.sshTunnelManager = sshTunnelManager;
    }

    // Method to update data usage
    public void updateDataUsage(long sentBytes, long receivedBytes) {
        Platform.runLater(() -> {
            String sentDisplay = String.format("%.2f MB", sentBytes / (1024.0 * 1024.0));
            String receivedDisplay = String.format("%.2f MB", receivedBytes / (1024.0 * 1024.0));

            this.bytesSent.set(sentDisplay);
            this.bytesReceived.set(receivedDisplay);
        });
    }

    // Getters and setters for the new properties
    public String getSessionId() {
        return sessionId.get();
    }

    public String getConnectionName() {
        return connectionName.get();
    }

    public String getProfileId() {
        return profileId.get();
    }

    public void startTimer(Runnable onTimerUpdate) {
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                elapsedSeconds++;
                String time = String.format("%02d:%02d:%02d", elapsedSeconds / 3600, (elapsedSeconds % 3600) / 60, elapsedSeconds % 60);
                Platform.runLater(() -> {
                    timerText.set(time);
                    onTimerUpdate.run();
                });
            }
        }, 1000, 1000);
    }

    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }

    public String getSessionNumber() {
        return sessionNumber.get();
    }

    public StringProperty sessionNumberProperty() {
        return sessionNumber;
    }

    public void setSessionNumber(String sessionNumber) {
        this.sessionNumber.set(sessionNumber);
    }

    public StringProperty sessionIdProperty() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId.set(sessionId);
    }

    public StringProperty connectionNameProperty() {
        return connectionName;
    }

    public void setConnectionName(String connectionName) {
        this.connectionName.set(connectionName);
    }

    public StringProperty profileIdProperty() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId.set(profileId);
    }

    public String getSshHost() {
        return sshHost.get();
    }

    public StringProperty sshHostProperty() {
        return sshHost;
    }

    public void setSshHost(String sshHost) {
        this.sshHost.set(sshHost);
    }

    public String getLocalHost() {
        return localHost.get();
    }

    public StringProperty localHostProperty() {
        return localHost;
    }

    public void setLocalHost(String localHost) {
        this.localHost.set(localHost);
    }

    public String getLocalPort() {
        return localPort.get();
    }

    public StringProperty localPortProperty() {
        return localPort;
    }

    public void setLocalPort(String localPort) {
        this.localPort.set(localPort);
    }

    public String getRemoteHost() {
        return remoteHost.get();
    }

    public StringProperty remoteHostProperty() {
        return remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost.set(remoteHost);
    }

    public String getRemotePort() {
        return remotePort.get();
    }

    public StringProperty remotePortProperty() {
        return remotePort;
    }

    public void setRemotePort(String remotePort) {
        this.remotePort.set(remotePort);
    }

    public String getStatus() {
        return status.get();
    }

    public StringProperty statusProperty() {
        return status;
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    public String getTimerText() {
        return timerText.get();
    }

    public StringProperty timerTextProperty() {
        return timerText;
    }

    public void setTimerText(String timerText) {
        this.timerText.set(timerText);
    }

    public String getBytesSent() {
        return bytesSent.get();
    }

    public StringProperty bytesSentProperty() {
        return bytesSent;
    }

    public void setBytesSent(String bytesSent) {
        this.bytesSent.set(bytesSent);
    }

    public String getBytesReceived() {
        return bytesReceived.get();
    }

    public StringProperty bytesReceivedProperty() {
        return bytesReceived;
    }

    public void setBytesReceived(String bytesReceived) {
        this.bytesReceived.set(bytesReceived);
    }

    public String getMode() {
        return mode.get();
    }

    public StringProperty modeProperty() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode.set(mode);
    }

    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    public int getElapsedSeconds() {
        return elapsedSeconds;
    }

    public void setElapsedSeconds(int elapsedSeconds) {
        this.elapsedSeconds = elapsedSeconds;
    }
    // Method to handle log messages
    public void appendLog(String message) {
        System.out.println(message);
    }
}
