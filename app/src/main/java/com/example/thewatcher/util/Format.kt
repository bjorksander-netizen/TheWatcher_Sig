package com.example.thewatcher.util

fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return "%.1f KB".format(kb)
    val mb = kb / 1024.0
    if (mb < 1024) return "%.1f MB".format(mb)
    val gb = mb / 1024.0
    return "%.2f GB".format(gb)
}

fun formatMac(mac: String): String {
    return if (mac.length >= 8) {
        val head = mac.take(5)
        val tail = mac.takeLast(2)
        "$head…$tail"
    } else mac
}

fun formatDuration(ms: Long): String {
    val s = (ms / 1000).coerceAtLeast(0)
    val m = s / 60
    val rem = s % 60
    return if (m > 0) "${m}m ${rem}s" else "${rem}s"
}

fun formatDateTime(ms: Long): String {
    val sdf = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(ms))
}
