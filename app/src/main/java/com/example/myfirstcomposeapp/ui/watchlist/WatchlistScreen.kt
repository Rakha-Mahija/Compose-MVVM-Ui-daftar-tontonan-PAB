package com.example.myfirstcomposeapp.ui.watchlist

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myfirstcomposeapp.model.Episode
import com.example.myfirstcomposeapp.model.Season
import com.example.myfirstcomposeapp.model.WatchItem
import com.example.myfirstcomposeapp.ui.theme.MyFirstComposeAppTheme

// Composable yang terhubung ke ViewModel (stateful)
@Composable
fun WatchlistScreen(viewModel: WatchlistViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    WatchlistContent(
        uiState = uiState,
        onTitleChange = viewModel::onTitleChange,
        onAdd = viewModel::addItem,
        onToggleWatched = viewModel::toggleWatched,
        onToggleEpisode = viewModel::toggleEpisode,
        onSelectSeason = viewModel::selectSeason,
        onSelect = viewModel::selectItem,
        onCloseDetail = viewModel::closeDetail,
        onStartEditSeasons = viewModel::startEditSeasons,
        onCancelEditSeasons = viewModel::cancelEditSeasons,
        onSeasonCountChange = viewModel::onSeasonCountChange,
        onEpisodeChange = viewModel::onEpisodeChange,
        onSaveSeasons = viewModel::saveSeasons
    )
}

// Composable tanpa state: hanya menampilkan state dan meneruskan event
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistContent(
    uiState: WatchlistUiState,
    onTitleChange: (String) -> Unit,
    onAdd: () -> Unit,
    onToggleWatched: (Int) -> Unit,
    onToggleEpisode: (Int, Int) -> Unit,
    onSelectSeason: (Int) -> Unit,
    onSelect: (Int) -> Unit,
    onCloseDetail: () -> Unit,
    onStartEditSeasons: () -> Unit,
    onCancelEditSeasons: () -> Unit,
    onSeasonCountChange: (String) -> Unit,
    onEpisodeChange: (Int, String) -> Unit,
    onSaveSeasons: () -> Unit
) {
    val selectedItem = uiState.selectedItem

    // Tombol Back HP menutup layar detail lebih dulu
    BackHandler(enabled = selectedItem != null, onBack = onCloseDetail)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Daftar Tontonan") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isWide = maxWidth >= 600.dp

            val mainList: @Composable (Modifier) -> Unit = { modifier ->
                MainList(uiState, onTitleChange, onAdd, onSelect, modifier)
            }
            val detail: @Composable (WatchItem, Boolean, Modifier) -> Unit = { item, showBack, modifier ->
                WatchDetailPane(
                    item = item,
                    uiState = uiState,
                    showBack = showBack,
                    onBack = onCloseDetail,
                    onToggleWatched = { onToggleWatched(item.id) },
                    onToggleEpisode = onToggleEpisode,
                    onSelectSeason = onSelectSeason,
                    onStartEdit = onStartEditSeasons,
                    onCancelEdit = onCancelEditSeasons,
                    onSeasonCountChange = onSeasonCountChange,
                    onEpisodeChange = onEpisodeChange,
                    onSave = onSaveSeasons,
                    modifier = modifier
                )
            }

            if (isWide) {
                // Layar lebar: daftar di kiri, detail di kanan
                Row(modifier = Modifier.fillMaxSize()) {
                    mainList(Modifier.weight(1f).fillMaxHeight())
                    if (selectedItem != null) {
                        detail(selectedItem, false, Modifier.weight(1.2f).fillMaxHeight())
                    } else {
                        NoSelection(Modifier.weight(1.2f).fillMaxHeight())
                    }
                }
            } else if (selectedItem != null) {
                // Layar sempit: detail tampil penuh menggantikan daftar
                detail(selectedItem, true, Modifier.fillMaxSize())
            } else {
                mainList(Modifier.fillMaxSize())
            }
        }
    }
}

