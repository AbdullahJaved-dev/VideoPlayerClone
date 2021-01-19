package com.logicielhouse.videoplayerclone.services

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.ImageView
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.logicielhouse.videoplayerclone.R
import com.logicielhouse.videoplayerclone.activities.VideoPlayActivity

/**
 * Created by Abdullah on 1/19/2021.
 */
class FloatingWidgetService() : Service() {

    private var windowManager: WindowManager? = null
    private var mFloatingWidget: View? = null
    private lateinit var videoURI: Uri

    private var player: SimpleExoPlayer? = null
    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition: Long = 0
    private val bandwidthMeter = DefaultBandwidthMeter()
    private val loadControl = DefaultLoadControl()
    private var trackSelector = DefaultTrackSelector(
        AdaptiveTrackSelection.Factory(bandwidthMeter)
    )
    private lateinit var params: WindowManager.LayoutParams


    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val uri: String = intent.getStringExtra("videoURI")!!
            currentWindow = intent.getIntExtra("currWindow", 0)
            playbackPosition = intent.getLongExtra("currPosition", 0L)
            videoURI = Uri.parse(uri)

            if (windowManager != null && mFloatingWidget?.isShown == true && player != null) {
                windowManager?.removeView(mFloatingWidget)
                mFloatingWidget = null
                windowManager = null
                player?.playWhenReady = false
                player?.release()
                player = null
            }

            mFloatingWidget = LayoutInflater.from(this).inflate(R.layout.custom_popup_window, null)

            params = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
                WindowManager.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
                )
            } else {
                WindowManager.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
                )
            }

            params.gravity = Gravity.TOP or Gravity.LEFT
            params.x = 200
            params.y = 200

            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            windowManager?.addView(mFloatingWidget, params)

            player = ExoPlayerFactory.newSimpleInstance(
                DefaultRenderersFactory(this),
                trackSelector, loadControl
            )

            val ivMaximize: ImageView? = mFloatingWidget?.findViewById(R.id.ivMaximize)

            ivMaximize?.setOnClickListener {
                windowManager?.removeView(mFloatingWidget)
                mFloatingWidget = null
                windowManager = null

                val intent = Intent(this, VideoPlayActivity::class.java)
                intent.putExtra("video", videoURI.toString())
                intent.putExtra("currWindow", player?.currentWindowIndex)
                intent.putExtra("currPosition", player?.currentPosition)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                player?.playWhenReady = false
                player?.release()
                player = null
                stopSelf()
            }
            val ivClose: ImageView? = mFloatingWidget?.findViewById(R.id.ivDismiss)

            ivClose?.setOnClickListener {
                if (windowManager != null && mFloatingWidget != null && player != null) {
                    windowManager?.removeView(mFloatingWidget)
                    mFloatingWidget = null
                    windowManager = null
                    player?.playWhenReady = false
                    player?.release()
                    player = null
                    stopSelf()
                }
            }

            playVideo()
            initializeView()


        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun initializeView() {
        mFloatingWidget!!.findViewById<View>(R.id.rvPopup)
            .setOnTouchListener(object : View.OnTouchListener {
                private var initialX = 0
                private var initialY = 0
                private var initialTouchX = 0f
                private var initialTouchY = 0f
                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            initialX = params.x
                            initialY = params.y
                            initialTouchX = event.rawX
                            initialTouchY = event.rawY
                            return true
                        }
                        MotionEvent.ACTION_UP -> {
                            //when the drag is ended switching the state of the widget
                            /*collapsedView?.visibility = View.GONE
                            expandedView?.visibility = View.VISIBLE*/
                            return true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            //this code is helping the widget to move around the screen with fingers
                            params.x = initialX + (event.rawX - initialTouchX).toInt()
                            params.y = initialY + (event.rawY - initialTouchY).toInt()
                            windowManager!!.updateViewLayout(mFloatingWidget, params)
                            return true
                        }
                    }
                    return false
                }
            })
    }

    private fun playVideo() {
        val exoplayer: PlayerView? = mFloatingWidget?.findViewById(R.id.exoplayerView)
        exoplayer?.player = player

        val mediaSource = buildMediaSource(videoURI)
        player?.playWhenReady = true
        player?.seekTo(currentWindow, playbackPosition)
        player?.prepare(mediaSource, false, false)

    }

    private fun buildMediaSource(uri: Uri): MediaSource? {
        val dataSourceFactory: DataSource.Factory =
            DefaultDataSourceFactory(this, "exoplayer-clone", bandwidthMeter)
        return ExtractorMediaSource.Factory(dataSourceFactory)
            .createMediaSource(uri)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mFloatingWidget != null) {
            windowManager?.removeView(mFloatingWidget)
        }
    }
}