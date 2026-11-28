package com.jagamata.app.data

import java.util.Calendar
import java.util.TimeZone

/**
 * Kunci hari lokal (M8) — pengganti `ms / 86_400_000` (UTC) yang menaruh
 * rentang 00:00–07:00 WIB (dan zona timur lain) di hari sebelumnya.
 *
 * [localDayEpoch] = epoch-day tanggal lokal (`LocalDate.toEpochDay()` — hari ke-n
 * sejak 1970-01-01 UTC untuk tanggal tersebut), dihitung dengan [Calendar]
 * agar aman di minSdk 24 (tanpa core-library desugaring).
 *
 * CATATAN (sengaja tanpa migrasi): baris `daily_stats` lama memakai
 * `ms / 86_400_000` sehingga untuk zona UTC+7 baris siang hari sama persis
 * dengan kunci baru, tetapi baris 00:00–07:00 bergeser 1 hari ke belakang.
 * Migrasi data dilarang (risiko migrasi destruktif) — selisih maksimal 1 hari
 * pada riwayat lama didokumentasikan di laporan audit.
 *
 * Semua fungsi murni dan menerima [TimeZone] eksplisit agar bisa diuji
 * deterministik (zona perangkat = [TimeZone.getDefault]).
 */
fun localDayEpoch(
    epochMs: Long,
    zone: TimeZone = TimeZone.getDefault(),
): Long {
    // Tanggal lokal dari timestamp, lalu "epoch-day" tanggal itu:
    // tengah malam UTC dari tanggal yang sama / 86_400_000 = hari ke-n sejak epoch.
    val local = Calendar.getInstance(zone).apply { timeInMillis = epochMs }
    val utcMidnight =
        Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(
                local.get(Calendar.YEAR),
                local.get(Calendar.MONTH),
                local.get(Calendar.DAY_OF_MONTH),
                0,
                0,
                0,
            )
        }
    return utcMidnight.timeInMillis / DAY_MS
}

/** Awal hari lokal (00:00.000 zona [zone]) untuk kunci [dayEpoch]. */
fun localDayStartMs(
    dayEpoch: Long,
    zone: TimeZone = TimeZone.getDefault(),
): Long {
    val utcDay =
        Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            timeInMillis = dayEpoch * DAY_MS
        }
    return Calendar.getInstance(zone).apply {
        clear()
        set(
            utcDay.get(Calendar.YEAR),
            utcDay.get(Calendar.MONTH),
            utcDay.get(Calendar.DAY_OF_MONTH),
            0,
            0,
            0,
        )
    }.timeInMillis
}

/** Akhir hari lokal (eksklusif) = awal hari berikutnya untuk kunci [dayEpoch]. */
fun localDayEndMs(
    dayEpoch: Long,
    zone: TimeZone = TimeZone.getDefault(),
): Long = localDayStartMs(dayEpoch + 1, zone)

/** [count] hari kalender lokal berakhir pada [endDayEpoch] (inklusif), urut naik. */
fun localDaysEndingAt(
    endDayEpoch: Long,
    count: Int,
): List<Long> = (endDayEpoch - count + 1..endDayEpoch).toList()

/**
 * Inisial hari (Senin..Minggu → S S R K J S M) dari kunci hari lokal —
 * label sumbu-x grafik dari tanggal sebenarnya, bukan string statis (T14).
 */
fun dayInitial(dayEpoch: Long): String {
    // 1970-01-01 (epoch-day 0) = Kamis.
    val idx = (((dayEpoch % 7L) + 7L) % 7L).toInt()
    return when (idx) {
        0 -> "K" // Kamis
        1 -> "J" // Jumat
        2 -> "S" // Sabtu
        3 -> "M" // Minggu
        4 -> "S" // Senin
        5 -> "S" // Selasa
        else -> "R" // Rabu
    }
}

/** Nama hari panjang lokal (Senin, 1 November) — label tanggal Statistik Hari Ini. */
fun localDayLabel(
    epochMs: Long,
    zone: TimeZone = TimeZone.getDefault(),
): String {
    val fmt = java.text.SimpleDateFormat("EEEE, d MMMM", java.util.Locale("id"))
    fmt.timeZone = zone
    return fmt.format(java.util.Date(epochMs))
}

private const val DAY_MS = 86_400_000L
