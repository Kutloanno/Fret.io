package com.example.guitarkaizen.ui.screens.resources

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.guitarkaizen.ui.screens.practice.RetroBox

data class ResourceData(
  val title: String,
  val category: String,
  val description: String,
  val icon: androidx.compose.ui.graphics.vector.ImageVector,
  val tintColor: androidx.compose.ui.graphics.Color
)

@Composable
fun ResourcesScreen(modifier: Modifier = Modifier) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf("ALL") }
  val categories = listOf("ALL", "TABS", "LESSONS", "PDFS")

  val allResources = remember {
    listOf(
      ResourceData(
        title = "Ultimate Guitar Scraper Directory",
        category = "TABS",
        description = "Access top-rated guitar tab transcriptions and chord charts directly.",
        icon = Icons.Default.List,
        tintColor = Color(0xFFCCCCCC)
      ),
      ResourceData(
        title = "Circle of Fifths & Music Theory",
        category = "LESSONS",
        description = "Master song key changes, chord substitutions, and fretboard harmony.",
        icon = Icons.Default.Info,
        tintColor = Color(0xFFE5E5E5)
      ),
      ResourceData(
        title = "Blues Jam Session Playalongs",
        category = "LESSONS",
        description = "Interactive audio files and pentatonic scale reference guides.",
        icon = Icons.Default.PlayArrow,
        tintColor = Color(0xFFFAF6F0)
      ),
      ResourceData(
        title = "100 Jazz Standards Chord Book",
        category = "PDFS",
        description = "Download a comprehensive sheet music compilation of classic jazz leadsheets.",
        icon = Icons.Default.List,
        tintColor = Color(0xFFCCCCCC)
      )
    )
  }

  val filteredResources = allResources.filter { item ->
    val matchesCategory = selectedCategory == "ALL" || item.category.equals(selectedCategory, ignoreCase = true)
    val matchesSearch = searchQuery.isEmpty() || 
        item.title.contains(searchQuery, ignoreCase = true) || 
        item.description.contains(searchQuery, ignoreCase = true)
    matchesCategory && matchesSearch
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState())
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(24.dp)
  ) {
    // 1. Retro Header Box (Solid Black)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .border(2.dp, MaterialTheme.colorScheme.outline)
        .background(MaterialTheme.colorScheme.outline)
        .padding(16.dp)
    ) {
      Column {
        Text(
          text = "RESOURCE HUB",
          color = MaterialTheme.colorScheme.background, // sand text
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "CURATED CHORDS, TABS, AND TUTORIALS.",
          color = MaterialTheme.colorScheme.background.copy(alpha = 0.85f),
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold
        )
      }
    }

    // 2. Retro Search Box (Solid thick-bordered Inset)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .border(2.dp, MaterialTheme.colorScheme.outline)
        .background(MaterialTheme.colorScheme.surface)
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Default.Search,
        contentDescription = "Search",
        tint = MaterialTheme.colorScheme.outline,
        modifier = Modifier.size(20.dp)
      )
      Spacer(modifier = Modifier.width(12.dp))
      
      // BasicTextField for complete control over flat input appearance
      BasicTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        modifier = Modifier.fillMaxWidth(),
        textStyle = MaterialTheme.typography.bodyMedium.copy(
          color = MaterialTheme.colorScheme.onSurface,
          fontSize = 13.sp
        ),
        singleLine = true,
        decorationBox = { innerTextField ->
          if (searchQuery.isEmpty()) {
            Text(
              text = "Search song, catalog, or scale...",
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
              fontSize = 13.sp,
              fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
          }
          innerTextField()
        }
      )
    }

    // 3. Retro Flat Toggle Chips
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      categories.forEach { category ->
        val isSelected = selectedCategory == category
        val chipBg = if (isSelected) Color(0xFFE5E5E5) else MaterialTheme.colorScheme.surface
        
        Box(
          modifier = Modifier
            .border(1.5.dp, MaterialTheme.colorScheme.outline)
            .background(chipBg)
            .clickable { selectedCategory = category }
            .padding(horizontal = 14.dp, vertical = 6.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = category,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
          )
        }
      }
    }

    Text(
      text = "FEATURED DIRECTORY",
      fontSize = 15.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface
    )

    if (filteredResources.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.5.dp, MaterialTheme.colorScheme.outline)
          .background(MaterialTheme.colorScheme.surfaceVariant)
          .padding(24.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "NO MATCHING RESOURCES FOUND",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    } else {
      filteredResources.forEach { item ->
        ResourceItem(
          title = item.title,
          category = item.category,
          description = item.description,
          icon = item.icon,
          tintColor = item.tintColor
        )
      }
    }
  }
}

@Composable
fun ResourceItem(
  title: String,
  category: String,
  description: String,
  icon: ImageVector,
  tintColor: Color
) {
  RetroBox(shadowOffset = 3.dp) {
    Column(modifier = Modifier.fillMaxWidth()) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Categorical Tag
        Box(
          modifier = Modifier
            .border(1.5.dp, MaterialTheme.colorScheme.outline)
            .background(tintColor)
            .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
          Text(
            text = category,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
          )
        }
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
          modifier = Modifier.size(18.dp)
        )
      }
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = title.uppercase(),
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = description,
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        lineHeight = 16.sp
      )
    }
  }
}
