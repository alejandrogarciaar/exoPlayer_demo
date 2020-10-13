package com.jgarcia.exoplayerdemo

import android.app.PictureInPictureParams
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.util.Util
import com.jgarcia.exoplayerdemo.databinding.ActivityLiveStreamingExoPlayerBinding
import java.lang.Exception

class LiveStreamingExoPlayerActivity : AppCompatActivity() {

    companion object {
        private const val LIVE_STREAMING = "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8"
    }

    private lateinit var activityLiveStreamingExoPlayerBinding: ActivityLiveStreamingExoPlayerBinding

    // exoPlayer
    private lateinit var exoPlayer: SimpleExoPlayer
    private lateinit var mediaSource: MediaSource
    private lateinit var bandwidthMeter: BandwidthMeter
    private lateinit var loadControl: LoadControl
    private lateinit var dataSource: DataSource.Factory
    private lateinit var trackSelector: TrackSelector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityLiveStreamingExoPlayerBinding = ActivityLiveStreamingExoPlayerBinding.inflate(layoutInflater)
        setContentView(activityLiveStreamingExoPlayerBinding.root)
        setListeners()
        startStreaming()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        startStreaming()
    }

    private fun setListeners() {
        activityLiveStreamingExoPlayerBinding.ivFullScreen.setOnClickListener {
            resolveOrientation()
        }
    }

    override fun onBackPressed() {
        activityLiveStreamingExoPlayerBinding.playerView.player?.let {
            if (it.isPlaying) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startPictureInPicture()
                } else {
                    super.onBackPressed()
                }
            } else {
                super.onBackPressed()
            }
        } ?: run {
            super.onBackPressed()
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration?) {
        if (isInPictureInPictureMode) {
            supportActionBar?.hide()
            activityLiveStreamingExoPlayerBinding.ivFullScreen.visibility = View.GONE
        } else {
            supportActionBar?.show()
            activityLiveStreamingExoPlayerBinding.ivFullScreen.visibility = View.VISIBLE
        }
    }

    private fun startStreaming() {
        // default track selector
        trackSelector = DefaultTrackSelector(this@LiveStreamingExoPlayerActivity)
        // default bandWidthMeter
        bandwidthMeter = DefaultBandwidthMeter.Builder(this@LiveStreamingExoPlayerActivity).build()
        // default loadControl
        loadControl = DefaultLoadControl.Builder().build()
        // set dataSource
        dataSource = DefaultHttpDataSourceFactory("appDemo", 10000, 10000, true)
        // build exoPlayer
        exoPlayer = SimpleExoPlayer.Builder(this@LiveStreamingExoPlayerActivity)
            .setLoadControl(loadControl)
            .setBandwidthMeter(bandwidthMeter)
            .setTrackSelector(trackSelector)
            .build()
        // set exoPlayer
        activityLiveStreamingExoPlayerBinding.playerView.player = exoPlayer
        // keep screen on
        activityLiveStreamingExoPlayerBinding.playerView.keepScreenOn = true
        // mediaSource
        mediaSource = when (Util.inferContentType(Uri.parse(LIVE_STREAMING))) {
            C.TYPE_DASH -> DashMediaSource.Factory(dataSource)
            C.TYPE_HLS -> HlsMediaSource.Factory(dataSource)
            C.TYPE_SS -> SsMediaSource.Factory(dataSource)
            C.TYPE_OTHER -> ProgressiveMediaSource.Factory(dataSource)
            else -> throw Exception("Uri is not valid")
        }.createMediaSource(MediaItem.fromUri(Uri.parse(LIVE_STREAMING)))
        // set mediaSource
        exoPlayer.addMediaSource(mediaSource)
        // prepare player
        exoPlayer.prepare()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startPictureInPicture() {
        val display = windowManager.defaultDisplay
        val point = Point()
        display.getSize(point)
        val pictureBuilder = PictureInPictureParams.Builder()
        pictureBuilder.setAspectRatio(Rational(point.x, point.y))
        enterPictureInPictureMode(pictureBuilder.build())
    }

    private fun resolveOrientation() {
        val orientation = resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // In landscape
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            activityLiveStreamingExoPlayerBinding.ivFullScreen.setImageDrawable(ContextCompat.getDrawable(this@LiveStreamingExoPlayerActivity, R.drawable.ic_fullscreen))
        } else {
            // In portrait
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            activityLiveStreamingExoPlayerBinding.ivFullScreen.setImageDrawable(ContextCompat.getDrawable(this@LiveStreamingExoPlayerActivity, R.drawable.ic_fullscreen_exit))
        }
    }

    override fun onResume() {
        super.onResume()
        exoPlayer.play()
    }

    override fun onStop() {
        super.onStop()
        exoPlayer.stop()
    }
}