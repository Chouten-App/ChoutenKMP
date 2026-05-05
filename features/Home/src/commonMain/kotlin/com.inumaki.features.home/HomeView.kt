package com.inumaki.features.home


import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inumaki.core.ui.theme.AppTheme
import dev.chouten.runners.relay.RelayLogger
import kotlinx.datetime.*
import kotlinx.datetime.format.char
import kotlin.time.Instant


fun formatInstant(instant: Instant): String {
    val zone = TimeZone.currentSystemDefault() // or TimeZone.of("Europe/Oslo")
    val dateTime = instant.toLocalDateTime(zone)

    val day = dateTime.dayOfMonth.toString().padStart(2, '0')
    val month = dateTime.monthNumber.toString().padStart(2, '0')
    val year = dateTime.year

    val hour24 = dateTime.hour
    val minute = dateTime.minute.toString().padStart(2, '0')

    val amPm = if (hour24 < 12) "AM" else "PM"
    val hour12 = when {
        hour24 == 0 -> 12
        hour24 > 12 -> hour24 - 12
        else -> hour24
    }

    return "[$day/$month/$year, $hour12:$minute $amPm]"
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeView() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 120.dp, bottom = 100.dp)
    ) {
        itemsIndexed(RelayLogger.logs) { index, log ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(
                        topStart = if (index == 0) 20.dp else 0.dp,
                        topEnd = if (index == 0) 20.dp else 0.dp,
                        bottomStart = if (index == RelayLogger.logs.lastIndex) 20.dp else 0.dp,
                        bottomEnd = if (index == RelayLogger.logs.lastIndex) 20.dp else 0.dp
                    ))
                    .background(AppTheme.colors.container)
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = if (index == 0) 16.dp else 0.dp,
                        bottom = if (index == RelayLogger.logs.lastIndex) 16.dp else 0.dp
                    ),
                horizontalAlignment = Alignment.Start
            ) {
                Text(formatInstant(log.time), style = AppTheme.typography.body.copy(fontSize = 12.sp), modifier = Modifier.alpha(0.7f).padding(bottom = 8.dp))
                Text(log.message, style = AppTheme.typography.body.copy(fontSize = 12.sp), modifier = Modifier.alpha(0.7f))

                if (index != RelayLogger.logs.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        thickness = 1.dp,
                        color = AppTheme.colors.border.copy(0.7f)
                    )
                }
            }
        }
    }
}
