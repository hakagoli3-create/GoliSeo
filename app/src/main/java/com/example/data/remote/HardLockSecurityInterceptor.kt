package com.example.data.remote

import com.example.data.model.HardLockValidator
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import java.io.IOException

/**
 * Hard lock network interceptor.
 * Intercepts all outgoing HTTP requests (POST, PUT, PATCH).
 * If any request body attempts to mutate locked fields (price, slug, categories),
 * it immediately rejects the request with an HTTP 403 Forbidden and Persian error message.
 */
class HardLockSecurityInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val method = request.method.uppercase()

        if (method in listOf("POST", "PUT", "PATCH")) {
            val body = request.body
            if (body != null) {
                val buffer = Buffer()
                body.writeTo(buffer)
                val bodyContent = buffer.readUtf8()

                val violations = HardLockValidator.inspectRawPayload(bodyContent)
                if (violations.isNotEmpty()) {
                    val violationList = violations.joinToString("، ")
                    val errorJson = """
                        {
                            "code": "gliseo_hard_lock_violation",
                            "message": "خطای امنیتی گلیسئو: تلاش برای تغییر فیلدهای مسدود شده ($violationList). تغییر قیمت، اسلاگ و دسته‌بندی غیرقابل دورزدن است.",
                            "data": { "status": 403, "violations": ["${violations.joinToString("\",\"")}"] }
                        }
                    """.trimIndent()

                    return Response.Builder()
                        .code(403)
                        .message("Blocked by Gliseo Hard Lock Policy")
                        .protocol(Protocol.HTTP_1_1)
                        .request(request)
                        .body(errorJson.toResponseBody("application/json".toMediaType()))
                        .build()
                }
            }
        }

        return chain.proceed(request)
    }
}
