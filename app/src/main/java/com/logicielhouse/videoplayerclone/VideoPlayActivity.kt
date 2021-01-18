package com.logicielhouse.videoplayerclone

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_video_play.*

class VideoPlayActivity : AppCompatActivity(), Player.EventListener {

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
        if (intent!= null){
            videoURI = intent.getStringExtra("video")!!
        }
    }

    private fun initializePlayer() {

        player = ExoPlayerFactory.newSimpleInstance(
            DefaultRenderersFactory(this),
            trackSelector, loadControl
        )
        exoplayerView.player = player

        val uri = Uri.parse(videoURI)
        val mediaSource = buildMediaSource(uri)
        player?.playWhenReady = false
        player?.seekTo(currentWindow, playbackPosition)
        player?.prepare(mediaSource, false, false)

        player?.addListener(this)
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT >= 24) {
            initializePlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT < 24) {
            releasePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        if ((Util.SDK_INT < 24 || player == null)) {
            initializePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT >= 24) {
            releasePlayer()
        }
    }

    private fun buildMediaSource(uri: Uri): MediaSource? {
        val dataSourceFactory: DataSource.Factory =
            DefaultDataSourceFactory(this, "exoplayer-clone", bandwidthMeter)
        return ExtractorMediaSource.Factory(dataSourceFactory)
            .createMediaSource(uri)
    }

    private fun releasePlayer() {
        if (player != null) {
            playWhenReady = player!!.playWhenReady
            playbackPosition = player!!.currentPosition
            currentWindow = player!!.currentWindowIndex
            player!!.release()
            player = null
        }
    }

    override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {
        
    }

    override fun onTracksChanged(
        trackGroups: TrackGroupArray?,
        trackSelections: TrackSelectionArray?
    ) {
        
    }

    override fun onLoadingChanged(isLoading: Boolean) {
        
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
        
    }

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
        
    }

    override fun onPlayerError(error: ExoPlaybackException?) {
        Log.e("TAG", "onPlayerError: ${error?.localizedMessage}" )
    }

    override fun onPositionDiscontinuity(reason: Int) {
        
    }

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
        
    }

    override fun onSeekProcessed() {
        
    }
}