package com.gabon.gabchat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.gabon.gabchat.databinding.ActivityOtpVerificationBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class OtpVerificationActivity extends AppCompatActivity {

    private ActivityOtpVerificationBinding binding;
    private FirebaseAuth mAuth;
    private String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtpVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        verificationId = getIntent().getStringExtra("verificationId");
        String phone = getIntent().getStringExtra("phone");

        binding.tvPhone.setText("Code envoyé au " + phone);
        binding.btnVerify.setOnClickListener(v -> verifyCode());
        binding.btnResend.setOnClickListener(v -> finish());
    }

    private void verifyCode() {
        String code = binding.etOtp.getText().toString().trim();
        if (TextUtils.isEmpty(code) || code.length() < 6) {
            binding.tilOtp.setError("Code invalide");
            return;
        }

        showLoading(true);
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        mAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    showLoading(false);
                    startActivity(new Intent(this, MainActivity.class));
                    finishAffinity();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Code incorrect", Toast.LENGTH_SHORT).show();
                });
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.btnVerify.setEnabled(!show);
    }
}
