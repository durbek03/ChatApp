package com.example.chatapp.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.databinding.AddUserItemBinding
import com.example.chatapp.models.User
import com.squareup.picasso.Picasso

class AddUserRvAdapter(val list: ArrayList<User>, val clicker: SelectListener): RecyclerView.Adapter<AddUserRvAdapter.Vh>() {
    private val TAG = "AddUserRvAdapter"
    inner class Vh(val binding: AddUserItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun onBind(user: User) {

            binding.avatar.clipToOutline = true
            if (user.profileImg != null) {
                Log.d(TAG, "onBind: notnull")
                Picasso.get().load(user.profileImg).into(binding.avatar)
            } else {
                binding.avatar.setImageResource(R.drawable.user)
            }

            binding.nickname.text = user.nickName
            binding.root.setOnClickListener {
                clicker.onSelected(user, binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.add_user_item, parent, false)
        return Vh(AddUserItemBinding.bind(itemView))
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface SelectListener {
        fun onSelected(user: User, rvBinder: AddUserItemBinding)
    }
}