package com.metalshard.projectwave

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.decode.SvgDecoder


// I literally had to use a D-pad system for moving stations because nothing else supported Android 5. I don't know what I'm doing
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RadioCard(
    station: RadioStation,
    isEditingMode: Boolean,
    showLeftArrow: Boolean,
    showRightArrow: Boolean,
    showUpArrow: Boolean,
    showDownArrow: Boolean,
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onLongClick: () -> Unit,
    onCloseEditing: () -> Unit,
    onEditClick: () -> Unit,
    onClick: () -> Unit
) {
    val placeholder = rememberVectorPainter(Icons.Default.Radio)

    OutlinedCard(
        modifier = Modifier
            .padding(6.dp)
            .aspectRatio(1f)
            .combinedClickable(
                onClick = { if (isEditingMode) onCloseEditing() else onClick() },
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isEditingMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            )
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AsyncImage(
                    model = coil.request.ImageRequest.Builder(LocalContext.current)
                        .data(station.imageUrl)
                        .addHeader("User-Agent", "ProjectWave/1.3 (themetalshard@softmodd.ing) feat. NexGenDriven")
                        .decoderFactory { result, options, imageLoader ->
                            if (station.imageUrl.endsWith(".svg", ignoreCase = true)) {
                                SvgDecoder.Factory().create(result, options, imageLoader)
                            } else {
                                null
                            }
                        }
                        .build(),
                    contentDescription = station.name,
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                    contentScale = ContentScale.Fit,
                    placeholder = placeholder,
                    error = placeholder
                )
                Text(
                    text = station.name,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(bottom = 6.dp, start = 4.dp, end = 4.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (isEditingMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                       IconButton(
                            onClick = {
                                onEditClick()
                            },
                            modifier = Modifier.align(Alignment.TopStart).padding(2.dp).size(24.dp),
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Station", modifier = Modifier.size(14.dp))
                        }

                        IconButton(
                            onClick = onCloseEditing,
                            modifier = Modifier.align(Alignment.TopEnd).padding(2.dp).size(22.dp)
                        ) {
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        if (showUpArrow) {
                            FilledIconButton(
                                onClick = onMoveUp,
                                modifier = Modifier.align(Alignment.TopCenter).padding(top = 4.dp).size(28.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.ArrowUpward, null, modifier = Modifier.size(16.dp))
                            }
                        }

                        if (showDownArrow) {
                            FilledIconButton(
                                onClick = onMoveDown,
                                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp).size(28.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.ArrowDownward, null, modifier = Modifier.size(16.dp))
                            }
                        }

                        if (showLeftArrow) {
                            FilledIconButton(
                                onClick = onMoveLeft,
                                modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp).size(28.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, modifier = Modifier.size(16.dp))
                            }
                        }

                        if (showRightArrow) {
                            FilledIconButton(
                                onClick = onMoveRight,
                                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp).size(28.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RadioGrid(
    modifier: Modifier,
    stations: List<RadioStation>,
    onStationSelected: (RadioStation) -> Unit,
    onStationEditRequested: (RadioStation) -> Unit,
    onStationsReordered: (List<RadioStation>) -> Unit
) {
    val gridState = rememberLazyGridState()
    var activeEditingStationId by remember { mutableStateOf<Int?>(null) }

    val columnsCount by remember {
        derivedStateOf {
            val visibleItems = gridState.layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) return@derivedStateOf 1

            val firstRowY = visibleItems.first().offset.y
            visibleItems.count { it.offset.y == firstRowY }.coerceAtLeast(1)
        }
    }

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Adaptive(minSize = 100.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp)
    ) {
        itemsIndexed(stations, key = { _, station -> station.id }) { index, station ->
            val isCurrentCardEditing = activeEditingStationId == station.id

            RadioCard(
                station = station,
                isEditingMode = isCurrentCardEditing,
                showLeftArrow = index > 0,
                showRightArrow = index < stations.lastIndex,
                showUpArrow = index >= columnsCount,
                showDownArrow = index + columnsCount <= stations.lastIndex,
                onMoveLeft = {
                    val list = stations.toMutableList()
                    val item = list.removeAt(index)
                    list.add(index - 1, item)
                    onStationsReordered(list)
                    activeEditingStationId = station.id
                },
                onMoveRight = {
                    val list = stations.toMutableList()
                    val item = list.removeAt(index)
                    list.add(index + 1, item)
                    onStationsReordered(list)
                    activeEditingStationId = station.id
                },
                onMoveUp = {
                    val list = stations.toMutableList()
                    val targetIndex = index - columnsCount
                    if (targetIndex in list.indices) {
                        val item = list.removeAt(index)
                        list.add(targetIndex, item)
                        onStationsReordered(list)
                        activeEditingStationId = station.id
                    }
                },
                onMoveDown = {
                    val list = stations.toMutableList()
                    val targetIndex = index + columnsCount
                    if (targetIndex in list.indices) {
                        val item = list.removeAt(index)
                        list.add(targetIndex, item)
                        onStationsReordered(list)
                        activeEditingStationId = station.id
                    }
                },
                onLongClick = { activeEditingStationId = station.id },
                onCloseEditing = { activeEditingStationId = null },
                onEditClick = { onStationEditRequested(station) },
                onClick = { onStationSelected(station) }
            )
        }
    }
}