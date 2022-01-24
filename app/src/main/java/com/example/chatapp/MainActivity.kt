package com.example.chatapp

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavHostController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.example.chatapp.databinding.ActivityMainBinding
import com.example.chatapp.models.Group
import com.example.chatapp.models.User
import com.example.chatapp.utils.ToolbarService
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity(), ToolbarService {
    lateinit var binding: ActivityMainBinding
    lateinit var reference: DatabaseReference
    lateinit var auth: FirebaseAuth
    lateinit var database: FirebaseDatabase
    lateinit var currentUser: FirebaseUser
    lateinit var firebaseMessaging: FirebaseMessaging
    private val TAG = "MainActivity"

    @SuppressLint("StringFormatInvalid")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser!!

        database = FirebaseDatabase.getInstance()
        firebaseMessaging = FirebaseMessaging.getInstance()

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            database.getReference("users/${currentUser.uid}").child("token")
                .setValue(token)
            // Log and toast
            val msg = getString(R.string.app_name, token)
            Log.d(TAG, "token: $token")
            //  Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
        })

        binding.toolbar.profile.clipToOutline = true
        if (currentUser.photoUrl != null) {
            binding.toolbar.profile.imageTintList = null
            Picasso.get().load(currentUser.photoUrl).into(binding.toolbar.profile)
        }

        setSupportActionBar(binding.toolbar.root)
        supportActionBar?.title = "PDP GFramm"
    }

    override fun onNavigateUp(): Boolean {
        return findNavController(R.id.fragmentContainerView).navigateUp()
    }

    override fun getToolbar(): Toolbar {
        return binding.toolbar.root
    }

    override fun onResume() {
        val user = database.getReference("users").child(currentUser.uid)
        user.child("online").setValue(true)

        super.onResume()
    }

    override fun onPause() {
        val user = database.getReference("users").child(currentUser.uid)
        user.child("online").setValue(false)
        super.onPause()
    }
}