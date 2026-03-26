package com.gabon.gabchat.utils;

import com.gabon.gabchat.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import java.util.HashMap;
import java.util.Map;

public class FirebaseHelper {

    private static final DatabaseReference usersRef =
            FirebaseDatabase.getInstance().getReference("users");
    private static final DatabaseReference conversationsRef =
            FirebaseDatabase.getInstance().getReference("conversations");

    public static String getCurrentUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public static void updateOnlineStatus(boolean isOnline) {
        String uid = getCurrentUserId();
        if (uid == null) return;
        Map<String, Object> updates = new HashMap<>();
        updates.put("online", isOnline);
        updates.put("lastSeen", ServerValue.TIMESTAMP);
        usersRef.child(uid).updateChildren(updates);
    }

    public static void saveUserToDatabase(FirebaseUser firebaseUser) {
        if (firebaseUser == null) return;
        User user = new User(
                firebaseUser.getUid(),
                firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "Utilisateur",
                firebaseUser.getEmail() != null ? firebaseUser.getEmail() : "",
                firebaseUser.getPhoneNumber() != null ? firebaseUser.getPhoneNumber() : ""
        );
        if (firebaseUser.getPhotoUrl() != null) {
            user.setPhotoUrl(firebaseUser.getPhotoUrl().toString());
        }
        usersRef.child(firebaseUser.getUid()).setValue(user);
    }

    public static void updateConversation(String uid1, String uid2,
                                          String lastMessage, String type) {
        String convId = generateConversationId(uid1, uid2);
        long timestamp = System.currentTimeMillis();

        Map<String, Object> data = new HashMap<>();
        data.put("lastMessage", lastMessage);
        data.put("lastMessageType", type);
        data.put("lastMessageTime", timestamp);

        conversationsRef.child(uid1).child(convId).updateChildren(data);
        conversationsRef.child(uid2).child(convId).updateChildren(data);

        // Increment unread for receiver
        conversationsRef.child(uid2).child(convId).child("unreadCount")
                .setValue(ServerValue.increment(1));
    }

    public static void resetUnreadCount(String currentUid, String conversationId) {
        conversationsRef.child(currentUid).child(conversationId)
                .child("unreadCount").setValue(0);
    }

    public static String generateConversationId(String uid1, String uid2) {
        return uid1.compareTo(uid2) < 0 ? uid1 + "_" + uid2 : uid2 + "_" + uid1;
    }
}
