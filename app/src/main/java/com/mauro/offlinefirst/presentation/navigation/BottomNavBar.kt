package com.mauro.offlinefirst.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.mauro.offlinefirst.ui.theme.DeezerColor

enum class BottomNavTab {
    Home,
    Search,
    Charts
}

@Composable
fun BottomNavBar(
    selectedTab: BottomNavTab,
    onHomeClick: () -> Unit,
    onSearchClick: () -> Unit,
    onSearchDoubleTap: () -> Unit,
    onChartsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItemData(BottomNavTab.Home, Icons.Default.Home, onHomeClick),
        BottomNavItemData(
            BottomNavTab.Search,
            Icons.Default.Search,
            onClick = {
                if (selectedTab == BottomNavTab.Search) {
                    onSearchDoubleTap()
                } else {
                    onSearchClick()
                }
            }
        ),
        BottomNavItemData(BottomNavTab.Charts, Icons.Default.BarChart, onChartsClick)
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF000409))
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items.forEach { item ->
            BottomNavItem(
                item = item,
                selected = item.tab == selectedTab
            )
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.BottomNavItem(
    item: BottomNavItemData,
    selected: Boolean
) {
    val iconTint = if (selected) Color.White else Color.White.copy(alpha = 0.5f)

    Box(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Transparent)
            .clickable(onClick = item.onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.tab.name,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
    }
}

private data class BottomNavItemData(
    val tab: BottomNavTab,
    val icon: ImageVector,
    val onClick: () -> Unit
)
