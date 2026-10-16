package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.ui.AspectRatioFrameLayout
import com.example.data.ChannelRepository
import com.example.model.Channel
import com.example.model.EpgResponse
import com.example.model.FeaturedSportsEvent
import com.example.model.StreamQuality
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NumberInputState(
    val digits: String = "",
    val matchingChannel: Channel? = null,
    val isVisible: Boolean = false,
    val countdownRemainingMs: Long = 0L,
    val notFoundMessage: String? = null
)

data class TvUiState(
    val channels: List<Channel> = emptyList(),
    val filteredChannels: List<Channel> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String = "All",
    val searchQuery: String = "",
    val currentChannel: Channel? = null,
    val currentStreamUrl: String = "",
    val currentEpg: EpgResponse? = null,
    val featuredSports: List<FeaturedSportsEvent> = emptyList(),
    val selectedQualityLabel: String = "Auto",
    val availableQualities: List<String> = listOf("Auto"),
    val networkBitrateEstimate: Long = 0L,
    val isUsingBackupUrl: Boolean = false,
    val isQualityDialogVisible: Boolean = false,
    val isSportsDrawerOpen: Boolean = false,
    val isMuted: Boolean = false,
    val isLoading: Boolean = true,
    val networkError: String? = null,
    val playbackError: String? = null,
    val retryCount: Int = 0,
    val isPlaying: Boolean = true,
    val isBuffering: Boolean = false,
    val isDrawerOpen: Boolean = false,
    val isInfoHudVisible: Boolean = false,
    val numberInput: NumberInputState = NumberInputState(),
    val resizeMode: Int = AspectRatioFrameLayout.RESIZE_MODE_FIT
)

class TvPlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChannelRepository(application)

    private val _uiState = MutableStateFlow(TvUiState())
    val uiState: StateFlow<TvUiState> = _uiState.asStateFlow()

    private var infoHudJob: Job? = null
    private var numberInputJob: Job? = null
    private var adaptiveQualityJob: Job? = null
    private var retryJob: Job? = null

    init {
        loadChannels()
    }

    fun loadChannels() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, networkError = null) }
            val sports = repository.getFeaturedSportsEvents()
            val result = repository.fetchChannels()
            result.onSuccess { channelList ->
                if (channelList.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            networkError = "No channels available."
                        )
                    }
                    return@onSuccess
                }

                val categories = listOf("All") + channelList.map { it.cleanCategory }.distinct()
                val lastId = repository.getLastWatchedChannelId()
                val initialChannel = channelList.find { it.id == lastId } ?: channelList.first()
                val qualities = buildQualityList(initialChannel)

                _uiState.update {
                    it.copy(
                        channels = channelList,
                        filteredChannels = channelList,
                        categories = categories,
                        selectedCategory = "All",
                        searchQuery = "",
                        currentChannel = initialChannel,
                        currentStreamUrl = initialChannel.activeStreamUrl,
                        featuredSports = sports,
                        selectedQualityLabel = "Auto",
                        availableQualities = qualities,
                        isUsingBackupUrl = false,
                        isLoading = false,
                        networkError = null,
                        playbackError = null,
                        retryCount = 0,
                        isPlaying = true
                    )
                }
                repository.saveLastWatchedChannelId(initialChannel.id)
                fetchEpgForCurrentChannel(initialChannel)
                showInfoHud()
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        networkError = err.localizedMessage ?: "Failed to load channels"
                    )
                }
            }
        }
    }

    private fun fetchEpgForCurrentChannel(channel: Channel) {
        viewModelScope.launch {
            val epg = repository.fetchEpg(channel)
            _uiState.update { it.copy(currentEpg = epg) }
        }
    }

    private fun buildQualityList(channel: Channel): List<String> {
        val list = mutableListOf("Auto")
        channel.validQualities.forEach { q ->
            if (!list.contains(q.displayLabel)) {
                list.add(q.displayLabel)
            }
        }
        if (!channel.streamUrlBackup.isNullOrBlank()) {
            list.add("Backup Stream")
        }
        return list
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun selectCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
        applyFilters()
    }

    private fun applyFilters() {
        val all = _uiState.value.channels
        val cat = _uiState.value.selectedCategory
        val query = _uiState.value.searchQuery.trim().lowercase()

        val filtered = all.filter { channel ->
            val matchesCategory = (cat == "All" || channel.cleanCategory.equals(cat, ignoreCase = true))
            val matchesQuery = query.isEmpty() ||
                    channel.cleanName.lowercase().contains(query) ||
                    channel.displayNumber.toString() == query ||
                    channel.cleanCategory.lowercase().contains(query)
            matchesCategory && matchesQuery
        }

        _uiState.update { it.copy(filteredChannels = filtered) }
    }

    fun playChannel(channel: Channel) {
        retryJob?.cancel()
        repository.saveLastWatchedChannelId(channel.id)
        val qualities = buildQualityList(channel)
        _uiState.update {
            it.copy(
                currentChannel = channel,
                currentStreamUrl = channel.activeStreamUrl,
                selectedQualityLabel = "Auto",
                availableQualities = qualities,
                isUsingBackupUrl = false,
                playbackError = null,
                retryCount = 0,
                isPlaying = true,
                isDrawerOpen = false,
                isSportsDrawerOpen = false,
                isQualityDialogVisible = false
            )
        }
        cancelNumberInput()
        fetchEpgForCurrentChannel(channel)
        showInfoHud()
    }

    fun selectQuality(label: String) {
        val current = _uiState.value.currentChannel ?: return
        var targetUrl = current.activeStreamUrl
        var isBackup = false

        when (label) {
            "Auto" -> {
                targetUrl = current.activeStreamUrl
                isBackup = false
            }
            "Backup Stream" -> {
                targetUrl = current.streamUrlBackup?.trim() ?: current.activeStreamUrl
                isBackup = true
            }
            else -> {
                val matched = current.validQualities.find { it.displayLabel.equals(label, ignoreCase = true) }
                if (matched != null && matched.effectiveUrl.isNotBlank()) {
                    targetUrl = matched.effectiveUrl
                }
            }
        }

        _uiState.update {
            it.copy(
                selectedQualityLabel = label,
                currentStreamUrl = targetUrl,
                isUsingBackupUrl = isBackup,
                playbackError = null,
                retryCount = 0,
                isQualityDialogVisible = false
            )
        }
        showInfoHud()
    }

    fun onBandwidthEstimate(bitrateBps: Long) {
        _uiState.update { it.copy(networkBitrateEstimate = bitrateBps) }

        if (_uiState.value.selectedQualityLabel == "Auto") {
            val current = _uiState.value.currentChannel ?: return
            val qualities = current.validQualities
            if (qualities.isNotEmpty()) {
                adaptiveQualityJob?.cancel()
                adaptiveQualityJob = viewModelScope.launch {
                    delay(2000)
                    val bestQuality = selectBestQualityForBitrate(qualities, bitrateBps)
                    if (bestQuality != null && bestQuality.effectiveUrl != _uiState.value.currentStreamUrl) {
                        _uiState.update {
                            it.copy(
                                currentStreamUrl = bestQuality.effectiveUrl,
                                selectedQualityLabel = "Auto (${bestQuality.displayLabel})"
                            )
                        }
                    }
                }
            }
        }
    }

    private fun selectBestQualityForBitrate(qualities: List<StreamQuality>, bitrateBps: Long): StreamQuality? {
        val kbps = bitrateBps / 1000
        return qualities.sortedByDescending { it.bitrate ?: estimateBitrateFromLabel(it.displayLabel) }
            .firstOrNull { (it.bitrate ?: estimateBitrateFromLabel(it.displayLabel)) <= kbps * 800 }
            ?: qualities.lastOrNull()
    }

    private fun estimateBitrateFromLabel(label: String): Long {
        return when {
            label.contains("1080", ignoreCase = true) -> 4500L
            label.contains("720", ignoreCase = true) -> 2500L
            label.contains("480", ignoreCase = true) -> 1200L
            label.contains("360", ignoreCase = true) -> 700L
            label.contains("HD", ignoreCase = true) -> 3000L
            label.contains("SD", ignoreCase = true) -> 1000L
            else -> 2000L
        }
    }

    fun nextChannel() {
        val list = _uiState.value.channels
        if (list.isEmpty()) return
        val current = _uiState.value.currentChannel
        val currentIndex = list.indexOfFirst { it.id == current?.id }
        val nextIndex = if (currentIndex < 0 || currentIndex + 1 >= list.size) 0 else currentIndex + 1
        playChannel(list[nextIndex])
    }

    fun previousChannel() {
        val list = _uiState.value.channels
        if (list.isEmpty()) return
        val current = _uiState.value.currentChannel
        val currentIndex = list.indexOfFirst { it.id == current?.id }
        val prevIndex = if (currentIndex <= 0) list.size - 1 else currentIndex - 1
        playChannel(list[prevIndex])
    }

    fun onNumberKeyPressed(digit: Int) {
        numberInputJob?.cancel()

        val currentDigits = _uiState.value.numberInput.digits
        val newDigits = if (currentDigits.length >= 4) digit.toString() else currentDigits + digit
        val targetNumber = newDigits.toIntOrNull() ?: return
        val matchedChannel = _uiState.value.channels.find { it.displayNumber == targetNumber || it.id == targetNumber }

        _uiState.update {
            it.copy(
                numberInput = NumberInputState(
                    digits = newDigits,
                    matchingChannel = matchedChannel,
                    isVisible = true,
                    countdownRemainingMs = 1500L,
                    notFoundMessage = if (matchedChannel == null) "No channel #$newDigits" else null
                )
            )
        }

        numberInputJob = viewModelScope.launch {
            val totalWait = 1500L
            val step = 100L
            var remaining = totalWait
            while (remaining > 0) {
                delay(step)
                remaining -= step
                _uiState.update { state ->
                    state.copy(
                        numberInput = state.numberInput.copy(countdownRemainingMs = remaining)
                    )
                }
            }
            confirmNumberInput()
        }
    }

    fun confirmNumberInput() {
        numberInputJob?.cancel()
        val digits = _uiState.value.numberInput.digits
        if (digits.isBlank()) {
            cancelNumberInput()
            return
        }
        val targetNumber = digits.toIntOrNull()
        val channel = _uiState.value.channels.find { it.displayNumber == targetNumber || it.id == targetNumber }
        if (channel != null) {
            playChannel(channel)
        } else {
            viewModelScope.launch {
                _uiState.update {
                    it.copy(
                        numberInput = it.numberInput.copy(
                            notFoundMessage = "Channel #$digits not found"
                        )
                    )
                }
                delay(1200)
                cancelNumberInput()
            }
        }
    }

    fun cancelNumberInput() {
        numberInputJob?.cancel()
        _uiState.update { it.copy(numberInput = NumberInputState()) }
    }

    fun openDrawer() {
        _uiState.update { it.copy(isDrawerOpen = true, isSportsDrawerOpen = false, isQualityDialogVisible = false) }
    }

    fun closeDrawer() {
        _uiState.update { it.copy(isDrawerOpen = false) }
    }

    fun toggleDrawer() {
        _uiState.update { it.copy(isDrawerOpen = !it.isDrawerOpen, isSportsDrawerOpen = false, isQualityDialogVisible = false) }
    }

    fun toggleSportsDrawer() {
        _uiState.update { it.copy(isSportsDrawerOpen = !it.isSportsDrawerOpen, isDrawerOpen = false, isQualityDialogVisible = false) }
    }

    fun closeSportsDrawer() {
        _uiState.update { it.copy(isSportsDrawerOpen = false) }
    }

    fun toggleQualityDialog() {
        _uiState.update { it.copy(isQualityDialogVisible = !it.isQualityDialogVisible, isDrawerOpen = false, isSportsDrawerOpen = false) }
    }

    fun closeQualityDialog() {
        _uiState.update { it.copy(isQualityDialogVisible = false) }
    }

    fun toggleMute() {
        _uiState.update { it.copy(isMuted = !it.isMuted) }
        showInfoHud()
    }

    fun showInfoHud() {
        infoHudJob?.cancel()
        _uiState.update { it.copy(isInfoHudVisible = true) }
        infoHudJob = viewModelScope.launch {
            delay(5000)
            _uiState.update { it.copy(isInfoHudVisible = false) }
        }
    }

    fun toggleInfoHud() {
        if (_uiState.value.isInfoHudVisible) {
            infoHudJob?.cancel()
            _uiState.update { it.copy(isInfoHudVisible = false) }
        } else {
            showInfoHud()
        }
    }

    fun togglePlayPause() {
        _uiState.update { it.copy(isPlaying = !it.isPlaying) }
        showInfoHud()
    }

    fun setBuffering(isBuffering: Boolean) {
        _uiState.update { it.copy(isBuffering = isBuffering) }

        if (isBuffering && _uiState.value.selectedQualityLabel.startsWith("Auto")) {
            val current = _uiState.value.currentChannel ?: return
            val validQualities = current.validQualities
            if (validQualities.size > 1) {
                val lowest = validQualities.lastOrNull()
                if (lowest != null && lowest.effectiveUrl != _uiState.value.currentStreamUrl) {
                    _uiState.update {
                        it.copy(
                            currentStreamUrl = lowest.effectiveUrl,
                            selectedQualityLabel = "Auto (${lowest.displayLabel})"
                        )
                    }
                }
            }
        }
    }

    fun onPlayerError(errorMessage: String) {
        val current = _uiState.value.currentChannel
        val retries = _uiState.value.retryCount

        if (retries < 3) {
            // Smart auto-recovery with 2-second retry logic (as specified in FIXES.md)
            val nextRetry = retries + 1
            _uiState.update {
                it.copy(
                    retryCount = nextRetry,
                    playbackError = "Network issue ($errorMessage). Auto-retrying ($nextRetry/3)..."
                )
            }
            retryJob?.cancel()
            retryJob = viewModelScope.launch {
                delay(2000)
                retryPlayback()
            }
            return
        }

        // After 3 retries, seamlessly try the backup stream URL if available
        if (!_uiState.value.isUsingBackupUrl && !current?.streamUrlBackup.isNullOrBlank()) {
            _uiState.update {
                it.copy(
                    currentStreamUrl = current!!.streamUrlBackup!!.trim(),
                    isUsingBackupUrl = true,
                    selectedQualityLabel = "Backup Stream",
                    retryCount = 0,
                    playbackError = "Primary stream interrupted. Seamlessly switched to backup stream."
                )
            }
            showInfoHud()
        } else {
            // Try alternate quality stream
            val currentQualities = current?.validQualities ?: emptyList()
            val alternate = currentQualities.find { it.effectiveUrl != _uiState.value.currentStreamUrl }
            if (alternate != null) {
                _uiState.update {
                    it.copy(
                        currentStreamUrl = alternate.effectiveUrl,
                        selectedQualityLabel = alternate.displayLabel,
                        retryCount = 0,
                        playbackError = "Switched to ${alternate.displayLabel} quality."
                    )
                }
                showInfoHud()
            } else {
                _uiState.update {
                    it.copy(
                        playbackError = "Stream temporarily unavailable. Click Retry or switch channel."
                    )
                }
            }
        }
    }

    fun cycleResizeMode() {
        val nextMode = when (_uiState.value.resizeMode) {
            AspectRatioFrameLayout.RESIZE_MODE_FIT -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_FILL
            else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
        }
        _uiState.update { it.copy(resizeMode = nextMode) }
        showInfoHud()
    }

    fun retryPlayback() {
        val current = _uiState.value.currentChannel ?: return
        _uiState.update {
            it.copy(
                currentStreamUrl = current.activeStreamUrl,
                isUsingBackupUrl = false,
                playbackError = null,
                isPlaying = true
            )
        }
    }
}
