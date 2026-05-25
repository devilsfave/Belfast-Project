package com.example.medgem

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.medgem.data.KnowledgeEntity
import com.example.medgem.ui.components.MedGemTopBar
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material3.RichText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import org.json.JSONObject

// Data class to hold parsed metadata for UI display
data class ParsedMetadata(val badgeText: String?, val pdfFile: String?, val page: Int)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KnowledgeSearchScreen(
    onPdfClick: (String, Int, String?) -> Unit = { _, _, _ -> },
    onBack: () -> Unit
) {
    val viewModel: com.example.medgem.ui.viewmodel.KnowledgeSearchViewModel = hiltViewModel()
    val searchState by viewModel.searchState.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val isLoadingModel by viewModel.isLoadingModel.collectAsState()

    var pendingSearchQuery by remember { mutableStateOf<String?>(null) }

    // Content overlay state - hoisted here to avoid nesting LazyColumn
    var overlayContent by remember { mutableStateOf<Pair<String, String>?>(null) }

    rememberCoroutineScope()

    // Clear stale "Searching..." status if we are not actually searching
    LaunchedEffect(Unit) {
        if (searchState.status == "Searching medical database..." && !isSearching) {
            // No-op or fix status if needed, but VM handles it.
        }
    }

    // Suggested queries for zero state
    val suggestedQueries =
        listOf(
            "CPR",
            "Choking",
            "Bandaging",
            "Bleeding",
            "Burns",
            "Fracture",
            "Shock",
            "Snake Bite",
            "Drowning"
        )

    // Handle suggestion chip clicks - trigger search when pendingSearchQuery is set
    LaunchedEffect(Unit) {
        snapshotFlow { pendingSearchQuery }.filterNotNull().collectLatest { query ->
            if (query.isBlank() || isSearching || isLoadingModel) return@collectLatest
            pendingSearchQuery = null
            viewModel.runSearch(query)
        }
    }

    // Simplified structure: Removed nested Scaffold
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MedGemTopBar(
            title = "Knowledge Base",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = searchState.query,
                onValueChange = { viewModel.updateQuery(it) },
                label = { Text("Search medical database...") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (!isSearching && searchState.query.isNotBlank()) {
                        IconButton(
                            onClick = {
                                pendingSearchQuery = null
                                viewModel.clearSearch()
                            }
                        ) { Icon(Icons.Default.Close, contentDescription = "Clear search") }
                    }
                },
                singleLine = true,
                enabled = !isSearching
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Action Button (Search)
            Button(
                onClick = {
                    if (searchState.query.isNotBlank()) {
                        viewModel.runSearch(searchState.query)
                    }
                },
                enabled = !isSearching && !isLoadingModel && searchState.query.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoadingModel || isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Searching...")
                } else {
                    Text("Search")
                }
            }


            Spacer(modifier = Modifier.height(16.dp))

            // Status Text (Error or Empty)
            if (searchState.status.isNotEmpty() && !searchState.status.startsWith("Found")) {
                Text(
                    text = searchState.status,
                    style = MaterialTheme.typography.bodyMedium,
                    color =
                        if (searchState.status.startsWith("Error"))
                            MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Content Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {

                // Zero State: Suggested Queries
                if (!isSearching &&
                    !isLoadingModel &&
                    searchState.results.isEmpty() &&
                    searchState.query.isEmpty()
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Suggested Topics",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            suggestedQueries.forEach { query ->
                                Surface(
                                    onClick = { pendingSearchQuery = query },
                                    shape = RoundedCornerShape(16.dp),
                                    color =
                                        MaterialTheme.colorScheme.secondaryContainer.copy(
                                            alpha = 0.5f
                                        ),
                                    border = null
                                ) {
                                    Row(
                                        modifier =
                                            Modifier.padding(
                                                horizontal = 12.dp,
                                                vertical = 8.dp
                                            ),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = query,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Loading State: Skeleton Loaders
                if (isSearching || isLoadingModel) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        repeat(3) { SkeletonResultItem() }
                    }
                }

                // Results List
                if (searchState.results.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(searchState.results) { (entity, score) ->
                            ResultItem(
                                entity = entity,
                                score = score,
                                searchQuery = searchState.query,
                                onPdfClick = onPdfClick,
                                onViewContent = { title, content ->
                                    overlayContent = Pair(title, content)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Content Overlay - rendered outside the LazyColumn
    overlayContent?.let { (title, content) ->
        ContentOverlay(title = title, content = content, onDismiss = { overlayContent = null })
    }
}

@Composable
fun ResultItem(
    entity: KnowledgeEntity,
    score: Double,
    searchQuery: String,
    onPdfClick: (String, Int, String?) -> Unit,
    onViewContent: (title: String, content: String) -> Unit
) {
    // Parse metadata
    val metadata =
        remember(entity.metadata) {
            try {
                val json = JSONObject(entity.metadata)
                val chapter = json.optString("chapter", "")
                // Try to make chapter more readable if it's a filename
                val readableChapter =
                    if (chapter.contains("/")) {
                        chapter.substringAfterLast("/")
                            .removeSuffix(".pdf")
                            .replace("_", " ")
                            .uppercase()
                    } else if (chapter.isNotEmpty()) {
                        chapter.replace("_", " ").uppercase()
                    } else {
                        "Unknown Source"
                    }

                val startPage = json.optInt("start_page", 0)

                val pdfPath =
                    if (chapter.endsWith(".pdf")) {
                        chapter
                    } else if (chapter.contains("/")) {
                        "$chapter.pdf"
                    } else if (chapter.isNotEmpty()) {
                        "en_wtnd_2025/$chapter.pdf" // Default fallback path
                    } else {
                        null
                    }

                val pdfPage = if (startPage > 0) startPage - 1 else 0

                ParsedMetadata(readableChapter, pdfPath, pdfPage)
            } catch (e: Exception) {
                ParsedMetadata(null, null, 0)
            }
        }

    // Title Case Logic for Title
    val displayTitle =
        remember(entity.title) {
            entity.title.lowercase().split(" ").joinToString(" ") {
                it.replaceFirstChar { char -> char.uppercase() }
            }
        }

    val displaySnippet =
        if (entity.content.length > 200) entity.content.substring(0, 200) + "..."
        else entity.content
    // Score is now similarity (0..1), so percentage is just score * 100
    val matchPercentage = (score * 100).coerceIn(0.0, 100.0).toInt()

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable {
                    onViewContent(displayTitle, entity.content)
                },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Title and Match Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = displayTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    color =
                        if (score > 0.7) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "$matchPercentage% Match",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Snippet
            Text(
                text = displaySnippet,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            // Footer: Source and Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Source Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = metadata.badgeText ?: "Unknown Source",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                // PDF Action (Subtle)
                if (metadata.pdfFile != null) {
                    TextButton(
                        onClick = { onPdfClick(metadata.pdfFile, metadata.page, searchQuery) },
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Open PDF", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun ContentOverlay(title: String, content: String, onDismiss: () -> Unit) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(onClick = onDismiss)
    ) {
        Card(
            modifier =
                Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxSize(0.9f)
                    .align(Alignment.Center)
                    .clickable(enabled = false) {}, // Prevent click-through
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors =
                CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header with title and close button
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Scrollable content
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    item { RichText { Markdown(content = content) } }
                }
            }
        }
    }
}

@Composable
fun SkeletonResultItem() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Badge skeleton
            Box(
                modifier =
                    Modifier
                        .size(width = 80.dp, height = 20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Title skeleton
            Box(
                modifier =
                    Modifier
                        .size(width = 200.dp, height = 24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Content skeleton (3 lines)
            repeat(3) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth(fraction = if (it == 2) 0.6f else 1f)
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                MaterialTheme.colorScheme.onSurface.copy(
                                    alpha = 0.05f
                                )
                            )
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}
