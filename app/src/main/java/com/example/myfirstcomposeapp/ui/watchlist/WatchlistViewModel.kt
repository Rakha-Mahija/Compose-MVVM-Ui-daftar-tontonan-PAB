package com.example.myfirstcomposeapp.ui.watchlist

import androidx.lifecycle.ViewModel
import com.example.myfirstcomposeapp.model.Episode
import com.example.myfirstcomposeapp.model.Season
import com.example.myfirstcomposeapp.model.WatchItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class WatchlistViewModel : ViewModel() {

    // Hanya ViewModel yang boleh mengubah state
    private val _uiState = MutableStateFlow(WatchlistUiState())

    // UI hanya bisa membaca
    val uiState: StateFlow<WatchlistUiState> = _uiState.asStateFlow()

    private var nextId = 1

    // ---------- Halaman utama ----------

    fun onTitleChange(newTitle: String) {
        // Pesan error hilang begitu pengguna mulai mengetik lagi
        _uiState.update { it.copy(titleInput = newTitle, titleError = null) }
    }

    // Fitur 1: tambah judul dengan validasi
    fun addItem() {
        val state = _uiState.value
        val title = state.titleInput.trim()

        val error = when {
            title.isEmpty() -> "Judul tidak boleh kosong."
            title.length < 2 -> "Judul minimal 2 karakter."
            state.items.any { it.title.equals(title, ignoreCase = true) } ->
                "\"$title\" sudah ada di daftar."
            else -> null
        }

        if (error != null) {
            _uiState.update { it.copy(titleError = error) }
            return
        }

        val newItem = WatchItem(id = nextId++, title = title)
        _uiState.update {
            it.copy(items = it.items + newItem, titleInput = "", titleError = null)
        }
    }

    // ---------- Layar detail ----------

    fun selectItem(id: Int) {
        if (_uiState.value.items.none { it.id == id }) return
        _uiState.update { it.clearSeasonForm().copy(selectedId = id, selectedSeason = 1) }
    }

    // Pilih season yang episodenya ingin ditampilkan
    fun selectSeason(number: Int) {
        val seasons = _uiState.value.selectedItem?.seasons ?: return
        if (seasons.none { it.number == number }) return
        _uiState.update { it.copy(selectedSeason = number) }
    }

    fun closeDetail() {
        _uiState.update { it.clearSeasonForm().copy(selectedId = null) }
    }

    // Fitur 2: tandai judul sudah / belum ditonton
    fun toggleWatched(id: Int) {
        updateItem(id) { it.copy(isWatched = !it.isWatched) }
    }

    // Fitur 3a: centang episode yang sudah ditonton
    fun toggleEpisode(seasonNumber: Int, episodeNumber: Int) {
        val id = _uiState.value.selectedId ?: return
        updateItem(id) { item ->
            item.copy(seasons = item.seasons.map { season ->
                if (season.number != seasonNumber) {
                    season
                } else {
                    season.copy(episodes = season.episodes.map { episode ->
                        if (episode.number == episodeNumber) {
                            episode.copy(isWatched = !episode.isWatched)
                        } else {
                            episode
                        }
                    })
                }
            })
        }
    }

    // ---------- Form edit season (fitur 3b) ----------

    // Field ringkasan diklik: buka form dan isi dengan data yang tersimpan
    fun startEditSeasons() {
        val item = _uiState.value.selectedItem ?: return
        _uiState.update {
            it.copy(
                isEditingSeasons = true,
                seasonCountInput = if (item.seasons.isEmpty()) "" else item.seasons.size.toString(),
                seasonCountError = null,
                episodeInputs = item.seasons.map { season -> season.episodes.size.toString() },
                episodeErrors = item.seasons.map { null },
                saveMessage = null
            )
        }
    }

    fun cancelEditSeasons() {
        _uiState.update { it.clearSeasonForm() }
    }

    fun onSeasonCountChange(input: String) {
        val digits = input.filter { it.isDigit() }.take(2)
        val count = digits.toIntOrNull()
        _uiState.update { state ->
            when {
                digits.isEmpty() ->
                    state.copy(seasonCountInput = "", seasonCountError = null)

                count == null || count !in 1..MAX_SEASONS ->
                    state.copy(
                        seasonCountInput = digits,
                        seasonCountError = "Jumlah season harus 1–$MAX_SEASONS."
                    )

                // Jumlah kolom episode mengikuti jumlah season; isian lama dipertahankan
                else -> state.copy(
                    seasonCountInput = digits,
                    seasonCountError = null,
                    episodeInputs = List(count) { i -> state.episodeInputs.getOrElse(i) { "" } },
                    episodeErrors = List(count) { null }
                )
            }
        }
    }

    fun onEpisodeChange(index: Int, input: String) {
        val digits = input.filter { it.isDigit() }.take(3)
        _uiState.update { state ->
            if (index !in state.episodeInputs.indices) return@update state
            state.copy(
                episodeInputs = state.episodeInputs.toMutableList().also { it[index] = digits },
                episodeErrors = state.episodeErrors.toMutableList().also {
                    if (index in it.indices) it[index] = null
                }
            )
        }
    }

    fun saveSeasons() {
        val state = _uiState.value
        val item = state.selectedItem ?: return
        val count = state.seasonCount

        if (count == null) {
            _uiState.update {
                it.copy(
                    seasonCountError = if (it.seasonCountInput.isBlank()) {
                        "Isi jumlah season terlebih dahulu."
                    } else {
                        "Jumlah season harus 1–$MAX_SEASONS."
                    }
                )
            }
            return
        }

        val inputs = state.episodeInputs.take(count)
        val errors = inputs.map { text ->
            val episodes = text.toIntOrNull()
            when {
                text.isBlank() -> "Isi jumlah episode."
                episodes == null || episodes !in 1..MAX_EPISODES ->
                    "Jumlah episode harus 1–$MAX_EPISODES."
                else -> null
            }
        }

        if (errors.any { it != null }) {
            _uiState.update { it.copy(episodeErrors = errors) }
            return
        }

        // Bangun ulang daftar episode; centang episode lama tetap dipertahankan
        val seasons = inputs.mapIndexed { i, text ->
            val oldSeason = item.seasons.getOrNull(i)
            Season(
                number = i + 1,
                episodes = List(text.toInt()) { e ->
                    Episode(
                        number = e + 1,
                        isWatched = oldSeason?.episodes?.getOrNull(e)?.isWatched ?: false
                    )
                }
            )
        }

        updateItem(item.id) { it.copy(seasons = seasons) }
        // Jika jumlah season berkurang, season terpilih disesuaikan
        _uiState.update {
            it.clearSeasonForm().copy(
                selectedSeason = it.selectedSeason.coerceIn(1, count),
                saveMessage = "Data season tersimpan."
            )
        }
    }

    // ---------- Helper ----------

    private fun updateItem(id: Int, change: (WatchItem) -> WatchItem) {
        _uiState.update { state ->
            state.copy(items = state.items.map { if (it.id == id) change(it) else it })
        }
    }

    private fun WatchlistUiState.clearSeasonForm() = copy(
        isEditingSeasons = false,
        seasonCountInput = "",
        seasonCountError = null,
        episodeInputs = emptyList(),
        episodeErrors = emptyList(),
        saveMessage = null
    )
}
