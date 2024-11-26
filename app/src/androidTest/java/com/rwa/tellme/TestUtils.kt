package com.rwa.tellme

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object TestUtils {
    fun loginAndSaveToken(email: String, password: String): String {
        val client = OkHttpClient()
        val body = JSONObject().apply {
            put("email", email)
            put("password", password)
        }.toString()

        val request = Request.Builder()
            .url(BuildConfig.BASE_URL)
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            val token = JSONObject(response.body!!.string()).getString("token")
            saveTokenToPrefs(token)
            return token
        } else {
            throw Exception("Login failed: ${response.message}")
        }
    }

    private fun saveTokenToPrefs(token: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val sharedPreferences = context.getSharedPreferences("session", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("auth_token", token).apply()
    }
}
