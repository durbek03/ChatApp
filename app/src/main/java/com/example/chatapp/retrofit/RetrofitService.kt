package com.example.chatapp.retrofit

import com.example.chatapp.notification.MyResponse
import com.example.chatapp.notification.Sender
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface RetrofitService {

    @Headers(
        "Content-type:application/json",
        "Authorization:key=AAAAAxHNlR4:APA91bEyxafbCpHdpEXSqXgM4M4gMK4oMM_oMROmG46CPh0S819BPZQJ5LHop0qPclTVF0yEYpB7q9nVAyzKa6uoCCJNnoEJ7yhUfoMTcNNKM383Nyv-aNBo2X-melLFBQRgKlRhA4I8"
    )
    @POST("fcm/send")
    fun sendNotification(@Body sender: Sender): Call<MyResponse>
}