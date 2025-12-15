package edu.bluejack24_2.myapplication.models;

public class TodoItem {
    private String id;
    private String title;
    private String description;
    private boolean completed;
    private long createdAt;
    private String userId;

    public TodoItem() {
        // Required for Firebase
    }

    public TodoItem(String id, String title, String description, String userId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.completed = false;
        this.userId = userId;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}