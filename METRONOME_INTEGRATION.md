# Metronome System — Frontend Integration Guide

Dokumen ini menjelaskan cara tim Frontend mengintegrasikan backend engine, controller audio click, tap tempo, subdivision, dan state management untuk fitur **Metronome**.

---

## 1. Inisialisasi & Controller (`MetronomeController`)

Controller menyediakan interface lengkap untuk mengontrol metronome dan mengamati statusnya via Compose `StateFlow`.

### **Inisialisasi di Jetpack Compose:**
```kotlin
val controller = remember { MetronomeController() }
val state by controller.state.collectAsState()
```

---

## 2. Struktur State (`MetronomeState`)

`state` menyediakan properti real-time berikut:

| Properti | Tipe Data | Keterangan |
|---|---|---|
| `state.status` | `MetronomeStatus` | `STOPPED`, `RUNNING`, `PAUSED` |
| `state.bpm` | `Int` | Tempo aktif (30–300 BPM) |
| `state.tempoMarking` | `String` | Nama tempo (misal: `"Andante"`, `"Allegro"`, `"Moderato"`) |
| `state.timeSignature` | `TimeSignature` | Time signature aktif (misal: `4/4`, `3/4`, `2/4`, `6/8`) |
| `state.currentBeat` | `Int` | Ketukan saat ini dalam bar (misal: 1, 2, 3, 4) |
| `state.currentBar` | `Int` | Hitungan bar yang sedang berjalan |
| `state.isAccentBeat` | `Boolean` | `true` jika ketukan pertama bar (Beat 1 / Accent) |
| `state.subdivision` | `Subdivision` | `QUARTER`, `EIGHTH`, `TRIPLET`, `SIXTEENTH` |
| `state.isMuted` | `Boolean` | Status suara di-mute atau tidak |
| `state.volume` | `Float` | Volume audio (0.0f hingga 1.0f) |

---

## 3. Kontrol Playback (Start, Pause, Resume, Stop)

```kotlin
// Memulai metronom (Beat 1 langsung berbunyi Accent Click)
controller.start()

// Jeda sementara (posisi beat dipertahankan)
controller.pause()

// Melanjutkan dari posisi jeda
controller.resume()

// Menghentikan total & mereset beat ke 1
controller.stop()
```

---

## 4. Pengaturan BPM & Stepper

```kotlin
// Mengatur BPM secara langsung (diklem otomatis di rentang 30-300)
controller.setBpm(120)

// Naikkan BPM (+1 atau +5)
controller.increaseBpm(1)
controller.increaseBpm(5)

// Turunkan BPM (-1 atau -5)
controller.decreaseBpm(1)
controller.decreaseBpm(5)
```

---

## 5. Pengaturan Time Signature & Subdivision

```kotlin
// Mengganti Time Signature (2/4, 3/4, 4/4, 6/8, dll)
controller.setTimeSignature(numerator = 4, denominator = 4)
controller.setTimeSignature(numerator = 6, denominator = 8)

// Mengganti Subdivision ketukan
controller.setSubdivision(Subdivision.QUARTER)   // 1 tick per beat
controller.setSubdivision(Subdivision.EIGHTH)    // 2 ticks per beat
controller.setSubdivision(Subdivision.TRIPLET)   // 3 ticks per beat
controller.setSubdivision(Subdivision.SIXTEENTH) // 4 ticks per beat
```

---

## 6. Tap Tempo

Memungkinkan pengguna menentukan BPM dengan menekan layar mengikuti irama:

```kotlin
// Panggil fungsi ini setiap kali pengguna menekan tombol Tap
val newBpm: Int? = controller.tapTempo()

// Reset riwayat tap tempo jika diperlukan
controller.resetTapTempo()
```

---

## 7. Mute & Volume Control

```kotlin
controller.setMuted(true)   // Mute suara click
controller.setMuted(false)  // Unmute

controller.setVolume(0.8f)  // Atur volume 0.0f - 1.0f
```

---

## 8. Cleanup Lifecycle

```kotlin
DisposableEffect(Unit) {
    onDispose {
        controller.release() // Melepas audio track & menghentikan job
    }
}
```
