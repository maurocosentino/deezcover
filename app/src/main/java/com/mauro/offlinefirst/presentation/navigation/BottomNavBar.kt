package com.mauro.offlinefirst.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.mauro.offlinefirst.ui.theme.SurfaceDark

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
        BottomNavItemData(BottomNavTab.Home, Icons.Default.Home, "Inicio", onHomeClick),
        BottomNavItemData(
            BottomNavTab.Search,
            Icons.Default.Search,
            "Buscar",
            onClick = {
                if (selectedTab == BottomNavTab.Search) {
                    onSearchDoubleTap()
                } else {
                    onSearchClick()
                }
            }
        ),
        BottomNavItemData(BottomNavTab.Charts, Icons.Default.BarChart, "Charts", onChartsClick)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .navigationBarsPadding()
            .padding(bottom = 2.dp)

    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(
                    Color(0xFF1A1A1A),
                    RoundedCornerShape(
                        topStart = 8.dp,
                        topEnd = 8.dp,
                        bottomStart = 22.dp,
                        bottomEnd = 22.dp
                    )
                )
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items.forEach { item ->
                BottomNavItem(
                    item = item,
                    selected = item.tab == selectedTab
                )
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.BottomNavItem(
    item: BottomNavItemData,
    selected: Boolean
) {
    val iconTint = if (selected) Color.White else Color.White.copy(alpha = 0.5f)
    val labelColor = if (selected) Color.White else Color.White.copy(alpha = 0.5f)

    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(50.dp))
            .clickable(onClick = item.onClick)
            .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .background(Color(0xFF2A2A2A), RoundedCornerShape(50.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.tab.name,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = labelColor
                    )
                }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.tab.name,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor
                )
            }
        }
    }
}

private data class BottomNavItemData(
    val tab: BottomNavTab,
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit
)
