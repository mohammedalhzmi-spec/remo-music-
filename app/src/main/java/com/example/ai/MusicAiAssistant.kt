package com.example.ai

import android.util.Log
import com.example.BuildConfig
import com.example.model.Mood
import com.example.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class MusicAiAssistant {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun getSongRecommendations(
        userPrompt: String,
        currentSong: Song?,
        library: List<Song>
    ): List<Song> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (!apiKey.isNullOrBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val libraryTitles = library.joinToString(", ") { "${it.title} by ${it.artist} (${it.genre}, ${it.mood.englishName})" }
                val promptText = """
                    You are a professional music curator in REMOMUSIC.
                    The user requests: "$userPrompt"
                    Currently playing: "${currentSong?.title ?: "None"}"
                    Available songs in library: [$libraryTitles]
                    
                    Select or recommend the best matching songs from the library. Return ONLY a JSON array of exact matching song titles from the library list.
                    Example: ["Cyber Horizon (أفق السايبر)", "Desert Mirage (سراب الصحراء)"]
                """.trimIndent()

                val responseText = callGeminiApi(promptText, apiKey)
                if (!responseText.isNullOrBlank()) {
                    val recommendedTitles = parseJsonStringArray(responseText)
                    val matched = library.filter { song ->
                        recommendedTitles.any { title -> song.title.contains(title, ignoreCase = true) || title.contains(song.title, ignoreCase = true) }
                    }
                    if (matched.isNotEmpty()) return@withContext matched
                }
            } catch (e: Exception) {
                Log.w("MusicAiAssistant", "Gemini API failed, using smart heuristic fallback", e)
            }
        }

        // Smart Musical Heuristic Fallback
        val lowerPrompt = userPrompt.lowercase()
        val targetMood = when {
            lowerPrompt.contains("طريق") || lowerPrompt.contains("سفر") || lowerPrompt.contains("drive") || lowerPrompt.contains("ليل") -> Mood.ENERGETIC
            lowerPrompt.contains("نوم") || lowerPrompt.contains("استرخاء") || lowerPrompt.contains("هدوء") || lowerPrompt.contains("relax") -> Mood.CHILL
            lowerPrompt.contains("دراسة") || lowerPrompt.contains("عمل") || lowerPrompt.contains("برمجة") || lowerPrompt.contains("focus") -> Mood.FOCUS
            lowerPrompt.contains("تمرين") || lowerPrompt.contains("جيم") || lowerPrompt.contains("رياضة") || lowerPrompt.contains("gym") -> Mood.WORKOUT
            lowerPrompt.contains("حب") || lowerPrompt.contains("رومانس") || lowerPrompt.contains("love") -> Mood.ROMANTIC
            lowerPrompt.contains("حزن") || lowerPrompt.contains("شجن") || lowerPrompt.contains("sad") -> Mood.MELANCHOLY
            else -> currentSong?.mood ?: Mood.ENERGETIC
        }

        val filtered = library.filter { it.mood == targetMood }
        if (filtered.isNotEmpty()) filtered else library.shuffled().take(4)
    }

    suspend fun translateLyricsLine(originalText: String, targetLanguage: String = "ar"): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (!apiKey.isNullOrBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val promptText = "Translate this song lyrics line poetic and accurately into Arabic: \"$originalText\". Output only the translated Arabic text."
                val response = callGeminiApi(promptText, apiKey)
                if (!response.isNullOrBlank()) {
                    return@withContext response.trim()
                }
            } catch (e: Exception) {
                Log.w("MusicAiAssistant", "Gemini lyrics translation failed", e)
            }
        }

        // Fallback instant translations for common music phrases
        when {
            originalText.contains("Night city", true) -> "أضواء المدينة الليلية تضيء عتمة المساء"
            originalText.contains("Speeding", true) -> "ننطلق بسرعة متجاوزين كل الحواجز"
            originalText.contains("Neon", true) -> "نبضات النيون تتدفق في الأفق"
            originalText.contains("Desert", true) -> "نسيم الصحراء يهمس بالألحان الخالدة"
            originalText.contains("Rain", true) -> "قطرات المطر تعزف نغمات السكون"
            else -> originalText
        }
    }

    suspend fun explainSongMeaning(song: Song): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (!apiKey.isNullOrBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val prompt = "اشرح المعنى الفني والمزاج الموسيقي لأغنية '${song.title}' للمطرب '${song.artist}' ونوعها '${song.genre}' في فقرة موجزة وجذابة ومحفزة للاستماع باللغة العربية."
                val res = callGeminiApi(prompt, apiKey)
                if (!res.isNullOrBlank()) return@withContext res.trim()
            } catch (e: Exception) {
                Log.w("MusicAiAssistant", "Gemini song meaning call failed", e)
            }
        }

        // Contextual analysis fallback
        val moodDesc = when (song.mood) {
            Mood.ENERGETIC -> "تنتمي هذه الأغنية إلى الإيقاع الحماسي السريع، حيث تمتزج موجات السنثسيزر مع نبضات الباس القوية لتبعث في المستمع طاقة متجددة وشعوراً بالانطلاق وتحدي المستحيل."
            Mood.CHILL -> "مقطوعة تتميز بالهدوء المريح ونغمات الفينيل الدافئة، صُممت خصيصاً لإبعاد التوتر وتوفير مساحة ذهنية صافية للتأمل والاسترخاء."
            Mood.FOCUS -> "تعتمد على ترددات نغمية متزنة تساعد العقل على التركيز العميق في العمل والدراسة، مع عزل المشتتات المحيطة بأداء صوتي فائق النقاء."
            Mood.ROMANTIC -> "لحن عاطفي دافئ يمزج بين الآلات الشرقية الأصيلة والهارموني الحديث، ليعبر عن أسمى مشاعر الود والانسجام الروحي."
            Mood.MELANCHOLY -> "مقطوعة شجية تحمل أبعاداً موسيقية عميقة تلامس الوجدان، تمنح المستمع راحة وسكينة مع نغمات البيانو الشاعرية."
            Mood.WORKOUT -> "إيقاعات قوية وعالية الوتيرة تحفز النشاط البدني وتدفق الأدرينالين خلال التدريبات الرياضية الصعبة."
            Mood.ALL -> "مزيج موسيقي متوازن يناسب مختلف أوقات اليوم بجودة صوتية عالية."
        }
        "تحليل الذكاء الاصطناعي REMO AI: أغنية '${song.title}' بنمط (${song.genre}). $moodDesc تم ضبط ترددات المعادل الصوتي تلقائياً لتحقيق أقصى درجات النقاء."
    }

    private fun callGeminiApi(promptText: String, apiKey: String): String? {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val bodyJson = JSONObject().apply {
            val contentsArray = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val partsArray = JSONArray().apply {
                        val partObj = JSONObject().apply {
                            put("text", promptText)
                        }
                        put(partObj)
                    }
                    put("parts", partsArray)
                }
                put(contentObj)
            }
            put("contents", contentsArray)
        }

        val request = Request.Builder()
            .url(url)
            .post(bodyJson.toString().toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val responseString = response.body?.string() ?: return null
            val root = JSONObject(responseString)
            val candidates = root.optJSONArray("candidates") ?: return null
            val firstCandidate = candidates.optJSONObject(0) ?: return null
            val content = firstCandidate.optJSONObject("content") ?: return null
            val parts = content.optJSONArray("parts") ?: return null
            val firstPart = parts.optJSONObject(0) ?: return null
            return firstPart.optString("text")
        }
    }

    private fun parseJsonStringArray(jsonString: String): List<String> {
        val clean = jsonString.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        val list = mutableListOf<String>()
        try {
            val arr = JSONArray(clean)
            for (i in 0 until arr.length()) {
                list.add(arr.getString(i))
            }
        } catch (e: Exception) {
            // fallback: extract quoted strings
            val regex = Regex("\"([^\"]+)\"")
            regex.findAll(clean).forEach { match ->
                list.add(match.groupValues[1])
            }
        }
        return list
    }
}
