package com.gabon.gabchat.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.gabon.gabchat.databinding.ActivityMediaViewerBinding;
import com.gabon.gabchat.models.Message;

public class MediaViewerActivity extends AppCompatActivity {

    private ActivityMediaViewerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMediaViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String mediaUrl = getIntent().getStringExtra("mediaUrl");
        String mediaType = getIntent().getStringExtra("mediaType");

        if (Message.TYPE_IMAGE.equals(mediaType)) {
            Glide.with(this).load(mediaUrl).into(binding.ivMedia);
        }

        binding.btnClose.setOnClickListener(v -> finish());
    }
}
