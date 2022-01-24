package com.example.chatapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.databinding.MessageReceiveLayoutBinding
import com.example.chatapp.databinding.MessageSendLayoutBinding
import com.example.chatapp.models.Message

class PrivateChatRvAdapter(val sender: String): ListAdapter<Message, RecyclerView.ViewHolder>(MyDiffUtils()) {
    inner class ReceiveVh(val binding: MessageReceiveLayoutBinding): RecyclerView.ViewHolder(binding.root) {
        fun onBind(message: Message) {
            binding.msg.text = message.text
            binding.time.text = message.time
        }
    }

    inner class SendVh(val binding: MessageSendLayoutBinding): RecyclerView.ViewHolder(binding.root) {
        fun onBind(message: Message) {
            binding.msg.text = message.text
            binding.time.text = message.time
        }

    }

    class MyDiffUtils() : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.equals(newItem)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 1) {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.message_send_layout, parent, false)
            return SendVh(MessageSendLayoutBinding.bind(itemView))
        } else {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.message_receive_layout, parent, false)
            return ReceiveVh(MessageReceiveLayoutBinding.bind(itemView))
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (getItem(position).from == sender) {
            return 1
        } else {
            return 2
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType == 1) {
            holder as SendVh
            holder.onBind(getItem(position))
        } else {
            holder as ReceiveVh
            holder.onBind(getItem(position))
        }
    }
}