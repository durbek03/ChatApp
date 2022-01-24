package com.example.chatapp.adapters

import android.text.Layout
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.databinding.GroupReceiveLayoutBinding
import com.example.chatapp.databinding.MessageSendLayoutBinding
import com.example.chatapp.models.GroupMessage

class GroupChatRvAdaper(val currentUser: String): ListAdapter<GroupMessage, RecyclerView.ViewHolder>(MyDiffUtils()) {
    inner class SendVh(val binding: MessageSendLayoutBinding): RecyclerView.ViewHolder(binding.root) {
        fun onBind(groupMessage: GroupMessage) {
            binding.time.text = groupMessage.time
            binding.msg.text = groupMessage.text
        }
    }

    inner class ReceiveVh(val binding: GroupReceiveLayoutBinding): RecyclerView.ViewHolder(binding.root) {
        fun onBind(groupMessage: GroupMessage) {
            binding.msg.text = groupMessage.text
            binding.senderNickname.text = groupMessage.senderName
            binding.time.text = groupMessage.time
        }
    }

    class MyDiffUtils: DiffUtil.ItemCallback<GroupMessage>() {
        override fun areItemsTheSame(oldItem: GroupMessage, newItem: GroupMessage): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(oldItem: GroupMessage, newItem: GroupMessage): Boolean {
            return oldItem.equals(newItem)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 1) {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.message_send_layout, parent, false)
            return SendVh(MessageSendLayoutBinding.bind(itemView))
        } else {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.group_receive_layout, parent, false)
            return ReceiveVh(GroupReceiveLayoutBinding.bind(itemView))
        }
    }

    override fun getItemViewType(position: Int): Int {
        val msg = getItem(position)
        if (msg.senderUID == currentUser) {
            return 1
        } else {
            return 2
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = getItem(position)
        if (msg.senderUID == currentUser) {
            holder as SendVh
            holder.onBind(msg)
        } else {
            holder as ReceiveVh
            holder.onBind(msg)
        }
    }
}