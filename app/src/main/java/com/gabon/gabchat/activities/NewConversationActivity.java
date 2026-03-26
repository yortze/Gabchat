package com.gabon.gabchat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.*;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.gabon.gabchat.adapters.UserAdapter;
import com.gabon.gabchat.databinding.ActivityNewConversationBinding;
import com.gabon.gabchat.models.User;
import com.gabon.gabchat.utils.FirebaseHelper;
import com.google.firebase.database.*;
import java.util.*;

public class NewConversationActivity extends AppCompatActivity {

    private ActivityNewConversationBinding binding;
    private UserAdapter adapter;
    private List<User> allUsers = new ArrayList<>();
    private List<User> filteredUsers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewConversationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Nouvelle conversation");

        adapter = new UserAdapter(this, filteredUsers, user -> {
            Intent i = new Intent(this, ChatActivity.class);
            i.putExtra("receiverId", user.getUid());
            startActivity(i);
            finish();
        });

        binding.rvUsers.setLayoutManager(new LinearLayoutManager(this));
        binding.rvUsers.setAdapter(adapter);

        binding.btnCreateGroup.setOnClickListener(v ->
                startActivity(new Intent(this, CreateGroupActivity.class)));

        loadUsers();

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
        });
    }

    private void loadUsers() {
        String uid = FirebaseHelper.getCurrentUserId();
        FirebaseDatabase.getInstance().getReference("users")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        allUsers.clear();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            User user = snap.getValue(User.class);
                            if (user != null && !user.getUid().equals(uid)) allUsers.add(user);
                        }
                        filteredUsers.clear();
                        filteredUsers.addAll(allUsers);
                        adapter.notifyDataSetChanged();
                    }
                    @Override public void onCancelled(DatabaseError e) {}
                });
    }

    private void filter(String query) {
        filteredUsers.clear();
        if (query.isEmpty()) {
            filteredUsers.addAll(allUsers);
        } else {
            String q = query.toLowerCase();
            for (User u : allUsers) {
                if (u.getDisplayName().toLowerCase().contains(q)) filteredUsers.add(u);
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }
}
