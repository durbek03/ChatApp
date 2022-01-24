package com.example.chatapp.notification

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.RemoteInput
import com.example.chatapp.R
import com.example.chatapp.models.GroupMessage
import com.example.chatapp.models.Message
import com.example.chatapp.models.User
import com.example.chatapp.retrofit.Common
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class MyService: Service() {
    val KEY_TEXT_REPLY = "key_text_reply"
    private val TAG = "MyService"
    val currentUser = FirebaseAuth.getInstance().currentUser
    val firebaseDatabase = FirebaseDatabase.getInstance()
    val retrofit = Common.retrofitService
    val allUsers = arrayListOf<User>()
    var toUid = ""
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {

            val text = getMessageText(intent)
            val type = intent.getStringExtra("type")
            toUid = intent.getStringExtra("uid")!!
            val senderName = intent.getStringExtra("senderName")
            allUsers.clear()

            firebaseDatabase.getReference("groups/${toUid}/users")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val children = snapshot.children
                        for (i in children) {
                            val value = i.getValue(User::class.java)
                            if (value!!.uid != currentUser?.uid) {
                                allUsers.add(value)
                            }
                        }

                        if (type == "private") {
                            firebaseDatabase.getReference("users/$toUid")
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val toUser = snapshot.getValue(User::class.java)
                                        val reference = firebaseDatabase.getReference("users/$toUid/messages/${currentUser?.uid}")
                                        val key = reference.push().key
                                        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                                        val message = Message(key, currentUser?.uid, toUid, text.toString(), time)
                                        reference.child(key!!).setValue(message)

                                        val reference2 = firebaseDatabase.getReference("users/${currentUser?.uid}/messages/$toUid")
                                        reference2.child(key).setValue(message)

                                        retrofit.sendNotification(Sender(Data(currentUser?.uid, R.drawable.user,
                                            text.toString(), "New Message", "private", toUid, currentUser?.displayName), toUser?.token))
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
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                    }
                                })
                        } else {
                            val msgRef = firebaseDatabase.getReference("groups/${toUid}/messages")
                            val key = msgRef.push().key
                            val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                            val groupMessage = GroupMessage(key, currentUser?.uid, currentUser?.displayName, time, text.toString())
                            msgRef.child(key!!).setValue(groupMessage)

                            for (i in allUsers) {
                                retrofit.sendNotification(Sender(Data(toUid, R.drawable.user, text.toString(), senderName, "group", currentUser?.uid, senderName), i.token))
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

                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private fun getMessageText(intent: Intent): CharSequence? {
        return RemoteInput.getResultsFromIntent(intent)?.getCharSequence(KEY_TEXT_REPLY)
    }
}