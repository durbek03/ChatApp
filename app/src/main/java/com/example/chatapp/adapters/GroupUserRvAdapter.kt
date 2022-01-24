package com.example.chatapp.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.databinding.GroupUserItemBinding
import com.example.chatapp.models.User
import com.google.firebase.auth.FirebaseUser
import com.squareup.picasso.Picasso

class GroupUserRvAdapter(val clicker: OnUserClick): ListAdapter<User, GroupUserRvAdapter.Vh>(MyDiffUtils()) {
    inner class Vh(val binding: GroupUserItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun onBind(user: User) {

            binding.avatar.clipToOutline = true
            if (user.profileImg != null) {
                Picasso.get().load(user.profileImg).into(binding.avatar)
            } else {
                binding.avatar.setImageResource(R.drawable.user)
            }

            binding.username.text = user.nickName
            binding.root.setOnClickListener { clicker.onClick(user) }
        }
    }

    class MyDiffUtils: DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.equals(newItem)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.group_user_item, parent, false)
        return Vh(GroupUserItemBinding.bind(itemView))
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.onBind(getItem(position))
    }

    interface OnUserClick {
        fun onClick(user: User)
    }
}