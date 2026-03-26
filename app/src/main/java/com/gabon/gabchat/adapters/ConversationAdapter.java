package com.gabon.gabchat.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.gabon.gabchat.R;
import com.gabon.gabchat.activities.ChatActivity;
import com.gabon.gabchat.models.User;
import com.gabon.gabchat.utils.TimeUtils;
import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {

    private final Context context;
    private final List<User> users;
    private final List<String> lastMessages;
    private final List<Long> timestamps;
    private final List<Integer> unreadCounts;

    public ConversationAdapter(Context context, List<User> users,
                               List<String> lastMessages, List<Long> timestamps,
                               List<Integer> unreadCounts) {
        this.context = context;
        this.users = users;
        this.lastMessages = lastMessages;
        this.timestamps = timestamps;
        this.unreadCounts = unreadCounts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_conversation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);
        if (user == null) return;

        String name = user.getDisplayName();
        holder.tvName.setText(name != null ? name : "Utilisateur");
        holder.tvLastMessage.setText(lastMessages.get(position));
        holder.tvTime.setText(TimeUtils.formatConversationTime(timestamps.get(position)));

        // Avatar
        String photoUrl = user.getPhotoUrl();
        if (holder.ivAvatar != null) {
            if (photoUrl != null && !photoUrl.isEmpty()) {
                Glide.with(context).load(photoUrl)
                        .placeholder(R.drawable.ic_default_avatar)
                        .circleCrop()
                        .into(holder.ivAvatar);
            } else {
                holder.ivAvatar.setImageResource(R.drawable.ic_default_avatar);
            }
        }

        // Indicateur en ligne — View générique, pas ImageView
        if (holder.vOnline != null) {
            holder.vOnline.setVisibility(user.isOnline() ? View.VISIBLE : View.GONE);
        }

        // Badge non lu
        int unread = unreadCounts.get(position);
        if (holder.tvUnread != null) {
            if (unread > 0) {
                holder.tvUnread.setVisibility(View.VISIBLE);
                holder.tvUnread.setText(unread > 99 ? "99+" : String.valueOf(unread));
            } else {
                holder.tvUnread.setVisibility(View.GONE);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (user.getUid() == null) return;
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("receiverId", user.getUid());
            intent.putExtra("receiverName", name != null ? name : "");
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return users != null ? users.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        View vOnline;        // ← View simple, PAS ImageView
        TextView tvName;
        TextView tvLastMessage;
        TextView tvTime;
        TextView tvUnread;

        ViewHolder(View itemView) {
            super(itemView);
            ivAvatar      = itemView.findViewById(R.id.iv_avatar);
            vOnline       = itemView.findViewById(R.id.iv_online_indicator); // View, pas ImageView
            tvName        = itemView.findViewById(R.id.tv_name);
            tvLastMessage = itemView.findViewById(R.id.tv_last_message);
            tvTime        = itemView.findViewById(R.id.tv_time);
            tvUnread      = itemView.findViewById(R.id.tv_unread);
        }
    }
}