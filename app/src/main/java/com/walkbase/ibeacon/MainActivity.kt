package com.walkbase.ibeacon

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.walkbase.ibeacon.sdk.IBeacon
import com.walkbase.ibeacon.ui.theme.IBeaconTheme

class MainActivity : ComponentActivity() {
    private var playbackState = mutableStateOf(PlaybackState.STOPPED)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IBeaconTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DemoApp(
                        onPauseButtonClick = handlePauseButtonClick,
                        onPlayButtonClick = handlePlayButtonClick,
                        onStopButtonClick = handleStopButtonClick,
                        playbackState = playbackState.value,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }

        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE
                // Add if needed.
                // Manifest.permission.BLUETOOTH_CONNECT,
                // TODO: Handle this permission separately since you cannot request it directly.
                // https://developer.android.com/reference/android/Manifest.permission#ACCESS_BACKGROUND_LOCATION
                // Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            )
        )
    }

    private val handlePauseButtonClick: () -> Unit = {
        // TODO: Implement
        playbackState.value = PlaybackState.PAUSED
    }

    private val handlePlayButtonClick: () -> Unit = {
        playbackState.value = PlaybackState.PLAYING
    }

    private val handleStopButtonClick: () -> Unit = {
        // TODO: Implement
        playbackState.value = PlaybackState.STOPPED
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions: Map<String, @JvmSuppressWildcards Boolean> ->
        if (permissions.values.all { it }) {
            Log.d(this::class.simpleName, "All permissions granted.")
        } else {
            Log.d(
                this::class.simpleName,
                "Granted permissions: ${
                    permissions.entries.filter { it.value }.map { it.key }
                }"
            )
            Log.d(
                this::class.simpleName,
                "Denied permissions: ${permissions.entries.filter { !it.value }.map { it.key }}"
            )
            // TODO: Handle denied permissions.
        }
    }
}

@Composable
fun DemoApp(
    onPauseButtonClick: () -> Unit,
    onPlayButtonClick: () -> Unit,
    onStopButtonClick: () -> Unit,
    playbackState: PlaybackState,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxSize()
    ) {
        if (playbackState == PlaybackState.STOPPED || playbackState == PlaybackState.PAUSED) {
            FilledIconButton(onClick = onPlayButtonClick, modifier = Modifier.size(100.dp)) {
                Icon(
                    contentDescription = "Play button",
                    painter = painterResource(R.drawable.play_arrow_24px),
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else if (playbackState == PlaybackState.PLAYING) {
            FilledIconButton(onClick = onPauseButtonClick, modifier = Modifier.size(100.dp)) {
                Icon(
                    contentDescription = "Pause button",
                    painter = painterResource(R.drawable.pause_24px),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(7.dp)
                )
            }
        }

        if (playbackState == PlaybackState.PLAYING || playbackState == PlaybackState.PAUSED) {
            Spacer(modifier = Modifier.width(10.dp))
            FilledIconButton(onClick = onStopButtonClick, modifier = Modifier.size(100.dp)) {
                Icon(
                    contentDescription = "Stop button",
                    painter = painterResource(R.drawable.stop_24px),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    IBeaconTheme {
        DemoApp(
            onPauseButtonClick = {},
            onPlayButtonClick = {},
            onStopButtonClick = {},
            playbackState = PlaybackState.PLAYING
        )
    }
}

enum class PlaybackState {
    STOPPED,
    PLAYING,
    PAUSED
}