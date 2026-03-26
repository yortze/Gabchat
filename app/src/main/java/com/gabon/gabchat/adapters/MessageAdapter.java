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
import com.gabon.gabchat.activities.MediaViewerActivity;
import com.gabon.gabchat.models.Message;
import com.gabon.gabchat.utils.TimeUtils;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private Context context;
    private List<Message> messages;
    private String currentUserId;

    public MessageAdapter(Context context, List<Message> messages, String currentUserId) {
        this.context = context;
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getSenderId().equals(currentUserId)
                ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        if (holder instanceof SentMessageHolder) {
            ((SentMessageHolder) holder).bind(message);
        } else {
            ((ReceivedMessageHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() { return messages.size(); }

    class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime, tvStatus;
        ImageView ivMedia;
        View layoutMedia;

        SentMessageHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvStatus = itemView.findViewById(R.id.tv_status);
            ivMedia = itemView.findViewById(R.id.iv_media);
            layoutMedia = itemView.findViewById(R.id.layout_media);
        }

        void bind(Message message) {
            tvTime.setText(TimeUtils.formatMessageTime(message.getTimestamp()));

            switch (message.getType()) {
                case Message.TYPE_TEXT:
                    tvMessage.setVisibility(View.VISIBLE);
                    layoutMedia.setVisibility(View.GONE);
                    tvMessage.setText(message.isDeleted() ? "🚫 Message supprimé" : message.getContent());
                    break;
                case Message.TYPE_IMAGE:
                    tvMessage.setVisibility(View.GONE);
                    layoutMedia.setVisibility(View.VISIBLE);
                    Glide.with(context).load(message.getMediaUrl())
                            .placeholder(R.drawable.ic_image_placeholder).into(ivMedia);
                    ivMedia.setOnClickListener(v -> openMedia(message));
                    break;
                case Message.TYPE_AUDIO:
                    tvMessage.setVisibility(View.VISIBLE);
                    tvMessage.setText("🎵 Message vocal");
                    layoutMedia.setVisibility(View.GONE);
                    break;
                case Message.TYPE_VIDEO:
                    tvMessage.setVisibility(View.GONE);
                    layoutMedia.setVisibility(View.VISIBLE);
                    ivMedia.setImageResource(R.drawable.ic_video_placeholder);
                    ivMedia.setOnClickListener(v -> openMedia(message));
                    break;
            }

            // Status indicators
            switch (message.getStatus()) {
                case Message.STATUS_SENT: tvStatus.setText("✓"); break;
                case Message.STATUS_DELIVERED: tvStatus.setText("✓✓"); break;
                case Message.STATUS_READ: tvStatus.setText("✓✓"); tvStatus.setTextColor(0xFF00B0FF); break;
            }
        }
    }

    class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;
        ImageView ivMedia;
        View layoutMedia;

        ReceivedMessageHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTime = itemView.findViewById(R.id.tv_time);
            ivMedia = itemView.findViewById(R.id.iv_media);
            layoutMedia = itemView.findViewById(R.id.layout_media);
        }

        void bind(Message message) {
            tvTime.setText(TimeUtils.formatMessageTime(message.getTimestamp()));

            switch (message.getType()) {
                case Message.TYPE_TEXT:
                    tvMessage.setVisibility(View.VISIBLE);
                    layoutMedia.setVisibility(View.GONE);
                    tvMessage.setText(message.isDeleted() ? "🚫 Message supprimé" : message.getContent());
                    break;
                case Message.TYPE_IMAGE:
                    tvMessage.setVisibility(View.GONE);
                    layoutMedia.setVisibility(View.VISIBLE);
                    Glide.with(context).load(message.getMediaUrl())
                            .placeholder(R.drawable.ic_image_placeholder).into(ivMedia);
                    ivMedia.setOnClickListener(v -> openMedia(message));
                    break;
                case Message.TYPE_AUDIO:
                    tvMessage.setVisibility(View.VISIBLE);
                    tvMessage.setText("🎵 Message vocal");
                    layoutMedia.setVisibility(View.GONE);
                    break;
                case Message.TYPE_VIDEO:
                    tvMessage.setVisibility(View.GONE);
                    layoutMedia.setVisibility(View.VISIBLE);
                    ivMedia.setImageResource(R.drawable.ic_video_placeholder);
                    ivMedia.setOnClickListener(v -> openMedia(message));
                    break;
            }
        }
    }

    private void openMedia(Message message) {
        Intent intent = new Intent(context, MediaViewerActivity.class);
        intent.putExtra("mediaUrl", message.getMediaUrl());
        intent.putExtra("mediaType", message.getType());
        context.startActivity(intent);
    }
}
