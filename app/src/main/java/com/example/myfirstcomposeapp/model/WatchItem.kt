package com.example.myfirstcomposeapp.model

// Satu episode beserta status ditonton
data class Episode(
    val number: Int,
    val isWatched: Boolean = false
)

// Satu season berisi daftar episodenya
data class Season(
    val number: Int,
    val episodes: List<Episode>
) {
    val watchedCount: Int get() = episodes.count { it.isWatched }
}

// Model data satu judul di daftar tontonan
data class WatchItem(
    val id: Int,
    val title: String,
    val isWatched: Boolean = false,
    val seasons: List<Season> = emptyList()
) {
    val totalEpisodes: Int get() = seasons.sumOf { it.episodes.size }
}
