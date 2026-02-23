package com.inumaki.features.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.FlingBehavior
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inumaki.core.ui.components.PosterCard
import com.inumaki.core.ui.modifiers.shiningBorder
import com.inumaki.core.ui.theme.AppTheme
import com.inumaki.features.discover.components.CarouselCard
import com.inumaki.features.discover.model.DiscoverList

@Composable
fun DiscoverViewSuccess(items: List<DiscoverList>, angle: Float) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
        horizontalAlignment = Alignment.Start
    ) {
        LazyColumn(
            contentPadding = PaddingValues(top = 92.dp, bottom = 100.dp),
        ) {
            item {
                CarouselCard(items[0].list[0], angle)
            }
            items(items.drop(1)) { list ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        list.title,
                        style = AppTheme.typography.title3,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(AppTheme.layout.contentPadding)
                    )

                    Row(
                        modifier = Modifier
                            .padding(end = 24.dp)
                            .shiningBorder(angle, 50.dp, AppTheme.colors.overlay)
                            .clip(RoundedCornerShape(50))
                            .background(AppTheme.colors.overlay)
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text("more", fontSize = 10.sp)
                    }
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