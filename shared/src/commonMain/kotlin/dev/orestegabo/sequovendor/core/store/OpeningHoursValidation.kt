package dev.orestegabo.sequovendor.core.store

import dev.orestegabo.sequovendor.model.OpeningHoursDay

fun OpeningHoursDay.isValidOpeningHours(): Boolean =
    closed || isValidTimeRange(opensAt, closesAt)

fun isValidTimeRange(opensAt: String, closesAt: String): Boolean =
    isValidOpeningHoursTime(opensAt) && isValidOpeningHoursTime(closesAt) && opensAt < closesAt

fun isValidOpeningHoursTime(value: String): Boolean {
    if (!Regex("\\d{2}:\\d{2}").matches(value)) return false
    val hour = value.substring(0, 2).toIntOrNull() ?: return false
    val minute = value.substring(3, 5).toIntOrNull() ?: return false
    return hour in 0..23 && minute in 0..59
}
