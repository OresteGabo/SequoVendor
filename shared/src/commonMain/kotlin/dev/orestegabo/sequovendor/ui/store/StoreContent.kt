package dev.orestegabo.sequovendor.ui.store

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequovendor.feature.settings.appText
import dev.orestegabo.sequovendor.core.store.isValidOpeningHours
import dev.orestegabo.sequovendor.core.store.isValidTimeRange
import dev.orestegabo.sequovendor.model.OpeningHoursDay
import dev.orestegabo.sequovendor.model.OpeningHoursException
import dev.orestegabo.sequovendor.model.StoreStatus
import dev.orestegabo.sequovendor.model.formatIsoTimestampOrDash
import dev.orestegabo.sequovendor.ui.components.KeyValueRow
import dev.orestegabo.sequovendor.ui.components.VendorCard
import sequovendor.shared.generated.resources.*
import sequovendor.shared.generated.resources.Res
import sequovendor.shared.generated.resources.store_add_exception
import sequovendor.shared.generated.resources.store_closed
import sequovendor.shared.generated.resources.store_closed_toggle
import sequovendor.shared.generated.resources.store_closes_at
import sequovendor.shared.generated.resources.store_copy_monday_weekdays
import sequovendor.shared.generated.resources.store_customer_preview
import sequovendor.shared.generated.resources.store_day_friday
import sequovendor.shared.generated.resources.store_day_monday
import sequovendor.shared.generated.resources.store_day_saturday
import sequovendor.shared.generated.resources.store_day_sunday
import sequovendor.shared.generated.resources.store_day_thursday
import sequovendor.shared.generated.resources.store_day_tuesday
import sequovendor.shared.generated.resources.store_day_wednesday
import sequovendor.shared.generated.resources.store_detail
import sequovendor.shared.generated.resources.store_exception_closed
import sequovendor.shared.generated.resources.store_exception_date
import sequovendor.shared.generated.resources.store_exception_date_placeholder
import sequovendor.shared.generated.resources.store_exception_open
import sequovendor.shared.generated.resources.store_exception_override
import sequovendor.shared.generated.resources.store_hours_invalid
import sequovendor.shared.generated.resources.store_last_updated
import sequovendor.shared.generated.resources.store_mark_closed
import sequovendor.shared.generated.resources.store_mark_open
import sequovendor.shared.generated.resources.store_no_exceptions
import sequovendor.shared.generated.resources.store_open
import sequovendor.shared.generated.resources.store_open_toggle
import sequovendor.shared.generated.resources.store_opens_at
import sequovendor.shared.generated.resources.store_operational_state
import sequovendor.shared.generated.resources.store_preview_closed
import sequovendor.shared.generated.resources.store_preview_closed_reason
import sequovendor.shared.generated.resources.store_preview_open
import sequovendor.shared.generated.resources.store_temporary_exceptions
import sequovendor.shared.generated.resources.store_temporary_reason
import sequovendor.shared.generated.resources.store_temporary_reason_placeholder
import sequovendor.shared.generated.resources.store_title
import sequovendor.shared.generated.resources.store_weekday
import sequovendor.shared.generated.resources.store_weekend
import sequovendor.shared.generated.resources.store_weekly_hours

