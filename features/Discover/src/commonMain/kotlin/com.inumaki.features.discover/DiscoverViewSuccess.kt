package com.inumaki.features.discover

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.inumaki.core.ui.components.PosterCard
import com.inumaki.core.ui.theme.AppTheme
import com.inumaki.features.discover.components.CarouselCard
import com.inumaki.features.discover.model.DiscoverList

@Composable
fun DiscoverViewSuccess(items: List<DiscoverList>, angle: Float) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.Start
    ) {
        LazyColumn(
            contentPadding = PaddingValues(top = 92.dp, bottom = 100.dp)
        ) {
            item {
                CarouselCard(items[0].list[0], angle)
            }
            items(items.drop(1)) { list ->
                Row {
                    Text(
                        list.title,
                        style = AppTheme.typography.headline,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .padding(AppTheme.layout.contentPadding)
                            .padding(bottom = 12.dp)
                    )
                }
                LazyRow(
                    modifier = Modifier
                        .padding(bottom = 20.dp)
                        .fillMaxWidth(),
                    contentPadding = AppTheme.layout.contentPadding,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(list.list) { item ->
                        PosterCard(item, angle)
                    }
                }
            }
        }
    }
}