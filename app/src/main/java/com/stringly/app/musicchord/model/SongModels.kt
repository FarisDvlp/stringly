package com.stringly.app.musicchord.model

data class ChordPlacement(
    val chord: String,
    val position: Int
)

data class ChordLine(
    val chords: List<ChordPlacement>,
    val lyric: String
)

data class ChordSection(
    val name: String,
    val lines: List<ChordLine>
)

data class ChordSheet(
    val sections: List<ChordSection>
)

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val originalKey: String,
    val tuning: String,
    val capoFret: Int?,
    val difficulty: String,
    val bpm: Int? = null,
    val chordSheet: ChordSheet,
    val guitarTab: String? = null
) {
    val capoLabel: String
        get() = if (capoFret == null || capoFret <= 0) "No Capo" else "Capo $capoFret"
}

data class TransposedSong(
    val song: Song,
    val semitones: Int,
    val transposedKey: String,
    val originalKey: String = song.originalKey,
    val capoLabel: String = song.capoLabel,
    val tuning: String = song.tuning,
    val transposedChordSheet: ChordSheet
)
