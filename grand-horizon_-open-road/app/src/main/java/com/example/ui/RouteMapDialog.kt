package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.CountryRoute

@Composable
fun RouteMapDialog(
    country: CountryRoute,
    currentOdometerKm: Float,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, Color(0xFF334155), RoundedCornerShape(24.dp)),
            color = Color(0xFF0F172A)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(country.flagEmoji, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "${country.displayName} Nationwide Route",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            country.routeCode,
                            fontSize = 11.sp,
                            color = country.primaryColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_map_dialog_btn")) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Current Route Progress Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("CURRENT JOURNEY PROGRESS", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                            Text("${String.format("%.1f", currentOdometerKm)} KM Explored", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFFFDE047))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("STARTING CITY", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                            Text(country.startCity, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "Connected Cities & Landmarks:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Vertical List of All 10 Major Cities in Country
                val cityProgress = country.findCityProgress(currentOdometerKm)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(country.cities) { index, city ->
                        val isStart = index == 0
                        val isPassed = index <= cityProgress.currentCityIndex
                        val isCurrentTarget = index == cityProgress.nextCityIndex

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    when {
                                        isCurrentTarget -> Color(0xFF0369A1).copy(alpha = 0.5f)
                                        isPassed -> Color(0xFF1E293B)
                                        else -> Color(0xFF111827)
                                    }
                                )
                                .border(
                                    width = if (isCurrentTarget) 1.5.dp else 1.dp,
                                    color = if (isCurrentTarget) Color(0xFF38BDF8) else Color(0xFF334155),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Milestone indicator
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isStart -> Color(0xFFE11D48)
                                            isPassed -> Color(0xFF10B981)
                                            isCurrentTarget -> Color(0xFF0284C7)
                                            else -> Color(0xFF475569)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isPassed) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                } else if (isCurrentTarget) {
                                    Icon(Icons.Default.Navigation, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                } else {
                                    Text("${index + 1}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(city.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("(${city.localName})", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                }
                                Text(city.landmarkName, fontSize = 11.sp, color = country.primaryColor, fontWeight = FontWeight.Medium)
                                Text(city.landmarkDescription, fontSize = 10.sp, color = Color(0xFFCBD5E1), maxLines = 1)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    if (isStart) "START" else "+${city.distanceKm.toInt()} km",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isStart) Color(0xFFFDE047) else Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
