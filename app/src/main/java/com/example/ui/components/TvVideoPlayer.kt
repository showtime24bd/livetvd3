package com.example.ui.components

import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.AdaptiveTrackSelection
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.upstream.BandwidthMeter
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter
import androidx.media3.ui.PlayerView
import com.example.model.Channel
import com.example.ui.theme.TvCyanAccent
import com.example.ui.theme.TvSurfaceDark

@OptIn(UnstableApi::class)
@Composable
fun TvVideoPlayer(
    channel: Channel?,
    streamUrl: String,
    isPlaying: Boolean,
    isMuted: Boolean,
    resizeMode: Int,
    playbackError: String?,
    retryCount: Int,
    onBufferingChanged: (Boolean) -> Unit,
    onPlayerError: (String) -> Unit,
    onBandwidthEstimate: (Long) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val bandwidthMeter = remember {
        DefaultBandwidthMeter.Builder(context).build()
    }

    val trackSelector = remember {
        val adaptiveTrackSelectionFactory = AdaptiveTrackSelection.Factory()
        DefaultTrackSelector(context, adaptiveTrackSelectionFactory).apply {
            parameters = buildUponParameters()
                .setForceHighestSupportedBitrate(false)
                .build()
        }
    }

    val exoPlayer = remember {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15000)
            .setReadTimeoutMs(15000)
            .setTransferListener(bandwidthMeter)
            .setUserAgent(
                channel?.userAgent?.takeIf { it.isNotBlank() }
                    ?: "Mozilla/5.0 (Linux; Android TV; Build/PPR1.180610.011) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
            )

        val headers = mutableMapOf<String, String>()
        if (!channel?.referer.isNullOrBlank()) {
            val referer = channel?.referer?.trim() ?: ""
            headers["Referer"] = referer
            headers["Origin"] = referer.trimEnd('/')
        }
        if (headers.isNotEmpty()) {
            httpDataSourceFactory.setDefaultRequestProperties(headers)
        }

        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(httpDataSourceFactory)

        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .setTrackSelector(trackSelector)
            .setBandwidthMeter(bandwidthMeter)
            .setSeekBackIncrementMs(10000)
            .setSeekForwardIncrementMs(10000)
            .build()
            .apply {
                playWhenReady = true
                repeatMode = Player.REPEAT_MODE_OFF
                volume = if (isMuted) 0f else 1f
            }
    }

    DisposableEffect(exoPlayer, bandwidthMeter) {
        val bandwidthListener = BandwidthMeter.EventListener { _, _, bitrateEstimate ->
            onBandwidthEstimate(bitrateEstimate)
        }
        bandwidthMeter.addEventListener(Handler(Looper.getMainLooper()), bandwidthListener)

        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> onBufferingChanged(true)
                    Player.STATE_READY -> onBufferingChanged(false)
                    Player.STATE_ENDED -> onBufferingChanged(false)
                    Player.STATE_IDLE -> onBufferingChanged(false)
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                onBufferingChanged(false)
                val msg = error.localizedMessage ?: "Playback error code ${error.errorCodeName}"
                onPlayerError(msg)
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            bandwidthMeter.removeEventListener(bandwidthListener)
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    LaunchedEffect(isMuted) {
        exoPlayer.volume = if (isMuted) 0f else 1f
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            exoPlayer.play()
        } else {
            exoPlayer.pause()
        }
    }

    LaunchedEffect(streamUrl) {
        if (streamUrl.isNotBlank()) {
            val uri = Uri.parse(streamUrl)
            val mediaItemBuilder = MediaItem.Builder().setUri(uri)

            val lower = streamUrl.lowercase()
            if (lower.contains(".m3u8") || lower.contains("m3u8") || lower.contains("hls")) {
                mediaItemBuilder.setMimeType(MimeTypes.APPLICATION_M3U8)
            } else if (lower.contains(".mpd") || lower.contains("dash")) {
                mediaItemBuilder.setMimeType(MimeTypes.APPLICATION_MPD)
            }

            val currentPos = if (exoPlayer.isCurrentMediaItemLive) 0L else exoPlayer.currentPosition
            exoPlayer.setMediaItem(mediaItemBuilder.build(), currentPos)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = isPlaying
        } else {
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    this.resizeMode = resizeMode
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { playerView ->
                playerView.player = exoPlayer
                playerView.resizeMode = resizeMode
            },
            modifier = Modifier.fillMaxSize()
        )

        // Error message overlay with automatic retry info & button
        AnimatedVisibility(
            visible = playbackError != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .padding(32.dp)
                    .background(TvSurfaceDark.copy(alpha = 0.94f), RoundedCornerShape(16.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Playback Error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = channel?.cleanName ?: "Channel Stream",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = playbackError ?: "Stream issue encountered",
                        fontSize = 14.sp,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center
                    )
                    if (retryCount > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Auto-recovery attempt $retryCount of 3...",
                            fontSize = 12.sp,
                            color = TvCyanAccent
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TvCyanAccent,
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Retry Now", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
