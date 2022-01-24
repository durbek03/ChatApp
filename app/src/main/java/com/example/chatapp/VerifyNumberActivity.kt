package com.example.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.widget.addTextChangedListener
import com.example.chatapp.databinding.ActivityVerifyNumberBinding
import com.example.chatapp.models.User
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.concurrent.TimeUnit

class VerifyNumberActivity : AppCompatActivity() {
    lateinit var binding: ActivityVerifyNumberBinding
    lateinit var countDownTimer: CountDownTimer
    lateinit var auth: FirebaseAuth
    lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    lateinit var credential: PhoneAuthCredential
    lateinit var database: FirebaseDatabase
    var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    var storedVerificationId: String? = null
    var numberFromExtra: String? = null
    private val TAG = "VerifyNumberActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyNumberBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        database = FirebaseDatabase.getInstance()
        numberFromExtra = intent.getStringExtra("number")
        binding.tv1.text = "Confirmation code has been sent to number $numberFromExtra"
        val number = intent.getStringExtra("number")

        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:$credential")
                this@VerifyNumberActivity.credential = credential
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(
                    this@VerifyNumberActivity,
                    "Could not verify this number",
                    Toast.LENGTH_SHORT
                ).show()
                val intent = Intent(this@VerifyNumberActivity, LoginActivity::class.java)
                intent.putExtra("number", numberFromExtra)
                startActivity(intent)
                finish()

                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e)

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                }

                // Show a message and update the UI
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:$verificationId")

                // Save verification ID and resending token so we can use them later
                storedVerificationId = verificationId
                resendToken = token
            }
        }.also { callbacks = it }

        auth = FirebaseAuth.getInstance()
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(number!!)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)

        countDownTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(p0: Long) {
                if (binding.timer.text == "00") {
                    binding.timerPrefix.text = "00:"
                    binding.timer.text = "59"
                } else {
                    val time = binding.timer.text.toString().trim()
                    val newTime = time.toInt() - 1
                    val formatted = String.format("%02d", newTime)
                    binding.timer.text = formatted
                }
            }

            override fun onFinish() {
                binding.timer.text = "00"
            }
        }.start()

        binding.icRefresh.setOnClickListener {
            if (binding.timer.text != "00") {
                Toast.makeText(this, "Wait until time runs out", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            resendToken to PhoneAuthProvider.verifyPhoneNumber(options)
            Toast.makeText(this, "Code has been sent", Toast.LENGTH_SHORT).show()
            binding.timerPrefix.text = "01:"
            countDownTimer.start()
        }
        binding.tvRefresh.setOnClickListener { binding.icRefresh.callOnClick() }

        binding.code.addTextChangedListener {
            val text = binding.code.text.toString().trim()
            if (text.length == 6) {
                val credential = PhoneAuthProvider.getCredential(storedVerificationId!!, text)
                signInWithPhoneAuthCredential(credential)
            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")

                    val user = task.result?.user

                    val reference = database.getReference("users/${user?.uid}")
                    reference.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                val intent = Intent(this@VerifyNumberActivity, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                val userReference = database.getReference("users")
                                val newUser = User(user?.uid, numberFromExtra, null, null, numberFromExtra, null, false, null)
                                userReference.child(newUser.uid!!).setValue(newUser).addOnCompleteListener {
                                    val intent = Intent(this@VerifyNumberActivity, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                    reference.removeEventListener(this)
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }
                    })
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                    // Update UI
                }
            }
    }

    override fun onBackPressed() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.putExtra("number", numberFromExtra)
        startActivity(intent)
        finish()
    }
}