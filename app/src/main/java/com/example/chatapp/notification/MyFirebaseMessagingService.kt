package com.example.chatapp.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import androidx.navigation.NavDeepLinkBuilder
import com.example.chatapp.MainActivity
import com.example.chatapp.R
import com.example.chatapp.models.Group
import com.example.chatapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val TAG = "MyFirebaseMessagingServ"
    val CHANNEL_ID = "1"
    override fun onMessageReceived(msg: RemoteMessage) {
        createNotification(msg)
    }

    fun createNotification(msg: RemoteMessage) {
        createNotificationChannel()
        val firebaseDatabase = FirebaseDatabase.getInstance()

        var notId = ""
        val text = msg.data["body"]
        val senderName = msg.data["sentedName"]
        val fromUid = msg.data["user"]
        Log.d(TAG, "createNotification: fromuser: $fromUid")
        val type = msg.data["type"]
        Log.d(TAG, "createNotification: type: $type")
        for (i in fromUid!!) {
            try {
                val digitToInt = i.digitToInt()
                notId += digitToInt
            } catch (e: Exception) {
            }
        }

        val KEY_TEXT_REPLY = "key_text_reply"
        val remoteInput: RemoteInput = RemoteInput.Builder(KEY_TEXT_REPLY).run {
            setLabel("Reply...")
            build()
        }

        val intent = Intent(this, MyService::class.java)
        intent.action = "ACTION_REPLY"
        intent.putExtra("type", type)
        intent.putExtra("uid", fromUid)
        intent.putExtra("senderName", senderName)

        val replyPendingIntent: PendingIntent =
            PendingIntent.getService(
                this,
                notId.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

        val action: NotificationCompat.Action =
            NotificationCompat.Action.Builder(
                R.drawable.user,
                "Reply...", replyPendingIntent
            )
                .addRemoteInput(remoteInput)
                .build()

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.user)
            .addAction(action)
            .setContentTitle(senderName)
            .setContentText(text)
            .setAutoCancel(true)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(text)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (type == "private") {
            firebaseDatabase.getReference("users/$fromUid")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val user = snapshot.getValue(User::class.java)
                        val bundle = Bundle().apply {
                            putSerializable("user", user)
                        }
                        val pendingIntent = NavDeepLinkBuilder(this@MyFirebaseMessagingService)
                            .setComponentName(MainActivity::class.java)
                            .setGraph(R.navigation.navigation)
                            .setDestination(R.id.userChatFragment)
                            .setArguments(bundle)
                            .createPendingIntent()
                        builder.setContentIntent(pendingIntent)
                        notificationManager.notify(notId.toInt(), builder.build())
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
        } else {
            firebaseDatabase.getReference("groups/$fromUid")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val group = snapshot.getValue(Group::class.java)
                        val bundle = Bundle().apply {
                            putSerializable("group", group)
                        }
                        val pendingIntent = NavDeepLinkBuilder(this@MyFirebaseMessagingService)
                            .setComponentName(MainActivity::class.java)
                            .setGraph(R.navigation.navigation)
                            .setDestination(R.id.groupChatFragment)
                            .setArguments(bundle)
                            .createPendingIntent()
                        builder.setContentIntent(pendingIntent)
                        notificationManager.notify(notId.toInt(), builder.build())
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
        }

    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.app_name)
            val descriptionText = getString(R.string.app_name)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}