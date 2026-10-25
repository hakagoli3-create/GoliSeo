package com.example.data.remote

import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface WordPressApi {
    @GET("wp-json/")
    suspend fun getSiteInfo(
        @Header("Authorization") authHeader: String? = null
    ): Response<ResponseBody>

    @GET("wp-json/wp/v2/posts")
    suspend fun getPosts(
        @Query("per_page") perPage: Int = 10,
        @Header("Authorization") authHeader: String? = null
    ): Response<ResponseBody>

    @GET("wp-json/wp/v2/pages")
    suspend fun getPages(
        @Query("per_page") perPage: Int = 10,
        @Header("Authorization") authHeader: String? = null
    ): Response<ResponseBody>
}

interface WooCommerceApi {
    @GET("wp-json/wc/v3/system_status")
    suspend fun getSystemStatus(
        @Header("Authorization") authHeader: String? = null
    ): Response<ResponseBody>

    @GET("wp-json/wc/v3/products")
    suspend fun getProducts(
        @Query("per_page") perPage: Int = 10,
        @Header("Authorization") authHeader: String? = null
    ): Response<ResponseBody>
}

object NetworkClientProvider {
    fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HardLockSecurityInterceptor())
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    fun createWordPressApi(baseUrl: String, client: OkHttpClient): WordPressApi {
        val sanitizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return Retrofit.Builder()
            .baseUrl(sanitizedUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(WordPressApi::class.java)
    }

    fun createWooCommerceApi(baseUrl: String, client: OkHttpClient): WooCommerceApi {
        val sanitizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return Retrofit.Builder()
            .baseUrl(sanitizedUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(WooCommerceApi::class.java)
    }

    fun createBasicAuth(user: String, pass: String): String {
        return Credentials.basic(user.trim(), pass.trim())
    }
}
