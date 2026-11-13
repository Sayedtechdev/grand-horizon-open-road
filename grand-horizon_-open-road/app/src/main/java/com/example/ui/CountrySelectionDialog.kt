package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.CountryRoute
import com.example.model.CountryType

@Composable
fun CountrySelectionDialog(
    currentCountry: CountryType,
    onCountrySelected: (CountryType) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val turkeyDrawableId = context.resources.getIdentifier("img_turkey_highway", "drawable", context.packageName)
    val bdDrawableId = context.resources.getIdentifier("img_bangladesh_highway", "drawable", context.packageName)

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
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Choose Your Country Road",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Open-world exploration with authentic road graphics",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_country_dialog_btn")) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // OPTION 1: TURKEY (Start: Istanbul)
                CountryCard(
                    country = CountryRoute.TURKEY,
                    isSelected = currentCountry == CountryType.TURKEY,
                    imageResId = turkeyDrawableId,
                    testTag = "country_card_turkey",
                    onSelect = {
                        onCountrySelected(CountryType.TURKEY)
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // OPTION 2: BANGLADESH (Start: Chittagong)
                CountryCard(
                    country = CountryRoute.BANGLADESH,
                    isSelected = currentCountry == CountryType.BANGLADESH,
                    imageResId = bdDrawableId,
                    testTag = "country_card_bangladesh",
                    onSelect = {
                        onCountrySelected(CountryType.BANGLADESH)
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
private fun CountryCard(
    country: CountryRoute,
    isSelected: Boolean,
    imageResId: Int,
    testTag: String,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = if (isSelected) 2.5.dp else 1.dp,
                color = if (isSelected) country.primaryColor else Color(0xFF334155),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onSelect() }
            .testTag(testTag),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Scenic Real Highway Photo Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                if (imageResId != 0) {
                    Image(
                        painter = painterResource(id = imageResId),
                        contentDescription = country.displayName,
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(country.primaryColor, Color(0xFF0F172A))
                                )
                            )
                    )
                }

                // Overlay gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xFF1E293B))
                            )
                        )
                )

                // Country Flag & Name Badge
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(country.flagEmoji, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            country.displayName,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            country.localName,
                            fontSize = 11.sp,
                            color = Color(0xFFE2E8F0)
                        )
                    }
                }

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(country.primaryColor)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ACTIVE ROUTE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // Route Details & Start City Notice
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = country.primaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Starting City: ${country.startCity}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFDE047)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Route,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "${country.cities.size} Major Cities",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    country.description,
                    fontSize = 11.sp,
                    color = Color(0xFFCBD5E1),
                    lineHeight = 15.sp,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(10.dp))

                // City chips preview
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    country.cities.take(4).forEach { city ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF0F172A))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(city.name, fontSize = 10.sp, color = Color(0xFF94A3B8))
                        }
                    }
                    Text("+${country.cities.size - 4} more", fontSize = 10.sp, color = Color(0xFF64748B), modifier = Modifier.align(Alignment.CenterVertically))
                }
            }
        }
    }
}
