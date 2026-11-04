package com.siit.recipeapp.data.model.account;

import com.siit.recipeapp.data.model.media.MediaAsset;

import java.time.LocalDateTime;

public class User {

    private String username;
    private String email;
    // private String passwordHash;
    private UserRole role;
    private boolean emailVerified;
    private LocalDateTime createdAt;
    private MediaAsset profileImage;
    private Profile profile;

    // Constructor

    public User(String username, String email, UserRole role, boolean emailVerified, LocalDateTime createdAt, MediaAsset profileImage, Profile profile) {
        this.username = username;
        this.email = email;
        this.role = role;
        this.emailVerified = emailVerified;
        this.createdAt = createdAt;
        this.profileImage = profileImage;
        this.profile = profile;
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public UserRole getRole() {
        return role;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public MediaAsset getProfileImage() {
        return profileImage;
    }

    public Profile getProfile() {
        return profile;
    }

    // Setters
    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setProfileImage(MediaAsset profileImage) {
        this.profileImage = profileImage;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }
}
