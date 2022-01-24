package com.example.chatapp.retrofit

object Common {
    val BASE_URL = "https://fcm.googleapis.com/"

    val retrofitService: RetrofitService
        get() = RetrofitClient.getClient(BASE_URL)!!.create(RetrofitService::class.java)
}