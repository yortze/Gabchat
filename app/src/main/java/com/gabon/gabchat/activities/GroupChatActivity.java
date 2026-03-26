package com.gabon.gabchat.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.gabon.gabchat.adapters.MessageAdapter;
import com.gabon.gabchat.databinding.ActivityGroupChatBinding;
import com.gabon.gabchat.models.Message;
import com.gabon.gabchat.utils.FirebaseHelper;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;

public class GroupChatActivity extends AppCompatActivity {

    private ActivityGroupChatBinding binding;
    private MessageAdapter messageAdapter;
    private List<Message> messageList = new ArrayList<>();
    private DatabaseReference messagesRef;
    private String currentUserId, groupId, groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        groupId = getIntent().getStringExtra("groupId");
        groupName = getIntent().getStringExtra("groupName");
        currentUserId = FirebaseHelper.getCurrentUserId();

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(groupName);

        setupRecyclerView();
        loadMessages();

        binding.btnSend.setOnClickListener(v -> sendMessage());
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(this, messageList, currentUserId);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        binding.rvMessages.setLayoutManager(lm);
        binding.rvMessages.setAdapter(messageAdapter);
    }

    private void loadMessages() {
        messagesRef = FirebaseDatabase.getInstance()
                .getReference("group_messages").child(groupId);

        messagesRef.orderByChild("timestamp")
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot snapshot, String prev) {
                        Message msg = snapshot.getValue(Message.class);
                        if (msg != null) {
                            msg.setMessageId(snapshot.getKey());
                            messageList.add(msg);
                            messageAdapter.notifyItemInserted(messageList.size() - 1);
                            binding.rvMessages.scrollToPosition(messageList.size() - 1);
                        }
                    }
                    @Override public void onChildChanged(DataSnapshot s, String p) {}
                    @Override public void onChildRemoved(DataSnapshot s) {}
                    @Override public void onChildMoved(DataSnapshot s, String p) {}
                    @Override public void onCancelled(DatabaseError e) {}
                });
    }

    private void sendMessage() {
        String text = binding.etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;
        binding.etMessage.setText("");

        Message msg = new Message(currentUserId, groupId, text, Message.TYPE_TEXT);
        msg.setConversationId(groupId);
        messagesRef.push().setValue(msg);

        // Update group last message
        FirebaseDatabase.getInstance().getReference("groups")
                .child(groupId).child("lastMessage").setValue(text);
        FirebaseDatabase.getInstance().getReference("groups")
                .child(groupId).child("lastMessageTime").setValue(System.currentTimeMillis());
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }
}
