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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
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
import com.example.model.CountryType
import com.example.model.TravelCompanion

@Composable
fun CompanionSelectionDialog(
    selectedCompanion: TravelCompanion,
    onCompanionSelected: (TravelCompanion) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val companionDrawableId = context.resources.getIdentifier("img_companion_copilot", "drawable", context.packageName)

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
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Choose Person (Co-Pilot)",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            "Select Sayed, Hena, or Afia for your journey",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_companion_dialog_btn")) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // FEATURED SELECTED PERSON HERO BANNER
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(125.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            1.5.dp,
                            if (selectedCompanion.gender == "Male") Color(0xFF0284C7) else Color(0xFFFB7185),
                            RoundedCornerShape(16.dp)
                        )
                ) {
                    if (companionDrawableId != 0) {
                        Image(
                            painter = painterResource(id = companionDrawableId),
                            contentDescription = "Travel Person",
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(125.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color(0xFF0F172A).copy(alpha = 0.95f))
                                )
                            )
                    )
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (selectedCompanion.gender == "Male") Color(0xFF0284C7) else Color(0xFFE11D48)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(selectedCompanion.personalityEmoji, fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "${selectedCompanion.displayName}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (selectedCompanion.gender == "Male") Color(0xFF0284C7) else Color(0xFFE11D48))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        if (selectedCompanion.gender == "Male") "MALE • HANDSOME" else "FEMALE • BEAUTIFUL",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }
                            }
                            Text(
                                "\"${selectedCompanion.dialogueQuotes.first()}\"",
                                fontSize = 10.sp,
                                color = Color(0xFFFDE047),
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // LIST OF 3 PEOPLE (Sayed, Hena, Afia)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(290.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(TravelCompanion.entries) { companion ->
                        val isSelected = companion == selectedCompanion
                        val accentColor = if (companion.gender == "Male") Color(0xFF38BDF8) else Color(0xFFFB7185)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) accentColor else Color(0xFF334155),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    onCompanionSelected(companion)
                                }
                                .testTag("companion_${companion.id}"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF1E293B) else Color(0xFF111827)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (companion.gender == "Male") Color(0xFF0369A1) else Color(0xFFBE185D)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(companion.personalityEmoji, fontSize = 24.sp)
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(companion.displayName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (companion.gender == "Male") Color(0xFF0284C7).copy(alpha = 0.35f) else Color(0xFFE11D48).copy(alpha = 0.35f))
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                if (companion.gender == "Male") "Male (Handsome)" else "Female (Beautiful)",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (companion.gender == "Male") Color(0xFF38BDF8) else Color(0xFFFB7185)
                                            )
                                        }
                                    }
                                    Text(companion.title, fontSize = 11.sp, color = accentColor, fontWeight = FontWeight.Medium)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(companion.bio, fontSize = 10.sp, color = Color(0xFFCBD5E1), maxLines = 2, lineHeight = 13.sp)
                                }

                                if (isSelected) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = accentColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
