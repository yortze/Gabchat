package com.gabon.gabchat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.gabon.gabchat.adapters.UserAdapter;
import com.gabon.gabchat.databinding.ActivityCreateGroupBinding;
import com.gabon.gabchat.models.Group;
import com.gabon.gabchat.models.User;
import com.gabon.gabchat.utils.FirebaseHelper;
import com.google.firebase.database.*;
import java.util.*;

public class CreateGroupActivity extends AppCompatActivity {

    private ActivityCreateGroupBinding binding;
    private UserAdapter adapter;
    private List<User> allUsers = new ArrayList<>();
    private List<User> displayUsers = new ArrayList<>();
    private Set<String> selectedUserIds = new HashSet<>();
    private String currentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Créer un groupe");

        currentUid = FirebaseHelper.getCurrentUserId();

        adapter = new UserAdapter(this, displayUsers, user -> {
            if (selectedUserIds.contains(user.getUid())) {
                selectedUserIds.remove(user.getUid());
            } else {
                selectedUserIds.add(user.getUid());
            }
            binding.tvSelectedCount.setText(selectedUserIds.size() + " sélectionné(s)");
        });

        binding.rvUsers.setLayoutManager(new LinearLayoutManager(this));
        binding.rvUsers.setAdapter(adapter);
        binding.btnCreate.setOnClickListener(v -> createGroup());
        loadUsers();
    }

    private void loadUsers() {
        FirebaseDatabase.getInstance().getReference("users")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        allUsers.clear();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            User u = snap.getValue(User.class);
                            if (u != null && !u.getUid().equals(currentUid)) allUsers.add(u);
                        }
                        displayUsers.clear();
                        displayUsers.addAll(allUsers);
                        adapter.notifyDataSetChanged();
                    }
                    @Override public void onCancelled(DatabaseError e) {}
                });
    }

    private void createGroup() {
        String name = binding.etGroupName.getText().toString().trim();
        String desc = binding.etGroupDescription.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            binding.tilGroupName.setError("Nom du groupe requis");
            return;
        }
        if (selectedUserIds.isEmpty()) {
            Toast.makeText(this, "Sélectionnez au moins un membre", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> members = new ArrayList<>(selectedUserIds);
        members.add(currentUid);
        Group group = new Group(name, desc, currentUid, members);

        DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference("groups").push();
        String groupId = groupRef.getKey();
        group.setGroupId(groupId);

        groupRef.setValue(group).addOnSuccessListener(unused -> {
            // Add group to each member's group list
            for (String uid : members) {
                FirebaseDatabase.getInstance().getReference("user_groups")
                        .child(uid).child(groupId).setValue(true);
            }
            Toast.makeText(this, "Groupe \"" + name + "\" créé !", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(this, GroupChatActivity.class);
            i.putExtra("groupId", groupId);
            i.putExtra("groupName", name);
            startActivity(i);
            finish();
        });
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }
}
