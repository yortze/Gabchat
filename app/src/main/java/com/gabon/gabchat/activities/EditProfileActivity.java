package com.gabon.gabchat.activities;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.gabon.gabchat.R;
import com.gabon.gabchat.databinding.ActivityEditProfileBinding;
import com.gabon.gabchat.models.User;
import com.gabon.gabchat.utils.FirebaseHelper;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;
    private Uri selectedImageUri;
    private String currentPhotoUrl;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    Glide.with(this).load(uri).circleCrop().into(binding.ivProfilePhoto);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Modifier le profil");

        loadCurrentProfile();

        binding.ivProfilePhoto.setOnClickListener(v ->
                imagePickerLauncher.launch("image/*"));
        binding.btnFabPhoto.setOnClickListener(v ->
                imagePickerLauncher.launch("image/*"));
        binding.btnSave.setOnClickListener(v -> saveProfile());
    }

    private void loadCurrentProfile() {
        String uid = FirebaseHelper.getCurrentUserId();
        if (uid == null) return;

        FirebaseDatabase.getInstance().getReference("users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            binding.etName.setText(user.getDisplayName());
                            binding.etStatus.setText(user.getStatus());
                            binding.etBio.setText(user.getBio());
                            currentPhotoUrl = user.getPhotoUrl();
                            if (currentPhotoUrl != null) {
                                Glide.with(EditProfileActivity.this)
                                        .load(currentPhotoUrl)
                                        .placeholder(R.drawable.ic_default_avatar)
                                        .circleCrop()
                                        .into(binding.ivProfilePhoto);
                            }
                        }
                    }
                    @Override public void onCancelled(DatabaseError error) {}
                });
    }

    private void saveProfile() {
        String name = binding.etName.getText().toString().trim();
        String status = binding.etStatus.getText().toString().trim();
        String bio = binding.etBio.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            binding.tilName.setError("Le nom est requis");
            return;
        }

        showLoading(true);

        if (selectedImageUri != null) {
            uploadPhotoThenSave(name, status, bio);
        } else {
            saveToDatabase(name, status, bio, currentPhotoUrl);
        }
    }

    private void uploadPhotoThenSave(String name, String status, String bio) {
        String uid = FirebaseHelper.getCurrentUserId();
        StorageReference ref = FirebaseStorage.getInstance().getReference()
                .child("profile_photos").child(uid + ".jpg");

        ref.putFile(selectedImageUri)
                .addOnSuccessListener(task -> ref.getDownloadUrl()
                        .addOnSuccessListener(uri -> saveToDatabase(name, status, bio, uri.toString())))
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Erreur upload photo", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveToDatabase(String name, String status, String bio, String photoUrl) {
        String uid = FirebaseHelper.getCurrentUserId();
        Map<String, Object> updates = new HashMap<>();
        updates.put("displayName", name);
        updates.put("status", status);
        updates.put("bio", bio);
        if (photoUrl != null) updates.put("photoUrl", photoUrl);

        FirebaseDatabase.getInstance().getReference("users").child(uid)
                .updateChildren(updates)
                .addOnSuccessListener(unused -> {
                    showLoading(false);
                    Toast.makeText(this, "Profil mis à jour ✓", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.btnSave.setEnabled(!show);
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }
}
