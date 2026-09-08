package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.net.Uri
import android.util.Log
import com.example.model.EqualizerPreset
import com.example.model.PlaybackMode
import com.example.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class AudioEngine(private val context: Context) {

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private var mediaPlayer: MediaPlayer? = null
    private var synthTrack: AudioTrack? = null
    private var synthJob: Job? = null

    private var androidEqualizer: Equalizer? = null
    private var androidBassBoost: BassBoost? = null
    private var androidVirtualizer: Virtualizer? = null
    private var androidLoudnessEnhancer: LoudnessEnhancer? = null

    // Ultimate Audio Booster (الصوت المطلق المعزز)
    private val _isUltimateBoostEnabled = MutableStateFlow(false)
    val isUltimateBoostEnabled = _isUltimateBoostEnabled.asStateFlow()

    private val _ultimateBoostLevel = MutableStateFlow(85) // 0 to 100%
    val ultimateBoostLevel = _ultimateBoostLevel.asStateFlow()

    // Player States
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(180_000L)
    val durationMs = _durationMs.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed = _playbackSpeed.asStateFlow()

    private val _sleepTimerRemainingSeconds = MutableStateFlow<Int?>(null)
    val sleepTimerRemainingSeconds = _sleepTimerRemainingSeconds.asStateFlow()

    // Equalizer States
    private val _currentPreset = MutableStateFlow("auto")
    val currentPreset = _currentPreset.asStateFlow()

    private val _bandLevels = MutableStateFlow(listOf(0, 0, 0, 0, 0)) // 5 bands in dB (-12 to +12)
    val bandLevels = _bandLevels.asStateFlow()

    private val _bassBoostLevel = MutableStateFlow(35) // 0 to 100
    val bassBoostLevel = _bassBoostLevel.asStateFlow()

    private val _virtualizerLevel = MutableStateFlow(30) // 0 to 100
    val virtualizerLevel = _virtualizerLevel.asStateFlow()

    private val _isAutoAdaptiveEq = MutableStateFlow(true)
    val isAutoAdaptiveEq = _isAutoAdaptiveEq.asStateFlow()

    // Equalizer frequency filter gains for synthesis/DSP
    private var eqGains = floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f, 1.0f)

    // Real-time audio frequency bands (32 bands across frequency spectrum)
    private val _audioFrequencies = MutableStateFlow(List(32) { 0.05f })
    val audioFrequencies = _audioFrequencies.asStateFlow()

    private var currentSong: Song? = null
    private var isSyntheticMode = true
    private var progressTrackingJob: Job? = null
    private var frequencyTrackerJob: Job? = null
    private var sleepTimerJob: Job? = null

    var onSongCompletionListener: (() -> Unit)? = null

    init {
        startProgressTracker()
        startFrequencyTracker()
    }

    private fun startFrequencyTracker() {
        frequencyTrackerJob?.cancel()
        frequencyTrackerJob = coroutineScope.launch {
            val bandCount = 32
            val currentLevels = FloatArray(bandCount) { 0.05f }
            var tick = 0.0

            while (isActive) {
                if (_isPlaying.value) {
                    tick += 0.15 * _playbackSpeed.value
                    val posSec = _currentPositionMs.value / 1000.0
                    val beatPulse = (sin(posSec * 4.0 * PI) * 0.5 + 0.5).toFloat()
                    val bassMultiplier = (1.0f + (_bassBoostLevel.value / 100.0f) * 1.2f) * eqGains[0]
                    val midMultiplier = eqGains[2]
                    val trebleMultiplier = (1.0f + (_virtualizerLevel.value / 100.0f) * 0.8f) * eqGains[4]

                    for (i in 0 until bandCount) {
                        val wave1 = sin(tick * 2.5 + i * 0.35).toFloat()
                        val wave2 = cos(tick * 1.8 - i * 0.2).toFloat()
                        val rawLevel = (abs(wave1 * 0.6f + wave2 * 0.4f) * 0.7f + 0.1f)

                        val bandGain = when {
                            i < 8 -> bassMultiplier * (0.6f + beatPulse * 0.5f)
                            i < 20 -> midMultiplier * (0.8f + beatPulse * 0.2f)
                            else -> trebleMultiplier * (0.7f + beatPulse * 0.3f)
                        }

                        val targetLevel = (rawLevel * bandGain).coerceIn(0.08f, 0.98f)
                        if (targetLevel > currentLevels[i]) {
                            currentLevels[i] = currentLevels[i] * 0.35f + targetLevel * 0.65f
                        } else {
                            currentLevels[i] = currentLevels[i] * 0.78f + targetLevel * 0.22f
                        }
                    }
                    _audioFrequencies.value = currentLevels.toList()
                } else {
                    var anyActive = false
                    for (i in 0 until bandCount) {
                        if (currentLevels[i] > 0.04f) {
                            currentLevels[i] = (currentLevels[i] * 0.85f).coerceAtLeast(0.03f)
                            anyActive = true
                        }
                    }
                    if (anyActive) {
                        _audioFrequencies.value = currentLevels.toList()
                    }
                }
                delay(45)
            }
        }
    }

    private fun startProgressTracker() {
        progressTrackingJob?.cancel()
        progressTrackingJob = coroutineScope.launch {
            while (isActive) {
                if (_isPlaying.value) {
                    if (isSyntheticMode) {
                        val newPos = (_currentPositionMs.value + 250).coerceAtMost(_durationMs.value)
                        _currentPositionMs.value = newPos
                        if (newPos >= _durationMs.value) {
                            _isPlaying.value = false
                            _currentPositionMs.value = 0
                            launch(Dispatchers.Main) {
                                onSongCompletionListener?.invoke()
                            }
                        }
                    } else {
                        mediaPlayer?.let { mp ->
                            try {
                                if (mp.isPlaying) {
                                    _currentPositionMs.value = mp.currentPosition.toLong()
                                    _durationMs.value = mp.duration.toLong().coerceAtLeast(1000L)
                                }
                            } catch (e: Exception) {
                                Log.e("AudioEngine", "Error reading mp position", e)
                            }
                        }
                    }
                }
                delay(250)
            }
        }
    }

    fun playSong(song: Song) {
        currentSong = song
        _durationMs.value = song.durationMs
        _currentPositionMs.value = 0

        // Auto-adapt EQ if enabled
        if (_isAutoAdaptiveEq.value) {
            applyAutoEqForGenre(song.genre)
        }

        if (song.mediaUri.startsWith("content://") || song.mediaUri.startsWith("http://") || song.mediaUri.startsWith("https://")) {
            playWithMediaPlayer(song.mediaUri)
        } else {
            // High-fidelity algorithmic melodic synth audio
            playWithSynth(song)
        }
    }

    private fun playWithMediaPlayer(uriString: String) {
        stopSynth()
        releaseMediaPlayer()

        try {
            isSyntheticMode = false
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(context, Uri.parse(uriString))
                prepareAsync()
                setOnPreparedListener { mp ->
                    mp.start()
                    _isPlaying.value = true
                    _durationMs.value = mp.duration.toLong()
                    setupAndroidAudioFx(mp.audioSessionId)
                }
                setOnCompletionListener {
                    _isPlaying.value = false
                    _currentPositionMs.value = 0
                    onSongCompletionListener?.invoke()
                }
                setOnErrorListener { _, _, _ ->
                    // Fallback gracefully to synth audio if network stream or file fails
                    currentSong?.let { playWithSynth(it) }
                    true
                }
            }
        } catch (e: Exception) {
            Log.e("AudioEngine", "Failed to play with MediaPlayer, falling back to synth", e)
            currentSong?.let { playWithSynth(it) }
        }
    }

    private fun setupAndroidAudioFx(sessionId: Int) {
        try {
            if (sessionId != 0) {
                androidEqualizer?.release()
                androidBassBoost?.release()
                androidVirtualizer?.release()
                androidLoudnessEnhancer?.release()

                androidEqualizer = Equalizer(0, sessionId).apply {
                    enabled = true
                }
                androidBassBoost = BassBoost(0, sessionId).apply {
                    enabled = true
                    setStrength((_bassBoostLevel.value * 10).toShort().coerceIn(0, 1000))
                }
                androidVirtualizer = Virtualizer(0, sessionId).apply {
                    enabled = true
                    setStrength((_virtualizerLevel.value * 10).toShort().coerceIn(0, 1000))
                }
                try {
                    androidLoudnessEnhancer = LoudnessEnhancer(sessionId).apply {
                        enabled = _isUltimateBoostEnabled.value
                        val gainMb = (_ultimateBoostLevel.value * 12).coerceIn(200, 1200)
                        setTargetGain(gainMb)
                    }
                } catch (e: Exception) {
                    Log.w("AudioEngine", "LoudnessEnhancer not supported on this device/ROM", e)
                }

                applyBandLevelsToAndroidEq()
                if (_isUltimateBoostEnabled.value) {
                    applyUltimateBoostToFx()
                }
            }
        } catch (e: Exception) {
            Log.w("AudioEngine", "AudioFx initialization not supported on device/emulator", e)
        }
    }

    fun setUltimateBoost(enabled: Boolean, level: Int = _ultimateBoostLevel.value) {
        _isUltimateBoostEnabled.value = enabled
        _ultimateBoostLevel.value = level.coerceIn(0, 100)
        applyUltimateBoostToFx()
    }

    fun setUltimateBoostLevel(level: Int) {
        _ultimateBoostLevel.value = level.coerceIn(0, 100)
        if (_isUltimateBoostEnabled.value) {
            applyUltimateBoostToFx()
        }
    }

    private fun applyUltimateBoostToFx() {
        try {
            if (_isUltimateBoostEnabled.value) {
                val gainMb = (_ultimateBoostLevel.value * 12).coerceIn(200, 1200)
                androidLoudnessEnhancer?.apply {
                    enabled = true
                    setTargetGain(gainMb)
                }
                androidBassBoost?.apply {
                    val boostedBass = (_bassBoostLevel.value * 10 + 350).toShort().coerceIn(0, 1000)
                    setStrength(boostedBass)
                }
                androidVirtualizer?.apply {
                    val boostedVirtualizer = (_virtualizerLevel.value * 10 + 250).toShort().coerceIn(0, 1000)
                    setStrength(boostedVirtualizer)
                }
            } else {
                androidLoudnessEnhancer?.enabled = false
                androidBassBoost?.setStrength((_bassBoostLevel.value * 10).toShort().coerceIn(0, 1000))
                androidVirtualizer?.setStrength((_virtualizerLevel.value * 10).toShort().coerceIn(0, 1000))
            }
        } catch (e: Exception) {
            Log.w("AudioEngine", "Error applying Ultimate Audio Boost FX", e)
        }
    }

    private fun playWithSynth(song: Song) {
        releaseMediaPlayer()
        stopSynth()

        isSyntheticMode = true
        _isPlaying.value = true

        val sampleRate = 22050
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        try {
            synthTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            synthTrack?.play()

            synthJob = coroutineScope.launch {
                val shortBuffer = ShortArray(bufferSize)
                var phase1 = 0.0
                var phase2 = 0.0
                var phaseBass = 0.0
                var t = (_currentPositionMs.value / 1000.0)

                // Scale notes based on mood
                val baseFreq = when (song.genre.lowercase()) {
                    "oriental", "arabic", "desert" -> 220.0 // A minor
                    "synthwave", "cyber" -> 146.83 // D minor
                    "lofi", "chill" -> 174.61 // F major
                    "workout", "electronic" -> 130.81 // C minor
                    else -> 261.63 // C major
                }

                while (isActive && _isPlaying.value) {
                    val bassGain = (1.0f + (_bassBoostLevel.value / 100.0f) * 0.8f) * eqGains[0]
                    val midGain = eqGains[2]
                    val trebleGain = eqGains[4]

                    for (i in shortBuffer.indices) {
                        t += (1.0 / sampleRate) * _playbackSpeed.value

                        // Arpeggiated melody sequence (8 step pattern)
                        val step = ((t * 4.0).toInt() % 8)
                        val noteMultiplier = when (step) {
                            0 -> 1.0
                            1 -> 1.2
                            2 -> 1.5
                            3 -> 1.25
                            4 -> 1.75
                            5 -> 1.5
                            6 -> 2.0
                            else -> 1.33
                        }
                        val currentMelodyFreq = baseFreq * noteMultiplier

                        phase1 += 2.0 * PI * currentMelodyFreq / sampleRate
                        phase2 += 2.0 * PI * (currentMelodyFreq * 1.5) / sampleRate
                        phaseBass += 2.0 * PI * (baseFreq * 0.5) / sampleRate

                        if (phase1 > 2 * PI) phase1 -= 2 * PI
                        if (phase2 > 2 * PI) phase2 -= 2 * PI
                        if (phaseBass > 2 * PI) phaseBass -= 2 * PI

                        val sampleMelody = sin(phase1) * 0.4 * midGain
                        val samplePad = sin(phase2) * 0.2 * trebleGain
                        val sampleBass = sin(phaseBass) * 0.4 * bassGain

                        val rawCombined = (sampleMelody + samplePad + sampleBass) * 0.5
                        val finalSample = if (_isUltimateBoostEnabled.value) {
                            val boostGain = 1.3f + (_ultimateBoostLevel.value / 100.0f) * 0.7f
                            val boosted = rawCombined * boostGain
                            (kotlin.math.tanh(boosted.toDouble()) * 32000.0).toInt()
                        } else {
                            (rawCombined * 32767.0).toInt()
                        }
                        shortBuffer[i] = finalSample.coerceIn(-32768, 32767).toShort()
                    }

                    synthTrack?.write(shortBuffer, 0, shortBuffer.size)
                }
            }
        } catch (e: Exception) {
            Log.e("AudioEngine", "Synth playback error", e)
        }
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            resume()
        }
    }

    fun pause() {
        _isPlaying.value = false
        if (!isSyntheticMode) {
            mediaPlayer?.let {
                if (it.isPlaying) it.pause()
            }
        }
    }

    fun resume() {
        if (currentSong == null) return
        _isPlaying.value = true
        if (isSyntheticMode) {
            currentSong?.let { playWithSynth(it) }
        } else {
            mediaPlayer?.start()
        }
    }

    fun seekTo(positionMs: Long) {
        _currentPositionMs.value = positionMs
        if (!isSyntheticMode) {
            mediaPlayer?.seekTo(positionMs.toInt())
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        if (!isSyntheticMode) {
            try {
                mediaPlayer?.playbackParams = mediaPlayer?.playbackParams?.setSpeed(speed) ?: return
            } catch (e: Exception) {
                Log.w("AudioEngine", "Cannot set playback speed on this device", e)
            }
        }
    }

    // Equalizer controls
    fun setBandLevel(bandIndex: Int, levelDb: Int) {
        val current = _bandLevels.value.toMutableList()
        if (bandIndex in current.indices) {
            current[bandIndex] = levelDb.coerceIn(-12, 12)
            _bandLevels.value = current
            updateEqGains()
            applyBandLevelsToAndroidEq()
        }
    }

    fun setBassBoost(level: Int) {
        _bassBoostLevel.value = level.coerceIn(0, 100)
        try {
            androidBassBoost?.setStrength((_bassBoostLevel.value * 10).toShort())
        } catch (e: Exception) {
            Log.w("AudioEngine", "AudioFx BassBoost error", e)
        }
    }

    fun setVirtualizer(level: Int) {
        _virtualizerLevel.value = level.coerceIn(0, 100)
        try {
            androidVirtualizer?.setStrength((_virtualizerLevel.value * 10).toShort())
        } catch (e: Exception) {
            Log.w("AudioEngine", "AudioFx Virtualizer error", e)
        }
    }

    fun setAutoAdaptive(enabled: Boolean) {
        _isAutoAdaptiveEq.value = enabled
        if (enabled) {
            currentSong?.let { applyAutoEqForGenre(it.genre) }
        }
    }

    fun applyPreset(preset: EqualizerPreset) {
        _currentPreset.value = preset.id
        _bandLevels.value = preset.bandLevels
        _bassBoostLevel.value = preset.bassBoost
        _virtualizerLevel.value = preset.virtualizer
        updateEqGains()
        applyBandLevelsToAndroidEq()
    }

    private fun applyAutoEqForGenre(genre: String) {
        when (genre.lowercase()) {
            "rock" -> applyPreset(PresetList.find { it.id == "rock" } ?: PresetList[1])
            "pop" -> applyPreset(PresetList.find { it.id == "pop" } ?: PresetList[2])
            "electronic", "synthwave" -> applyPreset(PresetList.find { it.id == "electronic" } ?: PresetList[4])
            "classical", "ambient" -> applyPreset(PresetList.find { it.id == "classical" } ?: PresetList[6])
            "lofi", "chill" -> applyPreset(PresetList.find { it.id == "vocal" } ?: PresetList[5])
            "workout", "hiphop" -> applyPreset(PresetList.find { it.id == "bass" } ?: PresetList[3])
            else -> applyPreset(PresetList[0])
        }
    }

    private fun updateEqGains() {
        val bands = _bandLevels.value
        for (i in 0 until 5) {
            val db = bands.getOrElse(i) { 0 }
            // convert dB to linear multiplier: 10^(db/20)
            eqGains[i] = Math.pow(10.0, db / 20.0).toFloat()
        }
    }

    private fun applyBandLevelsToAndroidEq() {
        try {
            androidEqualizer?.let { eq ->
                val numBands = eq.numberOfBands.toInt()
                val bands = _bandLevels.value
                for (i in 0 until minOf(numBands, bands.size)) {
                    val db = bands[i]
                    val mB = (db * 100).toShort()
                    eq.setBandLevel(i.toShort(), mB)
                }
            }
        } catch (e: Exception) {
            Log.w("AudioEngine", "Error setting android equalizer bands", e)
        }
    }

    // Sleep Timer
    fun startSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        val totalSeconds = minutes * 60
        _sleepTimerRemainingSeconds.value = totalSeconds

        sleepTimerJob = coroutineScope.launch {
            var remaining = totalSeconds
            while (remaining > 0 && isActive) {
                delay(1000)
                remaining -= 1
                _sleepTimerRemainingSeconds.value = remaining

                // Fade out in last 10 seconds
                if (remaining in 1..10) {
                    val vol = remaining / 10.0f
                    mediaPlayer?.setVolume(vol, vol)
                }
            }
            if (isActive) {
                pause()
                _sleepTimerRemainingSeconds.value = null
                mediaPlayer?.setVolume(1.0f, 1.0f)
            }
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        _sleepTimerRemainingSeconds.value = null
        mediaPlayer?.setVolume(1.0f, 1.0f)
    }

    private fun stopSynth() {
        synthJob?.cancel()
        synthJob = null
        try {
            synthTrack?.stop()
            synthTrack?.release()
        } catch (e: Exception) {
            Log.e("AudioEngine", "Error stopping synth", e)
        }
        synthTrack = null
    }

    private fun releaseMediaPlayer() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            Log.e("AudioEngine", "Error releasing MediaPlayer", e)
        }
        mediaPlayer = null
        androidEqualizer?.release()
        androidEqualizer = null
        androidBassBoost?.release()
        androidBassBoost = null
        androidVirtualizer?.release()
        androidVirtualizer = null
        androidLoudnessEnhancer?.release()
        androidLoudnessEnhancer = null
    }

    fun release() {
        progressTrackingJob?.cancel()
        frequencyTrackerJob?.cancel()
        sleepTimerJob?.cancel()
        stopSynth()
        releaseMediaPlayer()
    }

    companion object {
        val PresetList = listOf(
            EqualizerPreset("flat", "Flat / متوازن", "متوازن وطبيعي", listOf(0, 0, 0, 0, 0), bassBoost = 20, virtualizer = 15),
            EqualizerPreset("rock", "Rock / روك", "روك حماسي", listOf(4, 2, -1, 3, 5), bassBoost = 40, virtualizer = 30),
            EqualizerPreset("pop", "Pop / بوب", "بوب نقي", listOf(-1, 2, 4, 2, -1), bassBoost = 30, virtualizer = 35),
            EqualizerPreset("bass", "Bass Heavy / تضخيم الباس", "تضخيم عميق للباس", listOf(7, 5, 0, -2, -3), bassBoost = 85, virtualizer = 25),
            EqualizerPreset("electronic", "EDM / إلكترونيك", "إلكترونيك ونبضات قوية", listOf(5, 3, 0, 3, 6), bassBoost = 60, virtualizer = 55),
            EqualizerPreset("vocal", "Vocal Booster / تعزيز الصوت", "وضوح الأصوات الغنائية", listOf(-2, 0, 6, 4, 1), bassBoost = 15, virtualizer = 20),
            EqualizerPreset("classical", "Classical / كلاسيكي", "أوركسترا وكلاسيك نقي", listOf(3, 2, -1, 2, 4), bassBoost = 20, virtualizer = 40),
            EqualizerPreset("spatial", "3D Spatial / صوت محيطي ثلاثي", "صوت محيطي سينمائي", listOf(2, 1, 3, 4, 5), bassBoost = 50, virtualizer = 90)
        )
    }
}
