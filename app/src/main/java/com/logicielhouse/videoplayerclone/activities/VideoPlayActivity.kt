package com.logicielhouse.videoplayerclone.activities

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.logicielhouse.videoplayerclone.R
import com.logicielhouse.videoplayerclone.services.FloatingWidgetService
import kotlinx.android.synthetic.main.activity_video_play.*
import kotlinx.android.synthetic.main.layout_exoplayer_controller.*

class VideoPlayActivity : AppCompatActivity() {

    private var videoURI: String = ""
    private var player: SimpleExoPlayer? = null
    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition: Long = 0
    private val bandwidthMeter = DefaultBandwidthMeter()
    private val loadControl = DefaultLoadControl()
    private var trackSelector = DefaultTrackSelector(
        AdaptiveTrackSelection.Factory(bandwidthMeter)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_play)
        if (intent != null) {
            videoURI = intent.getStringExtra("video") ?: ""
            currentWindow = intent.getIntExtra("currWindow", 0)
            playbackPosition = intent.getLongExtra("currPosition", 0L)
        }

        exo_floating_widget.setOnClickListener {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                when {
                    Settings.canDrawOverlays(this) -> {
                        startService()
                    }
                    else -> {
                        askPermission()
                        Toast.makeText(
                            this,
                            "You need System Alert Window Permission to do this",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                startService()
            }


        }

        initializePlayer()
    }

    private fun askPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, 100)
        } else {
            startService()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            val serviceIntent = Intent(this, FloatingWidgetService::class.java)
            serviceIntent.putExtra("videoURI", videoURI)
            startService()
        } else {
            Toast.makeText(
                this,
                "Draw over other app permission not available.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun startService() {
        val serviceIntent = Intent(this, FloatingWidgetService::class.java)
        serviceIntent.putExtra("videoURI", videoURI)
        serviceIntent.putExtra("currWindow", player?.currentWindowIndex)
        serviceIntent.putExtra("currPosition", player?.currentPosition)
        startService(serviceIntent)
        releasePlayer()
        finishAffinity()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun initializePlayer() {

        player = ExoPlayerFactory.newSimpleInstance(
            DefaultRenderersFactory(this),
            trackSelector, loadControl
        )
        exoplayerView.player = player

        val uri = Uri.parse(videoURI)
        val mediaSource = buildMediaSource(uri)
        player?.playWhenReady = true
        player?.seekTo(currentWindow, playbackPosition)
        player?.prepare(mediaSource, false, false)

    }

    override fun onPause() {
        super.onPause()
        releasePlayer()
    }

    override fun onResume() {
        super.onResume()
        initializePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }


    private fun buildMediaSource(uri: Uri): MediaSource? {
        val dataSourceFactory: DataSource.Factory =
            DefaultDataSourceFactory(this, "exoplayer-clone", bandwidthMeter)
        return ExtractorMediaSource.Factory(dataSourceFactory)
            .createMediaSource(uri)
    }

    private fun releasePlayer() {
        if (player != null) {
            playWhenReady = false
            playbackPosition = player!!.currentPosition
            currentWindow = player!!.currentWindowIndex
            player!!.release()
            player = null
        }
    }
}