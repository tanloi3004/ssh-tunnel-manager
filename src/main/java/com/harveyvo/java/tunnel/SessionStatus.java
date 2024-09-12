package com.harveyvo.java.tunnel;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Timer;
import java.util.TimerTask;

public class SessionStatus {
    private final StringProperty sessionNumber;
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

    public SessionStatus(String sessionNumber, String sshHost, String localHost, String localPort,
                         String remoteHost, String remotePort, Boolean isLocal, String status) {
        this.sessionNumber = new SimpleStringProperty(sessionNumber);
        this.sshHost = new SimpleStringProperty(sshHost);
        this.localHost = new SimpleStringProperty(localHost);
        this.localPort = new SimpleStringProperty(localPort);
        this.remoteHost = new SimpleStringProperty(remoteHost);
        this.remotePort = new SimpleStringProperty(remotePort);
        this.status = new SimpleStringProperty(status);
        this.mode = new SimpleStringProperty(isLocal?"-L":"-R");
        this.timerText = new SimpleStringProperty("00:00:00");
        this.bytesSent = new SimpleStringProperty("0 MB");
        this.bytesReceived = new SimpleStringProperty("0 MB");
    }

    public StringProperty sessionNumberProperty() {
        return sessionNumber;
    }

    public String getMode() {
        return mode.get();
    }

    public StringProperty modeProperty() {
        return mode;
    }

    public StringProperty sshHostProperty() {
        return sshHost;
    }

    public String getLocalHost() {
        return localHost.get();
    }

    public String getLocalPort() {
        return localPort.get();
    }

    public String getRemoteHost() {
        return remoteHost.get();
    }

    public String getRemotePort() {
        return remotePort.get();
    }

    public StringProperty localHostProperty() {
        return localHost;
    }

    public StringProperty localPortProperty() {
        return localPort;
    }

    public StringProperty remoteHostProperty() {
        return remoteHost;
    }

    public StringProperty remotePortProperty() {
        return remotePort;
    }

    public StringProperty statusProperty() {
        return status;
    }

    public StringProperty timerTextProperty() {
        return timerText;
    }

    public StringProperty bytesSentProperty() {
        return bytesSent;
    }

    public StringProperty bytesReceivedProperty() {
        return bytesReceived;
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    public void updateDataUsage(long sentBytes, long receivedBytes) {
        this.bytesSent.set(String.format("%.2f MB", sentBytes / (1024.0 * 1024.0)));
        this.bytesReceived.set(String.format("%.2f MB", receivedBytes / (1024.0 * 1024.0)));
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
}
