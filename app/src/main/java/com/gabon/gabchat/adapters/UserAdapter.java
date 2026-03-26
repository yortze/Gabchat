package com.gabon.gabchat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.gabon.gabchat.R;
import com.gabon.gabchat.models.User;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    private final Context context;
    private final List<User> users;
    private final OnUserClickListener listener;

    public UserAdapter(Context context, List<User> users, OnUserClickListener listener) {
        this.context = context;
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);
        if (user == null) return;

        String name = user.getDisplayName();
        holder.tvName.setText(name != null ? name : "Utilisateur");

        String status = user.getStatus();
        holder.tvStatus.setText(status != null ? status : "");

        // Indicateur en ligne
        if (holder.vOnline != null) {
            holder.vOnline.setVisibility(user.isOnline() ? View.VISIBLE : View.GONE);
        }

        // Photo de profil
        String photoUrl = user.getPhotoUrl();
        if (photoUrl != null && !photoUrl.isEmpty() && holder.ivAvatar != null) {
            Glide.with(context)
                    .load(photoUrl)
                    .placeholder(R.drawable.ic_default_avatar)
                    .circleCrop()
                    .into(holder.ivAvatar);
        } else if (holder.ivAvatar != null) {
            holder.ivAvatar.setImageResource(R.drawable.ic_default_avatar);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onUserClick(user);
        });
    }

    @Override
    public int getItemCount() {
        return users != null ? users.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        View vOnline;        // View générique — pas ImageView
        TextView tvName;
        TextView tvStatus;

        ViewHolder(View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            vOnline  = itemView.findViewById(R.id.iv_online_indicator); // View, pas ImageView
            tvName   = itemView.findViewById(R.id.tv_name);
            tvStatus = itemView.findViewById(R.id.tv_status);
        }
    }
}