package com.jagamata.app.ui.parent

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jagamata.app.R
import com.jagamata.app.data.SettingsRepository
import com.jagamata.app.ui.components.JagaCard
import com.jagamata.app.ui.components.JagaSwitch
import com.jagamata.app.ui.components.KikoExpression
import com.jagamata.app.ui.components.KikoImage
import com.jagamata.app.ui.theme.Background
import com.jagamata.app.ui.theme.BlueTint
import com.jagamata.app.ui.theme.BlueTintDeep
import com.jagamata.app.ui.theme.Divider
import com.jagamata.app.ui.theme.Ink
import com.jagamata.app.ui.theme.InkSoft
import com.jagamata.app.ui.theme.Primary
import com.jagamata.app.ui.theme.Sunny
import com.jagamata.app.ui.theme.Surface
import com.jagamata.app.ui.theme.SurfaceAlt
import com.jagamata.app.ui.viewModel.SettingsViewModel
import kotlinx.coroutines.launch

/** Top bar layar desain: tombol kembali 44 + judul H2 (18) di tengah. */
@Composable
internal fun JagaTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(top = 11.dp)
                .height(44.dp),
    ) {
        BackCircle(
            onBack = onBack,
            modifier = Modifier.align(Alignment.CenterStart),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = Ink,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

/** Tombol kembali lingkaran putih 44×44 r22 + chevron (Shadow Card). */
@Composable
internal fun BackCircle(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(44.dp)
                .shadow(4.dp, RoundedCornerShape(22.dp))
                .clip(RoundedCornerShape(22.dp))
                .background(Surface)
                .clickable(onClick = onBack),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.KeyboardArrowLeft,
            contentDescription = stringResource(R.string.onboarding_back),
            tint = Ink,
            modifier = Modifier.size(28.dp),
        )
    }
}

/**
 * Kartu hero desain: gradasi #D6E6FF→#B7D2FF, r24, tinggi 154,
 * maskot Kiko kiri + gelembung bicara putih kanan.
 */
@Composable
internal fun MascotHeroCard(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(154.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.verticalGradient(listOf(BlueTint, BlueTintDeep))),
    ) {
        HeroSparkle(
            color = Color.White,
            side = 13.dp,
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 19.dp, top = 17.dp),
        )
        HeroSparkle(
            color = Color.White,
            side = 10.dp,
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 125.dp, top = 31.dp),
        )
        HeroSparkle(
            color = Color.White,
            side = 12.dp,
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 323.dp, top = 15.dp),
        )
        Box(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 136.dp, y = 40.dp)
                    .size(12.dp)
                    .rotate(45f)
                    .background(Surface, RoundedCornerShape(3.dp)),
        )
        KikoImage(
            expression = KikoExpression.SENANG,
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp)
                    .width(112.dp)
                    .height(130.dp),
        )
        Box(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 146.dp, top = 18.dp)
                    .size(187.dp, 94.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Surface),
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = Ink,
                modifier = Modifier.padding(14.dp),
            )
        }
    }
}

/** Caption footer desain: Nunito 13/600, Ink 72%, rata tengah. */
@Composable
internal fun FooterCaption(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = Ink.copy(alpha = 0.72f),
        textAlign = TextAlign.Center,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
    )
}

/** Dekorasi bawah layar: kuning kecil kiri + awan putih kanan. */
@Composable
internal fun ScreenDecorations(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(40.dp),
    ) {
        HeroSparkle(
            color = Sunny,
            side = 13.dp,
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 51.dp, top = 19.dp),
        )
        Icon(
            imageVector = Icons.Filled.Cloud,
            contentDescription = null,
            tint = Surface,
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 13.dp)
                    .size(width = 68.dp, height = 40.dp),
        )
    }
}

/** Bintang 4 sudut (dekorasi). */
@Composable
internal fun HeroSparkle(
    color: Color,
    side: Dp,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(side)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f
        val k = cx * 0.26f
        val path =
            Path().apply {
                moveTo(cx, 0f)
                cubicTo(cx + k, cy - k, cx + k, cy - k, w, cy)
                cubicTo(cx + k, cy + k, cx + k, cy + k, cx, h)
                cubicTo(cx - k, cy + k, cx - k, cy + k, 0f, cy)
                cubicTo(cx - k, cy - k, cx - k, cy - k, cx, 0f)
                close()
            }
        drawPath(path = path, color = color)
    }
}

