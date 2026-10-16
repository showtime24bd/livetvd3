package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.TvChannelDrawer
import com.example.ui.components.TvChannelInfoHud
import com.example.ui.components.TvNumberInputOverlay
import com.example.ui.components.TvQualityDialog
import com.example.ui.components.TvQuickRemoteControl
import com.example.ui.components.TvSportsScheduleDrawer
import com.example.ui.components.TvVideoPlayer
import com.example.ui.theme.TvBackgroundDark
import com.example.ui.theme.TvCyanAccent
import com.example.ui.theme.TvSurfaceDark

@Composable
fun TvMainScreen(
    viewModel: TvPlayerViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = null,
                indication = null
            ) {
                when {
                    uiState.isQualityDialogVisible -> viewModel.closeQualityDialog()
                    uiState.isSportsDrawerOpen -> viewModel.closeSportsDrawer()
                    uiState.isDrawerOpen -> viewModel.closeDrawer()
                    else -> viewModel.toggleInfoHud()
                }
            }
    ) {
        // Fullscreen adaptive video layer
        TvVideoPlayer(
            channel = uiState.currentChannel,
            streamUrl = uiState.currentStreamUrl,
            isPlaying = uiState.isPlaying,
            isMuted = uiState.isMuted,
            resizeMode = uiState.resizeMode,
            playbackError = uiState.playbackError,
            retryCount = uiState.retryCount,
            onBufferingChanged = { isBuffering -> viewModel.setBuffering(isBuffering) },
            onPlayerError = { errorMsg -> viewModel.onPlayerError(errorMsg) },
            onBandwidthEstimate = { bps -> viewModel.onBandwidthEstimate(bps) },
            onRetry = { viewModel.retryPlayback() },
            modifier = Modifier.fillMaxSize()
        )

        // Loading spinner for initial channels fetch
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(TvBackgroundDark.copy(alpha = 0.88f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = TvCyanAccent,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Connecting to Live Channels...",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "StreamingZone TV",
                        color = TvCyanAccent,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Network error overlay
        if (uiState.networkError != null && !uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(TvBackgroundDark),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .padding(32.dp)
                        .background(TvSurfaceDark, RoundedCornerShape(16.dp))
                        .padding(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Connection Error",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.networkError ?: "Failed to load channel list",
                            fontSize = 14.sp,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { viewModel.loadChannels() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TvCyanAccent,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier.testTag("retry_channels_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Retry Connection", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Live stream buffering indicator
        AnimatedVisibility(
            visible = uiState.isBuffering && !uiState.isLoading && uiState.playbackError == null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .background(Color(0x99000000), RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = TvCyanAccent,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Adapting & Buffering...",
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Bottom Channel Info HUD OSD (with live EPG program schedule)
        TvChannelInfoHud(
            isVisible = uiState.isInfoHudVisible && !uiState.isDrawerOpen && !uiState.isSportsDrawerOpen && !uiState.isQualityDialogVisible,
            channel = uiState.currentChannel,
            epgData = uiState.currentEpg,
            isBuffering = uiState.isBuffering,
            isPlaying = uiState.isPlaying,
            isMuted = uiState.isMuted,
            isUsingBackup = uiState.isUsingBackupUrl,
            qualityLabel = uiState.selectedQualityLabel,
            estimatedBitrateBps = uiState.networkBitrateEstimate,
            onToggleMute = { viewModel.toggleMute() },
            onCycleResize = { viewModel.cycleResizeMode() },
            onOpenQuality = { viewModel.toggleQualityDialog() },
            onOpenSports = { viewModel.toggleSportsDrawer() },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Top-Right TV Remote Number Input Overlay
        TvNumberInputOverlay(
            state = uiState.numberInput,
            modifier = Modifier.align(Alignment.TopEnd)
        )

        // Left-Side Channel Guide Drawer Overlay (with search & categories)
        TvChannelDrawer(
            isOpen = uiState.isDrawerOpen,
            channels = uiState.filteredChannels,
            categories = uiState.categories,
            selectedCategory = uiState.selectedCategory,
            currentChannel = uiState.currentChannel,
            searchQuery = uiState.searchQuery,
            onSearchQueryChange = { q -> viewModel.onSearchQueryChange(q) },
            onSelectChannel = { channel -> viewModel.playChannel(channel) },
            onSelectCategory = { cat -> viewModel.selectCategory(cat) },
            onClose = { viewModel.closeDrawer() },
            modifier = Modifier.align(Alignment.CenterStart)
        )

        // Right-Side Live Sports & Match Schedule Drawer
        TvSportsScheduleDrawer(
            isOpen = uiState.isSportsDrawerOpen,
            events = uiState.featuredSports,
            channels = uiState.channels,
            onSelectEventChannel = { channel -> viewModel.playChannel(channel) },
            onClose = { viewModel.closeSportsDrawer() },
            modifier = Modifier.align(Alignment.CenterEnd)
        )

        // Quality & Adaptive Speed Selection Dialog
        TvQualityDialog(
            isVisible = uiState.isQualityDialogVisible,
            availableQualities = uiState.availableQualities,
            selectedQuality = uiState.selectedQualityLabel,
            estimatedBitrateBps = uiState.networkBitrateEstimate,
            onSelectQuality = { q -> viewModel.selectQuality(q) },
            onDismiss = { viewModel.closeQualityDialog() }
        )

        // Top-Left On-Screen Quick Remote Control (for emulator touch or quick mouse testing)
        TvQuickRemoteControl(
            isPlaying = uiState.isPlaying,
            isMuted = uiState.isMuted,
            onChannelUp = { viewModel.nextChannel() },
            onChannelDown = { viewModel.previousChannel() },
            onToggleDrawer = { viewModel.toggleDrawer() },
            onTogglePlayPause = { viewModel.togglePlayPause() },
            onToggleMute = { viewModel.toggleMute() },
            onToggleSports = { viewModel.toggleSportsDrawer() },
            onNumberClick = { digit -> viewModel.onNumberKeyPressed(digit) },
            onToggleHud = { viewModel.toggleInfoHud() },
            onCycleResize = { viewModel.cycleResizeMode() },
            onOpenQuality = { viewModel.toggleQualityDialog() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 24.dp, top = 24.dp)
        )
    }
}
