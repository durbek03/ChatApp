package com.example.chatapp.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.databinding.RvItemBinding
import com.example.chatapp.models.Message
import com.example.chatapp.models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.lang.Exception
import java.nio.charset.Charset

class UserRvAdapter(val database: FirebaseDatabase, val currentUserUid: String, val clicker: UserClick, val context: Context) :
    ListAdapter<User, UserRvAdapter.Vh>(MyDiffUtils()) {
    inner class Vh(val binding: RvItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(user: User) {
            binding.chatName.text = user.nickName
            binding.avatar.clipToOutline = true
            if (user.profileImg != null) {
                Picasso.get().load(user.profileImg).into(binding.avatar)
            }

            if (user.online) {
                binding.onlineIndicator.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.secondaryColor))
            } else {
                binding.onlineIndicator.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.grey))
            }

            val reference = database.getReference("users/$currentUserUid/messages/${user.uid}")
            reference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val messages = snapshot.children
                    try {
                        val last = messages.last().getValue(Message::class.java)
                        if (last != null) {
                            binding.chatLastMsg.text = last.text
                            binding.time.text = last.time
                        } else {
                            binding.chatLastMsg.text = ""
                            binding.time.text = ""
                        }
                    } catch (e: Exception) {
                        binding.chatLastMsg.text = ""
                        binding.time.text = ""
                        e.printStackTrace()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
            binding.root.setOnClickListener { clicker.onClick(user) }
        }
    }

    class MyDiffUtils : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.equals(newItem)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.rv_item, parent, false)
        return Vh(RvItemBinding.bind(itemView))
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.onBind(getItem(position))
    }

    interface UserClick {
        fun onClick(user: User)
    }
}