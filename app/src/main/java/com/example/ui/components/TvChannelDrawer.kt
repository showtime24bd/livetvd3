package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Channel
import com.example.ui.theme.TvBackgroundDark
import com.example.ui.theme.TvBorderFocused
import com.example.ui.theme.TvBorderUnfocused
import com.example.ui.theme.TvCardBackground
import com.example.ui.theme.TvCyanAccent
import com.example.ui.theme.TvLiveRed
import com.example.ui.theme.TvSurfaceDark
import com.example.ui.theme.TvSurfaceElevated
import com.example.ui.theme.TvTextMuted
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary

@Composable
fun TvChannelDrawer(
    isOpen: Boolean,
    channels: List<Channel>,
    categories: List<String>,
    selectedCategory: String,
    currentChannel: Channel?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSelectChannel: (Channel) -> Unit,
    onSelectCategory: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isOpen,
        enter = slideInHorizontally(initialOffsetX = { -it }),
        exit = slideOutHorizontally(targetOffsetX = { -it }),
        modifier = modifier
    ) {
        val listState = rememberLazyListState()

        // Auto-scroll to active channel when drawer opens
        LaunchedEffect(isOpen, currentChannel) {
            if (isOpen && currentChannel != null) {
                val index = channels.indexOfFirst { it.id == currentChannel.id }
                if (index >= 0) {
                    listState.animateScrollToItem(index)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(420.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            TvBackgroundDark.copy(alpha = 0.98f),
                            TvSurfaceDark.copy(alpha = 0.94f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(Color(0x3300E5FF), Color(0x0AFFFFFF))
                    ),
                    shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
                )
                .padding(top = 24.dp, bottom = 16.dp, start = 20.dp, end = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxHeight()) {
                // Header with title and close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(TvCyanAccent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tv,
                                contentDescription = null,
                                tint = TvCyanAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Live TV Channels",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TvTextPrimary
                            )
                            Text(
                                text = "${channels.size} Channels Available",
                                fontSize = 12.sp,
                                color = TvTextMuted
                            )
                        }
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("close_drawer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Channel List",
                            tint = TvTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(TvSurfaceElevated)
                        .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TvCyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search channel by name or #...",
                                    fontSize = 13.sp,
                                    color = TvTextMuted
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = onSearchQueryChange,
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(TvCyanAccent),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        if (searchQuery.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = TvTextSecondary,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { onSearchQueryChange("") }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Category selector pills
                if (categories.size > 1) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { category ->
                            val isSelected = category == selectedCategory
                            val interactionSource = remember { MutableInteractionSource() }
                            val isFocused by interactionSource.collectIsFocusedAsState()

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = when {
                                    isFocused -> TvCyanAccent
                                    isSelected -> TvCyanAccent.copy(alpha = 0.2f)
                                    else -> TvSurfaceElevated
                                },
                                border = if (isFocused || isSelected) {
                                    androidx.compose.foundation.BorderStroke(1.dp, TvCyanAccent)
                                } else null,
                                modifier = Modifier
                                    .focusable(interactionSource = interactionSource)
                                    .clickable { onSelectCategory(category) }
                            ) {
                                Text(
                                    text = category,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium,
                                    color = when {
                                        isFocused -> Color.Black
                                        isSelected -> TvCyanAccent
                                        else -> TvTextSecondary
                                    },
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Channels list
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(channels, key = { it.id }) { channel ->
                        val isPlaying = channel.id == currentChannel?.id
                        TvChannelListItem(
                            channel = channel,
                            isPlaying = isPlaying,
                            onClick = { onSelectChannel(channel) }
                        )
                    }
                }

                // Remote navigation helper hints at the footer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "OK: Select • ◀ / BACK: Close",
                        fontSize = 12.sp,
                        color = TvTextMuted
                    )
                    Text(
                        text = "Direct: 0-9",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TvCyanAccent
                    )
                }
            }
        }
    }
}

@Composable
private fun TvChannelListItem(
    channel: Channel,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val scale = if (isFocused) 1.03f else 1f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    isFocused -> TvSurfaceElevated
                    isPlaying -> TvCyanAccent.copy(alpha = 0.12f)
                    else -> TvCardBackground
                }
            )
            .border(
                width = if (isFocused) 2.dp else if (isPlaying) 1.5.dp else 1.dp,
                color = when {
                    isFocused -> TvBorderFocused
                    isPlaying -> TvCyanAccent.copy(alpha = 0.6f)
                    else -> TvBorderUnfocused
                },
                shape = RoundedCornerShape(12.dp)
            )
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("channel_item_${channel.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Channel Number Badge
            Box(
                modifier = Modifier
                    .size(width = 46.dp, height = 36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isFocused) TvCyanAccent else TvSurfaceDark
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${channel.displayNumber}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isFocused) Color.Black else TvCyanAccent
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Channel Logo if available, otherwise Icon
            if (!channel.logoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = channel.logoUrl,
                    contentDescription = channel.cleanName,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(6.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))
            }

            // Channel Name & Category & Stream format badge
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = channel.cleanName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isFocused) Color.White else TvTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = channel.cleanCategory,
                        fontSize = 12.sp,
                        color = if (isFocused) TvCyanAccent else TvTextSecondary
                    )
                    Text(
                        text = " • ${channel.streamFormatDisplay}",
                        fontSize = 11.sp,
                        color = TvTextMuted
                    )
                }
            }

            // Playing Indicator
            if (isPlaying) {
                Spacer(modifier = Modifier.width(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(TvLiveRed.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(TvLiveRed)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "PLAYING",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = TvLiveRed
                    )
                }
            } else if (isFocused) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = TvCyanAccent,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
