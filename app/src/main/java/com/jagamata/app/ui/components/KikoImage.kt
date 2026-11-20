package com.jagamata.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.jagamata.app.R

/** Varian ekspresi Kiko sesuai component set desain (Ekspresi=…). */
enum class KikoExpression(
    @DrawableRes val res: Int,
) {
    NETRAL(R.drawable.kiko_netral),
    SENANG(R.drawable.kiko_senang),
    FOKUS(R.drawable.kiko_fokus),
    KAGET(R.drawable.kiko_kaget),
    BANGGA(R.drawable.kiko_bangga),
    NGANTUK(R.drawable.kiko_ngantuk),
    SENANG_KACAMATA(R.drawable.kiko_senang_kacamata),
    FOKUS_PENGARIS(R.drawable.kiko_fokus_penggaris),
}

/**
 * Maskot Kiko — aset persis dari FigCraft (component set "Kiko", scale 4x).
 * Ganti semua Canvas/drawable mascot lama dengan composable ini.
 */
@Composable
fun KikoImage(
    modifier: Modifier = Modifier,
    expression: KikoExpression = KikoExpression.SENANG,
    contentDescription: String? = null,
) {
    Image(
        painter = painterResource(expression.res),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Fit,
    )
}
