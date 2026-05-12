package com.example.nutriai.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class GeminiRepository {

    private val apiKey = " "
    private val apiUrl = "https://api.groq.com/openai/v1/chat/completions"

    suspend fun getRecipes(ingredients: String, dietType: String = "balanced"): String {
        return withContext(Dispatchers.IO) {
            try {
                val prompt = """
                    You are a nutrition expert. Based on these ingredients: $ingredients
                    Suggest exactly 3 recipes suitable for a $dietType diet.
                    
                    IMPORTANT: You MUST start EACH recipe with the marker 'RECIPE_START'.
                    Format each of the 3 recipes exactly as follows:
                    
                    RECIPE_START
                    Title: [Recipe Name]
                    Calories: [Number] kcal
                    Time: [Number] min
                    Instructions: [Brief description]
                    
                    Do not add any introductory or concluding text. Provide exactly 3 recipes.
                """.trimIndent()

                sendRequest(prompt)
            } catch (e: Exception) {
                "Error: ${e.message}"
            }
        }
    }

    suspend fun getMealPlan(dietType: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val prompt = """
                    Create a 7-day meal plan for $dietType diet.
                    For each day provide breakfast, lunch, dinner with approximate calories.
                    Format it clearly day by day.
                    Language: English.
                """.trimIndent()

                sendRequest(prompt)
            } catch (e: Exception) {
                "Error: ${e.message}"
            }
        }
    }

    private fun sendRequest(prompt: String): String {
        val url = URL(apiUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Authorization", "Bearer $apiKey")
        connection.connectTimeout = 15000
        connection.readTimeout = 15000
        connection.doOutput = true
        connection.doInput = true

        val body = JSONObject().apply {
            put("model", "llama-3.1-8b-instant")
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            })
            put("max_tokens", 2048)
            put("temperature", 0.7)
        }

        val outputStream = connection.outputStream
        outputStream.write(body.toString().toByteArray(Charsets.UTF_8))
        outputStream.flush()
        outputStream.close()

        val responseCode = connection.responseCode

        return if (responseCode == HttpURLConnection.HTTP_OK) {
            val response = connection.inputStream.bufferedReader(Charsets.UTF_8).readText()
            val jsonResponse = JSONObject(response)
            jsonResponse
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
        } else {
            val error = connection.errorStream?.bufferedReader(Charsets.UTF_8)?.readText()
            "Error $responseCode: $error"
        }
    }
}