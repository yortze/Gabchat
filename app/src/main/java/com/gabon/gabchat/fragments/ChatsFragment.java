package com.gabon.gabchat.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.gabon.gabchat.adapters.ConversationAdapter;
import com.gabon.gabchat.databinding.FragmentChatsBinding;
import com.gabon.gabchat.models.User;
import com.gabon.gabchat.utils.FirebaseHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class ChatsFragment extends Fragment {

    private static final String TAG = "ChatsFragment";
    private FragmentChatsBinding binding;
    private ConversationAdapter adapter;
    private final List<User> userList = new ArrayList<>();
    private final List<String> lastMessages = new ArrayList<>();
    private final List<Long> timestamps = new ArrayList<>();
    private final List<Integer> unreadCounts = new ArrayList<>();
    private ValueEventListener conversationsListener;
    private DatabaseReference conversationsRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentChatsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new ConversationAdapter(requireContext(), userList, lastMessages, timestamps, unreadCounts);
        binding.rvConversations.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvConversations.setAdapter(adapter);

        String currentUid = FirebaseHelper.getCurrentUserId();
        if (currentUid == null) {
            showEmpty("Non connecte.");
            return;
        }
        loadConversations(currentUid);
    }

    private void loadConversations(String currentUid) {
        if (binding == null) return;
        binding.progressBar.setVisibility(View.VISIBLE);

        conversationsRef = FirebaseDatabase.getInstance()
                .getReference("conversations").child(currentUid);

        conversationsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // IMPORTANT: vérifier avant d'accéder à binding
                if (!isAdded() || binding == null) return;

                userList.clear();
                lastMessages.clear();
                timestamps.clear();
                unreadCounts.clear();

                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    binding.progressBar.setVisibility(View.GONE);
                    showEmpty("Aucune conversation.\nCommencez a chatter !");
                    adapter.notifyDataSetChanged();
                    return;
                }

                binding.tvEmpty.setVisibility(View.GONE);
                final long[] remaining = {snapshot.getChildrenCount()};

                for (DataSnapshot convSnap : snapshot.getChildren()) {
                    String convId = convSnap.getKey();
                    if (convId == null) { remaining[0]--; continue; }

                    // Extraire l'UID de l'autre participant
                    String otherUid = convId.replace(currentUid + "_", "")
                            .replace("_" + currentUid, "");
                    if (otherUid.equals(convId)) { remaining[0]--; continue; }

                    String lastMsg = convSnap.child("lastMessage").getValue(String.class);
                    Long ts = convSnap.child("lastMessageTime").getValue(Long.class);
                    Integer unread = convSnap.child("unreadCount").getValue(Integer.class);

                    FirebaseDatabase.getInstance().getReference("users")
                            .child(otherUid)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot userSnap) {
                                    // Vérifier à nouveau — callback asynchrone
                                    if (!isAdded() || binding == null) return;

                                    User user = userSnap.getValue(User.class);
                                    if (user != null) {
                                        if (user.getUid() == null) user.setUid(userSnap.getKey());
                                        userList.add(user);
                                        lastMessages.add(lastMsg != null ? lastMsg : "");
                                        timestamps.add(ts != null ? ts : 0L);
                                        unreadCounts.add(unread != null ? unread : 0);
                                    }
                                    remaining[0]--;
                                    if (remaining[0] <= 0) {
                                        binding.progressBar.setVisibility(View.GONE);
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError e) {
                                    remaining[0]--;
                                    if (isAdded() && binding != null && remaining[0] <= 0) {
                                        binding.progressBar.setVisibility(View.GONE);
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // IMPORTANT: vérifier binding avant d'y accéder
                if (!isAdded() || binding == null) return;
                binding.progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Erreur conversations: " + error.getMessage());
                showEmpty("Erreur: " + error.getMessage());
            }
        };

        conversationsRef.addValueEventListener(conversationsListener);
    }

    private void showEmpty(String msg) {
        if (binding == null) return;
        binding.tvEmpty.setVisibility(View.VISIBLE);
        binding.tvEmpty.setText(msg);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (conversationsRef != null && conversationsListener != null) {
            conversationsRef.removeEventListener(conversationsListener);
            conversationsListener = null;
        }
        binding = null;
    }
}