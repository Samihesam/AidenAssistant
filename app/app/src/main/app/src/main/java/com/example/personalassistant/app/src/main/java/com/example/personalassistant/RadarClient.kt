package com.example.personalassistant

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

object RadarClient {

    private const val BASE_URL = "https://YOUR_SERVER_ADDRESS.com/api"
    private val client = OkHttpClient()

    data class NearbyUser(val userId: String, val displayName: String, val approxDistance: String)

    suspend fun setVisibility(token: String, visible: Boolean): Boolean = withContext(Dispatchers.IO) {
        val body = JSONObject().put("visible", visible).toString()
            .toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("$BASE_URL/radar/visibility")
            .addHeader("Authorization", "Bearer $token")
            .post(body)
            .build()
        client.newCall(request).execute().isSuccessful
    }

    suspend fun updateLocation(token: String, lat: Double, lng: Double): Boolean = withContext(Dispatchers.IO) {
        val body = JSONObject().put("lat", lat).put("lng", lng).toString()
            .toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("$BASE_URL/radar/location")
            .addHeader("Authorization", "Bearer $token")
            .post(body)
            .build()
        client.newCall(request).execute().isSuccessful
    }

    suspend fun getNearbyUsers(token: String): List<NearbyUser> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$BASE_URL/radar/nearby")
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return@withContext emptyList()

        val json = JSONObject(response.body?.string() ?: "{}")
        val usersArray: JSONArray = json.optJSONArray("users") ?: JSONArray()

        (0 until usersArray.length()).map { i ->
            val obj = usersArray.getJSONObject(i)
            NearbyUser(
                userId = obj.getString("userId"),
                displayName = obj.getString("displayName"),
                approxDistance = obj.getString("approxDistance")
            )
        }
    }

    suspend fun sendChatRequest(token: String, targetUserId: String): Boolean = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$BASE_URL/radar/chat-request/$targetUserId")
            .addHeader("Authorization", "Bearer $token")
            .post("".toRequestBody())
            .build()
        client.newCall(request).execute().isSuccessful
    }

    suspend fun blockUser(token: String, targetUserId: String): Boolean = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$BASE_URL/radar/block/$targetUserId")
            .addHeader("Authorization", "Bearer $token")
            .post("".toRequestBody())
            .build()
        client.newCall(request).execute().isSuccessful
    }

    suspend fun reportUser(token: String, targetUserId: String): Boolean = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$BASE_URL/radar/report/$targetUserId")
            .addHeader("Authorization", "Bearer $token")
            .post("".toRequestBody())
            .build()
        client.newCall(request).execute().isSuccessful
    }
}