/** Toggle desain: track 52×26 r13, knob putih 22dp — delegasi ke [JagaSwitch]. */
@Composable
internal fun DesignSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    JagaSwitch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
    )
}

/**
 * Baris kartu desain: ikon lingkaran 40 (#E1EFFF), label Nunito 16,
 * nilai 13 Ink 72% opsional, trailing chevron/switch.
 */
@Composable
internal fun SettingsRow(
    icon: @Composable () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    value: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit = {},
) {
    val clickModifier =
        if (onClick != null) {
            Modifier.clickable(onClick = onClick)
        } else {
            Modifier
        }
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = 40.dp)
                .then(clickModifier),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceAlt),
            contentAlignment = Alignment.Center,
        ) {
            icon()
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = Ink,
            modifier = Modifier.weight(1f),
        )
        if (value != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = Ink.copy(alpha = 0.72f),
                maxLines = 1,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        trailing()
    }
}

/** Ikon baris 20dp, tint Ink (di dalam lingkaran #E1EFFF). */
@Composable
internal fun RowIcon(imageVector: androidx.compose.ui.graphics.vector.ImageVector) {
    Icon(
        imageVector = imageVector,
        contentDescription = null,
        tint = Ink,
        modifier = Modifier.size(20.dp),
    )
}

/** Chevron kanan 24dp penutup baris desain. */
@Composable
internal fun ChevronTrailing() {
    Icon(
        imageVector = Icons.Filled.KeyboardArrowRight,
        contentDescription = null,
        tint = Ink,
        modifier = Modifier.size(24.dp),
    )
}

