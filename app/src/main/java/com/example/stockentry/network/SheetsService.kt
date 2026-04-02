package com.example.stockentry.network

import com.example.stockentry.data.StockEntry
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class SheetsService(
    private val webhookUrl: String,
    private val client: OkHttpClient = OkHttpClient()
) {
    fun upload(entries: List<StockEntry>): Boolean {
        if (entries.isEmpty()) return true

        val payload = JSONObject().apply {
            put("entries", JSONArray().apply {
                entries.forEach { entry ->
                    put(JSONObject().apply {
                        put("id", entry.id)
                        put("productType", entry.productType)
                        put("rate", entry.rate)
                        put("quantity", entry.quantity)
                        put("createdAt", entry.createdAt)
                    })
                }
            })
        }

        val request = Request.Builder()
            .url(webhookUrl)
            .post(payload.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            return response.isSuccessful
        }
    }
}
