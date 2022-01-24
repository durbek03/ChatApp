package com.example.chatapp

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.example.chatapp.databinding.FragmentAddGroupBinding
import com.example.chatapp.models.Group
import com.example.chatapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AddGroupFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddGroupFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var binding: FragmentAddGroupBinding
    lateinit var database: FirebaseDatabase
    lateinit var auth: FirebaseAuth
    lateinit var currentUser: FirebaseUser
    lateinit var firebaseMessaging: FirebaseMessaging
    var curUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        firebaseMessaging = FirebaseMessaging.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        (activity as AppCompatActivity).supportActionBar?.hide()
        val root = inflater.inflate(R.layout.fragment_add_group, container, false)
        binding = FragmentAddGroupBinding.bind(root)
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser!!
        getUser()

        binding.icReturn.setOnClickListener { findNavController().popBackStack() }

        binding.createBtn.setOnClickListener {
            val groupname = binding.groupname.editText?.text.toString().trim()
            val bio = binding.bio.text.toString().trim()

            if (groupname.isEmpty()) {
                Toast.makeText(context, "Group at least should have a name", Toast.LENGTH_SHORT).show()
                binding.groupname.error = "Give group a name"
                binding.groupname.isErrorEnabled = true
                return@setOnClickListener
            }
            binding.groupname.isErrorEnabled = false

            val dialog = AlertDialog.Builder(requireContext()).create()
            dialog.setTitle("Do you want to create this group?")
            dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes") {
                a, b ->
                val reference = database.getReference("groups")
                val key = reference.push().key
                val group = Group(key, groupname, bio, null)
                reference.child(key!!).setValue(group)

                val userGroupReference = database.getReference("users/${currentUser.uid}/groups")
                userGroupReference.child(key).setValue(key)

                val usersRef = database.getReference("groups/$key/users")
                usersRef.child(currentUser.uid).setValue(curUser)

                dialog.dismiss()
                findNavController().popBackStack()
            }
            dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No") {
                a, b ->
                dialog.dismiss()
            }
            dialog.show()
        }

        binding.avatar.setOnClickListener {
            Toast.makeText(context, "Action", Toast.LENGTH_SHORT).show()
        }
        return root
    }

    private fun getUser() {
        val reference = database.getReference("users/${currentUser.uid}")
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                curUser = snapshot.getValue(User::class.java)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun onDestroy() {
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
         * @return A new instance of fragment AddGroupFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddGroupFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}