// Halaman utama: form tambah, ringkasan, daftar judul
@Composable
private fun MainList(
    uiState: WatchlistUiState,
    onTitleChange: (String) -> Unit,
    onAdd: () -> Unit,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { AddItemForm(uiState, onTitleChange, onAdd) }
        item { SummaryText(uiState) }
        watchlistItems(uiState, onSelect)
    }
}

// Isi daftar: pesan kosong atau kartu-kartu judul
private fun LazyListScope.watchlistItems(
    uiState: WatchlistUiState,
    onSelect: (Int) -> Unit
) {
    if (uiState.isEmpty) {
        item { EmptyState() }
    } else {
        items(uiState.items, key = { it.id }) { item ->
            WatchItemCard(
                item = item,
                isSelected = item.id == uiState.selectedId,
                onOpen = { onSelect(item.id) }
            )
        }
    }
}

@Composable
private fun AddItemForm(
    uiState: WatchlistUiState,
    onTitleChange: (String) -> Unit,
    onAdd: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Tambah judul", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = uiState.titleInput,
            onValueChange = onTitleChange,
            label = { Text("Judul film / series") },
            singleLine = true,
            isError = uiState.titleError != null,
            supportingText = {
                Text(uiState.titleError ?: "Contoh: Stranger Things")
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onAdd() }),
            modifier = Modifier.fillMaxWidth()
        )

        Button(onClick = onAdd, modifier = Modifier.fillMaxWidth()) {
            Text("Tambah ke daftar")
        }
    }
}

@Composable
private fun SummaryText(uiState: WatchlistUiState) {
    Text(
        text = if (uiState.isEmpty) {
            "Belum ada judul."
        } else {
            "${uiState.items.size} judul · ketuk judul untuk melihat detail"
        },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Daftar tontonan masih kosong.\nTambahkan film atau series pertamamu.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Kartu di halaman utama: hanya judul
@Composable
private fun WatchItemCard(
    item: WatchItem,
    isSelected: Boolean,
    onOpen: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Text(
            text = item.title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun NoSelection(modifier: Modifier = Modifier) {
    Box(modifier = modifier.padding(16.dp), contentAlignment = Alignment.Center) {
        Text(
            text = "Pilih judul di daftar untuk mengisi data season dan episode.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private val previewItems = listOf(
    WatchItem(
        1, "Stranger Things",
        seasons = listOf(
            Season(1, List(3) { Episode(it + 1, isWatched = it == 0) }),
            Season(2, List(2) { Episode(it + 1) })
        )
    ),
    WatchItem(2, "Interstellar")
)

@Composable
private fun PreviewContent(uiState: WatchlistUiState) {
    MyFirstComposeAppTheme {
        WatchlistContent(
            uiState = uiState,
            onTitleChange = {},
            onAdd = {},
            onToggleWatched = {},
            onToggleEpisode = { _, _ -> },
            onSelectSeason = {},
            onSelect = {},
            onCloseDetail = {},
            onStartEditSeasons = {},
            onCancelEditSeasons = {},
            onSeasonCountChange = {},
            onEpisodeChange = { _, _ -> },
            onSaveSeasons = {}
        )
    }
}

@Preview(name = "Sempit - kosong", widthDp = 360, heightDp = 720, showBackground = true)
@Composable
private fun EmptyPreview() {
    PreviewContent(WatchlistUiState())
}

@Preview(name = "Sempit - validasi", widthDp = 360, heightDp = 720, showBackground = true)
@Composable
private fun ErrorPreview() {
    PreviewContent(WatchlistUiState(titleError = "Judul tidak boleh kosong.", items = previewItems))
}

@Preview(name = "Lebar - daftar + detail", widthDp = 900, heightDp = 600, showBackground = true)
@Composable
private fun WidePreview() {
    PreviewContent(
        WatchlistUiState(items = previewItems, selectedId = 1)
    )
}
