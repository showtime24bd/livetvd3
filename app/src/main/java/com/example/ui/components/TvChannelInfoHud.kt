package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Channel
import com.example.model.EpgResponse
import com.example.ui.theme.TvBackgroundDark
import com.example.ui.theme.TvCyanAccent
import com.example.ui.theme.TvEmeraldAccent
import com.example.ui.theme.TvLiveRed
import com.example.ui.theme.TvSurfaceDark
import com.example.ui.theme.TvSurfaceElevated
import com.example.ui.theme.TvTextMuted
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary

@Composable
fun TvChannelInfoHud(
    isVisible: Boolean,
    channel: Channel?,
    epgData: EpgResponse?,
    isBuffering: Boolean,
    isPlaying: Boolean,
    isMuted: Boolean,
    isUsingBackup: Boolean,
    qualityLabel: String = "Auto",
    estimatedBitrateBps: Long = 0L,
    onToggleMute: () -> Unit,
    onCycleResize: () -> Unit,
    onOpenQuality: () -> Unit,
    onOpenSports: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible && channel != null,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
        modifier = modifier
    ) {
        if (channel == null) return@AnimatedVisibility

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp, vertical = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                TvSurfaceDark.copy(alpha = 0.92f),
                                TvBackgroundDark.copy(alpha = 0.98f)
                            )
                        )
                    )
                    .border(
                        width = 1.2.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(TvCyanAccent.copy(alpha = 0.6f), Color(0x22FFFFFF))
                        ),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .padding(horizontal = 24.dp, vertical = 18.dp)
            ) {
                Column {
                    // Top Row: Channel Info and Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Channel Number
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(TvCyanAccent)
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "CH ${channel.displayNumber}",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            if (!channel.logoUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = channel.logoUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            }

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = channel.cleanName,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TvTextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    // Stream format pill (M3U8 / TS / MP4)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0x33FFFFFF))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = channel.streamFormatDisplay,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TvTextSecondary
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = channel.cleanCategory,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TvCyanAccent
                                    )
                                    if (isUsingBackup) {
                                        Text(
                                            text = " • Backup Stream Active",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFFD54F)
                                        )
                                    }
                                }
                            }
                        }

                        // Right Status Badges & Quick Action Controls
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Mute button
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(TvSurfaceElevated)
                                    .clickable { onToggleMute() }
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                                    contentDescription = "Mute",
                                    tint = if (isMuted) Color(0xFFFFB300) else TvTextPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))

                            // Aspect ratio button
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(TvSurfaceElevated)
                                    .clickable { onCycleResize() }
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AspectRatio,
                                    contentDescription = "Aspect Ratio",
                                    tint = TvTextPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))

                            // Sports Schedule button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0x3300E5FF))
                                    .border(1.dp, TvCyanAccent.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                    .clickable { onOpenSports() }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = "Matches",
                                        tint = TvCyanAccent,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Matches",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TvCyanAccent
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))

                            // Quality Badge / Button
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0x3300E5FF))
                                    .border(1.dp, TvCyanAccent.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                    .clickable { onOpenQuality() }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HighQuality,
                                    contentDescription = null,
                                    tint = TvCyanAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = qualityLabel.uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TvCyanAccent
                                )
                                if (estimatedBitrateBps > 0) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    val mbps = estimatedBitrateBps.toFloat() / 1_000_000f
                                    Text(
                                        text = "• %.1fM".format(mbps),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TvEmeraldAccent
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))

                            // Buffering / Paused indicator
                            if (isBuffering) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0x33FFB300))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFFB300))
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "BUFFERING",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFB300)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            // Pulsing LIVE badge
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(TvLiveRed.copy(alpha = 0.2f))
                                    .border(1.dp, TvLiveRed.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(TvLiveRed)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "LIVE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TvLiveRed
                                )
                            }
                        }
                    }

                    // Middle Row: Live EPG Program Schedule (Now Playing & Upcoming)
                    val currentProg = epgData?.current
                    val upcomingProg = epgData?.upcoming?.firstOrNull()

                    if (currentProg != null || upcomingProg != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x22000000))
                                .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(10.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Column {
                                if (currentProg != null) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Schedule,
                                                contentDescription = null,
                                                tint = TvCyanAccent,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "NOW PLAYING: ",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TvCyanAccent
                                            )
                                            Text(
                                                text = currentProg.title,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        if (currentProg.startFmt.isNotBlank()) {
                                            Text(
                                                text = "${currentProg.startFmt} - ${currentProg.stopFmt}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = TvEmeraldAccent
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = { 0.45f },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(3.dp)
                                            .clip(RoundedCornerShape(2.dp)),
                                        color = TvCyanAccent,
                                        trackColor = Color(0x33FFFFFF)
                                    )
                                }

                                if (upcomingProg != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "UP NEXT: ",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TvTextMuted
                                        )
                                        Text(
                                            text = "${upcomingProg.startFmt} ${upcomingProg.title}",
                                            fontSize = 11.sp,
                                            color = TvTextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Remote Navigation Shortcut Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(
                                text = "▲ / ▼ Next/Prev",
                                fontSize = 11.sp,
                                color = TvTextSecondary
                            )
                            Text(
                                text = "◀ Guide",
                                fontSize = 11.sp,
                                color = TvTextSecondary
                            )
                            Text(
                                text = "▶ Sports Matches",
                                fontSize = 11.sp,
                                color = TvCyanAccent,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "0-9 Direct Channel",
                                fontSize = 11.sp,
                                color = TvEmeraldAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "StreamingZone TV",
                            fontSize = 10.sp,
                            color = TvTextMuted
                        )
                    }
                }
            }
        }
    }
}
