package com.bau.musicvideoplayer

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.media.AudioManager
import android.media.AudioFocusRequest
import android.media.AudioAttributes
import android.os.Build


class MediaPlayerService : Service() {

    private var mediaPlayer: MediaPlayer? = null

    companion object {
        const val ACTION_PLAY = "ACTION_PLAY"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_STOP = "ACTION_STOP"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                playMusic(intent.extras?.getInt("resId"))
                PreferencesUtil.setMediaPaused(this, false)
            }
            ACTION_PAUSE -> {
                mediaPlayer?.pause()
                PreferencesUtil.setMediaPaused(this, true)
            }
            ACTION_RESUME -> {
                mediaPlayer?.start()
                PreferencesUtil.setMediaPaused(this, false)
            }
            ACTION_STOP -> {
                stopMusic()
                PreferencesUtil.setMediaPaused(this, false)
            }
        }
        return START_STICKY
    }

    private fun playMusic(resId: Int?) {
        resId?.let {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(this, it)
            mediaPlayer?.start()
        }
    }
    private fun stopMusic() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onCreate() {
        super.onCreate()
        // Initialize MediaPlayer
        mediaPlayer = MediaPlayer.create(this, R.raw.song1) // Ensure you have a sample_audio.mp3 in your res/raw folder
        mediaPlayer?.isLooping = true // Set looping
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private val focusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                // Permanent loss of audio focus
                mediaPlayer?.pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // Pause playback temporarily and expect to resume
                mediaPlayer?.pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // Lower the volume if applicable
                mediaPlayer?.setVolume(0.1f, 0.1f)
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                // Resume playback
                mediaPlayer?.start()
                mediaPlayer?.setVolume(1.0f, 1.0f)
            }
        }
    }
    private fun requestAudioFocus(): Boolean {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()

            val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(audioAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(focusChangeListener)
                .build()

            audioManager.requestAudioFocus(focusRequest)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(focusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        }
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun abandonAudioFocus() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(audioAttributes)
                .setOnAudioFocusChangeListener(focusChangeListener)
                .build()

            audioManager.abandonAudioFocusRequest(focusRequest)
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(focusChangeListener)
        }
    }


    override fun onDestroy() {
        abandonAudioFocus()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }

    object PreferencesUtil {
        private const val PREF_NAME = "MediaPlayerPreferences"
        private const val KEY_IS_PAUSED = "isPaused"

        fun setMediaPaused(context: Context, isPaused: Boolean) {
            val editor = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit()
            editor.putBoolean(KEY_IS_PAUSED, isPaused)
            editor.apply()
        }

        fun isMediaPaused(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_IS_PAUSED, false)
        }
    }





}


