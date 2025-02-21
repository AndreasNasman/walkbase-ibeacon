package com.walkbase.ibeacon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.walkbase.ibeacon.sdk.IBeacon
import com.walkbase.ibeacon.ui.theme.IBeaconTheme

class MainActivity : ComponentActivity() {
    private var playbackState = mutableStateOf(PlaybackState.STOPPED)
    private lateinit var iBeacon: IBeacon

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        iBeacon = IBeacon(context = this)

        enableEdgeToEdge()
        setContent {
            IBeaconTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DemoApp(
                        modes = iBeacon.modes,
                        onMajorValueChange = { iBeacon.majorValue = it },
                        onMinorValueChange = { iBeacon.minorValue = it },
                        onModeChange = { iBeacon.changeMode(it) },
                        onPauseButtonClick = pause,
                        onPlayButtonClick = play,
                        onStopButtonClick = stop,
                        onTxPowerLevelChange = { iBeacon.changeTxPowerLevel(it) },
                        playbackState = playbackState.value,
                        txPowerLevels = iBeacon.txPowerLevels,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private val pause: () -> Unit = {
        iBeacon.pauseBeaconTransmission()
        playbackState.value = PlaybackState.PAUSED
    }

    private val play: () -> Unit = {
        when (playbackState.value) {
            PlaybackState.STOPPED -> iBeacon.startBeaconTransmission()
            PlaybackState.PAUSED -> iBeacon.resumeBeaconTransmission()
            // TODO: Handle this branch appropriately.
            else -> error("Unhandled playback state.")
        }
        playbackState.value = PlaybackState.PLAYING
    }

    override fun onStop() {
        super.onStop()
        stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        stop()
    }

    private val stop: () -> Unit = {
        iBeacon.stopBeaconTransmission()
        playbackState.value = PlaybackState.STOPPED
    }
}

@Composable
fun DemoApp(
    modes: Map<String, Int>,
    onMajorValueChange: (String) -> Unit,
    onMinorValueChange: (String) -> Unit,
    onModeChange: (Int) -> Unit,
    onPauseButtonClick: () -> Unit,
    onPlayButtonClick: () -> Unit,
    onStopButtonClick: () -> Unit,
    onTxPowerLevelChange: (Int) -> Unit,
    playbackState: PlaybackState,
    txPowerLevels: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Text(
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            text = "Transmitting: ${if (playbackState == PlaybackState.PLAYING) "Yes" else "No"}\nPlayback state: $playbackState",
            textAlign = TextAlign.Center,
            modifier = Modifier
                .background(color = colorResource(R.color.teal_200))
                .fillMaxWidth()
                .size(80.dp)
                .wrapContentHeight(align = Alignment.CenterVertically)
        )

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
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

        Column(
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            Row {
                Select(
                    handleOnClick = onModeChange,
                    label = stringResource(R.string.mode),
                    options = modes,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Select(
                    handleOnClick = onTxPowerLevelChange,
                    label = stringResource(R.string.tx_power),
                    options = txPowerLevels,
                    modifier = Modifier.weight(1f)
                )
            }

            Row {
                NumericTextField(
                    enabled = playbackState == PlaybackState.STOPPED,
                    handleValueChange = { newValue -> onMajorValueChange(newValue) },
                    initialValue = "1",
                    label = stringResource(R.string.major_value),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(10.dp))
                NumericTextField(
                    enabled = playbackState == PlaybackState.STOPPED,
                    handleValueChange = { newValue -> onMinorValueChange(newValue) },
                    imeAction = ImeAction.Done,
                    initialValue = "2",
                    label = stringResource(R.string.minor_value),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun NumericTextField(
    enabled: Boolean,
    handleValueChange: (String) -> Unit,
    initialValue: String,
    label: String,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Next,
) {
    var value by remember { mutableStateOf(initialValue) }

    LaunchedEffect(Unit) {
        handleValueChange(value)
    }

    TextField(
        enabled = enabled,
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = imeAction,
            keyboardType = KeyboardType.Number
        ),
        label = { Text(label) },
        onValueChange = {
            value = it
            handleValueChange(it)
        },
        value = value,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Select(
    handleOnClick: (Int) -> Unit,
    label: String,
    options: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(options.keys.first()) }

    LaunchedEffect(Unit) {
        // TODO: Handle the error more gracefully.
        handleOnClick(options[selectedOption] ?: error("Invalid option"))
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        TextField(
            label = { Text(label) },
            onValueChange = { selectedOption = it },
            readOnly = true,
            singleLine = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            value = selectedOption,
            modifier = Modifier.menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (key, value) ->
                DropdownMenuItem(
                    text = { Text(key) },
                    onClick = {
                        selectedOption = key
                        expanded = false
                        handleOnClick(value)
                    }
                )
            }
        }
    }
}

enum class PlaybackState {
    STOPPED,
    PLAYING,
    PAUSED
}