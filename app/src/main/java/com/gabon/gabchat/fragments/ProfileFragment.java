package com.gabon.gabchat.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.gabon.gabchat.R;
import com.gabon.gabchat.activities.EditProfileActivity;
import com.gabon.gabchat.databinding.FragmentProfileBinding;
import com.gabon.gabchat.models.User;
import com.gabon.gabchat.utils.FirebaseHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private FragmentProfileBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), EditProfileActivity.class)));

        String uid = FirebaseHelper.getCurrentUserId();
        if (uid == null) return;

        FirebaseDatabase.getInstance().getReference("users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // IMPORTANT: vérifier avant d'accéder à binding
                        if (!isAdded() || binding == null) return;

                        User user = snapshot.getValue(User.class);
                        if (user == null) return;

                        binding.tvName.setText(
                                user.getDisplayName() != null ? user.getDisplayName() : "");
                        binding.tvStatus.setText(
                                user.getStatus() != null ? user.getStatus() : "Disponible");
                        binding.tvEmail.setText(
                                user.getEmail() != null ? user.getEmail() : "");
                        binding.tvPhone.setText(
                                user.getPhone() != null && !user.getPhone().isEmpty()
                                        ? user.getPhone() : "Non renseigne");
                        binding.tvBio.setText(
                                user.getBio() != null && !user.getBio().isEmpty()
                                        ? user.getBio() : "Aucune bio");

                        String photoUrl = user.getPhotoUrl();
                        if (photoUrl != null && !photoUrl.isEmpty()) {
                            Glide.with(requireContext())
                                    .load(photoUrl)
                                    .placeholder(R.drawable.ic_default_avatar)
                                    .circleCrop()
                                    .into(binding.ivProfilePhoto);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Erreur profil: " + error.getMessage());
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}