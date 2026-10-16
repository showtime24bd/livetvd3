package com.example

import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.TvMainScreen
import com.example.ui.TvPlayerViewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TvBackgroundDark

class MainActivity : ComponentActivity() {

    private val viewModel: TvPlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Keep screen on during TV live video playback
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = TvBackgroundDark
                ) {
                    TvMainScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Direct remote numeric button presses (0-9)
        when (keyCode) {
            in KeyEvent.KEYCODE_0..KeyEvent.KEYCODE_9 -> {
                viewModel.onNumberKeyPressed(keyCode - KeyEvent.KEYCODE_0)
                return true
            }
            in KeyEvent.KEYCODE_NUMPAD_0..KeyEvent.KEYCODE_NUMPAD_9 -> {
                viewModel.onNumberKeyPressed(keyCode - KeyEvent.KEYCODE_NUMPAD_0)
                return true
            }
            KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_CHANNEL_UP -> {
                if (!viewModel.uiState.value.isDrawerOpen && !viewModel.uiState.value.isSportsDrawerOpen) {
                    viewModel.previousChannel()
                    return true
                }
            }
            KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_CHANNEL_DOWN -> {
                if (!viewModel.uiState.value.isDrawerOpen && !viewModel.uiState.value.isSportsDrawerOpen) {
                    viewModel.nextChannel()
                    return true
                }
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                if (viewModel.uiState.value.isSportsDrawerOpen) {
                    viewModel.closeSportsDrawer()
                    return true
                }
                if (!viewModel.uiState.value.isDrawerOpen) {
                    viewModel.openDrawer()
                    return true
                }
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                if (viewModel.uiState.value.isDrawerOpen) {
                    viewModel.closeDrawer()
                    return true
                }
                if (!viewModel.uiState.value.isSportsDrawerOpen) {
                    viewModel.toggleSportsDrawer()
                    return true
                }
            }
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                if (viewModel.uiState.value.numberInput.isVisible) {
                    viewModel.confirmNumberInput()
                    return true
                }
                if (!viewModel.uiState.value.isDrawerOpen && !viewModel.uiState.value.isSportsDrawerOpen) {
                    viewModel.toggleInfoHud()
                    return true
                }
            }
            KeyEvent.KEYCODE_MENU, KeyEvent.KEYCODE_G -> {
                viewModel.toggleDrawer()
                return true
            }
            KeyEvent.KEYCODE_S -> {
                viewModel.toggleSportsDrawer()
                return true
            }
            KeyEvent.KEYCODE_M, KeyEvent.KEYCODE_VOLUME_MUTE -> {
                viewModel.toggleMute()
                return true
            }
            KeyEvent.KEYCODE_F, KeyEvent.KEYCODE_A -> {
                viewModel.cycleResizeMode()
                return true
            }
            KeyEvent.KEYCODE_Q -> {
                viewModel.toggleQualityDialog()
                return true
            }
            KeyEvent.KEYCODE_SPACE, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                viewModel.togglePlayPause()
                return true
            }
            KeyEvent.KEYCODE_MEDIA_PLAY -> {
                if (!viewModel.uiState.value.isPlaying) {
                    viewModel.togglePlayPause()
                }
                return true
            }
            KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                if (viewModel.uiState.value.isPlaying) {
                    viewModel.togglePlayPause()
                }
                return true
            }
            KeyEvent.KEYCODE_BACK -> {
                if (viewModel.uiState.value.isQualityDialogVisible) {
                    viewModel.closeQualityDialog()
                    return true
                }
                if (viewModel.uiState.value.numberInput.isVisible) {
                    viewModel.cancelNumberInput()
                    return true
                }
                if (viewModel.uiState.value.isSportsDrawerOpen) {
                    viewModel.closeSportsDrawer()
                    return true
                }
                if (viewModel.uiState.value.isDrawerOpen) {
                    viewModel.closeDrawer()
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}
