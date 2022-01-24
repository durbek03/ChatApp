package com.example.chatapp.viewpagerFragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.chatapp.NavOptions
import com.example.chatapp.R
import com.example.chatapp.adapters.UserRvAdapter
import com.example.chatapp.databinding.FragmentUsersBinding
import com.example.chatapp.databinding.ToolbarBinding
import com.example.chatapp.models.User
import com.example.chatapp.utils.ToolbarService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [UsersFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UsersFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var binding: FragmentUsersBinding
    lateinit var database: FirebaseDatabase
    lateinit var userRvAdapter: UserRvAdapter
    lateinit var toolbarService: ToolbarService
    var currentUser: FirebaseUser? = null
    lateinit var reference: DatabaseReference
    lateinit var valueEventListener: ValueEventListener

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
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_users, container, false)
        binding = FragmentUsersBinding.bind(root)
        val toolbar = toolbarService.getToolbar()
        val toolBinding = ToolbarBinding.bind(toolbar)
        toolBinding.profile.setOnClickListener {
            findNavController().navigate(R.id.profileSettingFragment, null, NavOptions.getNavOptions().build())
        }
        database = FirebaseDatabase.getInstance()
        val auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser

        userRvAdapter = UserRvAdapter(database, currentUser?.uid!!, object : UserRvAdapter.UserClick {
            override fun onClick(user: User) {
                findNavController().navigate(R.id.userChatFragment, Bundle().apply { putSerializable("user", user) }, NavOptions.getNavOptions().build())
            }
        }, requireContext())
        binding.rv.adapter = userRvAdapter
        reference = database.getReference("users")
        val userList = arrayListOf<User>()

        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                val users = snapshot.children
                for (i in users) {
                    val user = i.getValue(User::class.java)
                    if (user != null && user.uid != currentUser?.uid) {
                        userList.add(user)
                        userRvAdapter.submitList(userList.toList())
                    }
                }

            }
            override fun onCancelled(error: DatabaseError) {
            }
        }
        reference.addValueEventListener(valueEventListener)

        return root
    }

    override fun onDestroyView() {
        reference.removeEventListener(valueEventListener)
        super.onDestroyView()
    }

    override fun onAttach(context: Context) {
        toolbarService = context as ToolbarService
        super.onAttach(context)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment UsersFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UsersFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}