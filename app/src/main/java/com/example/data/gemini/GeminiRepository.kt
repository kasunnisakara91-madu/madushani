package com.example.data.gemini

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiRepository(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()
) {

    companion object {
        private const val TAG = "GeminiRepository"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"

        fun getBuildConfigApiKey(): String {
            return try {
                val field = BuildConfig::class.java.getField("GEMINI_API_KEY")
                field.get(null) as? String ?: ""
            } catch (_: Throwable) {
                ""
            }
        }
    }

    suspend fun askJarvis(
        userPrompt: String,
        languagePreference: String,
        customKey: String = "",
        model: String = "gemini-2.5-flash"
    ): String = withContext(Dispatchers.IO) {
        val buildConfigKey = getBuildConfigApiKey()
        val apiKey = when {
            customKey.isNotBlank() -> customKey
            buildConfigKey.isNotBlank() -> buildConfigKey
            else -> ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "No valid Gemini API key configured. Utilizing local Jarvis intelligence.")
            return@withContext getOfflineJarvisResponse(userPrompt, languagePreference)
        }

        try {
            val systemInstruction = """
                You are MADU JARVIS AI, an advanced, highly intelligent voice AI assistant for Android.
                Your persona: loyal, polite, concise, and ultra-futuristic.
                Languages: You fluently understand Sinhala (සිංහල), English, and Sinhala+English mixed commands.
                Instructions:
                1. Always keep spoken answers short, natural, and crisp (1 to 3 sentences maximum), suitable for Android Text-To-Speech.
                2. If the user addresses you or speaks in Sinhala, respond warmly and clearly in natural Sinhala.
                3. If the user asks in English, respond in polished, crisp English.
                4. If the user mixes Sinhala and English, respond naturally in mixed or Sinhala.
                5. Do NOT include markdown code blocks, asterisk formatting, or bullet points in your speech answers.
            """.trimIndent()

            val requestBodyJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", userPrompt))
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", systemInstruction))
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("maxOutputTokens", 250)
                })
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestBodyJson.toString().toRequestBody(mediaType)

            val endpoint = "$BASE_URL$model:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(endpoint)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.e(TAG, "Gemini API error ${response.code}: $errBody")
                    return@withContext getOfflineJarvisResponse(userPrompt, languagePreference)
                }

                val resString = response.body?.string() ?: ""
                val rootJson = JSONObject(resString)
                val candidates = rootJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val text = parts.getJSONObject(0).optString("text", "")
                        if (text.isNotBlank()) {
                            return@withContext text.trim()
                        }
                    }
                }
            }
            getOfflineJarvisResponse(userPrompt, languagePreference)
        } catch (e: Exception) {
            Log.e(TAG, "Error invoking Gemini API", e)
            getOfflineJarvisResponse(userPrompt, languagePreference)
        }
    }

    private fun getOfflineJarvisResponse(prompt: String, lang: String): String {
        val lower = prompt.lowercase().trim()
        val hasSinhala = prompt.any { it in '\u0D80'..'\u0DFF' } || lang == "sinhala"

        return when {
            lower.contains("who are you") || lower.contains("ඔබ කවුද") -> {
                if (hasSinhala) "මම MADU JARVIS AI. ඔබගේ දුරකථන සහායකයා."
                else "I am MADU JARVIS AI, your voice-activated personal Android assistant."
            }
            lower.contains("how are you") || lower.contains("කොහොමද") -> {
                if (hasSinhala) "මම ඉතා හොඳින් සූදානම්ව සිටිමි. ඔබට මගෙන් කුමක්ද අවශ්‍ය?"
                else "All systems are operating at peak efficiency, sir. How may I assist you?"
            }
            lower.contains("thank") || lower.contains("ස්තූතියි") -> {
                if (hasSinhala) "ඔබට උදවු කිරීමට ලැබීම මගේ සතුටක්!"
                else "You are always welcome. At your service anytime."
            }
            lower.contains("hello") || lower.contains("hey") || lower.contains("හායි") || lower.contains("හලෝ") -> {
                if (hasSinhala) "ආයුබෝවන්! මම මාදු ජාර්විස්. මම ඔබට උදව් කිරීමට සූදානම්."
                else "Greetings! Jarvis online and ready for your command."
            }
            else -> {
                if (hasSinhala) {
                    "සමාවෙන්න, එම විධානය සැකසීමට නොහැකි විය. කරුණාකර නැවත උත්සාහ කරන්න හෝ Settings හි Gemini API Key පරීක්ෂා කරන්න."
                } else {
                    "I have received your request. To enable complete conversational depth, please configure your Gemini API Key in the Settings panel."
                }
            }
        }
    }
}
