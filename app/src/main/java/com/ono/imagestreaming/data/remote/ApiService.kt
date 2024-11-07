package com.ono.imagestreaming.data.remote

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @Multipart
    @POST("https://file.io")
    suspend fun uploadImage(@Part image: MultipartBody.Part): Response<Void>
}
