package com.walkbase.ibeacon

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
    private var majorValue = mutableStateOf("1")
    private var minorValue = mutableStateOf("2")
    private var playbackState = mutableStateOf(PlaybackState.STOPPED)
    private lateinit var iBeacon: IBeacon

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        iBeacon = IBeacon(
            context = this,
            majorValue = majorValue.value,
            minorValue = minorValue.value,
            uuid = "856E3AB6-5EA8-45EB-9813-676BB29C4316"
        )

        enableEdgeToEdge()
        setContent {
            IBeaconTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DemoApp(
                        majorValue = majorValue.value,
                        minorValue = minorValue.value,
                        modes = iBeacon.modes,
                        onMajorValueChange = handleMajorValueChange,
                        onMinorValueChange = handleMinorValueChange,
                        onModeChange = handleModeChange,
                        onPauseButtonClick = handlePauseButtonClick,
                        onPlayButtonClick = handlePlayButtonClick,
                        onStopButtonClick = handleStopButtonClick,
                        onTxPowerLevelChange = handleTxPowerLevelChange,
                        playbackState = playbackState.value,
                        txPowerLevels = iBeacon.txPowerLevels,
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

    private val handleMajorValueChange: (String) -> Unit = {
        majorValue.value = it
        iBeacon.majorValue = it
    }
    private val handleMinorValueChange: (String) -> Unit = {
        minorValue.value = it
        iBeacon.minorValue = it
    }

    private val handleModeChange: (Int) -> Unit = {
        iBeacon.changeMode(it)
    }

    private val handlePauseButtonClick: () -> Unit = {
        iBeacon.pauseBeaconTransmission()
        playbackState.value = PlaybackState.PAUSED
    }

    private val handlePlayButtonClick: () -> Unit = {
        if (playbackState.value == PlaybackState.PAUSED) {
            iBeacon.resumeBeaconTransmission()
        } else if (playbackState.value == PlaybackState.STOPPED) {
            val majorValueInt = majorValue.value.toIntOrNull()
            if (majorValueInt == null || majorValueInt <= 0) {
                majorValue.value = "0"
                iBeacon.majorValue = majorValue.value
            }
            val minorValueInt = minorValue.value.toIntOrNull()
            if (minorValueInt == null || minorValueInt <= 0) {
                minorValue.value = "0"
                iBeacon.minorValue = minorValue.value
            }

            iBeacon.startBeaconTransmission()
        }
        playbackState.value = PlaybackState.PLAYING
    }

    private val handleStopButtonClick: () -> Unit = {
        iBeacon.stopBeaconTransmission()
        playbackState.value = PlaybackState.STOPPED
    }

    private val handleTxPowerLevelChange: (Int) -> Unit = {
        iBeacon.changeTxPowerLevel(it)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions: Map<String, @JvmSuppressWildcards Boolean> ->
        if (permissions.values.all { it }) {
            Toast.makeText(this, "All permissions granted.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(
                this,
                R.string.permission_error,
                Toast.LENGTH_LONG
            ).show()
        }
    }
}

@Composable
fun DemoApp(
    majorValue: String,
    minorValue: String,
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
                    options = txPowerLevels,
                    handleOnClick = onTxPowerLevelChange,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Select(
                    options = modes,
                    handleOnClick = onModeChange,
                    modifier = Modifier.weight(1f)
                )
            }

            Row {
                TextField(
                    enabled = playbackState == PlaybackState.STOPPED,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    label = { Text(stringResource(R.string.major_value)) },
                    onValueChange = { newValue ->
                        onMajorValueChange(newValue)
                    },
                    value = majorValue,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(10.dp))
                TextField(
                    enabled = playbackState == PlaybackState.STOPPED,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    label = { Text(stringResource(R.string.minor_value)) },
                    onValueChange = { newValue ->
                        onMinorValueChange(newValue)
                    },
                    value = minorValue,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Select(options: Map<String, Int>, handleOnClick: (Int) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(options.keys.first()) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        TextField(
            label = { Text(stringResource(R.string.tx_power)) },
            onValueChange = { selectedOption = it },
            readOnly = true,
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