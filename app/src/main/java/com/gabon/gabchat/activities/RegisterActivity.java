package com.gabon.gabchat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.gabon.gabchat.databinding.ActivityRegisterBinding;
import com.gabon.gabchat.models.User;
import com.gabon.gabchat.utils.FirebaseHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        binding.btnRegister.setOnClickListener(v -> register());
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void register() {
        String name = binding.etName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) { binding.tilName.setError("Nom requis"); return; }
        if (TextUtils.isEmpty(email)) { binding.tilEmail.setError("E-mail requis"); return; }
        if (TextUtils.isEmpty(password)) { binding.tilPassword.setError("Mot de passe requis"); return; }
        if (password.length() < 6) { binding.tilPassword.setError("Minimum 6 caractères"); return; }
        if (!password.equals(confirmPassword)) {
            binding.tilConfirmPassword.setError("Les mots de passe ne correspondent pas"); return;
        }

        showLoading(true);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    User user = new User(uid, name, email, phone);

                    FirebaseDatabase.getInstance().getReference("users").child(uid)
                            .setValue(user)
                            .addOnSuccessListener(unused -> {
                                authResult.getUser().sendEmailVerification();
                                showLoading(false);
                                Toast.makeText(this, "Compte créé ! Vérifiez votre e-mail.", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(this, MainActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                showLoading(false);
                                Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Erreur d'inscription: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.btnRegister.setEnabled(!show);
    }
}
