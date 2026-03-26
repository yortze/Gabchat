package com.gabon.gabchat.models;

import java.util.List;
import java.util.Map;

public class Group {
    private String groupId;
    private String name;
    private String description;
    private String photoUrl;
    private String createdBy;
    private long createdAt;
    private List<String> members;
    private Map<String, String> admins; // uid -> "admin"
    private String lastMessage;
    private long lastMessageTime;

    public Group() {}

    public Group(String name, String description, String createdBy, List<String> members) {
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
        this.members = members;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters & Setters
    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public List<String> getMembers() { return members; }
    public void setMembers(List<String> members) { this.members = members; }

    public Map<String, String> getAdmins() { return admins; }
    public void setAdmins(Map<String, String> admins) { this.admins = admins; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public long getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(long lastMessageTime) { this.lastMessageTime = lastMessageTime; }
}
