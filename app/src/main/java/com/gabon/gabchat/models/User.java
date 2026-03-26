package com.gabon.gabchat.models;

public class User {
    private String uid;
    private String displayName;
    private String email;
    private String phone;
    private String photoUrl;
    private String status;
    private String bio;
    private long lastSeen;
    private boolean isOnline;
    private String fcmToken;
    private String theme;

    public User() {}

    public User(String uid, String displayName, String email, String phone) {
        this.uid = uid;
        this.displayName = displayName;
        this.email = email;
        this.phone = phone;
        this.status = "Disponible 🇬🇦";
        this.isOnline = false;
        this.lastSeen = System.currentTimeMillis();
        this.theme = "default";
    }

    // Getters & Setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public long getLastSeen() { return lastSeen; }
    public void setLastSeen(long lastSeen) { this.lastSeen = lastSeen; }

    public boolean isOnline() { return isOnline; }
    public void setOnline(boolean online) { isOnline = online; }

    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
}
