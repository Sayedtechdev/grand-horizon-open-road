package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

class AudioSynthEngine {
    private var audioTrack: AudioTrack? = null
    private val isRunning = AtomicBoolean(false)
    private var synthThread: Thread? = null

    // Real-time audio parameters
    @Volatile var isEnabled: Boolean = true
    @Volatile var masterVolume: Float = 0.7f
    @Volatile var rpm: Float = 850f // 800 to 8500
    @Volatile var speedRatio: Float = 0f // 0f to 1f
    @Volatile var isHandbraking: Boolean = false
    @Volatile var isFlightMode: Boolean = false
    @Volatile var isBoosting: Boolean = false
    @Volatile var rainIntensity: Float = 0f
    @Volatile var isHornActive: Boolean = false
    @Volatile var radioStation: Int = 0 // 0 = Chill Melodic, 1 = Synthwave, 2 = Off

    private val sampleRate = 22050
    private val bufferSize = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    ).coerceAtLeast(sampleRate / 10)

    fun start() {
        if (isRunning.get()) return
        isRunning.set(true)

        synthThread = thread(name = "GrandHorizonSynthThread", isDaemon = true) {
            try {
                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
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

                audioTrack?.play()

                val shortBuffer = ShortArray(1024)
                var phaseEngine = 0.0
                var phaseEngineSub = 0.0
                var phaseHornA = 0.0
                var phaseHornB = 0.0
                var phaseMusic = 0.0
                var musicBeatCounter = 0
                var currentChordIndex = 0

                // Ambient melodic chord progressions (Cmaj9, Am9, Fmaj7, Gsus4)
                val chords = arrayOf(
                    floatArrayOf(261.63f, 329.63f, 392.00f, 493.88f), // Cmaj7
                    floatArrayOf(220.00f, 261.63f, 329.63f, 392.00f), // Am7
                    floatArrayOf(174.61f, 220.00f, 261.63f, 329.63f), // Fmaj7
                    floatArrayOf(196.00f, 246.94f, 293.66f, 392.00f)  // G
                )

                while (isRunning.get()) {
                    if (!isEnabled) {
                        shortBuffer.fill(0)
                        audioTrack?.write(shortBuffer, 0, shortBuffer.size)
                        Thread.sleep(20)
                        continue
                    }

                    // Base engine frequency calculated from RPM
                    val baseFreq = (rpm / 60.0) * 1.5 // 20Hz at idle to 210Hz at redline
                    val engineInc = 2.0 * PI * baseFreq / sampleRate
                    val engineSubInc = 2.0 * PI * (baseFreq * 0.5) / sampleRate

                    val hornIncA = 2.0 * PI * 440.0 / sampleRate
                    val hornIncB = 2.0 * PI * 554.37 / sampleRate

                    // Radio / Music step (distinct presets for 0 = Chill Melodic, 1 = Synthwave)
                    musicBeatCounter++
                    val cycleThreshold = if (radioStation == 1) 28 else 50
                    if (musicBeatCounter > cycleThreshold) {
                        musicBeatCounter = 0
                        currentChordIndex = (currentChordIndex + 1) % chords.size
                    }
                    val currentNotes = chords[currentChordIndex]
                    val arpeggioIndex = if (radioStation == 1) {
                        (musicBeatCounter / 7) % currentNotes.size
                    } else {
                        (musicBeatCounter / 13) % currentNotes.size
                    }
                    val melodyFreq = currentNotes[arpeggioIndex] * (if (radioStation == 1) 1.25f else 1.0f)
                    val musicInc = 2.0 * PI * melodyFreq / sampleRate

                    for (i in shortBuffer.indices) {
                        var sample = 0.0

                        // 1. Engine sound (Harmonic blend)
                        phaseEngine += engineInc
                        phaseEngineSub += engineSubInc
                        if (phaseEngine > 2.0 * PI) phaseEngine -= 2.0 * PI
                        if (phaseEngineSub > 2.0 * PI) phaseEngineSub -= 2.0 * PI

                        val engineWave = (sin(phaseEngine) * 0.5 +
                                sin(phaseEngine * 2.0) * 0.3 +
                                sin(phaseEngine * 3.0) * 0.15 +
                                sin(phaseEngineSub) * 0.4)

                        val engineGain = if (isFlightMode) 0.15 else (0.28 + (speedRatio * 0.18))
                        sample += engineWave * engineGain

                        // 2. Flight mode plasma thruster hum & whoosh
                        if (isFlightMode) {
                            val plasmaNoise = (Random.nextFloat() * 2f - 1f) * 0.12
                            val thrusterTone = sin(phaseEngine * 1.8) * 0.22
                            val boostFactor = if (isBoosting) 1.8 else 1.0
                            sample += (plasmaNoise + thrusterTone) * 0.35 * boostFactor
                        }

                        // 3. Tire screech on handbrake
                        if (isHandbraking && speedRatio > 0.15f) {
                            val screechNoise = (Random.nextFloat() * 2f - 1f) * (0.35 * speedRatio)
                            sample += screechNoise
                        }

                        // 4. Rain patter noise
                        if (rainIntensity > 0.05f) {
                            val rainNoise = (Random.nextFloat() * 2f - 1f) * (rainIntensity * 0.12)
                            sample += rainNoise
                        }

                        // 5. Horn
                        if (isHornActive) {
                            phaseHornA += hornIncA
                            phaseHornB += hornIncB
                            sample += (sin(phaseHornA) + sin(phaseHornB)) * 0.35
                        }

                        // 6. In-car radio ambience (0 = Chill Melodic, 1 = Synthwave, 2 = Off)
                        if (radioStation != 2) {
                            phaseMusic += musicInc
                            if (phaseMusic > 2.0 * PI) phaseMusic -= 2.0 * PI
                            val musicWave = if (radioStation == 1) {
                                // Synthwave pulse + sub octave shimmer
                                (sin(phaseMusic) * 0.65 + sin(phaseMusic * 2.0) * 0.35) * 0.11
                            } else {
                                // Warm Chill Melodic sine
                                sin(phaseMusic) * 0.10
                            }
                            sample += musicWave
                        }

                        // Master volume clamp to 16-bit PCM
                        val clamped = (sample * masterVolume * Short.MAX_VALUE).coerceIn(
                            Short.MIN_VALUE.toDouble(),
                            Short.MAX_VALUE.toDouble()
                        )
                        shortBuffer[i] = clamped.toInt().toShort()
                    }

                    audioTrack?.write(shortBuffer, 0, shortBuffer.size)
                }
            } catch (e: Exception) {
                // Audio track safely terminated
            } finally {
                try {
                    audioTrack?.stop()
                    audioTrack?.release()
                } catch (ignored: Exception) {}
                audioTrack = null
            }
        }
    }

    fun stop() {
        isRunning.set(false)
        synthThread?.interrupt()
        synthThread = null
    }
}
