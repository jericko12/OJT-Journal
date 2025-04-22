package com.protech.ojtjournal.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Task
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.protech.ojtjournal.data.JournalEntry
import com.protech.ojtjournal.ui.theme.accentGreen
import com.protech.ojtjournal.ui.theme.accentOrange
import com.protech.ojtjournal.ui.theme.accentPink
import com.protech.ojtjournal.ui.theme.primaryLight
import java.util.Locale
import kotlinx.datetime.LocalDate

@Composable
fun JournalCard(
    entry: JournalEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Date row
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = LocalDate.parse(entry.date).let { 
                        "${it.dayOfMonth} ${it.month.name.lowercase().replaceFirstChar { c -> c.uppercase() }} ${it.year}" 
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Title
            Text(
                text = entry.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Content preview
            Text(
                text = entry.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            // If there are additional sections, show indicators
            if (entry.tasksDone.isNotEmpty() || entry.learnings.isNotEmpty() || 
                entry.challenges.isNotEmpty() || entry.nextDayPlans.isNotEmpty() ||
                entry.location.isNotEmpty()) {
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Tasks
                    if (entry.tasksDone.isNotEmpty()) {
                        SectionIndicator(
                            color = accentGreen,
                            icon = Icons.Outlined.Task,
                            contentDescription = "Tasks completed"
                        )
                    }
                    
                    // Learnings
                    if (entry.learnings.isNotEmpty()) {
                        SectionIndicator(
                            color = accentOrange,
                            icon = Icons.Outlined.Lightbulb,
                            contentDescription = "Learnings"
                        )
                    }
                    
                    // Challenges
                    if (entry.challenges.isNotEmpty()) {
                        SectionIndicator(
                            color = accentPink,
                            icon = Icons.Outlined.Psychology,
                            contentDescription = "Challenges"
                        )
                    }
                    
                    // Next day plans
                    if (entry.nextDayPlans.isNotEmpty()) {
                        SectionIndicator(
                            color = primaryLight,
                            icon = Icons.Outlined.Description,
                            contentDescription = "Plans for next day"
                        )
                    }
                    
                    // Location
                    if (entry.location.isNotEmpty()) {
                        SectionIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            icon = Icons.Default.LocationOn,
                            contentDescription = "Location"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionIndicator(
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String?
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun JournalCardCompact(
    entry: JournalEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(primaryLight, accentOrange)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = LocalDate.parse(entry.date).dayOfMonth.toString(),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = LocalDate.parse(entry.date).let { 
                        "${it.month.name.lowercase().replaceFirstChar { c -> c.uppercase() }} ${it.year}" 
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
} 