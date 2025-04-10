package com.lapTrinhUUDD.movie.Adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.lapTrinhUUDD.movie.Common.ChatCallback;
import com.lapTrinhUUDD.movie.Models.Message;
import com.lapTrinhUUDD.movie.databinding.LayoutLeftChatItemBinding;
import com.lapTrinhUUDD.movie.databinding.LayoutRightChatItemBinding;

public class ChatAdapter extends ListAdapter<Message, RecyclerView.ViewHolder> {

    private final ChatCallback callback;
    private LeftChatViewHolder leftViewHolder;

    public static final int RIGHT = 1;
    public static final int LEFT = 2;

    public static final DiffUtil.ItemCallback<Message> CHAT_DIFF = new DiffUtil.ItemCallback<Message>() {
        @Override
        public boolean areItemsTheSame(Message oldItem, Message newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @SuppressLint("DiffUtilEquals")
        @Override
        public boolean areContentsTheSame(Message oldItem, Message newItem) {
            return oldItem.equals(newItem);
        }
    };

    public ChatAdapter(ChatCallback callback) {
        super(CHAT_DIFF);
        this.callback = callback;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == RIGHT) {
            LayoutRightChatItemBinding binding = LayoutRightChatItemBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new RightChatViewHolder(binding);
        } else {
            LayoutLeftChatItemBinding binding = LayoutLeftChatItemBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new LeftChatViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Message message = getItem(position);
        if (holder instanceof LeftChatViewHolder) {
            LeftChatViewHolder leftHolder = (LeftChatViewHolder) holder;
            leftHolder.bind(message);
            if (position == getItemCount() - 1) {
                leftViewHolder = leftHolder;
            }
        } else if (holder instanceof RightChatViewHolder) {
            RightChatViewHolder rightHolder = (RightChatViewHolder) holder;
            rightHolder.bind(message);
            if (position == getItemCount() - 1) {
                callback.onMessageSent(message);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getRole() == RIGHT ? RIGHT : LEFT;
    }

    public class RightChatViewHolder extends RecyclerView.ViewHolder {
        private final LayoutRightChatItemBinding binding;

        public RightChatViewHolder(LayoutRightChatItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Message message) {
            binding.tvMessageRight.setText(message.getContent());
            binding.tvTimeRight.setText(message.getTime());
        }
    }

    public class LeftChatViewHolder extends RecyclerView.ViewHolder {
        private final LayoutLeftChatItemBinding binding;
        private String currentContent;

        public LeftChatViewHolder(LayoutLeftChatItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Message message) {
            binding.tvTimeLeft.setText(message.getTime());

            if ("...".equals(message.getContent())) {
                binding.tvMessageLeft.setText(message.getContent());
                currentContent = message.getContent();
                return;
            }

            binding.tvMessageLeft.setText(message.getContent());
            currentContent = message.getContent();
        }
    }
}
