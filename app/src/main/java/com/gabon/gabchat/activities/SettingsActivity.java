package com.gabon.gabchat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.gabon.gabchat.databinding.ActivitySettingsBinding;
import com.gabon.gabchat.utils.FirebaseHelper;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Paramètres");

        setupListeners();
    }

    private void setupListeners() {
        binding.itemEditProfile.setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class)));

        binding.switchNotifications.setOnCheckedChangeListener((btn, checked) ->
                Toast.makeText(this,
                        checked ? "Notifications activées" : "Notifications désactivées",
                        Toast.LENGTH_SHORT).show());

        binding.switchDarkMode.setOnCheckedChangeListener((btn, checked) ->
                Toast.makeText(this, "Thème: " + (checked ? "Sombre" : "Clair"),
                        Toast.LENGTH_SHORT).show());

        binding.itemPrivacy.setOnClickListener(v ->
                Toast.makeText(this, "Confidentialité - Bientôt disponible", Toast.LENGTH_SHORT).show());

        binding.itemSecurity.setOnClickListener(v ->
                Toast.makeText(this, "Sécurité - Bientôt disponible", Toast.LENGTH_SHORT).show());

        binding.itemAbout.setOnClickListener(v ->
                Toast.makeText(this, "GabChat v1.0 - Fait au Gabon 🇬🇦", Toast.LENGTH_LONG).show());

        binding.btnLogout.setOnClickListener(v -> {
            FirebaseHelper.updateOnlineStatus(false);
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity();
        });
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }
}
