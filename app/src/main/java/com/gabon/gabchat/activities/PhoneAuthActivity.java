package com.gabon.gabchat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.gabon.gabchat.databinding.ActivityPhoneAuthBinding;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import java.util.concurrent.TimeUnit;

public class PhoneAuthActivity extends AppCompatActivity {

    private ActivityPhoneAuthBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPhoneAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        binding.btnSendCode.setOnClickListener(v -> sendVerificationCode());
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void sendVerificationCode() {
        String countryCode = binding.ccp.getSelectedCountryCodeWithPlus();
        String number = binding.etPhone.getText().toString().trim();

        if (TextUtils.isEmpty(number) || number.length() < 8) {
            binding.tilPhone.setError("Numéro de téléphone invalide");
            return;
        }

        String fullPhone = countryCode + number;
        showLoading(true);

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(fullPhone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        showLoading(false);
                        signInWithPhoneCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        showLoading(false);
                        Toast.makeText(PhoneAuthActivity.this,
                                "Échec envoi code: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCodeSent(String verificationId,
                                           PhoneAuthProvider.ForceResendingToken token) {
                        showLoading(false);
                        Intent intent = new Intent(PhoneAuthActivity.this, OtpVerificationActivity.class);
                        intent.putExtra("verificationId", verificationId);
                        intent.putExtra("phone", fullPhone);
                        startActivity(intent);
                    }
                })
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void signInWithPhoneCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    startActivity(new Intent(this, MainActivity.class));
                    finishAffinity();
                })
                .addOnFailureListener(e ->
                    Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.btnSendCode.setEnabled(!show);
    }
}