/**
 * Pengaturan orang tua (PRD §2.17, US-11).
 * Slider ambang: jarak 20–50 cm, cahaya 50–500 lux, postur 20–60°.
 * Perubahan langsung berlaku ke state machine tanpa restart service.
 * Layout mengikuti desain 2823 (kartu hero + baris desain), logika asli
 * dipertahankan pada kartu pengaturan lanjutan di bawah.
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenCalibration: () -> Unit = {},
    onOpenChildProfile: () -> Unit = {},
    onOpenPermissions: () -> Unit = {},
    viewModel: SettingsViewModel =
        SettingsViewModel(
            androidx.compose.ui.platform.LocalContext.current.applicationContext as android.app.Application,
        ),
) {
    var notifOn by remember { mutableStateOf(true) }
    var darkOn by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val settingsRepo = remember { SettingsRepository(context) }
    val voiceOn by settingsRepo.voiceEnabled.collectAsState(initial = true)

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Background)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
    ) {
        JagaTopBar(
            title = stringResource(R.string.settings_title),
            onBack = onBack,
        )
        Spacer(modifier = Modifier.height(16.dp))
        MascotHeroCard(text = stringResource(R.string.s10_hero))
        Spacer(modifier = Modifier.height(16.dp))
        DesignSettingsCard(
            notifOn = notifOn,
            onNotifChange = { notifOn = it },
            darkOn = darkOn,
            onDarkChange = { darkOn = it },
            voiceOn = voiceOn,
            onVoiceChange = { scope.launch { settingsRepo.setVoiceEnabled(it) } },
            onOpenCalibration = onOpenCalibration,
            onOpenChildProfile = onOpenChildProfile,
            onOpenPermissions = onOpenPermissions,
        )
        FooterCaption(text = stringResource(R.string.s10_footer))
        Spacer(modifier = Modifier.height(116.dp))
        ScreenDecorations()
        Spacer(modifier = Modifier.height(200.dp))
        AdvancedSettingsCard(
            viewModel = viewModel,
            onOpenPrivacy = onOpenPrivacy,
        )
        Spacer(modifier = Modifier.height(60.dp))
    }
}

/** Kartu baris sesuai desain 2823: Notifikasi · Kalibrasi · Bahasa · Mode Gelap · Profil · Izin. */
@Composable
private fun DesignSettingsCard(
    notifOn: Boolean,
    onNotifChange: (Boolean) -> Unit,
    darkOn: Boolean,
    onDarkChange: (Boolean) -> Unit,
    voiceOn: Boolean,
    onVoiceChange: (Boolean) -> Unit,
    onOpenCalibration: () -> Unit,
    onOpenChildProfile: () -> Unit,
    onOpenPermissions: () -> Unit,
) {
    JagaCard(contentPadding = PaddingValues(16.dp)) {
        SettingsRow(
            icon = { RowIcon(Icons.Outlined.Face) },
            title = "Profil Anak (Usia & Avatar)",
            onClick = onOpenChildProfile,
            trailing = { ChevronTrailing() },
        )
        Spacer(modifier = Modifier.height(24.dp))
        SettingsRow(
            icon = { RowIcon(Icons.Outlined.PhotoCamera) },
            title = stringResource(R.string.s10_row_calibration),
            onClick = onOpenCalibration,
            trailing = { ChevronTrailing() },
        )
        Spacer(modifier = Modifier.height(24.dp))
        SettingsRow(
            icon = { RowIcon(Icons.Outlined.Lock) },
            title = "Kelola Izin & Akses Sistem",
            onClick = onOpenPermissions,
            trailing = { ChevronTrailing() },
        )
        Spacer(modifier = Modifier.height(24.dp))
        SettingsRow(
            icon = { RowIcon(Icons.Filled.VolumeUp) },
            title = "Suara Ramah Maskot Kiko",
            trailing = { DesignSwitch(checked = voiceOn, onCheckedChange = onVoiceChange) },
        )
        Spacer(modifier = Modifier.height(24.dp))
        SettingsRow(
            icon = { RowIcon(Icons.Outlined.Notifications) },
            title = stringResource(R.string.s10_row_notif),
            trailing = { DesignSwitch(checked = notifOn, onCheckedChange = onNotifChange) },
        )
        Spacer(modifier = Modifier.height(24.dp))
        SettingsRow(
            icon = { RowIcon(Icons.Outlined.Language) },
            title = stringResource(R.string.s10_row_language),
            value = stringResource(R.string.s10_lang_value),
            trailing = { ChevronTrailing() },
        )
        Spacer(modifier = Modifier.height(24.dp))
        SettingsRow(
            icon = { RowIcon(Icons.Outlined.DarkMode) },
            title = stringResource(R.string.s10_row_dark),
            trailing = { DesignSwitch(checked = darkOn, onCheckedChange = onDarkChange) },
        )
    }
}

/** Pengaturan lanjutan (logika asli): 3 ambang, Mode Ringan, banner aman, Privasi. */
@Composable
private fun AdvancedSettingsCard(
    viewModel: SettingsViewModel,
    onOpenPrivacy: () -> Unit,
) {
    val closeCm by viewModel.closeThresholdCm.collectAsState()
    val lux by viewModel.luxThreshold.collectAsState()
    val pitch by viewModel.pitchThresholdDeg.collectAsState()
    val lightMode by viewModel.lightModeEnabled.collectAsState()
    val safeBanner by viewModel.safeBannerEnabled.collectAsState()

    JagaCard(contentPadding = PaddingValues(16.dp)) {
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
        HorizontalDivider(
            color = Divider,
            modifier = Modifier.padding(vertical = 12.dp),
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
        SettingsRow(
            icon = { RowIcon(Icons.Outlined.Lock) },
            title = stringResource(R.string.settings_privacy),
            onClick = onOpenPrivacy,
            trailing = { ChevronTrailing() },
        )
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
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = Ink)
            Text(valueLabel, style = MaterialTheme.typography.bodySmall, color = Ink)
        }
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = range,
            modifier = Modifier.fillMaxWidth(),
            colors =
                SliderDefaults.colors(
                    activeTrackColor = Primary,
                    inactiveTrackColor = SurfaceAlt,
                    thumbColor = Primary,
                ),
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
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = Ink)
            if (desc.isNotEmpty()) {
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSoft,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        DesignSwitch(checked = checked, onCheckedChange = onChange)
    }
}
