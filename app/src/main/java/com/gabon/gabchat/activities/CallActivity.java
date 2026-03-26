package com.gabon.gabchat.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.gabon.gabchat.R;
import com.gabon.gabchat.databinding.ActivityCallBinding;
import com.gabon.gabchat.models.User;
import com.google.firebase.database.*;
import java.util.Locale;

public class CallActivity extends AppCompatActivity {

    private ActivityCallBinding binding;
    private String receiverId;
    private boolean isVideo;
    private boolean isOutgoing;
    private boolean isMuted = false;
    private boolean isSpeakerOn = false;
    private boolean isCameraOff = false;
    private long callStartTime;
    private Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        receiverId = getIntent().getStringExtra("receiverId");
        isVideo = getIntent().getBooleanExtra("isVideo", false);
        isOutgoing = getIntent().getBooleanExtra("isOutgoing", true);

        loadContactInfo();
        setupControls();
        setupCallType();

        if (isOutgoing) {
            binding.tvCallStatus.setText("Appel en cours...");
            // Simulate call connection after 3 seconds (replace with real WebRTC)
            new Handler(Looper.getMainLooper()).postDelayed(this::onCallConnected, 3000);
        } else {
            binding.tvCallStatus.setText("Appel entrant");
            binding.layoutIncomingControls.setVisibility(View.VISIBLE);
            binding.layoutCallControls.setVisibility(View.GONE);
        }
    }

    private void loadContactInfo() {
        FirebaseDatabase.getInstance().getReference("users").child(receiverId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            binding.tvContactName.setText(user.getDisplayName());
                            if (user.getPhotoUrl() != null) {
                                Glide.with(CallActivity.this)
                                        .load(user.getPhotoUrl())
                                        .placeholder(R.drawable.ic_default_avatar)
                                        .circleCrop()
                                        .into(binding.ivContactAvatar);
                            }
                        }
                    }
                    @Override public void onCancelled(DatabaseError error) {}
                });
    }

    private void setupCallType() {
        binding.btnToggleCamera.setVisibility(isVideo ? View.VISIBLE : View.GONE);
        binding.ivLocalVideo.setVisibility(isVideo ? View.VISIBLE : View.GONE);
    }

    private void setupControls() {
        binding.btnEndCall.setOnClickListener(v -> endCall());
        binding.btnAcceptCall.setOnClickListener(v -> acceptCall());
        binding.btnDeclineCall.setOnClickListener(v -> declineCall());

        binding.btnMute.setOnClickListener(v -> {
            isMuted = !isMuted;
            binding.btnMute.setImageResource(isMuted ? R.drawable.ic_mic_off : R.drawable.ic_mic);
            Toast.makeText(this, isMuted ? "Micro désactivé" : "Micro activé",
                    Toast.LENGTH_SHORT).show();
        });

        binding.btnSpeaker.setOnClickListener(v -> {
            isSpeakerOn = !isSpeakerOn;
            binding.btnSpeaker.setImageResource(isSpeakerOn ?
                    R.drawable.ic_speaker_on : R.drawable.ic_speaker_off);
        });

        binding.btnToggleCamera.setOnClickListener(v -> {
            isCameraOff = !isCameraOff;
            binding.btnToggleCamera.setImageResource(isCameraOff ?
                    R.drawable.ic_camera_off : R.drawable.ic_camera);
        });
    }

    private void onCallConnected() {
        binding.tvCallStatus.setVisibility(View.GONE);
        binding.tvCallDuration.setVisibility(View.VISIBLE);
        callStartTime = System.currentTimeMillis();
        startTimer();
    }

    private void startTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - callStartTime;
                long seconds = (elapsed / 1000) % 60;
                long minutes = (elapsed / 60000) % 60;
                long hours = elapsed / 3600000;
                String time = hours > 0
                        ? String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
                        : String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
                binding.tvCallDuration.setText(time);
                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.post(timerRunnable);
    }

    private void acceptCall() {
        binding.layoutIncomingControls.setVisibility(View.GONE);
        binding.layoutCallControls.setVisibility(View.VISIBLE);
        onCallConnected();
    }

    private void declineCall() {
        finish();
    }

    private void endCall() {
        timerHandler.removeCallbacks(timerRunnable);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacksAndMessages(null);
    }
}
