package com.gabon.gabchat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.gabon.gabchat.R;
import com.gabon.gabchat.databinding.ActivityMainBinding;
import com.gabon.gabchat.fragments.ChatsFragment;
import com.gabon.gabchat.fragments.ContactsFragment;
import com.gabon.gabchat.fragments.ProfileFragment;
import com.gabon.gabchat.fragments.StatusFragment;
import com.gabon.gabchat.utils.FirebaseHelper;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        // Load default fragment
        loadFragment(new ChatsFragment());

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_chats) {
                loadFragment(new ChatsFragment());
                binding.toolbar.setTitle("GabChat");
            } else if (id == R.id.nav_status) {
                loadFragment(new StatusFragment());
                binding.toolbar.setTitle("Statuts");
            } else if (id == R.id.nav_contacts) {
                loadFragment(new ContactsFragment());
                binding.toolbar.setTitle("Contacts");
            } else if (id == R.id.nav_profile) {
                loadFragment(new ProfileFragment());
                binding.toolbar.setTitle("Profil");
            }
            return true;
        });

        // FAB for new conversation
        binding.fabNewChat.setOnClickListener(v -> {
            startActivity(new Intent(this, NewConversationActivity.class));
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            // TODO: Implement search
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            FirebaseHelper.updateOnlineStatus(false);
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseHelper.updateOnlineStatus(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseHelper.updateOnlineStatus(false);
    }
}
