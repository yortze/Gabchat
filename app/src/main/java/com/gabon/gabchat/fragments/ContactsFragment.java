package com.gabon.gabchat.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.gabon.gabchat.activities.ChatActivity;
import com.gabon.gabchat.adapters.UserAdapter;
import com.gabon.gabchat.databinding.FragmentContactsBinding;
import com.gabon.gabchat.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class ContactsFragment extends Fragment {

    private static final String TAG = "ContactsFragment";
    private FragmentContactsBinding binding;
    private UserAdapter adapter;
    private final List<User> allUsers = new ArrayList<>();
    private final List<User> filteredUsers = new ArrayList<>();
    private ValueEventListener usersListener;
    private DatabaseReference usersRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentContactsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new UserAdapter(requireContext(), filteredUsers, user -> {
            if (user == null || user.getUid() == null) return;
            Intent intent = new Intent(requireContext(), ChatActivity.class);
            intent.putExtra("receiverId", user.getUid());
            intent.putExtra("receiverName", user.getDisplayName() != null ? user.getDisplayName() : "");
            startActivity(intent);
        });

        binding.rvContacts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvContacts.setAdapter(adapter);

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s != null ? s.toString() : "");
            }
        });

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            showMessage("Non connecte. Reconnectez-vous.");
            return;
        }
        loadUsers(currentUser.getUid());
    }

    private void loadUsers(String currentUid) {
        if (binding == null) return;
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.tvEmpty.setVisibility(View.GONE);

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // IMPORTANT: vérifier que le fragment est encore attaché
                if (!isAdded() || binding == null) return;

                allUsers.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    try {
                        User user = snap.getValue(User.class);
                        if (user == null) continue;

                        // Corriger uid null — BUG PRINCIPAL
                        String uid = user.getUid();
                        if (uid == null || uid.isEmpty()) {
                            uid = snap.getKey();
                            user.setUid(uid);
                        }

                        // Corriger displayName null
                        if (user.getDisplayName() == null || user.getDisplayName().isEmpty()) {
                            user.setDisplayName("Utilisateur");
                        }

                        // Exclure l'utilisateur courant
                        // Utiliser uid local (jamais null maintenant)
                        if (!currentUid.equals(uid)) {
                            allUsers.add(user);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Erreur parsing: " + e.getMessage());
                    }
                }

                Log.d(TAG, "Contacts charges: " + allUsers.size());
                filteredUsers.clear();
                filteredUsers.addAll(allUsers);
                if (adapter != null) adapter.notifyDataSetChanged();
                binding.progressBar.setVisibility(View.GONE);

                if (allUsers.isEmpty()) {
                    showMessage("Aucun contact trouve.\nInvitez des amis !");
                } else {
                    binding.tvEmpty.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // IMPORTANT: vérifier binding avant d'y accéder
                if (!isAdded() || binding == null) return;
                binding.progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Firebase erreur: " + error.getCode() + " - " + error.getMessage());
                if (error.getCode() == DatabaseError.PERMISSION_DENIED) {
                    showMessage("Acces refuse. Corrigez les regles Firebase:\nAjoutez \".read\": \"auth != null\" sous \"users\"");
                } else {
                    showMessage("Erreur: " + error.getMessage());
                }
            }
        };
        usersRef.addValueEventListener(usersListener);
    }

    private void filter(String query) {
        if (binding == null) return;
        filteredUsers.clear();
        if (query == null || query.isEmpty()) {
            filteredUsers.addAll(allUsers);
        } else {
            String q = query.toLowerCase().trim();
            for (User u : allUsers) {
                if (u == null) continue;
                boolean nm = u.getDisplayName() != null && u.getDisplayName().toLowerCase().contains(q);
                boolean em = u.getEmail() != null && u.getEmail().toLowerCase().contains(q);
                boolean pm = u.getPhone() != null && u.getPhone().contains(q);
                if (nm || em || pm) filteredUsers.add(u);
            }
        }
        if (adapter != null) adapter.notifyDataSetChanged();
        binding.tvEmpty.setVisibility(filteredUsers.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showMessage(String msg) {
        if (binding == null) return;
        binding.progressBar.setVisibility(View.GONE);
        binding.tvEmpty.setVisibility(View.VISIBLE);
        binding.tvEmpty.setText(msg);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (usersRef != null && usersListener != null) {
            usersRef.removeEventListener(usersListener);
            usersListener = null;
        }
        binding = null;
    }
}