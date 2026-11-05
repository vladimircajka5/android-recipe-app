package com.siit.recipeapp.data.model.recipe;

import com.siit.recipeapp.data.model.media.MediaAsset;

import java.util.List;

public class RecipeStep {

    // Svojstva
    private int stepNumber;
    private String title;
    private String instructions;
    private Integer durationMinutes;
    private PreparationType preparationType;

    // Veze
    private List<MediaAsset> photos;

    // Constructor
    public RecipeStep(int stepNumber, String title, String instructions, Integer durationMinutes, PreparationType preparationType, List<MediaAsset> photos) {
        this.stepNumber = stepNumber;
        this.title = title;
        this.instructions = instructions;
        this.durationMinutes = durationMinutes;
        this.preparationType = preparationType;
        this.photos = photos;
    }

    // Getters
    public int getStepNumber() {
        return stepNumber;
    }

    public String getTitle() {
        return title;
    }

    public String getInstructions() {
        return instructions;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public PreparationType getPreparationType() {
        return preparationType;
    }

    public List<MediaAsset> getPhotos() {
        return photos;
    }

    // Setters
    public void setStepNumber(int stepNumber) {
        this.stepNumber = stepNumber;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setInstructions(String instructions) {
        instructions = instructions;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public void setPreparationType(PreparationType preparationType) {
        this.preparationType = preparationType;
    }

    public void setPhotos(List<MediaAsset> photos) {
        this.photos = photos;
    }
}
