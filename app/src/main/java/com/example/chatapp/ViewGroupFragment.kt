package com.example.chatapp

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.example.chatapp.adapters.AddUserRvAdapter
import com.example.chatapp.adapters.GroupUserRvAdapter
import com.example.chatapp.databinding.AddUserItemBinding
import com.example.chatapp.databinding.FragmentViewGroupBinding
import com.example.chatapp.databinding.UserlistDialogLayoutBinding
import com.example.chatapp.models.Group
import com.example.chatapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ViewGroupFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ViewGroupFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var database: FirebaseDatabase
    lateinit var binding: FragmentViewGroupBinding
    lateinit var userList: ArrayList<User>
    lateinit var args: Group
    lateinit var auth: FirebaseAuth
    lateinit var currentUser: FirebaseUser
    lateinit var rvAdapter: GroupUserRvAdapter
    lateinit var userReference: DatabaseReference
    lateinit var userValueEventListener: ValueEventListener

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
        args = arguments?.getSerializable("group") as Group
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_view_group, container, false)
        binding = FragmentViewGroupBinding.bind(root)
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser!!
        binding.groupname.text = args.groupName
        binding.bio.text = args.bio

        userList = arrayListOf()
        rvAdapter = GroupUserRvAdapter(object : GroupUserRvAdapter.OnUserClick {
            override fun onClick(user: User) {
                if (user.uid == currentUser.uid) {
                    findNavController().navigate(R.id.profileSettingFragment, null, NavOptions.getNavOptions().build())
                } else {
                    findNavController().navigate(
                        R.id.viewUserFragment,
                        Bundle().apply { putSerializable("user", user) }, NavOptions.getNavOptions().build())
                }
            }
        })
        binding.rv.adapter = rvAdapter
        userReference = database.getReference("groups/${args.uid}/users")
        userValueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                val users = snapshot.children
                for (i in users) {
                    val user = i.getValue(User::class.java)
                    if (user != null) userList.add(user)
                    rvAdapter.submitList(userList.toList())
                }
                binding.membersAmount.text = "Members: ${userList.size}"
                binding.membersAmount2.text = "${userList.size} MEMBERS"
                if (userList.isEmpty()) {
                    val reference = database.getReference("groups/${args.uid}")
                    reference.removeValue()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }
        userReference.addValueEventListener(userValueEventListener)

        binding.icAddUser.setOnClickListener {
            val dialog = AlertDialog.Builder(requireContext()).create()
            val view = layoutInflater.inflate(R.layout.userlist_dialog_layout, null, false)

            val binder = UserlistDialogLayoutBinding.bind(view)

            val userList = arrayListOf<User>()
            val selectedUserList = arrayListOf<User>()
            val reference = database.getReference("users")
            reference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    userList.clear()
                    selectedUserList.clear()
                    val children = snapshot.children
                    for (i in children) {
                        val user = i.getValue(User::class.java)
                        if (user?.uid != currentUser.uid && user != null) {
                            userList.add(user)
                        }
                    }
                    val selectRvAdapter = AddUserRvAdapter(userList, object : AddUserRvAdapter.SelectListener {
                        override fun onSelected(user: User, rvBinder: AddUserItemBinding) {
                            if (user in selectedUserList) {
                                selectedUserList.remove(user)
                                rvBinder.icTick.visibility = View.GONE
                            } else {
                                selectedUserList.add(user)
                                rvBinder.icTick.visibility = View.VISIBLE
                            }
                        }
                    })
                    binder.rv.adapter = selectRvAdapter
                    dialog.setView(binder.root)
                    dialog.show()
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
            binder.dismissBtn.setOnClickListener {dialog.dismiss()}
            binder.addBtn.setOnClickListener {
                for (i in selectedUserList) {
                    val reference1 = database.getReference("users/${i.uid}/groups")
                    reference1.child(args.uid!!).setValue(args.uid)

                    val reference2 = database.getReference("groups/${args.uid}/users")
                    reference2.child(i.uid!!).setValue(i)
                }
                dialog.dismiss()
            }
        }

        binding.icReturnLayout.setOnClickListener { findNavController().popBackStack() }

        return root
    }

    override fun onDestroy() {
        userReference.removeEventListener(userValueEventListener)
        super.onDestroy()
    }

    override fun onResume() {
        (activity as AppCompatActivity).supportActionBar?.hide()
        super.onResume()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ViewGroupFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ViewGroupFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}