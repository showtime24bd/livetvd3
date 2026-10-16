package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.NumberInputState
import com.example.ui.theme.TvBackgroundDark
import com.example.ui.theme.TvBorderFocused
import com.example.ui.theme.TvCyanAccent
import com.example.ui.theme.TvLiveRed
import com.example.ui.theme.TvTextMuted
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary

@Composable
fun TvNumberInputOverlay(
    state: NumberInputState,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = state.isVisible,
        enter = fadeIn() + slideInHorizontally(initialOffsetX = { it }),
        exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it }),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .padding(top = 28.dp, end = 28.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(TvBackgroundDark.copy(alpha = 0.94f))
                .border(2.dp, TvBorderFocused, RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = null,
                        tint = TvCyanAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "JUMP TO CHANNEL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = TvTextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Big typed number
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "CH ",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TvTextMuted
                    )
                    Text(
                        text = state.digits,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = TvCyanAccent
                    )
                }

                // Matching channel preview or not found
                if (state.matchingChannel != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = state.matchingChannel.cleanName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TvTextPrimary
                    )
                    Text(
                        text = state.matchingChannel.cleanCategory,
                        fontSize = 12.sp,
                        color = TvCyanAccent
                    )
                } else if (state.notFoundMessage != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = state.notFoundMessage,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TvLiveRed
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Progress countdown bar
                val progress = (state.countdownRemainingMs.toFloat() / 1500f).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .width(160.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = TvCyanAccent,
                    trackColor = Color(0x33FFFFFF)
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Press OK or wait to switch",
                    fontSize = 11.sp,
                    color = TvTextMuted
                )
            }
        }
    }
}
