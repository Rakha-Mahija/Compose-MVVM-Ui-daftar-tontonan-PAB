package com.example.myfirstcomposeapp.ui.watchlist

import com.example.myfirstcomposeapp.model.WatchItem

const val MAX_SEASONS = 30
const val MAX_EPISODES = 500

// Seluruh state layar daftar tontonan dalam satu objek
data class WatchlistUiState(
    // Form tambah judul (halaman depan)
    val titleInput: String = "",
    val titleError: String? = null,
    val items: List<WatchItem> = emptyList(),

    // Layar detail: judul yang sedang dibuka
    val selectedId: Int? = null,
    // Season yang episodenya sedang ditampilkan
    val selectedSeason: Int = 1,

    // Form edit season: hanya tampil jika isEditingSeasons = true
    val isEditingSeasons: Boolean = false,
    val seasonCountInput: String = "",
    val seasonCountError: String? = null,
    val episodeInputs: List<String> = emptyList(),
    val episodeErrors: List<String?> = emptyList(),
    val saveMessage: String? = null
) {
    val isEmpty: Boolean get() = items.isEmpty()
    val watchedCount: Int get() = items.count { it.isWatched }
    val selectedItem: WatchItem? get() = items.firstOrNull { it.id == selectedId }

    // Jumlah season yang valid (1..MAX_SEASONS), null jika belum valid
    val seasonCount: Int? get() = seasonCountInput.toIntOrNull()?.takeIf { it in 1..MAX_SEASONS }

    // Total episode dari isian yang sedang diketik
    val draftTotalEpisodes: Int
        get() = episodeInputs.take(seasonCount ?: 0).sumOf { it.toIntOrNull() ?: 0 }
}
