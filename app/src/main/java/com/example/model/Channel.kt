package com.example.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StreamQuality(
    @Json(name = "quality") val quality: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "label") val label: String? = null,
    @Json(name = "quality_label") val qualityLabel: String? = null,
    @Json(name = "resolution") val resolution: String? = null,
    @Json(name = "url") val url: String? = null,
    @Json(name = "stream_url") val streamUrl: String? = null,
    @Json(name = "bitrate") val bitrate: Long? = null,
    @Json(name = "is_default") val isDefault: Boolean? = false
) {
    val effectiveUrl: String
        get() = url?.trim()?.ifEmpty { streamUrl?.trim() ?: "" } ?: (streamUrl?.trim() ?: "")

    val displayLabel: String
        get() = qualityLabel?.trim()?.ifEmpty { null }
            ?: quality?.trim()?.ifEmpty { null }
            ?: name?.trim()?.ifEmpty { null }
            ?: label?.trim()?.ifEmpty { null }
            ?: resolution?.trim()?.ifEmpty { null }
            ?: if (bitrate != null && bitrate > 0) "${bitrate / 1000}k" else "Auto"
}

@JsonClass(generateAdapter = true)
data class EpgProgram(
    @Json(name = "title") val title: String = "",
    @Json(name = "desc") val desc: String = "",
    @Json(name = "category") val category: String = "",
    @Json(name = "icon") val icon: String = "",
    @Json(name = "start") val start: String = "",
    @Json(name = "stop") val stop: String = "",
    @Json(name = "start_fmt") val startFmt: String = "",
    @Json(name = "stop_fmt") val stopFmt: String = "",
    @Json(name = "is_live") val isLive: Boolean = false
)

@JsonClass(generateAdapter = true)
data class EpgResponse(
    @Json(name = "channel_id") val channelId: Int? = null,
    @Json(name = "channel_name") val channelName: String? = null,
    @Json(name = "timezone") val timezone: String? = "Asia/Dhaka",
    @Json(name = "current") val current: EpgProgram? = null,
    @Json(name = "upcoming") val upcoming: List<EpgProgram> = emptyList(),
    @Json(name = "all") val all: List<EpgProgram> = emptyList()
)

@JsonClass(generateAdapter = true)
data class FeaturedSportsEvent(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "channel_id") val channelId: Int,
    @Json(name = "channel_name") val channelName: String? = null,
    @Json(name = "channel_logo") val channelLogo: String? = null,
    @Json(name = "event_title") val eventTitle: String,
    @Json(name = "event_description") val eventDescription: String? = null,
    @Json(name = "thumbnail_url") val thumbnailUrl: String? = null,
    @Json(name = "start_time") val startTime: String,
    @Json(name = "end_time") val endTime: String? = null,
    @Json(name = "category") val category: String? = "Sports",
    @Json(name = "league_tournament") val leagueTournament: String? = null,
    @Json(name = "teams_playing") val teamsPlaying: String? = null,
    @Json(name = "is_active") val isActive: Boolean? = true,
    @Json(name = "order") val order: Int? = 0
)

@JsonClass(generateAdapter = true)
data class Channel(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "channel_number") val channelNumber: Int? = null,
    @Json(name = "category_id") val categoryId: Int? = null,
    @Json(name = "category_name") val categoryName: String? = null,
    @Json(name = "stream_url") val streamUrl: String,
    @Json(name = "stream_url_backup") val streamUrlBackup: String? = null,
    @Json(name = "stream_type") val streamType: String? = null,
    @Json(name = "logo_url") val logoUrl: String? = null,
    @Json(name = "epg_channel_id") val epgChannelId: String? = null,
    @Json(name = "is_active") val isActive: Boolean? = true,
    @Json(name = "referer") val referer: String? = null,
    @Json(name = "user_agent") val userAgent: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "order") val order: Int? = 0,
    @Json(name = "stream_qualities") val streamQualities: List<StreamQuality>? = null
) {
    val cleanName: String
        get() = name.trim().ifEmpty { "Channel $displayNumber" }

    val displayNumber: Int
        get() = channelNumber ?: id

    val cleanCategory: String
        get() = categoryName?.trim()?.ifEmpty { "General" } ?: "General"

    val activeStreamUrl: String
        get() = streamUrl.trim().ifEmpty { streamUrlBackup?.trim() ?: "" }

    val validQualities: List<StreamQuality>
        get() = streamQualities?.filter { it.effectiveUrl.isNotBlank() } ?: emptyList()

    val streamFormatDisplay: String
        get() = when (streamType?.lowercase()?.trim()) {
            "m3u8", "hls" -> "HLS (M3U8)"
            "ts" -> "MPEG-TS"
            "mpd", "dash" -> "DASH (MPD)"
            "mp4" -> "MP4 Video"
            else -> if (activeStreamUrl.contains(".m3u8", ignoreCase = true)) "HLS (M3U8)" else "LIVE STREAM"
        }
}
