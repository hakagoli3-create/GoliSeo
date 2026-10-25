package com.example.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface WordPressApi {
    @GET("wp-json/")
    suspend fun getSiteInfo(
        @Header("Authorization") authHeader: String? = null
    ): retrofit2.Response<ResponseBody>

    @GET("wp-json/wp/v2/users/me")
    suspend fun getUserMe(
        @Header("Authorization") authHeader: String? = null
    ): retrofit2.Response<ResponseBody>

    @GET("wp-json/wp/v2/posts")
    suspend fun getPosts(
        @Query("per_page") perPage: Int = 20,
        @Query("page") page: Int = 1,
        @Query("status") status: String? = null,
        @Header("Authorization") authHeader: String? = null
    ): retrofit2.Response<ResponseBody>

    @POST("wp-json/wp/v2/posts")
    suspend fun createPost(
        @Body body: RequestBody,
        @Header("Authorization") authHeader: String? = null
    ): retrofit2.Response<ResponseBody>

    @POST("wp-json/wp/v2/posts/{id}")
    suspend fun updatePost(
        @Path("id") id: Long,
        @Body body: RequestBody,
        @Header("Authorization") authHeader: String? = null
    ): retrofit2.Response<ResponseBody>

    @GET("wp-json/wp/v2/pages")
    suspend fun getPages(
        @Query("per_page") perPage: Int = 20,
        @Query("page") page: Int = 1,
        @Query("status") status: String? = null,
        @Header("Authorization") authHeader: String? = null
    ): retrofit2.Response<ResponseBody>

    @POST("wp-json/wp/v2/pages")
    suspend fun createPage(
        @Body body: RequestBody,
        @Header("Authorization") authHeader: String? = null
    ): retrofit2.Response<ResponseBody>

    @POST("wp-json/wp/v2/pages/{id}")
    suspend fun updatePage(
        @Path("id") id: Long,
        @Body body: RequestBody,
        @Header("Authorization") authHeader: String? = null
    ): retrofit2.Response<ResponseBody>

    @GET("wp-json/wp/v2/product")
    suspend fun getCustomPostProducts(
        @Query("per_page") perPage: Int = 20,
        @Query("page") page: Int = 1,
        @Header("Authorization") authHeader: String? = null
    ): retrofit2.Response<ResponseBody>

    @GET("wp-json/wc/store/v1/products")
    suspend fun getStoreProducts(
        @Query("per_page") perPage: Int = 20,
        @Query("page") page: Int = 1
    ): retrofit2.Response<ResponseBody>
}

interface WooCommerceApi {
    @GET("wp-json/wc/v3/system_status")
    suspend fun getSystemStatus(
        @Header("Authorization") authHeader: String? = null
    ): retrofit2.Response<ResponseBody>

    @GET("wp-json/wc/v3/products")
    suspend fun getProducts(
        @Query("per_page") perPage: Int = 20,
        @Query("page") page: Int = 1,
        @Query("status") status: String? = null,
        @Header("Authorization") authHeader: String? = null
    ): retrofit2.Response<ResponseBody>

    @POST("wp-json/wc/v3/products")
    suspend fun createProduct(
        @Body body: RequestBody,
        @Header("Authorization") authHeader: String? = null
    ): retrofit2.Response<ResponseBody>

    @PUT("wp-json/wc/v3/products/{id}")
    suspend fun updateProduct(
        @Path("id") id: Long,
        @Body body: RequestBody,
        @Header("Authorization") authHeader: String? = null
    ): retrofit2.Response<ResponseBody>
}

class WordPressJsonSanitizerInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestWithAgent = originalRequest.newBuilder()
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Gliseo-App) AppleWebKit/537.36 Chrome/120.0 Mobile Safari/537.36")
            .header("Accept", "application/json, text/plain, */*")
            .build()

        val response = chain.proceed(requestWithAgent)
        val body = response.body
        if (body != null) {
            val contentType = response.header("Content-Type") ?: ""
            if (originalRequest.url.encodedPath.contains("wp-json") || contentType.contains("json")) {
                val raw = body.string()
                val cleaned = cleanJsonResponse(raw)
                val newBody = cleaned.toResponseBody(body.contentType())
                return response.newBuilder().body(newBody).build()
            }
        }
        return response
    }

    companion object {
        fun cleanJsonResponse(raw: String): String {
            val trimmed = raw.trim()
            val sb = trimmed.indexOf('[')
            val so = trimmed.indexOf('{')
            val start = when {
                sb != -1 && so != -1 -> minOf(sb, so)
                sb != -1 -> sb
                so != -1 -> so
                else -> -1
            }
            val eb = trimmed.lastIndexOf(']')
            val eo = trimmed.lastIndexOf('}')
            val end = maxOf(eb, eo)
            if (start != -1 && end != -1 && end > start) {
                return trimmed.substring(start, end + 1)
            }
            return trimmed
        }
    }
}

object WordPressAuthHelper {
    suspend fun autoProvisionAppPassword(
        siteUrl: String,
        username: String,
        rawPassword: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            val cleanBase = if (siteUrl.endsWith("/")) siteUrl else "$siteUrl/"
            val cookieStore = mutableMapOf<String, String>()

            val cookieClient = OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .addInterceptor { chain ->
                    val reqBuilder = chain.request().newBuilder()
                    if (cookieStore.isNotEmpty()) {
                        val cookieHeader = cookieStore.entries.joinToString("; ") { "${it.key}=${it.value}" }
                        reqBuilder.header("Cookie", cookieHeader)
                    }
                    reqBuilder.header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Gliseo-App)")
                    val resp = chain.proceed(reqBuilder.build())
                    resp.headers("Set-Cookie").forEach { cookieStr ->
                        val parts = cookieStr.split(";")[0].split("=", limit = 2)
                        if (parts.size == 2) {
                            cookieStore[parts[0].trim()] = parts[1].trim()
                        }
                    }
                    resp
                }
                .build()

            // 1. Post to wp-login.php
            val formBody = FormBody.Builder()
                .add("log", username)
                .add("pwd", rawPassword)
                .add("wp-submit", "ورود")
                .add("redirect_to", "${cleanBase}wp-admin/")
                .build()

            val loginReq = Request.Builder()
                .url("${cleanBase}wp-login.php")
                .post(formBody)
                .build()

            val loginResp = cookieClient.newCall(loginReq).execute()
            if (!loginResp.isSuccessful && loginResp.code !in 200..302) return@withContext null

            // 2. Fetch wp-admin to get REST nonce
            val adminReq = Request.Builder()
                .url("${cleanBase}wp-admin/")
                .get()
                .build()
            val adminResp = cookieClient.newCall(adminReq).execute()
            val adminHtml = adminResp.body?.string() ?: ""

            val nonceRegex = Regex("""wpApiSettings\s*=\s*\{.*?"nonce"\s*:\s*"([^"]+)"""")
            val fallbackNonceRegex = Regex(""""nonce"\s*:\s*"([a-zA-Z0-9]+)"""")
            val nonce = nonceRegex.find(adminHtml)?.groupValues?.get(1)
                ?: fallbackNonceRegex.find(adminHtml)?.groupValues?.get(1)
                ?: ""

            if (nonce.isBlank()) return@withContext null

            // 3. Create Application Password via REST API
            val appPassPayload = JSONObject().apply {
                put("name", "GliseoApp-${System.currentTimeMillis() % 10000}")
            }.toString()

            val appPassReq = Request.Builder()
                .url("${cleanBase}wp-json/wp/v2/users/me/application-passwords")
                .header("X-WP-Nonce", nonce)
                .header("Content-Type", "application/json")
                .post(appPassPayload.toRequestBody("application/json".toMediaTypeOrNull()))
                .build()

            val appPassResp = cookieClient.newCall(appPassReq).execute()
            if (appPassResp.isSuccessful) {
                val respStr = appPassResp.body?.string() ?: ""
                val cleanResp = WordPressJsonSanitizerInterceptor.cleanJsonResponse(respStr)
                val json = JSONObject(cleanResp)
                val createdPass = json.optString("password", "")
                if (createdPass.isNotBlank()) {
                    return@withContext createdPass
                }
            }
        } catch (e: Exception) {
            // Error handling or fallback
        }
        null
    }
}

object NetworkClientProvider {
    fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(WordPressJsonSanitizerInterceptor())
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
