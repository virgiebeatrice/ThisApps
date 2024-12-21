package com.example.thisapp

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.FirebaseMessaging

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Handle the received message
        remoteMessage.data.isNotEmpty().let {
            // Process the message data
        }
        remoteMessage.notification?.let {
            // Process notification
        }
    }

    override fun onNewToken(token: String) {
        // Log the new token
        Log.d("FCM", "New token: $token")

        // Optional: Send the token to your server
        sendTokenToServer(token)

        // Retrieve token explicitly, if needed
        getFCMToken()
    }

    private fun sendTokenToServer(token: String) {
        // Your logic to send the token to the server
        Log.d("FCM", "Sending token to server: $token")
    }

    private fun getFCMToken() {
        // Retrieve the FCM token manually if needed
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM", "Retrieved token: $token")
            } else {
                Log.e("FCM", "Failed to get FCM token", task.exception)
            }
        }
    }
}
