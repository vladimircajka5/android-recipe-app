package com.siit.recipeapp.data.model.media;

import java.time.LocalDateTime;

public class MediaAsset {
    private String uri;
    private String caption;
    private LocalDateTime uploadedAt;
    private MediaType mediaType;
    private MediaPurpose purpose;

    // Constructor
    public MediaAsset(String uri, String caption, LocalDateTime uploadedAt, MediaType mediaType, MediaPurpose purpose) {
        this.uri = uri;
        this.caption = caption;
        this.uploadedAt = uploadedAt;
        this.mediaType = mediaType;
        this.purpose = purpose;
    }

    // Getters
    public String getUri() {
        return uri;
    }

    public String getCaption() {
        return caption;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public MediaPurpose getPurpose() {
        return purpose;
    }

    // Setters
    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public void setPurpose(MediaPurpose purpose) {
        this.purpose = purpose;
    }
}
