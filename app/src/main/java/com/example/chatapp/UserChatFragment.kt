package com.example.chatapp

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.chatapp.adapters.PrivateChatRvAdapter
import com.example.chatapp.databinding.FragmentUserChatBinding
import com.example.chatapp.models.Message
import com.example.chatapp.models.User
import com.example.chatapp.notification.Data
import com.example.chatapp.notification.MyResponse
import com.example.chatapp.notification.Sender
import com.example.chatapp.retrofit.Common
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [UserChatFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UserChatFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var binding: FragmentUserChatBinding
    lateinit var database: FirebaseDatabase
    lateinit var auth: FirebaseAuth
    lateinit var currentUser: FirebaseUser
    lateinit var rvAdapter: PrivateChatRvAdapter
    lateinit var args: User
    lateinit var msgReference: DatabaseReference
    lateinit var msgValueEventListener: ValueEventListener
    lateinit var statusReference: DatabaseReference
    lateinit var statusValueEventListener: ValueEventListener
    val retrofit = Common.retrofitService
    private val TAG = "UserChatFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as AppCompatActivity).supportActionBar?.hide()
        args = arguments?.getSerializable("user") as User
        Log.d(TAG, "onCreateView: ${args.nickName}")
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_user_chat, container, false)
        binding = FragmentUserChatBinding.bind(root)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        currentUser = auth.currentUser!!
        partnerStatusListener()
        binding.avatar.clipToOutline = true
        if (args.profileImg != null) {
            binding.avatar.imageTintList = null
            Picasso.get().load(args.profileImg).into(binding.avatar)
        }
        rvAdapter = PrivateChatRvAdapter(currentUser.uid)
        binding.rv.adapter = rvAdapter

        msgReference = database.getReference("users/${currentUser.uid}/messages/${args.uid}")

        val msgList = arrayListOf<Message>()
        msgValueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                msgList.clear()
                val messages = snapshot.children
                for (i in messages) {
                    val msg = i.getValue(Message::class.java)
                    if (msg != null) {
                        msgList.add(msg)
                        rvAdapter.submitList(msgList.toList())
                        binding.rv.smoothScrollToPosition(50)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }
        msgReference.addValueEventListener(msgValueEventListener)

        binding.icReturn.setOnClickListener { findNavController().popBackStack() }
        binding.nickname.text = args.nickName
        binding.icSend.setOnClickListener {
            val text = binding.message.text.toString().trim()
            if (text.isEmpty()) {
                return@setOnClickListener
            }

            val reference1 = database.getReference("users/${currentUser.uid}/messages/${args.uid}")
            val msgID1 = reference1.push().key
            val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            val message = Message(msgID1, currentUser.uid, args.uid, text, time)
            reference1.child(msgID1!!).setValue(message)

            val reference2 = database.getReference("users/${args.uid}/messages/${currentUser.uid}")
            reference2.child(msgID1).setValue(message)

            retrofit.sendNotification(Sender(Data(currentUser.uid, R.drawable.user,
                text, "New Message", "private", args.uid, "${currentUser.displayName}"), args.token))
                .enqueue(object : Callback<MyResponse> {
                    override fun onResponse(
                        call: Call<MyResponse>,
                        response: Response<MyResponse>
                    ) {
                        Log.d(TAG, "onResponse: Notification sent")
                    }

                    override fun onFailure(call: Call<MyResponse>, t: Throwable) {
                        Log.d(TAG, "onFailure: notification not sent")
                    }

                })

            binding.message.setText("")
        }
        binding.avatar.setOnClickListener {
            findNavController().navigate(
                R.id.viewUserFragment,
                Bundle().apply { putSerializable("user", args) }, NavOptions.getNavOptions().build()
            )
        }

        return root
    }

    fun partnerStatusListener() {
        statusReference = database.getReference("users/${args.uid}/online")
        statusValueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.value as Boolean

                if (status) {
                    binding.onlineIndicator.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.secondaryColor))
                    binding.status.text = "online"
                } else {
                    binding.onlineIndicator.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.grey))
                    binding.status.text = "last seen recently"
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }
        statusReference.addValueEventListener(statusValueEventListener)
    }

    override fun onDestroy() {
        msgReference.removeEventListener(msgValueEventListener)
        statusReference.removeEventListener(statusValueEventListener)
        (activity as AppCompatActivity).supportActionBar?.show()
        super.onDestroy()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment UserChatFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UserChatFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}