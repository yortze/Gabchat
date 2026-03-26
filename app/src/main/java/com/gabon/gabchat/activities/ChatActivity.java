package com.gabon.gabchat.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bumptech.glide.Glide;
import com.gabon.gabchat.R;
import com.gabon.gabchat.adapters.MessageAdapter;
import com.gabon.gabchat.databinding.ActivityChatBinding;
import com.gabon.gabchat.models.Message;
import com.gabon.gabchat.models.User;
import com.gabon.gabchat.utils.FirebaseHelper;
import com.gabon.gabchat.utils.TimeUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private MessageAdapter messageAdapter;
    private List<Message> messageList = new ArrayList<>();
    private DatabaseReference messagesRef;
    private String currentUserId, receiverId, conversationId;
    private MediaRecorder mediaRecorder;
    private File audioFile;
    private boolean isRecording = false;
    private Handler typingHandler = new Handler(Looper.getMainLooper());

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) uploadMedia(uri, Message.TYPE_IMAGE);
            });

    private final ActivityResultLauncher<String> videoPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) uploadMedia(uri, Message.TYPE_VIDEO);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        receiverId = getIntent().getStringExtra("receiverId");
        conversationId = generateConversationId(currentUserId, receiverId);

        setupToolbar();
        setupRecyclerView();
        setupListeners();
        loadMessages();
        observeReceiverStatus();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Load receiver info
        FirebaseDatabase.getInstance().getReference("users").child(receiverId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            binding.tvContactName.setText(user.getDisplayName());
                            if (user.getPhotoUrl() != null) {
                                Glide.with(ChatActivity.this)
                                        .load(user.getPhotoUrl())
                                        .placeholder(R.drawable.ic_default_avatar)
                                        .into(binding.ivContactAvatar);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(this, messageList, currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.rvMessages.setLayoutManager(layoutManager);
        binding.rvMessages.setAdapter(messageAdapter);
    }

    private void setupListeners() {
        // Text input watcher for typing indicator
        binding.etMessage.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean hasText = s.length() > 0;
                binding.btnSend.setVisibility(hasText ? View.VISIBLE : View.GONE);
                binding.btnAudio.setVisibility(hasText ? View.GONE : View.VISIBLE);
                setTypingStatus(hasText);
            }
        });

        binding.btnSend.setOnClickListener(v -> sendTextMessage());
        binding.btnAttachment.setOnClickListener(v -> showAttachmentOptions());
        binding.btnEmoji.setOnClickListener(v -> toggleEmojiPanel());
        binding.btnAudio.setOnLongClickListener(v -> { startRecording(); return true; });
        binding.btnAudio.setOnClickListener(v -> {
            if (isRecording) stopRecordingAndSend();
        });

        // Call buttons
        binding.btnVoiceCall.setOnClickListener(v -> startCall(false));
        binding.btnVideoCall.setOnClickListener(v -> startCall(true));
    }

    private void sendTextMessage() {
        String text = binding.etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        binding.etMessage.setText("");
        Message message = new Message(currentUserId, receiverId, text, Message.TYPE_TEXT);
        message.setConversationId(conversationId);

        messagesRef.push().setValue(message)
                .addOnSuccessListener(unused -> {
                    FirebaseHelper.updateConversation(currentUserId, receiverId, text, Message.TYPE_TEXT);
                    scrollToBottom();
                });
    }

    private void loadMessages() {
        messagesRef = FirebaseDatabase.getInstance()
                .getReference("messages").child(conversationId);

        messagesRef.orderByChild("timestamp")
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot snapshot, String prev) {
                        Message message = snapshot.getValue(Message.class);
                        if (message != null) {
                            message.setMessageId(snapshot.getKey());
                            messageList.add(message);
                            messageAdapter.notifyItemInserted(messageList.size() - 1);
                            scrollToBottom();

                            // Mark as read
                            if (!message.getSenderId().equals(currentUserId)) {
                                snapshot.getRef().child("status").setValue(Message.STATUS_READ);
                            }
                        }
                    }
                    @Override public void onChildChanged(DataSnapshot s, String p) {
                        // Handle message status updates
                        messageAdapter.notifyDataSetChanged();
                    }
                    @Override public void onChildRemoved(DataSnapshot s) {}
                    @Override public void onChildMoved(DataSnapshot s, String p) {}
                    @Override public void onCancelled(DatabaseError e) {}
                });
    }

    private void observeReceiverStatus() {
        FirebaseDatabase.getInstance().getReference("users").child(receiverId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            if (user.isOnline()) {
                                binding.tvStatus.setText("En ligne");
                            } else {
                                binding.tvStatus.setText("Vu " +
                                        TimeUtils.getLastSeenText(user.getLastSeen()));
                            }
                        }
                    }
                    @Override public void onCancelled(DatabaseError error) {}
                });
    }

    private void uploadMedia(Uri uri, String type) {
        String fileName = UUID.randomUUID().toString();
        StorageReference ref = FirebaseStorage.getInstance().getReference()
                .child("media").child(conversationId).child(fileName);

        binding.progressUpload.setVisibility(View.VISIBLE);
        ref.putFile(uri)
                .addOnSuccessListener(task -> ref.getDownloadUrl()
                        .addOnSuccessListener(downloadUri -> {
                            binding.progressUpload.setVisibility(View.GONE);
                            Message message = new Message(currentUserId, receiverId, "", type);
                            message.setMediaUrl(downloadUri.toString());
                            message.setConversationId(conversationId);
                            messagesRef.push().setValue(message);
                            String preview = type.equals(Message.TYPE_IMAGE) ? "📷 Image" :
                                    type.equals(Message.TYPE_VIDEO) ? "🎥 Vidéo" : "🎵 Audio";
                            FirebaseHelper.updateConversation(currentUserId, receiverId, preview, type);
                        }))
                .addOnFailureListener(e -> {
                    binding.progressUpload.setVisibility(View.GONE);
                    Toast.makeText(this, "Échec envoi fichier", Toast.LENGTH_SHORT).show();
                });
    }

    private void startRecording() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, 100);
            return;
        }

        try {
            audioFile = File.createTempFile("audio_", ".3gp", getCacheDir());
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(audioFile.getAbsolutePath());
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            binding.btnAudio.setImageResource(R.drawable.ic_stop);
            Toast.makeText(this, "Enregistrement...", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Erreur d'enregistrement", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecordingAndSend() {
        if (!isRecording) return;
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
        isRecording = false;
        binding.btnAudio.setImageResource(R.drawable.ic_mic);
        uploadMedia(Uri.fromFile(audioFile), Message.TYPE_AUDIO);
    }

    private void showAttachmentOptions() {
        // Bottom sheet with image/video/file options
        imagePickerLauncher.launch("image/*");
    }

    private void toggleEmojiPanel() {
        // Toggle emoji keyboard
    }

    private void startCall(boolean isVideo) {
        Intent intent = new Intent(this, CallActivity.class);
        intent.putExtra("receiverId", receiverId);
        intent.putExtra("isVideo", isVideo);
        intent.putExtra("isOutgoing", true);
        startActivity(intent);
    }

    private void setTypingStatus(boolean isTyping) {
        FirebaseDatabase.getInstance().getReference("typing")
                .child(conversationId).child(currentUserId).setValue(isTyping);
        typingHandler.removeCallbacksAndMessages(null);
        if (isTyping) {
            typingHandler.postDelayed(() ->
                    FirebaseDatabase.getInstance().getReference("typing")
                            .child(conversationId).child(currentUserId).setValue(false), 3000);
        }
    }

    private String generateConversationId(String uid1, String uid2) {
        return uid1.compareTo(uid2) < 0 ? uid1 + "_" + uid2 : uid2 + "_" + uid1;
    }

    private void scrollToBottom() {
        if (messageList.size() > 0) {
            binding.rvMessages.smoothScrollToPosition(messageList.size() - 1);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
