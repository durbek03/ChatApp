package com.example.chatapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.example.chatapp.databinding.ActivityLoginBinding
import com.example.chatapp.models.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*
import java.time.Duration
import java.util.*

class LoginActivity : AppCompatActivity() {
    lateinit var permissionLauncher: ActivityResultLauncher<String>
    lateinit var binding: ActivityLoginBinding
    lateinit var activityForResult: ActivityResultLauncher<Intent>
    lateinit var googleSignInClient: GoogleSignInClient
    lateinit var auth: FirebaseAuth
    lateinit var database: FirebaseDatabase
    val RC_SIGN_IN = 1
    private val TAG = "LoginActivity"

    @SuppressLint("SimpleDateFormat", "ShowToast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()
        binding.icApp.clipToOutline = true
        database = FirebaseDatabase.getInstance()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        val numberFromExtra = intent.getStringExtra("number")
        if (numberFromExtra != null) {
            binding.number.editText?.setText(numberFromExtra)
        }

        activityForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    val account = task.getResult(ApiException::class.java)!!
                    Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                    firebaseAuthWithGoogle(account.idToken!!)

                } catch (e: ApiException) {
                    // Google Sign In failed, update UI appropriately
                    Log.w(TAG, "Google sign in failed", e)
                }

            }

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                if (it) {
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                    ) {
                        val dialog = AlertDialog.Builder(this).create()
                        dialog.setTitle("Please accept permission\nto upload your image")
                        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Ok") { a, b ->
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No") { a, b ->
                            dialog.dismiss()
                        }
                    }
                }
            }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_DENIED
        ) {
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("13183587614-likpm315lbfrk7oki197oise01lkf94m.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.signIn.setOnClickListener {
            val number = binding.number.editText?.text.toString().trim()
            for (i in number) {
                if (i != '+') {
                    try {
                        i.digitToInt()
                    } catch (e: Exception) {
                        binding.number.error = "Number cannot contain letters"
                        return@setOnClickListener
                    }
                }
            }
            if (number.isEmpty()) {
                Toast.makeText(this, "Enter your number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            binding.number.error = null
            binding.number.isErrorEnabled = false

            val intent = Intent(this, VerifyNumberActivity::class.java)
            intent.putExtra("number", number)
            startActivity(intent)
            finish()

        }

        binding.icGoogle.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                signIn()
            }
        }
        binding.tvContinue.setOnClickListener {
            binding.icGoogle.callOnClick()
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        activityForResult.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser

                    val reference = database.getReference("users/${user?.uid}")
                    reference.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                val intent =
                                    Intent(this@LoginActivity, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                val userReference = database.getReference("users")
                                val newUser = User(
                                    user?.uid,
                                    user?.displayName,
                                    user?.photoUrl.toString(),
                                    null,
                                    user?.phoneNumber,
                                    user?.email,
                                    false, null
                                )
                                userReference.child(newUser.uid!!).setValue(newUser)
                                    .addOnCompleteListener {
                                        val intent =
                                            Intent(this@LoginActivity, MainActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }
                    })
                    // updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    //  updateUI(null)
                }
            }
    }
}