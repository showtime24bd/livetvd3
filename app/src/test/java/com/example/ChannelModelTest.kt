package com.example

import com.example.model.Channel
import org.junit.Assert.assertEquals
import org.junit.Test

class ChannelModelTest {

    @Test
    fun testChannelCleanProperties() {
        val channel = Channel(
            id = 4,
            name = " FOX 501 HD  ",
            categoryName = " Sports ",
            streamUrl = "http://example.com/stream.m3u8"
        )
        assertEquals("FOX 501 HD", channel.cleanName)
        assertEquals("Sports", channel.cleanCategory)
        assertEquals("http://example.com/stream.m3u8", channel.activeStreamUrl)
    }

    @Test
    fun testChannelFallbackStream() {
        val channel = Channel(
            id = 5,
            name = "Sky Sports",
            streamUrl = "",
            streamUrlBackup = "http://backup.com/stream.m3u8"
        )
        assertEquals("http://backup.com/stream.m3u8", channel.activeStreamUrl)
    }

    @Test
    fun testStreamQualities() {
        val channel = Channel(
            id = 7,
            name = "Sky Sports Football",
            streamUrl = "http://stream.com/live.m3u8",
            streamQualities = listOf(
                com.example.model.StreamQuality(quality = "1080p", url = "http://stream.com/1080.m3u8"),
                com.example.model.StreamQuality(quality = "720p", url = "http://stream.com/720.m3u8"),
                com.example.model.StreamQuality(quality = "Empty", url = "")
            )
        )
        assertEquals(2, channel.validQualities.size)
        assertEquals("1080p", channel.validQualities[0].displayLabel)
        assertEquals("http://stream.com/1080.m3u8", channel.validQualities[0].effectiveUrl)
    }
}
