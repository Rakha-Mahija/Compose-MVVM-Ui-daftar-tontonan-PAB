package com.example.myfirstcomposeapp.ui.watchlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myfirstcomposeapp.model.Episode
import com.example.myfirstcomposeapp.model.Season
import com.example.myfirstcomposeapp.model.WatchItem
import com.example.myfirstcomposeapp.ui.theme.MyFirstComposeAppTheme

// Layar detail satu judul
@Composable
fun WatchDetailPane(
    item: WatchItem,
    uiState: WatchlistUiState,
    showBack: Boolean,
    onBack: () -> Unit,
    onToggleWatched: () -> Unit,
    onToggleEpisode: (Int, Int) -> Unit,
    onSelectSeason: (Int) -> Unit,
    onStartEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onSeasonCountChange: (String) -> Unit,
    onEpisodeChange: (Int, String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (showBack) {
            item { TextButton(onClick = onBack) { Text("← Kembali ke daftar") } }
        }

        item { ImagePlaceholder() }

        item {
            Text(
                text = item.title,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .semantics { heading() }
            )
        }

        item {
            CheckRow(
                checked = item.isWatched,
                label = "Sudah ditonton",
                onToggle = onToggleWatched
            )
        }

        item { HorizontalDivider() }

        // Satu field ringkasan; form edit baru muncul setelah field diklik
        item {
            if (uiState.isEditingSeasons) {
                SeasonEditForm(uiState, onSeasonCountChange, onEpisodeChange, onSave, onCancelEdit)
            } else {
                SeasonSummaryField(item, onClick = onStartEdit)
            }
        }

        uiState.saveMessage?.let { message ->
            item { Text(message, color = MaterialTheme.colorScheme.primary) }
        }

        // Pilihan season + episode season terpilih, disembunyikan saat form edit terbuka
        if (!uiState.isEditingSeasons) {
            val season = item.seasons.firstOrNull { it.number == uiState.selectedSeason }
                ?: item.seasons.firstOrNull()

            if (season == null) {
                item {
                    Text(
                        text = "Belum ada episode. Ketuk kolom season di atas untuk mengisi.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                item {
                    SeasonPicker(
                        seasons = item.seasons,
                        selected = season.number,
                        onSelect = onSelectSeason
                    )
                }
                item(key = "season-${season.number}") { SeasonHeader(season) }
                items(season.episodes, key = { "s${season.number}-e${it.number}" }) { episode ->
                    CheckRow(
                        checked = episode.isWatched,
                        label = "Season ${season.number} · Episode ${episode.number}",
                        onToggle = { onToggleEpisode(season.number, episode.number) }
                    )
                }
            }
        }
    }
}

// Area kosong di bagian atas, nanti bisa diganti Image
@Composable
private fun ImagePlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .semantics { contentDescription = "Area gambar" }
    )
}

// Baris checkbox; seluruh baris bisa diketuk
@Composable
private fun CheckRow(checked: Boolean, label: String, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = { onToggle() }),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = null, modifier = Modifier.padding(12.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun SeasonSummaryField(item: WatchItem, onClick: () -> Unit) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Season & episode",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (item.seasons.isEmpty()) {
                        "Belum diisi"
                    } else {
                        "${item.seasons.size} season · ${item.totalEpisodes} episode"
                    },
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Text("Edit", color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun SeasonEditForm(
    uiState: WatchlistUiState,
    onSeasonCountChange: (String) -> Unit,
    onEpisodeChange: (Int, String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Edit season & episode", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = uiState.seasonCountInput,
            onValueChange = onSeasonCountChange,
            label = { Text("Jumlah season") },
            singleLine = true,
            isError = uiState.seasonCountError != null,
            supportingText = {
                Text(uiState.seasonCountError ?: "Isi angka 1–$MAX_SEASONS")
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        val count = uiState.seasonCount
        if (count != null) {
            for (index in 0 until count) {
                val error = uiState.episodeErrors.getOrNull(index)
                OutlinedTextField(
                    value = uiState.episodeInputs.getOrElse(index) { "" },
                    onValueChange = { onEpisodeChange(index, it) },
                    label = { Text("Season ${index + 1} – jumlah episode") },
                    singleLine = true,
                    isError = error != null,
                    supportingText = { if (error != null) Text(error) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Text(
                text = "Total: ${uiState.draftTotalEpisodes} episode di $count season",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                Text("Batal")
            }
            Button(onClick = onSave, modifier = Modifier.weight(1f)) {
                Text("Simpan")
            }
        }
    }
}

// Deretan chip season yang bisa digeser ke samping
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SeasonPicker(
    seasons: List<Season>,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            "Pilih season",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(seasons, key = { it.number }) { season ->
                FilterChip(
                    selected = season.number == selected,
                    onClick = { onSelect(season.number) },
                    label = { Text("Season ${season.number}") }
                )
            }
        }
    }
}

@Composable
private fun SeasonHeader(season: Season) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Season ${season.number}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() }
        )
        Text(
            text = "${season.watchedCount}/${season.episodes.size} ditonton",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private val previewItem = WatchItem(
    id = 1,
    title = "Stranger Things",
    isWatched = false,
    seasons = listOf(
        Season(1, List(3) { Episode(it + 1, isWatched = it < 2) }),
        Season(2, List(2) { Episode(it + 1) })
    )
)

@Composable
private fun PreviewPane(uiState: WatchlistUiState) {
    MyFirstComposeAppTheme {
        WatchDetailPane(
            item = previewItem,
            uiState = uiState,
            showBack = true,
            onBack = {},
            onToggleWatched = {},
            onToggleEpisode = { _, _ -> },
            onSelectSeason = {},
            onStartEdit = {},
            onCancelEdit = {},
            onSeasonCountChange = {},
            onEpisodeChange = { _, _ -> },
            onSave = {}
        )
    }
}

@Preview(name = "Detail - daftar episode", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun DetailListPreview() {
    PreviewPane(WatchlistUiState(items = listOf(previewItem), selectedId = 1))
}

@Preview(name = "Detail - form edit", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun DetailEditPreview() {
    PreviewPane(
        WatchlistUiState(
            items = listOf(previewItem),
            selectedId = 1,
            isEditingSeasons = true,
            seasonCountInput = "3",
            episodeInputs = listOf("3", "2", ""),
            episodeErrors = listOf(null, null, "Isi jumlah episode.")
        )
    )
}
