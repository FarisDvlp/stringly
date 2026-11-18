# Music Chord & Tabs — Frontend Integration Guide

Dokumen ini menjelaskan cara tim Frontend mengintegrasikan backend lokal, model data, repository, sistem transpose chord, dan controller auto-scroll untuk fitur **Music Chord**.

---

## 1. Repository & Database Lokal (`SongRepository`)

Seluruh data 14 lagu tersimpan dalam bentuk JSON di `app/src/main/assets/songs.json`.

### **Inisialisasi Repository:**
```kotlin
val songRepository: SongRepository = SongRepositoryImpl(context)
```

### **Mendapatkan Semua Lagu:**
```kotlin
val allSongs: List<Song> = songRepository.getAllSongs()
```

### **Mencari Lagu (Case-Insensitive pada Judul & Artis):**
```kotlin
val searchResults: List<Song> = songRepository.searchSongs("payung teduh")
```

### **Mendapatkan Detail Lagu Berdasarkan ID:**
```kotlin
val song: Song? = songRepository.getSongById("song_001")
```

---

## 2. Struktur Model Data (`Song` & `TransposedSong`)

Setiap objek `Song` memiliki properti berikut:
- `song.id`: ID unik lagu.
- `song.title`: Judul lagu.
- `song.artist`: Nama penyanyi/band.
- `song.originalKey`: Key dasar asli lagu (misal: `"E"`, `"C"`, `"G"`).
- `song.tuning`: Jenis tuning gitar (misal: `"Standard (E A D G B E)"`, `"Half-Step Down"`).
- `song.capoFret`: Posisi capo (`Int?`, `null` atau `0` jika tidak pakai capo).
- `song.capoLabel`: String siap tampil di UI (`"No Capo"` jika tanpa capo, atau `"Capo X"` jika menggunakan capo).
- `song.difficulty`: Tingkat kesulitan (`"Easy"`, `"Medium"`, `"Hard"`).
- `song.bpm`: Beats per minute (`Int?`).
- `song.chordSheet`: Struktur lirik & penempatan chord.
- `song.guitarTab`: Tablatur petikan/rhythm (`String?`, `null` jika belum ada).

---

## 3. Sistem Transpose Chord (`ChordTransposer`)

Sistem transpose dapat menaikkan/menurunkan seluruh chord dalam chord sheet tanpa mengubah metadata asli lagu, `tuning`, maupun `capoLabel`.

### **Menaikkan Transpose (+1 Semitone):**
```kotlin
val transposedSong: TransposedSong = ChordTransposer.transposeSong(song, semitones = 1)
```

### **Menurunkan Transpose (-1 Semitone):**
```kotlin
val transposedSong: TransposedSong = ChordTransposer.transposeSong(song, semitones = -1)
```

### **Reset Transpose Kembali ke Original Key:**
```kotlin
val resetSong: TransposedSong = ChordTransposer.resetTranspose(song)
```

### **Akses Hasil Transpose pada `TransposedSong`:**
```kotlin
val currentKey = transposedSong.transposedKey      // e.g. "F#"
val originalKey = transposedSong.originalKey      // e.g. "E"
val capoText = transposedSong.capoLabel           // e.g. "Capo 2" (Tidak berubah)
val tuningText = transposedSong.tuning             // e.g. "Standard" (Tidak berubah)
val newChordSheet = transposedSong.transposedChordSheet // Chord sheet hasil transpose
```

---

## 4. Sistem Auto Scroll (`AutoScrollController`)

Controller auto-scroll berjalan pada Coroutines tanpa membebangi UI thread.

### **Inisialisasi Controller:**
```kotlin
val autoScrollController = remember { AutoScrollController() }
val autoScrollState by autoScrollController.uiState.collectAsState()
```

### **Kontrol Playback Auto Scroll:**
```kotlin
// Jalankan Scroll
autoScrollController.start()

// Jeda (Pause) — Posisi scroll dipertahankan
autoScrollController.pause()

// Lanjutkan (Resume)
autoScrollController.resume()

// Hentikan Total & Reset Offset
autoScrollController.stop()
```

### **Mengubah Kecepatan Scroll (0.5x, 1.0x, 1.5x, 2.0x):**
```kotlin
autoScrollController.setSpeed(1.5f)
```

### **Menghubungkan Delta Scroll ke Compose `ScrollState` / `LazyListState`:**
```kotlin
val scrollState = rememberScrollState()

LaunchedEffect(autoScrollController) {
    autoScrollController.scrollDeltaFlow.collect { deltaPixels ->
        scrollState.scrollBy(deltaPixels)
    }
}
```

### **Cleanup Lifecycle:**
```kotlin
DisposableEffect(Unit) {
    onDispose {
        autoScrollController.release()
    }
}
```
