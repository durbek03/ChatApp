package com.example.chatapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import com.example.chatapp.databinding.FragmentViewProfileBinding
import com.example.chatapp.models.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ViewProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ViewProfileFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var binding: FragmentViewProfileBinding
    lateinit var auth: FirebaseAuth
    lateinit var database: FirebaseDatabase
    lateinit var user: User
    lateinit var permissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                if (it) {
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            requireActivity(),
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                    ) {
                        val dialog = AlertDialog.Builder(requireContext()).create()
                        dialog.setTitle("Please accept permission\nto upload your image")
                        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Ok") { a, b ->
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No") { a, b ->
                            dialog.dismiss()
                        }
                    }
                    Toast.makeText(
                        requireContext(),
                        "Cannot upload your image without permission",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_view_profile, container, false)
        binding = FragmentViewProfileBinding.bind(root)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val currentUser = auth.currentUser

        val reference = database.getReference("users/${currentUser?.uid}")

        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                user = snapshot.getValue(User::class.java)!!

                binding.nickname.editText?.setText(user.nickName)
                binding.bio.setText(user.bio)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        binding.avatar.clipToOutline = true
        if (currentUser?.photoUrl != null) {
            Picasso.get().load(currentUser.photoUrl).into(binding.avatar)
        } else {
            binding.avatar.setImageResource(R.drawable.user)
        }
        binding.avatar.setOnClickListener {
            Toast.makeText(requireContext(), "Action", Toast.LENGTH_SHORT).show()
        }

        binding.icAddLayout.setOnClickListener {
            Toast.makeText(requireContext(), "Action", Toast.LENGTH_SHORT).show()
        }

        binding.logOut.setOnClickListener {
            val dialog = AlertDialog.Builder(requireContext()).create()
            dialog.setTitle("Do you want to log out?")
            dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes") { a, b ->
                val client =
                    GoogleSignIn.getClient(requireActivity(), GoogleSignInOptions.DEFAULT_SIGN_IN)
                client.signOut().addOnCompleteListener {
                    auth.signOut()
                    val intent = Intent(requireActivity(), LoginActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }
            }
            dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No") { a, b ->
                dialog.dismiss()
            }
            dialog.show()
        }

        binding.icReturn.setOnClickListener {
            val nickname = binding.nickname.editText?.text.toString().trim()
            val bio = binding.bio.text.toString().trim()

            if (nickname == user.nickName && bio == user.bio) {
                findNavController().popBackStack()
            } else {
                user.nickName = nickname
                user.bio = bio
                val userReference = database.getReference("users")
                userReference.child(user.uid!!).setValue(user)
                findNavController().popBackStack()
            }
        }

        return root
    }


    override fun onAttach(context: Context) {
        (activity as AppCompatActivity).supportActionBar?.hide()
        super.onAttach(context)
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
         * @return A new instance of fragment ViewProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ViewProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}