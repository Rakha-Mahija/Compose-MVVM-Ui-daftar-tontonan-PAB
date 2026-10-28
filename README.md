[README.md](https://github.com/user-attachments/files/34086465/README.md)
# Daftar Tontonan

Aplikasi Android satu layar utama untuk mencatat film dan series yang ingin ditonton, beserta season dan episode yang sudah ditonton.
Dibuat dengan Kotlin, Jetpack Compose, dan arsitektur MVVM.

## Nama Anggota
- Mahija Ramadhan Prabaswarakha (24523103)
- Harunsyah (24523103)

## Masalah

Rekomendasi film dan series dari teman sering tercecer di chat dan catatan. Untuk series yang panjang, kita juga sering lupa sudah sampai season dan episode berapa.

## Pengguna sasaran

siapa saja yang sering menonton film atau series dan ingin satu tempat sederhana untuk mencatatnya.

## Solusi

Satu daftar tontonan yang menyimpan judul, status sudah atau belum ditonton, serta daftar episode per season dengan checkbox. Data disimpan di memori, tanpa login, API, atau database.

## Fitur

1. **Tambah judul** dengan validasi: judul tidak boleh kosong, minimal 2 karakter, dan tidak boleh sama dengan judul yang sudah ada.
2. **Tandai sudah ditonton** untuk satu judul, di halaman detail.
3. **Kelola season dan episode**:
   - Satu field "Season & episode" yang membuka form edit saat diklik.
   - Isi jumlah season (1–30) dan jumlah episode per season (1–500), dengan validasi.
   - Pilih season lewat chip, lalu centang episode yang sudah ditonton.

Tampilan menyesuaikan ukuran layar:
- **Layar sempit (< 600dp):** daftar dan detail tampil bergantian.
- **Layar lebar (≥ 600dp):** daftar di kiri, detail di kanan.

## Struktur kode (MVVM)

```
app/src/main/java/com/example/myfirstcomposeapp/
├── MainActivity.kt                  # Pintu masuk, memanggil WatchlistScreen()
├── model/
│   └── WatchItem.kt                 # Model: WatchItem, Season, Episode
└── ui/watchlist/
    ├── WatchlistUiState.kt          # data class UiState
    ├── WatchlistViewModel.kt        # Logika fitur + MutableStateFlow privat / StateFlow publik
    ├── WatchlistScreen.kt           # Halaman utama + pengatur layout sempit/lebar
    └── WatchDetailPane.kt           # Halaman detail
```

## Cara menjalankan

1. Buka folder project ini di **Android Studio**.
2. Tunggu atau klik **Sync Now** agar Gradle mengunduh dependency (termasuk `lifecycle-runtime-compose` dan `lifecycle-viewmodel-compose`).
3. Pilih emulator atau HP Android (minSdk 24), lalu klik **Run ▶**.
4. Untuk mencoba layout lebar, miringkan emulator ke landscape atau gunakan emulator tablet.

## Contoh alur UDF: menekan "Tambah ke daftar"

1. **Event UI:** tombol di `AddItemForm` memanggil callback `onAdd`.
2. **ViewModel:** `onAdd` terhubung ke `viewModel.addItem()`. Fungsi ini memvalidasi judul lalu membuat `WatchItem` baru.
3. **UiState berubah:**
   ```kotlin
   _uiState.update {
       it.copy(items = it.items + newItem, titleInput = "", titleError = null)
   }
   ```
4. **UI diperbarui:** `WatchlistScreen` membaca state dengan `collectAsStateWithLifecycle()`. Kolom judul jadi kosong dan kartu baru muncul di daftar.

Jika judul kosong, langkah 3 hanya mengisi `titleError = "Judul tidak boleh kosong."`, sehingga kolom judul menjadi merah.

## Screenshot

| Layar sempit (HP tegak) | Layar lebar (HP miring / tablet) |
| --- | --- |
| !(image-1.png) | !(image.png) |
