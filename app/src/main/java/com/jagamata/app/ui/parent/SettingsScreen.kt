package com.jagamata.app.ui.parent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jagamata.app.R
import com.jagamata.app.data.SettingsRepository
import com.jagamata.app.ui.viewModel.SettingsViewModel

/**
 * Pengaturan orang tua (PRD §2.17, US-11).
 * Slider ambang: jarak 20–50 cm, cahaya 50–500 lux, postur 20–60°.
 * Perubahan langsung berlaku ke state machine tanpa restart service.
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenPrivacy: () -> Unit,
    viewModel: SettingsViewModel =
        SettingsViewModel(
            androidx.compose.ui.platform.LocalContext.current.applicationContext as android.app.Application,
        ),
) {
    val closeCm by viewModel.closeThresholdCm.collectAsState()
    val lux by viewModel.luxThreshold.collectAsState()
    val pitch by viewModel.pitchThresholdDeg.collectAsState()
    val lightMode by viewModel.lightModeEnabled.collectAsState()
    val safeBanner by viewModel.safeBannerEnabled.collectAsState()

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.displayMedium,
        )

        ThresholdSlider(
            title = stringResource(R.string.settings_threshold_close),
            value = closeCm,
            range = SettingsRepository.CLOSE_CM_RANGE_MIN..SettingsRepository.CLOSE_CM_RANGE_MAX,
            valueLabel = stringResource(R.string.cm_value, closeCm),
            onChange = viewModel::setCloseThresholdCm,
        )
        ThresholdSlider(
            title = stringResource(R.string.settings_threshold_dark),
            value = lux,
            range = SettingsRepository.LUX_RANGE_MIN..SettingsRepository.LUX_RANGE_MAX,
            valueLabel = stringResource(R.string.lux_value, lux),
            onChange = viewModel::setLuxThreshold,
        )
        ThresholdSlider(
            title = stringResource(R.string.settings_threshold_posture),
            value = pitch,
            range = SettingsRepository.PITCH_RANGE_MIN..SettingsRepository.PITCH_RANGE_MAX,
            valueLabel = stringResource(R.string.degree_value, pitch),
            onChange = viewModel::setPitchThresholdDeg,
        )

        ToggleRow(
            title = stringResource(R.string.settings_light_mode),
            desc = stringResource(R.string.settings_light_mode_desc),
            checked = lightMode,
            onChange = viewModel::setLightModeEnabled,
        )
        ToggleRow(
            title = stringResource(R.string.settings_safe_banner),
            desc = "",
            checked = safeBanner,
            onChange = viewModel::setSafeBannerEnabled,
        )

        Button(
            onClick = onOpenPrivacy,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.settings_privacy))
        }
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.close))
        }
    }
}

@Composable
private fun ThresholdSlider(
    title: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    valueLabel: String,
    onChange: (Float) -> Unit,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(valueLabel, style = MaterialTheme.typography.titleSmall)
        }
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = range,
        )
    }
}

@Composable
private fun ToggleRow(
    title: String,
    desc: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            if (desc.isNotEmpty()) {
                Text(desc, style = MaterialTheme.typography.bodySmall)
            }
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
