package com.harveyvo.java.tunnel;

public class SSHProfile {

    public enum AuthMethod {
        PASSWORD, SSH_KEY
    }

    private String profileName;
    private String sshHost;
    private int sshPort;
    private String username;
    private String password;  // For password-based authentication
    private AuthMethod authMethod;  // Either PASSWORD or SSH_KEY
    private String sshKeyContent;  // Store SSH key PEM content
    private String passphrase;  // Optional passphrase for the SSH key

    // Default constructor (needed by Jackson)
    public SSHProfile() {
    }

    // Parameterized constructor for initialization
    public SSHProfile(String profileName, String sshHost, int sshPort, String username, String password, AuthMethod authMethod, String sshKeyContent, String passphrase) {
        this.profileName = profileName;
        this.sshHost = sshHost;
        this.sshPort = sshPort;
        this.username = username;
        this.password = password;
        this.authMethod = authMethod;
        this.sshKeyContent = sshKeyContent;
        this.passphrase = passphrase;
    }

    // Getters and Setters for Jackson

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getSshHost() {
        return sshHost;
    }

    public void setSshHost(String sshHost) {
        this.sshHost = sshHost;
    }

    public int getSshPort() {
        return sshPort;
    }

    public void setSshPort(int sshPort) {
        this.sshPort = sshPort;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public AuthMethod getAuthMethod() {
        return authMethod;
    }

    public void setAuthMethod(AuthMethod authMethod) {
        this.authMethod = authMethod;
    }

    public String getSshKeyContent() {
        return sshKeyContent;
    }

    public void setSshKeyContent(String sshKeyContent) {
        this.sshKeyContent = sshKeyContent;
    }

    public String getPassphrase() {
        return passphrase;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }
}
