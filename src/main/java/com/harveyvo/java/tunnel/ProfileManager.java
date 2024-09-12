package com.harveyvo.java.tunnel;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ProfileManager {

    private static final String APP_DIRECTORY_NAME = ".ssh_tunnel_manager";  // Subdirectory in the user's home
    private static final String PROFILE_FILE_NAME = "ssh_profiles.json";  // File to store profiles
    private ObjectMapper objectMapper;  // JSON serializer/deserializer

    public ProfileManager() {
        objectMapper = new ObjectMapper();
    }

    // Get the user-specific directory for storing profiles (cross-platform)
    private Path getUserProfileDirectory() {
        String userHome = System.getProperty("user.home");  // Get the user's home directory
        return Paths.get(userHome, APP_DIRECTORY_NAME);  // Subdirectory in the user's home
    }

    // Ensure that the directory for storing the profile file exists
    private void ensureDirectoryExists() throws IOException {
        Path profileDirectory = getUserProfileDirectory();
        if (!Files.exists(profileDirectory)) {
            Files.createDirectories(profileDirectory);  // Create the directory if it doesn't exist
        }
    }

    // Get the full path to the profile file
    private File getProfileFile() throws IOException {
        ensureDirectoryExists();  // Make sure the directory exists
        return getUserProfileDirectory().resolve(PROFILE_FILE_NAME).toFile();
    }

    // Load profiles from the JSON file
    public List<SSHProfile> loadProfiles() {
        try {
            File file = getProfileFile();
            if (!file.exists()) {
                return new ArrayList<>();  // Return empty list if file doesn't exist
            }
            return objectMapper.readValue(file, new TypeReference<List<SSHProfile>>() {});
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();  // Return empty list if there was an error
        }
    }

    // Save the list of profiles to the JSON file
    public void saveProfiles(List<SSHProfile> profiles) {
        try {
            File file = getProfileFile();
            objectMapper.writeValue(file, profiles);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Add or update a profile
    public void saveOrUpdateProfile(SSHProfile profile) {
        List<SSHProfile> profiles = loadProfiles();
        boolean updated = false;
        for (int i = 0; i < profiles.size(); i++) {
            if (profiles.get(i).getProfileName().equals(profile.getProfileName())) {
                profiles.set(i, profile);  // Update existing profile
                updated = true;
                break;
            }
        }
        if (!updated) {
            profiles.add(profile);  // Add new profile if not found
        }
        saveProfiles(profiles);
    }

    // Get a profile by its name
    public SSHProfile getProfileByName(String profileName) {
        List<SSHProfile> profiles = loadProfiles();
        for (SSHProfile profile : profiles) {
            if (profile.getProfileName().equals(profileName)) {
                return profile;  // Return the found profile
            }
        }
        return null;  // Return null if profile not found
    }

    // Delete a profile by its name
    public void deleteProfile(String profileName) {
        List<SSHProfile> profiles = loadProfiles();
        profiles.removeIf(p -> p.getProfileName().equals(profileName));
        saveProfiles(profiles);
    }

    // Check if a profile already exists by name
    public boolean profileExists(String profileName) {
        List<SSHProfile> profiles = loadProfiles();
        for (SSHProfile profile : profiles) {
            if (profile.getProfileName().equals(profileName)) {
                return true;
            }
        }
        return false;
    }
}
