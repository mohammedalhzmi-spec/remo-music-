package com.example.model

import androidx.annotation.DrawableRes

enum class PlaybackMode {
    SEQUENCE, REPEAT_ALL, REPEAT_ONE, SHUFFLE
}

enum class Mood(val arabicName: String, val englishName: String, val icon: String) {
    ALL("الكل", "All", "🎵"),
    ENERGETIC("حماسي ونشط", "Energetic", "⚡"),
    CHILL("هادئ ومريح", "Chill & Relax", "☕"),
    FOCUS("تركيز ودراسة", "Focus", "🎯"),
    ROMANTIC("رومانسي", "Romantic", "✨"),
    MELANCHOLY("حزين وعميق", "Melancholy", "🌧️"),
    WORKOUT("رياضة وتدريب", "Workout", "🔥")
}

enum class AppWallpaper(
    val id: String,
    val arabicName: String,
    val description: String,
    val icon: String,
    val gradientHexes: List<Long>,
    val accentHex: Long
) {
    OBSIDIAN_PRO(
        "obsidian",
        "الوضع الملكي الأسود",
        "أسود عميق وفخم مع لمسات نيون أنيقة",
        "🖤",
        listOf(0xFF07080EL, 0xFF0D101DL, 0xFF14172BL),
        0xFF00F0FFL
    ),
    YOUTH_CYBERPUNK(
        "youth",
        "شبابية (سايبر بانك)",
        "طاقة نيون حيوية بدرجات البنفسجي والأزرق",
        "⚡",
        listOf(0xFF0D061CL, 0xFF1B0C36L, 0xFF0B1936L),
        0xFF8A2BE2L
    ),
    WOMEN_AESTHETIC(
        "women",
        "نسائية (روز وسكينة)",
        "لمسات وردية هادئة، لافندر وشمبانيا ناعمة",
        "🌸",
        listOf(0xFF240A1DL, 0xFF35122AL, 0xFF1B0C27L),
        0xFFFF69B4L
    ),
    SPORT_RUSH(
        "sport",
        "رياضية (تيتانيوم ولهب)",
        "حماس أحمر ورياضي عالي الطاقة",
        "🔥",
        listOf(0xFF1C0505L, 0xFF300A0AL, 0xFF120404L),
        0xFFFF334BL
    ),
    NATURE_AURORA(
        "nature",
        "طبيعية (شفق زمردي)",
        "أخضر زمردي منعش وهدوء الغابات",
        "🍃",
        listOf(0xFF041710L, 0xFF08281CL, 0xFF051318L),
        0xFF00FFB2L
    )
}

enum class ChromaStyle(
    val id: String,
    val arabicName: String,
    val description: String,
    val icon: String
) {
    NEON_CYBER("neon_cyber", "نيون سايبر نبضي", "حلقات أزرق وبرتقالي دوارة مع نبضات صوتية", "⚡"),
    RAINBOW_NEBULA("rainbow", "طيف سديمي قوسي", "حلقة طيفية كاملة بألوان قوس قزح المشعة", "🌈"),
    SPECTRUM_WAVE("spectrum", "موجات التردد 360°", "أعمدة ترددات صوتية دائرية تنبض مع الإيقاع", "🌊"),
    GOLDEN_FIRE("golden_fire", "لهب وشرارات ذهبية", "توهج ناري ذهبي وعنبري متصاعد", "🔥"),
    COSMIC_PULSE("cosmic", "نبض فضائي كوني", "هالة مجرية نيلي وبنفسجي غامرة", "🪐")
}

data class LyricsLine(
    val timeMs: Long,
    val originalText: String,
    val translatedText: String
)

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val mediaUri: String, // local content URI or web stream or synthetic ID
    val albumArtRes: Int? = null,
    val albumArtUri: String? = null,
    val genre: String = "Pop",
    val mood: Mood = Mood.ENERGETIC,
    val folder: String = "Music",
    val folderPath: String = "",
    val storageType: String = "ذاكرة الهاتف",
    val isCloud: Boolean = false,
    val lyrics: List<LyricsLine> = emptyList(),
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val userNotes: String = ""
)

data class Artist(
    val name: String,
    val songCount: Int,
    val albumCount: Int,
    val albumArtRes: Int? = null
)

data class Album(
    val name: String,
    val artist: String,
    val songCount: Int,
    val albumArtRes: Int? = null
)

data class Folder(
    val id: String,
    val name: String,
    val path: String,
    val storageType: String = "ذاكرة الهاتف",
    val songCount: Int
)

data class EqualizerPreset(
    val id: String,
    val name: String,
    val arabicName: String,
    val bandLevels: List<Int>, // 5 bands in dB (-12 to +12)
    val bassBoost: Int = 0, // 0 to 100
    val virtualizer: Int = 0 // 0 to 100
)
