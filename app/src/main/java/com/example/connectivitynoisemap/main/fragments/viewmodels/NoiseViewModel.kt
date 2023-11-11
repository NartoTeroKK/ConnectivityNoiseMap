package com.example.connectivitynoisemap.main.fragments.viewmodels

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.math.abs
import kotlin.math.log10

class NoiseViewModel : ViewModel() {

    private val soundMeter = SoundMeter()

    private inner class SoundMeter {
        private var ar: AudioRecord? = null
        private val sampleRate = 8000
        private var minSize = 0
        private var isRecording = false
        private lateinit var recordingJob : Job

        var volume = 0.0  // [dB]

        @SuppressLint("MissingPermission")
        private fun start() {
            minSize = AudioRecord.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            ar = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minSize
            )
            ar?.startRecording()
            isRecording = true

            recordingJob = viewModelScope.launch(Dispatchers.IO) {
                var sum = 0
                var count = 0

                while (isRecording) {
                    val buffer = ShortArray(minSize)
                    val bytesRead = ar?.read(buffer, 0, minSize)
                    if (bytesRead == AudioRecord.ERROR_INVALID_OPERATION || bytesRead == AudioRecord.ERROR_BAD_VALUE) {
                        Log.e("AudioRecord", "Error reading audio data")
                        break
                    }

                    for (s in buffer) {
                        sum += abs(s.toInt())
                        count++
                    }
                }

                // Compute the average amplitude and volume
                val avgAmp = sum.toDouble() / count.toDouble()
                val avgVol = amp2dB(avgAmp)

                volume = avgVol
                recordingJob.cancel()
            }

        }

         private fun stop() {
            isRecording = false
            runBlocking {
                recordingJob.join()
            }
            ar?.stop()
            ar?.release()
        }

        private fun amp2dB(amp:Double): Double {
            return 20 * log10(amp)
        }

        fun recordNoise(delay: Long): Deferred<Unit> =
            viewModelScope.async(Dispatchers.IO) {
                start()
                delay(delay)
                stop()
            }
    }

    suspend fun measureNoise(delay: Long) : Double {
        soundMeter.recordNoise(delay).await()
        return soundMeter.volume
    }

}
