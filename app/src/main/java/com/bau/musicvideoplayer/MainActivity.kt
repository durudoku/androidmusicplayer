package com.bau.musicvideoplayer

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.widget.Button
import android.widget.ArrayAdapter
import android.widget.ListView

class MainActivity : AppCompatActivity() {
    private lateinit var listView: ListView
    private val songs = arrayOf("song1", "song2", "song3")  // Names of the songs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listView = findViewById(R.id.lvSongs)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, songs)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedSong = songs[position]
            playSong(selectedSong)
        }

        val btnPlay: Button = findViewById(R.id.btnPlay)
        btnPlay.setOnClickListener {
            if (MediaPlayerService.PreferencesUtil.isMediaPaused(this)) {
                val intent = Intent(this, MediaPlayerService::class.java)
                intent.action = MediaPlayerService.ACTION_RESUME
                startService(intent)
            }
        }

        val btnPause: Button = findViewById(R.id.btnPause)
        btnPause.setOnClickListener {
            val intent = Intent(this, MediaPlayerService::class.java)
            intent.action = MediaPlayerService.ACTION_PAUSE
            startService(intent)
        }

        val btnStop: Button = findViewById(R.id.btnStop)
        btnStop.setOnClickListener {
            val intent = Intent(this, MediaPlayerService::class.java)
            intent.action = MediaPlayerService.ACTION_STOP
            startService(intent)
        }



    }

    private fun playSong(songName: String) {
        val resId = resources.getIdentifier(songName, "raw", packageName)
        Intent(this, MediaPlayerService::class.java).also { intent ->
            intent.putExtra("resId", resId)
            startService(intent)
        }
    }
}
/*
    btnPlay.setOnClickListener {
        // Start the MediaPlayerService
        startService(Intent(this, MediaPlayerService::class.java))
    }
    btnPause.setOnClickListener {
        // You could implement pause functionality here
    }
    btnStop.setOnClickListener {
        // Stop the MediaPlayerService
        stopService(Intent(this, MediaPlayerService::class.java))
    }
*/