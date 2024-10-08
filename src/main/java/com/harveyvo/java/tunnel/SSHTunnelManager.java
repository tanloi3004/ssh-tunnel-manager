package com.harveyvo.java.tunnel;

import com.jcraft.jsch.*;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.io.output.CountingOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class SSHTunnelManager {

    private Session session;
    private AtomicLong bytesSent = new AtomicLong(0);
    private AtomicLong bytesReceived = new AtomicLong(0);
    private final LogManager logManager = LogManager.getInstance();
    private final SessionStatus sessionStatus;
    private final Consumer<String> logConsumer;

    public SSHTunnelManager(SessionStatus sessionStatus, Consumer<String> logConsumer) {
        this.sessionStatus = sessionStatus;
        this.logConsumer = logConsumer;

        // Set the custom logger
        JSch.setLogger(new JSchLogger(logConsumer));
    }
    // Connect based on the profile authentication method
    public void connect(SSHProfile profile) throws JSchException {
        JSch jsch = new JSch();
//        JSch.setLogger(new Logger() {
//            @Override
//            public boolean isEnabled(int level) {
//                return true; // Enable all logging levels
//            }
//
//            @Override
//            public void log(int level, String message) {
//                System.out.println("JSch Log: " + message);
//            }
//        });

        // Use SSH Key or Password authentication based on the profile data
        if (profile.getAuthMethod() == SSHProfile.AuthMethod.SSH_KEY) {
            connectWithPrivateKey(jsch, profile.getSshHost(), profile.getSshPort(), profile.getUsername(), profile.getSshKeyContent(), profile.getPassphrase());
        } else {
            connectWithPassword(jsch, profile.getSshHost(), profile.getSshPort(), profile.getUsername(), profile.getPassword());
        }
    }

    // Connect using password-based authentication
    private void connectWithPassword(JSch jsch, String sshHost, int sshPort, String username, String password) throws JSchException {
        session = jsch.getSession(username, sshHost, sshPort);
        session.setPassword(password);

        // Configure SSH session properties
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);

        // Establish connection
        session.connect();
        System.out.println("Connected using password to " + sshHost + ":" + sshPort);
        logManager.log("Connected using password to " + sshHost + ":" + sshPort);
    }

    // Connect using SSH key-based authentication
    private void connectWithPrivateKey(JSch jsch, String sshHost, int sshPort, String username, String sshKeyContent, String passphrase) throws JSchException {
        byte[] privateKeyBytes = sshKeyContent.getBytes();
        jsch.addIdentity(username, privateKeyBytes, null, passphrase != null ? passphrase.getBytes() : null);

        session = jsch.getSession(username, sshHost, sshPort);
        session.setConfig("StrictHostKeyChecking", "no");

        // Establish connection
        session.connect(10000);
        System.out.println("Connected using SSH key to " + sshHost + ":" + sshPort);
        logManager.log("Connected using SSH key to " + sshHost + ":" + sshPort);
    }

    // Set up local port forwarding (-L)
    public void setUpTunnel(String bindAddress, int localPort, String remoteHost, int remotePort) throws JSchException, IOException {
        if (session != null && session.isConnected()) {
            session.setPortForwardingL(bindAddress, localPort, remoteHost, remotePort);
            System.out.println("Local port forwarding set up: localhost:" + localPort + " -> " + remoteHost + ":" + remotePort);
            logManager.log("Local port forwarding set up: localhost:" + localPort + " -> " + remoteHost + ":" + remotePort);

            // Hooking the input/output streams for byte tracking
            Channel channel = session.openChannel("direct-tcpip");
            InputStream in = monitorInputStream(channel.getInputStream());
            OutputStream out = monitorOutputStream(channel.getOutputStream());

            channel.setInputStream(in);
            channel.setOutputStream(out);
            channel.connect();
        } else {
            logManager.log("Session is not connected. Cannot set up tunnel.", LogManager.LogLevel.ERROR);
            throw new JSchException("Session is not connected. Cannot set up tunnel.");
        }
    }

    // Set up remote port forwarding (-R)
    public void setUpRemoteTunnel(String bindAddress, int remotePort, String localHost, int localPort) throws JSchException, IOException {
        if (session != null && session.isConnected()) {
            session.setPortForwardingR(bindAddress, remotePort, localHost, localPort);
            System.out.println("Remote port forwarding set up: " + bindAddress + ":" + remotePort + " -> " + localHost + ":" + localPort);
            logManager.log("Remote port forwarding set up: " + bindAddress + ":" + remotePort + " -> " + localHost + ":" + localPort);

            // Hooking the input/output streams for byte tracking
            Channel channel = session.openChannel("direct-tcpip");
            InputStream in = monitorInputStream(channel.getInputStream());
            OutputStream out = monitorOutputStream(channel.getOutputStream());

            channel.setInputStream(in);
            channel.setOutputStream(out);
            channel.connect();
        } else {
            logManager.log("Session is not connected. Cannot set up tunnel.", LogManager.LogLevel.ERROR);
            throw new JSchException("Session is not connected. Cannot set up tunnel.");
        }
    }

    // Monitor methods to track data usage
    public InputStream monitorInputStream(InputStream in) {
        return new CountingInputStream(in) {
            @Override
            protected void afterRead(int n) {
                super.afterRead(n);
                if (n > 0) {
                    bytesReceived.addAndGet(n);
                    sessionStatus.updateDataUsage(bytesSent.get(), bytesReceived.get());
                }
            }
        };
    }

    public OutputStream monitorOutputStream(OutputStream out) {
        return new CountingOutputStream(out) {
            @Override
            protected void beforeWrite(int n) {
                super.beforeWrite(n);
                bytesSent.addAndGet(n);
                sessionStatus.updateDataUsage(bytesSent.get(), bytesReceived.get());
            }
        };
    }

    public long getBytesSent() {
        return bytesSent.get();
    }

    public long getBytesReceived() {
        return bytesReceived.get();
    }

    // Disconnect the SSH session
    public void disconnect() {
        if (session != null && session.isConnected()) {
            session.disconnect();
            System.out.println("Disconnected from SSH server.");
            logManager.log("Disconnected from SSH server.");
        }
    }

    public static void testConnection(SSHProfile profile) throws Exception {
        JSch jsch = new JSch();

       // JSch.setLogger(new JSchLogger(logConsumer));
        // Set up authentication based on the profile
        if (profile.getAuthMethod() == SSHProfile.AuthMethod.SSH_KEY) {
            byte[] privateKeyBytes = profile.getSshKeyContent().getBytes();
            jsch.addIdentity(profile.getUsername(), privateKeyBytes, null,
                    profile.getPassphrase() != null ? profile.getPassphrase().getBytes() : null);
        }

        Session session = jsch.getSession(profile.getUsername(), profile.getSshHost(), profile.getSshPort());
        if (profile.getAuthMethod() == SSHProfile.AuthMethod.PASSWORD) {
            session.setPassword(profile.getPassword());
        }
        session.setConfig("StrictHostKeyChecking", "no");
        try {
            // Attempt to connect
            session.connect(10000); // 10-second timeout
            session.disconnect();
        }
        catch (JSchException e) {
            // Handle different types of exceptions
            String errorMessage;
            if (e.getCause() instanceof UnknownHostException) {
                errorMessage = "Unknown host: " + profile.getSshHost();
            } else if (e.getMessage().contains("Auth fail")) {
                errorMessage = "Authentication failed. Please check your username and password.";
            } else {
                errorMessage = "Failed to connect: " + e.getMessage();
            }
            throw new Exception(errorMessage, e);
        }
    }
}
