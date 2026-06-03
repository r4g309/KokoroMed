package dev.r4g309.kokoromed.domain.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)

fun todayKey(date: Date = Date()): String = fmt.format(date)
