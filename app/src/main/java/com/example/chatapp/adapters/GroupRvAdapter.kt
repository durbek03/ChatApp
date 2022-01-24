package com.example.chatapp.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.databinding.GroupRvItemBinding
import com.example.chatapp.models.Group
import com.example.chatapp.models.GroupMessage
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class GroupRvAdapter(val database: FirebaseDatabase, val clicker: GroupClick): ListAdapter<Group, GroupRvAdapter.Vh>(MyDiffUtils()) {

    inner class Vh(val binding: GroupRvItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun onBind(group: Group) {
            binding.chatName.text = group.groupName

            val messages = database.getReference("groups/messages")
            messages.addListenerForSingleValueEvent(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    val children = snapshot.children
                    try {
                        val last = children.last()
                        val msg = last.getValue(GroupMessage::class.java)

                        if (msg != null) {
                            binding.chatLastMsg.text = msg.text
                            binding.time.text = msg.time
                            binding.chatLastSender.text = msg.senderName + ":  "
                        } else {
                            binding.chatLastMsg.text = ""
                            binding.chatLastSender.text = ""
                            binding.time.text = ""
                        }
                    } catch (e: Exception) {
                        binding.chatLastMsg.text = ""
                        binding.chatLastSender.text = ""
                        binding.time.text = ""
                        e.printStackTrace()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

            binding.root.setOnClickListener { clicker.onClick(group) }
        }
    }

    class MyDiffUtils(): DiffUtil.ItemCallback<Group>() {
        override fun areItemsTheSame(oldItem: Group, newItem: Group): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(oldItem: Group, newItem: Group): Boolean {
            return oldItem.equals(newItem)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.group_rv_item, parent, false)
        return Vh(GroupRvItemBinding.bind(itemView))
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.onBind(getItem(position))
    }

    interface GroupClick {
        fun onClick(group: Group)
    }
}