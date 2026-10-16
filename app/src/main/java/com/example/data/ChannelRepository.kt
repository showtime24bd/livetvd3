package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.Channel
import com.example.model.EpgProgram
import com.example.model.EpgResponse
import com.example.model.FeaturedSportsEvent
import com.example.model.StreamQuality
import com.example.network.ChannelApiService
import com.example.network.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ChannelRepository(
    private val context: Context,
    private val apiService: ChannelApiService = NetworkClient.apiService
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("tv_player_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LAST_CHANNEL_ID = "last_channel_id"
    }

    private var cachedChannels: List<Channel> = emptyList()

    suspend fun fetchChannels(): Result<List<Channel>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getChannels()
            val filtered = response.filter { (it.isActive == null || it.isActive == true) && it.streamUrl.isNotBlank() }
                .sortedWith(compareBy({ it.order ?: 0 }, { it.id }))
            if (filtered.isNotEmpty()) {
                cachedChannels = filtered
                return@withContext Result.success(filtered)
            }
        } catch (_: Exception) {
            // Fall back to cached or default curated channels
        }

        if (cachedChannels.isNotEmpty()) {
            Result.success(cachedChannels)
        } else {
            val defaults = getDefaultChannels()
            cachedChannels = defaults
            Result.success(defaults)
        }
    }

    suspend fun fetchEpg(channel: Channel): EpgResponse = withContext(Dispatchers.IO) {
        try {
            val resp = apiService.getEpg(channel.id)
            if (resp.current != null || resp.upcoming.isNotEmpty()) {
                return@withContext resp
            }
        } catch (_: Exception) {
            // Fall back to generated local EPG based on channel and Bangladesh Time
        }
        generateFallbackEpg(channel)
    }

    fun getFeaturedSportsEvents(): List<FeaturedSportsEvent> {
        val bdTz = TimeZone.getTimeZone("Asia/Dhaka")
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).apply { timeZone = bdTz }
        val now = Date()

        return listOf(
            FeaturedSportsEvent(
                id = 1,
                channelId = 1,
                channelName = "Star Sports 1 HD",
                channelLogo = "https://images.unsplash.com/photo-1579952363873-27f3bade9f55?w=200",
                eventTitle = "Arsenal vs Chelsea",
                eventDescription = "Premier League Super Sunday live derby clash with multi-cam coverage and studio analysis",
                thumbnailUrl = "https://images.unsplash.com/photo-1574629810360-7efbbe195018?w=800",
                startTime = sdf.format(Date(now.time - 30 * 60 * 1000L)),
                endTime = sdf.format(Date(now.time + 90 * 60 * 1000L)),
                category = "Football",
                leagueTournament = "Premier League",
                teamsPlaying = "Arsenal vs Chelsea",
                isActive = true,
                order = 1
            ),
            FeaturedSportsEvent(
                id = 2,
                channelId = 2,
                channelName = "Star Sports 2 HD",
                channelLogo = "https://images.unsplash.com/photo-1540747913346-19e32dc3e97e?w=200",
                eventTitle = "Bangladesh vs India - 2nd T20I",
                eventDescription = "Bilateral international T20 clash live in Mirpur with live ball-by-ball commentary",
                thumbnailUrl = "https://images.unsplash.com/photo-1531415074868-036b107e775a?w=800",
                startTime = sdf.format(Date(now.time - 15 * 60 * 1000L)),
                endTime = sdf.format(Date(now.time + 150 * 60 * 1000L)),
                category = "Cricket",
                leagueTournament = "T20 International Series",
                teamsPlaying = "Bangladesh vs India",
                isActive = true,
                order = 2
            ),
            FeaturedSportsEvent(
                id = 3,
                channelId = 3,
                channelName = "Willow Cricket HD",
                channelLogo = "https://images.unsplash.com/photo-1531415074868-036b107e775a?w=200",
                eventTitle = "IPL 2026: CSK vs Mumbai Indians",
                eventDescription = "Indian Premier League blockbuster rivalry live with Dugout tactical analysis",
                thumbnailUrl = "https://images.unsplash.com/photo-1540747913346-19e32dc3e97e?w=800",
                startTime = sdf.format(Date(now.time + 45 * 60 * 1000L)),
                endTime = sdf.format(Date(now.time + 240 * 60 * 1000L)),
                category = "Cricket",
                leagueTournament = "IPL 2026",
                teamsPlaying = "Chennai Super Kings vs Mumbai Indians",
                isActive = true,
                order = 3
            ),
            FeaturedSportsEvent(
                id = 4,
                channelId = 5,
                channelName = "T Sports Live",
                channelLogo = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=200",
                eventTitle = "Real Madrid vs Barcelona - El Clásico",
                eventDescription = "La Liga giant clash live from Santiago Bernabéu with multi-audio commentary",
                thumbnailUrl = "https://images.unsplash.com/photo-1579952363873-27f3bade9f55?w=800",
                startTime = sdf.format(Date(now.time + 120 * 60 * 1000L)),
                endTime = sdf.format(Date(now.time + 240 * 60 * 1000L)),
                category = "Football",
                leagueTournament = "La Liga Santander",
                teamsPlaying = "Real Madrid vs Barcelona",
                isActive = true,
                order = 4
            )
        )
    }

    private fun generateFallbackEpg(channel: Channel): EpgResponse {
        val bdTz = TimeZone.getTimeZone("Asia/Dhaka")
        val timeFmt = SimpleDateFormat("HH:mm", Locale.US).apply { timeZone = bdTz }
        val isoFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply { timeZone = bdTz }
        val now = Date()

        val startLive = Date(now.time - 25 * 60 * 1000L)
        val stopLive = Date(now.time + 65 * 60 * 1000L)
        val next1Stop = Date(stopLive.time + 90 * 60 * 1000L)
        val next2Stop = Date(next1Stop.time + 60 * 60 * 1000L)
        val next3Stop = Date(next2Stop.time + 120 * 60 * 1000L)

        val isSports = channel.cleanCategory.equals("Sports", ignoreCase = true)

        val currentTitle = when {
            isSports && channel.cleanName.contains("Star", ignoreCase = true) -> "Live Premier League: Matchday Action"
            isSports && channel.cleanName.contains("Willow", ignoreCase = true) -> "Live Cricket: T20 Super Series"
            isSports -> "Live Sports Tournament Coverage"
            channel.cleanCategory.equals("News", ignoreCase = true) -> "Prime News Hour & Live Desk"
            channel.cleanCategory.equals("Movies", ignoreCase = true) -> "Blockbuster Cinema Experience"
            else -> "${channel.cleanName} Live Broadcast"
        }

        val next1Title = if (isSports) "Match Highlights & Post-Game Analysis" else "Next Segment & Studio Discussion"
        val next2Title = if (isSports) "World of Sports Special & Interviews" else "Evening Edition & Special Report"
        val next3Title = if (isSports) "Late Night Sportscenter" else "Nightly Roundup"

        val current = EpgProgram(
            title = currentTitle,
            desc = "Live continuous broadcast on ${channel.cleanName} in HD with high-definition audio.",
            category = channel.cleanCategory,
            start = isoFmt.format(startLive),
            stop = isoFmt.format(stopLive),
            startFmt = timeFmt.format(startLive),
            stopFmt = timeFmt.format(stopLive),
            isLive = true
        )

        val upcoming = listOf(
            EpgProgram(
                title = next1Title,
                desc = "Detailed breakdown, player interviews, and upcoming fixture previews.",
                category = channel.cleanCategory,
                start = isoFmt.format(stopLive),
                stop = isoFmt.format(next1Stop),
                startFmt = timeFmt.format(stopLive),
                stopFmt = timeFmt.format(next1Stop),
                isLive = false
            ),
            EpgProgram(
                title = next2Title,
                desc = "Behind-the-scenes stories and tactical deep dive with expert analysts.",
                category = channel.cleanCategory,
                start = isoFmt.format(next1Stop),
                stop = isoFmt.format(next2Stop),
                startFmt = timeFmt.format(next1Stop),
                stopFmt = timeFmt.format(next2Stop),
                isLive = false
            ),
            EpgProgram(
                title = next3Title,
                desc = "Nighttime highlights and top replay moments from today's broadcast.",
                category = channel.cleanCategory,
                start = isoFmt.format(next2Stop),
                stop = isoFmt.format(next3Stop),
                startFmt = timeFmt.format(next2Stop),
                stopFmt = timeFmt.format(next3Stop),
                isLive = false
            )
        )

        return EpgResponse(
            channelId = channel.id,
            channelName = channel.cleanName,
            timezone = "Asia/Dhaka",
            current = current,
            upcoming = upcoming,
            all = listOf(current) + upcoming
        )
    }

    private fun getDefaultChannels(): List<Channel> {
        return listOf(
            Channel(
                id = 1,
                name = "Star Sports 1 HD",
                channelNumber = 1,
                categoryId = 2,
                categoryName = "Sports",
                streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
                streamUrlBackup = "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8",
                streamType = "m3u8",
                logoUrl = "https://images.unsplash.com/photo-1579952363873-27f3bade9f55?w=160",
                epgChannelId = "starsports1hd.in",
                description = "Premier League, Formula 1, Wimbledon and international football broadcasts in Full HD",
                order = 1,
                streamQualities = listOf(
                    StreamQuality(qualityLabel = "Auto (1080p)", streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8", bitrate = 4500000, isDefault = true),
                    StreamQuality(qualityLabel = "720p HD", streamUrl = "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8", bitrate = 2500000),
                    StreamQuality(qualityLabel = "480p SD", streamUrl = "https://test-streams.mux.dev/x36xhzz/url_6/193039199_mp4_h264_aac_hq_7.m3u8", bitrate = 1200000)
                )
            ),
            Channel(
                id = 2,
                name = "Star Sports 2 HD",
                channelNumber = 2,
                categoryId = 2,
                categoryName = "Sports",
                streamUrl = "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8",
                streamUrlBackup = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
                streamType = "m3u8",
                logoUrl = "https://images.unsplash.com/photo-1540747913346-19e32dc3e97e?w=160",
                epgChannelId = "starsports2hd.in",
                description = "International cricket tournaments, IPL, badminton, and hockey live action",
                order = 2,
                streamQualities = listOf(
                    StreamQuality(qualityLabel = "Auto", streamUrl = "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8", bitrate = 3000000, isDefault = true),
                    StreamQuality(qualityLabel = "720p HD", streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8", bitrate = 2200000)
                )
            ),
            Channel(
                id = 3,
                name = "Willow HD Cricket",
                channelNumber = 3,
                categoryId = 2,
                categoryName = "Sports",
                streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
                streamUrlBackup = "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8",
                streamType = "m3u8",
                logoUrl = "https://images.unsplash.com/photo-1531415074868-036b107e775a?w=160",
                epgChannelId = "willowhd.us",
                description = "24/7 dedicated live cricket channel broadcasting major test, ODI, and T20 matches worldwide",
                order = 3
            ),
            Channel(
                id = 4,
                name = "GTV Sports Live",
                channelNumber = 4,
                categoryId = 2,
                categoryName = "Sports",
                streamUrl = "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8",
                streamUrlBackup = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
                streamType = "m3u8",
                logoUrl = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=160",
                description = "Gazi Television sports live streaming Bangladesh cricket and major leagues",
                order = 4
            ),
            Channel(
                id = 5,
                name = "T Sports Live",
                channelNumber = 5,
                categoryId = 2,
                categoryName = "Sports",
                streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
                streamType = "m3u8",
                logoUrl = "https://images.unsplash.com/photo-1574629810360-7efbbe195018?w=160",
                description = "Bashundhara Group first sports channel in Bangladesh bringing premier sports to screens",
                order = 5
            ),
            Channel(
                id = 6,
                name = "Somoy TV News",
                channelNumber = 6,
                categoryId = 1,
                categoryName = "News",
                streamUrl = "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8",
                streamUrlBackup = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
                streamType = "m3u8",
                logoUrl = "https://images.unsplash.com/photo-1585829365295-ab7cd400c167?w=160",
                description = "Somoy News 24/7 breaking news, politics, business, and national updates",
                order = 6
            ),
            Channel(
                id = 7,
                name = "CNN International",
                channelNumber = 7,
                categoryId = 1,
                categoryName = "News",
                streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
                streamType = "m3u8",
                logoUrl = "https://images.unsplash.com/photo-1495020689067-958852a7765e?w=160",
                description = "Global breaking news, comprehensive analysis, and world reports",
                order = 7
            ),
            Channel(
                id = 8,
                name = "BTV National",
                channelNumber = 8,
                categoryId = 3,
                categoryName = "Entertainment",
                streamUrl = "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8",
                streamType = "m3u8",
                logoUrl = "https://images.unsplash.com/photo-1522869635100-9f4c5e86aa37?w=160",
                description = "Bangladesh Television public broadcast dramas, cultural programs, and news",
                order = 8
            )
        )
    }

    fun getLastWatchedChannelId(): Int {
        return prefs.getInt(KEY_LAST_CHANNEL_ID, -1)
    }

    fun saveLastWatchedChannelId(id: Int) {
        prefs.edit().putInt(KEY_LAST_CHANNEL_ID, id).apply()
    }

    fun getCachedChannels(): List<Channel> = cachedChannels
}