@Composable
internal fun StoreContent(
    status: StoreStatus,
    closureReason: String,
    onOpenChange: (Boolean) -> Unit,
    onClosureReasonChange: (String) -> Unit,
    onOpeningHoursChange: (List<OpeningHoursDay>) -> Unit,
    onAddTemporaryException: (OpeningHoursException) -> Unit,
    onSaveClosureReason: () -> Unit = {},
    changesEnabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    var weeklyHours by remember(status.merchantId, status.updatedAt) { mutableStateOf(status.openingHours.sortedBy { it.dayOfWeek }) }
    val customerPreview = when {
        status.openStatus == "UNAVAILABLE" -> appText(Res.string.store_status_unavailable)
        !status.configured -> appText(Res.string.store_not_configured)
        status.canAcceptOrders ?: status.open -> appText(Res.string.store_preview_open)
        closureReason.isNotBlank() -> appText(Res.string.store_preview_closed_reason)
            .replace("%1\$s", closureReason)
        status.temporaryClosure?.reason?.isNotBlank() == true -> appText(Res.string.store_preview_closed_reason)
            .replace("%1\$s", status.temporaryClosure.reason.orEmpty())
        else -> appText(Res.string.store_preview_closed)
    }

    LazyColumn(modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(appText(Res.string.store_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(appText(Res.string.store_detail), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        item {
            VendorCard {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(appText(Res.string.store_operational_state), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                storeAvailabilityLabel(status),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        Text(
                            status.openStatus ?: if (status.open) "OPEN" else "PAUSED",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { onOpenChange(true) },
                            enabled = changesEnabled && (!status.configured || !status.open),
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(appText(Res.string.store_mark_open))
                        }
                        OutlinedButton(
                            onClick = { onOpenChange(false) },
                            enabled = changesEnabled && (!status.configured || status.open),
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(appText(Res.string.store_mark_closed))
                        }
                    }

                    if (!status.configured) Text(appText(Res.string.store_timezone_new))
                    KeyValueRow(appText(Res.string.store_weekday), status.weekdayHours.ifBlank { "—" })
                    KeyValueRow(appText(Res.string.store_weekend), status.weekendHours.ifBlank { "—" })
                    KeyValueRow(appText(Res.string.store_last_updated), status.updatedAt.formatIsoTimestampOrDash())
                }
            }
        }

        item {
            VendorCard {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(appText(Res.string.store_weekly_hours), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedButton(
                        onClick = { weeklyHours = copyMondayToWeekdays(weeklyHours) },
                        enabled = changesEnabled && weeklyHours.firstOrNull { it.dayOfWeek == 1 }?.isValidOpeningHours() == true,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(appText(Res.string.store_copy_monday_weekdays))
                    }
                    if (weeklyHours.isEmpty()) {
                        Text(appText(Res.string.store_hours_not_configured))
                        OutlinedButton(enabled = changesEnabled, onClick = { weeklyHours = (1..7).map { OpeningHoursDay(it, "", "") } }) { Text(appText(Res.string.store_setup_hours)) }
                    }
                    weeklyHours.forEach { day ->
                        OpeningHoursRow(
                            day = day,
                            enabled = changesEnabled,
                            onDayChange = { updated ->
                                weeklyHours = weeklyHours.map { if (it.dayOfWeek == updated.dayOfWeek) updated else it }
                            },
                        )
                    }
                    if (weeklyHours.isNotEmpty()) Button(enabled = changesEnabled && weeklyHours.size == 7 && weeklyHours.all { it.isValidOpeningHours() }, onClick = { onOpeningHoursChange(weeklyHours) }) { Text(appText(Res.string.store_save_hours)) }
                }
            }
        }

        item {
            VendorCard {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(appText(Res.string.store_temporary_reason), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = closureReason,
                        onValueChange = { onClosureReasonChange(sanitizeClosureReason(it)) },
                        placeholder = { Text(appText(Res.string.store_temporary_reason_placeholder)) },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(enabled = changesEnabled, onClick = onSaveClosureReason) { Text(appText(Res.string.store_save_closure_note)) }
                }
            }
        }

        item {
            TemporaryExceptionsCard(
                exceptions = status.temporaryExceptions,
                changesEnabled = changesEnabled,
                onAddTemporaryException = onAddTemporaryException,
            )
        }

        item {
            VendorCard {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(appText(Res.string.store_customer_preview), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(customerPreview, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun OpeningHoursRow(
    day: OpeningHoursDay,
    enabled: Boolean,
    onDayChange: (OpeningHoursDay) -> Unit,
) {
    val open = !day.closed
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(day.label(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    if (open) appText(Res.string.store_open_toggle) else appText(Res.string.store_closed_toggle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                enabled = enabled,
                checked = open,
                onCheckedChange = { checked ->
                    onDayChange(
                        if (checked) {
                            day.copy(closed = false)
                        } else {
                            day.copy(closed = true, opensAt = "", closesAt = "")
                        },
                    )
                },
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = day.opensAt,
                onValueChange = { onDayChange(day.copy(opensAt = sanitizeTimeInput(it))) },
                label = { Text(appText(Res.string.store_opens_at)) },
                enabled = enabled && open,
                isError = open && !day.isValidOpeningHours(),
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = day.closesAt,
                onValueChange = { onDayChange(day.copy(closesAt = sanitizeTimeInput(it))) },
                label = { Text(appText(Res.string.store_closes_at)) },
                enabled = enabled && open,
                isError = open && !day.isValidOpeningHours(),
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
        }
        if (open && !day.isValidOpeningHours()) {
            Text(appText(Res.string.store_hours_invalid), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun TemporaryExceptionsCard(
    exceptions: List<OpeningHoursException>,
    changesEnabled: Boolean,
    onAddTemporaryException: (OpeningHoursException) -> Unit,
) {
    var date by remember { mutableStateOf("") }
    var closed by remember { mutableStateOf(true) }
    var opensAt by remember { mutableStateOf("") }
    var closesAt by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    val validHours = closed || isValidTimeRange(opensAt, closesAt)
    val canAdd = date.isNotBlank() && validHours

    VendorCard {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(appText(Res.string.store_temporary_exceptions), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (exceptions.isEmpty()) {
                Text(appText(Res.string.store_no_exceptions), color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                exceptions.forEach { exception ->
                    KeyValueRow(
                        exception.date,
                        buildString {
                            append(if (exception.closed) appText(Res.string.store_exception_closed) else appText(Res.string.store_exception_open))
                            if (!exception.closed) append(" ${exception.opensAt.orEmpty()}-${exception.closesAt.orEmpty()}")
                            exception.reason?.takeIf { it.isNotBlank() }?.let { append(" - $it") }
                        },
                    )
                }
            }

            OutlinedTextField(
                value = date,
                onValueChange = { date = sanitizeDateInput(it) },
                label = { Text(appText(Res.string.store_exception_date)) },
                placeholder = { Text(appText(Res.string.store_exception_date_placeholder)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = { closed = true }, modifier = Modifier.weight(1f)) {
                    Text(appText(Res.string.store_exception_closed))
                }
                OutlinedButton(onClick = { closed = false }, modifier = Modifier.weight(1f)) {
                    Text(appText(Res.string.store_exception_open))
                }
            }
            Text(appText(Res.string.store_exception_override), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (!closed) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = opensAt,
                        onValueChange = { opensAt = sanitizeTimeInput(it) },
                        label = { Text(appText(Res.string.store_opens_at)) },
                        isError = !validHours,
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = closesAt,
                        onValueChange = { closesAt = sanitizeTimeInput(it) },
                        label = { Text(appText(Res.string.store_closes_at)) },
                        isError = !validHours,
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (!validHours) {
                    Text(appText(Res.string.store_hours_invalid), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
            OutlinedTextField(
                value = reason,
                onValueChange = { reason = sanitizeClosureReason(it) },
                label = { Text(appText(Res.string.store_temporary_reason)) },
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = {
                    onAddTemporaryException(
                        OpeningHoursException(
                            date = date,
                            closed = closed,
                            opensAt = if (closed) null else opensAt,
                            closesAt = if (closed) null else closesAt,
                            reason = reason.ifBlank { null },
                        ),
                    )
                    date = ""
                    reason = ""
                },
                enabled = changesEnabled && canAdd,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(appText(Res.string.store_add_exception))
            }
        }
    }
}

@Composable
private fun OpeningHoursDay.label(): String = when (dayOfWeek) {
    1 -> appText(Res.string.store_day_monday)
    2 -> appText(Res.string.store_day_tuesday)
    3 -> appText(Res.string.store_day_wednesday)
    4 -> appText(Res.string.store_day_thursday)
    5 -> appText(Res.string.store_day_friday)
    6 -> appText(Res.string.store_day_saturday)
    else -> appText(Res.string.store_day_sunday)
}

private fun copyMondayToWeekdays(hours: List<OpeningHoursDay>): List<OpeningHoursDay> {
    val monday = hours.firstOrNull { it.dayOfWeek == 1 } ?: return hours
    return hours.map { day ->
        if (day.dayOfWeek in 1..5) {
            monday.copy(dayOfWeek = day.dayOfWeek)
        } else {
            day
        }
    }
}

private fun defaultWeeklyHours(): List<OpeningHoursDay> = listOf(
    OpeningHoursDay(1, "08:00", "18:00"),
    OpeningHoursDay(2, "08:00", "18:00"),
    OpeningHoursDay(3, "08:00", "18:00"),
    OpeningHoursDay(4, "08:00", "18:00"),
    OpeningHoursDay(5, "08:00", "18:00"),
    OpeningHoursDay(6, "09:00", "14:00"),
    OpeningHoursDay(7, "", "", closed = true),
)

private fun sanitizeTimeInput(input: String): String =
    input.filter { it.isDigit() || it == ':' }.take(5)

private fun sanitizeDateInput(input: String): String =
    input.filter { it.isDigit() || it == '-' }.take(10)

private fun sanitizeClosureReason(input: String): String =
    input
        .filter { it.isAllowedClosureReasonChar() }
        .take(MaxClosureReasonLength)

private fun Char.isAllowedClosureReasonChar(): Boolean =
    isLetterOrDigit() ||
        isWhitespace() ||
        this in setOf('.', ',', '\'', '"', '’', '-', '/', '!', '?', ':', ';', '(', ')')

private const val MaxClosureReasonLength = 120
