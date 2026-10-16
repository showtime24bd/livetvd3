package com.example.network

import com.example.model.Channel
import com.example.model.EpgResponse
import com.example.model.FeaturedSportsEvent
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface ChannelApiService {
    @GET("api/channels")
    suspend fun getChannels(): List<Channel>

    @GET("api/epg/{channel_id}")
    suspend fun getEpg(@Path("channel_id") channelId: Int): EpgResponse
}

object NetworkClient {
    private const val BASE_URL = "https://streamingzone.up.railway.app/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val apiService: ChannelApiService = retrofit.create(ChannelApiService::class.java)
}
