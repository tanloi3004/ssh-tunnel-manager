package com.harveyvo.java.tunnel;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ProfileManager {

    private static final String PROFILE_FILE_NAME = "ssh_profiles.json";  // File to store profiles
    private ObjectMapper objectMapper;  // JSON serializer/deserializer
    private FileManager fileManager;  // Use FileManager for managing files

    public ProfileManager() {
        this.fileManager = new FileManager();
        objectMapper = new ObjectMapper();

        // Ensure the profile file exists
        this.fileManager.createFileIfNotExists(PROFILE_FILE_NAME);
    }

    public List<SSHProfile> loadProfiles() {
        Path profileFilePath = fileManager.getFilePath(PROFILE_FILE_NAME);
        File file = profileFilePath.toFile();

        if (!file.exists() || file.length() == 0) {
            return new ArrayList<>();  // Return an empty list if file doesn't exist or is empty
        }

        try {
            return objectMapper.readValue(file, new TypeReference<List<SSHProfile>>() {});
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();  // Return empty list if there was an error
        }
    }

    // Save the list of profiles to the JSON file
    public void saveProfiles(List<SSHProfile> profiles) {
        try {
            Path profileFilePath = fileManager.getFilePath(PROFILE_FILE_NAME); // Get the profile file path
            objectMapper.writeValue(profileFilePath.toFile(), profiles);
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
