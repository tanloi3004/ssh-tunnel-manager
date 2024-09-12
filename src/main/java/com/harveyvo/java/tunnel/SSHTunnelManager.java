package com.harveyvo.java.tunnel;

import com.jcraft.jsch.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class SSHTunnelManager {

    private Session session;
    private long bytesSent = 0;
    private long bytesReceived = 0;
    private final LogManager logManager = LogManager.getInstance();
    // Connect based on the profile authentication method
    public void connect(SSHProfile profile) throws JSchException {
        JSch jsch = new JSch();
        JSch.setLogger(new Logger() {
            @Override
            public boolean isEnabled(int level) {
                return true;  // Enable all logging levels
            }

            @Override
            public void log(int level, String message) {
                System.out.println("JSch Log: " + message);
            }
        });

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

            // Hook into the input/output streams here for tracking.
            Channel channel = session.openChannel("direct-tcpip");
            InputStream in = channel.getInputStream();
            OutputStream out = channel.getOutputStream();

            InputStream monitoredIn = new InputStream() {
                @Override
                public int read() {
                    try {
                        int byteValue = in.read();
                        if (byteValue != -1) bytesReceived++;
                        return byteValue;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return -1;
                }
            };

            OutputStream monitoredOut = new OutputStream() {
                @Override
                public void write(int b) {
                    try {
                        out.write(b);
                        bytesSent++;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            channel.connect();
            System.out.println("Local port forwarding set up: localhost:" + localPort + " -> " + remoteHost + ":" + remotePort);
            logManager.log("Local port forwarding set up: localhost:" + localPort + " -> " + remoteHost + ":" + remotePort);
        } else {
            logManager.log("Session is not connected. Cannot set up tunnel.", LogManager.LogLevel.ERROR);
            throw new JSchException("Session is not connected. Cannot set up tunnel.");
        }
    }

    // Set up remote port forwarding (-R)
    public void setUpRemoteTunnel(String bindAddress, int remotePort, String localHost, int localPort) throws JSchException, IOException {
        if (session != null && session.isConnected()) {
            session.setPortForwardingR(bindAddress, remotePort, localHost, localPort);

            // Hook into the input/output streams here for tracking.
            Channel channel = session.openChannel("direct-tcpip");
            InputStream in = channel.getInputStream();
            OutputStream out = channel.getOutputStream();

            InputStream monitoredIn = new InputStream() {
                @Override
                public int read() {
                    try {
                        int byteValue = in.read();
                        if (byteValue != -1) bytesReceived++;
                        return byteValue;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return -1;
                }
            };

            OutputStream monitoredOut = new OutputStream() {
                @Override
                public void write(int b) {
                    try {
                        out.write(b);
                        bytesSent++;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            channel.connect();
            System.out.println("Remote port forwarding set up: " + bindAddress + ":" + remotePort + " -> " + localHost + ":" + localPort);
            logManager.log("Remote port forwarding set up: " + bindAddress + ":" + remotePort + " -> " + localHost + ":" + localPort);
        } else {
            logManager.log("Session is not connected. Cannot set up tunnel.", LogManager.LogLevel.ERROR);
            throw new JSchException("Session is not connected. Cannot set up tunnel.");
        }
    }

    public long getBytesSent() {
        return bytesSent;
    }

    public long getBytesReceived() {
        return bytesReceived;
    }

    // Disconnect the SSH session
    public void disconnect() {
        if (session != null && session.isConnected()) {
            session.disconnect();
            System.out.println("Disconnected from SSH server.");
            logManager.log("Disconnected from SSH server.");
        }
    }
}
