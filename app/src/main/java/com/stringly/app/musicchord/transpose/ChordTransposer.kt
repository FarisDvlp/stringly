package com.stringly.app.musicchord.transpose

import com.stringly.app.musicchord.model.*
import java.util.Locale

object ChordTransposer {

    private val sharpScale = arrayOf(
        "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
    )

    private val flatScale = arrayOf(
        "C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B"
    )

    private val noteToSemitone = mapOf(
        "C" to 0, "C#" to 1, "DB" to 1,
        "D" to 2, "D#" to 3, "EB" to 3,
        "E" to 4,
        "F" to 5, "F#" to 6, "GB" to 6,
        "G" to 7, "G#" to 8, "AB" to 8,
        "A" to 9, "A#" to 10, "BB" to 10,
        "B" to 11
    )

    fun transposeSingleNote(noteStr: String, semitones: Int, preferFlats: Boolean? = null): String {
        if (noteStr.isEmpty()) return noteStr

        val (rootNote, suffix) = parseNoteAndSuffix(noteStr) ?: return noteStr
        val currentSemitone = noteToSemitone[rootNote.uppercase(Locale.US)] ?: return noteStr

        val newSemitone = ((currentSemitone + semitones) % 12 + 12) % 12
        val isFlatNote = rootNote.length > 1 && (rootNote[1] == 'b' || rootNote[1] == 'B')
        val useFlats = preferFlats ?: isFlatNote
        val scale = if (useFlats) flatScale else sharpScale
        val newNote = scale[newSemitone]

        return "$newNote$suffix"
    }

    fun transposeChord(chord: String, semitones: Int, preferFlats: Boolean? = null): String {
        if (chord.isBlank() || semitones == 0) return chord

        if (chord.contains("/")) {
            val parts = chord.split("/")
            if (parts.size == 2) {
                val transposedRoot = transposeSingleNote(parts[0], semitones, preferFlats)
                val transposedBass = transposeSingleNote(parts[1], semitones, preferFlats)
                return "$transposedRoot/$transposedBass"
            }
        }
        return transposeSingleNote(chord, semitones, preferFlats)
    }

    fun transposeChordSheet(chordSheet: ChordSheet, semitones: Int, preferFlats: Boolean? = null): ChordSheet {
        if (semitones == 0) return chordSheet

        val newSections = chordSheet.sections.map { section ->
            val newLines = section.lines.map { line ->
                val newPlacements = line.chords.map { placement ->
                    placement.copy(chord = transposeChord(placement.chord, semitones, preferFlats))
                }
                line.copy(chords = newPlacements)
            }
            section.copy(lines = newLines)
        }
        return ChordSheet(newSections)
    }

    fun transposeSong(song: Song, semitones: Int, preferFlats: Boolean? = null): TransposedSong {
        val transposedKey = transposeSingleNote(song.originalKey, semitones, preferFlats)
        val transposedSheet = transposeChordSheet(song.chordSheet, semitones, preferFlats)

        return TransposedSong(
            song = song,
            semitones = semitones,
            transposedKey = transposedKey,
            originalKey = song.originalKey,
            capoLabel = song.capoLabel,
            tuning = song.tuning,
            transposedChordSheet = transposedSheet
        )
    }

    fun resetTranspose(song: Song): TransposedSong {
        return transposeSong(song, 0)
    }

    private fun parseNoteAndSuffix(chord: String): Pair<String, String>? {
        if (chord.isEmpty()) return null

        if (chord.length >= 2) {
            val candidate2 = chord.substring(0, 2)
            if (noteToSemitone.containsKey(candidate2.uppercase(Locale.US))) {
                return Pair(candidate2, chord.substring(2))
            }
        }

        val candidate1 = chord.substring(0, 1)
        if (noteToSemitone.containsKey(candidate1.uppercase(Locale.US))) {
            return Pair(candidate1, chord.substring(1))
        }

        return null
    }
}
