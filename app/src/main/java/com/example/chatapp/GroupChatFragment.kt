package com.example.chatapp

import android.os.Build
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.example.chatapp.adapters.GroupChatRvAdaper
import com.example.chatapp.adapters.GroupRvAdapter
import com.example.chatapp.databinding.FragmentGroupChatBinding
import com.example.chatapp.models.Group
import com.example.chatapp.models.GroupMessage
import com.example.chatapp.models.User
import com.example.chatapp.notification.Data
import com.example.chatapp.notification.MyResponse
import com.example.chatapp.notification.Sender
import com.example.chatapp.retrofit.Common
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GroupChatFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GroupChatFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var binding: FragmentGroupChatBinding
    lateinit var database: FirebaseDatabase
    lateinit var auth: FirebaseAuth
    lateinit var args: Group
    lateinit var messages: ArrayList<GroupMessage>
    lateinit var rvAdapter: GroupChatRvAdaper
    lateinit var currentUser: FirebaseUser
    lateinit var msgReference: DatabaseReference
    lateinit var msgValueEventListener: ValueEventListener
    lateinit var userReference: DatabaseReference
    lateinit var userValueEventListener: ValueEventListener
    var curUser: User? = null
    val retrofit = Common.retrofitService
    val allUsers = arrayListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        allUsers.clear()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as AppCompatActivity).supportActionBar?.hide()
        args = arguments?.getSerializable("group") as Group
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_group_chat, container, false)
        binding = FragmentGroupChatBinding.bind(root)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        currentUser = auth.currentUser!!
        getCurUser()
        getAllUsers()

        binding.groupname.text = args.groupName
        binding.icReturn.setOnClickListener {
            findNavController().popBackStack()
        }
        rvAdapter = GroupChatRvAdaper(auth.currentUser?.uid!!)
        binding.rv.adapter = rvAdapter
        messages = arrayListOf()
        msgReference = database.getReference("groups/${args.uid}/messages")
        msgValueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messages.clear()
                val children = snapshot.children
                for (i in children) {
                    val msg = i.getValue(GroupMessage::class.java)
                    if (msg != null) {
                        messages.add(msg)
                        rvAdapter.submitList(messages.toList())
                        binding.rv.smoothScrollToPosition(50)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }
        msgReference.addValueEventListener(msgValueEventListener)

        binding.avatar.setOnClickListener {
            findNavController().navigate(
                R.id.viewGroupFragment,
                Bundle().apply { putSerializable("group", args) },
                NavOptions.getNavOptions().build()
            )
        }

        val popupMenu = PopupMenu(requireContext(), binding.icMore)
        popupMenu.inflate(R.menu.more_menu)
        popupMenu.setOnMenuItemClickListener {
            val userGroupsRef = database.getReference("users/${currentUser.uid}/groups/${args.uid}")
            userGroupsRef.removeValue()
            val groupUserRef = database.getReference("groups/${args.uid}/users/${curUser?.uid}")
            groupUserRef.removeValue()
            findNavController().popBackStack()
            true
        }
        binding.icMore.setOnClickListener {
            popupMenu.show()
        }

        binding.icSend.setOnClickListener {
            val msg = binding.message.text.toString().trim()
            if (msg.isEmpty()) {
                return@setOnClickListener
            }

            val msgRef = database.getReference("groups/${args.uid}/messages")
            val key = msgRef.push().key
            val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            val groupMessage = GroupMessage(key, currentUser.uid, curUser?.nickName, time, msg)
            msgRef.child(key!!).setValue(groupMessage)

            binding.message.setText("")

            for (i in allUsers) {
                retrofit.sendNotification(Sender(Data(args.uid, R.drawable.user, msg, args.groupName, "group", curUser?.uid, args.groupName), i.token))
                    .enqueue(object : Callback<MyResponse> {
                        override fun onResponse(
                            call: Call<MyResponse>,
                            response: Response<MyResponse>
                        ) {

                        }

                        override fun onFailure(call: Call<MyResponse>, t: Throwable) {
                        }
                    })
            }
        }

        return root
    }

    fun getAllUsers() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        database.getReference("groups/${args.uid}/users")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val children = snapshot.children
                    for (i in children) {
                        val value = i.getValue(User::class.java)
                        if (value!!.uid != currentUser?.uid) {
                            allUsers.add(value)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    fun getCurUser() {
        userReference = database.getReference("users/${currentUser.uid}")
        userValueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                curUser = snapshot.getValue(User::class.java)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }
        userReference.addListenerForSingleValueEvent(userValueEventListener)
    }

    override fun onResume() {
        (activity as AppCompatActivity).supportActionBar?.hide()
        super.onResume()
    }

    override fun onDestroy() {
        msgReference.removeEventListener(msgValueEventListener)
        userReference.removeEventListener(userValueEventListener)
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
         * @return A new instance of fragment GroupChatFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            GroupChatFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}