# SSH Tunnel Manager

The **SSH Tunnel Manager** is a desktop application built using Java and JavaFX that allows users to manage SSH tunneling (both local forwarding and remote forwarding). It provides a graphical user interface to create, monitor, and manage SSH tunnels. The application stores SSH profiles for easier access, tracks data sent/received during active sessions, and includes a session timer for each tunnel.


## Features

- **Local Forwarding (`-L`)**: Forward traffic from a local port to a remote server via SSH.
- **Remote Forwarding (`-R`)**: Forward traffic from a remote server port to a local machine via SSH.
- **Profile Management**: Save and load SSH connection profiles, including server details and authentication methods (password/SSH key).
- **Session Management**: Start and stop SSH tunneling sessions, view active sessions in a table, and track data usage (sent/received) and time elapsed for each session.
- **Cross-Platform**: Works on Windows, Linux, and macOS by storing configuration files in the user’s home directory.
- **Data Storage**: SSH profiles are saved in a JSON file under the user’s home directory in a `.ssh_tunnel_manager` folder.

## Installation

### Prerequisites

- **Java 17+**: Ensure that you have Java 17 or later installed on your system.
    - Download and install Java from [Oracle's official website](https://www.oracle.com/java/technologies/javase-downloads.html) or use a package manager like `apt`, `brew`, or `choco`.

- **JavaFX**: JavaFX is required for the graphical user interface. You can download the appropriate JavaFX SDK for your system from [Gluon](https://gluonhq.com/products/javafx/).

### Download

1. Clone or download the project to your local machine:

   ```
   git clone https://github.com/tanloi3004/ssh-tunnel-manager.git
   cd ssh-tunnel-manager
   ```

### Building and Running

1. **Compile the Project**:
   If you are using an IDE like IntelliJ or Eclipse, make sure to configure the JavaFX SDK in your project build path.

   If you are using `javac` and `java` from the terminal, ensure that the JavaFX libraries are included in the classpath:

   ```
   javac --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml -d bin src/com/harveyvo/java/tunnel/*.java
   ```

2. **Run the Application**:

   After compiling, run the application:

   ```
   java --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml -cp bin com.harveyvo.java.tunnel.Main
   ```

## Usage

### Main Interface

When you run the SSH Tunnel Manager, the main window will appear with the following features:

1. **Profile Selection**: Select an SSH profile from the drop-down menu or create new profiles using the **Manage Profiles** button.
2. **Session Management**:
    - Enter details for SSH tunneling (local host, local port, remote host, remote port).
    - Select the tunneling mode (`-L` for local forwarding, `-R` for remote forwarding).
    - Click **Add New Session** to start the SSH tunnel.
3. **Session Table**: View active SSH sessions, including information such as:
    - SSH Host
    - Local/Remote Host and Ports
    - Status (Connected, Connecting, Failed, Stopped)
    - Time elapsed since the session started
    - Data sent and received (in MB)
    - Stop button to terminate an active session.

### Profile Management

You can save frequently used SSH connections by managing profiles. Each profile contains:

- **SSH Host**: The server you want to connect to via SSH.
- **SSH Port**: The SSH port (default is 22).
- **Username**: The username for authentication.
- **Authentication Method**: Choose between password or SSH key authentication.

Profiles are stored in a JSON file located in the user’s home directory under `.ssh_tunnel_manager/ssh_profiles.json`. This file is automatically created and managed by the application.

### Data Storage and Configuration

- **SSH Profiles**: Stored in the user’s home directory:
    - **Windows**: `C:\Users\<username>\.ssh_tunnel_manager\ssh_profiles.json`
    - **Linux/macOS**: `/home/<username>/.ssh_tunnel_manager/ssh_profiles.json`

  The profiles are saved as JSON and can be easily backed up or shared between machines.

### Validation and Guidelines

1. **Port Validation**: The application validates port numbers to ensure they are within the range of `1-65535`.
2. **Guidelines**: The application provides guidelines next to the port fields to help users understand valid port ranges.

### Error Handling

- If invalid data (such as an invalid port number) is entered, the application will show a warning message to the user.
- If the SSH tunnel fails to connect, the session status will update with the appropriate error message, and the session will not be added to the active list.

### Example Workflows

1. **Creating a Local Forwarding Tunnel**:
    - Select a saved profile or create a new one.
    - Enter the local host and local port.
    - Enter the remote host and remote port you want to forward traffic to.
    - Select **Local Forwarding (`-L`)**.
    - Click **Add New Session** to start the SSH tunnel.

2. **Creating a Remote Forwarding Tunnel**:
    - Select a saved profile or create a new one.
    - Enter the local host and local port.
    - Enter the remote host and remote port you want to forward traffic to.
    - Select **Remote Forwarding (`-R`)**.
    - Click **Add New Session** to start the SSH tunnel.

## Contributions

Contributions are welcome! Please fork the repository and create a pull request with any features or improvements you would like to contribute.

## License

This project is licensed under the MIT License. See the `LICENSE` file for more information.
